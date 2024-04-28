package hexlet.code.repository;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import hexlet.code.model.Url;
import org.eclipse.jetty.server.LocalConnector;

import java.net.URL;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UrlRepository extends BaseRepository {

    public static void save(Url url) throws SQLException {
        var sql = "INSERT INTO urls (name, created_at) VALUES (?, ?)";
        try (var conn = dataSource.getConnection();
             var preparedStatement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            LocalDateTime created_at = LocalDateTime.now();
            preparedStatement.setString(1, url.getProtocol () );
            preparedStatement.setString(2, created_at.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")));
            preparedStatement.executeUpdate();
            var generatedKeys = preparedStatement.getGeneratedKeys();
            if (generatedKeys.next()) {
                url.setId(generatedKeys.getLong(1));
            } else {
                throw new SQLException("DB have not returned an id after saving an entity");
            }
        }
    }

    public static Optional<Url> getById(Long id) throws SQLException {
        var sql = "SELECT id, name, protocol, authority, created_at FROM urls WHERE id = ? ";
        try (var conn = dataSource.getConnection();
             var preparedStatement = conn.prepareStatement(sql)) {
            preparedStatement.setString(1, id.toString());
            var resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                Url url = new Url(resultSet.getLong("id"),
                        resultSet.getString("name"),
                        resultSet.getString("protocol"),
                        resultSet.getString("authority"),
                        resultSet.getTimestamp("created_at").toLocalDateTime());
                return  Optional.of(url);
            }
            else return Optional.empty();
        }
    }

    public static List<Url> getUrlEntities(Long id) throws SQLException {
        var sql = "SELECT id, name, protocol, authority, created_at FROM urls ORDER BY created_at DESC LIMIT 50";
        List<Url> entities = new ArrayList<>();
        try (var conn = dataSource.getConnection();
             var preparedStatement = conn.prepareStatement(sql)) {
            preparedStatement.setString(1, id.toString());
            var resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                Url url = new Url(resultSet.getLong("id"),
                        resultSet.getString("name"),
                        resultSet.getString("protocol"),
                        resultSet.getString("authority"),
                        resultSet.getTimestamp("created_at").toLocalDateTime());
                entities.add(url);
            }

        }
        return entities;
    }

    public boolean existsByName(String name) throws SQLException {
        var sql = "SELECT name FROM urls WHERE name = ?";
        try (var conn = dataSource.getConnection();
        var preparedStatement = conn.prepareStatement(sql)) {
            preparedStatement.setString(1, name);
            var resultSet = preparedStatement.executeQuery();
            return resultSet.next();
        }
    }

    public static boolean existsByURL(URL url) throws SQLException {
        var sql = "SELECT name FROM urls WHERE protocol = ? AND authority = ?";
        try (var conn = dataSource.getConnection();
             var preparedStatement = conn.prepareStatement(sql)) {
            preparedStatement.setString(1, url.getProtocol());
            preparedStatement.setString(2, url.getAuthority());
            var resultSet = preparedStatement.executeQuery();
            return resultSet.next();
        }
    }
}
