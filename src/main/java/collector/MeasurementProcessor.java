package collector;

import collector.recordset.ChunkResultSet;
import collector.recordset.MeasurementChunkDescription;
import collector.util.DateUtil;


import java.util.List;

public class MeasurementProcessor {

    public void run(ChunkResultSet chunkResultSet) {

        List<MeasurementChunkDescription> chunks = chunkResultSet.getRecords();
        for(MeasurementChunkDescription c : chunks){
            if (c.getCountMeasurements() == null || c.getCountMeasurements() <= 0){
                // skip empty chunk records
                continue;
            }

            String dateFrom = DateUtil.getFormattedUtcString(c.getTimespan().getDateFrom());
            String dateTo = DateUtil.getFormattedUtcString(c.getTimespan().getDateTo());
            String deviceId = chunkResultSet.getCollectorCfg().getOid();

            // TODO: request Measurements as CSV, append them in a file
        }

    }
}
