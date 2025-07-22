package com.smart.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.Optional;
import java.util.UUID;

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
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.smart.dao.ContactRepostiory;
import com.smart.dao.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.User;
import com.smart.helper.Message;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/user")
public class UserController {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ContactRepostiory contactRepository;

	// method for adding common data to Response
	@ModelAttribute
	public void addCommonData(Model model, Principal principal) {
		String userName = principal.getName();
		System.out.println("USERNAME : " + userName);

		// get user using username(Email)

		User user = userRepository.getUserByUserName(userName);
		System.out.println("user" + user);

		model.addAttribute("user", user);

	}

	// Dashboard - home
	@RequestMapping("/index")
	public String dashboard(Model model, Principal principal) {

		return "normal/user_Dashboard";

	}

	// open add form handler
	@GetMapping("/add-contact")
	public String openAddContactForm(Model model) {

		model.addAttribute("title", "Add Contact");
		model.addAttribute("contact", new Contact());
		return "normal/add_contact_form";
	}

	// processing add contact form
	@PostMapping("/process-contact")
	public String processContact(@ModelAttribute Contact contact, @RequestParam("profileImage") MultipartFile file,
			Principal principal, HttpSession session) throws IOException {

		String name = principal.getName();
		User user = this.userRepository.getUserByUserName(name);

		// processing and uploading file...
		if (file.isEmpty()) {
			// if the file is empty then try our message
			System.out.println("file is empty");
			contact.setImage("default.jpg");
		} else {
			// file the file to folder and update the name to contact
			File saveFile = new ClassPathResource("static/img").getFile();
			
			String originalName = file.getOriginalFilename();

			String uuid = UUID.randomUUID().toString();
			String uniqueFilename = originalName + "_" + uuid;

			Path path = Paths.get(saveFile.getAbsolutePath(), uniqueFilename);

			Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
			
			contact.setImage(uniqueFilename);

			System.out.println("Image is uploaded");

		}
		try {

			user.getContacts().add(contact);

			contact.setUser(user);

			this.userRepository.save(user);

			System.out.println("Data " + contact);

			System.out.println("Added to database");

			// message success......
			session.setAttribute("message", new Message("Your contact is added || Add more..", "success"));

		} catch (Exception e) {
			System.out.println("ERROR" + e.getMessage());
			e.printStackTrace();
			// message error
			session.setAttribute("message", new Message("Something went wrong || Try again..", "danger"));

		}
		return "normal/add_contact_form";

	}

	// show contacts handler
	// per page 5[0]
	// current page 0 [page]
	@GetMapping("/show-contacts/{page}")
	public String showContacts(@PathVariable("page") Integer page, Model m, Principal principal) {
		m.addAttribute("title", "Show User Contacts");

		String userName = principal.getName();

		User user = this.userRepository.getUserByUserName(userName);
		
	    Pageable pageable = PageRequest.of(page, 5);

		Page<Contact> contacts = this.contactRepository.findContactByUser(user.getId(), pageable);

		m.addAttribute("title", "Show Contact");
		m.addAttribute("contacts", contacts);
		m.addAttribute("currentPage", page);
		
		m.addAttribute("totalPages",contacts.getTotalPages());
		
		return "normal/show_contacts";
	}
	
	//showing particular contact details
	
	@GetMapping("/{cId}/contact")
	public String showContactDetails(@PathVariable("cId") Integer cId, Model model, Principal principal) {
		System.out.println("Cid" + cId);
		
		Optional<Contact> contactOptional = this.contactRepository.findById(cId);
		Contact contact = contactOptional.get();
		
		String userName = principal.getName();
		User user = this.userRepository.getUserByUserName(userName);
		
		
		if(user.getId() == contact.getUser().getId()) {
			model.addAttribute("contact" , contact);
			model.addAttribute("title", contact.getName());
		}
		
		return "normal/contact_detail";
	}
	
	@GetMapping("/delete/{cid}")
	public String deleteContact(@PathVariable("cid") Integer cId, Model model, HttpSession session) {
		Optional<Contact> contactOptional = this.contactRepository.findById(cId);	
		Contact contact = contactOptional.get();
		
		// Check...
		
		System.out.println("Contact" + contact.getcId());
		
		contact.setUser(null);
		
		//remove 
		//img
		//contact.getImage();
		this.contactRepository.delete(contact);
		System.out.println("DELETED");
		session.setAttribute("message", new Message("Contact deleted successfully..." , "success"));
		
		return "redirect:/user/show-contacts/0";
	}
	
	@PostMapping("/update-contact/{cid}")
	public String updateForm(@PathVariable("cid") Integer cid, Model m) {
		
		m.addAttribute("title", "Update Contact");
		
		Contact contact = this.contactRepository.findById(cid).get();
		
		m.addAttribute("contact", contact);
		return "normal/update_form";
	}
	

	@RequestMapping(value = "/process-update", method = RequestMethod.POST)
	public String updateImage(
	        @PathVariable("cId") Integer cId,
	        @RequestParam("profileImage") MultipartFile file,
	        HttpSession session,
	        Principal principal) {

	    try {
	        // Fetch existing contact
	        Contact existingContact = contactRepository.findById(cId).get();

	        // Setup upload folder
	        String uploadDir = new File("uploads").getAbsolutePath();
	        File uploadFolder = new File(uploadDir);
	        if (!uploadFolder.exists()) {
	            uploadFolder.mkdirs();
	        }

	        // Handle image upload
	        if (!file.isEmpty()) {
	            // Delete old image if not default
	            String oldImage = existingContact.getImage();
	            if (!oldImage.equals("default.png")) {
	                File oldFile = new File(uploadFolder, oldImage);
	                if (oldFile.exists()) oldFile.delete();
	            }

	            // Create unique filename
	            String originalName = file.getOriginalFilename();
	            String uuid = UUID.randomUUID().toString();
	            String uniqueFilename = uuid + "_" + originalName;

	            // Save new image
	            Path path = Paths.get(uploadFolder.getAbsolutePath(), uniqueFilename);
	            Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

	            // Update contact image
	            existingContact.setImage(uniqueFilename);
	        }

	        // Save updated contact
	        contactRepository.save(existingContact);
	        session.setAttribute("message", new Message("Profile image updated successfully!", "alert-success"));

	    } catch (Exception e) {
	        e.printStackTrace();
	        session.setAttribute("message", new Message("Failed to update image", "alert-danger"));
	    }

	    return "redirect:/user/show-contacts/0";
	}
	
	@GetMapping("/profile")
	public String yourProfile(Model model) {
		
		model.addAttribute("tittle", "Profile Page");
		return "normal/profile";
	}
}
