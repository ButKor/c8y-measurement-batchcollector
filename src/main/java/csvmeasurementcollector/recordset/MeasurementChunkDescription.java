package csvmeasurementcollector.recordset;

import util.TimeSpan;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@ToString
@AllArgsConstructor
@Getter
public class MeasurementChunkDescription {
    private final TimeSpan timespan;
    private final Integer countMeasurements;
}
