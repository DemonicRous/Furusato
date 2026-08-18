package com.demonicrous.furusato.client;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import org.junit.Test;

public class DiagnosticReportFilesTest {
    @Test
    public void addsSuffixInsteadOfReplacingExistingReport() throws IOException {
        File directory = Files.createTempDirectory("furusato-report-test").toFile();
        File first = DiagnosticReportFiles.uniqueFile(directory, "2026-08-18_12-00-00");
        first.createNewFile();

        File second = DiagnosticReportFiles.uniqueFile(directory, "2026-08-18_12-00-00");

        assertEquals("furusato-diagnostics-2026-08-18_12-00-00-1.txt",
                second.getName());
    }
}
