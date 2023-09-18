package com.happy.chat.controller;

import static com.happy.chat.constants.Constant.DATA;
import static com.happy.chat.constants.Constant.PERF_ERROR_MODULE;
import static com.happy.chat.constants.Constant.PERF_PAYMENT_MODULE;

import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.JsonSyntaxException;
import com.happy.chat.enums.ErrorEnum;
import com.happy.chat.exception.ServiceException;
import com.happy.chat.service.PaymentService;
import com.happy.chat.service.RobotService;
import com.happy.chat.uitls.ApiResult;
import com.happy.chat.uitls.ObjectMapperUtils;
import com.happy.chat.uitls.PrometheusUtils;
import com.happy.chat.view.PaymentIntentView;
import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Price;
import com.stripe.model.StripeObject;
import com.stripe.net.Webhook;
import com.stripe.param.PaymentIntentCreateParams;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/rest/h/payment")
public class PaymentController {

    private final String defaultPriceId = "price_1NiCirBegHkiPVmE9hDFX5gJ";
    @Autowired
    private PrometheusUtils prometheusUtil;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private RobotService robotService;

    @Value("${stripe.apiKey}")
    private String privateKey;

    @Value("${stripe.webhookSecret}")
    private String endpointSecret;

    // 调用Stripe来产生SessionId
    @PostMapping("/create_payment_intent")
    public Map<String, Object> createPaymentIntent(@RequestParam("ud") String userId,
                                                   @RequestParam("robotId") String robotId) {

        prometheusUtil.perf(PERF_PAYMENT_MODULE, "create_payment_intent_api_enter");

        Stripe.apiKey = privateKey;
        Map<String, Object> result = ApiResult.ofSuccess();
        try {
            // 根据robot拿到它的priceId
            String priceId = robotService.getRobotStripePriceId(robotId);
            if (StringUtils.isEmpty(priceId)) {
                log.warn("{} has no config price", robotId);
                prometheusUtil.perf(PERF_PAYMENT_MODULE, "robot_lack_price_config_" + robotId);
                priceId = defaultPriceId;
            }
            Price price = Price.retrieve(priceId);
            if (price == null) {
                log.error("{} {} price {} null", userId, robotId, priceId);
                prometheusUtil.perf(PERF_PAYMENT_MODULE, "stripe_retrieve_price_failed");
                prometheusUtil.perf(PERF_ERROR_MODULE, "stripe_retrieve_price_failed");
                return ApiResult.ofFail(ErrorEnum.STRIPE_PRICE_RETRIEVE_FAILED);
            }
            PaymentIntent paymentIntent = PaymentIntent.create(PaymentIntentCreateParams.builder()
                    .setCurrency(price.getCurrency())
                    .setAmount(price.getUnitAmount())
                    .build());
            // insert request 失败了怎么弄？？？
            int effect = paymentService.addPayRequest(userId, robotId, paymentIntent.getClientSecret());
            if (effect <= 0) {
                log.error("addPayRequest failed {} {} {}", userId, robotId, ObjectMapperUtils.toJSON(paymentIntent));
                prometheusUtil.perf(PERF_PAYMENT_MODULE, "insert_pay_request_db_failed");
                prometheusUtil.perf(PERF_ERROR_MODULE, "insert_pay_request_db_failed");
                return ApiResult.ofFail(ErrorEnum.SERVER_ERROR);
            }
            result.put(DATA, PaymentIntentView.builder()
                    .requestId(paymentIntent.getClientSecret())
                    .priceId(priceId)
                    .currency(price.getCurrency())
                    .unitAmount(String.valueOf(price.getUnitAmount()))
                    .build());
            prometheusUtil.perf(PERF_PAYMENT_MODULE, "create_payment_intent_success");
            return result;
        } catch (Exception e) {
            log.error("createPaymentIntent exception {} {}", userId, robotId, e);
            prometheusUtil.perf(PERF_PAYMENT_MODULE, "create_payment_intent_exception");
            prometheusUtil.perf(PERF_ERROR_MODULE, "create_payment_intent_exception");
            throw ServiceException.ofMessage(ErrorEnum.STRIPE_CREATE_SESSION_FAILED.getErrCode(), e.getMessage());
        }
    }

    // 回调结果处理
    @RequestMapping("/event_callback")
    public void eventCallback(HttpServletRequest request, HttpServletResponse response) {
        prometheusUtil.perf(PERF_PAYMENT_MODULE, "event_callback_api_enter");

        Stripe.apiKey = privateKey;
        String payload;
        try {
            payload = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
            if (StringUtils.isEmpty(payload)) {
                log.error("event callback payload empty");
                prometheusUtil.perf(PERF_ERROR_MODULE, "callback_payload_empty");
                return;
            }
        } catch (IOException e) {
            log.error("event callback process IOException", e);
            prometheusUtil.perf(PERF_ERROR_MODULE, "callback_payload_ioException");
            return;
        }

        String sigHeader = request.getHeader("Stripe-Signature");
        Event event;
        try {
            event = Webhook.constructEvent(payload, sigHeader, endpointSecret);
        } catch (JsonSyntaxException e) {
            // Invalid payload
            log.error("event callback payload invalid");
            prometheusUtil.perf(PERF_ERROR_MODULE, "callback_payload_invalid");
            return;
        } catch (SignatureVerificationException e) {
            // Invalid signature
            log.error("event callback sig invalid");
            prometheusUtil.perf(PERF_ERROR_MODULE, "callback_sig_invalid");
            return;
        }

        EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();
        StripeObject stripeObject = null;
        if (dataObjectDeserializer.getObject().isPresent()) {
            stripeObject = dataObjectDeserializer.getObject().get();
        } else {
            log.error("event data deserializer failed {}", ObjectMapperUtils.toJSON(event));
            prometheusUtil.perf(PERF_ERROR_MODULE, "callback_webhook_verify_failed");
            return;
        }

        if ("payment_intent.succeeded".equals(event.getType())) {
            PaymentIntent paymentIntent = (PaymentIntent) stripeObject;
            String sessionId = paymentIntent.getClientSecret();
            log.info("handle payment_intent.succeeded event {}", sessionId);

            boolean ok = paymentService.handleUserPaymentSuccess(sessionId);
            if (!ok) {
                log.error("updatePayRequest failed {}", sessionId);
                prometheusUtil.perf(PERF_PAYMENT_MODULE, "event_callback_db_failed");
                prometheusUtil.perf(PERF_ERROR_MODULE, "event_callback_db_failed");
                return;
            }
        } else {
            log.warn("Unhandled event type: " + event.getType());
        }
        prometheusUtil.perf(PERF_PAYMENT_MODULE, "event_callback_api_success");

    }
}
