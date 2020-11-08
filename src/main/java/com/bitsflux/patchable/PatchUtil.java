package com.bitsflux.patchable;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import lombok.extern.slf4j.Slf4j;

import java.util.Iterator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Slf4j
final class PatchUtil {

    public static JsonNode merge(JsonNode patchNode, JsonNode targetNode, Class patchClass, Class targetClass) {
        return merge(patchNode, targetNode, FieldSetMeta.of(patchClass), PatchableMeta.of(targetClass));
    }

    private static JsonNode merge(
            JsonNode patchNode,
            JsonNode targetNode,
            FieldSetMeta fieldSetMeta,
            PatchableMeta patchableMeta) {

        asStream(patchNode.fieldNames(), false)
                .filter(field -> {
                    boolean included = fieldSetMeta.included(field);
                    if(!included) {
                        log.debug("Field `{}` is ignored as per {} in patch JSON: {}", field, fieldSetMeta.patchClass(), patchNode);
                    }
                    return included;
                })
                .filter(field -> {
                    boolean fieldExists = targetNode.get(field) != null;
                    if(!fieldExists) {
                        log.debug("Field `{}` doesn't exist in `{}`", field, patchableMeta.targetClass());
                    }
                    boolean fieldPatchable = patchableMeta.patchable(field);
                    if(!fieldPatchable) {
                        log.debug("Field `{}` is not patchable in `{}`", field, patchableMeta.targetClass());
                    }
                    return fieldExists && fieldPatchable;
                })
                .forEach(patchFieldName -> {
                    final JsonNode targetFieldNode = targetNode.get(patchFieldName);
                    final JsonNode patchFieldNode = getPatchFieldNode(patchNode, patchFieldName);

                    if (isArrayNode(targetFieldNode) && patchFieldNode.isArray()) {
                        mergeArray((ArrayNode) targetFieldNode, (ArrayNode) patchFieldNode);
                    } else if(patchFieldNode.isNull() && targetFieldNode != null) {
                        ((ObjectNode) targetNode).replace(patchFieldName, null);
                    } else if (targetFieldNode != null && targetFieldNode.isObject()) {
                        merge(patchFieldNode, targetFieldNode, fieldSetMeta.field(patchFieldName), patchableMeta.field(patchFieldName));
                    } else if(targetNode instanceof ObjectNode) {
                        ((ObjectNode) targetNode).replace(patchFieldName, patchFieldNode);
                    }
                });

        return targetNode;
    }

    public static <T> Stream<T> asStream(Iterator<T> sourceIterator, boolean parallel) {
        Iterable<T> iterable = () -> sourceIterator;
        return StreamSupport.stream(iterable.spliterator(), parallel);
    }


    private static boolean isArrayNode(final JsonNode valueToBeUpdated) {
        return valueToBeUpdated != null && valueToBeUpdated.isArray();
    }

    private static void mergeArray(final ArrayNode valueToBeUpdated, final ArrayNode updatedValue) {
        valueToBeUpdated.removeAll();
        valueToBeUpdated.addAll(updatedValue);
    }

    private static JsonNode getPatchFieldNode(final JsonNode patchNode, final String patchFieldName) {

        JsonNode patchFieldNode = patchNode.get(patchFieldName);

        if(patchFieldNode.isTextual()) {
            final TextNode textNode = (TextNode) patchFieldNode;
            if(isBlank(textNode.asText())) {
                return new TextNode(null);
            }
            final String trimmed = textNode.asText().trim();
            if (!trimmed.equals(textNode.asText())) {
                return new TextNode(trimmed);
            }
        }
        return patchFieldNode;
    }

    private static boolean isBlank(String text) {
        return text == null || "".equals(text);
    }
}
