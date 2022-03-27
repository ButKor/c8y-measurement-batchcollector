package collector.recordset;

import collector.util.DateUtil;

import java.text.DecimalFormat;
import java.time.temporal.ChronoUnit;


public class ConsoleChunkResultDescriptor {

    private static final ConsoleChunkResultDescriptor instance = new ConsoleChunkResultDescriptor();

    public static ConsoleChunkResultDescriptor instance(){
        return instance;
    }

    public void print(ChunkResultSet resultSet, boolean includeChunks) {
        System.out.println(describe(resultSet, includeChunks));
    }

    public String describe (ChunkResultSet resultSet, boolean includeChunks) {
        StringBuilder sb = new StringBuilder();
        DecimalFormat formatter = new DecimalFormat("#,###");

        sb.append(System.lineSeparator());
        sb.append("*** Measurement Chunk result set ***").append(System.lineSeparator());

        // ### Input Config ###
        sb.append("Configuration:").append(System.lineSeparator());
        sb.append("-".repeat(60)).append(System.lineSeparator());
        sb.append(String.format("%-15s %s" + System.lineSeparator(), "dateFrom:", DateUtil.getFormattedUtcString(resultSet.getCollectorCfg().getDateFrom())));
        sb.append(String.format("%-15s %s" + System.lineSeparator(), "dateTo:", DateUtil.getFormattedUtcString(resultSet.getCollectorCfg().getDateTo())));
        sb.append(String.format("%-15s \"%s\"" + System.lineSeparator(), "deviceId:", resultSet.getCollectorCfg().getOid()));
        sb.append(String.format("%-15s %s" + System.lineSeparator(), "chunkSize:", formatter.format(resultSet.getCollectorCfg().getChunkSize())));

        // ### Runtime infos ###
        sb.append(System.lineSeparator());
        sb.append("Runtime:").append(System.lineSeparator());
        sb.append("-".repeat(60)).append(System.lineSeparator());
        sb.append(String.format("%-15s %s" + System.lineSeparator(), "start:", DateUtil.getFormattedUtcString(resultSet.getRunningTime().getDateFrom())));
        sb.append(String.format("%-15s %s" + System.lineSeparator(), "end:", DateUtil.getFormattedUtcString(resultSet.getRunningTime().getDateTo())));
        sb.append(String.format("%-15s %s ms" + System.lineSeparator(), "duration:", formatter.format(resultSet.getRunningTime().between(ChronoUnit.MILLIS))));

        // ### Results Overview ###
        sb.append(System.lineSeparator());
        sb.append("Results Overview:").append(System.lineSeparator());
        sb.append("-".repeat(60)).append(System.lineSeparator());
        sb.append(String.format("%-35s %s" + System.lineSeparator(), "Count Measurements (total):",
                formatter.format(
                        resultSet.getRecords().stream()
                                .map(MeasurementChunkDescription::getCountMeasurements)
                                .reduce(Integer::sum)
                                .orElse(null))
                )
        );
        sb.append(String.format("%-35s %s" + System.lineSeparator(), "Count Chunks (total):", formatter.format(resultSet.getRecords().size())));
        sb.append(String.format("%-35s %s" + System.lineSeparator(), "Count Chunks (total, no null):",
                formatter.format(
                        resultSet.getRecords().stream()
                                .map(MeasurementChunkDescription::getCountMeasurements)
                                .filter(l -> l > 0)
                                .count())
                )
        );
        sb.append(String.format("%-35s %s" + System.lineSeparator(), "Count exec. data splits (total):", resultSet.getCtDataSplits()));
        sb.append(String.format("%-35s %s" + System.lineSeparator(), "Chunk size (max):",
                formatter.format(
                        resultSet.getRecords().stream()
                                .map(MeasurementChunkDescription::getCountMeasurements)
                                .reduce(Integer::max)
                                .orElse(null))
                )
        );
        sb.append(String.format("%-35s %s" + System.lineSeparator(), "Chunk size (min, no null):",
                formatter.format(
                        resultSet.getRecords().stream()
                                .map(MeasurementChunkDescription::getCountMeasurements)
                                .filter(l -> l > 0)
                                .reduce(Integer::min)
                                .orElse(null))
                )
        );

        // ### Chunk Details ###
        if (includeChunks) {
            sb.append(System.lineSeparator());
            sb.append("Chunks:").append(System.lineSeparator());
            sb.append(String.format("%-35s   %-35s   %-20s" + System.lineSeparator(), "dateFrom", "dateTo", "countElements"));
            sb.append(String.format("%-35s   %-35s   %-20s" + System.lineSeparator(), "-".repeat(35), "-".repeat(35), "-".repeat(20)));
            resultSet.getRecords().forEach(
                    d -> sb.append(
                            String.format("%-35s   %-35s   %20s" + System.lineSeparator(),
                                    DateUtil.getFormattedUtcString(d.getTimespan().getDateFrom()),
                                    DateUtil.getFormattedUtcString(d.getTimespan().getDateTo()),
                                    formatter.format(d.getCountMeasurements())
                            )
                    )
            );
        }

        return sb.toString();
    }
}
