package collector.runtime;

import collector.recordset.ChunkResultSet;
import config.IPlatformConfig;
import generic.C8yServiceDevice;
import lombok.Getter;
import org.apache.commons.lang3.time.StopWatch;
import org.javatuples.Pair;

import java.util.Optional;

@Getter
public class RuntimeDeviceLogObserver implements IRuntimeObserver{

    private C8yServiceDevice device;

    public RuntimeDeviceLogObserver(C8yServiceDevice device){
        this.device = device;
    }

    public void runtimeStarted(){
        device.sendEvent("runtimeStateChanged", "Runtime started", Optional.empty());
    }

    @Override
    public void fileWritten(Pair<Boolean, StopWatch> processResult, String outputFilePath) {
        if(processResult.getValue0()){
            device.sendEvent("fileExported", String.format("Created file %s", outputFilePath), Optional.empty());
        } else {
            device.sendAlarm("fileExport", String.format("File could not be written to %s", outputFilePath), "MAJOR", Optional.empty());
        }
    }

    @Override
    public void runtimeFinished() {
        device.sendEvent("runtimeStateChanged", "Runtime finished", Optional.empty());
    }

}
