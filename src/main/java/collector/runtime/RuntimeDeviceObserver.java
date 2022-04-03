package collector.runtime;

import collector.c8yapi.C8yHttpClient;
import com.cumulocity.rest.representation.inventory.ManagedObjectRepresentation;
import lombok.Getter;
import org.apache.commons.lang3.time.StopWatch;
import org.javatuples.Pair;
import org.joda.time.DateTime;

@Getter
public class RuntimeDeviceObserver implements IRuntimeObserver {

    private final ManagedObjectRepresentation device;
    private final C8yHttpClient client;

    public RuntimeDeviceObserver(ManagedObjectRepresentation device, C8yHttpClient client) {
        this.device = device;
        this.client = client;
    }

    public void runtimeStarted() {
        client.sendEvent(device.getId().getValue(), "runtimeStateChanged", "Runtime started", new DateTime());
    }

    @Override
    public void fileWritten(Pair<Boolean, StopWatch> processResult, String outputFilePath) {
        if (processResult.getValue0()) {
            client.sendEvent(device.getId().getValue(), "fileExported",
                    String.format("Created file %s", outputFilePath), new DateTime());
        } else {
            client.sendAlarm(device.getId().getValue(), "fileExport",
                    String.format("File could not be written to %s", outputFilePath), "MAJOR", new DateTime());
        }
    }

    @Override
    public void runtimeFinished() {
        client.sendEvent(device.getId().getValue(), "runtimeStateChanged", "Runtime finished", new DateTime());
    }

}
