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

/** DVD-specific attributes — Table 14 from AIMS SRS */
@Entity
@Table(name = "dvd")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@PrimaryKeyJoinColumn(name = "id")
public class DVD extends Media {

    @NotBlank(message = "Director is required for a DVD")
    private String director;

    private String  discType;
    private String  language;
    private Integer runtimeMinutes;
    private String  studio;
    private String  subtitles;
    private String  genre;
    private String  releaseDate;
}
