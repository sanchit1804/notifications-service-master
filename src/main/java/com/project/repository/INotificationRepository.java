package com.project.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.project.model.Notification;

@Repository
public interface INotificationRepository extends JpaRepository<Notification, Long> {

	List<Notification> findAllByUserId(Long userId);
}
