package hyung.jin.seo.jae.service.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import com.sendgrid.helpers.mail.objects.Personalization;
import com.sendgrid.helpers.mail.objects.Attachments;

import hyung.jin.seo.jae.dto.NoticeEmailDTO;
import hyung.jin.seo.jae.model.NoticeEmail;
import hyung.jin.seo.jae.repository.NoticeEmailRepository;
import hyung.jin.seo.jae.service.EmailService;

@Service
public class EmailServiceImpl implements EmailService {
	
	// @Autowired
	// private JavaMailSender mailSender;

	@Autowired
	private NoticeEmailRepository noticeEmailRepository;

	@Value("${email.api.key}")
	private String emailKey;

	private static String SENDER = "jaccomvictoria@gmail.com";

	// Compose Mail for SendGrid
	private Mail composeMail(String from, String[] tos, String[] ccs, String[] bccs, String subject, String body ){
		Mail mail = new Mail();
		// set From
		Email fromEmail = new Email();
		fromEmail.setName(from);
		fromEmail.setEmail(SENDER);
		mail.setFrom(fromEmail);
		mail.setSubject(subject);
		// Content content = new Content("text/plain", body);
		Content content = new Content("text/html", body);
		mail.addContent(content);
		Personalization p = new Personalization();
		// set To
		for(String to : tos){
			p.addTo(new Email(to));
		}
		// set CC
		if(ccs != null && ccs.length > 0){
			for(String cc : ccs){
				p.addCc(new Email(cc));
			}
		}
		// set BCC
		if(bccs != null && bccs.length > 0){
			for(String bcc : bccs){
				p.addBcc(new Email(bcc));
			}
		}
		mail.addPersonalization(p);
		return mail;
	}


	@Override
	public void sendGridEmail(String from, String[] tos, String[] ccs, String[] bccs, String subject, String body) {
		// set contents
		Mail mail = composeMail(from, tos, ccs, bccs, subject, body);
		// configure SendGrid client
		SendGrid sendGrid = new SendGrid(emailKey);
		// set request
		Request request = new Request();
		try{
			request.setMethod(Method.POST);
			request.setEndpoint("mail/send");
			request.setBody(mail.build());
			// send email
			Response response = sendGrid.api(request);
			System.out.println("Status Code : " + response.getStatusCode());
			System.out.println("Status Body : " + response.getBody());
		}catch(IOException e){
			e.printStackTrace();
		}
	}


	@Override
	public void sendGridEmailWithPdf(String from, String[] tos, String[] ccs, String[] bccs, String subject,
			String body, String fileName, byte[] pdfBytes) {
		// set contents
		Mail mail = composeMail(from, tos, ccs, bccs, subject, body);		
		// Add attachment if present
		if(pdfBytes != null && pdfBytes.length > 0) {
			try {
				Attachments attachments = new Attachments();
				String base64Content = Base64.getEncoder().encodeToString(pdfBytes);
				attachments.setContent(base64Content);
				attachments.setFilename(fileName);
				attachments.setType("application/pdf");
				attachments.setDisposition("attachment");
				mail.addAttachments(attachments);
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		// Send email using SendGrid
		SendGrid sendGrid = new SendGrid(emailKey);
		Request request = new Request();
		try {
			request.setMethod(Method.POST);
			request.setEndpoint("mail/send");
			request.setBody(mail.build());
			Response response = sendGrid.api(request);
			System.out.println("Status Code: " + response.getStatusCode());
			System.out.println("Response Body: " + response.getBody());
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void sendGridEmailWithExcel(String from, String[] tos, String[] ccs, String[] bccs, String subject,
			String body, String fileName, byte[] excelBytes) {
		// set contents
		Mail mail = composeMail(from, tos, ccs, bccs, subject, body);		
		// Add attachment if present
		if(excelBytes != null && excelBytes.length > 0) {
			try {
				Attachments attachments = new Attachments();
				String base64Content = Base64.getEncoder().encodeToString(excelBytes);
				attachments.setContent(base64Content);
				attachments.setFilename(fileName);
				// Set Excel MIME type based on file extension
				String mimeType = fileName.toLowerCase().endsWith(".xlsx") ? 
					"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" : 
					"application/vnd.ms-excel";
				attachments.setType(mimeType);
				attachments.setDisposition("attachment");
				mail.addAttachments(attachments);
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		// Send email using SendGrid
		SendGrid sendGrid = new SendGrid(emailKey);
		Request request = new Request();
		try {
			request.setMethod(Method.POST);
			request.setEndpoint("mail/send");
			request.setBody(mail.build());
			Response response = sendGrid.api(request);
			System.out.println("Status Code: " + response.getStatusCode());
			System.out.println("Response Body: " + response.getBody());
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public List<NoticeEmailDTO> getNoticeEmails(String state, String sender, String grade) {
		List<NoticeEmailDTO> dtos = new ArrayList<>();
		try{
			dtos = noticeEmailRepository.findEmails(state, sender, grade);
		}catch(Exception e){
			System.out.println("No Email Found");
		}
		return dtos;
	}

	@Override
	public List<NoticeEmailDTO> getNoticeEmails(String state, String branch, String sender, String grade) {
		List<NoticeEmailDTO> dtos = new ArrayList<>();
		try{
			dtos = noticeEmailRepository.findEmails(state, branch, sender, grade);
		}catch(Exception e){
			System.out.println("No Email Found");
		}
		return dtos;
	}


	// @Transactional
	// public void saveNoticeEmail(NoticeEmail email) {
	// 	noticeEmailRepository.save(email);
	// }


	@Override
	public NoticeEmailDTO getNoticeEmail(Long id) {
		NoticeEmailDTO dto = null;
		try{
			NoticeEmail email = noticeEmailRepository.findById(id).get();
			dto = new NoticeEmailDTO(email);
		}catch(Exception e){
			System.out.println("No Email Found");
		}
		return dto;
	}

	// @Transactional
	// public void deleteNoticeEmail(Long id) {
	// 	try{
	// 		noticeEmailRepository.deleteById(id);
	// 	}catch(Exception e){
	// 		System.out.println("No Email Found");
	// 	}
	// }


}
