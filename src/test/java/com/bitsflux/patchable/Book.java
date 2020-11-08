package com.bitsflux.patchable;

import io.micronaut.core.annotation.Introspected;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Introspected
@Setter @Accessors(chain = true) @Getter
public class Book {
    @NotNull
    private Long id;
    @NotNull
    @NotBlank
    private String name;
    @Patchable
    private String description;

    @Introspected
    @Setter @Accessors(chain = true) @Getter
    public static class BookToCreate {
        @NotNull
        @NotBlank
        @Size(min = 6)
        private String name;
        private String description;
    }


    @Introspected
    @Setter @Accessors(chain = true) @Getter
    public static class BookToPatch {
        private String name;
        @NotNull
        @Size(min = 8)
        private String description;
    }
}
