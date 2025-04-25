package com.io.tedtalk.model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import lombok.*;

@Entity
@Table(name = "ted_talks")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TedTalk {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @NotBlank
    @NotNull(message = "Title is required")
    private String title;

    @Column(nullable = false)
    @NotBlank
    @NotNull(message = "Author is required")
    private String author;

    @JsonIgnore
    @NotNull(message = "month is required")
    @Column(name = "`month`", nullable = false)
    @Min(0) @Max(11)
    private Integer month;

    @JsonIgnore
    @NotNull(message = "year is required")
    @Column(name = "`year`", nullable = false)
    @Min(1900) @Max(2100)  // Reasonable year constraints
    private Integer year;

    @NotNull(message = "view is required")
    @Column(nullable = false)
    @Min(0)
    private Integer views;

    @NotNull(message = "likes is required")
    @Column(nullable = false)
    @Min(0)
    private Integer likes;

    @NotNull(message = "Link is required")
    @Column(nullable = false,unique = true)
    @NotBlank
    private String link;

    // Custom constructor for the date string format
    public TedTalk(String title, String author, String date, int views, int likes, String link) {
        this.title = title;
        this.author = author;
        parseDate(date);
        this.views = views;
        this.likes = likes;
        this.link = link;
    }

    // Combined date getter for convenience
    public String getDate() {
        return DateTimeFormatter.ofPattern("MMMM")
                .withLocale(Locale.ENGLISH)
                .format(java.time.Month.of(month + 1)) + " " + year;
    }

    // Combined date setter that parses the date string
    public void setDate(String date) {
        parseDate(date);
    }

    // Helper method to parse the date string into month and year
    private void parseDate(String date) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH);
            if(!date.isEmpty()) {
                java.time.YearMonth yearMonth = java.time.YearMonth.parse(date, formatter);
                this.month = yearMonth.getMonthValue() - 1;  // Convert to 0-11 range
                this.year = yearMonth.getYear();
            }
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date format. Expected format like 'December 2021'", e);
        }
    }
}