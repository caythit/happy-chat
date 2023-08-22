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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.JsonSyntaxException;
import com.happy.chat.enums.ErrorEnum;
import com.happy.chat.enums.PaymentState;
import com.happy.chat.exception.ServiceException;
import com.happy.chat.model.CheckoutPayment;
import com.happy.chat.service.PaymentService;
import com.happy.chat.uitls.ApiResult;
import com.happy.chat.uitls.ObjectMapperUtils;
import com.happy.chat.uitls.PrometheusUtils;
import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/rest/h/payment")
public class PaymentController {

    @Autowired
    private PrometheusUtils prometheusUtil;

    @Autowired
    private PaymentService paymentService;

    @Value("${stripe.apiKey}")
    private String privateKey;

    @Value("${stripe.webhookSecret}")
    private String endpointSecret;


    // 调用Stripe来产生SessionId
    @RequestMapping("/create_checkout_session")
    public Map<String, Object> createCheckoutSession(@RequestBody CheckoutPayment payment) {
        // 类似于price_1NhPYZBegHkiPVmEEH5lxC8Q
        Stripe.apiKey = privateKey;
        Map<String, Object> result = ApiResult.ofSuccess();
        try {
            SessionCreateParams params =
                    SessionCreateParams.builder()
                            .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
                            .setMode(SessionCreateParams.Mode.PAYMENT)
                            .setSuccessUrl(payment.getSuccessUrl())
                            .setCancelUrl(payment.getCancelUrl())
                            .setAutomaticTax(
                                    SessionCreateParams.AutomaticTax.builder()
                                            .setEnabled(true)
                                            .build())
                            .addLineItem(
                                    SessionCreateParams.LineItem.builder()
                                            .setQuantity(payment.getQuantity())
                                            // Provide the exact Price ID (for example, pr_1234) of the product you want to sell
                                            .setPrice(payment.getPriceId())
                                            .build())
                            .build();
            Session session = Session.create(params);
            // insert request 失败了怎么弄？？？
            int effect = paymentService.addPayRequest(payment, session);
            if (effect <= 0) {
                log.error("addPayRequest failed {}", ObjectMapperUtils.toJSON(payment));
                prometheusUtil.perf("stripe_create_checkout_session_failed_by_db_" + payment.getPriceId());
                return ApiResult.ofFail(ErrorEnum.SERVER_ERROR);
            }
            result.put(DATA, session.getId());
            return result;
        } catch (Exception e) {
            log.error("createCheckoutSession exception", e);
            prometheusUtil.perf("stripe_create_checkout_session_exception_" + payment.getPriceId());
            throw ServiceException.ofMessage(ErrorEnum.STRIPE_CREATE_CHECKOUT_SESSION_FAILED.getErrCode(), e.getMessage());
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
                prometheusUtil.perf("stripe_webhook_failed_by_db");
            }
        } else {
            log.warn("Unhandled event type: " + event.getType());
        }
    }
}
