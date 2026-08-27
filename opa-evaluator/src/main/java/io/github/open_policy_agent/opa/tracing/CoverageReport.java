package io.github.open_policy_agent.opa.tracing;

import io.github.open_policy_agent.opa.ir.policy.UnplannedRule;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * The computed coverage result for one policy: which source ranges executed, which did not, the
 * distinct-line counts, and the coverage percentage. Mirrors the shape of OPA's {@code opa eval
 * --coverage} output.
 *
 * <p>This holds the coverage figures themselves, computed once from a {@link CoverageProfiler}. A
 * {@link CoverageReportWriter} turns the model into JSON.
 *
 * <p>Each executed statement range is reported individually, sorted by position and not coalesced,
 * matching OPA. {@code covered}/{@code notCovered} may be empty and the line counts count distinct
 * source rows. File indices outside {@code filenames} are skipped (synthetic statements with no
 * source mapping). {@code notCovered} ranges come from the plan's {@code unplanned_rules}: rules
 * the planner never compiled into statements, so they can never be covered.
 */
public record CoverageReport(
    Map<String, FileCoverage> files, int coveredLines, int notCoveredLines, double coverage) {

  /**
   * Per-file coverage. {@code covered} and {@code notCovered} are sorted by position and may be
   * empty; {@code coveredLines}/{@code notCoveredLines} count distinct source rows.
   */
  public record FileCoverage(
      List<Range> covered,
      List<Range> notCovered,
      int coveredLines,
      int notCoveredLines,
      double coverage) {}

  /**
   * Build the report from recorded coverage.
   *
   * @param profiler the profiler that recorded coverage during evaluation
   * @param filenames file index to filename mapping, typically {@code policy.getStaticFilenames()}
   * @param unplannedRules rules to report as not covered, typically {@code
   *     policy.getUnplannedRules()}; may be null
   * @return the computed, library-neutral report
   */
  public static CoverageReport from(
      CoverageProfiler profiler, List<String> filenames, List<UnplannedRule> unplannedRules) {
    Map<Integer, Set<Range>> coveredByFile = profiler.getCoveredRanges();
    Map<Integer, List<Range>> notCoveredByFile = notCoveredRangesByFile(unplannedRules);

    Set<Integer> fileIndices = new TreeSet<>(coveredByFile.keySet());
    fileIndices.addAll(notCoveredByFile.keySet());

    Map<String, FileCoverage> files = new LinkedHashMap<>();
    int totalCovered = 0;
    int totalNotCovered = 0;
    for (int fileIndex : fileIndices) {
      if (fileIndex < 0 || fileIndex >= filenames.size()) {
        continue;
      }

      Set<Range> covered = coveredByFile.get(fileIndex);
      List<Range> notCovered = notCoveredByFile.get(fileIndex);
      boolean hasCovered = covered != null && !covered.isEmpty();
      boolean hasNotCovered = notCovered != null && !notCovered.isEmpty();
      if (!hasCovered && !hasNotCovered) {
        continue;
      }

      int coveredLines = countLines(covered);
      int notCoveredLines = countLines(notCovered);
      files.put(
          filenames.get(fileIndex),
          new FileCoverage(
              sorted(covered),
              sorted(notCovered),
              coveredLines,
              notCoveredLines,
              coveragePercent(coveredLines, notCoveredLines)));
      totalCovered += coveredLines;
      totalNotCovered += notCoveredLines;
    }

    return new CoverageReport(
        files, totalCovered, totalNotCovered, coveragePercent(totalCovered, totalNotCovered));
  }

  /** Distinct source rows spanned by {@code ranges}, matching OPA's line-based counts. */
  private static int countLines(Collection<Range> ranges) {
    if (ranges == null || ranges.isEmpty()) {
      return 0;
    }
    Set<Integer> rows = new TreeSet<>();
    for (Range range : ranges) {
      for (int row = range.start().getRow(); row <= range.end().getRow(); row++) {
        rows.add(row);
      }
    }
    return rows.size();
  }

  private static List<Range> sorted(Collection<Range> ranges) {
    if (ranges == null || ranges.isEmpty()) {
      return List.of();
    }
    List<Range> result = new ArrayList<>(ranges);
    result.sort(Range::compareTo);
    return result;
  }

  private static double coveragePercent(int coveredLines, int notCoveredLines) {
    int total = coveredLines + notCoveredLines;
    return total == 0 ? 0.0 : (double) coveredLines / total * 100;
  }

  private static Map<Integer, List<Range>> notCoveredRangesByFile(
      List<UnplannedRule> unplannedRules) {
    Map<Integer, List<Range>> byFile = new HashMap<>();
    if (unplannedRules == null) {
      return byFile;
    }
    for (UnplannedRule rule : unplannedRules) {
      byFile
          .computeIfAbsent(rule.getLocation().getFile(), k -> new ArrayList<>())
          .add(Range.of(rule.getLocation()));
    }
    return byFile;
  }
}
