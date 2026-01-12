package com.internship.crypto_tracker.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.internship.crypto_tracker.model.RiskAlert;
import com.internship.crypto_tracker.model.User;

@Service
public class NotificationService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendRiskNotification(User user, RiskAlert alert) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();

            message.setFrom(fromEmail);
            
            message.setTo(user.getEmail());
            message.setSubject("🚨 URGENT: Risk Detected - " + alert.getAssetSymbol());
            
            String body = "Dear " + user.getName() + ",\n\n" +
                    "WARNING: We have detected a high-risk asset in your portfolio.\n" +
                    "Token: " + alert.getAssetSymbol() + "\n" +
                    "Risk Type: " + alert.getAlertType() + "\n" +
                    "Details: " + alert.getDetails() + "\n\n" +
                    "Please investigate immediately.\n" +
                    "- Crypto Tracker Security Team";

            message.setText(body);

            mailSender.send(message);
            System.out.println("✅ Email sent successfully to " + user.getEmail());

        } catch (Exception e) {
            System.err.println("❌ Failed to send email: " + e.getMessage());
        }
    }
}