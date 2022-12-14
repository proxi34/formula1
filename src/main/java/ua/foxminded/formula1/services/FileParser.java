package ua.foxminded.formula1.services;

import ua.foxminded.formula1.racer.Racer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileParser {

    public static final String SEPARATOR = "_";
    public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH:mm:ss.SSS");
    public static final int ABBREVIATION_LENGTH = 3;

    public List<Racer> parseAbbreviationsFile(File file) {
        checkFile(file);

        List<Racer> racers;

        try (Stream<String> lines = Files.lines(file.toPath())) {
            racers = lines.map(line -> line.split(SEPARATOR))
                .map(s -> new Racer(s[0], s[1], s[2]))
                .collect(Collectors.toList());
        } catch (IOException e) {
            racers = Collections.emptyList();
        }
        return racers;
    }

    public Map<String, Duration> parseLogFiles(File startLog, File endLog) {

        Map<String, LocalDateTime> start = parseLogFile(startLog);
        Map<String, LocalDateTime> end = parseLogFile(endLog);

        if (start.isEmpty() || end.isEmpty()) {
            return Collections.emptyMap();
        }

        return Stream.of(start)
            .flatMap(map -> map.entrySet().stream())
            .collect(Collectors.toMap(Map.Entry::getKey, value -> Duration.between(value.getValue(), end.get(value.getKey()))));
    }

    private Map<String, LocalDateTime> parseLogFile(File log) {
        checkFile(log);

        Map<String, LocalDateTime> logTimes;

        try (Stream<String> lines = Files.lines(log.toPath())) {
            logTimes = lines.collect(Collectors.toMap(
                key -> key.substring(0, ABBREVIATION_LENGTH),
                value -> LocalDateTime.parse(value.substring(ABBREVIATION_LENGTH), FORMATTER)));
        } catch (IOException e) {
            logTimes = Collections.emptyMap();
        }
        return logTimes;
    }

    private void checkFile(File file) {
        if (file == null) {
            throw new IllegalArgumentException("File cannot be null");
        }

        if (!file.exists()) {
            throw new IllegalArgumentException("File does not exist");
        }

        if (file.length() == 0) {
            throw new IllegalArgumentException("File cannot be empty");
        }
    }
}
