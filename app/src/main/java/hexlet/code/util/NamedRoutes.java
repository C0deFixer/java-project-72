package hexlet.code.util;

public class NamedRoutes {

    public static String rootPath() {
        return "/";
    }

    public static String urls() {
        return "/urls";
    }

    public static String sessionsPath() {
        return "/sessions";
    }

    public static String buildSessionPath() {
        return "/sessions/build";
    }

    public static String buildUrlPath() {
        return "/urls";
    }

    public static String showUrlPath() {
        return "/urls/{id}";
    }

    public static String urlsPath() {
        return "/urls";
    }

    public static String buildUrlCheckPath() {
        return "/urls/{id}/checks";
    }
}
