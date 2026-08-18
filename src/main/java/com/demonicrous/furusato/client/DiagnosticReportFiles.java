package com.demonicrous.furusato.client;

import java.io.File;

/** Selects report names without replacing an earlier export. */
final class DiagnosticReportFiles {
    private DiagnosticReportFiles() {
    }

    static File uniqueFile(File directory, String timestamp) {
        File candidate = reportFile(directory, timestamp, 0);
        int suffix = 1;
        while (candidate.exists()) {
            candidate = reportFile(directory, timestamp, suffix++);
        }
        return candidate;
    }

    private static File reportFile(File directory, String timestamp, int suffix) {
        return new File(directory, "furusato-diagnostics-" + timestamp
                + (suffix == 0 ? "" : "-" + suffix) + ".txt");
    }
}
