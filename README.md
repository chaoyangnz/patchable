# Patch support

## Install

Gradle:

`implementation 'com.bitsflux.patchable:patchable-micronaut:1.0.0`

## Usage

### Configure a bean
```java
@Factory
public class PatchableFactory {
    @Inject
    private ObjectMapper objectMapper;

    @Singleton
    public PatchInfoRequestBinder patchInfoRequestBinder() {
        return new PatchInfoRequestBinder(objectMapper);
    }
}
```

### Define models

```java
@Introspected
@Getter @Setter
class Book {
    @NotNull
    private Long id;
    @NotNull
    @Size(min = 10)
    private String name;
    @Patchable
    @NotNull
    private String description;
}

@Introspected
@Getter @Setter
class BookToPatch {
    @NotNull
    private String description;
}
```

### Bind PatchInfo in controller
```java
@Controller
@Validated
public class BookController {

    @Inject
    private BookRepository bookRepository;

    @Patch("/{id}")
    public Book patch(
        @NotNull Long id, 
        @Valid PatchInfo<BookToPatch, Book> patchInfo
    ) {
        Book book = bookRepository.findById(id);
        return bookRepository.save(patchInfo.merge(book));
    }
}
```
