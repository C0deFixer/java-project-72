package hexlet.code;

import static org.assertj.core.api.Assertions.assertThat;

import hexlet.code.model.Url;
import hexlet.code.model.UrlCheck;
import hexlet.code.repository.UrlCheckRepository;
import hexlet.code.repository.UrlRepository;
import hexlet.code.util.NamedRoutes;
import io.javalin.Javalin;
import io.javalin.testtools.JavalinTest;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junitpioneer.jupiter.SetEnvironmentVariable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;


@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AppTest {
    private Javalin app;
    private final static int portMockWebServer = 9000;
    private static MockWebServer mockWebServer;
    private static ObjectMapper objectMapper;

    private final String FIXTURE_PATH = "/app/src/test/resources";

    private static String readResourceFile(String fileName) throws IOException {
        var inputStream = AppTest.class.getClassLoader().getResourceAsStream(fileName);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            return reader.lines().collect(Collectors.joining("\n"));
        }
    }

    @BeforeAll
    static public void setUpAll() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start(portMockWebServer);

        objectMapper = new ObjectMapper();
    }

    @BeforeEach
    @SetEnvironmentVariable(key = "JDBC_DATABASE_URL", value = "jdbc:h2:mem:project;DB_CLOSE_DELAY=-1;")
    public final void setUp() throws IOException, SQLException {
        app = App.getApp();
    }

    @AfterAll
    static public void tearDownAll() throws IOException {
        mockWebServer.shutdown();
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

    @Order(5)
    @DisplayName("Test create Url Check")
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
        Url url = Url.valueOf(uri.toURL());
        UrlRepository.save(url);

        JavalinTest.test(app, (server, client) -> {
            var response = client.post(NamedRoutes.buildUrlCheckPath().replace("{id}", url.getId().toString()));
            assertThat(response.code()).isEqualTo(200);
            String body = response.body().string();
            Document doc = Jsoup.parse(body);
            Elements tables = doc.getElementsByClass("table-bordered");

            assertThat(tables).isNotNull();
            assertThat(tables).hasSize(2);

            Element table = tables.get(1);
            Element tbody = table.selectFirst("tbody");
            assertThat(tbody).isNotNull();
            Element row = tbody.selectFirst("tr");
            assertThat(row).isNotNull();
            Elements tds = row.getElementsByTag("td");

            //Status code column 2
            assertThat(tds.get(1).text()).isEqualTo("200");
            //H1 column 4
            assertThat(tds.get(3).text()).contains("This page is just Mock response");
            //description column 5
            assertThat(tds.get(4).text()).contains("Bla bla bla");
            //Created at column 6
            LocalDateTime createdAt = LocalDateTime.parse(tds.get(5).text(), hexlet.code.dto.BasePage.DATE_TIME_FORMATTER);

            assertThat(createdAt).isAfter(LocalDateTime.now().minusSeconds(10L));
            assertThat(createdAt).isBefore(LocalDateTime.now());
        });

        List<UrlCheck> urlsChecksByUrlId = UrlCheckRepository.getUrlChecksByUrlId(url.getId());
        assertThat(urlsChecksByUrlId).hasSize(1);
        UrlCheck urlCheck = urlsChecksByUrlId.get(0);
        assertThat(urlCheck).isNotNull();
        assertThat(urlCheck.getUrlId()).isEqualTo(url.getId());
        assertThat(urlCheck.getStatusCode()).isEqualTo(200);
        assertThat(urlCheck.getH1()).isEqualTo("This page is just Mock response");

    }

    @Order(6)
    @DisplayName("Test fail create Url Check")
    @Test
    void testFailUrlCheck() throws IOException, SQLException, URISyntaxException {
        String baseUrl = mockWebServer.url("/").toString();
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(404)
                .addHeader("Content-Type", "text/html;charset=utf-8")
                .setBody("Bad request"));

        URI uri = new URI(baseUrl);
        Url url = Url.valueOf(uri.toURL());
        UrlRepository.save(url);

        JavalinTest.test(app, (server, client) -> {
            var response = client.post(NamedRoutes.buildUrlCheckPath().replace("{id}", url.getId().toString()));
            assertThat(response.code()).isEqualTo(200);
/*
            String body = response.body().string();
            Document doc = Jsoup.parse(body);
*/

            /*            //Chack if alert block contains correct message
             *//*  Block of alert should look like this
                    <div class="rounded-0 m-0 alert alert-dismissible fade show alert-danger" role="alert">
                    <p class="m-0">Некорректный адрес</p>
                    <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                </div>*//*
            Elements alert = doc.getElementsByClass("alert-dismissible");

            Element palert = alert.getFirst().selectFirst("p");
            assertThat(palert.text()).contains("Некорректный адрес");

            Elements tables = doc.getElementsByClass("table-bordered");
            assertThat(tables).isNotNull();
            assertThat(tables).hasSize(2);

            //check table has no info about checks
            Element table = tables.get(1);
            Element tbody = table.selectFirst("tbody");
            assertThat(tbody).isNotNull();
            Element row = tbody.selectFirst("tr");
            assertThat(row).isNull();*/

        });

        assertThat(UrlCheckRepository.getUrlChecksByUrlId(url.getId())).hasSize(0);
    }
}
