package test.event;

/**
 * Created by Жамбыл on 4/24/2016.
 */
public class StartCheckEvent {
    private final int timeOut;
    private final String url;

    public StartCheckEvent(int timeOut, String url) {
        this.timeOut = timeOut;
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public int getTimeOut() {
        return timeOut;
    }
}
