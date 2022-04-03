package collector;


import com.google.common.base.Preconditions;
import config.IPlatformConfig;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;
import java.util.Optional;

import org.json.*;

@Data
public class C8yHttpClient {

    private final String baseUrl;
    private final String base64Auth;

    public C8yHttpClient(IPlatformConfig config) {
        this.baseUrl = config.baseUrl();
        this.base64Auth = basicAuth(config.user(), config.pass());
    }

    public Optional<String> fetchCsvMeasurements(String dateFrom, String dateTo, String sourceId) throws C8yHttpCallException {
        URI uri = URI.create(String.format("%s/measurement/measurements?pageSize=1&withTotalPages=true%s%s%s",
                baseUrl,
                StringUtils.isNoneEmpty(dateFrom) ? "&dateFrom=" + dateFrom : "",
                StringUtils.isNoneEmpty(dateTo) ? "&dateTo=" + dateTo : "",
                StringUtils.isNoneEmpty(sourceId) ? "&source=" + sourceId : ""));

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(uri)
                    .header("Authorization", base64Auth)
                    .header("Accept", "text/csv")
                    .method("GET", HttpRequest.BodyPublishers.noBody())
                    .build();
            HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            response = validatePlatformResponse(uri, response, 200);
            return Optional.of(response.body());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }


    public int fetchNumberOfMeasurements(String dateFrom, String dateTo, String sourceId) throws C8yHttpCallException{
        URI uri = URI.create(String.format("%s/measurement/measurements?pageSize=1&withTotalPages=true%s%s%s",
                baseUrl,
                StringUtils.isNoneEmpty(dateFrom) ? "&dateFrom=" + dateFrom : "",
                StringUtils.isNoneEmpty(dateTo) ? "&dateTo=" + dateTo : "",
                StringUtils.isNoneEmpty(sourceId) ? "&source=" + sourceId : ""));

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(uri)
                    .header("Authorization", base64Auth)
                    .method("GET", HttpRequest.BodyPublishers.noBody())
                    .build();
            HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            response = validatePlatformResponse(uri, response, 200);

            Integer totalPages = getTotalNumberOfPages(response.body());
            return totalPages.intValue();
        } catch (JSONException e){
            throw new C8yHttpCallException("Error while marshalling platform response to JSON", uri, this, e);
        } catch(NullPointerException e){
            throw new C8yHttpCallException("Null Pointer Exception while requesting and parsing JSON Response", uri, this, e);
        } catch (IOException | InterruptedException e) {
            throw new C8yHttpCallException("IO-/Interrupted Exception while requesting data", uri, this, e);
        }
    }

    private HttpResponse<String> validatePlatformResponse(URI uri, HttpResponse<String> response, int expectedStatusCode) throws C8yHttpCallException {
        if(response == null){
            throw new C8yHttpCallException("Response is null", uri, this, new NullPointerException());
        }
        if(response.statusCode() != expectedStatusCode){
            throw new  C8yHttpCallException("Received invalid status code '" + response.statusCode() + "'", uri, this, new IllegalStateException("Response status code expected to be '" + expectedStatusCode + "'"));
        }
        if(response.body() == null){
            throw new  C8yHttpCallException("Response body is null", uri, this, new IllegalStateException("Response body expected to be non-null"));
        }
        return response;
    }

    private Integer getTotalNumberOfPages(String responseBody) throws JSONException, NullPointerException {
        Preconditions.checkNotNull(responseBody, "Response Body not allowed being null");
        if(responseBody.length() == 0){
            return 0;
        }

        JSONObject obj = new JSONObject(responseBody);
        JSONObject statistics = obj.getJSONObject("statistics");
        Preconditions.checkNotNull(statistics,"Statistics fragment could not be found for response: " + responseBody);

        Number number = statistics.getNumber("totalPages");
        Preconditions.checkNotNull(number, "fragment 'totalPages' could not be found in statistics for response: " + responseBody);

        return number.intValue();
    }

    private String basicAuth(String username, String password) {
        return "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
    }

}

