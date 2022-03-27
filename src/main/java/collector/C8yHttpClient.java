package collector;


import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;
import java.util.Optional;
import org.json.*;

public class C8yHttpClient {

    private final String baseUrl;
    private final String base64Auth;

    public C8yHttpClient(String baseUrl, String user, String pass) {
        this.baseUrl = baseUrl;
        this.base64Auth = basicAuth(user, pass);
    }


    public Optional<Integer> fetchNumberOfMeasurements(String dateFrom, String dateTo, String oid) {
        URI uri = URI.create(String.format("%s/measurement/measurements?pageSize=1&withTotalPages=true%s%s%s",
                baseUrl,
                StringUtils.isNoneEmpty(dateFrom) ? "&dateFrom=" + dateFrom : "",
                StringUtils.isNoneEmpty(dateTo) ? "&dateTo=" + dateTo : "",
                StringUtils.isNoneEmpty(oid) ? "&source=" + oid : ""));

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(uri)
                    .header("Authorization", base64Auth)
                    .method("GET", HttpRequest.BodyPublishers.noBody())
                    .build();
            HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

            if(response == null || response.statusCode() != 200 || StringUtils.isEmpty(response.body())){
                return Optional.empty();
            }

            Integer totalPages = getTotalNumberOfPages(response.body());
            return Optional.of(totalPages);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    private Integer getTotalNumberOfPages(String responseBody) {
        JSONObject obj = new JSONObject(responseBody);
        Number number = obj.getJSONObject("statistics").getNumber("totalPages");
        int res = number.intValue();
        return res;
    }

    private String basicAuth(String username, String password) {
        return "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
    }

}
