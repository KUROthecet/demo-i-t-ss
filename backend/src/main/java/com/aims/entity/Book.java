package com.aims.entity;

import com.aims.dto.response.MediaResponseDto;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.LinkedHashMap;
import java.util.Map;

@Entity
@Table(name = "book")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@PrimaryKeyJoinColumn(name = "id")
public class Book extends PhysicalMedia {

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

    @Override
    public void populateDto(MediaResponseDto dto) {
        dto.setType("Book");
        dto.setAuthor(author);
        dto.setCoverType(coverType);
        dto.setPublicationDate(publicationDate);
        dto.setPublisher(publisher);
        dto.setGenre(genre);
        dto.setLanguage(language);
        dto.setNumberOfPages(numberOfPages);
    }
}
