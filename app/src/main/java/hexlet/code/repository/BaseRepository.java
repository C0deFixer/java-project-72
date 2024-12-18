package hexlet.code.repository;

import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;

import java.time.format.DateTimeFormatter;

@Getter
public abstract class BaseRepository {
    public static HikariDataSource dataSource;
    //Just for logs output DateTime format
    static final String PATTERN_FORMAT = "dd/MM/yyyy HH:mm";
    static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(PATTERN_FORMAT);
}
