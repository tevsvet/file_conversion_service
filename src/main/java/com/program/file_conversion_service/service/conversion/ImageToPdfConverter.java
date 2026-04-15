package com.program.file_conversion_service.service.conversion;

import com.program.file_conversion_service.dto.ConvertedFile;
import com.program.file_conversion_service.domain.model.SupportedFileType;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

@Service
public class ImageToPdfConverter implements FileToPdfConverter {

    @Override
    public Set<SupportedFileType> supportedTypes() {
        return Set.of(SupportedFileType.JPG, SupportedFileType.JPEG, SupportedFileType.PNG);
    }

    @Override
    public ConvertedFile convert(InputStream inputStream, String sourceObjectKey) throws IOException {
        BufferedImage image = ImageIO.read(inputStream);
        if (image == null) {
            throw new IOException("Image cannot be decoded: " + sourceObjectKey);
        }

        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            PDImageXObject pdImage = LosslessFactory.createFromImage(document, image);
            PDRectangle mediaBox = page.getMediaBox();

            float widthRatio = mediaBox.getWidth() / image.getWidth();
            float heightRatio = mediaBox.getHeight() / image.getHeight();
            float scale = Math.min(widthRatio, heightRatio);

            float renderedWidth = image.getWidth() * scale;
            float renderedHeight = image.getHeight() * scale;
            float x = (mediaBox.getWidth() - renderedWidth) / 2;
            float y = (mediaBox.getHeight() - renderedHeight) / 2;

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                contentStream.drawImage(pdImage, x, y, renderedWidth, renderedHeight);
            }

            document.save(outputStream);
            return new ConvertedFile(outputStream.toByteArray(), "application/pdf");
        }
    }
}
