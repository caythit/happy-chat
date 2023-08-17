package com.happy.chat.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.happy.chat.annotation.LoginRequired;
import com.happy.chat.uitls.ApiResult;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/rest/h/payment")
public class PaymentController {


    @Value("${stripe.apiKey}")
    private String privateKey;

    @Value("${stripe.webhookSecret}")
    private String endpointSecret;


    // 调用Stripe来产生SessionId
    @LoginRequired
    @RequestMapping("/create_session")
    public Map<String, Object> createSession() {
        Map<String, Object> resultMap = ApiResult.ofSuccess();
        try {
            Stripe.apiKey = privateKey;

            Map<String, Object> params = new HashMap<>();

            List<String> paymentMethodTypes = new ArrayList<>();
            paymentMethodTypes.add("card");
            params.put("payment_method_types", paymentMethodTypes);

            List<HashMap<String, Object>> lineItems = new ArrayList<>();
            HashMap<String, Object> lineItem = new HashMap<>();
            lineItem.put("name", "胡鹏飞测试商品");
            lineItem.put("description", "这是一个测试单描述");
            lineItem.put("amount", 50);
            lineItem.put("currency", "usd");
            lineItem.put("quantity", 1);
            lineItems.add(lineItem);

            params.put("line_items", lineItems);
            params.put("client_reference_id", UUID.randomUUID().toString());//业务系统唯一标识 即订单唯一编号
            params.put("success_url", "https://localhost:8080/rest/o/h/paySuccess");
            params.put("cancel_url", "https://localhost:8080/rest/o/h/payCancel");
//            params.put("customer",  "cus_Gw66rrWnolSXyW");
            Session session = Session.create(params);
            String sessionId = session.getId();
            log.info("sessionId :{}", session.getId());
            resultMap.put("sessionId", sessionId);
        } catch (StripeException e) {
            e.printStackTrace();
        }
        return resultMap;
    }


    // 回调结果处理
    @LoginRequired
    @PostMapping("/event_callback")
    public Map<String, Object> eventCallback(HttpServletRequest request, HttpServletResponse response) {
//        Stripe.apiKey = privateKey;
//        log.info("webhooks begin");
//        InputStream inputStream = request.getInputStream();
//        byte[] bytes = IOUtils.toByteArray(inputStream);
//        String payload = new String(bytes, "UTF-8");
//
//        String sigHeader = request.getHeader("Stripe-Signature");
//        Event event = null;
//        try {
//            event = Webhook.constructEvent(
//                    payload, sigHeader, endpointSecret
//            );
//        } catch (SignatureVerificationException e) {
//            response.setStatus(400);
//            return "";
//        }
//
//        // Deserialize the nested object inside the event
//        EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();
//        StripeObject stripeObject = null;
//        if (dataObjectDeserializer.getObject().isPresent()) {
//            stripeObject = dataObjectDeserializer.getObject().orElse(null);
//        } else {
//            // Deserialization failed, probably due to an API version mismatch.
//            // Refer to the Javadoc documentation on `EventDataObjectDeserializer` for
//            // instructions on how to handle this case, or return an error here.
//        }
//
//        // Handle the event
//        log.info("event.getType{}", event.getType());
//        switch (event.getType()) {
//            case "payment_intent.succeeded":
//                PaymentIntent paymentIntent = (PaymentIntent) stripeObject;
//                System.out.println(paymentIntent);
//                response.setStatus(200);
//                /*退款代码
//                Charge charge = paymentIntent.getCharges().getData().get(0);
//                Refund refund = Refund.create(RefundCreateParams.builder()
//                        .setCharge(charge.getId())
//                        .setAmount(500L)
//                        .build());*/
//                break;
//            case "charge.succeeded":
//                //使用token支付成功回调
//                Charge charge = (Charge) stripeObject;
//                System.out.println(charge);
//                //TODO 此时根据charge ID 查询出关联的订单并处理支付成功业务代码
//                response.setStatus(200);
//                break;
//            case "checkout.session.completed":
//                //使用checkout支付成功回调
//                Session session = (Session) stripeObject;
//                System.out.println(session);
//                String paymentIntentId = session.getPaymentIntent();
//                String chargeId = PaymentIntent.retrieve(paymentIntentId).getCharges().getData().get(0).getId();
//                System.out.println(session.getClientReferenceId());//订单编号
//                //TODO 请处理支付成功业务代码 并将charge id 关联在订单
//                response.setStatus(200);
//                break;
//            default:
//                response.setStatus(400);
//                return "";
//        }
//        response.setStatus(200);
//        return "";
        return ApiResult.ofSuccess();
    }
}
