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
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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
	public String showContactDetails(@PathVariable("cId") Integer cId, Model model) {
		System.out.println("CID "+cId);
		
		Optional<Contact> contactOptional =  this.contactRepository.findById(cId);
		Contact contact = contactOptional.get();
		
		model.addAttribute("contact", contact);
		
		
		return "normal/contact_detail";
	}
	
	
}
