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

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@RestController
public class IndexController {

    private final PuppeteerPrintService puppeteerPrintService;
    private final PostProcessService postProcessService;

    @Autowired
    public IndexController(PuppeteerPrintService puppeteerPrintService, PostProcessService postProcessService) {
        this.puppeteerPrintService = puppeteerPrintService;
        this.postProcessService = postProcessService;
    }

    @PostMapping("/generate")
    public ResponseEntity<InputStreamResource> index(@RequestBody UrlToPdfRequest info) {
        List<InputStream> pdfs = puppeteerPrintService.toPdfs(info.getUrls());
        InputStream mergedPdf = postProcessService.mergePdfFiles(pdfs);
        InputStream mergedPdfsWithFooter = postProcessService.addFooter(info.getFooter(), mergedPdf);
        String filename = buildPdfFileName(info.getUrls().get(0));
        HttpHeaders headers = buildHttpHeaders(filename);
        return new ResponseEntity<>(new InputStreamResource(mergedPdfsWithFooter), headers, HttpStatus.OK);
    }

    private HttpHeaders buildHttpHeaders(String filename) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/pdf"));
        headers.setContentDispositionFormData(filename, filename);
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
        return headers;
    }

    private String buildPdfFileName(String url) {
        String result = url;
        while (result.endsWith("/")) {
            result = result.substring(0, result.lastIndexOf('/'));
        }

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");

        return result.substring(result.lastIndexOf('/') + 1).replaceAll("[^a-zA-Z0-9\\-_]+", "_")
                + "_"
                + simpleDateFormat.format(new Date())
                + ".pdf";
    }
}
