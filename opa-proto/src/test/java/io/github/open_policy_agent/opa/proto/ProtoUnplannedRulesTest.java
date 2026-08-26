package io.github.open_policy_agent.opa.proto;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.open_policy_agent.opa.ir.Location;
import io.github.open_policy_agent.opa.ir.policy.Policy;
import io.github.open_policy_agent.opa.ir.policy.UnplannedRule;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import opa.ir.v1.Plans;
import org.junit.jupiter.api.Test;

/** Verifies {@code unplanned_rules} decode into the SDK's {@link Policy} model. */
class ProtoUnplannedRulesTest {

  @Test
  void unplannedRulesDecodeIntoSdkModel() throws IOException {
    opa.ir.v1.Policy proto =
        opa.ir.v1.Policy.newBuilder()
            .setPlans(Plans.newBuilder().build())
            .addUnplannedRules(
                opa.ir.v1.UnplannedRule.newBuilder()
                    .setPath("data.example.unused")
                    .setLocation(
                        opa.ir.v1.Location.newBuilder()
                            .setFile(0)
                            .setCol(1)
                            .setRow(11)
                            .setEndCol(15)
                            .setEndRow(11)))
            .build();

    Policy policy = new ProtoBundleReader().decodePlan(new ByteArrayInputStream(proto.toByteArray()));

    assertThat(policy.getUnplannedRules())
        .containsExactly(new UnplannedRule("data.example.unused", new Location(0, 1, 11, 15, 11)));
  }
}
