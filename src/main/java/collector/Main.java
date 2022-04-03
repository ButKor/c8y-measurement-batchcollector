package collector;

import collector.recordset.ChunkResultSet;
import collector.recordset.ConsoleChunkResultDescriptor;
import config.Configuration;
import config.IPlatformConfig;
import config.IRequestConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;
import org.javatuples.Pair;

@Slf4j
public class Main {

    public static void main(String[] args) {
        new Main().run();
    }

    public void run() {
        log.info("Application started");

        IPlatformConfig platformConfig = Configuration.configurationProvider().bind("platform", IPlatformConfig.class);
        IRequestConfig requestConfig = Configuration.configurationProvider().bind("request", IRequestConfig.class);
        String outputFilePath = Configuration.configurationProvider().getProperty("outputFile", String.class);

        C8yHttpClient client = new C8yHttpClient(platformConfig);

        log.info("Build request-chunks ... ");
        Pair<ChunkResultSet, StopWatch> chunkResult = collectChunks(requestConfig, client);
        log.info("Request-chunks are built (elapsed time: {})", chunkResult.getValue1().formatTime());

        ConsoleChunkResultDescriptor.instance().print(chunkResult.getValue0(), true);

        log.info("Request measurements and export to file '{}'...", outputFilePath);
        Pair<Boolean, StopWatch> processResult = fetchAndExportMeasurementsToFile(client, chunkResult, outputFilePath);
        log.info("Exported measurements to file '{}' (elapsed time: {})", outputFilePath, processResult.getValue1().formatTime());
        log.info("Application finished");
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


}
