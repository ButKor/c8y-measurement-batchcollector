package collector.runtime;

import collector.c8yapi.C8yHttpCallException;
import collector.c8yapi.C8yHttpClient;
import collector.recordset.ChunkResultSet;
import collector.recordset.MeasurementChunkDescription;
import collector.util.DateUtil;
import collector.util.TimeSpan;
import config.IRequestConfig;
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

    private final C8yHttpClient client;

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
        } catch (C8yHttpCallException e) {
            log.error("Critical error while collecting Chunks occurred. Error details: \n{}", e.prettify());
            return new ArrayList<>();
        }
        return lst;
    }


}
