package com.project.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.project.exception.BaseException;
import com.project.model.User;
import com.project.service.IUserService;

@RestController
public class UserController {

	@Autowired
	private IUserService userService;

	@PostMapping("/users")
	public ResponseEntity<User> createUser(@RequestBody User user) throws BaseException {
		if (ObjectUtils.isEmpty(user) || ObjectUtils.isEmpty(user.getEmail())) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		User saved = userService.createUser(user);
		return new ResponseEntity<>(saved, HttpStatus.CREATED);
	}

	@GetMapping("/users")
	public ResponseEntity<List<User>> getUsers() throws BaseException {
		List<User> users = userService.getUsers();
		if (!ObjectUtils.isEmpty(users)) {
			return new ResponseEntity<>(users, HttpStatus.OK);
		}
		return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	}
}
