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
@Table(name = "dvd")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@PrimaryKeyJoinColumn(name = "id")
public class DVD extends PhysicalMedia {

    public static final List<FieldSchema> FORM_SCHEMA = List.of(
        new FieldSchema("director",       "Director",    "Director",      "text",   null,                                       true),
        new FieldSchema("studio",         "Studio",      "Studio",        "text",   null,                                       false),
        new FieldSchema("genre",          "Genre",       "Genre",         "text",   null,                                       false),
        new FieldSchema("language",       "Language",    "Language",      "text",   null,                                       false),
        new FieldSchema("discType",       "Disc Type",   "Disc Type",     "select", List.of("Blu-ray", "HD-DVD", "Standard DVD"), false),
        new FieldSchema("runtimeMinutes", "Runtime",     "Runtime (min)", "number", null,                                       false),
        new FieldSchema("subtitles",      "Subtitles",   "Subtitles",     "text",   null,                                       false),
        new FieldSchema("releaseDate",    "Release Date","Release Date",  "date",   null,                                       false)
    );

    @NotBlank(message = "Director is required for a DVD")
    private String director;

    private String  discType;
    private String  language;
    private Integer runtimeMinutes;
    private String  studio;

    @NotBlank(message = "Subtitles is required for a DVD")
    private String  subtitles;
    private String  genre;
    private String  releaseDate;

    @Override
    public Map<String, String> getTypeSpecificAttributes() {
        Map<String, String> attrs = new LinkedHashMap<>();
        if (director != null)        attrs.put("Director", director);
        if (studio != null)          attrs.put("Studio", studio);
        if (genre != null)           attrs.put("Genre", genre);
        if (language != null)        attrs.put("Language", language);
        if (discType != null)        attrs.put("Disc Type", discType);
        if (runtimeMinutes != null)  attrs.put("Runtime", runtimeMinutes + " min");
        if (subtitles != null)       attrs.put("Subtitles", subtitles);
        if (releaseDate != null)     attrs.put("Release Date", releaseDate);
        return attrs;
    }
}
