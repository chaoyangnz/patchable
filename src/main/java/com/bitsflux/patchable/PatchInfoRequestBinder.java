package com.bitsflux.patchable;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.core.convert.ArgumentConversionContext;
import io.micronaut.core.type.Argument;
import io.micronaut.core.type.DefaultArgument;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.bind.binders.TypedRequestArgumentBinder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j
public class PatchInfoRequestBinder implements TypedRequestArgumentBinder<PatchInfo> {

    private final ObjectMapper objectMapper;

    @Override
    public Argument<PatchInfo> argumentType() {
        return Argument.of(PatchInfo.class);
    }

    @Override
    public BindingResult<PatchInfo> bind(ArgumentConversionContext<PatchInfo> context, HttpRequest<?> request) {
        String json = request.getBody(String.class).orElse(null);
        if(json == null) {
            return BindingResult.EMPTY;
        }
        try {
            List<Class> patchInfoParameterTypes = Arrays.stream(context.getArgument()
                    .asParameterizedType()
                    .getActualTypeArguments())
                    .map(argument -> ((DefaultArgument)argument).getType()).collect(Collectors.toList());
            Class patchType = patchInfoParameterTypes.get(0);
            Object patch = objectMapper.readValue(json, patchType);
            Class targetType = patchInfoParameterTypes.get(1);
            PatchInfo patchInfo = new PatchInfo(objectMapper, patchType, targetType).setPatch(patch).setJson(json);
            return () -> Optional.of(patchInfo);
        } catch (Exception ex) {
            log.warn("PatchInfo binding error", ex);
            return BindingResult.EMPTY;
        }
    }
}
