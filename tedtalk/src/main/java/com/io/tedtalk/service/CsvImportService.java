package com.io.tedtalk.service;

import com.io.tedtalk.dto.ImportResponse;
import com.io.tedtalk.model.TedTalk;
import com.io.tedtalk.repository.TedTalkRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

import static com.io.tedtalk.util.Validate.*;

@Service
public class CsvImportService {

    private final TedTalkRepository tedTalkRepository;

    public CsvImportService(TedTalkRepository tedTalkRepository) {
        this.tedTalkRepository = tedTalkRepository;
    }


    public ImportResponse importTedTalksFromCsv(MultipartFile file) throws Exception {
        if (!Objects.requireNonNull(file.getOriginalFilename()).endsWith(".csv")) {
            throw new IllegalArgumentException("Only CSV files are supported.");
        }

        List<TedTalk> talksToInsert = new ArrayList<>();
        List<String> messages = new ArrayList<>();
        int skipped = 0;
        int errors = 0;
        Set<String> seenLinks = new HashSet<>();
        int rowNumber = 1;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String header = reader.readLine(); // Read header
            if (header == null || !header.replace("\uFEFF", "").trim().equalsIgnoreCase("title,author,date,views,likes,link")) {
                throw new IllegalArgumentException("CSV header is missing or invalid.");
            }

            String line;
            while ((line = reader.readLine()) != null) {
                rowNumber++;

                if (line.trim().isEmpty()) {
                    errors++;
                    messages.add("Row " + rowNumber + ": Empty row");
                    continue;
                }

                String[] cols = line.split(",", -1); // include empty strings
                if (cols.length != 6) {
                    errors++;
                    messages.add("Row " + rowNumber + ": Expected 6 columns, but found " + cols.length);
                    continue;
                }

                String title = cols[0].trim();
                String author = cols[1].trim();
                String date = cols[2].trim();
                String viewsStr = cols[3].trim();
                String likesStr = cols[4].trim();
                String link = cols[5].trim();

                Optional<String> validationError = validateCsvRow(title, author, date, viewsStr, likesStr, link, rowNumber, seenLinks);
                if (validationError.isPresent()) {
                    errors++;
                    messages.add(validationError.get());
                    continue;
                }

                // Check if link already exists
                if (tedTalkRepository.findByLink(link).isPresent()) {
                    skipped++;
                    messages.add("Row " + rowNumber + ": Skipped due to duplicate link.");
                    continue;
                }
                    TedTalk talk = new TedTalk(
                            title,
                            author,
                            date,
                            Integer.parseInt(viewsStr),
                            Integer.parseInt(likesStr),
                            link
                    );
                    talksToInsert.add(talk);

            }

            tedTalkRepository.saveAll(talksToInsert);
        } catch (IOException e) {
            throw new IOException("Error reading the CSV file", e);
        } catch (Exception e) {
            throw new Exception("Error processing file, RowNumber at the time of error is : " + rowNumber, e);
        }


        return new ImportResponse(talksToInsert.size(), skipped, errors, messages);
    }

    private Optional<String> validateCsvRow(String title, String author, String date,
                                            String viewsStr, String likesStr, String link, int rowNumber, Set<String> seenLinks) {
        if (isAnyEmpty(title, author, date, viewsStr, likesStr, link)) {
            return Optional.of("Row " + rowNumber + ": One or more fields are empty.");
        }

        if (!isInteger(viewsStr)) {
            return Optional.of("Row " + rowNumber + ": Invalid integer for views.");
        }

        if (!isInteger(likesStr)) {
            return Optional.of("Row " + rowNumber + ": Invalid integer for likes.");
        }

        if (!isValidDate(date)) {
            return Optional.of("Row " + rowNumber + ": Invalid date format. Expected 'MMMM yyyy'.");
        }

        if (!isValidUrl(link)) {
            return Optional.of("Row " + rowNumber + ": Invalid URL format.");
        }

        if (seenLinks.contains(link)) {
            return Optional.of("Row " + rowNumber + ": Duplicate link in CSV - " + link);
        }

        seenLinks.add(link);
        return Optional.empty(); // no errors
    }



}
