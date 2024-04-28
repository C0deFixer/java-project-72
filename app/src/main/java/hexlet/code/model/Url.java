package hexlet.code.model;

import io.micrometer.core.instrument.distribution.StepBucketHistogram;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@AllArgsConstructor
@Getter
@Setter
public class Url {
    Long  id;
    String name;
    String protocol;
    String authority;

    LocalDateTime createdAt;

    public Url(String name, String protocol, String authority) {
        this.name = name;
        this.protocol = protocol;
        this.authority = authority;

    }
}
