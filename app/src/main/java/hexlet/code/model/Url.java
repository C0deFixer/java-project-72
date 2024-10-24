package hexlet.code.model;

import lombok.*;

import java.net.URL;
import java.time.LocalDateTime;

@AllArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class Url {
    Long id;
    String protocol;
    String host;
    int port;

    LocalDateTime createdAt;

    public Url(String protocol, String host) {
        this.protocol = protocol;
        this.host = host;

    }

    public String getName() {
        return protocol + host + port;
    }

    public static Url valueOf(URL url) {
        return Url.builder()
                .protocol(url.getProtocol())
                .host(url.getHost())
                .port(url.getPort())
                .build();
    }

}
