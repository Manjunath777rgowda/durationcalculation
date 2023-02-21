package com.example.park;

import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@RestController
@RequestMapping( "/start" )
public class Controller {

    @GetMapping
    public void startTheProcess()
            throws IOException, ParseException, CsvRequiredFieldEmptyException, CsvDataTypeMismatchException
    {
        HashMap<String, List<AuditLog>> hashmap = ReadCSV.readCSV();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date decStart = simpleDateFormat.parse("2022-12-01");
        Date decEnd = new Date(simpleDateFormat.parse("2023-01-01").getTime() - 1);
        Date janStart = simpleDateFormat.parse("2023-01-01");
        Date janEnd = new Date(simpleDateFormat.parse("2023-02-01").getTime() - 1);
        List<Report> result = new ArrayList<>();

        for( Map.Entry<String, List<AuditLog>> entry : hashmap.entrySet() )
        {
            Report report = new Report();
            report.setResourceId(entry.getKey());
            List<AuditLog> value = entry.getValue();
            Collections.sort(value);
            Date start = null;
            Date end = null;
            boolean isStarted = false;
            for( int i = 0, valueSize = value.size(); i < valueSize; i++ )
            {
                AuditLog auditLog = value.get(i);

                if( auditLog.getEventName().equals("Park") && auditLog.getStatus().equals("Complete")
                        && auditLog.getTriggerTime().before(janEnd) )
                {
                    start = auditLog.getTriggerTime();
                    if( start.before(decStart) )
                        start = decStart;

                    if( isStarted )
                        report.setComments("");

                    isStarted = true;
                    String log = "Park Complete on : " + auditLog.getTriggerTime();
                    String startLog = report.getComments().isEmpty() ? log : report.getComments() + " | " + log;
                    report.setComments(startLog);
                }

                if( start != null && start.before(auditLog.getTriggerTime()) && (auditLog.getEventName()
                        .equals("Unpark")) && auditLog.getStatus().equals("Complete") )
                {
                    end = auditLog.getTriggerTime();
                    String endLog = report.getComments() + " | " + auditLog.getEventName() + " Complete on : "
                            + auditLog.getTriggerTime();
                    report.setComments(endLog);
                    isStarted = false;
                }

                if( isStarted && start != null && start.before(auditLog.getTriggerTime()) && (auditLog.getEventName()
                        .equals("Delete")) && auditLog.getStatus().equals("Complete") )
                {
                    end = auditLog.getTriggerTime();
                    String endLog = report.getComments() + " | " + auditLog.getEventName() + " Complete on : "
                            + auditLog.getTriggerTime();
                    report.setComments(endLog);
                    isStarted = false;
                }

                if( start != null && end != null )
                {
                    if( start.equals(decStart) || (start.after(decStart) && start.before(decEnd)) )
                    {
                        if( end.before(decEnd) )
                        {
                            report.setDecember(start.getTime(), end.getTime());
                        }
                        else if( end.after(janStart) && end.before(janEnd) )
                        {
                            report.setDecember(start.getTime(), decEnd.getTime());
                            report.setJanuary(janStart.getTime(), end.getTime());
                        }
                        else if( end.after(janEnd) )
                        {
                            report.setDecember(start.getTime(), decEnd.getTime());
                            report.setJanuary(janStart.getTime(), janEnd.getTime());
                        }
                        else
                            System.out.println("Error");
                    }
                    else if( start.after(janStart) && start.before(janEnd) )
                    {
                        if( end.before(janEnd) )
                        {
                            report.setJanuary(start.getTime(), end.getTime());
                        }
                        else if( end.after(janEnd) )
                        {
                            report.setJanuary(start.getTime(), janEnd.getTime());
                        }
                        else
                            System.out.println("Error");
                    }
                    else
                        System.out.println("Error");

                    start = null;
                    end = null;
                }

                if( start != null && i == valueSize - 1 )
                {
                    if( start.equals(decStart) || (start.after(decStart) && start.before(decEnd)) )
                    {
                        report.setDecember(start.getTime(), decEnd.getTime());
                        report.setJanuary(janStart.getTime(), janEnd.getTime());
                    }
                    else if( start.after(janStart) && start.before(janEnd) )
                    {
                        report.setJanuary(start.getTime(), janEnd.getTime());
                    }
                    else
                        System.out.println("Error");
                }

            }

            if( report.getDecember() != 0 || report.getJanuary() != 0 )
                result.add(report);

        }

        for( Report report : result )
        {

            if( report.getDecember() != 0 )
                report.setDecember(report.getDecember() / (1000 * 60));

            if( report.getJanuary() != 0 )
                report.setJanuary(report.getJanuary() / (1000 * 60));

            System.out.println(report.getResourceId() + "|" + report.getDecember() + "|" + report.getJanuary() + "|"
                    + report.getDecemberCalculation() + "|" + report.getJanuaryCalculation());

            if( report.getDecember() > 44640 || report.getJanuary() > 44640 )
            {
                System.out.println("_______________________________________________________________");
                System.out.println("More value : " + report.getResourceId());
                System.out.println(report.getResourceId() + "|" + report.getDecember() + "|" + report.getJanuary() + "|"
                        + report.getComments());
                System.out.println("_______________________________________________________________");
            }

            if( report.getDecember() < 0 || report.getJanuary() < 0 )
            {
                System.out.println("_______________________________________________________________");
                System.out.println("Negative value : " + report.getResourceId());
                System.out.println(report.getResourceId() + "|" + report.getDecember() + "|" + report.getJanuary() + "|"
                        + report.getComments());
                System.out.println("_______________________________________________________________");

            }
        }

        ReadCSV.writeCSV(result);
        System.out.println("Completed");
    }

}
