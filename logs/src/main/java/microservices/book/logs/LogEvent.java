package microservices.book.logs;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Arrays;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class LogEvent {
    private String source;
    private String host;
    private String path;
    private String type;
    private String[] tags;
    private String message;
    @JsonProperty("@timestamp")
    private String timeStamp;
    private String logger;
    private String level;
    private String thread;
    @Override
    public String toString() {
        return "{" +
                "source='" + source + '\'' +
                ", host='" + host + '\'' +
                ", path='" + path + '\'' +
                ", type='" + type + '\'' +
                ", tags=" + Arrays.toString(tags) +
                ", message='" + message + '\'' +
                ", timeStamp='" + timeStamp + '\'' +
                ", logger='" + logger + '\'' +
                ", level='" + level + '\'' +
                ", thread='" + thread + '\'' +
                '}';
    }
}
