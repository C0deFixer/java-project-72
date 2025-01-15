package hexlet.code.exeptions;

public class BadResponseException extends IllegalArgumentException{

    private Long urlId;

    public BadResponseException(Long id, String message) {
        super(message);
        urlId = id;
    }

    public Long getUrlId() {
        return urlId;
    }
}
