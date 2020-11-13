package patchable;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.core.convert.ArgumentConversionContext;
import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.bind.binders.TypedRequestArgumentBinder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Singleton;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Singleton
@RequiredArgsConstructor
@Slf4j
public class PatchInfoRequestBinder implements TypedRequestArgumentBinder<PatchInfo> {

    private final ObjectMapper objectMapper;

    @Override
    public Argument<PatchInfo> argumentType() {
        return Argument.of(PatchInfo.class);
    }

    @Override
    public BindingResult<PatchInfo> bind(ArgumentConversionContext<PatchInfo> context, HttpRequest<?> source) {
        Type[] typeArguments = context.getArgument()
                .asParameterizedType()
                .getActualTypeArguments();
        if(typeArguments.length != 2) {
            log.warn("PatchInfo must specify the patch type and target type");
            return BindingResult.EMPTY;
        }

        List<Class> patchInfoParameterTypes = Arrays.stream(typeArguments)
                .map(argument -> ((Argument)argument).getType()).collect(Collectors.toList());

        Class patchType = patchInfoParameterTypes.get(0);
        Class targetType = patchInfoParameterTypes.get(1);

        if(patchType == null) {
            throw new IllegalArgumentException("Patch Type is missing for PatchInfo");
        }

        if(targetType == null) {
            throw new IllegalArgumentException("Target Type is missing for PatchInfo");
        }

        Optional<JsonNode> body = source.getBody(JsonNode.class);

        if(body.isEmpty()) {
            return BindingResult.EMPTY;
        }
        try {
            JsonNode patchNode = body.get();
            Object patch = objectMapper.convertValue(patchNode, patchType);;
            PatchInfo patchInfo = new PatchInfo(patch, (target) -> {
                JsonNode targetNode = objectMapper.convertValue(target, JsonNode.class);
                return objectMapper.convertValue(
                        PatchUtil.merge(patchNode, targetNode, patchType, targetType),
                        target.getClass()
                );
            });
            return () -> Optional.of(patchInfo);
        } catch (Exception ex) {
            log.warn("PatchInfo binding error", ex);
            return BindingResult.EMPTY;
        }
    }
}
