package in.wowpdf.Wowpdf.serviceImpl;

import in.wowpdf.Wowpdf.services.CompressPdfService;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.stream.IntStream;

@Service
public class CompressPdfServiceImpl implements CompressPdfService {

    private byte[] convertBufferedImageToByteArray(BufferedImage image, String format, float quality) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            // Use ImageWriter for better quality control with JPEG
            Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName(format);
            if (!writers.hasNext()) {
                throw new IOException("No writer found for format: " + format);
            }

            ImageWriter writer = writers.next();
            ImageWriteParam param = writer.getDefaultWriteParam();

            if ("jpg".equalsIgnoreCase(format) || "jpeg".equalsIgnoreCase(format)) {
                param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                param.setCompressionQuality(quality);
            }

            writer.setOutput(ImageIO.createImageOutputStream(baos));
            writer.write(null, new IIOImage(image, null, null), param);
            writer.dispose();

            return baos.toByteArray();
        }
    }

    @Override
    public byte[] compressPdf(MultipartFile file, float quality) throws IOException {
        try (PDDocument document = PDDocument.load(file.getInputStream());
             PDDocument compressedDoc = new PDDocument()) {

            PDFRenderer renderer = new PDFRenderer(document);
            int numPages = document.getNumberOfPages();

            // Process pages with parallel stream for better performance
            IntStream.range(0, numPages)
                    .parallel()
                    .forEach(i -> {
                        try {
                            BufferedImage image = renderer.renderImageWithDPI(i, 100, ImageType.RGB);
                            PDPage originalPage = document.getPage(i);
                            PDPage newPage = new PDPage(originalPage.getMediaBox());
                            compressedDoc.addPage(newPage);

                            try (PDPageContentStream contentStream =
                                         new PDPageContentStream(compressedDoc, newPage)) {
                                PDImageXObject imageXObject = PDImageXObject.createFromByteArray(
                                        compressedDoc,
                                        convertBufferedImageToByteArray(image, "jpg", quality),
                                        "page_" + i
                                );

                                // Maintain aspect ratio
                                float width = imageXObject.getWidth();
                                float height = imageXObject.getHeight();
                                PDRectangle mediaBox = newPage.getMediaBox();
                                float scale = Math.min(mediaBox.getWidth() / width,
                                        mediaBox.getHeight() / height);

                                contentStream.drawImage(imageXObject, 0, 0,
                                        width * scale, height * scale);
                            }
                        } catch (IOException e) {
                            throw new RuntimeException("Error processing page " + i, e);
                        }
                    });

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            compressedDoc.save(outputStream);
            return outputStream.toByteArray();
        }

    }
}
