package collector.runtime;

import collector.C8yHttpClient;
import collector.ChunkCollector;
import collector.CsvMeasurementsToFileProcessors;
import collector.recordset.ChunkResultSet;
import collector.recordset.ConsoleChunkResultDescriptor;
import config.Configuration;
import config.IC8yDeviceConfig;
import config.IPlatformConfig;
import config.IRequestConfig;
import generic.C8yServiceDevice;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;
import org.javatuples.Pair;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class Controller implements IRuntimeObservable {

    private final List<IRuntimeObserver> observers = new ArrayList<>();

    public void run() {
        log.info("Runtime started...");

        // config located in src/main/resources (should be on default, but make sure its part of your classpath)
        IPlatformConfig platformConfig = Configuration.configurationProvider().bind("platform", IPlatformConfig.class);
        IC8yDeviceConfig serviceDeviceConfig = Configuration.configurationProvider().bind("serviceC8yDevice", IC8yDeviceConfig.class);
        IRequestConfig requestConfig = Configuration.configurationProvider().bind("request", IRequestConfig.class);
        String outputFilePath = Configuration.configurationProvider().getProperty("outputFile", String.class);

        // Optional: Create and register a Cumulocity device as runtime observer
        if(serviceDeviceConfig.activate()){
            C8yServiceDevice device = registerC8yDeviceObserver(platformConfig, serviceDeviceConfig);
            registerObserver(new RuntimeDeviceLogObserver(device));
        }
        observers.forEach(IRuntimeObserver::runtimeStarted);

        C8yHttpClient client = new C8yHttpClient(platformConfig);

        log.info("Build request-chunks ... ");
        Pair<ChunkResultSet, StopWatch> chunkResult = collectChunks(requestConfig, client);
        log.info("Request-chunks are built (elapsed time: {})", chunkResult.getValue1().formatTime());

        ConsoleChunkResultDescriptor.instance().print(chunkResult.getValue0(), true);

        log.info("Request measurements and export to file '{}'...", outputFilePath);
        Pair<Boolean, StopWatch> processResult = fetchAndExportMeasurementsToFile(client, chunkResult, outputFilePath);
        log.info("Exported measurements to file '{}' (elapsed time: {})", outputFilePath, processResult.getValue1().formatTime());
        observers.forEach(o -> o.fileWritten(processResult, outputFilePath));

        observers.forEach(IRuntimeObserver::runtimeFinished);
        log.info("Runtime finished");
    }

    private C8yServiceDevice registerC8yDeviceObserver(IPlatformConfig platformConfig, IC8yDeviceConfig serviceDeviceConfig) {
        C8yServiceDevice device = new C8yServiceDevice(platformConfig);
        device.bootstrap(serviceDeviceConfig);
        return device;
    }


    /*
     *   Spin up a binary tree for all records:
     *       1) Request the total amount of measurement records between time A and B
     *       2) Divide the time span (always by two) until the amount of measurementRecords is <= max allowed chunk size
     *   => Result will be a list of time spans with no entry having more than the allowed number of measurements
     *
     *   Note that only the number of measurements between certain timestamps requested, not the measurements themselves.
     *
     * */
    private Pair<ChunkResultSet, StopWatch> collectChunks(IRequestConfig requestConfig, C8yHttpClient client) {
        return new ChunkCollector(client).collectWithClock(requestConfig);
    }


    /*
     *   Run over all chunks, collect their (CSV-) Measurements and dump to File
     *   The chunks should be:
     *        a) already sorted ascending by time and
     *        b) have no time overlaps (thus should not have duplicates)
     * */
    private Pair<Boolean, StopWatch> fetchAndExportMeasurementsToFile(C8yHttpClient client, Pair<ChunkResultSet, StopWatch> chunkResult, String filePath) {
        return new CsvMeasurementsToFileProcessors(client)
                .runWithClock(chunkResult.getValue0(), filePath);
    }

    @Override
    public void registerObserver(IRuntimeObserver o){
        this.observers.add(o);
    }

    @Override
    public void unregisterObserver(IRuntimeObserver o){
        observers.remove(o);
    }

}