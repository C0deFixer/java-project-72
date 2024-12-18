package hexlet.code;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.stream.Collectors;

import gg.jte.ContentType;
import gg.jte.TemplateEngine;
import gg.jte.resolve.ResourceCodeResolver;
import hexlet.code.controller.UrlController;
import hexlet.code.repository.BaseRepository;
import hexlet.code.util.NamedRoutes;
import io.javalin.rendering.template.JavalinJte;


import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import io.javalin.Javalin;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class App {
    //test branches

    private static int getPort() {
        String port = System.getenv().getOrDefault("PORT", "7070");
        return Integer.valueOf(port);
    }

    //locally need can't be run on localhost
    public static String getAdress() {
        return System.getenv().getOrDefault("ADRESS", "127.0.0.1");
    }

    private static String getDataBaseURL() {
        return System.getenv().getOrDefault("JDBC_DATABASE_URL", "jdbc:h2:mem:project;DB_CLOSE_DELAY=-1;");
    }

    private static String getDataBaseUser() {
        return System.getenv().getOrDefault("JDBC_DATABASE_USER", "");
    }

    private static String getDataBasePass() {
        return System.getenv().getOrDefault("JDBC_DATABASE_PASSWORD", "");
    }

    private static String readResourceFile(String fileName) throws IOException {
        var inputStream = App.class.getClassLoader().getResourceAsStream(fileName);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            return reader.lines().collect(Collectors.joining("\n"));
        }
    }

    public static void main(String[] args) throws IOException, SQLException {
        var app = getApp();

        app.start(getAdress(), getPort()); //use ADRESS ENV for render deploy based on docker host 0.0.0.0
    }

    public static Javalin getApp() throws IOException, SQLException {
        // System.setProperty("h2.traceLevel", "TRACE_LEVEL_SYSTEM_OUT=4");

        var hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(getDataBaseURL());
        hikariConfig.setUsername(getDataBaseUser());
        hikariConfig.setPassword(getDataBasePass());

        var dataSource = new HikariDataSource(hikariConfig);
        var sql = readResourceFile("schema.sql");

        log.info(sql);
        try (var connection = dataSource.getConnection();
             var statement = connection.createStatement()) {
            log.info("Connection established!");
            statement.execute(sql);
            log.info("table urls created!");
        } catch (SQLException e) {
            log.info("SQL Exception happened!");
            log.info(e.toString());
            throw e;
        }

        var app = Javalin.create(config -> {
            config.bundledPlugins.enableDevLogging();
            config.fileRenderer(new JavalinJte(createTemplateEngine()));
        });

        BaseRepository.dataSource = dataSource;
        log.info("dataSource applied!");

        app.before(ctx -> {
            ctx.contentType("text/html; charset=utf-8");
        });

        app.get(NamedRoutes.rootPath(), UrlController::show); // "/"
        app.post(NamedRoutes.buildUrlPath(), UrlController::create); // "/urls"
        app.get(NamedRoutes.showUrlPath(), UrlController::showUrl); // "/urls/{id}"
        app.get(NamedRoutes.urlsPath(), UrlController::showUrlsPath); // "/urls" index.jte

        return app;
    }

    private static TemplateEngine createTemplateEngine() {
        ClassLoader classLoader = App.class.getClassLoader();
        ResourceCodeResolver codeResolver = new ResourceCodeResolver("templates", classLoader);
        TemplateEngine templateEngine = TemplateEngine.create(codeResolver, ContentType.Html);
        return templateEngine;
    }
}
