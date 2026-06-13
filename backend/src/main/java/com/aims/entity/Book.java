package com.aims.entity;

import com.aims.dto.FieldSchema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "book")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@PrimaryKeyJoinColumn(name = "id")
public class Book extends PhysicalMedia {

    public static final List<FieldSchema> FORM_SCHEMA = List.of(
        new FieldSchema("author",          "Author",           "Author",           "text",   null,                             true),
        new FieldSchema("publisher",       "Publisher",        "Publisher",        "text",   null,                             false),
        new FieldSchema("publicationDate", "Publication Date", "Publication Date", "date",   null,                             false),
        new FieldSchema("genre",           "Genre",            "Genre",            "text",   null,                             false),
        new FieldSchema("language",        "Language",         "Language",         "text",   null,                             false),
        new FieldSchema("numberOfPages",   "Pages",            "Number of Pages",  "number", null,                             false),
        new FieldSchema("coverType",       "Cover Type",       "Cover Type",       "select", List.of("Paperback", "Hardcover"), false)
    );

    @NotBlank(message = "Author is required for a book")
    private String author;

    private String  coverType;
    private String  publicationDate;
    private String  publisher;
    private String  genre;
    private String  language;
    private Integer numberOfPages;

    @Override
    public Map<String, String> getTypeSpecificAttributes() {
        Map<String, String> attrs = new LinkedHashMap<>();
        if (author != null)          attrs.put("Author", author);
        if (publisher != null)       attrs.put("Publisher", publisher);
        if (publicationDate != null) attrs.put("Publication Date", publicationDate);
        if (genre != null)           attrs.put("Genre", genre);
        if (language != null)        attrs.put("Language", language);
        if (numberOfPages != null)   attrs.put("Pages", String.valueOf(numberOfPages));
        if (coverType != null)       attrs.put("Cover Type", coverType);
        return attrs;
    }
}
