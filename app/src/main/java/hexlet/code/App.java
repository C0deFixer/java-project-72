package hexlet.code;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

import gg.jte.ContentType;
import gg.jte.TemplateEngine;
import gg.jte.resolve.ResourceCodeResolver;
import hexlet.code.dto.MainPage;
import io.javalin.rendering.template.JavalinJte;

import static io.javalin.rendering.template.TemplateUtil.model;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import io.javalin.Javalin;
import io.javalin.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class App {
    //test branches
    private static int getPort() {
        String port = System.getenv().getOrDefault("PORT", "7070");
        return Integer.valueOf(port);
    }

    //locally need can't be run on localhost
    private static String getAdress() {
        return System.getenv().getOrDefault("ADRESS", "127.0.0.1");
    }

    private static String geDataBaseURL() {
        return System.getenv().getOrDefault("JDBC_DATABASE_URL", "jdbc:h2:mem:project;DB_CLOSE_DELAY=-1;");
    }

    private static String geDataBaseUser() {
        return System.getenv().getOrDefault("JDBC_DATABASE_USER", "");
    }

    private static String geDataBasePass() {
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

        app.start(getAdress(), getPort());
    }

    public static Javalin getApp() throws IOException, SQLException {
        // System.setProperty("h2.traceLevel", "TRACE_LEVEL_SYSTEM_OUT=4");

        var hikariConfig = new HikariConfig();
        String dataBaseURL = geDataBaseURL();
        String userName = geDataBaseUser();
        String password = geDataBasePass();
        hikariConfig.setJdbcUrl(dataBaseURL);
        hikariConfig.setUsername(userName);
        hikariConfig.setPassword(password);

        var dataSource = new  HikariDataSource(hikariConfig);
              var sql = readResourceFile("schema.sql");

        log.info(sql);
        try (var connection = dataSource.getConnection();
             var statement = connection.createStatement()) {
            log.info("Connection established!");
            statement.execute(sql);
            //System.out.println("Connection established!");
            log.info("table urls created!");
        } catch (SQLException e) {
            log.info(e.toString());
        }

        var app = Javalin.create(config -> {
            config.bundledPlugins.enableDevLogging();
            config.fileRenderer(new JavalinJte(createTemplateEngine()));
        });

        //BaseRepository.dataSource = dataSource;

        app.before(ctx -> {
            ctx.contentType("text/html; charset=utf-8");
        });


        app.get("/", ctx -> {
            //ctx.render("index.jte", model("page",page));
            var visited = Boolean.valueOf(ctx.cookie("visited"));
            //var page = new MainPage(visited, ctx.sessionAttribute("currentUser"));
            MainPage page = new MainPage(visited, "Dmitry test");
            ctx.render("index.jte", model("page", page));
            ctx.cookie("visited", String.valueOf(true));
        });

        return app;
    }

    private static TemplateEngine createTemplateEngine() {
        ClassLoader classLoader = App.class.getClassLoader();
        ResourceCodeResolver codeResolver = new ResourceCodeResolver("templates", classLoader);
        TemplateEngine templateEngine = TemplateEngine.create(codeResolver, ContentType.Html);
        return templateEngine;
    }
}
