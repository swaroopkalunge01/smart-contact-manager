package com.smart.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
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
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ResponseBody;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.smart.dao.ContactRepository;
import com.smart.dao.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.User;
import com.smart.helper.Message;

import java.awt.Color;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletResponse;

import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

@Controller
@RequestMapping("/user")
public class UserController {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ContactRepository contactRepository;
	
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	
	// method for adding comman data
	@ModelAttribute
	public void addCommonData(Model model, Principal principal) {
		String userName = principal.getName();
		System.out.println("Username=" + userName);

		User user = userRepository.getUserByUserName(userName);
		//System.out.println("USER=" + user);
		// get the user using username(Email)
		model.addAttribute("user", user);
	}

	// dashboard Home
	@RequestMapping("/index")
	public String dashboard(Model model, Principal principal) {
		model.addAttribute("title", "User Dashboard");
		return "normal/user_dashboard";
	}

	@GetMapping("/add-contact")
	public String OpenAddContactForm(Model model) {
		model.addAttribute("title", "Add Contact");
		model.addAttribute("contact", new Contact());
		return "normal/add_contact_form";
	}

	// processing add contact home
	@PostMapping("/process-contact")
	public String processContact(@ModelAttribute Contact contact,@RequestParam("profileImage") MultipartFile file, Principal principal,HttpSession session){
		try {
			String name = principal.getName();
			User user = this.userRepository.getUserByUserName(name);
			
			//processing and uploading file
			if(file.isEmpty()){
				//if the file is empty then try our message
				System.out.println("File is Empty");
				contact.setImage("contact.png");
			}
			else{
				//upload the file
				contact.setImage(file.getOriginalFilename());
				File saveFile=new ClassPathResource("static/img").getFile();
				Path path=Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				System.out.println("Image is Uploaded");
			}
			contact.setUser(user);
			user.getContacts().add(contact);
			this.userRepository.save(user);
			System.out.println("Data=" + contact);
			System.out.println("Added to Database");
			//message success
			session.setAttribute("message", new Message("Your contact is added !! add more..","success"));
			
		} catch (Exception e) {
			System.out.println("Error:"+e.getMessage());
			e.printStackTrace();
			
			//error message
			session.setAttribute("message", new Message("Something went wrong !! Try again.. ","danger"));
		}
		return "normal/add_contact_form";
	}
	
	// settings handler
	@GetMapping("/settings")
	public String openSettings(Model model) {
	    model.addAttribute("title", "Settings");
	    return "normal/settings";
	}
	
	// open change password page

	@GetMapping("/change-password")
	public String openChangePassword(Model model){

	    model.addAttribute("title","Change Password");

	    return "normal/change_password";
	}
	
	@PostMapping("/change-password")
	public String changePassword(@RequestParam("oldPassword") String oldPassword,
	                             @RequestParam("newPassword") String newPassword,
	                             Principal principal,
	                             HttpSession session){

	    String userName = principal.getName();

	    User currentUser = this.userRepository.getUserByUserName(userName);

	    if(this.passwordEncoder.matches(oldPassword,currentUser.getPassword())){

	        currentUser.setPassword(this.passwordEncoder.encode(newPassword));

	        this.userRepository.save(currentUser);

	        session.setAttribute("message",
	                new Message("Password changed successfully","success"));

	    }else{

	        session.setAttribute("message",
	                new Message("Old password is incorrect","danger"));
	    }

	    return "redirect:/user/change-password";
	}
	
	@GetMapping("/delete-account")
	public String deleteAccount(Principal principal,
	                            HttpSession session){

	    String userName = principal.getName();

	    User user = this.userRepository.getUserByUserName(userName);

	    this.userRepository.deleteById(user.getId());

	    session.setAttribute("message",
	            new Message("Account deleted successfully","success"));

	    return "redirect:/logout";
	}
	
	@GetMapping("/edit-profile")
	public String openEditProfile(Model model,Principal principal){

	    String userName = principal.getName();

	    User user = this.userRepository.getUserByUserName(userName);

	    model.addAttribute("user",user);

	    model.addAttribute("title","Edit Profile");

	    return "normal/edit_profile";
	}
	
	@PostMapping("/update-profile")
	public String updateProfile(@ModelAttribute User updatedUser,
	                            @RequestParam("profileImage") MultipartFile file,
	                            Principal principal,
	                            HttpSession session){

	    try {

	        String userName = principal.getName();

	        User oldUser = this.userRepository.getUserByUserName(userName);

	        // update basic fields
	        oldUser.setName(updatedUser.getName());

	        oldUser.setEmail(updatedUser.getEmail());

	        oldUser.setAbout(updatedUser.getAbout());



	        // PROFILE IMAGE UPDATE

	        if(!file.isEmpty()){

	            // delete old image
	            File deleteFile =
	                    new ClassPathResource("static/img").getFile();

	            File file1 =
	                    new File(deleteFile, oldUser.getImageUrl());

	            file1.delete();


	            // save new image
	            File saveFile =
	                    new ClassPathResource("static/img").getFile();

	            Path path =
	                    Paths.get(saveFile.getAbsolutePath()
	                    + File.separator
	                    + file.getOriginalFilename());

	            Files.copy(file.getInputStream(),
	                    path,
	                    StandardCopyOption.REPLACE_EXISTING);

	            oldUser.setImageUrl(file.getOriginalFilename());
	        }


	        this.userRepository.save(oldUser);

	        session.setAttribute("message",
	                new Message("Profile updated successfully","success"));

	    }catch(Exception e){

	        e.printStackTrace();

	        session.setAttribute("message",
	                new Message("Something went wrong","danger"));
	    }

	    return "redirect:/user/profile";
	}
	
	//show contact handler
	@GetMapping("/show-contacts/{page}")
	public String showContacts(@PathVariable("page") Integer page,Model model,Principal principal){
		model.addAttribute("title", "Show User Contacts");
		
		String userName=principal.getName();
		User user=this.userRepository.getUserByUserName(userName);
		
		//contact per page 5
		Pageable pageable=PageRequest.of(page, 5);
		
		Page<Contact> contacts=this.contactRepository.findContactsByUser(user.getId(),pageable);
		model.addAttribute("contacts", contacts);
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", contacts.getTotalPages());
		return "normal/show_contacts";
	}
	
	//showing particular contact details
	@RequestMapping("/{cId}/contact")
	public String showContactDetail(@PathVariable("cId") Integer cId,Model model,Principal principle){
		System.out.println("CID="+cId);
		
		Optional<Contact> contactOptional=this.contactRepository.findById(cId);
		Contact contact=contactOptional.get();
		
		String userName=principle.getName();
		User user=this.userRepository.getUserByUserName(userName);
		
		if(user.getId()==contact.getUser().getId()){
			model.addAttribute("contact", contact);
			model.addAttribute("title", contact.getName());
		}
		return "/normal/contact_detail";
	}
	
	@GetMapping("/favorite/{cid}")
	public String favoriteContact(
	        @PathVariable("cid") Integer cid){

	    Contact contact =
	            this.contactRepository.findById(cid).get();

	    contact.setFavorite(!contact.isFavorite());

	    this.contactRepository.save(contact);

	    return "redirect:/user/show-contacts/0";
	}
	
	//delete contact handler
	@GetMapping("/delete/{cid}")
	public String deleteContact(@PathVariable("cid") Integer cId,Model model,HttpSession session,Principal principle){
		Optional<Contact> contactOptional=this.contactRepository.findById(cId);
		Contact contact=contactOptional.get();
		User user=this.userRepository.getUserByUserName(principle.getName());
		user.getContacts().remove(contact);
		this.userRepository.save(user);
		session.setAttribute("message", new Message("Conatct delete successfully..","success"));	
		return "redirect:/user/show-contacts/0";
	}
	//Open update form handler
	@PostMapping("/update-contact/{cid}")
	public String UpdateForm(@PathVariable("cid") Integer cid,Model model){
		model.addAttribute("title", "Update Contact");
		this.contactRepository.findById(cid);
		Contact contact=this.contactRepository.findById(cid).get();
		model.addAttribute("contact",contact);
		return"normal/update_form";
	}
	
	//update contact handler
	@RequestMapping(value="/process-update",method= RequestMethod.POST)
	public String updateHandler(@ModelAttribute Contact contact,@RequestParam("profileImage") MultipartFile file,Model model,HttpSession session,Principal principle){
		
		try {
			
			//fetch old contact details
			Contact oldcontactDetails=this.contactRepository.findById(contact.getcId()).get();
			
			
			if(!file.isEmpty()){
				//delete file
				File deleteFile=new ClassPathResource("static/img").getFile();
				File file1=new File(deleteFile,oldcontactDetails.getImage());
				file1.delete();
				
				//update image
				File saveFile=new ClassPathResource("static/img").getFile();
				
				Path path=Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
				
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				contact.setImage(file.getOriginalFilename());
			}
			else{
				contact.setImage(oldcontactDetails.getImage());
			}
			User user=this.userRepository.getUserByUserName(principle.getName());
			contact.setUser(user);
			this.contactRepository.save(contact);
			session.setAttribute("message", new Message("Your contact is updated...","success"));
			
		}catch(Exception e){
			e.printStackTrace();
		}	
		System.out.println("Conatct NAME="+contact.getName());
		System.out.println("Contact ID="+contact.getcId());
		return "redirect:/user/"+contact.getcId()+"/contact";
	}
	//your profile handler
	@GetMapping("/profile")
	public String yourProfile(Model model){
		model.addAttribute("title", "Profile Page");
		return "normal/profile";
	}
	@GetMapping("/search")
	public String searchHandler(
	        @RequestParam("query") String query,
	        Model model,
	        Principal principal){

	    User user =
	            this.userRepository
	                    .getUserByUserName(principal.getName());

	    List<Contact> contacts =
	            this.contactRepository
	                    .findByNameContainingAndUser(query,user);

	    model.addAttribute("contacts",contacts);

	    model.addAttribute("title","Search Results");

	    return "normal/search";
	}
	@GetMapping("/search/{query}")
	@ResponseBody
	public List<Contact> search(
	        @PathVariable("query") String query,
	        Principal principal){

	    User user =
	            this.userRepository
	                    .getUserByUserName(principal.getName());

	    return this.contactRepository
	            .findByNameContainingAndUser(query,user);
	}
	
	@GetMapping("/export/pdf")
	public void exportToPDF(HttpServletResponse response,
	                        Principal principal){

	    try{

	        response.setContentType("application/pdf");

	        response.setHeader(
	                "Content-Disposition",
	                "attachment; filename=contacts.pdf");

	        Document document = new Document();

	        PdfWriter.getInstance(
	                document,
	                response.getOutputStream());

	        document.open();

	        // TITLE

	        Font fontTitle =
	                FontFactory.getFont(
	                        FontFactory.HELVETICA_BOLD);

	        fontTitle.setSize(18);

	        fontTitle.setColor(Color.BLUE);

	        Paragraph paragraph =
	                new Paragraph(
	                        "Smart Contact Manager",
	                        fontTitle);

	        paragraph.setAlignment(Paragraph.ALIGN_CENTER);

	        document.add(paragraph);

	        document.add(new Paragraph(" "));

	        // TABLE

	        PdfPTable table = new PdfPTable(3);

	        table.setWidthPercentage(100);

	        table.setSpacingBefore(10);

	        // HEADER

	        PdfPCell cell = new PdfPCell();

	        cell.setBackgroundColor(Color.LIGHT_GRAY);

	        cell.setPadding(5);

	        Font font =
	                FontFactory.getFont(
	                        FontFactory.HELVETICA_BOLD);

	        // NAME

	        cell.setPhrase(new Phrase("Name", font));

	        table.addCell(cell);

	        // EMAIL

	        cell.setPhrase(new Phrase("Email", font));

	        table.addCell(cell);

	        // PHONE

	        cell.setPhrase(new Phrase("Phone", font));

	        table.addCell(cell);

	        // FETCH CONTACTS

	        User user =
	                this.userRepository
	                        .getUserByUserName(
	                                principal.getName());

	        List<Contact> contacts =
	                user.getContacts();

	        for(Contact c : contacts){

	            table.addCell(c.getName());

	            table.addCell(c.getEmail());

	            table.addCell(c.getPhone());
	        }

	        document.add(table);

	        document.close();

	    }catch(Exception e){

	        e.printStackTrace();
	    }
	}
}