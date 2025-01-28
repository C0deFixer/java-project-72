package hexlet.code.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@AllArgsConstructor
@Getter
@Setter
@Builder
@ToString(onlyExplicitlyIncluded = true)
public class UrlCheck {
    @ToString.Include
    Long id;
    @ToString.Include
    Long urlId;
    @ToString.Include
    int statusCode;
    @ToString.Include
    String h1;
    @ToString.Include
    String title;
    String description;
    @ToString.Include
    LocalDateTime createdAt;

    private static final String H1REG = "<h1\\s*.*>\\s*.*<\\/h1>";
    private static final String DESCRIPTION_REG = "<meta\\s*.*name\\s*.*>";
    private static final String TITLE_REG = "<title\\s*.*>\\s*.*<\\/title>";


    private static String getHtmlElemenContent(String body) {



/*        String[] element = body.split(regSearch);
        if (element.length == 1) {
            return element[0].substring(element[0].indexOf("<") + 1, element[0].lastIndexOf("</") - 1);

        } else {
            return "";
        }*/
        return "";
    }
}
