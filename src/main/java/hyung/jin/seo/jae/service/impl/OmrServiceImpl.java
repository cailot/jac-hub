package hyung.jin.seo.jae.service.impl;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.imageio.ImageIO;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.aspose.omr.GenerationResult;
import com.aspose.omr.License;
import com.aspose.omr.OmrEngine;
import com.aspose.omr.RecognitionResult;
import com.aspose.omr.TemplateProcessor;
import com.azure.core.util.Context;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.BlobHttpHeaders;
import com.azure.storage.blob.options.BlobParallelUploadOptions;
// import com.azure.storage.blob.BlobClient;
// import com.azure.storage.blob.BlobContainerClient;
// import com.azure.storage.blob.BlobServiceClient;
// import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.file.share.ShareFileClient;
import com.azure.storage.file.share.ShareFileClientBuilder;

import hyung.jin.seo.jae.dto.OmrSheetDTO;
import hyung.jin.seo.jae.dto.OmrUploadDTO;
import hyung.jin.seo.jae.dto.StudentTestDTO;
import hyung.jin.seo.jae.model.Student;
import hyung.jin.seo.jae.service.OmrService;
import hyung.jin.seo.jae.service.StudentService;
import hyung.jin.seo.jae.utils.JaeConstants;

@Service
public class OmrServiceImpl implements OmrService {

	private OmrEngine engine;

	private License omrLicense;
	
	private TemplateProcessor megaProcessor;

	private TemplateProcessor ttProcessor;


	// private String prefix = "https://jacstorage.blob.core.windows.net/work/omr/";


	@Value("${azure.storage.connection}")
	private String azureConnection;

	@Autowired
	private StudentService studentService;

	public OmrServiceImpl() {
		omrLicense = new License();
		try {
			omrLicense.setLicense("src/main/resources/omr/Aspose.OMR.Java.lic");
		} catch (Exception e) {
			e.printStackTrace();
		}
		engine = new OmrEngine();
		megaProcessor = engine.getTemplateProcessor("src/main/resources/omr/MEGA.omr");
		ttProcessor  = engine.getTemplateProcessor("src/main/resources/omr/TT.omr");
	}


	@Override
	public List<OmrSheetDTO> previewOmr(String branch, MultipartFile file) throws IOException {
		// 1. create List
		List<OmrSheetDTO> processed = new ArrayList<>();
		
		// 2. split pages
		PDDocument document = PDDocument.load(file.getInputStream());
		PDFRenderer renderer = new PDFRenderer(document);
		int numPages = document.getNumberOfPages();

		// Path tempDir = Files.createTempDirectory("omr_jpg_");
		// Path tempDirPath = Path.of(outputDir);


		for(int i=0; i<numPages; i++) {
			// render the PDF page to an image with 100 DPI JPG format
			BufferedImage image = renderer.renderImageWithDPI(i, 100, ImageType.RGB);
			// create OmrSheetDTO to contain StudentTestDTO
			OmrSheetDTO omrSheet = new OmrSheetDTO();
			// process the image
















			// Process the image using Aspose OMR
			// try {
			// 	// Create recognition results from the image
			// 	RecognitionResult[] results = megaProcessor.recognizeImage(image);
				
			// 	if (results != null && results.length > 0) {
			// 		// Get the first result
			// 		RecognitionResult result = results[0];
					
			// 		// Get all answers from the OMR sheet
			// 		Map<String, String> answers = result.getAnswers();
					
			// 		// Process each answer and add to omrSheet
			// 		for (Map.Entry<String, String> entry : answers.entrySet()) {
			// 			String questionId = entry.getKey();
			// 			String answer = entry.getValue();
						
			// 			// Create StudentTestDTO and populate with recognized data
			// 			StudentTestDTO testDTO = new StudentTestDTO();
			// 			testDTO.setFileName(branch + "_" + (i + 1));
						
			// 			// Parse the answer and add to DTO
			// 			// Assuming answer is a single character A-E or 1-5
			// 			int answerValue = -1;
			// 			if (answer != null && !answer.trim().isEmpty()) {
			// 				if (answer.matches("[A-E]")) {
			// 					answerValue = answer.charAt(0) - 'A';
			// 				} else if (answer.matches("[1-5]")) {
			// 					answerValue = Integer.parseInt(answer) - 1;
			// 				}
			// 				if (answerValue >= 0) {
			// 					testDTO.addAnswer(answerValue);
			// 				}
			// 			}
						
			// 			omrSheet.addTest(testDTO);
			// 		}
			// 	}
				
			// 	// Add processed sheet to the list
			// 	processed.add(omrSheet);
				
			// } catch (Exception e) {
			// 	e.printStackTrace();
			// 	// Log error but continue processing other pages
			// 	System.err.println("Error processing page " + (i + 1) + ": " + e.getMessage());
			// }
























			



			// Use ByteArrayOutputStream to hold the image data in memory
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageIO.write(image, "jpg", baos);
			byte[] imageBytes = baos.toByteArray();
			
			// Upload the image bytes directly to Azure
			String fileName = branch + "_" + (i + 1) + "_" + System.currentTimeMillis() + ".jpg";

			// Save image to static assets directory
			String staticDir = "src/main/resources/static/assets/pdf";
			File directory = new File(staticDir);
			if (!directory.exists()) {
				directory.mkdirs();
			}
			Path pdfPath = Paths.get(staticDir, fileName);
			Files.write(pdfPath, imageBytes);

			processMega(fileName);







			// upload Jpg to Blob
			uploadJpgToAzureBlob(fileName, imageBytes);
			// uploadPdfToAzureBlob(fileName, imageBytes);
			






			//////// dummy data
			// 3~6 random number
			long stdTempId = 11200000 + (i+1);
			Student stdTemp = studentService.getStudent(stdTempId);
			int testId = new Random().nextInt(10) + 10;
            

			// english
			StudentTestDTO dto1 = new StudentTestDTO();
			dto1.setFileName(fileName);
            dto1.setTestId((long)testId);
            dto1.setTestName("Acer Test");
            // Long studentId = 11301580L;//(long)new Random().nextInt(50000);
            dto1.setStudentId(stdTempId);
            dto1.setStudentName(stdTemp.getFirstName() + " " + stdTemp.getLastName());
            for(int j=0; j<40; j++) {
                // generate radom number from 0 to 4
                int radom = new Random().nextInt(5);
                dto1.addAnswer(radom);
            }


			// math
			StudentTestDTO dto2 = new StudentTestDTO();
			dto2.setFileName(fileName);
            dto2.setTestId((long)testId);
            dto2.setTestName("Acer Test");
            // Long studentId = 11301580L;//(long)new Random().nextInt(50000);
            dto2.setStudentId(stdTempId);
            dto2.setStudentName(stdTemp.getFirstName() + " " + stdTemp.getLastName());
            for(int j=0; j<40; j++) {
                // generate radom number from 0 to 4
                int radom = new Random().nextInt(5);
                dto2.addAnswer(radom);
            }

			// GA
			StudentTestDTO dto3 = new StudentTestDTO();
			dto3.setFileName(fileName);
            dto3.setTestId((long)testId);
            dto3.setTestName("Acer Test");
            // Long studentId = 11301580L;//(long)new Random().nextInt(50000);
            dto3.setStudentId(stdTempId);
            dto3.setStudentName(stdTemp.getFirstName() + " " + stdTemp.getLastName());
            for(int j=0; j<40; j++) {
                // generate radom number from 0 to 4
                int radom = new Random().nextInt(5);
                dto3.addAnswer(radom);
            }


			/// /////////////////////////////////
			omrSheet.addStudentTest(dto1);
			omrSheet.addStudentTest(dto2);
			// omrSheet.addStudentTest(dto3);
			processed.add(omrSheet);
			System.out.println("Saved: " + fileName);

		}
		document.close();

		// 3. return the list
		return processed;
	}

	// Create file in Azure File Storage
	private void uploadToAzureFile(String fileName, byte[] fileData) {
		ShareFileClient fileClient = new ShareFileClientBuilder()
				.connectionString(azureConnection)
				.shareName("pdf")
				.resourcePath("temp/" + fileName)
				.buildFileClient();
		fileClient.create(fileData.length);
		fileClient.uploadRange(new ByteArrayInputStream(fileData), fileData.length);	
		System.out.println("Temp file uploaded to Azure >>> " + fileName);
	}

	// Create file in Azure Blob Storage
	private void uploadJpgToAzureBlob(String fileName, byte[] fileData) {
		// Create a BlobServiceClient
		BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
		.connectionString(azureConnection)
		.buildClient();
		// Access the container: work
		BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient("work");
		// Define the full path inside the container (folder structure emulated using slashes)
		String blobPath = JaeConstants.OMR_FOLDER + "/" + fileName;
		// Get a blob client
		BlobClient blobClient = containerClient.getBlobClient(blobPath);
		// Set content-type
		BlobHttpHeaders headers = new BlobHttpHeaders().setContentType("image/jpeg");
		// Upload the file
		blobClient.upload(new ByteArrayInputStream(fileData), fileData.length, true);
		blobClient.setHttpHeaders(headers);

		System.out.println("Temp file uploaded to Azure >>> " + fileName);
	}
	
	// Create file in Azure Blob Storage for PDF files
	private void uploadPdfToAzureBlob(String fileName, byte[] fileData) {
		// Create a BlobServiceClient
		BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
		.connectionString(azureConnection)
		.buildClient();
		// Access the container: work
		BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(JaeConstants.WORK_FOLDER);
		// Define the full path inside the container (folder structure emulated using slashes)
		String blobPath = JaeConstants.OMR_FOLDER + "/" + fileName;
		// Get a blob client
		BlobClient blobClient = containerClient.getBlobClient(blobPath);
		// Set content-type for PDF
		BlobHttpHeaders headers = new BlobHttpHeaders().setContentType("application/pdf");
		// Upload the file
		BlobParallelUploadOptions options = new BlobParallelUploadOptions(new ByteArrayInputStream(fileData))
			.setHeaders(headers);
		blobClient.uploadWithResponse(options, null, Context.NONE);

		System.out.println("PDF file uploaded to Azure >>> " + fileName);
	}

	// process Mega/Revision sheet
	private void processMega(String fileName){
			// Use the file from static directory
			String staticPath = "src/main/resources/static/assets/pdf/" + fileName;
			// Process the file
			RecognitionResult result = megaProcessor.recognizeImage(staticPath, 40);			
			System.out.println("getCsv() : " + result.getCsv());
			System.out.println("getJson() : " + result.getJson());
			System.out.println("getOmrElement() : " + result.getOmrElements());
			System.out.println("getTemplateName() : " + result.getTemplateName());	
	}
		
	
}
