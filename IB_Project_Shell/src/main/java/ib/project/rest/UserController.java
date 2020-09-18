package ib.project.rest;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ib.project.model.Authority;
import ib.project.model.User;
import ib.project.service.AuthorityService;
import ib.project.service.UserService;

@RestController
@RequestMapping(value="api/users")
public class UserController {

	@Autowired
	public UserService userService;
	@Autowired
	public AuthorityService authorityService;
	
	
	@GetMapping(path="/")
	public ArrayList<User> findAll() {
		return userService.findAll();
	}
	
	@GetMapping(path="user/email")
	public ResponseEntity<User> userEmail(@RequestParam String email) {
		User user = userService.findByEmail(email);
		if (user != null) {
			return new ResponseEntity<User>(user,HttpStatus.OK);
		} else {
			System.out.println("User sa dati e-mailom nije pronadjen.");
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}
	
	@PostMapping(path="user/login")
	public ResponseEntity<User> loginUser(@RequestParam String email, @RequestParam String password) {
		User user = userService.findByEmailAndPassword(email, password);
		try {
			return new ResponseEntity<User>(user, HttpStatus.OK);
		} catch (Exception e) {
			System.out.println("User sa datim e-mailom i sifrom nije pronadjen.");
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}
	
	
	@PostMapping(path="user/registration")
	public ResponseEntity<User> registrationUser(@RequestParam String email, @RequestParam String password) {
		Authority auth = authorityService.findByName("Regular");
		User user = new User();
		User checkUser = userService.findByEmail(email);
		if (checkUser == null) {
			user.setActive(false);
			user.setAuthority(auth);
			user.setCertificate("");
			user.setEmail(email);
			user.setPassword(password);
			
			userService.save(user);
			return new ResponseEntity<User>(user,HttpStatus.CREATED);
		}else {
			System.out.println("Uneli ste vec postojeci e-mail.");
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}
}