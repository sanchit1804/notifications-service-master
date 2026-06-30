package com.project.config;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Provides the thread pool used to send emails in the background.
 * NotificationService submits Runnable tasks here instead of relying on
 * Spring's @Async, so the threading is explicit and easy to tune/monitor.
 */
@Configuration
public class ExecutorConfig {

	@Value("${notification.executor.pool-size:10}")
	private int poolSize;

	@Bean(destroyMethod = "shutdown")
	public ExecutorService notificationExecutor() {
		return Executors.newFixedThreadPool(poolSize);
	}
}
