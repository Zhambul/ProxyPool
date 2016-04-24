package test.event;

import jdk.nashorn.internal.codegen.CompilerConstants;

import java.util.function.Function;

/**
 * Created by Жамбыл on 4/23/2016.
 */
public class ExecuteRequestEvent {

    private final String url;
    private final boolean again;

    public ExecuteRequestEvent(String url) {
        this(url,false);
    }

    public ExecuteRequestEvent(String url, boolean again) {
        this.url = url;
        this.again = again;
    }

    public boolean isAgain() {
        return again;
    }

    public String getUrl() {
        return url;
    }

}
