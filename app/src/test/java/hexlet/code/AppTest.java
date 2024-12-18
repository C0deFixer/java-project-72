package hexlet.code;

import static org.assertj.core.api.Assertions.assertThat;

import hexlet.code.model.Url;
import hexlet.code.repository.UrlRepository;
import hexlet.code.util.NamedRoutes;
import io.javalin.Javalin;
import io.javalin.testtools.JavalinTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junitpioneer.jupiter.SetEnvironmentVariable;

import java.io.IOException;
import java.sql.SQLException;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AppTest {
    private Javalin app;

    @BeforeEach
    @SetEnvironmentVariable(key = "JDBC_DATABASE_URL", value = "jdbc:h2:mem:project;DB_CLOSE_DELAY=-1;")
    public final void setUp() throws IOException, SQLException {
        app = App.getApp();
    }

    @Test
    @Order(1)
    @DisplayName("Test Main page")
    void testMainPage() {
        JavalinTest.test(app, (server, client) -> {
            var response = client.get("/");
            assertThat(response.code()).isEqualTo(200);
            assertThat(response.body().string()).contains("Анализатор страниц")
                    .contains("Бесплатно проверяйте сайты на SEO пригодность");
        });
    }

    @Order(2)
    @DisplayName("Test post urls")
    @ParameterizedTest
    @ValueSource(strings = {"https://tproger.ru", "https://www.h2database.com", "https://app.docker.com"})
    void testCreateUrl(String candidate) {
        JavalinTest.test(app, (server, client) -> {
            var requestBody = "url=" + candidate;
            var response = client.post(NamedRoutes.urls(), requestBody);
            assertThat(response.code()).isEqualTo(200);
            assertThat(response.body().contentType().toString()).isEqualTo("text/html;charset=utf-8");
            assertThat(response.body().string()).contains("Сайт")
                    .contains(candidate);
        });

    }

    @Order(3)
    @DisplayName("Test exceptions of post urls")
    @ParameterizedTest
    @ValueSource(strings = {"tproger.ru", "https://www.h2database", "https://.com"})
    void testCreateUrlFail(String candidate) {

        JavalinTest.test(app, (server, client) -> {
            var requestBody = "url=" + candidate;
            var response = client.post("/urls", requestBody);
            assertThat(response.priorResponse().code()).isEqualTo(302); //redirect mean that
            assertThat(response.code()).isEqualTo(200);
            String stBody = response.body().string();
            assertThat(response.body().contentType().toString()).isEqualTo("text/html;charset=utf-8");
            assertThat(stBody).contains("Анализатор страниц");
        });
    }

    @Order(4)
    @DisplayName("Test url page")
    @Test
    public void testUrlPage() throws SQLException {
        var url = new Url("https", "homepage.su");
        UrlRepository.save(url);
        JavalinTest.test(app, (server, client) -> {
            var response = client.get(NamedRoutes.urls() + "/" + url.getId());
            assertThat(response.code()).isEqualTo(200);
            assertThat(response.body().string()).contains(url.toString());
        });
    }

    @Order(4)
    @DisplayName("Test url page not found")
    @Test
    void testUrlNotFound() {
        JavalinTest.test(app, (server, client) -> {
            var response = client.get(NamedRoutes.urls() + "/999999");
            assertThat(response.code()).isEqualTo(404);
        });
    }
}