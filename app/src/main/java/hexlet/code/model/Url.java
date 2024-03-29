package hexlet.code.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

public class Url {
    Long  id;
    String name;
    Timestamp createdAt;
}
