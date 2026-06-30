package com.project.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.project.dto.NotificationDTO;
import com.project.exception.BaseException;
import com.project.model.Notification;
import com.project.service.INotificationService;

@RestController
public class NotificationController {

	@Autowired
	private INotificationService notificationService;

	@PostMapping("/notify/user/{userId}")
	public ResponseEntity<String> notifyUser(@PathVariable("userId") Long userId,
			@RequestBody NotificationDTO notification) throws BaseException {
		if (userId == null || ObjectUtils.isEmpty(notification) || ObjectUtils.isEmpty(notification.getMessage())) {
			return new ResponseEntity<>("userId and message are required", HttpStatus.BAD_REQUEST);
		}
		notificationService.notify(userId, notification);
		return new ResponseEntity<>("Notification queued", HttpStatus.ACCEPTED);
	}

	@GetMapping("/get/user/{userId}/notification")
	public ResponseEntity<List<Notification>> getNotifications(@PathVariable("userId") Long userId)
			throws BaseException {
		List<Notification> notifList = notificationService.getNotifications(userId);
		if (!ObjectUtils.isEmpty(notifList)) {
			return new ResponseEntity<>(notifList, HttpStatus.OK);
		}
		return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	}
}
