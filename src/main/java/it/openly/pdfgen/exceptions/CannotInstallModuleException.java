package it.openly.pdfgen.exceptions;

public class CannotInstallModuleException extends RuntimeException {
    public CannotInstallModuleException(String moduleName) {
        super("Could not install module " + moduleName + " Please make sure that node is installed, that Internet connectivity is working and that the specified module exists in the npm repository.");
    }
}
