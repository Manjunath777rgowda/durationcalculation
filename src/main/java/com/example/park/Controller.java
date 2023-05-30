package com.example.park;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URLConnection;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping( "/rest" )
public class Controller {

    @Autowired
    private AuditlogReportsitory auditlogReportsitory;

    @ApiOperation( value = "API download park duration", notes = "API download park duration" )
    @PostMapping( "/download" )
    public void generateParkDuration( HttpServletRequest request, HttpServletResponse response,
            @RequestParam @ApiParam( required = true, defaultValue = "2,3" ) String monthString,
            @RequestParam @ApiParam( required = true, defaultValue = "1" ) Integer tenantId,
            @RequestParam @ApiParam( required = true, defaultValue = "2023-03-01 00:00:00" ) String startDateString,
            @RequestParam @ApiParam( required = true, defaultValue = "2023-04-01 00:00:00" ) String endDateString )
            throws Exception
    {
        List<Integer> months = validateMonthString(monthString);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date startDate = simpleDateFormat.parse(startDateString);
        Date endDate = simpleDateFormat.parse(endDateString);

        HashMap<String, List<AuditLog>> hashmap = getAuditLogFromDb(startDate, endDate, tenantId);
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
                    String log = "Park Complete on : " + DateUtils.convertISTtoUTC(auditLog.getTriggerTime());
                    String startLog = report.getComments().isEmpty() ? log : report.getComments() + " | " + log;
                    report.setComments(startLog);
                }

                if( start != null && start.before(auditLog.getTriggerTime()) && (auditLog.getEventName()
                        .equals("Unpark")) && auditLog.getStatus().equals("Complete") )
                {
                    end = auditLog.getTriggerTime();
                    String endLog = report.getComments() + " | " + auditLog.getEventName() + " Complete on : "
                            + DateUtils.convertISTtoUTC(auditLog.getTriggerTime());
                    report.setComments(endLog);
                    isStarted = false;
                }

                if( isStarted && start != null && start.before(auditLog.getTriggerTime()) && (auditLog.getEventName()
                        .equals("Delete")) && auditLog.getStatus().equals("Complete") )
                {
                    end = auditLog.getTriggerTime();
                    String endLog = report.getComments() + " | " + auditLog.getEventName() + " Complete on : "
                            + DateUtils.convertISTtoUTC(auditLog.getTriggerTime());
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
                    calculateTheDuration(start, endDate, report);
            }
            result.add(report);
        }

        File file = ReadCSV.writeCSV(result, months);

        if( file.exists() )
        {

            //get the mimetype
            String mimeType = URLConnection.guessContentTypeFromName(file.getName());
            if( mimeType == null )
            {
                //unknown mimetype so set the mimetype to application/octet-stream
                mimeType = "application/octet-stream";
            }

            response.setContentType(mimeType);
            response.setHeader("Content-Disposition", String.format("inline; filename=\"" + file.getName() + "\""));

            //Here we have mentioned it to show as attachment
            //response.setHeader("Content-Disposition", String.format("attachment; filename=\"" + file.getName() +
            // "\""));

            response.setContentLength((int) file.length());

            InputStream inputStream = new BufferedInputStream(Files.newInputStream(file.toPath()));

            FileCopyUtils.copy(inputStream, response.getOutputStream());

        }

        System.out.println("Complete");
    }

    private HashMap<String, List<AuditLog>> getAuditLogFromDb( Date startDate, Date endDate, Integer tenantId )
    {
        List<String> resourceIds = auditlogReportsitory.getAllResource(tenantId, startDate, endDate);
        HashMap<String, List<AuditLog>> hashMap = new HashMap<>();
        for( String id : resourceIds )
        {
            List<AuditLog> auditLogs = auditlogReportsitory.getAuditLogs(startDate, endDate, id);
            if( !auditLogs.isEmpty() )
                hashMap.put(id, auditLogs);
        }
        return hashMap;
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
            int month = DateUtils.getBeginningMonth(start).getMonth();
            int year = start.getYear() + 1900;
            Date endOfStartMonth = DateUtils.addMonths(start, 1);

            if( year != 2023 )
            {
                start = endOfStartMonth;
                continue;
            }

            Report.MonthData monthData = monthDataMap.get(month);
            if( monthData == null )
                monthData = new Report.MonthData();


            if( end.after(endOfStartMonth) )
                monthData.setDuration(start.getTime(), endOfStartMonth.getTime());
            else
                monthData.setDuration(start.getTime(), end.getTime());

            start = endOfStartMonth;
            monthDataMap.put(month, monthData);
        }

        report.setMonthData(monthDataMap);
    }



}
