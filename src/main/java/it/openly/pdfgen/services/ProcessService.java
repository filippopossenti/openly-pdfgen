package it.openly.pdfgen.services;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class ProcessService {
    @SneakyThrows
    public Process execute(File workDir, String... args) {
        List<String> parts = new ArrayList<>();
        for(int i = 0; i < args.length; i++) {
            parts.add(args[i]);
        }
        String os = System.getProperty("os.name").toLowerCase();
        if(os.contains("win")) {
            parts.add(0, "cmd");
            parts.add(1, "/c");
        }
        String[] pbargs = parts.toArray(new String[0]);


        ProcessBuilder pb = new ProcessBuilder(pbargs);
        if(workDir != null) {
            pb.directory(workDir);
        }
        pb.redirectErrorStream();
        return pb.start();
    }

    @SneakyThrows
    public String readConsoleLine(Process process) {
        InputStream s = process.getInputStream();
        InputStreamReader rdr = new InputStreamReader(s);
        BufferedReader brdr = new BufferedReader(rdr);
        return brdr.readLine();
    }

    @SneakyThrows
    public boolean waitSuccessfulExit(Process process, int timeoutSeconds) {
        if(!process.waitFor(timeoutSeconds, TimeUnit.SECONDS)) {
            return false;
        }
        int exitValue = process.exitValue();
        if(exitValue != 0) {
            log.warn("Process ended with code {}", exitValue);
            throw new RuntimeException("Process ended with code " + exitValue);
        }
        return true;
    }

    @SneakyThrows
    public boolean waitExit(Process process, int timeoutSeconds) {
        return process.waitFor(timeoutSeconds, TimeUnit.SECONDS);
    }

}
