package csvmeasurementcollector.runtime;

import c8yapi.C8yHttpApiCallException;
import c8yapi.C8yHttpApiClient;
import c8yapi.builder.AlarmParameters;
import c8yapi.builder.EventParameters;
import com.cumulocity.rest.representation.alarm.AlarmRepresentation;
import com.cumulocity.rest.representation.event.EventRepresentation;
import com.cumulocity.rest.representation.inventory.ManagedObjectRepresentation;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;
import org.javatuples.Pair;
import org.joda.time.DateTime;

@Getter
@Slf4j
public class RuntimeDeviceObserver implements IRuntimeObserver {

    private final ManagedObjectRepresentation device;
    private final C8yHttpApiClient client;

    public RuntimeDeviceObserver(ManagedObjectRepresentation device, C8yHttpApiClient client) {
        this.device = device;
        this.client = client;
    }

    public void runtimeStarted() {
        try {
            EventRepresentation event = client.sendEvent(EventParameters.builder()
                    .deviceId(device.getId().getValue())
                    .type("runtimeStateChanged")
                    .text("Runtime started")
                    .time(new DateTime())
                    .build());
            log.info("Created Platform event to audit runtime start: {}", event);
        } catch (C8yHttpApiCallException e) {
            log.error("Error while creating platform event to indicate service start. Error details: \n{}", e.createErrorDescription());
        }
    }

    @Override
    public void fileWritten(Pair<Boolean, StopWatch> processResult, String outputFilePath) {
//        if (processResult.getValue0()) {
//            try {
//                EventRepresentation event = client.sendEvent(EventParameters.builder()
//                        .deviceId(device.getId().getValue())
//                        .type("fileExported")
//                        .text(String.format("Created file %s", outputFilePath))
//                        .time(new DateTime())
//                        .build());
//                log.info("Created Platform event to audit fileWrite-Action: {}", event);
//            } catch (C8yHttpApiCallException e) {
//                log.error("Error while creating platform event to indicate a file written. Error details: \n{}", e.createErrorDescription());
//            }
//        } else {
            try {
                AlarmRepresentation alarm = client.sendAlarm(AlarmParameters.builder()
                        .deviceId(device.getId().getValue())
                        .type("fileExport")
                        .text(String.format("File could not be written to %s", outputFilePath))
                        .severity("MAJOR")
                        .time(new DateTime())
                        .build());
                log.info("Created platform alarm to audit failing file write to '{}': {}", outputFilePath, alarm);
            } catch (C8yHttpApiCallException e) {
                log.error("Error while creating platform event to indicate a file written. Error details: \n{}", e.createErrorDescription());
            }
        //}
    }

    @Override
    public void runtimeFinished() {
        try {
            EventRepresentation event = client.sendEvent(EventParameters.builder()
                    .deviceId(device.getId().getValue())
                    .type("runtimeStateChanged")
                    .text("Runtime finished")
                    .time(new DateTime())
                    .build());
            log.info("Created Platform event to audit runtime start: {}", event);
        } catch (C8yHttpApiCallException e) {
            log.error("Error while creating platform event to indicate service finish. Error details: \n{}", e.createErrorDescription());
            e.printStackTrace();
        }
    }

}
