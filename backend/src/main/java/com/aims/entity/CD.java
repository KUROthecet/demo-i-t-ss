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
@Table(name = "cd")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@PrimaryKeyJoinColumn(name = "id")
public class CD extends PhysicalMedia {

    public static final List<FieldSchema> FORM_SCHEMA = List.of(
        new FieldSchema("artist",      "Artist",       "Artist",       "text",     null, true),
        new FieldSchema("genre",       "Genre",        "Genre",        "text",     null, true),
        new FieldSchema("recordLabel", "Record Label", "Record Label", "text",     null, false),
        new FieldSchema("releaseDate", "Release Date", "Release Date", "date",     null, false),
        new FieldSchema("trackList",   "Track List",   "Track List",   "textarea", null, false)
    );

    @NotBlank(message = "Artist is required for a CD")
    private String artist;

    @NotBlank(message = "Genre is required for a CD")
    private String genre;

    private String recordLabel;

    @Column(columnDefinition = "TEXT")
    private String trackList;

    private String releaseDate;

    @Override
    public void updateDetails(Media updated) {
        super.updateDetails(updated);
        if (updated instanceof CD other) {
            this.artist      = other.getArtist();
            this.genre       = other.getGenre();
            this.recordLabel = other.getRecordLabel();
            this.trackList   = other.getTrackList();
            this.releaseDate = other.getReleaseDate();
        }
    }

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
}
