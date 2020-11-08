package com.bitsflux.patchable;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class PatchableFields extends HashMap<String, PatchableFields> {
    public static PatchableFields EMPTY = new PatchableFields();

    private static Map<Class, PatchableFields> cached = new HashMap<>();

    public PatchableFields() {
        super();
    }
    public PatchableFields(Map map) {
        super(map);
    }

    public static PatchableFields of(Class targetClass) {
        PatchableFields patchableFields = cached.get(targetClass);
        if(patchableFields != null) {
            return patchableFields;
        }

        if(targetClass == null ||
                Number.class.isAssignableFrom(targetClass) ||
                CharSequence.class.isAssignableFrom(targetClass)) {
            return PatchableFields.EMPTY;
        }
        patchableFields = new PatchableFields(Arrays.stream(targetClass.getDeclaredFields())
                .filter(field -> Arrays.stream(field.getAnnotations())
                        .anyMatch(annotation -> annotation instanceof Patchable))
                .collect(Collectors.toMap(Field::getName, (field) -> of(field.getType()))));
        cached.put(targetClass, patchableFields);
        return patchableFields;
    }
}
