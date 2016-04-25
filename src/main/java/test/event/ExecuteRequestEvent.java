package test.event;

/**
 * Created by Жамбыл on 4/23/2016.
 */
public class ExecuteRequestEvent {

    private final String url;
    private final int timeOut;
    private final int requestId;

    public ExecuteRequestEvent(String url, int timeOut,int requestId) {
        this.url = url;
        this.timeOut = timeOut;
        this.requestId = requestId;
    }

    public int getRequestId() {
        return requestId;
    }

    public int getTimeOut() {
        return timeOut;
    }

    public String getUrl() {
        return url;
    }

}
