package hyung.jin.seo.jae.service.impl;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
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

import hyung.jin.seo.jae.dto.OmrUploadDTO;
import hyung.jin.seo.jae.dto.StudentTestDTO;
import hyung.jin.seo.jae.model.Student;
import hyung.jin.seo.jae.service.OmrService;
import hyung.jin.seo.jae.service.StudentService;

@Service
public class OmrServiceImpl implements OmrService {

	private OmrEngine engine;

	private License omrLicense;
	
	// private String prefix = "https://jacstorage.blob.core.windows.net/work/pdf/";


	@Value("${azure.file.connection}")
	private String azureConnection;

	@Autowired
	private StudentService studentService;

	public OmrServiceImpl() {
		// omrLicense = new License();
		// try {
		// 	omrLicense.setLicense("src/main/resources/omr/Aspose.OMR.Java.lic");
		// } catch (Exception e) {
		// 	e.printStackTrace();
		// }
		engine = new OmrEngine();
	}


	@Override
	public List<StudentTestDTO> previewOmr(String branch, MultipartFile file) throws IOException {
		// 1. create List
		List<StudentTestDTO> processed = new ArrayList<>();
		
		// 2. split pages
		PDDocument document = PDDocument.load(file.getInputStream());
		PDFRenderer renderer = new PDFRenderer(document);
		int numPages = document.getNumberOfPages();

		// Path tempDir = Files.createTempDirectory("omr_jpg_");
		// Path tempDirPath = Path.of(outputDir);


		for(int i=0; i<numPages; i++) {
			// render the PDF page to an image with 100 DPI JPG format
			BufferedImage image = renderer.renderImageWithDPI(i, 100, ImageType.RGB);
			// process the image





			// Use ByteArrayOutputStream to hold the image data in memory
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageIO.write(image, "jpg", baos);
			byte[] imageBytes = baos.toByteArray();
			
			// Upload the image bytes directly to Azure
			String fileName = branch + "_" + (i + 1) + "_" + System.currentTimeMillis() + ".jpg";
			uploadToAzureBlob(fileName, imageBytes);

			


			//////// dummy data
			// 3~6 random number
			long stdTempId = 11200000 + (i+1);
			Student stdTemp = studentService.getStudent(stdTempId);
			StudentTestDTO dto = new StudentTestDTO();
			dto.setFileName(fileName);
            int testId = new Random().nextInt(4) + 3;
            dto.setTestId((long)testId);
            dto.setTestName("Mega Test");
            // Long studentId = 11301580L;//(long)new Random().nextInt(50000);
            dto.setStudentId(stdTempId);
            dto.setStudentName(stdTemp.getFirstName() + " " + stdTemp.getLastName());
            for(int j=0; j<40; j++) {
                // generate radom number from 0 to 4
                int radom = new Random().nextInt(5);
                dto.addAnswer(radom);
            }
			/// ////////////////////////////////
			processed.add(dto);
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
	private void uploadToAzureBlob(String fileName, byte[] fileData) {
		// Create a BlobServiceClient
		BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
		.connectionString(azureConnection)
		.buildClient();
		// Access the container: work
		BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient("work");
		// Define the full path inside the container (folder structure emulated using slashes)
		String blobPath = "pdf/" + fileName;
		// Get a blob client
		BlobClient blobClient = containerClient.getBlobClient(blobPath);
		// Set content-type
		BlobHttpHeaders headers = new BlobHttpHeaders().setContentType("image/jpeg");
		// Upload the file
		blobClient.upload(new ByteArrayInputStream(fileData), fileData.length, true);
		blobClient.setHttpHeaders(headers);

		System.out.println("Temp file uploaded to Azure >>> " + fileName);
	}
	
}
