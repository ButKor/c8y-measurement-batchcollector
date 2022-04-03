package collector.c8yapi;

import com.cumulocity.sdk.client.SDKException;
import org.apache.commons.lang3.exception.ExceptionUtils;

public class C8yApiUtil {

    public static String createErrorDescription(String note, SDKException e) {
        int colSize = 30;
        return String.format("%-" + colSize + "s: %s", "General", note) + System.lineSeparator() +
                String.format("%-" + colSize + "s: %s", "Exception Message", e.getMessage()) + System.lineSeparator() +
                String.format("%-" + colSize + "s: %s", "HTTP Status", e.getHttpStatus()) + System.lineSeparator() +
                String.format("%-" + colSize + "s: %s", "Cause", e.getCause()) + System.lineSeparator() +
                String.format("%-" + colSize + "s: %s", "Stacktrace", ExceptionUtils.getStackTrace(e)) + System.lineSeparator();
    }

    public static String createErrorDescription(C8yHttpCallException e) {
        int colSize = 30;
        return String.format("%-" + colSize + "s: %s", "General", "Error while requesting data from Cumulocity") + System.lineSeparator() +
                String.format("%-" + colSize + "s: %s", "Exception Message", e.getMessage()) + System.lineSeparator() +
                String.format("%-" + colSize + "s: %s", "Request URI", e.getUri()) + System.lineSeparator() +
                String.format("%-" + colSize + "s: %s", "HTTPClient baseURL", e.getHttpClient().getBaseUrl()) + System.lineSeparator() +
                String.format("%-" + colSize + "s: %s", "HTTPClient authentication", e.getHttpClient().getBasicAuthString()) + System.lineSeparator() +
                String.format("%-" + colSize + "s: %s", "Marshalled Exception message", e.getMessage()) + System.lineSeparator() +
                String.format("%-" + colSize + "s: %s", "Marshalled Exception cause", e.getCause()) + System.lineSeparator() +
                String.format("%-" + colSize + "s: %s", "Stacktrace", ExceptionUtils.getStackTrace(e)) + System.lineSeparator();
    }
}
