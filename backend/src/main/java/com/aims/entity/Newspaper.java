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
@Table(name = "newspaper")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@PrimaryKeyJoinColumn(name = "id")
public class Newspaper extends PhysicalMedia {

    public static final List<FieldSchema> FORM_SCHEMA = List.of(
        new FieldSchema("editorInChief",        "Editor-in-Chief",  "Editor-in-Chief",       "text",     null, true),
        new FieldSchema("publisher",            "Publisher",        "Publisher",             "text",     null, false),
        new FieldSchema("publicationDate",      "Publication Date", "Publication Date",      "date",     null, false),
        new FieldSchema("issn",                 "ISSN",             "ISSN",                  "text",     null, false),
        new FieldSchema("issueNumber",          "Issue Number",     "Issue Number",          "text",     null, false),
        new FieldSchema("language",             "Language",         "Language",              "text",     null, false),
        new FieldSchema("publicationFrequency", "Frequency",        "Publication Frequency", "text",     null, false),
        new FieldSchema("sections",             "Sections",         "Sections",              "textarea", null, false)
    );

    @NotBlank(message = "Editor-in-Chief is required for a newspaper")
    private String editorInChief;

    private String publicationDate;
    private String publisher;
    private String issn;
    private String issueNumber;
    private String language;
    private String publicationFrequency;

    @Column(columnDefinition = "TEXT")
    private String sections;

    @Override
    public Map<String, String> getTypeSpecificAttributes() {
        Map<String, String> attrs = new LinkedHashMap<>();
        if (editorInChief != null)        attrs.put("Editor-in-Chief", editorInChief);
        if (publisher != null)            attrs.put("Publisher", publisher);
        if (publicationDate != null)      attrs.put("Publication Date", publicationDate);
        if (issn != null)                 attrs.put("ISSN", issn);
        if (issueNumber != null)          attrs.put("Issue Number", issueNumber);
        if (language != null)             attrs.put("Language", language);
        if (publicationFrequency != null) attrs.put("Frequency", publicationFrequency);
        if (sections != null)             attrs.put("Sections", sections);
        return attrs;
    }
}
