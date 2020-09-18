package ib.project.service;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ib.project.model.User;
import ib.project.repository.UserRepository;

@Service
public class UserService implements UserServiceInterface {

	@Autowired
	private UserRepository userRepository;
	
	@Override
	public ArrayList<User> findAll() {
		return (ArrayList<User>) userRepository.findAll();
	}
	
	@Override
	public User findByEmail (String email) {
		return userRepository.findByEmail(email);
	}
	
	@Override
	public User findByEmailAndPassword (String email, String password) {
		return userRepository.findByEmailAndPassword(email, password);
	}
	
	
	@Override
	public User save(User user) {
		return userRepository.save(user);
	}
}
