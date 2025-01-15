package hexlet.code.dto;

import hexlet.code.model.UrlCheck;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@Getter
@Builder
public class UrlPage extends BasePage implements Cloneable {
    Long id;
    String name;
    LocalDateTime createdAt;
    List<UrlCheck> urlChecks;
}
