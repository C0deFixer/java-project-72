package hexlet.code;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.stream.Collectors;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;


import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import gg.jte.ContentType;
import gg.jte.TemplateEngine;
import gg.jte.resolve.ResourceCodeResolver;
import hexlet.code.repository.BaseRepository;
import hexlet.code.util.NamedRoutes;
import io.javalin.Javalin;
import io.javalin.rendering.template.JavalinJte;
import lombok.extern.slf4j.Slf4j;

import hexlet.code.controller.RootController;
import hexlet.code.controller.UrlController;

@Slf4j
public final class App {

    private static int getPort() {
        String port = System.getenv().getOrDefault("PORT", "3000");
        return Integer.valueOf(port);
    }

    public static String getAddress() {
        return System.getenv().getOrDefault("ADRESS", ""); //locally need can't be run on localhost
    }

    private static String getDataBaseURL() {
        return System.getenv().getOrDefault("JDBC_DATABASE_URL", "jdbc:h2:mem:project;DB_CLOSE_DELAY=-1;");
    }

    public static void main(String[] args) throws SQLException, IOException {
        Javalin app = getApp();
        String address = getAddress();
        if (address.isEmpty()) {
            app.start(getPort());
        } else {
            app.start(address, getPort()); //use ADRESS ENV for render deploy based on docker host 0.0.0.0
        }
    }

    public static Javalin getApp() throws IOException, SQLException {
        HikariConfig hikariConfig;
        // System.setProperty("h2.traceLevel", "TRACE_LEVEL_SYSTEM_OUT=4");

        //DataSource initiate 2 ways: 1) Use dataSourceClassName & Properties 2) DB URL
        String dataSourceClassName = System.getenv().getOrDefault("JDBC_DSCN", "");
        if (dataSourceClassName.isEmpty()) {
            hikariConfig = new HikariConfig();
            hikariConfig.setJdbcUrl(getDataBaseURL());
            hikariConfig.setUsername(System.getenv().getOrDefault("JDBC_DATABASE_USER", ""));
            hikariConfig.setPassword(System.getenv().getOrDefault("JDBC_DATABASE_PASSWORD", ""));
        } else {
            Properties props = new Properties();
            props.setProperty("dataSourceClassName", dataSourceClassName); //"org.postgresql.ds.PGSimpleDataSource"
            props.setProperty("dataSource.serverName", System.getenv().getOrDefault("JDBC_HOST", "localhost"));
            props.setProperty("dataSource.portNumber", System.getenv().getOrDefault("JDBC_PORT", "5432"));
            props.setProperty("dataSource.user", System.getenv().getOrDefault("JDBC_DATABASE_USER", ""));
            props.setProperty("dataSource.password", System.getenv().getOrDefault("JDBC_DATABASE_PASSWORD", ""));
            props.setProperty("dataSource.databaseName", System.getenv().getOrDefault("JDBC_DBNAME", "project"));
            hikariConfig = new HikariConfig(props);
        }

        var dataSource = new HikariDataSource(hikariConfig);
        String sql = readResourceFile("schema.sql");

        log.info(sql);
        try (var connection = dataSource.getConnection();
             var statement = connection.createStatement()) {
            log.info("Connection established!");
            statement.execute(sql);
            log.info("tables indexes and constrains created!");
        } catch (SQLException e) {
            log.info("SQL Exception happened!");
            log.info(e.toString());
            throw e;
        }
        BaseRepository.dataSource = dataSource;

        var app = Javalin.create(config -> {
            config.bundledPlugins.enableDevLogging();
            config.fileRenderer(new JavalinJte(createTemplateEngine()));
        });

        app.before(ctx -> {
            ctx.contentType("text/html; charset=utf-8");
        });


        app.get(NamedRoutes.rootPath(), RootController::show); // "/"
        app.get(NamedRoutes.urlsPath(), UrlController::listUrls); // "/urls" index.jte
        app.post(NamedRoutes.urlsPath(), UrlController::createUrl); // "/urls"
        app.get(NamedRoutes.urlPath("{id}"), UrlController::showUrl); // "/urls/{id}"
        app.post(NamedRoutes.urlChecksPath("{id}"), UrlController::createUrlCheck); //urls/{id}/checks

        return app;
    }

    private static TemplateEngine createTemplateEngine() {
        ClassLoader classLoader = App.class.getClassLoader();
        ResourceCodeResolver codeResolver = new ResourceCodeResolver("templates", classLoader);
        TemplateEngine templateEngine = TemplateEngine.create(codeResolver, ContentType.Html);
        return templateEngine;
    }

    private static String readResourceFile(String fileName) throws IOException {
        var inputStream = App.class.getClassLoader().getResourceAsStream(fileName);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            return reader.lines().collect(Collectors.joining("\n"));
        }

    }


}
