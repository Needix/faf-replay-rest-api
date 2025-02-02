package de.needix.games.faf.replay.analyser.parser;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.needix.games.faf.replay.api.entities.summarystats.BlueprintStats;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class BlueprintsDeserializer extends JsonDeserializer<Map<String, BlueprintStats>> {

    @Override
    public Map<String, BlueprintStats> deserialize(JsonParser parser, DeserializationContext context)
            throws IOException {

        // Check if current JSON token is an array
        if (parser.currentToken() == JsonToken.START_ARRAY) {
            // Skip the entire array and return an empty map
            parser.skipChildren();
            return Collections.emptyMap();
        }

        // Otherwise, treat it as an object and deserialize into a Map
        ObjectMapper mapper = (ObjectMapper) parser.getCodec();
        return mapper.readValue(parser, new TypeReference<LinkedHashMap<String, BlueprintStats>>() {
        });
    }
}