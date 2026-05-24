package com.aims.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/** Newspaper-specific attributes — Table 12 from AIMS SRS */
@Entity
@Table(name = "newspaper")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@PrimaryKeyJoinColumn(name = "id")
public class Newspaper extends Media {

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
}
