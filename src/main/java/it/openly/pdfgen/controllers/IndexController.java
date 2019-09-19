package it.openly.pdfgen.controllers;

import it.openly.pdfgen.models.Footer;
import it.openly.pdfgen.models.UrlToPdfRequest;
import it.openly.pdfgen.services.PostProcessService;
import it.openly.pdfgen.services.PuppeteerPrintService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class IndexController {

    @Autowired
    PuppeteerPrintService puppeteerPrintService;

    @Autowired
    PostProcessService postProcessService;

    @PostMapping("/generate")
    public ResponseEntity<InputStreamResource> index(@RequestBody UrlToPdfRequest info) {
        List<InputStream> pdfs = generateBasicPdfs(info.getUrls());
        InputStream mergedPdf = mergePdfs(pdfs);
        InputStream mergedPdfsWithFooter = appendFooterToPdf(info.getFooter(), mergedPdf);
        String filename = buildPdfFileName(info.getUrls());
        HttpHeaders headers = buildHttpHeaders(filename);
        return new ResponseEntity<>(new InputStreamResource(mergedPdfsWithFooter), headers, HttpStatus.OK);
    }

    private List<InputStream> generateBasicPdfs(@RequestBody List<String> urls) {
        return urls.stream().map(url -> puppeteerPrintService.toPdf(url)).collect(Collectors.toList());
    }

    private InputStream mergePdfs(List<InputStream> pdfs) {
        if(pdfs.size() <= 1) {
            return pdfs.get(0);
        }
        ByteArrayOutputStream outPdf = new ByteArrayOutputStream();
        postProcessService.mergePdfFiles(pdfs, outPdf);
        return new ByteArrayInputStream(outPdf.toByteArray());
    }

    private InputStream appendFooterToPdf(Footer footer, InputStream mergedPdf) {
        if(footer == null) {
            return mergedPdf;
        }
        ByteArrayOutputStream outPdf = new ByteArrayOutputStream();
        postProcessService.addFooter(mergedPdf, footer, outPdf);
        return new ByteArrayInputStream(outPdf.toByteArray());
    }

    private HttpHeaders buildHttpHeaders(String filename) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/pdf"));
        headers.setContentDispositionFormData(filename, filename);
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
        return headers;
    }

    private String buildPdfFileName(List<String> urls) {
        String url = urls.get(0);
        String result = url;
        while (result.endsWith("/")) {
            result = result.substring(0, result.lastIndexOf('/'));
        }

        String name = result.substring(result.lastIndexOf('/') + 1);
        name = name.replaceAll("[^a-zA-Z0-9\\-_]+", "_");

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");

        name += "_";
        name += simpleDateFormat.format(new Date());
        name += ".pdf";

        return name;
    }
}
