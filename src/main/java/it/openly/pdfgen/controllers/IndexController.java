package it.openly.pdfgen.controllers;

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

@RestController
public class IndexController {

    @Autowired
    PuppeteerPrintService puppeteerPrintService;

    @Autowired
    PostProcessService postProcessService;

    @PostMapping("/generate")
    public ResponseEntity<InputStreamResource> index(@RequestBody UrlToPdfRequest info) {
        List<InputStream> pdfs = new ArrayList<>();
        for(String url : info.getUrls()) {
            InputStream data = puppeteerPrintService.toPdf(url);
            pdfs.add(data);
        }
        InputStream data;
        if(pdfs.size() > 1) {
            ByteArrayOutputStream outPdf = new ByteArrayOutputStream();
            postProcessService.mergePdfFiles(pdfs, outPdf);
            data = new ByteArrayInputStream(outPdf.toByteArray());
        }
        else {
            data = pdfs.get(0);
        }

        ByteArrayOutputStream outPdf = new ByteArrayOutputStream();
        if(info.getFooter() != null) {
            postProcessService.addFooter(data, info.getFooter(), outPdf);
            data = new ByteArrayInputStream(outPdf.toByteArray());
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/pdf"));
        // Here you have to set the actual filename of your pdf
        String filename = buildPdfFileName(info.getUrls());
        headers.setContentDispositionFormData(filename, filename);
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
        return new ResponseEntity<>(new InputStreamResource(data), headers, HttpStatus.OK);
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
