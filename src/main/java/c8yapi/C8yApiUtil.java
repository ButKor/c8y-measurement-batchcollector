package c8yapi;

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


}
