package com.io.tedtalk.util;

import java.net.URI;
import java.net.URL;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Locale;

public class Validate {
    public static boolean isAnyEmpty(String... values) {
        return Arrays.stream(values).anyMatch(String::isBlank);
    }

    public static boolean isInteger(String value) {
        try {
            Integer.parseInt(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isValidDate(String date) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH);
            YearMonth.parse(date, formatter);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    public static boolean isValidUrl(String url) {
        try {
            URI uri = URI.create(url);
            return uri.getScheme() != null &&
                    (uri.getScheme().equalsIgnoreCase("http") || uri.getScheme().equalsIgnoreCase("https")) &&
                    uri.getHost() != null;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

}
