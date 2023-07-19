package it.cgmconsulting.mspost.service;

import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfDocumentInfo;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.VerticalAlignment;
import it.cgmconsulting.mspost.payload.response.PostResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.format.DateTimeFormatter;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PdfService {

    public InputStream createPdf(PostResponse p) {

        String title = p.getTitle();
        String content = p.getContent();
        String author = p.getAuthor();
        String publishedAt = p.getPublishedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String avg = "Rating: " +  p.getAvgRating();

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        PdfDocument pdf = new PdfDocument(new PdfWriter(out));

        Document document = new Document(pdf, PageSize.A4, false);

        addMetaData(p.getCategories(), p.getAuthor(), pdf);


        Paragraph pTitle = new Paragraph(title).setFontSize(20).setBold()
                .setTextAlignment(TextAlignment.CENTER).setFontColor(new DeviceRgb(100,149,247),95);
        document.add(pTitle);

        Paragraph pContent = new Paragraph(content).setFontSize(16).setTextAlignment(TextAlignment.JUSTIFIED);
        document.add(pContent);

        Paragraph pAvg = new Paragraph(avg).setFontSize(12).setItalic()
                .setFontColor(new DeviceRgb(100,149,247),75).setTextAlignment(TextAlignment.LEFT);
        document.add(pAvg);

        Paragraph pAuthor = new Paragraph(author).setFontSize(10).setItalic()
                .setFontColor(new DeviceRgb(128,128,128),85).setTextAlignment(TextAlignment.RIGHT);
        document.add(pAuthor);

        Paragraph pPublishedAt = new Paragraph(publishedAt).setFontSize(9).setItalic()
                .setFontColor(new DeviceRgb(128,128,128),85).setTextAlignment(TextAlignment.RIGHT);
        document.add(pPublishedAt);

        int pageNumbers = pdf.getNumberOfPages();

        for (int i = 1; i <= pageNumbers; i++){
            document.showTextAligned(new Paragraph(String.format("page %s of %s", i, pageNumbers)).setFontSize(8).setItalic(),
                    560, 20, i, TextAlignment.RIGHT, VerticalAlignment.BOTTOM, 0);
        }

        document.close();
        InputStream in = new ByteArrayInputStream(out.toByteArray());

        return in;
    }

    private void addMetaData(Set<String> categories, String author, PdfDocument pdfDocument){
        PdfDocumentInfo info = pdfDocument.getDocumentInfo();
        info.setKeywords(categories.toString());
        info.setAuthor(author);
    }
}
