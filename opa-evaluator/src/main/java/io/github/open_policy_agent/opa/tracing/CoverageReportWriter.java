package io.github.open_policy_agent.opa.tracing;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Serializes a computed {@link CoverageReport} to disk. Discovered by {@link CoverageRecorder} via
 * {@link java.util.ServiceLoader}; implementations live in the serialization modules, e.g.
 * {@code opa-jackson}.
 *
 * <p>All coverage semantics are computed upstream in {@link CoverageReport}; an implementation only
 * encodes that model in its JSON library and writes the bytes out.
 */
public interface CoverageReportWriter {

  /**
   * Write a single report to a distinct path under {@code outputDir}.
   *
   * @param report the computed, library-neutral coverage report
   * @param outputDir directory to write into; already created
   * @param baseName filename stem unique to this JVM and policy, without extension
   * @throws IOException if the report cannot be written
   */
  void write(CoverageReport report, Path outputDir, String baseName) throws IOException;
}
