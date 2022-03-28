package collector;

import collector.recordset.ChunkResultSet;
import collector.recordset.MeasurementChunkDescription;
import collector.util.DateUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.javatuples.Pair;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class CsvMeasurementsToFileProcessors {

    private final C8yHttpClient client;

    public CsvMeasurementsToFileProcessors(C8yHttpClient client) {
        this.client = client;
    }

    public Pair<Boolean, StopWatch> runWithClock(ChunkResultSet resultSet, String fileName){
        StopWatch watch = StopWatch.createStarted();
        Boolean success = run(resultSet, fileName);
        watch.stop();
        return new Pair<>(success, watch);
    }

    public boolean run(ChunkResultSet chunkResultSet, String fileName) {
        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(fileName);
            List<MeasurementChunkDescription> chunks = chunkResultSet.getRecords();
            boolean isWrittenOnce = false;
            for (MeasurementChunkDescription c : chunks) {
                if (c.getCountMeasurements() == null || c.getCountMeasurements() == 0) {
                    continue;
                }

                String dateFrom = DateUtil.getFormattedUtcString(c.getTimespan().getDateFrom());
                String dateTo = DateUtil.getFormattedUtcString(c.getTimespan().getDateTo());
                String deviceId = chunkResultSet.getCollectorCfg().getOid();

                Optional<String> measurementBlock = client.fetchCsvMeasurements(dateFrom, dateTo, deviceId);
                String meas = measurementBlock.orElse(StringUtils.EMPTY);
                if (StringUtils.isEmpty(meas)) {
                    continue;
                }

                // remove header for every set except the first written set
                if (isWrittenOnce) {
                    meas = meas.substring(meas.indexOf("\n") + 2);
                }

                fileWriter.append(meas).append(System.lineSeparator());
                isWrittenOnce = true;
            }
            fileWriter.flush();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
        }
        return false;
    }
}
