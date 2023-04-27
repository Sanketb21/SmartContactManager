package com.smart.controller;

import java.util.Random;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.dao.UserRepository;
import com.smart.entities.User;
import com.smart.service.EmailService;

@Controller
public class ForgotController {
	Random random = new Random(1000);
	
	@Autowired
	private EmailService emailService;
	
	@Autowired
	private UserRepository userRepository;
	
	
	//email id form open handler
	@RequestMapping("/forgot")
	public String openEmailForm() {
		return "forgot_email_form";
	}
	
	
	@PostMapping("/send-otp")
	public String sendOTP(@RequestParam("email") String email, HttpSession session) {
		
		System.out.println("EMAIL:" +email);
		
		//generating otp of 4 digits
		
		
		int otp = random.nextInt(9999999);
		
		System.out.println("OTP: "+otp);
		
		
		//write code for sending otp to email
		
		String subject = "OTP from SCM";
		String message = ""
				+"<div style = 'border:1px solid #e2e2e2; padding:20px'>"
				+"Hi!"
				+"<br>"
				+"We received your request for a single-use code to use with your Smart Contact Manager."
				+"<br>"
				+"Your One-Time-Password (OTP) is : "
				+"<b>"
				+ otp
				+"</b>"
				+"<br>"
				+"If you didn't request this code, you can safely ignore this email. Someone else might have typed your email address by mistake."
				+"<br>"
				+"Thanks,"
				+"<br>"
				+"Smark Contact Manager Team."
				+"</div>";
		String to = email;
		
		boolean flag = this.emailService.sendEmail(subject, message, to);
		
		if(flag) {
			session.setAttribute("myotp", otp);
			session.setAttribute("email", email);
			return "verify_otp";
			
		}
		else {
			
			session.setAttribute("message", "Check your Email id!");
			return "forgot_email_form";
		}	
	}
	
	//verify otp
	@PostMapping("/verify-otp")
	public String verifyOtp(@RequestParam("otp") int otp, HttpSession session) {
		int myOtp = (int)session.getAttribute("myotp");
		String email = (String)session.getAttribute("email");
		if(myOtp == otp) {
			//password change form
			User user = this.userRepository.getUserByUsername(email);
			
			if(user == null){
				//send error message
				session.setAttribute("message", "User does not exist with this Email Address!");
				return "forgot_email_form";
			}else {
				//send change password form
			}
			
			
			
			return "password_change_form";
		}
		else {
			session.setAttribute("message", "You have entered wrong OTP!");
			return "verify_otp";
		}
	}
	
	
	
}
