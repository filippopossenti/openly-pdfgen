package it.openly.pdfgen.models;

import lombok.Data;

@Data
public class Footer {
    String templateText;
    Float fontSize;
    Float bottomMargin;
    Float leftMargin;
    Float rightMargin;
}
