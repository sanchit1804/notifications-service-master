package com.project.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import com.project.dto.NotificationDTO;
import com.project.exception.BaseException;
import com.project.exception.NotificationException;
import com.project.exception.UserNotFoundException;
import com.project.model.Notification;
import com.project.model.NotificationStatus;
import com.project.model.User;
import com.project.repository.INotificationRepository;
import com.project.task.EmailNotificationTask;
import com.project.utility.service.EmailService;

@Service
public class NotificationService implements INotificationService {

	private static final Logger LOG = LoggerFactory.getLogger(NotificationService.class);
	private static final String DEFAULT_SUBJECT = "Notification";

	@Autowired
	private IUserService userService;

	@Autowired
	private EmailService emailService;

	@Autowired
	private INotificationRepository notifRepo;

	@Autowired
	private ExecutorService notificationExecutor;

	@Override
	public void notify(Long userId, NotificationDTO notificationDTO) throws BaseException {
		User user = userService.getUser(userId);
		if (user == null) {
			LOG.error("User not found in database");
			throw new UserNotFoundException("User not found in database");
		}
		Notification notification = createNotification(notificationDTO, user);
		notificationExecutor.submit(new EmailNotificationTask(notification, user, emailService, notifRepo));
	}

	private Notification createNotification(NotificationDTO dto, User user) throws BaseException {
		try {
			Notification notif = new Notification();
			notif.setSubject(ObjectUtils.isEmpty(dto.getSubject()) ? DEFAULT_SUBJECT : dto.getSubject());
			notif.setMessage(dto.getMessage());
			notif.setUser(user);
			notif.setStatus(NotificationStatus.PENDING);
			notif.setCreatedAt(LocalDateTime.now());
			return notifRepo.save(notif);
		} catch (Exception ex) {
			throw new NotificationException(ex.getMessage(), ex);
		}
	}

	@Override
	public List<Notification> getNotifications(Long userId) throws NotificationException {
		return notifRepo.findAllByUserId(userId);
	}
}
