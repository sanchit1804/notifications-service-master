package com.project.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.project.exception.BaseException;
import com.project.exception.UserNotFoundException;
import com.project.model.User;
import com.project.repository.IUserRepository;

@Service
public class UserService implements IUserService {

	@Autowired
	private IUserRepository userRepo;

	@Override
	public User createUser(User user) throws BaseException {
		return userRepo.save(user);
	}

	@Override
	public List<User> getUsers() throws BaseException {
		return userRepo.findAll();
	}

	@Override
	public User getUser(Long userId) throws BaseException {
		return userRepo.findById(userId)
				.orElseThrow(() -> new UserNotFoundException("User not found: " + userId));
	}
}
