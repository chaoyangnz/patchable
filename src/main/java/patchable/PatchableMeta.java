package patchable;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
class PatchableMeta<T> {
    private final Class<T> targetClass;
    private final Map<String, PatchableMeta> fields;

    private static Map<Class, PatchableMeta> patchableMetaCache = new HashMap<>();

    public boolean patchable(String field) {
        return fields.containsKey(field);
    }

    public PatchableMeta field(String field) {
        return fields.get(field);
    }

    public Class<T> targetClass() {
        return targetClass;
    }

    public static PatchableMeta of(Class targetClass) {
        if(targetClass == null) {
            return null;
        }

        if(patchableMetaCache.containsKey(targetClass)) {
            return patchableMetaCache.get(targetClass);
        }

        if(Number.class.isAssignableFrom(targetClass) || CharSequence.class.isAssignableFrom(targetClass)) {
            return new PatchableMeta(targetClass, Map.of());
        }
        PatchableMeta patchableMeta = new PatchableMeta(targetClass,
                Arrays.stream(targetClass.getDeclaredFields())
                        .filter(field -> Arrays.stream(field.getAnnotations())
                        .anyMatch(annotation -> annotation instanceof Patchable))
                        .collect(Collectors.toMap(Field::getName, (field) -> of(field.getType()))));
        patchableMetaCache.put(targetClass, patchableMeta);
        return patchableMeta;
    }
}
