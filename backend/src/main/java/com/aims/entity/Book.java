/*
Coupling level: Content coupling
Reason why: Entities expose all internal state via @Data; service uses blind bulk-setters (updateDetails) to copy fields.
*/

package com.aims.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/** Book-specific attributes — Table 11 from AIMS SRS */
@Entity
@Table(name = "book")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@PrimaryKeyJoinColumn(name = "id")
public class Book extends Media {

    @NotBlank(message = "Author is required for a book")
    private String author;

    private String  coverType;
    private String  publicationDate;
    private String  publisher;
    private String  genre;
    private String  language;
    private Integer numberOfPages;
}
