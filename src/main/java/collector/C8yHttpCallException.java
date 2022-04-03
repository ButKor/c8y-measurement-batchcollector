package collector;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.PrintStream;
import java.net.URI;

@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper=false)
public class C8yHttpCallException extends Exception {

    private String message;
    private URI uri;
    private C8yHttpClient httpClient;
    private Exception e;

    public void prettyPrint(PrintStream printStream) {
        printStream.println(prettify());
    }

    public String prettify() {
        StringBuilder sb = new StringBuilder();
        int colSize = 30;
        sb.append(String.format("%-" + colSize + "s: %s", "General", "Error while requesting data from Cumulocity")).append(System.lineSeparator());
        sb.append(String.format("%-" + colSize + "s: %s", "Exception Message", message)).append(System.lineSeparator());
        sb.append(String.format("%-" + colSize + "s: %s", "Request URI", uri)).append(System.lineSeparator());
        sb.append(String.format("%-" + colSize + "s: %s", "HTTPClient baseURL", httpClient.getBaseUrl())).append(System.lineSeparator());
        sb.append(String.format("%-" + colSize + "s: %s", "HTTPClient authentication", httpClient.getBase64Auth())).append(System.lineSeparator());
        sb.append(String.format("%-" + colSize + "s: %s", "Marshalled Exception message", e.getMessage())).append(System.lineSeparator());
        sb.append(String.format("%-" + colSize + "s: %s", "Marshalled Exception cause", e.getCause())).append(System.lineSeparator());
        sb.append(String.format("%-" + colSize + "s: %s", "Stacktrace", ExceptionUtils.getStackTrace(e))).append(System.lineSeparator());
        return sb.toString();
    }
}