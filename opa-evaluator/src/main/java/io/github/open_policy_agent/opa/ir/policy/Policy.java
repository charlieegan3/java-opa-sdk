package io.github.open_policy_agent.opa.ir.policy;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Represents a planned policy query. */
public class Policy {
  // Field is named "staticField" because "static" is a Java reserved word.
  // Accessors are named getStatic / setStatic so Jackson auto-detects the JSON "static" key.
  private Static staticField;

  private Plans plans;

  private Funcs funcs;

  private List<UnplannedRule> unplannedRules;

  public Policy(Static staticField, Plans plans, Funcs funcs, List<UnplannedRule> unplannedRules) {
    this.staticField = staticField;
    this.plans = plans;
    this.funcs = funcs;
    this.unplannedRules = unplannedRules;
  }

  public Policy(Static staticField, Plans plans, Funcs funcs) {
    this(staticField, plans, funcs, null);
  }

  public Policy() {}

  @Override
  public String toString() {
    return "Policy{"
        + "staticField="
        + staticField
        + ", plans="
        + plans
        + ", funcs="
        + funcs
        + ", unplannedRules="
        + unplannedRules
        + '}';
  }

  public Static getStatic() {
    return staticField;
  }

  /**
   * The plan's file table: index -&gt; filename, in the order the planner assigned indices.
   *
   * <p>Coverage data identifies source locations by file index, so consumers need this to resolve
   * an index back to a path. Indices are plan-local, so a table from one policy must never be used
   * to resolve another's.
   *
   * @return filenames by plan file index
   */
  public List<String> getStaticFilenames() {
    if (staticField == null || staticField.getFiles() == null) {
      return List.of();
    }
    List<String> filenames = new ArrayList<>(staticField.getFiles().size());
    for (StringConst file : staticField.getFiles()) {
      filenames.add(file == null ? null : file.getValue());
    }
    return filenames;
  }

  public void setStatic(Static staticField) {
    this.staticField = staticField;
  }

  public Plans getPlans() {
    return plans;
  }

  public void setPlans(Plans plans) {
    this.plans = plans;
  }

  public Funcs getFuncs() {
    return funcs;
  }

  public void setFuncs(Funcs funcs) {
    this.funcs = funcs;
  }

  public List<UnplannedRule> getUnplannedRules() {
    return unplannedRules;
  }

  public void setUnplannedRules(List<UnplannedRule> unplannedRules) {
    this.unplannedRules = unplannedRules;
  }

  @Override
  public boolean equals(Object o) {
      if (this == o) {
          return true;
      }
      if (o == null || getClass() != o.getClass()) {
          return false;
      }

    Policy policy = (Policy) o;

      if (!Objects.equals(staticField, policy.staticField)) {
          return false;
      }
      if (!Objects.equals(plans, policy.plans)) {
          return false;
      }
      if (!Objects.equals(funcs, policy.funcs)) {
          return false;
      }
    return Objects.equals(unplannedRules, policy.unplannedRules);
  }

  @Override
  public int hashCode() {
    int result = staticField != null ? staticField.hashCode() : 0;
    result = 31 * result + (plans != null ? plans.hashCode() : 0);
    result = 31 * result + (funcs != null ? funcs.hashCode() : 0);
    result = 31 * result + (unplannedRules != null ? unplannedRules.hashCode() : 0);
    return result;
  }
}
