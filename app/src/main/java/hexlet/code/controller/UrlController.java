package hexlet.code.controller;

import hexlet.code.dto.MainPage;
import hexlet.code.dto.UrlPage;
import hexlet.code.dto.UrlsPage;
import hexlet.code.model.Url;
import hexlet.code.repository.UrlRepository;
import hexlet.code.util.NamedRoutes;
import io.javalin.http.Context;
import io.javalin.http.NotFoundResponse;
import io.javalin.validation.ValidationException;
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
    public static final String FLASH_TYPE_ALERT = "alert-success";

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
                ctx.redirect(NamedRoutes.rootPath()); // "/"
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
                throw new NotFoundResponse("URL c id = %s не найден!");
                /*ctx.sessionAttribute("flashMessage", format("URL c id = %s не найден!", id));
                ctx.sessionAttribute("flashType", FLASH_TYPE_ALERT);
            //ctx.redirect(NamedRoutes.rootPath());*/
            } else {
                Url url = urlOptional.get();
                UrlPage page = new UrlPage(url.getId(), url.toString(), url.getCreatedAt());
                /*ctx.sessionAttribute("flashMessage", "Страница успешно добавлена");
                ctx.sessionAttribute("flashType", "alert alert-success");*/
                ctx.render("urls/url.jte", Collections.singletonMap("page", page));
            }
        } catch (ValidationException e) {
            var id = ctx.pathParam("id");
            ctx.sessionAttribute("flashMessage", format("Некорретное значение id для Url = %s !", id));
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
}
