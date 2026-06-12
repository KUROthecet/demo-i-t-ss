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
@Table(name = "dvd")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@PrimaryKeyJoinColumn(name = "id")
public class DVD extends PhysicalMedia {

    @NotBlank(message = "Director is required for a DVD")
    private String director;

    private String  discType;
    private String  language;
    private Integer runtimeMinutes;
    private String  studio;
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

    @Override
    public void populateDto(MediaResponseDto dto) {
        dto.setType("DVD");
        dto.setDirector(director);
        dto.setDiscType(discType);
        dto.setLanguage(language);
        dto.setRuntimeMinutes(runtimeMinutes);
        dto.setStudio(studio);
        dto.setSubtitles(subtitles);
        dto.setGenre(genre);
        dto.setReleaseDate(releaseDate);
    }
}
