package generic;

import com.cumulocity.model.ID;
import com.cumulocity.model.authentication.CumulocityBasicCredentials;
import com.cumulocity.model.event.Alarm;
import com.cumulocity.rest.representation.alarm.AlarmRepresentation;
import com.cumulocity.rest.representation.event.EventRepresentation;
import com.cumulocity.rest.representation.identity.ExternalIDRepresentation;
import com.cumulocity.rest.representation.inventory.ManagedObjectRepresentation;
import com.cumulocity.rest.representation.measurement.MeasurementRepresentation;
import com.cumulocity.sdk.client.Platform;
import com.cumulocity.sdk.client.PlatformBuilder;
import com.cumulocity.sdk.client.SDKException;
import com.google.common.base.Preconditions;
import config.IC8yDeviceConfig;
import config.IPlatformConfig;
import lombok.Getter;
import org.javatuples.Pair;
import org.joda.time.DateTime;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

@Getter
public class C8yServiceDevice {

    private final Platform platform;
    private ManagedObjectRepresentation device;

    public C8yServiceDevice(IPlatformConfig config) {
        this.platform = initPlatformInterface(config);
    }

    // TODO exception handling for invalid platform config (NPE/IndexOutOfBoundException)
    private Platform initPlatformInterface(IPlatformConfig config) {
        CumulocityBasicCredentials basicCredentials = CumulocityBasicCredentials.builder()
                .tenantId(config.user().split("/")[0])
                .username(config.user().split("/")[1])
                .password(config.pass())
                .build();
        return PlatformBuilder.platform()
                .withBaseUrl(config.baseUrl())
                .withCredentials(basicCredentials).
                build();
    }

    public void bootstrap(IC8yDeviceConfig serviceDeviceConfig) {
        bootstrap(serviceDeviceConfig.externalIdKey(),
                serviceDeviceConfig.externalIdValue(),
                serviceDeviceConfig.type(),
                serviceDeviceConfig.name(), true);
    }

    public ManagedObjectRepresentation bootstrap(String externalIdKey, String externalId, String type, String name, boolean isDevice) {
        try {
            ExternalIDRepresentation xid = platform.getIdentityApi().getExternalId(new ID(externalIdKey, externalId));
            ManagedObjectRepresentation deviceObject = xid.getManagedObject();
            this.device = deviceObject;
        } catch (SDKException e) {
            if (e.getHttpStatus() == 404) {
                ManagedObjectRepresentation mr = new ManagedObjectRepresentation();
                mr.setType(type);
                if (isDevice) {
                    mr.setProperty("c8y_IsDevice", new Object());
                }
                mr.setName(name);
                ManagedObjectRepresentation mo = platform.getInventoryApi().create(mr);

                ExternalIDRepresentation xid = new ExternalIDRepresentation();
                xid.setType(externalIdKey);
                xid.setExternalId(externalId);
                xid.setManagedObject(mo);
                platform.getIdentityApi().create(xid);

                this.device = mo;
            }
        }
        return this.device;
    }

    public EventRepresentation sendEvent(String type, String text, Optional<DateTime> time, Pair<String, Object>... fragments) {
        Preconditions.checkNotNull(device, "Device not allowed being null. Is the device bootstrapped?");
        EventRepresentation er = new EventRepresentation();
        er.setType(type);
        er.setText(text);
        er.setDateTime(time != null && time.isPresent() ? time.get() : new DateTime());
        if (fragments != null && fragments.length > 0) {
            Arrays.stream(fragments).sequential().forEach(p -> er.setProperty(p.getValue0(), p.getValue1()));
        }
        er.setSource(this.device);
        EventRepresentation event = this.platform.getEventApi().create(er);
        return event;
    }

    public EventRepresentation sendAlarm(String type, String text, String severity, Optional<DateTime> time, Pair<String, Object>... fragments) {
        Preconditions.checkNotNull(device, "Device not allowed being null. Is the device bootstrapped?");

        AlarmRepresentation ar = new AlarmRepresentation();
        ar.setType(type);
        ar.setText(text);
        ar.setDateTime(time != null && time.isPresent() ? time.get() : new DateTime());
        if (fragments != null && fragments.length > 0) {
            Arrays.stream(fragments).sequential().forEach(p -> ar.setProperty(p.getValue0(), p.getValue1()));
        }
        ar.setSource(this.device);
        ar.setSeverity(severity);
        EventRepresentation event = this.platform.getAlarmApi().create(ar);
        return event;
    }



    // public void sendMeasurements(String type, Optional<DateTime> signalTime, Map<String, Collection<String>> fragments) {
    //     MeasurementRepresentation mr = new MeasurementRepresentation();
    //     if (signalTime != null && signalTime.isPresent()) {
    //         mr.setDateTime(signalTime.get());
    //     }
    //     mr.setType(type);
    //     mr.setSource(this.device);
    //
    //     this.platform.getMeasurementApi().create(mr);
    // }

}
