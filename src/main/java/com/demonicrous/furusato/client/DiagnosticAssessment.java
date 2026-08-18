package com.demonicrous.furusato.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Converts raw runtime state into stable, user-facing diagnostic findings. */
final class DiagnosticAssessment {
    private DiagnosticAssessment() {
    }

    static List<Finding> assess(String patchStatus, boolean resourcesAvailable,
            boolean safeMode, int thirdPartyTransformerCount) {
        List<Finding> findings = new ArrayList<Finding>();
        if (!resourcesAvailable) {
            findings.add(Finding.MISSING_RESOURCES);
        }
        if ("FAILED".equals(patchStatus)) {
            findings.add(Finding.PATCH_FAILED);
        } else if ("SKIPPED".equals(patchStatus)) {
            findings.add(Finding.PATCH_SKIPPED);
        } else if ("UNKNOWN".equals(patchStatus)
                || "REGISTERED".equals(patchStatus)) {
            findings.add(Finding.PATCH_UNKNOWN);
        }
        if (safeMode) {
            findings.add(Finding.SAFE_MODE);
        }
        if (thirdPartyTransformerCount > 0) {
            findings.add(Finding.THIRD_PARTY_TRANSFORMERS);
        }
        return Collections.unmodifiableList(findings);
    }

    enum Finding {
        MISSING_RESOURCES("missingResources"),
        PATCH_FAILED("patchFailed"),
        PATCH_SKIPPED("patchSkipped"),
        PATCH_UNKNOWN("patchUnknown"),
        SAFE_MODE("safeMode"),
        THIRD_PARTY_TRANSFORMERS("thirdPartyTransformers");

        private final String translationSuffix;

        Finding(String translationSuffix) {
            this.translationSuffix = translationSuffix;
        }

        String titleKey() {
            return "furusato.diagnostics.issue." + translationSuffix;
        }

        String adviceKey() {
            return titleKey() + ".advice";
        }
    }
}
