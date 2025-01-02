package com.inn.cafe.serviceImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.google.common.base.Strings;
import com.inn.cafe.JWT.CustomerUserDetailsService;
import com.inn.cafe.JWT.JwtFilter;
import com.inn.cafe.JWT.JwtUtil;
import com.inn.cafe.POJO.User;
import com.inn.cafe.constants.CafeConstants;
import com.inn.cafe.dao.UserDao;
import com.inn.cafe.service.UserService;
import com.inn.cafe.utils.CafeUtils;
import com.inn.cafe.utils.EmailUtils;
import com.inn.cafe.wrapper.UserWrapper;


import org.springframework.security.crypto.password.PasswordEncoder;


import lombok.extern.slf4j.Slf4j;


@Slf4j

@Service
public class UserServiceImpl implements UserService{
	
	@Autowired
    AuthenticationManager authenticationManager;
	
	@Autowired
	CustomerUserDetailsService customerUserDetailsService;
	
	@Autowired
	UserDao userDao;
	
	@Autowired
	JwtUtil jwtUtil;
	
	@Autowired
	JwtFilter jwtFilter;
	
	@Autowired
    private PasswordEncoder passwordEncoder;
	
	@Autowired
	EmailUtils emailUtils;

	
	@Override
	public ResponseEntity<String> signUp(Map<String, String> requestMap) {
		
		log.info("UserServiceImpl || signup method Invoked || "+requestMap);
		try {
		if(validateSignUpMap(requestMap)) {
			User user = userDao.findByEmailId(requestMap.get("email"));
			if(Objects.isNull(user)) {
				userDao.save(getUserFromMap(requestMap));
				log.info("UserServiceImpl || signup || User has been Successfully Registered.");
				return CafeUtils.getResponseEntity("Successfully Registered.", HttpStatus.OK);
			}
			else {
				
				log.info("UserServiceImpl || signup || Email already exists.");
				return CafeUtils.getResponseEntity("Email already exists.", HttpStatus.BAD_REQUEST);
			}
		}
		else {
			log.info("UserServiceImpl || signup || Invalid Data");
			return CafeUtils.getResponseEntity(CafeConstants.INVALID_DATA, HttpStatus.BAD_REQUEST);
		}
		
		}
		catch (Exception ex) {
			//ex.printStackTrace();
			log.info("UserServiceImpl || signup Exception Occured"+ ex);
		}
		
		log.info("UserServiceImpl || signup method Leaving");
		return CafeUtils.getResponseEntity(CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
		
		
	}
	
	private boolean validateSignUpMap(Map<String, String> requestMap) {
		log.info("UserServiceImpl || validateSignUpMap method Invoked");
		
		if(requestMap.containsKey("name") && requestMap.containsKey("contactNumber")
		&& requestMap.containsKey("email") && requestMap.containsKey("password")) {
			log.info("UserServiceImpl || validateSignUpMap method Leaving || Validation Successfull");
			return true;
		}
		
		log.info("UserServiceImpl || validateSignUpMap method Leaving || Validation Unsuccessfull");
		return false;
	}
	
	private User getUserFromMap(Map<String, String> requestMap){
		User user = new User();
		user.setName(requestMap.get("name"));
		user.setContactNumber(requestMap.get("contactNumber"));
		user.setEmail(requestMap.get("email"));
		user.setPassword(passwordEncoder.encode(requestMap.get("password")));
		user.setStatus("false");
		user.setRole("user");
		return user;
		
	}

	@Override
	public ResponseEntity<String> login(Map<String, String> requestMap) {
		log.info("UserServiceImpl || login method Invoked ");
		try {
			Authentication auth = authenticationManager.authenticate(
	                new UsernamePasswordAuthenticationToken(requestMap.get("email"),requestMap.get("password"))
	            );
	
			if(auth.isAuthenticated()) {
			
				if(customerUserDetailsService.getUserDetail().getStatus().equalsIgnoreCase("true")) {
					 // Generate JWT token
		            String token = jwtUtil.generateToken(
		                customerUserDetailsService.getUserDetail().getEmail(),
		                customerUserDetailsService.getUserDetail().getRole()
		            );
		            log.info("Token yha hai |"+token);
		            
		         // Create JSON response
		            String jsonResponse = "{\"token\":\"" + token + "\"}";

		            // Return response entity with the JSON token and HTTP status OK
		            return new ResponseEntity<String>(jsonResponse, HttpStatus.OK);
					}
				else {
					return new ResponseEntity<String>("{\"message\":\""+"Wait for admin approval."+"\"}",
							HttpStatus.BAD_REQUEST);
				}
			}
		}
		catch (AuthenticationException  e) {
			log.error("{}",e);
		}
		
		return new ResponseEntity<String>("{\"message\":\""+"Bad Credentials."+"\"}",
				HttpStatus.BAD_REQUEST);

	}

	@Override
	public ResponseEntity<List<UserWrapper>> getAllUser() {
		try {
			
			if(jwtFilter.isAdmin()) {
				return new ResponseEntity<>(userDao.getAllUser(), HttpStatus.OK);
			}
			else {
				return new ResponseEntity<>(new ArrayList<>(), HttpStatus.UNAUTHORIZED);
			}
			
			
		}catch(Exception e) {
			e.printStackTrace();
		}
		return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@Override
	public ResponseEntity<String> update(Map<String, String> requestMap) {
		try {
			if(jwtFilter.isAdmin()) {
			Optional<User> optional= userDao.findById(Integer.parseInt(requestMap.get("id")));
			    if(!optional.isEmpty()) {
				   userDao.updateStatus(requestMap.get("status"), Integer.parseInt(requestMap.get("id")));
				   sendMailToAllAdmin(requestMap.get("status"), optional.get().getEmail(), userDao.getAllAdmin());
				   return CafeUtils.getResponseEntity("User Status updated Successfully", HttpStatus.OK);
			    }else {
				return CafeUtils.getResponseEntity("User id doesn't exist", HttpStatus.OK);
			    }
			}else {
				return CafeUtils.getResponseEntity(CafeConstants.UNAUTHORIZED_ACCESS, HttpStatus.UNAUTHORIZED);
			}
			
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return CafeUtils.getResponseEntity(CafeConstants.SOMETHING_WENT_WRONG ,HttpStatus.INTERNAL_SERVER_ERROR);
	}

	private void sendMailToAllAdmin(String status, String user, List<String> allAdmin) {
		allAdmin.remove(jwtFilter.getCurrentUser());
		if(status!=null && status.equalsIgnoreCase("true")) {
			emailUtils.sendSimpleMessage(jwtFilter.getCurrentUser(), "Your account has been approved.", "USER:- "+user+ "\n is approved by \n ADMIN:- "+jwtFilter.getCurrentUser(), allAdmin);
		}else {
			emailUtils.sendSimpleMessage(jwtFilter.getCurrentUser(), "Your account has been disabled.", "USER:- "+user+ "\n is disabled by \n ADMIN:- "+jwtFilter.getCurrentUser(), allAdmin);
			
		}
		
		
	}

	@Override
	public ResponseEntity<String> checkToken() {
		
		return CafeUtils.getResponseEntity("true" ,HttpStatus.OK);
	}

	@Override
	public ResponseEntity<String> changePassword(Map<String, String> requestMap) {
		try {
			User userObj = userDao.findByEmail(jwtFilter.getCurrentUser());
			if(!userObj.equals(null)) {
				if(passwordEncoder.matches( requestMap.get("oldPassword"), userObj.getPassword())) {
					userObj.setPassword( passwordEncoder.encode(requestMap.get("newPassword")));
					userDao.save(userObj);
					return CafeUtils.getResponseEntity("Password Updated Successfully", HttpStatus.OK);
				}
				return CafeUtils.getResponseEntity("Incorrect Old Password", HttpStatus.BAD_REQUEST);
			}
			return CafeUtils.getResponseEntity(CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
			
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return CafeUtils.getResponseEntity(CafeConstants.SOMETHING_WENT_WRONG ,HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@Override
	public ResponseEntity<String> forgotPassword(Map<String, String> requestMap) {
		try {
			User user = userDao.findByEmail(requestMap.get("email"));
			if(!Objects.isNull(user) && !Strings.isNullOrEmpty(user.getEmail())) 
			    emailUtils.forgotMail(user.getEmail(), "Credentials by Cafe Management System", passwordEncoder.encode(user.getPassword()));
				return CafeUtils.getResponseEntity("Check your mail for Credentials", HttpStatus.OK); 
		}catch (Exception e) {
			
		}
		return CafeUtils.getResponseEntity(CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	

}
