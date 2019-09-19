package it.openly.pdfgen.services;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import it.openly.pdfgen.models.Footer;
import lombok.SneakyThrows;
import org.apache.commons.text.StringSubstitutor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PostProcessService {

    private static final float A4_WIDTH = 21.0f;

    public InputStream addFooter(Footer footer, InputStream mergedPdf) {
        if(footer == null) {
            return mergedPdf;
        }
        ByteArrayOutputStream outPdf = new ByteArrayOutputStream();
        addFooter(mergedPdf, footer, outPdf);
        return new ByteArrayInputStream(outPdf.toByteArray());
    }

    public InputStream mergePdfFiles(List<InputStream> pdfs) {
        if(pdfs.size() <= 1) {
            return pdfs.get(0);
        }
        ByteArrayOutputStream outPdf = new ByteArrayOutputStream();
        mergePdfFiles(pdfs, outPdf);
        return new ByteArrayInputStream(outPdf.toByteArray());
    }

    @SneakyThrows
    private void addFooter(InputStream inPdf, Footer footerInfo, OutputStream outPdf) {
        Document document = new Document();
        PdfCopy copy = new PdfCopy(document, outPdf);
        document.open();

        PdfReader reader = new PdfReader(inPdf);
        int pages = reader.getNumberOfPages();

        for (int i = 1; i < pages + 1; i++) {
            addFooterToPage(footerInfo, copy, reader, pages, i);
        }

        document.close();
        reader.close();
        outPdf.flush();
    }

    @SneakyThrows
    private void addFooterToPage(Footer footerInfo, PdfCopy copy, PdfReader reader, int pages, int i) {
        float pointsPerCm = PageSize.A4.getWidth() / A4_WIDTH;
        float bottomMargin = footerInfo.getBottomMargin() * pointsPerCm;
        float leftMargin = footerInfo.getLeftMargin() * pointsPerCm;
        float rightMargin = footerInfo.getRightMargin() * pointsPerCm;
        float fontSize = footerInfo.getFontSize() * pointsPerCm;

        PdfImportedPage page = copy.getImportedPage(reader, i);
        Rectangle mediabox = reader.getPageSize(i);

        PdfCopy.PageStamp stamp = copy.createPageStamp(page);
        Map<String, String> values = new HashMap<>();
        values.put("page", String.valueOf(i));
        values.put("pages", String.valueOf(pages));
        StringSubstitutor sub = new StringSubstitutor(values);


        String text = sub.replace(footerInfo.getTemplateText());
        BaseFont baseFont = BaseFont.createFont();
        Font font = new Font(baseFont, fontSize);
        Phrase phrase = new Phrase(text, font);
        PdfContentByte pdfContentByte = stamp.getOverContent();

        float x;
        int align;
        if(i % 2 == 0) {
            x = mediabox.getRight() - rightMargin;
            align = Element.ALIGN_RIGHT;
        }
        else {
            x = leftMargin;
            align = Element.ALIGN_LEFT;
        }
        ColumnText.showTextAligned(pdfContentByte, align, phrase, x, bottomMargin, 0);
        stamp.alterContents();
        copy.addPage(page);
    }

    @SneakyThrows
    private void mergePdfFiles(List<InputStream> inPdfs, OutputStream outPdf) {
        Document document = new Document();
        PdfWriter writer = PdfWriter.getInstance(document, outPdf);
        document.open();
        PdfContentByte cb = writer.getDirectContent();

        for (InputStream in : inPdfs) {
            PdfReader reader = new PdfReader(in);
            for (int i = 1; i <= reader.getNumberOfPages(); i++) {
                document.newPage();
                //import the page from source pdf
                PdfImportedPage page = writer.getImportedPage(reader, i);
                //add the page to the destination pdf
                cb.addTemplate(page, 0, 0);
            }
        }

        outPdf.flush();
        document.close();
    }
}
