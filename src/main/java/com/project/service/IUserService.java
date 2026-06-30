package com.project.service;

import java.util.List;

import com.project.exception.BaseException;
import com.project.model.User;

public interface IUserService {

	User createUser(User user) throws BaseException;

	List<User> getUsers() throws BaseException;

	User getUser(Long userId) throws BaseException;
}
