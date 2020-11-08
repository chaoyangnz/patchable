package com.bitsflux.patchable;

import lombok.RequiredArgsConstructor;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class FieldSetMeta<T> {
    private static Map<Class, FieldSetMeta> fieldSetCache = new HashMap<>();

    private final Class<T> patchClass;
    private final Map<String, FieldSetMeta> fields;

    public FieldSetMeta field(String field) {
        return fields.get(field);
    }

    public boolean included(String field) {
        return fields.containsKey(field);
    }

    public Class<T> patchClass() {
        return patchClass;
    }

    public static FieldSetMeta of(Class patchClass) {
        if(patchClass == null) {
            return null;
        }

        if(fieldSetCache.containsKey(patchClass)) {
            return fieldSetCache.get(patchClass);
        }

        if(Number.class.isAssignableFrom(patchClass) || CharSequence.class.isAssignableFrom(patchClass)) {
            return new FieldSetMeta(patchClass, Map.of());
        }
        FieldSetMeta fieldSetMeta = new FieldSetMeta(patchClass,
                Arrays.stream(patchClass.getDeclaredFields())
                        .collect(Collectors.toMap(Field::getName, (field) -> of(field.getType()))));
        fieldSetCache.put(patchClass, fieldSetMeta);
        return fieldSetMeta;
    }
}
