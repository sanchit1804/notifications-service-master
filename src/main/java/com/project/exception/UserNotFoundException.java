package com.project.exception;

public class UserNotFoundException extends BaseException {

	private static final long serialVersionUID = 1L;

	public UserNotFoundException(String errorMsg) {
		super(errorMsg);
	}

	public UserNotFoundException(String errorMsg, Throwable throwable) {
		super(errorMsg, throwable);
	}
}
