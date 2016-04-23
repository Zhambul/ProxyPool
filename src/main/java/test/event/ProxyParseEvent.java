package test.event;

/**
 * Created by Жамбыл on 4/23/2016.
 */
public class ProxyParseEvent {
    private final String filePath;

    public ProxyParseEvent(String filePath) {
        this.filePath = filePath;
    }

    public String getFilePath() {
        return filePath;
    }
}
