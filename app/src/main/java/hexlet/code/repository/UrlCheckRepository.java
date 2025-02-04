package hexlet.code.repository;

import hexlet.code.model.UrlCheck;
import lombok.extern.slf4j.Slf4j;

import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class UrlCheckRepository extends BaseRepository {
    private static final int URL_CHECK_LIMIT = 50;

    public static void save(UrlCheck urlCheck) throws SQLException {
        var sql = "INSERT INTO url_checks (url_id, status_code, title, h1, description, created_at)"
                + " VALUES (?, ?, ?, ?, ?, ?)";
        try (var conn = dataSource.getConnection();
             var preparedStatement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            LocalDateTime createdAt = LocalDateTime.now();
            /*Clob descriptionClob = UrlCheckRepository.dataSource.getConnection().createClob();
            descriptionClob.setString(1, urlCheck.getDescription());*/
            log.info("Url Check Set created_at: " + createdAt.format(BaseRepository.DATE_TIME_FORMATTER));
            preparedStatement.setLong(1, urlCheck.getUrlId());
            preparedStatement.setInt(2, urlCheck.getStatusCode());
            preparedStatement.setString(3, urlCheck.getTitle());
            preparedStatement.setString(4, urlCheck.getH1());
            preparedStatement.setString(5, urlCheck.getDescription());
            preparedStatement.setTimestamp(6, Timestamp.valueOf(createdAt));
            preparedStatement.executeUpdate();
            var generatedKeys = preparedStatement.getGeneratedKeys();
            if (generatedKeys.next()) {
                urlCheck.setId(generatedKeys.getLong(1));
                urlCheck.setCreatedAt(createdAt);
                log.info("New Url Check save to DB: " + urlCheck);
            } else {
                throw new SQLException("DB have not returned an id after saving entity");
            }
        }
    }

    public static List<UrlCheck> getUrlChecksByUrlId(Long urlId) throws SQLException {
        var sql = "SELECT id, status_code, title, h1, description, created_at, url_id"
                + " FROM url_checks"
                + " WHERE url_id = (?) ORDER BY id DESC OFFSET 0 ROWS FETCH NEXT "
                + URL_CHECK_LIMIT + " ROWS ONLY";
        List<UrlCheck> result = new ArrayList<>(URL_CHECK_LIMIT);
        try (var conn = dataSource.getConnection();
             var preparedStatement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);) {
            preparedStatement.setLong(1, urlId);
            var resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                result.add(UrlCheck.builder()
                        .id(resultSet.getLong(1))
                        .statusCode(resultSet.getInt(2))
                        .title(resultSet.getString(3))
                        .h1(resultSet.getString(4))
                        .description(resultSet.getString(5))
                        .createdAt(resultSet.getTimestamp(6).toLocalDateTime())
                        .urlId(resultSet.getLong(7))
                        .build());
            }

        }
        return result;
    }
}
