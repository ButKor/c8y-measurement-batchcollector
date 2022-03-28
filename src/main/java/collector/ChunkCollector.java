package collector;

import collector.recordset.ChunkResultSet;
import collector.recordset.MeasurementChunkDescription;
import collector.util.DateUtil;
import collector.util.TimeSpan;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.time.StopWatch;
import org.javatuples.Pair;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@AllArgsConstructor
public class ChunkCollector {

    private final C8yHttpClient client;

    public Pair<ChunkResultSet, StopWatch> collectWithClock(ChunkCollectorConfig collectorCfg) {
        StopWatch watch = StopWatch.createStarted();
        ChunkResultSet set = collect(collectorCfg);
        watch.stop();
        return new Pair<>(set, watch);
    }

    public ChunkResultSet collect(ChunkCollectorConfig collectorCfg) {
        ChunkResultSet resultSet = new ChunkResultSet(collectorCfg);
        resultSet.registerRunStart(Instant.now());
        resultSet.addMeasurementChunkDescriptions(
                collectChunks(collectorCfg.getDateFrom(), collectorCfg.getDateTo(), collectorCfg.getOid(), collectorCfg.getChunkSize(), resultSet)
        );
        resultSet.registerRunFinish(Instant.now());
        return resultSet;
    }

    private List<MeasurementChunkDescription> collectChunks(Instant dateFrom, Instant dateTo, String oid, int chunkSize, ChunkResultSet resultSet) {
        List<MeasurementChunkDescription> lst = new ArrayList<>();
        Optional<Integer> countElements = client.fetchNumberOfMeasurements(DateUtil.getFormattedUtcString(dateFrom), DateUtil.getFormattedUtcString(dateTo), oid);

        if (countElements.isEmpty()) {
            // if no measurements are found its '0', if its empty => exception (you might want to catch it already earlier...)
        }

        int ctElements = countElements.get();
        MeasurementChunkDescription d = new MeasurementChunkDescription(new TimeSpan(dateFrom, dateTo), ctElements);

        if (ctElements > chunkSize) {
            Pair<TimeSpan, TimeSpan> timeTuple = DateUtil.divideTimesByTwo(dateFrom, dateTo);
            lst.addAll(collectChunks(timeTuple.getValue0().getDateFrom(), timeTuple.getValue0().getDateTo(), oid, chunkSize, resultSet));
            lst.addAll(collectChunks(timeTuple.getValue1().getDateFrom(), timeTuple.getValue1().getDateTo(), oid, chunkSize, resultSet));
            resultSet.registerDataSplitAction();
        } else {
            lst.add(d);
        }

        return lst;
    }


}
