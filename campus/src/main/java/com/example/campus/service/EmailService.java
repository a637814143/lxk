package com.example.campus.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * Simple email sender with graceful degradation when mail sender is not configured.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class
EmailService {

    private final ObjectProvider<JavaMailSender> mailSenderProvider;

    @Value("${spring.mail.from:}")
    private String defaultFrom;

    public void sendPlainText(String to, String subject, String content) {
        if (!StringUtils.hasText(to)) {
            log.warn("Email not sent: recipient is blank. Subject={}", subject);
            return;
        }
        JavaMailSender sender = mailSenderProvider.getIfAvailable();
        if (sender == null) {
            log.warn("Email sender not configured, skip sending email to {}", to);
            return;
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            if (StringUtils.hasText(defaultFrom)) {
                message.setFrom(defaultFrom);
            }
            message.setTo(to);
            message.setSubject(subject);
            message.setText(content);
            sender.send(message);
            log.info("Invitation email sent to {}", to);
        } catch (Exception ex) {
            log.error("Failed to send email to {}: {}", to, ex.getMessage(), ex);
        }
    }
}
