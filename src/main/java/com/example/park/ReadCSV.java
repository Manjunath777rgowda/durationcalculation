package com.example.park;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class ReadCSV {

    public static HashMap<String, List<AuditLog>> readCSV() throws IOException, ParseException
    {
        String fileName = "C:\\Users\\Admin\\Downloads\\park.csv";
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

    public static void writeCSV( List<Report> reportList )
            throws IOException, CsvRequiredFieldEmptyException, CsvDataTypeMismatchException
    {

        String destinationFile = "C:\\Users\\Admin\\Downloads\\result.csv";
        FileWriter writer = new FileWriter(destinationFile);
        ColumnPositionMappingStrategy mappingStrategy = new ColumnPositionMappingStrategy();
        mappingStrategy.setType(Report.class);

        CSVWriter csvWrite = new CSVWriter(writer);
        String[] header = new String[] { "Resource Id", "December Parked time in Mins", "January Parked time in Mins",
                "December Calculations Data", "January Calculation Data", "Comments" };
        csvWrite.writeNext(header);

        String[] columns = new String[] { "resourceId", "december", "january", "decemberCalculation",
                "januaryCalculation", "comments" };
        mappingStrategy.setColumnMapping(columns);

        // Creating StatefulBeanToCsv object
        StatefulBeanToCsvBuilder<Report> builder = new StatefulBeanToCsvBuilder(writer);
        StatefulBeanToCsv beanWriter = builder.withMappingStrategy(mappingStrategy).build();

        // Write list to StatefulBeanToCsv object
        beanWriter.write(reportList);

        // closing the writer object
        csvWrite.close();
        writer.close();

    }
}
