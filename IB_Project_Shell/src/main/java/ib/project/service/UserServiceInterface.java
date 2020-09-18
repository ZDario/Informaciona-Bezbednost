package ib.project.service;

import java.util.List;

import ib.project.model.User;

public interface UserServiceInterface {
	List<User> findAll();
	User findByEmail(String username);
	User findByEmailAndPassword (String email, String password);
	User save(User user);
}
