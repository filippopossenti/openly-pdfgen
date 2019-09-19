package it.openly.pdfgen.exceptions;

public class WrongExitValueException extends RuntimeException {
    public WrongExitValueException(int exitValue) {
        super("Exit code " + exitValue + " was returned. The operation did not complete successfully.");
    }
}
