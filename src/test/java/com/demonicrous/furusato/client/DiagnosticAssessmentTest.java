package com.demonicrous.furusato.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import org.junit.Test;

public class DiagnosticAssessmentTest {
    @Test
    public void healthyStateHasNoFindings() {
        assertTrue(DiagnosticAssessment.assess("APPLIED", true, false, 0).isEmpty());
    }

    @Test
    public void reportsEveryIndependentProblem() {
        List<DiagnosticAssessment.Finding> findings = DiagnosticAssessment.assess(
                "FAILED", false, true, 2);

        assertEquals(4, findings.size());
        assertTrue(findings.contains(DiagnosticAssessment.Finding.MISSING_RESOURCES));
        assertTrue(findings.contains(DiagnosticAssessment.Finding.PATCH_FAILED));
        assertTrue(findings.contains(DiagnosticAssessment.Finding.SAFE_MODE));
        assertTrue(findings.contains(
                DiagnosticAssessment.Finding.THIRD_PARTY_TRANSFORMERS));
    }

    @Test
    public void registeredPatchIsStillPending() {
        assertEquals(DiagnosticAssessment.Finding.PATCH_UNKNOWN,
                DiagnosticAssessment.assess("REGISTERED", true, false, 0).get(0));
    }
}
