package csvmeasurementcollector.runtime;

import c8yapi.C8yHttpApiCallException;
import c8yapi.C8yHttpApiClient;
import csvmeasurementcollector.recordset.ChunkResultSet;
import csvmeasurementcollector.recordset.MeasurementChunkDescription;
import util.DateUtil;
import lombok.extern.slf4j.Slf4j;
import me.tongfei.progressbar.DelegatingProgressBarConsumer;
import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarBuilder;
import me.tongfei.progressbar.ProgressBarStyle;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.javatuples.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.io.IOException;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
public class CsvMeasurementsToFileProcessors {

    final Logger logger = LoggerFactory.getLogger("test");
    private final C8yHttpApiClient client;

    public CsvMeasurementsToFileProcessors(C8yHttpApiClient client) {
        this.client = client;
    }

    public Pair<Boolean, StopWatch> runWithClock(ChunkResultSet resultSet, String fileName) {
        StopWatch watch = StopWatch.createStarted();
        Boolean success = run(resultSet, fileName);
        watch.stop();
        return new Pair<>(success, watch);
    }

    public boolean run(ChunkResultSet chunkResultSet, String fileName) {
        try (FileWriter fileWriter = new FileWriter(fileName)) {
            ProgressBar pb = createProgressBar(chunkResultSet.getRecords().size());
            List<MeasurementChunkDescription> chunks = chunkResultSet.getRecords();
            boolean isWrittenOnce = false;
            for (MeasurementChunkDescription c : chunks) {
                try {
                    pb.step();
                    if (c.getCountMeasurements() == null || c.getCountMeasurements() == 0) {
                        continue;
                    }

                    String dateFrom = DateUtil.getFormattedUtcString(c.getTimespan().getDateFrom());
                    String dateTo = DateUtil.getFormattedUtcString(c.getTimespan().getDateTo());
                    String deviceId = chunkResultSet.getRequestConfig().source();

                    String meas = client.fetchCsvMeasurements(dateFrom, dateTo, deviceId).orElse(StringUtils.EMPTY);
                    if (StringUtils.isEmpty(meas)) {
                        continue;
                    }

                    // remove header for every record except the first written record
                    if (isWrittenOnce) {
                        meas = meas.substring(meas.indexOf("\n") + 2);
                    }

                    fileWriter.append(meas).append(System.lineSeparator());
                    isWrittenOnce = true;
                } catch (C8yHttpApiCallException e) {
                    log.error("Error occurred while fetching CSV Measurements from Cumulocity " +
                            "- Runtime will jump over this chunk. Error details: " + System.lineSeparator() + "{}",
                            e.createErrorDescription());
                }
            }
            fileWriter.flush();
            return true;
        } catch (IOException e) {
            log.error("IO Exception while writing CSV. Error details:" + System.lineSeparator()
                            + "Error Message: {}" + System.lineSeparator()
                            + "Error Stacktrace: {}",
                    e.getMessage(),
                    ExceptionUtils.getStackTrace(e));
        }
        return false;
    }

    private ProgressBar createProgressBar(int maxSize) {
        return new ProgressBarBuilder()
                .setInitialMax(maxSize)
                .setStyle(ProgressBarStyle.ASCII)
                .setSpeedUnit(ChronoUnit.SECONDS)
                .setTaskName("Fetch and export Measurement chunks")
                .setConsumer(new DelegatingProgressBarConsumer(logger::info, 120))
                .setUpdateIntervalMillis(200)
                .build();
    }
}
