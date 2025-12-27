package com.vuxnye.coffeeshop.util;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class EmailService {

    private static final String SENDER_EMAIL = "longvuong01cs@gmail.com";
    private static final String SENDER_PASSWORD = "dwwgsjpbwfwakrha";

    public static void sendEmail(String recipientEmail, String subject, String content){
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SENDER_EMAIL, SENDER_PASSWORD);
            }
        });
        try {
            // Tạo nội dung thư
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(SENDER_EMAIL));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            message.setSubject(subject);

            // Nội dung HTML cho đẹp
            message.setContent(content, "text/html; charset=utf-8");

            // Gửi thư
            Transport.send(message);
            System.out.println("Gửi email thành công tới: " + recipientEmail);

        } catch (MessagingException e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi gửi email: " + e.getMessage());
        }
    }
}
