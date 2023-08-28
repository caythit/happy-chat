package com.happy.chat.controller;

import static com.happy.chat.constants.Constant.DATA;

import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.JsonSyntaxException;
import com.happy.chat.enums.ErrorEnum;
import com.happy.chat.exception.ServiceException;
import com.happy.chat.model.CheckoutRequest;
import com.happy.chat.service.PaymentService;
import com.happy.chat.service.RobotService;
import com.happy.chat.uitls.ApiResult;
import com.happy.chat.uitls.ObjectMapperUtils;
import com.happy.chat.uitls.PrometheusUtils;
import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Price;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.checkout.SessionCreateParams;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/rest/h/payment")
public class PaymentController {

    private final String defaultPriceId = "price_1NWM8CBegHkiPVmE5HIUDs43";
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
    public Map<String, Object> createPaymentIntent(@RequestParam("userId") String userId,
                                                   @RequestParam("robotId") String robotId) {
        Stripe.apiKey = privateKey;
        Map<String, Object> result = ApiResult.ofSuccess();
        try {
            // 根据robot拿到它的priceId
            String priceId = robotService.getRobotStripePriceId(robotId);
            if (StringUtils.isEmpty(priceId)) {
                log.warn("{} has no config price", robotId);
                priceId = defaultPriceId;
            }
            Price price = Price.retrieve(priceId);
            if (price == null) {
                log.error("price {} null", priceId);
                prometheusUtil.perf("stripe_price_retrieve_failed_" + priceId);
                return ApiResult.ofFail(ErrorEnum.STRIPE_PRICE_RETRIEVE_FAILED);
            }
            PaymentIntent paymentIntent = PaymentIntent.create(PaymentIntentCreateParams.builder()
                    .setAutomaticPaymentMethods(
                            PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                    .setEnabled(true)
                                    .build()
                    )
                    .addPaymentMethodType("card")
                    .setCurrency(price.getCurrency())
                    .setAmount(price.getUnitAmount())
                    .build());
            // insert request 失败了怎么弄？？？
            int effect = paymentService.addPayRequest(userId, robotId, paymentIntent.getId());
            if (effect <= 0) {
                log.error("addPayRequest failed {} {} {}", userId, robotId, ObjectMapperUtils.toJSON(paymentIntent));
                prometheusUtil.perf("stripe_add_pay_request_failed_by_db_" + robotId);
                return ApiResult.ofFail(ErrorEnum.SERVER_ERROR);
            }
            result.put(DATA, ObjectMapperUtils.toJSON(paymentIntent));
            return result;
        } catch (Exception e) {
            log.error("createCheckoutSession exception {} {}", userId, robotId, e);
            prometheusUtil.perf("stripe_create_request_exception_" + robotId);
            throw ServiceException.ofMessage(ErrorEnum.STRIPE_CREATE_SESSION_FAILED.getErrCode(), e.getMessage());
        }
    }

    // 调用Stripe来产生SessionId
    @RequestMapping("/create_checkout_session")
    @Deprecated
    public Map<String, Object> createCheckoutSession(@RequestBody CheckoutRequest request) {
        // 类似于price_1NhPYZBegHkiPVmEEH5lxC8Q
        Stripe.apiKey = privateKey;
        Map<String, Object> result = ApiResult.ofSuccess();
        try {
            SessionCreateParams params =
                    SessionCreateParams.builder()
                            .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
                            .setMode(SessionCreateParams.Mode.PAYMENT)
                            .setSuccessUrl(request.getSuccessUrl())
                            .setCancelUrl(request.getCancelUrl())
                            .setAutomaticTax(
                                    SessionCreateParams.AutomaticTax.builder()
                                            .setEnabled(true)
                                            .build())
                            .addLineItem(
                                    SessionCreateParams.LineItem.builder()
                                            .setQuantity(request.getQuantity())
                                            // Provide the exact Price ID (for example, pr_1234) of the product you want to sell
                                            .setPrice(request.getPriceId())
                                            .build())
                            .build();
            Session session = Session.create(params);
            // insert request 失败了怎么弄？？？
            int effect = paymentService.addPayRequest(request, session);
            if (effect <= 0) {
                log.error("addPayRequest failed {}", ObjectMapperUtils.toJSON(request));
                prometheusUtil.perf("stripe_create_checkout_session_failed_by_db_" + request.getPriceId());
                return ApiResult.ofFail(ErrorEnum.SERVER_ERROR);
            }
            result.put(DATA, session.getId());
            return result;
        } catch (Exception e) {
            log.error("createCheckoutSession exception", e);
            prometheusUtil.perf("stripe_create_checkout_session_exception_" + request.getPriceId());
            throw ServiceException.ofMessage(ErrorEnum.STRIPE_CREATE_SESSION_FAILED.getErrCode(), e.getMessage());
        }
    }


    // 回调结果处理
    @RequestMapping("/event_callback")
    public void eventCallback(HttpServletRequest request, HttpServletResponse response) {
        Stripe.apiKey = privateKey;
        String payload;
        try {
            payload = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
            if (StringUtils.isEmpty(payload)) {
                log.error("event callback payload empty");
                prometheusUtil.perf("stripe_event_callback_payload_empty");
                return;
            }
        } catch (IOException e) {
            log.error("event callback process IOException", e);
            prometheusUtil.perf("stripe_event_callback_ioException");
            return;
        }

        String sigHeader = request.getHeader("Stripe-Signature");
        Event event;
        try {
            event = Webhook.constructEvent(payload, sigHeader, endpointSecret);
        } catch (JsonSyntaxException e) {
            // Invalid payload
            log.error("event callback payload invalid");
            prometheusUtil.perf("stripe_event_callback_payload_invalid");
            return;
        } catch (SignatureVerificationException e) {
            // Invalid signature
            log.error("event callback sig invalid");
            prometheusUtil.perf("stripe_event_callback_sig_invalid");
            return;
        }

        EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();
        StripeObject stripeObject = null;
        if (dataObjectDeserializer.getObject().isPresent()) {
            stripeObject = dataObjectDeserializer.getObject().get();
        } else {
            log.error("event data deserializer failed {}", ObjectMapperUtils.toJSON(event));
            prometheusUtil.perf("stripe_webhook_verify_failed");
            return;
        }

        // todo update payment state，chat go
        if ("checkout.session.async_payment_succeeded".equals(event.getType())) {
            log.info("handle checkout.session.async_payment_succeeded event");

            Session session = (Session) stripeObject;
            String sessionId = session.getId();
            boolean ok = paymentService.handleUserPaymentSuccess(sessionId);
            if (!ok) {
                log.error("updatePayRequest failed {}", sessionId);
                prometheusUtil.perf("stripe_webhook_failed");
            }
        } else {
            log.warn("Unhandled event type: " + event.getType());
        }
    }
}
