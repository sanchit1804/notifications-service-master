package com.project.utility.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.project.exception.EmailException;

@Service
public class EmailService {

	private static final Logger LOG = LoggerFactory.getLogger(EmailService.class);

	@Value("${spring.mail.username}")
	private String senderMail;

	@Autowired
	private JavaMailSender javaMailSender;

	public void sendEmail(String toEmail, String subject, String message) throws EmailException {
		LOG.info("Sending mail to: {}", toEmail);
		SimpleMailMessage mail = new SimpleMailMessage();
		mail.setTo(toEmail);
		mail.setFrom(senderMail);
		mail.setSubject(subject);
		mail.setText(message);
		try {
			javaMailSender.send(mail);
			LOG.info("Mail sent to {}", toEmail);
		} catch (MailException ex) {
			LOG.error("Mail not sent to {}. Error={}", toEmail, ex.getMessage());
			throw new EmailException(ex.getMessage(), ex);
		}
	}
}
