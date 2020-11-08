package com.bitsflux.patchable;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.core.annotation.Introspected;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Introspected
@Accessors(chain = true)
@RequiredArgsConstructor
public class PatchInfo<Patch, Target> {
    private final ObjectMapper objectMapper;
    private final Class patchClass;
    private final Class targetClass;

    @Valid
    @Getter @Setter
    private Patch patch;
    @NotNull
    @Getter @Setter
    private String json;

    public Target merge(@Valid Target target) {
        try {
//            objectMapper.setSerializationInclusion(JsonInclude.Include.ALWAYS);
            JsonNode patchNode = objectMapper.readTree(json);
            JsonNode targetNode = objectMapper.convertValue(target, JsonNode.class);
            return (Target) objectMapper.convertValue(
                    PatchUtil.merge(patchNode, targetNode, targetClass),
                    target.getClass()
            );
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Failed to merge");
        }
    }
}
