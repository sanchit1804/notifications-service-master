package com.project.service;

import java.util.List;

import com.project.dto.NotificationDTO;
import com.project.exception.BaseException;
import com.project.model.Notification;

public interface INotificationService {

	void notify(Long userId, NotificationDTO notification) throws BaseException;

	List<Notification> getNotifications(Long userId) throws BaseException;
}
