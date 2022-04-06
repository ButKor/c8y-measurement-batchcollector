package c8yapi;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.net.URI;

@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = false)
public class C8yHttpApiCallException extends Exception {

    private String message;
    private URI uri;
    private C8yHttpApiClient httpClient;
    private Exception nestedException;

    public C8yHttpApiCallException(String message, C8yHttpApiClient httpClient, Exception nestedException) {
        this.message = message;
        this.httpClient = httpClient;
        this.nestedException = nestedException;
    }

    public C8yHttpApiCallException(String message, C8yHttpApiClient httpClient) {
        this.message = message;
        this.httpClient = httpClient;
        this.nestedException = new RuntimeException(message);
    }

    public String createErrorDescription() {
        int colSize = 30;
        return String.format("%-" + colSize + "s: %s", "General", "Error while requesting data from Cumulocity") + System.lineSeparator() +
                String.format("%-" + colSize + "s: %s", "Exception Message", nestedException.getMessage()) + System.lineSeparator() +
                String.format("%-" + colSize + "s: %s", "Request URI", getUri()) + System.lineSeparator() +
                String.format("%-" + colSize + "s: %s", "HTTPClient baseURL", getHttpClient().getBaseUrl()) + System.lineSeparator() +
                String.format("%-" + colSize + "s: %s", "HTTPClient authentication", getHttpClient().getBasicAuthString()) + System.lineSeparator() +
                String.format("%-" + colSize + "s: %s", "Marshalled Exception message", nestedException.getMessage()) + System.lineSeparator() +
                String.format("%-" + colSize + "s: %s", "Marshalled Exception cause", nestedException.getCause()) + System.lineSeparator() +
                String.format("%-" + colSize + "s: %s", "Stacktrace", ExceptionUtils.getStackTrace(nestedException)) + System.lineSeparator();
    }


}