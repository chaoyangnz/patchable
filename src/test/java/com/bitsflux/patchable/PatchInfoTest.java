package com.bitsflux.patchable;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

@MicronautTest
public class PatchInfoTest {

    @Inject
    PatchInfoRequestBinder patchInfoRequestBinder;

    @Inject
    ObjectMapper objectMapper;

    PatchInfo<Book.BookToPatch, Book> patchInfo;
    Book book;

    @BeforeEach
    public void setup() throws JsonProcessingException {
        book = new Book().setId(1L).setName("existing name").setDescription("existing description");
    }

    @Test
    void testPatch_given_missing_json_node() throws JsonProcessingException {
        JsonNode patchNode = objectMapper.readTree("");
        assertThatCode(() -> new PatchInfo<>(objectMapper, Book.BookToPatch.class, Book.class, patchNode))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testPatch_given_empty_patch_json() throws JsonProcessingException {
        JsonNode patchNode = objectMapper.readTree("{}");
        patchInfo = new PatchInfo<>(objectMapper, Book.BookToPatch.class, Book.class, patchNode);

        Book patched = patchInfo.merge(book);
        assertThat(patched.getId()).isEqualTo(book.getId());
        assertThat(patched.getName()).isEqualTo(book.getName());
        assertThat(patched.getDescription()).isEqualTo(book.getDescription());
    }

    @Test
    void testPatch_given_all_passed_restrictions() throws JsonProcessingException {
        JsonNode patchNode = objectMapper.readTree("{\"description\": \"patched description\"}");
        patchInfo = new PatchInfo<>(objectMapper, Book.BookToPatch.class, Book.class, patchNode);

        Book patched = patchInfo.merge(book);
        assertThat(patched.getId()).isEqualTo(book.getId());
        assertThat(patched.getName()).isEqualTo(book.getName());
        assertThat(patched.getDescription()).isEqualTo("patched description");
    }

    @Test
    void testPatch_given_all_passed_restrictions_but_not_validation() throws JsonProcessingException {
        JsonNode patchNode = objectMapper.readTree("{\"description\": \"patched\"}");
        patchInfo = new PatchInfo<>(objectMapper, Book.BookToPatch.class, Book.class, patchNode);

        Book patched = patchInfo.merge(book);
        assertThat(patched.getId()).isEqualTo(book.getId());
        assertThat(patched.getName()).isEqualTo(book.getName());
        assertThat(patched.getDescription()).isEqualTo("patched");
    }

    @Test
    void testPatch_given_field_not_included_in_patch_class() throws JsonProcessingException {
        JsonNode patchNode = objectMapper.readTree("{\"category\": \"category\"}");
        patchInfo = new PatchInfo<>(objectMapper, Book.BookToPatch.class, Book.class, patchNode);

        Book patched = patchInfo.merge(book);
        assertThat(patched.getId()).isEqualTo(book.getId());
        assertThat(patched.getName()).isEqualTo(book.getName());
        assertThat(patched.getDescription()).isEqualTo(book.getDescription());
    }

    @Test
    void testPatch_given_not_patchable_field_in_target_class() throws JsonProcessingException {
        JsonNode patchNode = objectMapper.readTree("{\"name\": \"patched name\"}");
        patchInfo = new PatchInfo<>(objectMapper, Book.BookToPatch.class, Book.class, patchNode);

        Book patched = patchInfo.merge(book);
        assertThat(patched.getId()).isEqualTo(book.getId());
        assertThat(patched.getName()).isEqualTo(book.getName());
        assertThat(patched.getDescription()).isEqualTo(book.getDescription());
    }

}
