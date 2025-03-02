package in.wowpdf.Wowpdf.services;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface CompressPdfService {
    public byte[] compressPdf(MultipartFile file, float quality) throws IOException;
}
