package hexlet.code.repository;

import hexlet.code.model.Url;
import lombok.extern.slf4j.Slf4j;

import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
public class UrlRepository extends BaseRepository {

    static final int LIMIT = 25;

    public static void save(Url url) throws SQLException {
        var sql = "INSERT INTO urls (protocol, host, port, created_at) VALUES (?, ?, ?, ?)";
        try (var conn = dataSource.getConnection();
             var preparedStatement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            LocalDateTime createdAt = LocalDateTime.now();
            log.info("Set created_at: " + createdAt.format(BaseRepository.DATE_TIME_FORMATTER));
            preparedStatement.setString(1, url.getProtocol());
            preparedStatement.setString(2, url.getHost());
            preparedStatement.setInt(3, url.getPort());
            preparedStatement.setTimestamp(4, Timestamp.valueOf(createdAt));
            preparedStatement.executeUpdate();
            var generatedKeys = preparedStatement.getGeneratedKeys();
            if (generatedKeys.next()) {
                url.setId(generatedKeys.getLong(1));
                url.setCreatedAt(createdAt);
                log.info("New Url save to DB: " + url);
            } else {
                throw new SQLException("DB have not returned an id after saving entity");
            }
        }
    }

    public static Optional<Url> findById(Long id) throws SQLException {
        var sql = "SELECT id, protocol, host, port, created_at FROM urls WHERE id = ? ";
        try (var conn = dataSource.getConnection();
             var preparedStatement = conn.prepareStatement(sql)) {
            preparedStatement.setLong(1, id);
            log.info("Searching Url in  DB by id: " + id);
            var resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                Url url = Url.builder()
                        .id(resultSet.getLong("id"))
                        .protocol(resultSet.getString("protocol"))
                        .host(resultSet.getString("host"))
                        .port(resultSet.getInt("port"))
                        .createdAt(resultSet.getTimestamp("created_at").toLocalDateTime())
                        .build();
                log.info("Found Url in  DB by id: " + url.toString());
                return Optional.of(url);
            } else {
                log.info("NOT Found Url in  DB by id: " + id);
                return Optional.empty();
            }
        }
    }

    public static List<Url> getUrlEntities() throws SQLException {
        var sql = "SELECT urls.id, urls.protocol, urls.host, urls.port, urls.created_at, "
                + "last_url_checks.status_code AS last_check_status_code, "
                + "last_url_checks.created_at AS last_check_created_at "
                + "FROM urls"
                + "    LEFT JOIN url_checks as last_url_checks"
                + "        ON urls.id = last_url_checks.url_id"
                + "    LEFT JOIN url_checks as url_checks"
                + "        ON urls.id = url_checks.url_id"
                + "        AND last_url_checks.created_at < url_checks.created_at"
                + "    WHERE url_checks.id IS NULL"
                + "    ORDER BY urls.created_at DESC"
                + "    OFFSET 0 ROWS FETCH NEXT " + LIMIT + " ROWS ONLY";

        List<Url> entities = new ArrayList<>();
        try (var conn = dataSource.getConnection();
             var preparedStatement = conn.prepareStatement(sql)) {
            var resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                Timestamp lastCheck = resultSet.getTimestamp("last_check_created_at");
                Url url = Url.builder()
                        .id(resultSet.getLong("id"))
                        .protocol(resultSet.getString("protocol"))
                        .host(resultSet.getString("host"))
                        .port(resultSet.getInt("port"))
                        .createdAt(resultSet.getTimestamp("created_at").toLocalDateTime())
                        .lastCheckStatusCode(resultSet.getInt("last_check_status_code"))
                        .lastCheckCreatedAt(lastCheck == null ? null : lastCheck.toLocalDateTime())
                        .build();
                entities.add(url);
            }

        } catch (SQLException e) {
            log.info(e.getMessage());
            throw e;
        }
        return entities;
    }

    public static boolean ifExistsByURL(Url url) throws SQLException {
        var sql = "SELECT id FROM urls WHERE urls.protocol = ? AND urls.host = ? AND urls.port =?";
        try (var conn = dataSource.getConnection();
             var preparedStatement = conn.prepareStatement(sql)) {
            preparedStatement.setString(1, url.getProtocol());
            preparedStatement.setString(2, url.getHost());
            preparedStatement.setInt(3, url.getPort());
            var resultSet = preparedStatement.executeQuery();
            return resultSet.next();
        }
    }
}
