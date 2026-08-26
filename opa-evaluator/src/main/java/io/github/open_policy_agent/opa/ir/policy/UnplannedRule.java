package io.github.open_policy_agent.opa.ir.policy;

import io.github.open_policy_agent.opa.ir.Location;
import java.util.Objects;

/**
 * A rule not reachable from the build-time entrypoint, so the planner never compiled it. Included
 * so that, when tracing is enabled, evaluators can report coverage data for rules the plan never
 * touches.
 */
public class UnplannedRule {
  private String path;

  private Location location;

  public UnplannedRule() {}

  public UnplannedRule(String path, Location location) {
    this.path = path;
    this.location = location;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public Location getLocation() {
    return location;
  }

  public void setLocation(Location location) {
    this.location = location;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    UnplannedRule that = (UnplannedRule) o;

    if (!Objects.equals(path, that.path)) {
      return false;
    }
    return Objects.equals(location, that.location);
  }

  @Override
  public int hashCode() {
    int result = path != null ? path.hashCode() : 0;
    result = 31 * result + (location != null ? location.hashCode() : 0);
    return result;
  }
}
