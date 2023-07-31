package com.happy.chat.helper;

import static com.happy.chat.uitls.CommonUtils.decryptPwd;
import static com.happy.chat.uitls.CommonUtils.randomMailCode;
import static com.happy.chat.uitls.PrometheusUtils.perf;

import java.util.Properties;
import java.util.concurrent.TimeUnit;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.happy.chat.domain.User;
import com.happy.chat.enums.ErrorEnum;
import com.happy.chat.model.UserGetRequest;
import com.happy.chat.service.UserService;
import com.happy.chat.uitls.CacheKeyProvider;
import com.happy.chat.uitls.CommonUtils;
import com.happy.chat.uitls.RedisUtil;

import io.prometheus.client.CollectorRegistry;

@Lazy
@Component
public class EmailHelper {
    private final String prometheusName = "email";
    private final String prometheusHelp = "email_operation";

    @Autowired
    private CollectorRegistry emailRegistry;
    @Value("${com.flirtopia.mail.user}")
    private String mailUser;
    @Value("${com.flirtopia.mail.salt}")
    private String mailPwdSalt;
    @Value("${com.flirtopia.mail.encry-pwd}")
    private String mailEncryPwd;
    @Value("${com.flirtopia.mail.from}")
    private String mailFrom;

    @Autowired
    private UserService userService;

    @Autowired
    private RedisUtil redisUtil;

    public ErrorEnum sendCode(String email, String subject, String text, boolean checkEmailBind, String purpose) {
        // check email 格式
        boolean emailValid = CommonUtils.emailPatternValid(email);
        if (!emailValid) {
            perf(emailRegistry, prometheusName, prometheusHelp, "send_email_failed_by_pattern_invalid", email, purpose);
            return ErrorEnum.EMAIL_PATTERN_INVALID;
        }

        // check email 是否被使用，必须是使用状态
        if (checkEmailBind) {
            User user = userService.getUser(UserGetRequest.builder()
                    .email(email)
                    .build());
            if (user == null) {
                perf(emailRegistry, prometheusName, prometheusHelp, "send_email_failed_by_not_exist", email, purpose);
                return ErrorEnum.EMAIL_NOT_EXIST;
            }
        }
        try {
            String code = String.valueOf(randomMailCode());
            // 5分钟有效
            redisUtil.set(CacheKeyProvider.mailCodeKey(email), code, 5, TimeUnit.MINUTES);
            text = text + "\n" + code;
            sendByTSL(mailUser, decryptPwd(mailPwdSalt, mailEncryPwd), mailFrom, email, subject, text);
            perf(emailRegistry, prometheusName, prometheusHelp, "send_email_success", email, purpose);
            return ErrorEnum.SUCCESS;
        } catch (Exception e) {
            perf(emailRegistry, prometheusName, prometheusHelp, "send_email_failed_by_exception", email, purpose);
            return ErrorEnum.EMAIL_SEND_CODE_FAIL;
        }
    }

    public ErrorEnum verifyCode(String email, String emailVerifyCode, String purpose) {
        String code = redisUtil.get(CacheKeyProvider.mailCodeKey(email));
        if (StringUtils.isEmpty(code)) {
            perf(emailRegistry, prometheusName, prometheusHelp, "verify_email_failed_expire", email, purpose);
            return ErrorEnum.EMAIL_VERIFY_CODE_EXPIRE;
        }
        if (!emailVerifyCode.equals(code)) {
            perf(emailRegistry, prometheusName, prometheusHelp, "verify_email_failed_error", email, purpose);
            return ErrorEnum.EMAIL_VERIFY_CODE_ERROR;
        }
        perf(emailRegistry, prometheusName, prometheusHelp, "verify_email_failed_success", email, purpose);
        return ErrorEnum.SUCCESS;

    }

    private void sendByTSL(String mailUser, String mailPwd, String from, String to,
                           String subject, String text) throws MessagingException {
        Properties prop = new Properties();
        prop.put("mail.smtp.host", "smtp.gmail.com");
        prop.put("mail.smtp.port", "587");
        prop.put("mail.smtp.auth", "true");
        prop.put("mail.smtp.starttls.enable", "true"); //TLS

        Session session = Session.getInstance(prop,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(mailUser, mailPwd);
                    }
                });
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(from));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        message.setSubject(subject);
        message.setText(text);

        Transport.send(message);
    }

    private void sendBySSL(String mailUser, String mailPwd, String from, String to,
                           String subject, String text) throws MessagingException {
        Properties prop = new Properties();
        prop.put("mail.smtp.host", "smtp.gmail.com");
        prop.put("mail.smtp.port", "465");
        prop.put("mail.smtp.auth", "true");
        prop.put("mail.smtp.socketFactory.port", "465");
        prop.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");

        Session session = Session.getInstance(prop,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(mailUser, mailPwd);
                    }
                });


        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(from));
        message.setRecipients(
                Message.RecipientType.TO,
                InternetAddress.parse(to)
        );
        message.setSubject(subject);
        message.setText(text);

        Transport.send(message);


    }
}
