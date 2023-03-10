package com.example.park;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping( "/start" )
public class Controller {

    @GetMapping
    public void generateParkDuration( @RequestParam String monthString ) throws Exception
    {
        List<Integer> months = validateMonthString(monthString);
        HashMap<String, List<AuditLog>> hashmap = ReadCSV.readCSV();
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

                if( auditLog.getEventName().equals("Park") && auditLog.getStatus().equals("Complete") )
                {
                    start = auditLog.getTriggerTime();

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
                    calculateTheDuration(start, end, report);
                    start = null;
                    end = null;
                }

                if( start != null && i == valueSize - 1 )
                    calculateTheDuration(start, new Date(), report);
            }
            result.add(report);
        }

        ReadCSV.writeCSV(result, months);
        System.out.println("Completed");
    }

    private List<Integer> validateMonthString( String monthString ) throws Exception
    {
        if( monthString == null || monthString.isEmpty() )
            throw new Exception("MonthString can not be empty");

        String[] split = monthString.split(",");
        return Arrays.stream(split).map(Integer::parseInt).filter(month -> month >= 1 && month <= 12).sorted()
                .collect(Collectors.toList());
    }

    private void calculateTheDuration( Date start, Date end, Report report )
    {
        HashMap<Integer, Report.MonthData> monthDataMap = report.getMonthData();
        while( start.before(end) )
        {
            int month = getBeginningMonth(start).getMonth();
            Report.MonthData monthData = monthDataMap.get(month);
            if( monthData == null )
                monthData = new Report.MonthData();

            Date endOfStartMonth = addMonths(start, 1);

            if( end.after(endOfStartMonth) )
                monthData.setDuration(start.getTime(), endOfStartMonth.getTime());
            else
                monthData.setDuration(start.getTime(), end.getTime());

            start = endOfStartMonth;
            monthDataMap.put(month, monthData);
        }

        report.setMonthData(monthDataMap);
    }

    public static Date addMonths( Date date, int months )
    {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.MONTH, months); //minus number would decrement the days
        return getBeginningMonth(cal.getTime());
    }

    public static Date getBeginningMonth( Date date )
    {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        return cal.getTime();
    }

}
