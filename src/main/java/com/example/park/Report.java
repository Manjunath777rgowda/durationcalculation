package com.example.park;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.HashMap;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Report {

    @Data
    public static class MonthData {

        private long duration = 0;
        private String durationCalculation = "";

        public void setDuration( long start, long end )
        {
            this.duration = this.duration + (end - start);
            this.durationCalculation =
                    this.durationCalculation + String.format("(%s,%s)", DateUtils.convertISTtoUTC(new Date(start)),
                            DateUtils.convertISTtoUTC(new Date(end)));
        }

        public long getDurationInMin()
        {
            return this.duration / (60 * 1000);
        }
    }

    private String resourceId;
    private String comments = "";
    private HashMap<Integer, MonthData> monthData = new HashMap<>();
}
