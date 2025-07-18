package hyung.jin.seo.jae.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import hyung.jin.seo.jae.dto.OmrSheetDTO;
import hyung.jin.seo.jae.service.OmrService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequestMapping("omr")
public class OmrController {

    @Autowired
    private OmrService omrService;

    /**
     * Preview the OMR results
     * @param branch
     * @param file
     * @return
     */
    @PostMapping("/preview")
    public ResponseEntity<List<OmrSheetDTO>> previewOmr(@RequestParam("branch") String branch,
                                                        @RequestParam("testGroup") String testGroup,
                                                        @RequestParam("grade") String grade,
                                                        @RequestParam("volume") String volume,                                                    
                                                        @RequestParam("file") MultipartFile file) {
        // Validate the file
        if (file.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ArrayList<>()); // Return an empty list for invalid requests
        }
        try {
            // Process the file and generate results
            List<OmrSheetDTO> results = omrService.previewOmr(branch, testGroup, grade, volume, file);
            return ResponseEntity.ok(results);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ArrayList<>()); // Return an empty list for errors
        }
    }

}
	