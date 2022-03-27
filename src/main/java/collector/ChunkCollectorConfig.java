package collector;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.time.Instant;

@AllArgsConstructor
@Getter
@ToString
@Builder
public class ChunkCollectorConfig {
    private Instant dateFrom;
    private Instant dateTo;
    private String oid;
    private int chunkSize;
}
