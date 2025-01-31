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
    int lastCheckStatusCode;
    LocalDateTime lastCheckCreatedAt;

    public Url() {
        //for jackson parsing to POJO from json test file
    }

    public Url(String protocol, String host) {
        this.protocol = protocol;
        this.host = host;
    }

    /**
     * Use for represent URL as String in jte.
     */
    @Override
    public String toString() {
        return String.format("%s://%s:%s", protocol, host, port ==-1 ? "": port);
    }

    /**
     * Use for request URL as String in Unirest.
     *
     * @return A formatted string
     */

    public String toUrlString() {
        return String.format("%s://%s:%s", protocol, host, port == -1 ? "" : String.valueOf(port));
    }

    public static Url valueOf(URL url) {
        return Url.builder()
                .protocol(url.getProtocol())
                .host(url.getHost())
                .port(url.getPort())
                .build();
    }

}
