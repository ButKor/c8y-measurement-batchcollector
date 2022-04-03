package collector.recordset;

import collector.util.TimeSpan;
import com.codahale.metrics.Counter;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import config.IRequestConfig;
import lombok.Getter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Getter
public class ChunkResultSet {

    private final IRequestConfig requestConfig;
    private final List<MeasurementChunkDescription> records = new ArrayList<>();

    // Monitored metrics
    private final TimeSpan runningTime = new TimeSpan();
    private final MetricRegistry metrics = new MetricRegistry();
    private final Histogram metricsChunkSizes = metrics.histogram("histogramMeasurements");
    private final Histogram metricsChunkSizesNotNull = metrics.histogram("histogramMeasurementsNotNull");
    private final Counter metricsCountDataSplits = metrics.counter("countDataSplits");
    private final Timer metricsTimer = metrics.timer("runningTime");


    public ChunkResultSet(IRequestConfig requestConfig) {
        this.requestConfig = requestConfig;
    }

    public void addMeasurementChunkDescriptions(List<MeasurementChunkDescription> l) {
        l.forEach(this::addMeasurementChunkDescription);
    }

    public void addMeasurementChunkDescription(MeasurementChunkDescription m){
        if(m == null){
            return;
        }
        if(!records.add(m))
            return;

        int ctMeas = m.getCountMeasurements();
        metricsChunkSizes.update(ctMeas);
        if(m.getCountMeasurements() > 0){
            metricsChunkSizesNotNull.update(ctMeas);
        }
    }

    public void registerRunStart(Instant startTime) {
        metricsTimer.time();
        this.runningTime.setDateFrom(startTime);
    }

    public void registerRunFinish(Instant finishTime) {
        metricsTimer.time().stop();
        this.runningTime.setDateTo(finishTime);
    }

    public void registerDataSplitAction() {
        metricsCountDataSplits.inc();
    }

}
