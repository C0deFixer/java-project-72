package hexlet.code.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.Builder;

import java.net.URI;
import java.time.LocalDateTime;

@AllArgsConstructor
@Getter
@Setter
@Builder
public class Url {
    Long id;
    String name;

    LocalDateTime createdAt;
    int lastCheckStatusCode;
    LocalDateTime lastCheckCreatedAt;

    public Url() {
        //for jackson parsing to POJO from json test file
    }

    public Url(String name) {
        this.name = name;
    }

    /**
     * Use for represent URL as String in jte.
     */
    @Override
    public String toString() {
        return name;
    }

    public static Url valueOf(URI uri) {
        return new Url(String
                .format(
                        "%s://%s%s",
                        uri.getScheme(),
                        uri.getHost(),
                        uri.getPort() == -1 ? "" : ":" + uri.getPort())
                .toLowerCase());
    }

}
