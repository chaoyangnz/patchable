package patchable;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.validation.Validated;
import lombok.Getter;

import javax.validation.Valid;
import java.util.function.Function;

@Introspected
@Validated
public class PatchInfo<Patch, Target> {
    @Valid
    @Getter
    private final Patch patch;
    private final Function<Target, Target> mergeFunction;

    public PatchInfo(Patch patch, Function<Target, Target> mergeFunction) {
        if(patch == null) {
            throw new IllegalArgumentException("patch is missing");
        }
        if(mergeFunction == null) {
            throw new IllegalArgumentException("mergeFunction is missing");
        }
        this.patch = patch;
        this.mergeFunction = mergeFunction;
    }

    public Target merge(@Valid Target target) {
        return mergeFunction.apply(target);
    }
}
