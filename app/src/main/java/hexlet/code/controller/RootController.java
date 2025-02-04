package hexlet.code.controller;

import hexlet.code.dto.MainPage;
import io.javalin.http.Context;

import java.util.Collections;

public class RootController {
    public static void show(Context ctx) {
        MainPage page = new MainPage(false);
        page.setFlash(ctx.consumeSessionAttribute("flashMessage"));
        page.setFlashType(ctx.consumeSessionAttribute("flashType"));
        ctx.render("index.jte", Collections.singletonMap("page", page));
    }
}
