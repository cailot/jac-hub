package hyung.jin.seo.jae.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import hyung.jin.seo.jae.dto.OmrSheetDTO;
import hyung.jin.seo.jae.dto.StudentTestDTO;
import hyung.jin.seo.jae.service.OmrService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequestMapping("omr")
public class OmrController {

    @Autowired
    private OmrService omrService;

    // @Value("${output.directory}")
    // private String outputDir;


    @GetMapping("/hi")
    public String getMethodName() {
        return "Hello Jin";
    }
    
    /**
     * Preview the OMR results
     * @param branch
     * @param file
     * @return
     */
    @PostMapping("/preview")
    public ResponseEntity<List<OmrSheetDTO>> previewOmr(@RequestParam("branch") String branch, @RequestParam("file") MultipartFile file) {
        // Validate the file
        if (file.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ArrayList<>()); // Return an empty list for invalid requests
        }
        try {
            // Process the file and generate results
            List<OmrSheetDTO> results = omrService.previewOmr(branch, file);//processOmrImage();//omrService.processOmrFile(branch, file);
            // Return the results
            //System.out.println("results: >>>>>>>>>> " + results);
            return ResponseEntity.ok(results);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ArrayList<>()); // Return an empty list for errors
        }
    }

}
	