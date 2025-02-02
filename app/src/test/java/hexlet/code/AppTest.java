package hexlet.code;

import static org.assertj.core.api.Assertions.assertThat;

import hexlet.code.model.Url;
import hexlet.code.model.UrlCheck;
import hexlet.code.repository.BaseRepository;
import hexlet.code.repository.UrlCheckRepository;
import hexlet.code.repository.UrlRepository;
import hexlet.code.util.NamedRoutes;
import io.javalin.Javalin;
import io.javalin.testtools.JavalinTest;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.ClassOrderer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestClassOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

@TestClassOrder(ClassOrderer.OrderAnnotation.class)
@Slf4j
class AppTest {
    private Javalin app;
    private static final int PORT_MOCK_WEB_SERVER = 9000;
    private static MockWebServer mockWebServer;

    private static final String FIXTURE_PATH = "/app/src/test/resources";

    private static String readResourceFile(String fileName) throws IOException {
        var inputStream = AppTest.class.getClassLoader().getResourceAsStream(fileName);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            return reader.lines().collect(Collectors.joining("\n"));
        }
    }

    @BeforeAll
    public static void beforeAll() throws IOException {
        log.info("beforeAll: MockWebServer get instance");
        mockWebServer = new MockWebServer();
        mockWebServer.start(PORT_MOCK_WEB_SERVER);
    }

    @BeforeEach
    //@SetEnvironmentVariable(key = "JDBC_DATABASE_URL", value = "jdbc:h2:mem:project;DB_CLOSE_DELAY=-1;")
    public final void setUp() throws IOException, SQLException {
        log.info("BeforeEach setUp:  App get instance");
        app = App.getApp();
    }

    @AfterEach
    public void closeResources() throws SQLException {
        log.info("AfterEach: Closing DB connections");
        BaseRepository.dataSource.getConnection().close();
    }

    @AfterAll
    public static void tearDownAll() throws IOException {
        log.info("AfterAll: tearDownAll");
        mockWebServer.shutdown();
    }

    //@Test
    //@Order(1)
    //@DisplayName("Test Main page")
    @Nested
    @Order(1)
    class TestMainPage {
        @Test
        void testMainPage() {
            JavalinTest.test(app, (server, client) -> {
                var response = client.get("/");
                assertThat(response.code()).isEqualTo(200);
                assertThat(response.body().string()).contains("Анализатор страниц")
                        .contains("Бесплатно проверяйте сайты на SEO пригодность");
            });
        }
    }

    @Nested
    @Order(2)
    @DisplayName("Test post urls")
    class TestCreateUrl {
        @ParameterizedTest
        @ValueSource(strings = {"https://tproger.ru", "https://www.h2database.com:8080", "http://localhost:41905"})
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
    }

    @Nested
    class TestCreateUrlFail {
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
    }

    @Nested
    @Order(4)
    @DisplayName("Test url page")
    class TestUrlPage {
        @Test
        public void testUrlPage() throws SQLException {
            var url = new Url("https//:homepage.su");
            UrlRepository.save(url);
            JavalinTest.test(app, (server, client) -> {
                var response = client.get(NamedRoutes.urls() + "/" + url.getId());
                assertThat(response.code()).isEqualTo(200);
                String body = response.body().string();
                assertThat(body).contains(url.toString());
                assertThat(body).contains(url.getId().toString());
            });
        }


        @DisplayName("Test url page not found")
        @Test
        void testUrlNotFound() {
            JavalinTest.test(app, (server, client) -> {
                var response = client.get(NamedRoutes.urls() + "/999999");
                assertThat(response.code()).isEqualTo(404);
            });
        }
    }

    @Nested
    @Order(5)
    @DisplayName("Test create Url Check")
    class CreateUrlCheck {
        @Test
        void testCreateUrlCheck() throws IOException, SQLException, URISyntaxException {
            String baseUrl = mockWebServer.url("/").toString();
            String bodyStub = readResourceFile("testUrlCheck.html");
            mockWebServer.enqueue(new MockResponse()
                    .setResponseCode(200)
                    .addHeader("Content-Type", "text/html;charset=utf-8")
                    .setBody(bodyStub));

            //Url url = objectMapper.readValue(urlJson, Url.class);
            URI uri = new URI(baseUrl);
            Url url = Url.valueOf(uri);
            UrlRepository.save(url);

            JavalinTest.test(app, (server, client) -> {
                var response = client.post(NamedRoutes.urlChecksPath(url.getId().toString()));
                assertThat(response.code()).isEqualTo(200);
                String body = response.body().string();
                assertThat(body).contains("This page is just Mock response");
            });

            List<UrlCheck> urlsChecksByUrlId = UrlCheckRepository.getUrlChecksByUrlId(url.getId());
            assertThat(urlsChecksByUrlId).hasSize(1);
            UrlCheck urlCheck = urlsChecksByUrlId.get(0);
            assertThat(urlCheck).isNotNull();
            assertThat(urlCheck.getUrlId()).isEqualTo(url.getId());
            assertThat(urlCheck.getStatusCode()).isEqualTo(200);
            assertThat(urlCheck.getH1()).isEqualTo("This page is just Mock response");

        }
    }

    @Nested
    @Order(6)
    @DisplayName("Test fail create Url Check")
    class TestFailUrlCheck {
        @Test
        void testFailUrlCheck() throws IOException, SQLException, URISyntaxException {
            String baseUrl = mockWebServer.url("/").toString();
            mockWebServer.enqueue(new MockResponse()
                    .setResponseCode(404)
                    .addHeader("Content-Type", "text/html;charset=utf-8")
                    .setBody("Bad request"));

            URI uri = new URI(baseUrl);
            Url url = Url.valueOf(uri);
            UrlRepository.save(url);

            JavalinTest.test(app, (server, client) -> {
                var response = client.post(NamedRoutes.urlChecksPath(url.getId().toString()));
                assertThat(response.code()).isEqualTo(200);

            });

            assertThat(UrlCheckRepository.getUrlChecksByUrlId(url.getId())).hasSize(0);
        }
    }
}
