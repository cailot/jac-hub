package hyung.jin.seo.jae.controller;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import hyung.jin.seo.jae.dto.NoticeEmailDTO;
import hyung.jin.seo.jae.model.NoticeEmail;
import hyung.jin.seo.jae.service.EmailService;
import hyung.jin.seo.jae.utils.JaeConstants;

@Controller
@RequestMapping("email")
public class EmailController {

	@Autowired
	private EmailService emailService;

	// @Autowired
	// private CodeService codeService;

	// @Autowired
	// private StudentService studentService;

	// @GetMapping("/sendAnnouncement")
	// @ResponseBody
    // public ResponseEntity<String> emailAnnouncement(@RequestParam String state, @RequestParam String branch, @RequestParam String grade, @RequestParam String sender, @RequestParam String subject, @RequestParam String body){
	// 	try{
	// 		// 1. get sender email address
	// 		String fromEmail = codeService.getBranchEmail(sender);
	// 		fromEmail = "braybrook@jamesancollegevic.com.au";
	// 		// 2. get receipients
	// 		List<String> receipients = studentService.getBranchReceipents(state, branch, grade);
	// 		int size = receipients.size();
	// 		receipients = new ArrayList<String>();
	// 		receipients.add("cailot@naver.com");
	// 		receipients.add("jh05052008@gmail.com");
	// 		// 3. send email
	// 		emailService.sendEmail(fromEmail, receipients, subject, body);
	// 		// 4. save email to database
	// 		NoticeEmail notice = new NoticeEmail();
	// 		notice.setState(state);
	// 		notice.setBranch(branch);
	// 		notice.setGrade(grade);
	// 		notice.setSender(sender);
	// 		notice.setTitle(subject);
	// 		notice.setBody(body);
	// 		emailService.saveNoticeEmail(notice);
	// 		// 5. return response
	// 		return ResponseEntity.ok(size+"");
	// 	}catch(Exception e){
	// 		String message = "\"Error sending email : " + e.getMessage() + "\"";
	// 		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(message);
	// 	}
    // }

	// email list for branchEmail.jsp
	@GetMapping("/emailList")
	public String renewStudents(@RequestParam("listState") String state,
								@RequestParam("listBranch") String branch,
								@RequestParam("listSender") String sender, 
								@RequestParam("listGrade") String grade, Model model){
		List<NoticeEmailDTO> dtos = emailService.getNoticeEmails(state, branch, sender, grade);
		model.addAttribute(JaeConstants.EMAIL_LIST, dtos);
		return "branchEmailPage";
	}

	// search email by ID
	@GetMapping("/get/{id}")
	@ResponseBody
	NoticeEmailDTO getNoticeEmail(@PathVariable Long id) {
		NoticeEmailDTO dto = emailService.getNoticeEmail(id);
		if(dto==null) return new NoticeEmailDTO(); // return empty if not found
		return dto;
	}

}