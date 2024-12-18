package hexlet.code.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.Builder;

import java.net.URL;
import java.time.LocalDateTime;

@AllArgsConstructor
@Getter
@Setter
@Builder
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

    /**
     * Use for represent URL as String in jte.
     */
    @Override
    public String toString() {
        return String.format("%s://%s", protocol, host);
    }

    public static Url valueOf(URL url) {
        return Url.builder()
                .protocol(url.getProtocol())
                .host(url.getHost())
                .port(url.getPort())
                .build();
    }

}
