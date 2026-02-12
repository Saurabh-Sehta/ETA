package com.Saurabh.ETA.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.properties.mail.smtp.from}")
    private String fromEmail;

    public void sendWelcomeEmail(String toEmail, String name) throws MessagingException {
        Context context = new Context();
        context.setVariable("productName", "Expense Tracking Application");
        context.setVariable("name", name);
        context.setVariable("loginUrl", "http://localhost:5173/login");

        String process = templateEngine.process("welcome-email", context);
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);

        helper.setFrom(fromEmail);
        helper.setTo(toEmail);
        helper.setSubject("Welcome to our family");
        helper.setText(process, true);

        mailSender.send(message);
    }

//    public void sendResetOtpEmail(String toEmail, String otp) {
//        SimpleMailMessage message = new SimpleMailMessage();
//        message.setFrom(fromEmail);
//        message.setTo(toEmail);
//        message.setSubject("Password Reset OTP");
//        message.setText("Your OTP for reseting your password is " + otp + ". Use this OTP to proceed with resetting your password.");
//        mailSender.send(message);
//    }
//
//    public void sendOtpEmail(String toEmail, String otp){
//        SimpleMailMessage message = new SimpleMailMessage();
//        message.setFrom(fromEmail);
//        message.setTo(toEmail);
//        message.setSubject("Account verifiation OTP");
//        message.setText("Your OTP is " + otp + ". Verify your account using this OTP.");
//        mailSender.send(message);
//    }

    public void sendOtpEmail(String toEmail, String otp) throws MessagingException {
        Context context = new Context();
        context.setVariable("name", "Sir/Mam");
        context.setVariable("otp",otp);
        context.setVariable("expiryMinutes", "10");

        String process = templateEngine.process("verify-email", context);
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage);

        helper.setFrom(fromEmail);
        helper.setTo(toEmail);
        helper.setSubject("Account verification OTP");
        helper.setText(process, true);

        mailSender.send(mimeMessage);
    }

    public void sendResetOtpEmail(String toEmail, String name, Long otp) throws MessagingException {
        Context context = new Context();
        context.setVariable("name", name);
        context.setVariable("otp",otp);
        context.setVariable("expiryMinutes", "15");

        String process = templateEngine.process("password-reset-email", context);
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage);

        helper.setFrom(fromEmail);
        helper.setTo(toEmail);
        helper.setSubject("Reset Password OTP");
        helper.setText(process, true);

        mailSender.send(mimeMessage);
    }
}
