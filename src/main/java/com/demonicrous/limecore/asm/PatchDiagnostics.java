package com.demonicrous.limecore.asm;

import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Collects the state of early bytecode patches for one startup. */
public final class PatchDiagnostics {
    private static final Logger LOGGER = LogManager.getLogger("Lime Core/Diagnostics");
    private static final Map<String, Result> RESULTS = new LinkedHashMap<String, Result>();

    private PatchDiagnostics() {
    }

    public static synchronized void register(String patch) {
        if (!RESULTS.containsKey(patch)) {
            RESULTS.put(patch, new Result("REGISTERED", "waiting for target class"));
        }
    }

    public static synchronized void applied(String patch, String detail) {
        RESULTS.put(patch, new Result("APPLIED", detail));
    }

    public static synchronized void disabled(String patch) {
        RESULTS.put(patch, new Result("DISABLED", "disabled by configuration"));
    }

    public static synchronized void skipped(String patch, String detail) {
        RESULTS.put(patch, new Result("SKIPPED", detail));
    }

    public static synchronized void failed(String patch, Throwable error) {
        RESULTS.put(patch, new Result("FAILED", error.getClass().getSimpleName()
                + ": " + String.valueOf(error.getMessage())));
    }

    public static synchronized void logSummary() {
        if (!LimeCoreEarlyConfig.isDiagnosticLoggingEnabled()) {
            return;
        }
        LOGGER.info("Lime Core patch diagnostics:");
        for (Map.Entry<String, Result> entry : RESULTS.entrySet()) {
            LOGGER.info("  {}: {} ({})", entry.getKey(),
                    entry.getValue().status, entry.getValue().detail);
        }
    }

    private static final class Result {
        private final String status;
        private final String detail;

        private Result(String status, String detail) {
            this.status = status;
            this.detail = detail;
        }
    }
}

