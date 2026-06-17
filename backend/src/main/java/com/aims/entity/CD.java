package com.aims.entity;

import com.aims.dto.FieldSchema;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSetter;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@JsonIgnoreProperties({"tracks"})
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

    @ElementCollection
    @CollectionTable(name = "cd_track", joinColumns = @JoinColumn(name = "cd_id"))
    @OrderColumn(name = "track_order")
    private List<CdTrack> tracks = new ArrayList<>();

    private String releaseDate;

    @JsonSetter("trackList")
    public void parseTrackList(String raw) {
        if (raw == null || raw.isBlank()) {
            this.tracks = new ArrayList<>();
            return;
        }
        this.tracks = Arrays.stream(raw.split("\n"))
            .map(String::trim)
            .filter(line -> !line.isBlank())
            .map(line -> {
                int sep = line.indexOf('|');
                return sep >= 0
                    ? new CdTrack(line.substring(0, sep).trim(), line.substring(sep + 1).trim())
                    : new CdTrack(line, "");
            })
            .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public void updateDetails(Media updated) {
        super.updateDetails(updated);
        if (updated instanceof CD other) {
            this.artist      = other.getArtist();
            this.genre       = other.getGenre();
            this.recordLabel = other.getRecordLabel();
            this.tracks      = other.getTracks() != null ? new ArrayList<>(other.getTracks()) : new ArrayList<>();
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
        if (tracks != null && !tracks.isEmpty()) {
            String formatted = IntStream.range(0, tracks.size())
                .mapToObj(i -> {
                    CdTrack t = tracks.get(i);
                    String entry = t.getTitle();
                    return (t.getLength() != null && !t.getLength().isBlank())
                        ? entry + " | " + t.getLength()
                        : entry;
                })
                .collect(Collectors.joining("\n"));
            attrs.put("Track List", formatted);
        }
        return attrs;
    }
}
