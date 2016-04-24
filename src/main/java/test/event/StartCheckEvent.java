package test.event;

/**
 * Created by Жамбыл on 4/24/2016.
 */
public class StartCheckEvent {
    private final int timeOut;

    public StartCheckEvent(int timeOut) {
        this.timeOut = timeOut;
    }

    public int getTimeOut() {
        return timeOut;
    }
}
