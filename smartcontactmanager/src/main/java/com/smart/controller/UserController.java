package com.smart.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Optional;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.smart.dao.ContactRepository;
import com.smart.dao.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.User;
import com.smart.helper.Message;

import antlr.collections.List;


@Controller
@RequestMapping("/user")	
public class UserController {
	
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;	
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private ContactRepository contactRepository;
	
	//method for adding common data to response
	@ModelAttribute
	public void addCommanData(Model model, Principal principal) {
		String userName = principal.getName();
		System.out.println("USERNAME: " + userName);
		//getting user using username
		User user = userRepository.getUserByUsername(userName);
		
		System.out.println("USER: "+user);
		
		model.addAttribute("user", user); 
	}
	
	//dashboard home
	@RequestMapping("/index")
	public String dashboard(Model model, Principal principal) {
		
		model.addAttribute("title", "User Dashboard");
		return "normal/user_dashboard";
	}
	
	//open add form controller
	@GetMapping("/add-contact")
	public String openAddContactForm(Model model) {
		
		model.addAttribute("title", "Add Contact");
		model.addAttribute("contact", new Contact());
		
		return "normal/add_contact_form";
	}
	
	//processing add contact form
	@PostMapping("/process-contact")
	public String processContact(@ModelAttribute Contact contact,
			@RequestParam("profileImage") MultipartFile file,
			Principal principal,
			HttpSession session
			) {
		
		try {
		
		String name = principal.getName();
		User user = this.userRepository.getUserByUsername(name);
		
		
		//processing and uploading file..
		if(file.isEmpty()) {
			//if the file is empty
			System.out.println("File is empty!");
			contact.setImage("contact.png");
			
		}
		else {
			//upload file to folder and update name
			contact.setImage(file.getOriginalFilename());
			
			File saveFile = new ClassPathResource("static/img").getFile();
			
			Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + file.getOriginalFilename());
			
			Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
			
			System.out.println("Image is uploaded!");
			
		}
		
		contact.setUser(user);
		
		user.getContacts().add(contact);
		this.userRepository.save(user);
		
		System.out.println("DATA "+contact);
		
		System.out.println("Added to database");
		
		//success message
		session.setAttribute("message", new Message("Your contact is added! Add more...", "success"));
		}
		catch(Exception e) {
			System.out.println("ERROR: "+e.getMessage());
			e.printStackTrace();
			
			//error message
			session.setAttribute("message", new Message("Something went wrong! Try again...", "danger"));
		}
		return "normal/add_contact_form";
	}
	
	
	//show contacts handler
	@GetMapping("/show-contacts/{page}")
	public String showContacts(@PathVariable("page") Integer page ,Model m, Principal principal) {
		m.addAttribute("title","View Contacts");
		
		String userName = principal.getName();
		User user = this.userRepository.getUserByUsername(userName);
		
		Pageable pageable = PageRequest.of(page, 5); 
		
		Page<Contact> contacts = this.contactRepository.findContactsByUser(user.getId(), pageable );
		m.addAttribute("contacts", contacts);
		m.addAttribute("currentPage", page);
		
		m.addAttribute("totalPages", contacts.getTotalPages());
		
		return "normal/show_contacts";
	}
	
	//showing particular contact details
	@RequestMapping("/{cId}/contact")
	public String showContactDetails(@PathVariable("cId") Integer cId, Model model, Principal principal) {
		System.out.println("CID "+cId);
		
		Optional<Contact> contactOptional =  this.contactRepository.findById(cId);
		Contact contact = contactOptional.get();
		
		String userName = principal.getName();
		User user = this.userRepository.getUserByUsername(userName);
		
		if(user.getId() == contact.getUser().getId()) {
			model.addAttribute("contact", contact);
			model.addAttribute("title", contact.getName());
		}
	
		return "normal/contact_detail";
	}
	
	//delete contact handler
	@GetMapping("/delete/{cid}")
	public String deleteContact(@PathVariable("cid") Integer cId, Model model, HttpSession session) {
		  
		Contact contact =  this.contactRepository.findById(cId).get();
				
		//contact.setUser(null); commented by me
		
		//deleting image as well
		
		
		
		this.contactRepository.deleteByIdCustom(cId);
		
		//this.contactRepository.delete(contact); commented by me
		session.setAttribute("message", new Message("Contact Deleted Successfully", "success"));
		return "redirect:/user/show-contacts/0";
	}
	
	// open update form handler
	@PostMapping("/update-contact/{cid}")
	public String updateForm(@PathVariable("cid") Integer cid, Model m) {
		m.addAttribute("title", "Update Contact");
		Contact contact = this.contactRepository.findById(cid).get();
		m.addAttribute("contact", contact); 
		
		return "normal/update_form";
	}
	
	//update contact handler
	@RequestMapping(value="/process-update", method = RequestMethod.POST)
	public String updateHandler(@ModelAttribute Contact contact, @RequestParam("profileImage") MultipartFile file, 
			Model m, HttpSession session, Principal principal) {
		
		try {
			
			//old contact details
			Contact oldContactDetail = this.contactRepository.findById(contact.getcId()).get(); 
			
			if(!file.isEmpty()) {
				//file work and rewrite
				//delete old photo
				File deleteFile = new ClassPathResource("static/img").getFile();
				File file1 = new File(deleteFile, oldContactDetail.getImage());
				file1.delete();
				
				//update new photo
				File saveFile = new ClassPathResource("static/img").getFile();
				
				Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + file.getOriginalFilename());
				
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				
				contact.setImage(file.getOriginalFilename());
				
			}else {
				contact.setImage(oldContactDetail.getImage());
			}
			
		User user = this.userRepository.getUserByUsername(principal.getName());
		contact.setUser(user);
		this.contactRepository.save(contact);
		session.setAttribute("message", new Message("Your contact is updated!", "success"));
		
		
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		
		System.out.println("Contact Name : " + contact.getName());
		System.out.println("Contact Id : " + contact.getcId());
		
		
		return "redirect:/user/" + contact.getcId()+"/contact";
	}
	
	//your profile handler
	@GetMapping("/profile")
	public String yourProfile(Model model) {
		
		model.addAttribute("title","Profile Page");
		return "normal/profile";
	}
	
	//open settings handler
	@GetMapping("/settings")
	public String openSettings() {
		return "normal/settings";
	}
	
	//change password handler
	@PostMapping("/change-password")
	public String changePassword(@RequestParam("oldPassword") String oldPassword, @RequestParam("newPassword") String newPassword
			, Principal principal, HttpSession session) {
		
		System.out.println("OLD PASSWORD: "+ oldPassword);
		System.out.println("NEW PASSWORD: "+ newPassword);
		
		String userName = principal.getName();
		User currentUser = this.userRepository.getUserByUsername(userName);
		
		if(this.bCryptPasswordEncoder.matches(oldPassword, currentUser.getPassword())) {
			//change password
			currentUser.setPassword(this.bCryptPasswordEncoder.encode(newPassword));
			this.userRepository.save(currentUser);
			session.setAttribute("message", new Message("Your Password has changed successfully!", "success"));
			
		}
		else {
			//error
			session.setAttribute("message", new Message("Please enter correct old Password!", "danger"));
			return "redirect:/user/settings";
		}
		
		
		return "redirect:/user/index";
	}
	
	
	
	
	
	
}
