package collector.util;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class TimeSpan {

    private Instant dateFrom;
    private Instant dateTo;

    public TimeSpan(){
    }

    public TimeSpan(Instant dateFrom, Instant dateTo){
        this.dateFrom = dateFrom;
        this.dateTo = dateTo;
    }

    public Instant getDateFrom() {
        return dateFrom;
    }

    public Instant getDateTo() {
        return dateTo;
    }

    public void setDateFrom(Instant dateFrom) {
        this.dateFrom = dateFrom;
    }

    public void setDateTo(Instant dateTo) {
        this.dateTo = dateTo;
    }

    public long between(ChronoUnit cu){
        return cu.between(dateFrom, dateTo);
    }

    @Override
    public String toString() {
        return "Timespan{" +
                "dateFrom=" + dateFrom +
                ", dateTo=" + dateTo +
                '}';
    }

}
