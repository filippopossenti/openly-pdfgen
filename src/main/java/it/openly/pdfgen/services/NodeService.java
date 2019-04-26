package it.openly.pdfgen.services;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class NodeService {

    @Autowired
    ProcessService processService;

    @SneakyThrows
    public String getInstalledVersion() {
        // check if node is installed. If not, don't start the service as it would be useless.
        Process p = processService.execute(null, "node", "-v");

        String result = processService.readConsoleLine(p);

        try {
            processService.waitSuccessfulExit(p, 30);
        }
        catch(Exception ex) {
            log.error("Execution failed. Is node installed?");
            throw ex;
        }
        p.destroyForcibly();

        return result;
    }

    @SneakyThrows
    public boolean npmIsInstalled(String where, String moduleName) {
        File dest = new File(where);
        if(!dest.exists()) {
            return false;
        }
        Process p = processService.execute(dest, "npm", "ls", moduleName);
        InputStream s = p.getInputStream();
        InputStreamReader rdr = new InputStreamReader(s);
        BufferedReader brdr = new BufferedReader(rdr);
        String result;
        int line = 0;
        while((result = brdr.readLine()) != null && line++ < 10) {
            if(result.contains(moduleName)) {
                return true;
            }
        }
        processService.waitExit(p, 30);
        p.destroyForcibly();
        return false;
    }


    @SneakyThrows
    public void npmInstall(String where, String moduleName) {
        File dest = new File(where);
        if(!dest.exists()) {
            dest.mkdirs();
        }
        log.info("Installing npm module '{}'", moduleName);
        Process p = processService.execute(dest, "npm", "install", moduleName);

        InputStream s = p.getInputStream();
        InputStreamReader rdr = new InputStreamReader(s);
        BufferedReader brdr = new BufferedReader(rdr);
        String result;
        while((result = brdr.readLine()) != null) {
            log.info(result);
        }

        processService.waitSuccessfulExit(p, 300);
        int exitValue = p.exitValue();
        if(exitValue != 0) {
            log.error("Could not install module '{}'", moduleName);
            throw new RuntimeException("Could not install a module. Please make sure that node is installed, that Internet connectivity is working and that the specified module exists in the npm repository.");
        }
        log.info("Installation of npm module '{}' completed.", moduleName);
    }

}
