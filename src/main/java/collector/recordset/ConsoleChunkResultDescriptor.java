package collector.recordset;

import collector.util.DateUtil;

import java.text.DecimalFormat;
import java.time.temporal.ChronoUnit;


public class ConsoleChunkResultDescriptor {

    private static final ConsoleChunkResultDescriptor instance = new ConsoleChunkResultDescriptor();
    private final DecimalFormat numberFormatter = new DecimalFormat("#,###");

    public static ConsoleChunkResultDescriptor instance() {
        return instance;
    }

    public void print(ChunkResultSet resultSet, boolean includeChunks) {
        System.out.println(describe(resultSet, includeChunks));
    }

    public String describe(ChunkResultSet resultSet, boolean includeChunks) {
        StringBuilder sb = new StringBuilder();
        sb.append(System.lineSeparator()).append("*** Measurement Chunk result set ***").append(System.lineSeparator());
        sb.append(describeInputConfig(resultSet)).append(System.lineSeparator());
        sb.append(describeRuntime(resultSet)).append(System.lineSeparator());
        sb.append(describeResultOverview(resultSet)).append(System.lineSeparator());
        if (includeChunks) {
            sb.append(describeChunkSet(resultSet));
        }

        return sb.toString();
    }

    public String describeRuntime(ChunkResultSet resultSet) {
        StringBuilder sb = new StringBuilder();
        sb.append("Runtime:").append(System.lineSeparator());
        sb.append("-".repeat(60)).append(System.lineSeparator());
        sb.append(String.format("%-15s %s", "start:", DateUtil.getFormattedUtcString(resultSet.getRunningTime().getDateFrom())))
                .append(System.lineSeparator());
        sb.append(String.format("%-15s %s", "end:", DateUtil.getFormattedUtcString(resultSet.getRunningTime().getDateTo())))
                .append(System.lineSeparator());
        sb.append(String.format("%-15s %s ms", "duration:", numberFormatter.format(resultSet.getRunningTime().between(ChronoUnit.MILLIS))))
                .append(System.lineSeparator());
        return sb.toString();
    }

    public String describeInputConfig(ChunkResultSet resultSet) {
        StringBuilder sb = new StringBuilder();
        sb.append("Configuration:").append(System.lineSeparator());
        sb.append("-".repeat(60)).append(System.lineSeparator());
        sb.append(String.format("%-15s %s", "dateFrom:", DateUtil.getFormattedUtcString(resultSet.getCollectorCfg().getDateFrom())))
                .append(System.lineSeparator());
        sb.append(String.format("%-15s %s", "dateTo:", DateUtil.getFormattedUtcString(resultSet.getCollectorCfg().getDateTo())))
                .append(System.lineSeparator());
        sb.append(String.format("%-15s \"%s\"", "deviceId:", resultSet.getCollectorCfg().getOid()))
                .append(System.lineSeparator());
        sb.append(String.format("%-15s %s", "chunkSize:", numberFormatter.format(resultSet.getCollectorCfg().getChunkSize())))
                .append(System.lineSeparator());
        return sb.toString();
    }

    public String describeChunkSet(ChunkResultSet resultSet) {
        StringBuilder sb = new StringBuilder();
        sb.append(System.lineSeparator());
        sb.append("Chunks:").append(System.lineSeparator());
        sb.append(String.format("%-35s   %-35s   %-20s" + System.lineSeparator(), "dateFrom", "dateTo", "countElements"));
        sb.append(String.format("%-35s   %-35s   %-20s" + System.lineSeparator(), "-".repeat(35), "-".repeat(35), "-".repeat(20)));
        resultSet.getRecords().forEach(
                d -> sb.append(
                        String.format("%-35s   %-35s   %20s" + System.lineSeparator(),
                                DateUtil.getFormattedUtcString(d.getTimespan().getDateFrom()),
                                DateUtil.getFormattedUtcString(d.getTimespan().getDateTo()),
                                numberFormatter.format(d.getCountMeasurements())
                        )
                )
        );
        return sb.toString();
    }

    public String describeResultOverview(ChunkResultSet resultSet) {
        StringBuilder sb = new StringBuilder();
        sb.append("Results Overview:").append(System.lineSeparator());
        sb.append("-".repeat(60)).append(System.lineSeparator());
        sb.append(String.format("%-35s %s", "Count Measurements (total):",
                        numberFormatter.format(
                                resultSet.getRecords().stream()
                                        .map(MeasurementChunkDescription::getCountMeasurements)
                                        .reduce(Integer::sum)
                                        .orElse(null))
                )
        ).append(System.lineSeparator());
        sb.append(String.format("%-35s %s", "Count Chunks (total):", numberFormatter.format(resultSet.getRecords().size()))).append(System.lineSeparator());
        sb.append(String.format("%-35s %s", "Count Chunks (total, no null):",
                        numberFormatter.format(
                                resultSet.getRecords().stream()
                                        .map(MeasurementChunkDescription::getCountMeasurements)
                                        .filter(l -> l > 0)
                                        .count())
                )
        ).append(System.lineSeparator());
        sb.append(String.format("%-35s %s", "Count exec. data splits (total):", resultSet.getCtDataSplits())).append(System.lineSeparator());
        sb.append(String.format("%-35s %s", "Chunk size (max):",
                        numberFormatter.format(
                                resultSet.getRecords().stream()
                                        .map(MeasurementChunkDescription::getCountMeasurements)
                                        .reduce(Integer::max)
                                        .orElse(null))
                )
        ).append(System.lineSeparator());
        sb.append(String.format("%-35s %s", "Chunk size (min, no null):",
                        numberFormatter.format(
                                resultSet.getRecords().stream()
                                        .map(MeasurementChunkDescription::getCountMeasurements)
                                        .filter(l -> l > 0)
                                        .reduce(Integer::min)
                                        .orElse(null))
                )
        );
        return sb.toString();
    }
}
