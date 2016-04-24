package test.event;

import jdk.nashorn.internal.codegen.CompilerConstants;

import java.util.function.Function;

/**
 * Created by Жамбыл on 4/23/2016.
 */
public class ExecuteRequestEvent {

    private final String url;
    private final boolean again;
    private final int timeOut;

    public ExecuteRequestEvent(String url, int timeOut) {
        this(url,timeOut,false);
    }

    public ExecuteRequestEvent(String url,int timeOut, boolean again) {
        this.url = url;
        this.timeOut = timeOut;
        this.again = again;
    }

    public int getTimeOut() {
        return timeOut;
    }

    public boolean isAgain() {
        return again;
    }

    public String getUrl() {
        return url;
    }

}
