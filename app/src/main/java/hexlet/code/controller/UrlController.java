package hexlet.code.controller;

import hexlet.code.dto.BasePage;
import hexlet.code.dto.MainPage;
import hexlet.code.dto.UrlPage;
import hexlet.code.dto.UrlsPage;
import hexlet.code.model.Url;
import hexlet.code.repository.UrlRepository;
import hexlet.code.util.NamedRoutes;
import io.javalin.http.Context;
import io.javalin.validation.ValidationException;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Optional;

import static java.lang.String.format;

public class UrlController {
    public static final String flashTipeSuccess = "alert-success";
    public static final String flashTipeDanger = "alert-success";

    public static void show(Context ctx) throws SQLException {
        MainPage page = new MainPage(false);
        page.setFlash(ctx.consumeSessionAttribute("flashMessage"));
        page.setFlashTipe(ctx.consumeSessionAttribute("flashType"));
        ctx.render("index.jte", Collections.singletonMap("page", page));
    }
    public static void create(Context ctx) throws SQLException {

        try {
            var name = ctx.formParamAsClass("url", String.class)
                    .check(value -> value.trim().length() > 2, "Url сайта должно быть длиннее двух символов")
                    .get();
            URI uri = new URI(name.trim()); //Throw URISyntaxException
            if (UrlRepository.alreadyExistsByURL(uri.toURL())) {
                ctx.sessionAttribute("flashMessage", "Страница уже существует");
                ctx.sessionAttribute("flashType", flashTipeDanger);
                ctx.redirect(NamedRoutes.rootPath());
            } else {
                var url = URI.create(name);
                UrlRepository.save(Url.valueOf(url.toURL()));
                ctx.sessionAttribute("flashMessage", "Страница успешно добавлена");
                ctx.sessionAttribute("flashType", flashTipeSuccess);
                ctx.redirect(NamedRoutes.urlsPath());
            }
        } catch (URISyntaxException | ValidationException | MalformedURLException e) {
            ctx.sessionAttribute("flashMessage", "Некоррекный URL");
            ctx.sessionAttribute("flashType", flashTipeDanger);
            ctx.redirect(NamedRoutes.rootPath());
        }

    }

    public static void showUrl(Context ctx) throws SQLException {

        try {
            var id = ctx.pathParamAsClass("id", Long.class)
                    .get();
            Optional<Url> urlOptional = UrlRepository.getById(id);
            if (urlOptional.isEmpty()) {
                ctx.sessionAttribute("flashMessage", format("URL c id = %s не найден!", id));
                ctx.sessionAttribute("flashType", flashTipeDanger);
                ctx.redirect(NamedRoutes.rootPath());
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
            ctx.sessionAttribute("flashType", flashTipeDanger);
            ctx.redirect(NamedRoutes.rootPath());
        }

    }

    public static void showUrlsPath(Context ctx) throws SQLException {
        UrlsPage page = new UrlsPage(UrlRepository.getUrlEntities());
/*            page.setFlash(ctx.consumeSessionAttribute("flash"));
            page.setFlashTipe(ctx.consumeSessionAttribute("flashTipe"));*/
        ctx.render("urls/index.jte", Collections.singletonMap("page", page));
    }
}
