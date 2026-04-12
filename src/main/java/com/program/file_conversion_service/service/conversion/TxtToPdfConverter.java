package com.program.file_conversion_service.service.conversion;

import com.program.file_conversion_service.api.dto.ConvertedFile;
import com.program.file_conversion_service.domain.model.SupportedFileType;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Set;

@Service
public class TxtToPdfConverter implements FileToPdfConverter {

    private static final PDType1Font FONT = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
    private static final float FONT_SIZE = 12f;
    private static final float LEADING = 16f;
    private static final float MARGIN = 50f;

    @Override
    public Set<SupportedFileType> supportedTypes() {
        return Set.of(SupportedFileType.TXT);
    }

    @Override
    public ConvertedFile convert(InputStream inputStream, String sourceObjectKey) throws IOException {
        String text = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);

        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            PDPageContentStream contentStream = new PDPageContentStream(document, page);
            contentStream.setFont(FONT, FONT_SIZE);
            contentStream.beginText();
            contentStream.newLineAtOffset(MARGIN, page.getMediaBox().getHeight() - MARGIN);

            float currentY = page.getMediaBox().getHeight() - MARGIN;
            for (String rawLine : text.split("\\R")) {
                if (currentY <= MARGIN) {
                    contentStream.endText();
                    contentStream.close();
                    page = new PDPage(PDRectangle.A4);
                    document.addPage(page);
                    contentStream = new PDPageContentStream(document, page);
                    contentStream.setFont(FONT, FONT_SIZE);
                    contentStream.beginText();
                    contentStream.newLineAtOffset(MARGIN, page.getMediaBox().getHeight() - MARGIN);
                    currentY = page.getMediaBox().getHeight() - MARGIN;
                }

                contentStream.showText(rawLine.replace("\t", "    "));
                contentStream.newLineAtOffset(0, -LEADING);
                currentY -= LEADING;
            }

            contentStream.endText();
            contentStream.close();

            document.save(outputStream);
            return new ConvertedFile(outputStream.toByteArray(), "application/pdf");
        }
    }
}
