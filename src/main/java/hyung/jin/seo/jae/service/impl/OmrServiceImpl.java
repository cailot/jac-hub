package hyung.jin.seo.jae.service.impl;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.imageio.ImageIO;

import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.aspose.omr.License;
import com.aspose.omr.OmrEngine;
import com.aspose.omr.RecognitionResult;
import com.aspose.omr.TemplateProcessor;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.BlobHttpHeaders;
import hyung.jin.seo.jae.dto.OmrSheetDTO;
import hyung.jin.seo.jae.dto.TestDTO;
import hyung.jin.seo.jae.service.OmrService;
import hyung.jin.seo.jae.service.StudentService;
import hyung.jin.seo.jae.service.ConnectedService;
import hyung.jin.seo.jae.utils.JaeConstants;
import hyung.jin.seo.jae.utils.JaeUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class OmrServiceImpl implements OmrService {

	private OmrEngine engine;

	private License omrLicense;
	
	private TemplateProcessor megaProcessor;

	private TemplateProcessor ttProcessor;

	// private String baseDir = "src/main/resources/static/assets/pdf/jpg";

	private static int OMR_THRESHOLD = 32;

	@Value("${azure.storage.connection}")
	private String azureConnection;

	@Autowired
	private StudentService studentService;

	@Autowired
	private ConnectedService ConnectedService;

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
	public List<OmrSheetDTO> previewOmr(String branch, String testGroup, String grade, String volume, MultipartFile file) throws IOException {
		// 1. create List
		List<OmrSheetDTO> sheets = new ArrayList<>();
		
		// 2. split pages
		PDDocument document = PDDocument.load(file.getInputStream());
		PDFRenderer renderer = new PDFRenderer(document);
		int numPages = document.getNumberOfPages();

		// iterate each answer sheet
		for(int i=0; i<numPages; i++) {
			// render the PDF page to an image with 200 DPI JPG format
			BufferedImage image = renderer.renderImageWithDPI(i, 200, ImageType.RGB);
			
			// 3. Process OMR scanning sheet
			// Check test type - Mega/Revision or others
			OmrSheetDTO omrSheet = new OmrSheetDTO();			
			if(StringUtils.equalsIgnoreCase(testGroup, JaeConstants.MEGA_TEST) || StringUtils.endsWithIgnoreCase(testGroup, JaeConstants.REVISION_TEST)){
				// Process OMR
				omrSheet = processMega(image);
			}else{
				// Process OMR
				omrSheet = processTT(image);
			}	
			// Use ByteArrayOutputStream to hold the image data in memory
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageIO.write(image, "jpg", baos);
			byte[] imageBytes = baos.toByteArray();
			String fileName = branch + "_" + (i + 1) + "_" + System.currentTimeMillis() + ".jpg";

			// 4. Upload to Azure
			uploadJpgToAzureBlob(fileName, imageBytes);

			omrSheet.setFileName(fileName);
			omrSheet.setTestName(JaeUtils.getTestName(testGroup));
			omrSheet.setVolume(Integer.parseInt(volume));
			List<TestDTO> tests = ConnectedService.getTestByGroup(Integer.parseInt(testGroup), grade, Integer.parseInt(volume));
			// Set testIds from tests to omrSheet
			if (tests != null && !tests.isEmpty()) {
				String[] testIds = {};
				for(TestDTO dto : tests){
					String tId = dto.getId();
					testIds = Arrays.copyOf(testIds, testIds.length + 1);
					testIds[testIds.length - 1] = tId;
				}
				omrSheet.setTestIds(testIds);
			}
			sheets.add(omrSheet);
			System.out.println("Returned: " + sheets);
		}
		
		// close document
		document.close();
		return sheets;
	}

	// Create file in Azure File Storage
	// private void uploadToAzureFile(String fileName, byte[] fileData) {
	// 	ShareFileClient fileClient = new ShareFileClientBuilder()
	// 			.connectionString(azureConnection)
	// 			.shareName("pdf")
	// 			.resourcePath("temp/" + fileName)
	// 			.buildFileClient();
	// 	fileClient.create(fileData.length);
	// 	fileClient.uploadRange(new ByteArrayInputStream(fileData), fileData.length);	
	// 	System.out.println("Temp file uploaded to Azure >>> " + fileName);
	// }

	// Create file in Azure Blob Storage
	private void uploadJpgToAzureBlob(String fileName, byte[] fileData) {
		try {
			// Create a BlobServiceClient
			BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
			.connectionString(azureConnection)
			.buildClient();
			// Access the container: work
			BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient("work");
			// Define the full path inside the container
			String blobPath = JaeConstants.OMR_FOLDER + "/" + fileName;
			// Get a blob client
			BlobClient blobClient = containerClient.getBlobClient(blobPath);
			// Set content-type
			BlobHttpHeaders headers = new BlobHttpHeaders().setContentType("image/jpeg");
			// Upload the file
			blobClient.upload(new ByteArrayInputStream(fileData), fileData.length, true);
			blobClient.setHttpHeaders(headers);

			// System.out.println("File uploaded to Azure successfully: " + fileName);
		} catch (Exception e) {
			System.err.println("Error uploading to Azure: " + e.getMessage());
			throw new RuntimeException("Failed to upload file to Azure", e);
		}
	}
	
	// Create file in Azure Blob Storage for PDF files
	// private void uploadPdfToAzureBlob(String fileName, byte[] fileData) {
	// 	// Create a BlobServiceClient
	// 	BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
	// 	.connectionString(azureConnection)
	// 	.buildClient();
	// 	// Access the container: work
	// 	BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(JaeConstants.WORK_FOLDER);
	// 	// Define the full path inside the container (folder structure emulated using slashes)
	// 	String blobPath = JaeConstants.OMR_FOLDER + "/" + fileName;
	// 	// Get a blob client
	// 	BlobClient blobClient = containerClient.getBlobClient(blobPath);
	// 	// Set content-type for PDF
	// 	BlobHttpHeaders headers = new BlobHttpHeaders().setContentType("application/pdf");
	// 	// Upload the file
	// 	BlobParallelUploadOptions options = new BlobParallelUploadOptions(new ByteArrayInputStream(fileData))
	// 		.setHeaders(headers);
	// 	blobClient.uploadWithResponse(options, null, Context.NONE);

	// 	System.out.println("PDF file uploaded to Azure >>> " + fileName);
	// }

	// process Mega/Revision sheet
	// private OmrSheetDTO processMegaFile(String filePath) {
	// 	// Process the file directly using the full path
	// 	RecognitionResult processResult;
	// 	try {
	// 		processResult = megaProcessor.recognizeImage(filePath, OMR_THRESHOLD);
	// 	} catch (FileNotFoundException e) {
	// 		e.printStackTrace();
	// 		return null;
	// 	}			
	// 	String jsonResult = processResult.getJson();
	// 	// System.out.println("getJson() : " + jsonResult);
	// 	OmrSheetDTO dto = new OmrSheetDTO();
	// 	try {
	// 		ObjectMapper mapper = new ObjectMapper();
	// 		JsonNode rootNode = mapper.readTree(jsonResult);
	// 		JsonNode results = rootNode.path("RecognitionResults");
			
	// 		// Initialize arrays for each group
	// 		int[] groupA = new int[30]; // Group 1 questions
	// 		int[] groupB = new int[30]; // Group 2 questions
	// 		int[] groupC = new int[30]; // Group 3 questions
	// 		String studentId = "";
			
	// 		// Process each result
	// 		for (JsonNode result : results) {
	// 			String elementName = result.get("ElementName").asText();
	// 			String value = result.get("Value").asText();
				
	// 			if ("Student_number".equals(elementName)) {
	// 				studentId = value;
	// 				if(StringUtils.isNotBlank(studentId)){
	// 					// avoid wrong studentId
	// 					try {
	// 						Long stdId = Long.parseLong(studentId);
	// 						String studentName = studentService.getStudentName(stdId);
	// 						dto.setStudentId(stdId);
	// 						dto.setStudentName(studentName);	
	// 					} catch (NumberFormatException nfe) {
	// 						// skip if studentId is not a valid number
	// 						continue;
	// 					}
	// 				}
	// 			}				
	// 			// Parse group questions (Group_X_QY format)
	// 			if (elementName.startsWith("Group_")) {
	// 				// Extract group number and question number
	// 				String[] parts = elementName.split("_");
	// 				if (parts.length == 3) {
	// 					int groupNum = Integer.parseInt(parts[1]);
	// 					int questionNum = Integer.parseInt(parts[2].substring(1)) - 1; // Convert Q1 to 0-based index
						
	// 					// Convert answer to numeric value (A=1, B=2, C=3, D=4)
	// 					int numericValue = 0;
	// 					if (!value.isEmpty() && !value.contains(",")) { // Skip empty or multiple answers
	// 						switch (value) {
	// 							case "A": numericValue = 1; break;
	// 							case "B": numericValue = 2; break;
	// 							case "C": numericValue = 3; break;
	// 							case "D": numericValue = 4; break;
	// 							default: numericValue = 0;
	// 						}
	// 					}
						
	// 					// Assign to appropriate group
	// 					switch (groupNum) {
	// 						case 1: groupA[questionNum] = numericValue; break;
	// 						case 2: groupB[questionNum] = numericValue; break;
	// 						case 3: groupC[questionNum] = numericValue; break;
	// 					}
	// 				}
	// 			}
	// 		}
			
	// 		// Add answers to DTO - each group as a separate array
	// 		dto.addAnswer(groupA);
	// 		dto.addAnswer(groupB);
	// 		dto.addAnswer(groupC);
			
	// 	} catch (Exception e) {
	// 		System.err.println("Error parsing OMR results: " + e.getMessage());
	// 	}
	// 	return dto;
	// }

	
	private OmrSheetDTO processMega(BufferedImage image) {
		// Process the file directly using the full path
		RecognitionResult processResult = megaProcessor.recognizeImage(image, OMR_THRESHOLD);
		String jsonResult = processResult.getJson();
		// System.out.println("getJson() : " + jsonResult);
		OmrSheetDTO dto = new OmrSheetDTO();
		try {
			ObjectMapper mapper = new ObjectMapper();
			JsonNode rootNode = mapper.readTree(jsonResult);
			JsonNode results = rootNode.path("RecognitionResults");
			
			// Initialize arrays for each group
			int[] groupA = new int[30]; // Group 1 questions
			int[] groupB = new int[30]; // Group 2 questions
			int[] groupC = new int[30]; // Group 3 questions
			String studentId = "";
			
			// Process each result
			for (JsonNode result : results) {
				String elementName = result.get("ElementName").asText();
				String value = result.get("Value").asText();
				
				if ("Student_number".equals(elementName)) {
					studentId = value;
					if(StringUtils.isNotBlank(studentId)){
						// avoid wrong studentId
						try {
							Long stdId = Long.parseLong(studentId);
							String studentName = studentService.getStudentName(stdId);
							dto.setStudentId(stdId);
							dto.setStudentName(studentName);	
						} catch (NumberFormatException nfe) {
							// skip if studentId is not a valid number
							continue;
						}
					}
				}				
				// Parse group questions (Group_X_QY format)
				if (elementName.startsWith("Group_")) {
					// Extract group number and question number
					String[] parts = elementName.split("_");
					if (parts.length == 3) {
						int groupNum = Integer.parseInt(parts[1]);
						int questionNum = Integer.parseInt(parts[2].substring(1)) - 1; // Convert Q1 to 0-based index
						
						// Convert answer to numeric value (A=1, B=2, C=3, D=4)
						int numericValue = 0;
						if (!value.isEmpty() && !value.contains(",")) { // Skip empty or multiple answers
							switch (value) {
								case "A": numericValue = 1; break;
								case "B": numericValue = 2; break;
								case "C": numericValue = 3; break;
								case "D": numericValue = 4; break;
								default: numericValue = 0;
							}
						}
						
						// Assign to appropriate group
						switch (groupNum) {
							case 1: groupA[questionNum] = numericValue; break;
							case 2: groupB[questionNum] = numericValue; break;
							case 3: groupC[questionNum] = numericValue; break;
						}
					}
				}
			}
			
			// Add answers to DTO - each group as a separate array
			dto.addAnswer(groupA);
			dto.addAnswer(groupB);
			dto.addAnswer(groupC);
			
		} catch (Exception e) {
			System.err.println("Error parsing OMR results: " + e.getMessage());
		}
		return dto;
	}

	
	// process Mega/Revision sheet
	// private OmrSheetDTO processTT(String filePath){
	// 	// Process the file directly from the provided path
	// 	RecognitionResult processResult = null;
	// 	try {
	// 		processResult = ttProcessor.recognizeImage(filePath, OMR_THRESHOLD);
	// 	} catch (FileNotFoundException e) {
	// 		e.printStackTrace();
	// 		return null;
	// 	}			
	// 	String jsonResult = processResult.getJson();
	// 	OmrSheetDTO dto = new OmrSheetDTO();
	// 	try {
	// 		ObjectMapper mapper = new ObjectMapper();
	// 		JsonNode rootNode = mapper.readTree(jsonResult);
	// 		JsonNode results = rootNode.path("RecognitionResults");
			
	// 		// Initialize arrays for each group
	// 		int[] group1 = new int[60]; // Group 1 questions 1-60
	// 		int[] group2 = new int[60]; // Group 2 questions 1-60
	// 		String studentId = "";
			
	// 		// Process each result
	// 		for (JsonNode result : results) {
	// 			String elementName = result.get("ElementName").asText();
	// 			String value = result.get("Value").asText();

	// 			// INSERT_YOUR_CODE
	// 			// System.out.println("ElementName: " + elementName + ", Value: " + value);

	// 			if ("Student_number".equals(elementName)) {
	// 				studentId = value;
	// 				if(StringUtils.isNotBlank(studentId)){
	// 					// avoid wrong studentId
	// 					try {
	// 						Long stdId = Long.parseLong(studentId);
	// 						String studentName = studentService.getStudentName(stdId);
	// 						dto.setStudentId(stdId);
	// 						dto.setStudentName(studentName);	
	// 					} catch (NumberFormatException nfe) {
	// 						// skip if studentId is not a valid number
	// 						continue;
	// 					}
	// 				}
	// 			}
				
	// 			// Parse group questions (Group_X_QY format)
	// 			if (elementName.startsWith("Group_")) {
	// 				// Extract group number and question number
	// 				String[] parts = elementName.split("_");
	// 				if (parts.length == 3) {
	// 					int groupNum = Integer.parseInt(parts[1]);
	// 					int questionNum = Integer.parseInt(parts[2].substring(1)) - 1; // Convert Q1 to 0-based index
						
	// 					// Convert answer to numeric value (A=1, B=2, C=3, D=4, E=5)
	// 					int numericValue = 0;
	// 					if (!value.isEmpty() && !value.contains(",")) { // Skip empty or multiple answers
	// 						switch (value) {
	// 							case "A": numericValue = 1; break;
	// 							case "B": numericValue = 2; break;
	// 							case "C": numericValue = 3; break;
	// 							case "D": numericValue = 4; break;
	// 							case "E": numericValue = 5; break;
	// 							default: numericValue = 0;
	// 						}
	// 					}
						
	// 					// Assign to appropriate group array
	// 					if (groupNum == 1) {
	// 						group1[questionNum] = numericValue;
	// 					} else if (groupNum == 2) {
	// 						group2[questionNum] = numericValue;
	// 					}
	// 				}
	// 			}
	// 		}

	// 			// Print results for verification
	// 		System.out.println("Student ID: " + studentId);
	// 		System.out.println("Group 1: " + Arrays.toString(group1));
	// 		System.out.println("Group 2: " + Arrays.toString(group2));
			
	// 		// Add answers to DTO - each group as a separate array
	// 		dto.addAnswer(group1);
	// 		dto.addAnswer(group2);
			
	// 	} catch (Exception e) {
	// 		System.err.println("Error parsing OMR results: " + e.getMessage());
	// 	}
	// 	return dto;
	// }

	// process Mega/Revision sheet
	private OmrSheetDTO processTT(BufferedImage image){
		// Process the file directly from the provided path
		RecognitionResult processResult = ttProcessor.recognizeImage(image, OMR_THRESHOLD);
		String jsonResult = processResult.getJson();
		OmrSheetDTO dto = new OmrSheetDTO();
		try {
			ObjectMapper mapper = new ObjectMapper();
			JsonNode rootNode = mapper.readTree(jsonResult);
			JsonNode results = rootNode.path("RecognitionResults");
			
			// Initialize arrays for each group
			int[] group1 = new int[60]; // Group 1 questions 1-60
			int[] group2 = new int[60]; // Group 2 questions 1-60
			String studentId = "";
			
			// Process each result
			for (JsonNode result : results) {
				String elementName = result.get("ElementName").asText();
				String value = result.get("Value").asText();

				// INSERT_YOUR_CODE
				// System.out.println("ElementName: " + elementName + ", Value: " + value);

				if ("Student_number".equals(elementName)) {
					studentId = value;
					if(StringUtils.isNotBlank(studentId)){
						// avoid wrong studentId
						try {
							Long stdId = Long.parseLong(studentId);
							String studentName = studentService.getStudentName(stdId);
							dto.setStudentId(stdId);
							dto.setStudentName(studentName);	
						} catch (NumberFormatException nfe) {
							// skip if studentId is not a valid number
							continue;
						}
					}
				}
				
				// Parse group questions (Group_X_QY format)
				if (elementName.startsWith("Group_")) {
					// Extract group number and question number
					String[] parts = elementName.split("_");
					if (parts.length == 3) {
						int groupNum = Integer.parseInt(parts[1]);
						int questionNum = Integer.parseInt(parts[2].substring(1)) - 1; // Convert Q1 to 0-based index
						
						// Convert answer to numeric value (A=1, B=2, C=3, D=4, E=5)
						int numericValue = 0;
						if (!value.isEmpty() && !value.contains(",")) { // Skip empty or multiple answers
							switch (value) {
								case "A": numericValue = 1; break;
								case "B": numericValue = 2; break;
								case "C": numericValue = 3; break;
								case "D": numericValue = 4; break;
								case "E": numericValue = 5; break;
								default: numericValue = 0;
							}
						}
						
						// Assign to appropriate group array
						if (groupNum == 1) {
							group1[questionNum] = numericValue;
						} else if (groupNum == 2) {
							group2[questionNum] = numericValue;
						}
					}
				}
			}

				// Print results for verification
			System.out.println("Student ID: " + studentId);
			System.out.println("Group 1: " + Arrays.toString(group1));
			System.out.println("Group 2: " + Arrays.toString(group2));
			
			// Add answers to DTO - each group as a separate array
			dto.addAnswer(group1);
			dto.addAnswer(group2);
			
		} catch (Exception e) {
			System.err.println("Error parsing OMR results: " + e.getMessage());
		}
		return dto;
	}
	
}
