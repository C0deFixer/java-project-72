package hexlet.code.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Getter
@Setter
public class BasePage {
    private static final String PATTERN_FORMAT = "dd/MM/yyyy HH:mm";
    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(PATTERN_FORMAT);

    private String flash;
    private String flashType;

    public static String getDateAsString(LocalDateTime localDateTime) {
        return localDateTime.format(dateTimeFormatter);
    }
}

