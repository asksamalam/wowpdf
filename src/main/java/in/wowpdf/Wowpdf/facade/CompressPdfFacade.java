package in.wowpdf.Wowpdf.facade;

import in.wowpdf.Wowpdf.serviceImpl.CompressPdfServiceImpl;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Component
public class CompressPdfFacade {
    @Autowired
    CompressPdfServiceImpl compressPdfService;

    public ResponseEntity<byte[]> compressPdf(MultipartFile file , float quality) {
        byte[] response;
        try {
            response = compressPdfService.compressPdf(file, quality);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(null);
        }
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=compressed.pdf");
        headers.add(HttpHeaders.CONTENT_TYPE, "application/pdf");
        return ResponseEntity.ok()
                .headers(headers)
                .body(response);
    }
}
