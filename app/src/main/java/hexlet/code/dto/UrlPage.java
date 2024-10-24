package hexlet.code.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@AllArgsConstructor
@Getter
public class UrlPage extends BasePage {
    Long id;
    String name;
    LocalDateTime createdAt;

}
