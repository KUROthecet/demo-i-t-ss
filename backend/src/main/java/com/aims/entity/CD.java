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
@Table(name = "cd")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@PrimaryKeyJoinColumn(name = "id")
public class CD extends PhysicalMedia {

    @NotBlank(message = "Artist is required for a CD")
    private String artist;

    @NotBlank(message = "Genre is required for a CD")
    private String genre;

    private String recordLabel;

    @Column(columnDefinition = "TEXT")
    private String trackList;

    private String releaseDate;

    @Override
    public Map<String, String> getTypeSpecificAttributes() {
        Map<String, String> attrs = new LinkedHashMap<>();
        if (artist != null)      attrs.put("Artist", artist);
        if (genre != null)       attrs.put("Genre", genre);
        if (recordLabel != null) attrs.put("Record Label", recordLabel);
        if (releaseDate != null) attrs.put("Release Date", releaseDate);
        if (trackList != null)   attrs.put("Track List", trackList);
        return attrs;
    }

    @Override
    public void populateDto(MediaResponseDto dto) {
        dto.setType("CD");
        dto.setArtist(artist);
        dto.setGenre(genre);
        dto.setRecordLabel(recordLabel);
        dto.setTrackList(trackList);
        dto.setReleaseDate(releaseDate);
    }
}
