package io.github.open_policy_agent.opa.tracing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.open_policy_agent.opa.ir.Location;
import io.github.open_policy_agent.opa.ir.policy.UnplannedRule;
import java.util.List;
import org.junit.jupiter.api.Test;

class CoverageReportTest {

  @Test
  void from_recordsCoveredRangesSortedPerFile() {
    CoverageProfiler profiler = new CoverageProfiler();
    profiler.addEntry(new Location(0, 5, 6, 12, 6), 0); // row 6
    profiler.addEntry(new Location(0, 2, 5, 14, 5), 0); // row 5

    CoverageReport report = CoverageReport.from(profiler, List.of("policy.rego"), List.of());

    CoverageReport.FileCoverage file = report.files().get("policy.rego");
    assertEquals(2, file.covered().size());
    // Sorted by position: row 5 before row 6.
    assertEquals(5, file.covered().get(0).start().getRow());
    assertEquals(6, file.covered().get(1).start().getRow());
    assertTrue(file.notCovered().isEmpty());
  }

  @Test
  void from_addsUnplannedRulesAsNotCovered() {
    CoverageProfiler profiler = new CoverageProfiler();
    profiler.addEntry(new Location(0, 1, 5, 10, 5), 0);
    List<UnplannedRule> unplannedRules =
        List.of(new UnplannedRule("data.example.unused", new Location(0, 1, 11, 15, 11)));

    CoverageReport report =
        CoverageReport.from(profiler, List.of("policy.rego"), unplannedRules);

    CoverageReport.FileCoverage file = report.files().get("policy.rego");
    assertEquals(1, file.covered().size());
    assertEquals(1, file.notCovered().size());
    assertEquals(11, file.notCovered().get(0).start().getRow());
  }

  @Test
  void from_countsDistinctLinesAndCoveragePerFileAndOverall() {
    CoverageProfiler profiler = new CoverageProfiler();
    profiler.addEntry(new Location(0, 1, 5, 10, 5), 0); // covered row 5
    List<UnplannedRule> unplannedRules =
        // not covered rows 11..12 (a two-line rule body)
        List.of(new UnplannedRule("data.example.unused", new Location(0, 1, 11, 15, 12)));

    CoverageReport report =
        CoverageReport.from(profiler, List.of("policy.rego"), unplannedRules);

    CoverageReport.FileCoverage file = report.files().get("policy.rego");
    assertEquals(1, file.coveredLines());
    assertEquals(2, file.notCoveredLines());
    assertEquals(1.0 / 3 * 100, file.coverage());

    assertEquals(1, report.coveredLines());
    assertEquals(2, report.notCoveredLines());
    assertEquals(1.0 / 3 * 100, report.coverage());
  }

  @Test
  void from_skipsFileIndicesOutsideFilenameList() {
    CoverageProfiler profiler = new CoverageProfiler();
    profiler.addEntry(new Location(0, 1, 5, 10, 5), 0);
    profiler.addEntry(new Location(7, 1, 5, 10, 5), 0); // index 7 has no filename

    CoverageReport report = CoverageReport.from(profiler, List.of("policy.rego"), List.of());

    assertEquals(1, report.files().size());
    assertTrue(report.files().containsKey("policy.rego"));
  }

  @Test
  void from_toleratesNullUnplannedRules() {
    CoverageProfiler profiler = new CoverageProfiler();
    profiler.addEntry(new Location(0, 1, 5, 10, 5), 0);

    CoverageReport report = CoverageReport.from(profiler, List.of("policy.rego"), null);

    CoverageReport.FileCoverage file = report.files().get("policy.rego");
    assertFalse(file.covered().isEmpty());
    assertTrue(file.notCovered().isEmpty());
  }

  @Test
  void from_coverageIsZeroWhenNothingRecorded() {
    CoverageReport report =
        CoverageReport.from(new CoverageProfiler(), List.of("policy.rego"), List.of());

    assertTrue(report.files().isEmpty());
    assertEquals(0, report.coveredLines());
    assertEquals(0, report.notCoveredLines());
    assertEquals(0.0, report.coverage());
  }
}
