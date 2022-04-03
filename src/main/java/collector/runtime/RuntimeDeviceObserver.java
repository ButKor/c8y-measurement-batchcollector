package collector.runtime;

import collector.c8yapi.C8yHttpClient;
import com.cumulocity.rest.representation.alarm.AlarmRepresentation;
import com.cumulocity.rest.representation.event.EventRepresentation;
import com.cumulocity.rest.representation.inventory.ManagedObjectRepresentation;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;
import org.javatuples.Pair;
import org.joda.time.DateTime;

import java.util.Optional;

@Getter
@Slf4j
public class RuntimeDeviceObserver implements IRuntimeObserver {

    private final ManagedObjectRepresentation device;
    private final C8yHttpClient client;

    public RuntimeDeviceObserver(ManagedObjectRepresentation device, C8yHttpClient client) {
        this.device = device;
        this.client = client;
    }

    public void runtimeStarted() {
        Optional<EventRepresentation> event = client.sendEvent(device.getId().getValue(),
                "runtimeStateChanged", "Runtime started", new DateTime());
        log.info("Created Platform event to audit runtime start: {}", event.orElse(null));
    }

    @Override
    public void fileWritten(Pair<Boolean, StopWatch> processResult, String outputFilePath) {
        if (processResult.getValue0()) {
            Optional<EventRepresentation> event = client.sendEvent(device.getId().getValue(), "fileExported",
                    String.format("Created file %s", outputFilePath), new DateTime());
            log.info("Created Platform event to audit fileWrite-Action: {}", event.orElse(null));
        } else {
            Optional<AlarmRepresentation> alarm = client.sendAlarm(device.getId().getValue(), "fileExport",
                    String.format("File could not be written to %s", outputFilePath), "MAJOR", new DateTime());
            log.warn("Created Platform alarm to audit fileWrite-Action: {}", alarm.orElse(null));
        }
    }

    @Override
    public void runtimeFinished() {
        Optional<EventRepresentation> event = client.sendEvent(device.getId().getValue(),
                "runtimeStateChanged", "Runtime finished", new DateTime());
        log.info("Created Platform event to audit runtime start: {}", event.orElse(null));
    }

}
