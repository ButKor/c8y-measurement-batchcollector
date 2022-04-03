package collector.recordset;

import collector.util.DateUtil;
import com.github.freva.asciitable.AsciiTable;
import com.github.freva.asciitable.Column;
import com.google.common.base.Preconditions;
import config.IRequestConfig;
import org.javatuples.Pair;

import java.text.DecimalFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static com.github.freva.asciitable.HorizontalAlign.LEFT;
import static com.github.freva.asciitable.HorizontalAlign.RIGHT;


public class ConsoleChunkResultDescriptor {

    private static final ConsoleChunkResultDescriptor instance = new ConsoleChunkResultDescriptor();
    private static final Character[] TABLE_BASIC_ASCII_ONLY_HEADER_BORDER = {null, null, null, null, null, ' ', null, null, '-', ' ', null, null, ' ', null, null, null, null, null, null, '-', ' ', null, null, null, null, null, null, null, null};
    private static final Character[] TABLE_BASIC_ASCII_ONLY_HEADER_BORDER_NO_SEP = {null, null, null, null, null, null, null, null, '-', null, null, null, null, null, null, null, null, null, null, '-', null, null, null, null, null, null, null, null, null};
    private final DecimalFormat numberFormatter = new DecimalFormat("#,###");

    public static ConsoleChunkResultDescriptor instance() {
        return instance;
    }

    public void print(ChunkResultSet resultSet, boolean includeChunks) {
        System.out.println(describe(resultSet, includeChunks));
    }

    public String describe(ChunkResultSet resultSet, boolean includeChunks) {
        Preconditions.checkNotNull(resultSet, "Chunk Result set not allowed being null");

        StringBuilder sb = new StringBuilder();
        sb.append(System.lineSeparator());
        sb.append("*** Measurement Chunk result set ***").append(System.lineSeparator());
        sb.append(describeInputConfig(resultSet.getRequestConfig())).append(System.lineSeparator());
        sb.append(System.lineSeparator());
        sb.append(describeRuntime(resultSet)).append(System.lineSeparator());
        sb.append(System.lineSeparator());
        sb.append(describeResultOverview(resultSet)).append(System.lineSeparator());
        sb.append(System.lineSeparator());
        if (includeChunks) {
            sb.append(describeChunkSet(resultSet)).append(System.lineSeparator());
        }

        return sb.toString();
    }

    public String describeRuntime(ChunkResultSet resultSet) {
        Preconditions.checkNotNull(resultSet, "Chunk Result set not allowed being null");

        List<Pair<String, String>> data = Arrays.asList(
                new Pair<>("Start:", DateUtil.getFormattedUtcString(resultSet.getRunningTime().getDateFrom())),
                new Pair<>("End:", DateUtil.getFormattedUtcString(resultSet.getRunningTime().getDateTo())),
                new Pair<>("Duration:", numberFormatter.format(resultSet.getRunningTime().between(ChronoUnit.MILLIS))));


        return createVerticalAsciiTable(35, new Pair<>("Runtime:", ""), data);
    }

    public String describeInputConfig(IRequestConfig requestConfig) {
        Preconditions.checkNotNull(requestConfig, "Request Config not allowed being null");

        List<Pair<String, String>> data = Arrays.asList(
                new Pair<>("Date from:", requestConfig.dateFrom()),
                new Pair<>("Date to:", requestConfig.dateTo()),
                new Pair<>("Max. chunk size:", numberFormatter.format(requestConfig.chunkSize())),
                new Pair<>("Device Id:", "\"" + String.valueOf(requestConfig.source()) + "\"")
        );

        return createVerticalAsciiTable(35, new Pair<>("Configuration:", ""), data);
    }

    public String describeResultOverview(ChunkResultSet resultSet) {
        Preconditions.checkNotNull(resultSet, "Result Set not allowed being null");
        Preconditions.checkNotNull(resultSet.getRecords(), "Result Set records not allowed being null");

        List<Pair<String, String>> data = Arrays.asList(
                new Pair<>("Count Measurements (total):", numberFormatter.format(
                        resultSet.getRecords().stream()
                                .map(MeasurementChunkDescription::getCountMeasurements)
                                .reduce(Integer::sum)
                                .orElse(0))),
                new Pair<>("Count Chunks (total):", numberFormatter.format(resultSet.getMetricsChunkSizes().getCount())),
                new Pair<>("Count Chunks (total, no null):", numberFormatter.format(resultSet.getMetricsChunkSizesNotNull().getCount())),
                new Pair<>("Chunk size (max, no null):", numberFormatter.format(resultSet.getMetricsChunkSizesNotNull().getSnapshot().getMax())),
                new Pair<>("Chunk size (mean, no null):", numberFormatter.format(resultSet.getMetricsChunkSizesNotNull().getSnapshot().getMean())),
                new Pair<>("Chunk size (min, no null):", numberFormatter.format(resultSet.getMetricsChunkSizesNotNull().getSnapshot().getMin())),
                new Pair<>("Count exec. data splits (total):", numberFormatter.format(resultSet.getMetricsCountDataSplits().getCount()))
        );

        return createVerticalAsciiTable(53, new Pair<>("Results Overview:", ""), data);
    }


    public String describeChunkSet(ChunkResultSet resultSet) {
        Preconditions.checkNotNull(resultSet, "Chunk Result set not allowed being null");

        return "Chunk Records:" + System.lineSeparator() +
                AsciiTable.getTable(TABLE_BASIC_ASCII_ONLY_HEADER_BORDER, resultSet.getRecords(),
                        Arrays.asList(
                                new Column().header(String.format("%-35s", "Date from")).headerAlign(LEFT).dataAlign(LEFT).with(
                                        record -> DateUtil.getFormattedUtcString(record.getTimespan().getDateFrom())),
                                new Column().header(String.format("%-35s", "Date To")).headerAlign(LEFT).dataAlign(LEFT).with(
                                        record -> DateUtil.getFormattedUtcString(record.getTimespan().getDateTo())),
                                new Column().header(String.format("%-20s", "Count elements")).headerAlign(LEFT).dataAlign(RIGHT).with(
                                        record -> numberFormatter.format(record.getCountMeasurements()))
                        )
                );
    }

    private String createVerticalAsciiTable(int leftColSize, Pair<String, String> horizontalHeaders, Collection<Pair<String, String>> data) {
        return AsciiTable.getTable(TABLE_BASIC_ASCII_ONLY_HEADER_BORDER_NO_SEP, data,
                Arrays.asList(
                        new Column().header(String.format("%-" + leftColSize + "s", horizontalHeaders.getValue0()))
                                .headerAlign(LEFT).dataAlign(LEFT).with(
                                        Pair::getValue0),
                        new Column().header(horizontalHeaders.getValue1()).headerAlign(LEFT).dataAlign(RIGHT).with(
                                Pair::getValue1)
                )
        );
    }
}
