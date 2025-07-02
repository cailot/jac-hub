package hyung.jin.seo.jae.service;

import java.io.IOException;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import hyung.jin.seo.jae.dto.OmrSheetDTO;

public interface OmrService {

	// preview the OMR
	List<OmrSheetDTO> previewOmr(String branch, String testGroup, String grade, String volume, MultipartFile file) throws IOException;

}
