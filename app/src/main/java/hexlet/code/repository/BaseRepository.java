package hexlet.code.repository;

import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;
@Getter
public class BaseRepository {
    public static HikariDataSource dataSource;
}