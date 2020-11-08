package com.bitsflux.patchable;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.validation.Validated;
import lombok.Getter;
import lombok.experimental.Accessors;

import javax.validation.Valid;

@Introspected
@Validated
@Accessors(chain = true)
public class PatchInfo<Patch, Target> {
    private final ObjectMapper objectMapper;
    private final Class patchClass;
    private final Class targetClass;
    private final JsonNode patchNode;

    public PatchInfo(ObjectMapper objectMapper, Class<Patch> patchClass, Class<Target> targetClass, JsonNode patchNode) {
        if(objectMapper == null) {
            throw new IllegalArgumentException("objectMapper is missing");
        }
        if(patchClass == null) {
            throw new IllegalArgumentException("patchClass is missing");
        }
        if(targetClass == null) {
            throw new IllegalArgumentException("targetClass is missing");
        }
        if(patchNode == null || patchNode.isMissingNode()) {
            throw new IllegalArgumentException("patchNode is missing");
        }
        this.objectMapper = objectMapper;
        this.patchClass = patchClass;
        this.targetClass = targetClass;
        this.patchNode = patchNode;
        this.patch = objectMapper.convertValue(patchNode, patchClass);
    }

    @Valid
    @Getter
    private final Patch patch;

    public Target merge(@Valid Target target) {
        JsonNode targetNode = objectMapper.convertValue(target, JsonNode.class);
        return (Target) objectMapper.convertValue(
                PatchUtil.merge(patchNode, targetNode, patchClass, targetClass),
                target.getClass()
        );
    }
}
