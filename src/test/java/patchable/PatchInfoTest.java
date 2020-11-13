package patchable;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

@MicronautTest
public class PatchInfoTest {

    @Inject
    PatchInfoRequestBinder patchInfoRequestBinder;

    @Inject
    ObjectMapper objectMapper;

    PatchInfo<Book.BookToPatch, Book> patchInfo;
    Book book;
    Function<String, PatchInfo<Book.BookToPatch, Book>> patchInfoSupplier = (json) -> {
        try {
            Book.BookToPatch patch = objectMapper.readValue(json, Book.BookToPatch.class);
            final JsonNode patchNode = objectMapper.readTree(json);
            Function<Book, Book> mergeFunction = (target) -> {
                JsonNode targetNode = objectMapper.convertValue(target, JsonNode.class);
                return objectMapper.convertValue(
                        PatchUtil.merge(patchNode, targetNode, Book.BookToPatch.class, Book.class),
                        target.getClass()
                );
            };
            return new PatchInfo<>(patch, mergeFunction);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    };

    @BeforeEach
    public void setup() {
        book = new Book().setId(1L).setName("existing name").setDescription("existing description");
    }

    @Test
    void testPatch_given_empty_patch_json() throws JsonProcessingException {
        String json = "{}";
        patchInfo = patchInfoSupplier.apply(json);

        Book patched = patchInfo.merge(book);
        assertThat(patched.getId()).isEqualTo(book.getId());
        assertThat(patched.getName()).isEqualTo(book.getName());
        assertThat(patched.getDescription()).isEqualTo(book.getDescription());
    }

    @Test
    void testPatch_given_all_passed_restrictions() {
        String json ="{\"description\": \"patched description\"}";
        patchInfo = patchInfoSupplier.apply(json);

        Book patched = patchInfo.merge(book);
        assertThat(patched.getId()).isEqualTo(book.getId());
        assertThat(patched.getName()).isEqualTo(book.getName());
        assertThat(patched.getDescription()).isEqualTo("patched description");
    }

    @Test
    void testPatch_given_all_passed_restrictions_but_not_validation() {
        String json ="{\"description\": \"patched\"}";
        patchInfo = patchInfoSupplier.apply(json);

        Book patched = patchInfo.merge(book);
        assertThat(patched.getId()).isEqualTo(book.getId());
        assertThat(patched.getName()).isEqualTo(book.getName());
        assertThat(patched.getDescription()).isEqualTo("patched");
    }

    @Test
    void testPatch_given_field_not_included_in_patch_class() {
        String json ="{\"category\": \"category\"}";
        patchInfo = patchInfoSupplier.apply(json);

        Book patched = patchInfo.merge(book);
        assertThat(patched.getId()).isEqualTo(book.getId());
        assertThat(patched.getName()).isEqualTo(book.getName());
        assertThat(patched.getDescription()).isEqualTo(book.getDescription());
    }

    @Test
    void testPatch_given_not_patchable_field_in_target_class() {
        String json ="{\"name\": \"patched name\"}";
        patchInfo = patchInfoSupplier.apply(json);

        Book patched = patchInfo.merge(book);
        assertThat(patched.getId()).isEqualTo(book.getId());
        assertThat(patched.getName()).isEqualTo(book.getName());
        assertThat(patched.getDescription()).isEqualTo(book.getDescription());
    }

}
