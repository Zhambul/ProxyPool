package test.event;

/**
 * Created by Жамбыл on 4/23/2016.
 */
public class ExecuteRequestEvent {

    private final String url;

    public ExecuteRequestEvent(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }
}
