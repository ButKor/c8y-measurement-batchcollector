package c8yapi.builder;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import org.javatuples.Pair;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class AlarmParameters {
    @NonNull
    private String deviceId;
    @NonNull
    private String type;
    @NonNull
    private String text;
    @NonNull
    private String severity;

    private DateTime time;
    private List<Pair<String, Object>> fragments;
}
