package csvmeasurementcollector.runtime;

import org.apache.commons.lang3.time.StopWatch;
import org.javatuples.Pair;

public interface IRuntimeObserver {

    void fileWritten(Pair<Boolean, StopWatch> processResult, String outputFilePath);

    void runtimeFinished();

    void runtimeStarted();
}
