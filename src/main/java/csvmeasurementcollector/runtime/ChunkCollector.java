package csvmeasurementcollector.runtime;

import c8yapi.C8yHttpApiCallException;
import c8yapi.C8yHttpApiClient;
import csvmeasurementcollector.recordset.ChunkResultSet;
import csvmeasurementcollector.recordset.MeasurementChunkDescription;
import util.DateUtil;
import util.TimeSpan;
import csvmeasurementcollector.config.IRequestConfig;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;
import org.javatuples.Pair;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@Slf4j
public class ChunkCollector {

    private final C8yHttpApiClient client;

    public Pair<ChunkResultSet, StopWatch> collectWithClock(IRequestConfig requestConfig) {
        StopWatch watch = StopWatch.createStarted();
        ChunkResultSet set = collect(requestConfig);
        watch.stop();
        return new Pair<>(set, watch);
    }

    public ChunkResultSet collect(IRequestConfig requestConfig) {
        ChunkResultSet resultSet = new ChunkResultSet(requestConfig);
        resultSet.registerRunStart(Instant.now());
        TimeSpan ts = new TimeSpan(DateUtil.getDateTime(requestConfig.dateFrom()), DateUtil.getDateTime(requestConfig.dateTo()));
        resultSet.addMeasurementChunkDescriptions(
                collectChunks(ts, requestConfig.source(), requestConfig.chunkSize(), resultSet)
        );
        resultSet.registerRunFinish(Instant.now());
        return resultSet;
    }

    private List<MeasurementChunkDescription> collectChunks(TimeSpan ts, String sourceId, int maxChunkSize, ChunkResultSet resultSet) {
        List<MeasurementChunkDescription> lst = new ArrayList<>();
        try {
            int ctElements = client.fetchNumberOfMeasurements(DateUtil.getFormattedUtcString(ts.getDateFrom()), DateUtil.getFormattedUtcString(ts.getDateTo()), sourceId);

            MeasurementChunkDescription d = new MeasurementChunkDescription(ts, ctElements);
            if (ctElements > maxChunkSize) {
                Pair<TimeSpan, TimeSpan> timeTuple = DateUtil.divideTimesByTwo(ts);
                lst.addAll(collectChunks(new TimeSpan(timeTuple.getValue0().getDateFrom(), timeTuple.getValue0().getDateTo()), sourceId, maxChunkSize, resultSet));
                lst.addAll(collectChunks(new TimeSpan(timeTuple.getValue1().getDateFrom(), timeTuple.getValue1().getDateTo()), sourceId, maxChunkSize, resultSet));
                resultSet.registerDataSplitAction();
            } else {
                lst.add(d);
            }
        } catch (C8yHttpApiCallException e) {
            log.error("Critical error while collecting Chunks occurred. Error details: \n{}", e.createErrorDescription());
            return new ArrayList<>();
        }
        return lst;
    }


}
