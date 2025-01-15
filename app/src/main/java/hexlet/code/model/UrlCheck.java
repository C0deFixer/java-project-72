package hexlet.code.model;

import kong.unirest.HttpResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

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

    public static UrlCheck parseHtmlBody(HttpResponse<String> response) {
        //Examples of tags
        //get title <title>Tproger — всё о программировании</title>
        //get desc <meta name="description" content="blabla">
        //get h1 <h1 class="logo-xxx" data-v-bdb999f8=""> bla bla </h1>
        Document doc = Jsoup.parse(response.getBody());

        Element h1Element = doc.selectFirst("h1");
        Element descriptionElement = doc.selectFirst("meta[name=description]");

        String body = response.getBody();
        return UrlCheck.builder()
                .statusCode(response.getStatus())
                .h1(h1Element == null ? "" : h1Element.text())
                .title(doc.title())
                .description(descriptionElement == null ? "" : descriptionElement.attr("content"))
                .build();
    }

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
