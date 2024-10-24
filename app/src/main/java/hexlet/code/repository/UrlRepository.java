package hexlet.code.repository;

import hexlet.code.model.Url;
import lombok.extern.slf4j.Slf4j;

import java.net.URL;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
public class UrlRepository extends BaseRepository {

    final static int LIMIT = 50;

    public static void save(Url url) throws SQLException {
        var sql = "INSERT INTO urls (protocol, host, port, created_at) VALUES (?, ?, ?, ?)";
        try (var conn = dataSource.getConnection();
             var preparedStatement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            LocalDateTime created_at = LocalDateTime.now();
            log.info("Set created_at: "  + created_at.format(BaseRepository.dateTimeFormatter));
            preparedStatement.setString(1, url.getProtocol());
            preparedStatement.setString(2, url.getHost());
            preparedStatement.setInt(3, url.getPort());
            preparedStatement.setTimestamp(4, Timestamp.valueOf(created_at));
            preparedStatement.executeUpdate();
            var generatedKeys = preparedStatement.getGeneratedKeys();
            if (generatedKeys.next()) {
                url.setId(generatedKeys.getLong(1));
                url.setCreatedAt(created_at);
                log.info("New Url save to DB: "+  url);
            } else {
                throw new SQLException("DB have not returned an id after saving an entity");
            }
        }
    }

    public static Optional<Url> getById(Long id) throws SQLException {
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
                return Optional.empty();}
        }
    }

    public static List<Url> getUrlEntities() throws SQLException {
        var sql = "SELECT id, protocol, host, port, created_at FROM urls ORDER BY created_at DESC LIMIT " + LIMIT;
        List<Url> entities = new ArrayList<>();
        try (var conn = dataSource.getConnection();
             var preparedStatement = conn.prepareStatement(sql)) {
            var resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                Url url = Url.builder()
                        .id(resultSet.getLong("id"))
                        .protocol(resultSet.getString("protocol"))
                        .host(resultSet.getString("host"))
                        .port(resultSet.getInt("port"))
                        .createdAt(resultSet.getTimestamp("created_at").toLocalDateTime())
                        .build();
                entities.add(url);
            }

        }
        return entities;
    }

    public static boolean alreadyExistsByURL(URL url) throws SQLException {
        var sql = "SELECT id FROM urls WHERE protocol = ? AND host = ? AND port =?";
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
