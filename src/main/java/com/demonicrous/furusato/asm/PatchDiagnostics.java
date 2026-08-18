package com.demonicrous.furusato.asm;

import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Collects the state of early bytecode patches for one startup. */
public final class PatchDiagnostics {
    private static final Logger LOGGER = LogManager.getLogger("Furusato/Diagnostics");
    private static final Map<String, Result> RESULTS = new LinkedHashMap<String, Result>();

    private PatchDiagnostics() {
    }

    public static synchronized void register(String patch) {
        if (!RESULTS.containsKey(patch)) {
            RESULTS.put(patch, new Result("REGISTERED", "waiting for target class"));
        }
    }

    public static synchronized void applied(String patch, String detail) {
        update(patch, "APPLIED", detail);
    }

    public static synchronized void disabled(String patch) {
        update(patch, "DISABLED", "disabled by configuration");
    }

    public static synchronized void skipped(String patch, String detail) {
        update(patch, "SKIPPED", detail);
    }

    public static synchronized void failed(String patch, Throwable error) {
        update(patch, "FAILED", error.getClass().getSimpleName()
                + ": " + String.valueOf(error.getMessage()));
    }

    private static void update(String patch, String status, String detail) {
        RESULTS.put(patch, new Result(status, detail));
        if (!FurusatoEarlyConfig.isDiagnosticLoggingEnabled()) {
            return;
        }
        LOGGER.info("{}: {} ({})", patch, status, detail);
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
