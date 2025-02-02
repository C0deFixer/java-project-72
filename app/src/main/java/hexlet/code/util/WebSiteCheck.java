package hexlet.code.util;

import hexlet.code.model.UrlCheck;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import kong.unirest.UnirestException;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

@Slf4j
public class WebSiteCheck {
    public static HttpResponse<String> webSiteCheck(Long id, String url) {
        HttpResponse<String> response = Unirest.get(url)
                .header("Accept", "text/html")
                .asString()
                .ifFailure(resp -> {
                    int status = resp.getStatus();
                    log.error("Url check of " + url + " fail with Status code" + status);
                    resp.getParsingError().ifPresent(e -> {
                        log.error("Parsing Exception: ", e);
                        log.error("Original body: " + e.getOriginalBody());
                    });
                    throw new UnirestException(
                            String.format("Сайт %s вернул Некорретный статус %d !", url, status));
                });
        return response;
    }

    public static UrlCheck parseHtmlBody(HttpResponse<String> response) {
        //Examples of tags
        //get title <title>Tproger — всё о программировании</title>
        //get desc <meta name="description" content="blabla">
        //get h1 <h1 class="logo-xxx" data-v-bdb999f8=""> bla bla </h1>
        Document doc = Jsoup.parse(response.getBody());

        Element h1Element = doc.selectFirst("h1");
        Element descriptionElement = doc.selectFirst("meta[name=description]");

        String body = response.getBody();
        return UrlCheck.builder()
                .statusCode(response.getStatus())
                .h1(h1Element == null ? "" : h1Element.text())
                .title(doc.title())
                .description(descriptionElement == null ? "" : descriptionElement.attr("content"))
                .build();
    }
}
