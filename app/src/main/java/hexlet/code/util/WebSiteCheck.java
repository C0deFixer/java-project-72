package hexlet.code.util;

import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import kong.unirest.UnirestException;
import lombok.extern.slf4j.Slf4j;

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
}
