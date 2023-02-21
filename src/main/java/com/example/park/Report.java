package com.example.park;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Report {

    private String resourceId;
    private long december = 0;
    private long january = 0;
    private String comments = "";
    private String decemberCalculation = "";
    private String januaryCalculation = "";

    public void setDecember( long start, long end )
    {
        this.december = this.december + (end - start);
        this.decemberCalculation = this.decemberCalculation + String.format("(%s,%s)", new Date(start), new Date(end));
    }

    public void setJanuary( long start, long end )
    {
        this.january = this.january + (end - start);
        this.januaryCalculation = this.januaryCalculation + String.format("(%s,%s)", new Date(start), new Date(end));
    }
}
