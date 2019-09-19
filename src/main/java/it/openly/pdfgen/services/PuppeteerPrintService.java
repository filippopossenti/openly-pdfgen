package it.openly.pdfgen.services;

import it.openly.pdfgen.exceptions.WrongExitValueException;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.*;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PuppeteerPrintService {

    @Value("${output.dir}")
    String outputDir;

    @Value("${work.dir}")
    String workDir;

    @Value("${dependency.npm.puppeteer}")
    String puppeteerDependencyNameAndVersion;

    @Autowired
    NodeService nodeService;

    @Autowired
    ResourceLoader resourceLoader;


    @SneakyThrows
    @PostConstruct
    public void setup() {
        String nodeVersion = nodeService.getInstalledVersion();
        log.info("Installed node version: {}", nodeVersion);

        if(!nodeService.npmIsInstalled(workDir, puppeteerDependencyNameAndVersion)) {
            log.info("The puppeteer module is not installed in the '{}' directory. The module will now be downloaded and installed.", workDir);
            log.info("If the server doesn't have internet connectivity, install the module manually by proceeding as follows:");
            log.info("  1. create the '{}' directory on another machine with Internet access.", workDir);
            log.info("  2. go in the newly created directory and execute 'npm install puppeteer'");
            log.info("  3. zip all the contents of the '{}' directory", workDir);
            log.info("  4. copy the zip file on this machine.");
            log.info("  5. extract the zip file contents in the '{}' directory.", workDir);
            log.info("Note that the machine must have the same architecture and operating system in order for the operation to succeed.");
            nodeService.npmInstall(workDir, puppeteerDependencyNameAndVersion);
        }
    }

    public List<InputStream> toPdfs(List<String> urls) {
        return urls.stream().map(this::toPdf).collect(Collectors.toList());
    }


    @SneakyThrows
    private InputStream toPdf(String url) {
        File outDir = new File(outputDir);
        if(!outDir.exists()) {
            outDir.mkdirs();
        }

        String uuid = UUID.randomUUID().toString();
        String outFileName = outputDir + uuid + ".pdf";

        Resource reportScript = resourceLoader.getResource("classpath:scripts/report.js");

        IOUtils.copy(reportScript.getInputStream(), new FileOutputStream(new File(workDir, "report.js")));

        ProcessBuilder pb = new ProcessBuilder("cmd", "/c", "node", "report.js", "--", "--url=" + url, "--outfile=" + outFileName);

        pb.redirectErrorStream();
        pb.directory(new File(workDir));
        Process p = pb.start();
        p.waitFor(120, TimeUnit.SECONDS);
        int exitValue = p.exitValue();
        if(exitValue == 0) {
            try(InputStream pdf = new FileInputStream(outFileName)) {
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                IOUtils.copy(pdf, os);
                return new ByteArrayInputStream(os.toByteArray());
            }
            finally {
                (new File(outFileName)).delete();
            }
        }
        else {
            throw new WrongExitValueException(exitValue);
        }
    }


}
