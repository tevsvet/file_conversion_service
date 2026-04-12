package com.program.file_conversion_service.service.conversion;

import com.program.file_conversion_service.api.dto.ConvertedFile;
import com.program.file_conversion_service.domain.model.SupportedFileType;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
@RequiredArgsConstructor
public class ZipToPdfConverter implements FileToPdfConverter {

    private final ObjectProvider<ConverterRegistry> converterRegistryProvider;

    @Override
    public Set<SupportedFileType> supportedTypes() {
        return Set.of(SupportedFileType.ZIP);
    }

    @Override
    public ConvertedFile convert(InputStream inputStream, String sourceObjectKey) throws IOException {
        try (ZipInputStream zipInputStream = new ZipInputStream(inputStream);
             PDDocument mergedDocument = new PDDocument();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            boolean hasEntries = false;
            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    continue;
                }

                SupportedFileType fileType = SupportedFileType.fromExtension(entry.getName());
                FileToPdfConverter converter = converterRegistryProvider.getObject().getConverter(fileType);
                ConvertedFile convertedFile = converter.convert(
                        new ByteArrayInputStream(zipInputStream.readAllBytes()),
                        entry.getName()
                );

                try (PDDocument innerDocument = Loader.loadPDF(convertedFile.content())) {
                    for (PDPage page : innerDocument.getPages()) {
                        mergedDocument.importPage(page);
                    }
                }

                hasEntries = true;
            }

            if (!hasEntries) {
                throw new IOException("ZIP archive does not contain convertible files: " + sourceObjectKey);
            }

            mergedDocument.save(outputStream);
            return new ConvertedFile(outputStream.toByteArray(), "application/pdf");
        }
    }
}
