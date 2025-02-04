package hexlet.code.controller;

import hexlet.code.dto.UrlPage;
import hexlet.code.dto.UrlsPage;
import hexlet.code.model.Url;
import hexlet.code.model.UrlCheck;
import hexlet.code.repository.UrlCheckRepository;
import hexlet.code.repository.UrlRepository;
import hexlet.code.util.NamedRoutes;
import hexlet.code.util.WebSiteCheck;
import io.javalin.http.Context;
import io.javalin.http.NotFoundResponse;
import kong.unirest.HttpResponse;
import kong.unirest.UnirestException;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.net.URL;
import java.sql.SQLException;
import java.util.Collections;

@Slf4j
public class UrlController {

    static final String FLASH_TYPE_SUCCESS = "alert-success";
    static final String FLASH_TYPE_ALERT = "alert-danger";

    static final String FLASH_TYPE_INFO = "alert-info";

    public static void createUrl(Context ctx) throws SQLException {

        var inputUrl = ctx.formParam("url");
        URI parsedUri;
        try {
            parsedUri = new URI(inputUrl); //Throw URISyntaxException
        } catch (Exception e) {
            ctx.sessionAttribute("flash", "Некорректный URL");
            ctx.sessionAttribute("flash-type", "danger");
            ctx.redirect(NamedRoutes.rootPath());
            return;
        }

        URL parsedUrl; //try catch validation URL is correct
        try {
            parsedUrl = parsedUri.toURL();
        } catch (Exception e) {
            ctx.sessionAttribute("flash", "Некорректный URL");
            ctx.sessionAttribute("flash-type", "danger");
            ctx.redirect(NamedRoutes.rootPath());
            return;
        }

        Url url = new Url(String.format("%s://%s%s",
                        parsedUrl.getProtocol(),
                        parsedUrl.getHost(),
                        parsedUrl.getPort() == -1 ? "" : ":" + parsedUrl.getPort())
                .toLowerCase());

        log.info("Post request create: " + ctx.path() + " form param url: " + inputUrl);
        if (UrlRepository.findByName(url.getName()).isPresent()) {
            log.info("Already exist: " + url.toString());
            ctx.sessionAttribute("flashMessage", "Страница уже существует");
            ctx.sessionAttribute("flashType", FLASH_TYPE_INFO);
            ctx.redirect(NamedRoutes.urlsPath()); // "/"
        } else {
            UrlRepository.save(url);
            ctx.sessionAttribute("flashMessage", "Страница успешно добавлена");
            ctx.sessionAttribute("flashType", FLASH_TYPE_SUCCESS);
            ctx.redirect(NamedRoutes.urlsPath()); // "/urls"
        }

    }

    public static void showUrl(Context ctx) throws SQLException {

        var id = ctx.pathParamAsClass("id", Long.class)
                .get();
        Url url = UrlRepository.findById(id).orElseThrow(()
                -> new NotFoundResponse(String.format("Url with id = %s not found!", id)));
        UrlPage page = UrlPage.builder()
                .id(url.getId())
                .name(url.toString())
                .createdAt(url.getCreatedAt())
                .urlChecks(UrlCheckRepository.getUrlChecksByUrlId(id))
                .build();
        page.setFlash(ctx.consumeSessionAttribute("flashMessage"));
        page.setFlashType(ctx.consumeSessionAttribute("flashType"));
        ctx.render("urls/url.jte", Collections.singletonMap("page", page));

    }

    public static void listUrls(Context ctx) throws SQLException {
        UrlsPage page = new UrlsPage(UrlRepository.getUrlEntities());
        page.setFlash(ctx.consumeSessionAttribute("flashMessage"));
        page.setFlashType(ctx.consumeSessionAttribute("flashType"));
        ctx.render("urls/index.jte", Collections.singletonMap("page", page));
    }

    public static void createUrlCheck(Context ctx) throws SQLException {

        var id = ctx.pathParamAsClass("id", Long.class)
                .get();
        Url url = UrlRepository.findById(id).orElseThrow(()
                -> new NotFoundResponse(String.format("Url with id %d not found", id)));

        HttpResponse<String> response = null;
        try {
            response = WebSiteCheck.webSiteCheck(id, url.toString());
        } catch (UnirestException e) {
            ctx.sessionAttribute("flashMessage", "Некорректный адрес");
            ctx.sessionAttribute("flashType", FLASH_TYPE_ALERT);
            ctx.redirect(NamedRoutes.urlsPath() + "/" + id);
        }

        if (response != null) {
            UrlCheck urlCheck = WebSiteCheck.parseHtmlBody(response);
            urlCheck.setUrlId(id);
            UrlCheckRepository.save(urlCheck);
            ctx.sessionAttribute("flashMessage", "Страница успешно проверена");
            ctx.sessionAttribute("flashType", FLASH_TYPE_SUCCESS);
        }
        ctx.redirect(NamedRoutes.urlsPath() + "/" + id);

    }


}
