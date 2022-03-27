package collector.recordset;

import collector.ChunkCollectorConfig;
import collector.util.DateUtil;
import collector.util.TimeSpan;
import lombok.Getter;

import java.text.DecimalFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Getter
public class ChunkResultSet {

    private final ChunkCollectorConfig collectorCfg;
    private final List<MeasurementChunkDescription> records = new ArrayList<>();
    private final TimeSpan runningTime = new TimeSpan();
    private int ctDataSplits = 0;

    public ChunkResultSet(ChunkCollectorConfig collectorCfg) {
        this.collectorCfg = collectorCfg;
    }

    public void addMeasurementChunkDescriptions(List<MeasurementChunkDescription> l) {
        this.records.addAll(l);
    }

    public void registerRunStart(Instant startTime) {
        this.runningTime.setDateFrom(startTime);
    }

    public void registerRunFinish(Instant finishTime) {
        this.runningTime.setDateTo(finishTime);
    }

    public void registerDataSplitAction() {
        this.ctDataSplits++;
    }

}
