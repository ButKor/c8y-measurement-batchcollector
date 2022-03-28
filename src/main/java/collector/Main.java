package collector;

import collector.recordset.ChunkResultSet;
import collector.recordset.ConsoleChunkResultDescriptor;
import collector.util.DateUtil;
import org.apache.commons.lang3.time.StopWatch;
import org.javatuples.Pair;

public class Main {

    private final String baseUrl = "https://example.cumulocity.com";
    private final String user = "t12345/myuser";
    private final String pass = "";

    private final String dateFrom = "2021-01-01T00:00:00.000Z";
    private final String dateTo = "2022-04-01T00:00:00.0000Z";
    private final String oid = "100200";

    private final int chunkSize = 49999;

    public static void main(String[] args) {
        new Main().run();
    }

    public void run() {
        C8yHttpClient client = new C8yHttpClient(baseUrl, user, pass);

        /*
         *   Spin up a binary tree for all records:
         *       1) Request the total amount of measurement records between time A and B
         *       2) Divide the time span (always by two) until the amount of measurementRecords is <= max allowed chunk size
         *   => Result will be a list of time spans with no entry having more than the allowed number of measurements
         *
         *   Note that only the number of measurements between certain timestamps requested, not the measurements themselves.
         *
         * */
        ChunkCollector collector = new ChunkCollector(client);
        System.out.print("Build request-chunks .... ");
        Pair<ChunkResultSet, StopWatch> chunkResult = collector.collectWithClock(
                ChunkCollectorConfig.builder()
                        .dateFrom(DateUtil.getDateTime(dateFrom))
                        .dateTo(DateUtil.getDateTime(dateTo))
                        .oid(oid)
                        .chunkSize(chunkSize)
                        .build()
        );
        System.out.printf(" => Elapsed time: %s" + System.lineSeparator(), chunkResult.getValue1().formatTime());

        ConsoleChunkResultDescriptor.instance().print(chunkResult.getValue0(), true);

        /*
         *   Run over all chunks, collect their (CSV-) Measurements and dump to File
         *   The chunks should be:
         *        a) already sorted ascending by time and
         *        b) have no time overlaps (thus should not have duplicates)
         * */
        System.out.print("Request measurements and dump to file....");
        Pair<Boolean, StopWatch> processResult = new CsvMeasurementsToFileProcessors(client).runWithClock(chunkResult.getValue0(), "./measurements.txt");
        System.out.printf(" => Elapsed time: %s" + System.lineSeparator(), processResult.getValue1().formatTime());
    }

}
