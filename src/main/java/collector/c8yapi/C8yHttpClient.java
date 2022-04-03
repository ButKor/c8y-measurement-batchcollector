package collector.c8yapi;


import com.cumulocity.model.ID;
import com.cumulocity.model.authentication.CumulocityBasicCredentials;
import com.cumulocity.model.idtype.GId;
import com.cumulocity.rest.representation.alarm.AlarmRepresentation;
import com.cumulocity.rest.representation.event.EventRepresentation;
import com.cumulocity.rest.representation.identity.ExternalIDRepresentation;
import com.cumulocity.rest.representation.inventory.ManagedObjectRepresentation;
import com.cumulocity.sdk.client.Platform;
import com.cumulocity.sdk.client.PlatformBuilder;
import com.cumulocity.sdk.client.SDKException;
import com.google.common.base.Preconditions;
import config.IPlatformConfig;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.javatuples.Pair;
import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.Base64;
import java.util.Optional;

@Slf4j
@Data
public class C8yHttpClient {

    private final String baseUrl;
    private Platform platform;
    private CumulocityBasicCredentials credentials;

    public C8yHttpClient(IPlatformConfig config) {
        this.baseUrl = config.baseUrl();
        this.initPlatformInterface(config);
    }

    // TODO: add validation and check for Exceptions (NPE/IndexOutOfBound)
    private void initPlatformInterface(IPlatformConfig config) {
        CumulocityBasicCredentials basicCredentials = CumulocityBasicCredentials.builder()
                .tenantId(config.user().split("/")[0])
                .username(config.user().split("/")[1])
                .password(config.pass())
                .build();
        this.credentials = basicCredentials;
        this.platform = PlatformBuilder.platform()
                .withBaseUrl(config.baseUrl())
                .withCredentials(basicCredentials).
                build();
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
                    .header("Authorization", getBasicAuthString())
                    .header("Accept", "text/csv")
                    .method("GET", HttpRequest.BodyPublishers.noBody())
                    .build();
            HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            response = validatePlatformResponse(uri, response);
            return Optional.of(response.body());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }


    public int fetchNumberOfMeasurements(String dateFrom, String dateTo, String sourceId) throws C8yHttpCallException {
        URI uri = URI.create(String.format("%s/measurement/measurements?pageSize=1&withTotalPages=true%s%s%s",
                baseUrl,
                StringUtils.isNoneEmpty(dateFrom) ? "&dateFrom=" + dateFrom : "",
                StringUtils.isNoneEmpty(dateTo) ? "&dateTo=" + dateTo : "",
                StringUtils.isNoneEmpty(sourceId) ? "&source=" + sourceId : ""));

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(uri)
                    .header("Authorization", getBasicAuthString())
                    .method("GET", HttpRequest.BodyPublishers.noBody())
                    .build();
            HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            response = validatePlatformResponse(uri, response);

            return getTotalNumberOfPages(response.body());
        } catch (JSONException e) {
            throw new C8yHttpCallException("Error while marshalling platform response to JSON", uri, this, e);
        } catch (NullPointerException e) {
            throw new C8yHttpCallException("Null Pointer Exception while requesting and parsing JSON Response", uri, this, e);
        } catch (IOException | InterruptedException e) {
            throw new C8yHttpCallException("IO-/Interrupted Exception while requesting data", uri, this, e);
        }
    }

    @SafeVarargs
    public final Optional<EventRepresentation> sendEvent(String deviceId, String type, String text, DateTime time, Pair<String, Object>... fragments) {
        try {
            Preconditions.checkArgument(StringUtils.isNoneEmpty(deviceId), "Device ID not allowed being null or empty");
            Preconditions.checkArgument(StringUtils.isNoneEmpty(type), "Type not allowed being null or empty");
            Preconditions.checkArgument(StringUtils.isNoneEmpty(text), "Text not allowed being null or empty");

            EventRepresentation event = new EventRepresentation();
            event.setType(type);
            event.setText(text);
            event.setDateTime(time != null ? time : new DateTime());
            if (fragments != null && fragments.length > 0) {
                Arrays.stream(fragments).sequential().forEach(p -> event.setProperty(p.getValue0(), p.getValue1()));
            }

            ManagedObjectRepresentation mo = new ManagedObjectRepresentation();
            mo.setId(GId.asGId(deviceId));
            event.setSource(mo);

            EventRepresentation er = platform.getEventApi().create(event);
            log.debug("Created Event: {}", er);
            return Optional.of(er);
        } catch (IllegalArgumentException e) {
            log.error("Illegal Argument passed to send a platform event. Details: {}", e.getMessage());
        } catch (SDKException e) {
            log.error("Error while sending an Event to Cumulocity. Details: " + System.lineSeparator() + "{}",
                    C8yApiUtil.createErrorDescription("Error while sending an Alarm to Cumulocity", e));
        }
        log.debug("Send Event ran in in unexpected state, returning empty Optional");
        return Optional.empty();
    }

    @SafeVarargs
    public final Optional<AlarmRepresentation> sendAlarm(String deviceId, String type, String text, String severity, DateTime time, Pair<String, Object>... fragments) {
        try {
            Preconditions.checkArgument(StringUtils.isNoneEmpty(deviceId), "Device ID not allowed being null or empty");
            Preconditions.checkArgument(StringUtils.isNoneEmpty(type), "Type not allowed being null or empty");
            Preconditions.checkArgument(StringUtils.isNoneEmpty(text), "Text not allowed being null or empty");
            Preconditions.checkArgument(StringUtils.isNoneEmpty(severity), "Severity not allowed being null or empty");

            AlarmRepresentation alarm = new AlarmRepresentation();
            alarm.setType(type);
            alarm.setText(text);
            alarm.setDateTime(time != null ? time : new DateTime());
            if (fragments != null && fragments.length > 0) {
                Arrays.stream(fragments).sequential().forEach(p -> alarm.setProperty(p.getValue0(), p.getValue1()));
            }
            alarm.setSeverity(severity);

            ManagedObjectRepresentation mo = new ManagedObjectRepresentation();
            mo.setId(GId.asGId(deviceId));
            alarm.setSource(mo);

            AlarmRepresentation ar = platform.getAlarmApi().create(alarm);
            log.debug("Created Alarm: {}", ar);
            return Optional.of(ar);
        } catch (IllegalArgumentException e) {
            log.error("Illegal Argument passed to send a platform event. Details: {}", e.getMessage());
        } catch (SDKException e) {
            log.error("Error while sending an Alarm to Cumulocity. Details: " + System.lineSeparator() + "{}",
                    C8yApiUtil.createErrorDescription("Error while sending an Alarm to Cumulocity", e));
        }
        log.debug("Send Alarm ran in in unexpected state, returning empty Optional");
        return Optional.empty();
    }

    /**
     * Method to throw an C8yHttpCallException if platform response is invalid. Returning the input response otherwise.
     */
    private HttpResponse<String> validatePlatformResponse(URI uri, HttpResponse<String> response) throws C8yHttpCallException {
        if (response == null) {
            throw new C8yHttpCallException("Response is null", uri, this, new NullPointerException());
        }
        if (response.statusCode() != 200) {
            throw new C8yHttpCallException("Received invalid status code '" + response.statusCode() + "'", uri, this, new IllegalStateException("Response status code expected to be '" + 200 + "'"));
        }
        if (response.body() == null) {
            throw new C8yHttpCallException("Response body is null", uri, this, new IllegalStateException("Response body expected to be non-null"));
        }
        return response;
    }

    public Optional<ManagedObjectRepresentation> bootstrap(String externalIdKey, String externalIdValue, String type, String name) {
        try {
            Preconditions.checkArgument(StringUtils.isNoneEmpty(externalIdKey), "ExternalIdKey not allowed being null or empty");
            Preconditions.checkArgument(StringUtils.isNoneEmpty(externalIdValue), "ExternalIdValue not allowed being null or empty");
            Preconditions.checkArgument(StringUtils.isNoneEmpty(type), "Type not allowed being null or empty");
            Preconditions.checkArgument(StringUtils.isNoneEmpty(name), "Name not allowed being null or empty");

            ExternalIDRepresentation xid = platform.getIdentityApi().getExternalId(new ID(externalIdKey, externalIdValue));
            ManagedObjectRepresentation mo = xid.getManagedObject();
            log.debug("Found Managed Object for externalIdKey = {} and externalIdValue = {}", externalIdKey, externalIdValue);
            return Optional.of(mo);
        } catch (SDKException e) {
            if (e.getHttpStatus() == 404) {
                log.debug("Did not find Managed Object for externalIdKey = {} and externalIdValue = {} => Creating it.", externalIdKey, externalIdValue);

                Optional<ManagedObjectRepresentation> device = getManagedObjectRepresentation(type, name, true);
                if(device.isEmpty()){
                    // TODO: issue, take care
                }
                Optional<ExternalIDRepresentation> xid = registerExternalId(externalIdKey, externalIdValue, device.get());
                if(xid.isEmpty()){
                    // TODO: issue, take care
                }

                return Optional.of(device.get());
            }
            // TODO: other issues, take care
        } catch (IllegalArgumentException e) {
            log.error("Illegal Argument passed to send a platform event. Details: {}", e.getMessage());
        }
        log.debug("Device Bootstrapping run in an unexpected issue, returning an empty Optional...");
        return Optional.empty();
    }

    private Optional<ManagedObjectRepresentation> getManagedObjectRepresentation(String type, String name, boolean isDevice) {
        try {
            ManagedObjectRepresentation device = new ManagedObjectRepresentation();
            device.setType(type);
            if (isDevice) {
                device.setProperty("c8y_IsDevice", new Object());
            }
            device.setName(name);
            device = platform.getInventoryApi().create(device);
            log.debug("Created platform device: {}", device);
            return Optional.of(device);
        } catch (IllegalArgumentException e) {
            log.error("Illegal Argument passed to create a Managed Object. Details: {}", e.getMessage());
        } catch (SDKException e) {
            log.error("Error while creating a Managed Object. Details: " + System.lineSeparator() + "{}",
                    C8yApiUtil.createErrorDescription("Error while creating platform device in Cumulocity", e));
        }
        log.debug("Create Managed Object in an unexpected issue, returning an empty Optional...");
        return Optional.empty();
    }

    private Optional<ExternalIDRepresentation> registerExternalId(String externalIdKey, String externalIdValue, ManagedObjectRepresentation device) {
        try {
            Preconditions.checkArgument(StringUtils.isNoneEmpty(externalIdKey), "ExternalIDKey not allowed being null or empty");
            Preconditions.checkArgument(StringUtils.isNoneEmpty(externalIdValue), "ExternalIdValue not allowed being null or empty");
            Preconditions.checkArgument(device != null, "Device not allowed being null");

            ExternalIDRepresentation xid = new ExternalIDRepresentation();
            xid.setType(externalIdKey);
            xid.setExternalId(externalIdValue);
            xid.setManagedObject(device);
            ExternalIDRepresentation res = platform.getIdentityApi().create(xid);
            log.debug("Created externalID for device {}: {}", device.getId().getValue(), res);
            return Optional.of(res);
        } catch (IllegalArgumentException e) {
            log.error("Illegal Argument passed to register external id to platform. Details: {}", e.getMessage());
        } catch (SDKException e) {
            log.error("Error while registering externalID to Cumulocity. Details: " + System.lineSeparator() + "{}",
                    C8yApiUtil.createErrorDescription("Error while registering externalID to Cumulocity", e));
        }
        log.debug("Register external id ran in in unexpected state, returning empty Optional");
        return Optional.empty();
    }

    private String basicAuth(String username, String password) {
        return "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
    }

    public String getBasicAuthString() {
        return basicAuth(credentials.getUsername(), credentials.getPassword());
    }

    private Integer getTotalNumberOfPages(String responseBody) throws JSONException, NullPointerException {
        Preconditions.checkNotNull(responseBody, "Response Body not allowed being null");
        if (responseBody.length() == 0) {
            return 0;
        }

        JSONObject obj = new JSONObject(responseBody);
        JSONObject statistics = obj.getJSONObject("statistics");
        Preconditions.checkNotNull(statistics, "Statistics fragment could not be found for response: " + responseBody);

        Number number = statistics.getNumber("totalPages");
        Preconditions.checkNotNull(number, "fragment 'totalPages' could not be found in statistics for response: " + responseBody);

        return number.intValue();
    }


}

