package hexlet.code.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public final class MainPage extends BasePage{
    private Boolean visited;
//    private String currentUser;

    public Boolean isVisited() {
        return visited;
    }
}