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

/** CD-specific attributes — Table 13 from AIMS SRS */
@Entity
@Table(name = "cd")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@PrimaryKeyJoinColumn(name = "id")
public class CD extends Media {

    @NotBlank(message = "Artist is required for a CD")
    private String artist;

    @NotBlank(message = "Genre is required for a CD")
    private String genre;

    private String recordLabel;

    @Column(columnDefinition = "TEXT")
    private String trackList;

    private String releaseDate;
}
