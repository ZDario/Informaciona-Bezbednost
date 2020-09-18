package ib.project.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import ib.project.model.User;

public interface UserRepository extends JpaRepository<User, Integer> {
	List<User> findAll();
	User findByEmail (String email);
	User findByEmailAndPassword(String email, String password);
}

