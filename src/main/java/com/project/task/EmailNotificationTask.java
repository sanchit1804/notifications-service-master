package com.project.task;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.project.exception.EmailException;
import com.project.model.Notification;
import com.project.model.NotificationStatus;
import com.project.model.User;
import com.project.repository.INotificationRepository;
import com.project.utility.service.EmailService;

/**
 * Runnable unit of work: send one notification's email, then persist the
 * outcome (SENT/FAILED) back onto the same row that was saved as PENDING.
 * Instances of this are handed to the ExecutorService bean rather than
 * relying on Spring's @Async, per the explicit threading requirement.
 */
public class EmailNotificationTask implements Runnable {

	private static final Logger LOG = LoggerFactory.getLogger(EmailNotificationTask.class);

	private final Notification notification;
	private final User user;
	private final EmailService emailService;
	private final INotificationRepository notificationRepository;

	public EmailNotificationTask(Notification notification, User user, EmailService emailService,
			INotificationRepository notificationRepository) {
		this.notification = notification;
		this.user = user;
		this.emailService = emailService;
		this.notificationRepository = notificationRepository;
	}

	@Override
	public void run() {
		LOG.info("[{}] sending notification id={} to {}", Thread.currentThread().getName(), notification.getId(),
				user.getEmail());
		try {
			emailService.sendEmail(user.getEmail(), notification.getSubject(), notification.getMessage());
			notification.setStatus(NotificationStatus.SENT);
			notification.setSentAt(LocalDateTime.now());
		} catch (EmailException ex) {
			LOG.error("Notification id={} failed: {}", notification.getId(), ex.getMessage());
			notification.setStatus(NotificationStatus.FAILED);
			notification.setFailureReason(ex.getMessage());
		} finally {
			notificationRepository.save(notification);
		}
	}
}
