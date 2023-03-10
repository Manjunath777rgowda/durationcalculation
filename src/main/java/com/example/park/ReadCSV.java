package com.example.park;

import com.opencsv.CSVReader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Month;
import java.util.*;

public class ReadCSV {

    public static HashMap<String, List<AuditLog>> readCSV() throws IOException, ParseException
    {
        String fileName = "C:\\Users\\Admin\\Downloads\\Auditlog_dataset.csv";
        HashMap<String, List<AuditLog>> hashMap = new HashMap<>();
        try( CSVReader csvReader = new CSVReader(new FileReader(fileName)); )
        {
            String[] values = null;
            while( (values = csvReader.readNext()) != null )
            {
                List<String> list = Arrays.asList(values);
                if( list.contains("id") )
                    continue;

                Date timestamp = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse(list.get(6));
                AuditLog auditLog = new AuditLog();
                auditLog.setId(Long.parseLong(list.get(0)));
                auditLog.setTriggerTime(timestamp);
                auditLog.setStatus(list.get(11));
                auditLog.setResourceId(list.get(10));
                auditLog.setEventName(list.get(8));
                auditLog.setDetails(list.get(7));

                if( !auditLog.getStatus().equals("Complete") )
                    continue;

                if( !hashMap.containsKey(auditLog.getResourceId()) )
                    hashMap.put(auditLog.getResourceId(), new ArrayList<>());

                hashMap.get(auditLog.getResourceId()).add(auditLog);

            }
        }

        return hashMap;
    }

    public static void writeCSV( List<Report> reportList, List<Integer> months )
    {
        try
        {
            String destinationFile = "C:\\Users\\Admin\\Downloads\\result.csv";
            File file = new File(destinationFile);
            FileWriter myWriter = new FileWriter(file, false);

            List<String> headerList = new ArrayList<>();
            headerList.add("Resource Id");
            for( Integer month : months )
            {
                String monthName = Month.of(month).name();
                headerList.add(monthName + " Parked time in Min");
                headerList.add(monthName + " Calculation Data");
            }
            headerList.add("Comments\n");
            myWriter.write(String.join(",", headerList));

            for( Report report : reportList )
            {
                List<String> valueList = new ArrayList<>();
                valueList.add(report.getResourceId());

                for( Integer month : months )
                {
                    Report.MonthData monthData = report.getMonthData().get(month - 1);
                    if( monthData == null )
                    {
                        valueList.add("0");
                        valueList.add("NA");
                    }
                    else
                    {
                        valueList.add(String.valueOf(monthData.getDurationInMin()));
                        valueList.add("\"" + monthData.getDurationCalculation() + "\"");
                    }
                }

                valueList.add(report.getComments() + "\n");
                myWriter.write(String.join(",", valueList));
            }

            myWriter.close();
        }
        catch( Exception e )
        {
            e.printStackTrace();
        }
    }
}
