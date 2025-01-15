package hexlet.code.controller;

import hexlet.code.dto.MainPage;
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
import io.javalin.validation.ValidationException;
import kong.unirest.HttpResponse;
import kong.unirest.UnirestException;
import lombok.extern.slf4j.Slf4j;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Optional;

import static java.lang.String.format;

@Slf4j
public class UrlController {
    public static final String FLASH_TYPE_SUCCESS = "alert-success";
    public static final String FLASH_TYPE_ALERT = "alert-danger";

    public static final String FLASH_TYPE_INFO = "alert-info";

    public static void show(Context ctx) {
        MainPage page = new MainPage(false);
        page.setFlash(ctx.consumeSessionAttribute("flashMessage"));
        page.setFlashType(ctx.consumeSessionAttribute("flashType"));
        ctx.render("index.jte", Collections.singletonMap("page", page));
    }

    public static void create(Context ctx) throws SQLException {

        try {
            var name = ctx.formParamAsClass("url", String.class)
                    .check(value -> value.trim().length() > 2, "Url сайта должно быть длиннее двух символов")
                    .get();
            log.info("Post request create: " + ctx.path() + " form param url: " + name);
            URI uri = new URI(name.trim()); //Throw URISyntaxException
            if (UrlRepository.ifExistsByURL(uri.toURL())) {
                log.info("Already exist: " + uri);
                ctx.sessionAttribute("flashMessage", "Страница уже существует");
                ctx.sessionAttribute("flashType", FLASH_TYPE_INFO);
                ctx.redirect(NamedRoutes.urlsPath()); // "/"
            } else {
                var url = URI.create(name);
                UrlRepository.save(Url.valueOf(url.toURL()));
                ctx.sessionAttribute("flashMessage", "Страница успешно добавлена");
                ctx.sessionAttribute("flashType", FLASH_TYPE_SUCCESS);
                ctx.redirect(NamedRoutes.urlsPath()); // "/urls"
            }
        } catch (URISyntaxException | ValidationException | MalformedURLException | IllegalArgumentException e) {
            //todo keep entered value of URL string by user
            ctx.sessionAttribute("flashMessage", "Некоррекный URL: " + e.getMessage());
            ctx.sessionAttribute("flashType", FLASH_TYPE_ALERT);
            ctx.redirect(NamedRoutes.rootPath());
        }

    }

    public static void showUrl(Context ctx) throws SQLException {

        try {
            var id = ctx.pathParamAsClass("id", Long.class)
                    .get();
            Optional<Url> urlOptional = UrlRepository.getById(id);
            if (urlOptional.isEmpty()) {
                log.info(String.format("Url with id = %s not found! throwing exception", id));
                throw new NotFoundResponse(String.format("Url with id = %s not found!", id));
                /*ctx.sessionAttribute("flashMessage", format("URL c id = %s не найден!", id));
                ctx.sessionAttribute("flashType", FLASH_TYPE_ALERT);
            //ctx.redirect(NamedRoutes.rootPath());*/
            } else {
                Url url = urlOptional.get();
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
        } catch (ValidationException e) {
            var id = ctx.pathParam("id");
            ctx.sessionAttribute("flashMessage", format("Некорретное значение id для Url = %s !", id));
            ctx.sessionAttribute("flashType", FLASH_TYPE_ALERT);
            ctx.redirect(NamedRoutes.rootPath());
        } catch (SQLException e) {
            var id = ctx.pathParam("id");
            ctx.sessionAttribute("flashMessage", format("Ошибка получения данных Url c id %d: " + e.getMessage(), id));
            ctx.sessionAttribute("flashType", FLASH_TYPE_ALERT);
            ctx.redirect(NamedRoutes.rootPath());
        }

    }

    public static void showUrlsPath(Context ctx) throws SQLException {
        UrlsPage page = new UrlsPage(UrlRepository.getUrlEntities());
        page.setFlash(ctx.consumeSessionAttribute("flashMessage"));
        page.setFlashType(ctx.consumeSessionAttribute("flashType"));
        ctx.render("urls/index.jte", Collections.singletonMap("page", page));
    }

    public static void createUrlCheck(Context ctx) throws SQLException {
        try {
            var id = ctx.pathParamAsClass("id", Long.class)
                    .get();
            Optional<Url> url = UrlRepository.getById(id);
            if (url.isEmpty()) {
                throw new NotFoundResponse(String.format("Url with id %d not found", id));
            } else {
                try {
                    HttpResponse<String> response = WebSiteCheck.webSiteCheck(id, url.get().toUrlString());
                    UrlCheck urlCheck = UrlCheck.parseHtmlBody(response);
                    urlCheck.setUrlId(id);
                    UrlCheckRepository.save(urlCheck);
                    ctx.sessionAttribute("flashMessage", "Страница успешно проверена");
                    ctx.sessionAttribute("flashType", FLASH_TYPE_SUCCESS);
                    ctx.redirect(NamedRoutes.urlsPath() + "/" + id);
                } catch (ValidationException | UnirestException e) {
                    ctx.sessionAttribute("flashMessage", "Некорректный адрес");
                    ctx.sessionAttribute("flashType", FLASH_TYPE_ALERT);
                    ctx.redirect(NamedRoutes.urlsPath() + "/" + id);
                }

            }

        } catch (Exception e) {
            log.info(e.getMessage());
            throw e;
        }


    }


}
