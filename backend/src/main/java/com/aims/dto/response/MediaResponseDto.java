package com.aims.dto.response;

import com.aims.entity.*;

public class MediaResponseDto {

    private Long    id;
    private String  barcode;
    private String  title;
    private String  category;
    private int     originalPrice;
    private int     currentPrice;
    private String  generalDescription;
    private String  dimensions;
    private double  weight;
    private String  imageUrl;
    private int     quantityInStock;
    private String  status;
    private boolean supportRushDelivery;
    private String  type;

    private String  author;
    private String  coverType;
    private String  publicationDate;
    private String  publisher;
    private String  genre;
    private String  language;
    private Integer numberOfPages;

    private String  artist;
    private String  recordLabel;
    private String  trackList;
    private String  releaseDate;

    private String  director;
    private String  discType;
    private Integer runtimeMinutes;
    private String  studio;
    private String  subtitles;

    private String  editorInChief;
    private String  issn;
    private String  issueNumber;
    private String  publicationFrequency;
    private String  sections;

    public MediaResponseDto() {}

    public static MediaResponseDto fromEntity(Media media) {
        MediaResponseDto dto = new MediaResponseDto();

        dto.setId(media.getId());
        dto.setBarcode(media.getBarcode());
        dto.setTitle(media.getTitle());
        dto.setCategory(media.getCategory());
        dto.setOriginalPrice(media.getOriginalPrice());
        dto.setCurrentPrice(media.getCurrentPrice());
        dto.setGeneralDescription(media.getGeneralDescription());
        dto.setDimensions(media.getDimensions());
        dto.setWeight(media.getWeight());
        dto.setImageUrl(media.getImageUrl());
        dto.setQuantityInStock(media.getQuantityInStock());
        dto.setStatus(media.getStatus() != null ? media.getStatus().name() : null);
        dto.setSupportRushDelivery(media.isSupportRushDelivery());

        if (media instanceof Book book) {
            dto.setType("Book");
            dto.setAuthor(book.getAuthor());
            dto.setCoverType(book.getCoverType());
            dto.setPublicationDate(book.getPublicationDate());
            dto.setPublisher(book.getPublisher());
            dto.setGenre(book.getGenre());
            dto.setLanguage(book.getLanguage());
            dto.setNumberOfPages(book.getNumberOfPages());
        } else if (media instanceof CD cd) {
            dto.setType("CD");
            dto.setArtist(cd.getArtist());
            dto.setGenre(cd.getGenre());
            dto.setRecordLabel(cd.getRecordLabel());
            dto.setTrackList(cd.getTrackList());
            dto.setReleaseDate(cd.getReleaseDate());
        } else if (media instanceof DVD dvd) {
            dto.setType("DVD");
            dto.setDirector(dvd.getDirector());
            dto.setDiscType(dvd.getDiscType());
            dto.setLanguage(dvd.getLanguage());
            dto.setRuntimeMinutes(dvd.getRuntimeMinutes());
            dto.setStudio(dvd.getStudio());
            dto.setSubtitles(dvd.getSubtitles());
            dto.setGenre(dvd.getGenre());
            dto.setReleaseDate(dvd.getReleaseDate());
        } else if (media instanceof Newspaper newspaper) {
            dto.setType("Newspaper");
            dto.setEditorInChief(newspaper.getEditorInChief());
            dto.setPublicationDate(newspaper.getPublicationDate());
            dto.setPublisher(newspaper.getPublisher());
            dto.setIssn(newspaper.getIssn());
            dto.setIssueNumber(newspaper.getIssueNumber());
            dto.setLanguage(newspaper.getLanguage());
            dto.setPublicationFrequency(newspaper.getPublicationFrequency());
            dto.setSections(newspaper.getSections());
        } else {
            dto.setType("Unknown");
        }

        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getBarcode() { return barcode; }
    public void setBarcode(String barcode) { this.barcode = barcode; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public int getOriginalPrice() { return originalPrice; }
    public void setOriginalPrice(int originalPrice) { this.originalPrice = originalPrice; }

    public int getCurrentPrice() { return currentPrice; }
    public void setCurrentPrice(int currentPrice) { this.currentPrice = currentPrice; }

    public String getGeneralDescription() { return generalDescription; }
    public void setGeneralDescription(String generalDescription) { this.generalDescription = generalDescription; }

    public String getDimensions() { return dimensions; }
    public void setDimensions(String dimensions) { this.dimensions = dimensions; }

    public double getWeight() { return weight; }
    public void setWeight(double weight) { this.weight = weight; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public int getQuantityInStock() { return quantityInStock; }
    public void setQuantityInStock(int quantityInStock) { this.quantityInStock = quantityInStock; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public boolean isSupportRushDelivery() { return supportRushDelivery; }
    public void setSupportRushDelivery(boolean supportRushDelivery) { this.supportRushDelivery = supportRushDelivery; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getCoverType() { return coverType; }
    public void setCoverType(String coverType) { this.coverType = coverType; }

    public String getPublicationDate() { return publicationDate; }
    public void setPublicationDate(String publicationDate) { this.publicationDate = publicationDate; }

    public String getPublisher() { return publisher; }
    public void setPublisher(String publisher) { this.publisher = publisher; }

    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public Integer getNumberOfPages() { return numberOfPages; }
    public void setNumberOfPages(Integer numberOfPages) { this.numberOfPages = numberOfPages; }

    public String getArtist() { return artist; }
    public void setArtist(String artist) { this.artist = artist; }

    public String getRecordLabel() { return recordLabel; }
    public void setRecordLabel(String recordLabel) { this.recordLabel = recordLabel; }

    public String getTrackList() { return trackList; }
    public void setTrackList(String trackList) { this.trackList = trackList; }

    public String getReleaseDate() { return releaseDate; }
    public void setReleaseDate(String releaseDate) { this.releaseDate = releaseDate; }

    public String getDirector() { return director; }
    public void setDirector(String director) { this.director = director; }

    public String getDiscType() { return discType; }
    public void setDiscType(String discType) { this.discType = discType; }

    public Integer getRuntimeMinutes() { return runtimeMinutes; }
    public void setRuntimeMinutes(Integer runtimeMinutes) { this.runtimeMinutes = runtimeMinutes; }

    public String getStudio() { return studio; }
    public void setStudio(String studio) { this.studio = studio; }

    public String getSubtitles() { return subtitles; }
    public void setSubtitles(String subtitles) { this.subtitles = subtitles; }

    public String getEditorInChief() { return editorInChief; }
    public void setEditorInChief(String editorInChief) { this.editorInChief = editorInChief; }

    public String getIssn() { return issn; }
    public void setIssn(String issn) { this.issn = issn; }

    public String getIssueNumber() { return issueNumber; }
    public void setIssueNumber(String issueNumber) { this.issueNumber = issueNumber; }

    public String getPublicationFrequency() { return publicationFrequency; }
    public void setPublicationFrequency(String publicationFrequency) { this.publicationFrequency = publicationFrequency; }

    public String getSections() { return sections; }
    public void setSections(String sections) { this.sections = sections; }
}
