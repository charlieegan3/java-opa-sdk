package io.github.open_policy_agent.opa.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import io.github.open_policy_agent.opa.ir.Location;
import java.io.IOException;

/**
 * Deserializes a standalone {@code Location} JSON object ({@code file/col/row/end_col/end_row}),
 * e.g. {@code UnplannedRule.location}. Until now, every JSON occurrence of these fields was
 * flattened onto a statement envelope and handled by {@link StmtDeserializer}; {@code
 * UnplannedRule} is the first place a {@code Location} appears as its own nested object, hence
 * this dedicated deserializer.
 */
class LocationDeserializer extends JsonDeserializer<Location> {
  @Override
  public Location deserialize(JsonParser jp, DeserializationContext ctx) throws IOException {
    JsonNode node = jp.getCodec().readTree(jp);
    return new Location(
        node.path("file").asInt(),
        node.path("col").asInt(),
        node.path("row").asInt(),
        node.path("end_col").asInt(),
        node.path("end_row").asInt());
  }
}
