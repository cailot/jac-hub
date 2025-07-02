package hyung.jin.seo.jae.service;

import java.util.List;

import hyung.jin.seo.jae.dto.NoticeEmailDTO;

public interface EmailService {

	// send simple email
	// void sendEmail(String from, String to, String subject, String body);

	// send simple email to multiple recipients
	// void sendEmail(String from, List<String> to, String subject, String body);

	// send simple email with attachment
	// void sendEmailWithAttachment(String from, String to, String subject, String body, String fileName, byte[] pdfBytes);

	// bring email from database
	List<NoticeEmailDTO> getNoticeEmails(String state, String sender, String grade);

	// bring email from database
	List<NoticeEmailDTO> getNoticeEmails(String state, String branch, String sender, String grade);

	// save email to database
	// void saveNoticeEmail(NoticeEmail email);

	// bring email by id
	NoticeEmailDTO getNoticeEmail(Long id);

	// send combined email to multiple recipients with bcc
	void sendGridEmail(String from, String[] to, String[] cc, String[] bcc, String subject, String body);

	// send combined email with attachment to multiple recipients with bcc
	void sendGridEmailWithPdf(String from, String[] to, String[] cc, String[] bcc, String subject, String body, String fileName, byte[] pdfBytes);

	// send combined email with Excel attachment to multiple recipients with bcc
	void sendGridEmailWithExcel(String from, String[] to, String[] cc, String[] bcc, String subject, String body, String fileName, byte[] excelBytes);
	
}
