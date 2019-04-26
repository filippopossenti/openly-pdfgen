package it.openly.pdfgen.models;

import lombok.Data;

import java.util.List;

@Data
public class UrlToPdfRequest {
    private List<String> urls;
    private Footer footer;
}
