package hexlet.code.controller;
import hexlet.code.dto.MainPage;
import hexlet.code.model.Url;
import hexlet.code.repository.UrlRepository;
import hexlet.code.util.NamedRoutes;
import io.javalin.http.Context;
import io.javalin.validation.ValidationException;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.sql.SQLException;
import java.util.Collections;

import static io.javalin.rendering.template.TemplateUtil.model;

public class UrlController {
    private String flashTipe = "alert alert-success"; // "alert alert-warning"

    public static void create(Context ctx) throws SQLException {

        try {
            var name = ctx.formParamAsClass("url", String.class)
                    .check(value -> value.trim().length() > 2, "Url сайта должно быть длиннее двух символов")
                    .check(value -> UrlRepository.existsByURL(URI.create(value.trim()).toURL()), "Страница уже существует")
                    .get();
            var uri = URI.create(name);

            UrlRepository.save(url);
            ctx.sessionAttribute("flashMessage", "Страница успешно добавлена");
            ctx.sessionAttribute("flashType", "alert alert-success");
            ctx.redirect(NamedRoutes.urlsPath());
        } catch (ValidationException|MalformedURLException e) {

            MainPage page = new MainPage(true);
            ctx.sessionAttribute("flashMessage", "Некоррекный URL");
            ctx.sessionAttribute("flashType", "alert alert-danger");
            ctx.render("index.jte", Collections.singletonMap("page", page));
        }


    }

    public static void showUrl(Context ctx) throws SQLException {

        try {
            var name = ctx.pathParamAsClass("id", Long.class)
                    .get();

            var url = new URL(name);

            Post post = new Post(name, body);
            PostRepository.save(post);
            ctx.redirect(NamedRoutes.postsPath());
        } catch (ValidationException e) {
            var name = ctx.formParamAsClass("name", String.class)
                    .getOrDefault("");
            var body = ctx.formParamAsClass("body", String.class)
                    .getOrDefault("");
            //Post post = new Post(name, body);
            PostPage page = new PostPage(name, body, e.getErrors());
            ctx.render("posts/show.jte", Collections.singletonMap("page", page));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }


    }

    public static void showUrlsPath(Context ctx) throws SQLException {

        try {
            var name = ctx.formParamAsClass("url", String.class)
                    .check(value -> value.trim().length() > 2, "Url сайта должно быть длиннее двух символов")
                    .check(UrlRepository::existsByName, "Пост с таким названием уже существует")
                    .get();
            var body = ctx.formParamAsClass("body", String.class)
                    .check(value -> value.trim().length() > 10, "")
                    .get();
            var url = new URL(name);
            ctx.consumeSessionAttribute("flash");
            Post post = new Post(name, body);
            PostRepository.save(post);
            ctx.redirect(NamedRoutes.postsPath());
        } catch (ValidationException e) {
            var name = ctx.formParamAsClass("name", String.class)
                    .getOrDefault("");
            var body = ctx.formParamAsClass("body", String.class)
                    .getOrDefault("");
            //Post post = new Post(name, body);
            PostPage page = new PostPage(name, body, e.getErrors());
            ctx.render("posts/show.jte", model("",));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }


    }
}
