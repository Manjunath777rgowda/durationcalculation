/*
 * Copyright(C) 2015-16. Nuvepro Ltd. All rights reserved.
 */

package com.example.park;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuditLog implements Comparable<AuditLog> {

    public static enum eNOTIFICATION_LEVEL {
        SUCCESS, ERROR, WARNING, INFORMATION, DEBUG;

        private eNOTIFICATION_LEVEL()
        {
        }
    }

    public static enum eNOTIFICATION_PRIORITY {
        HIGH, MEDIUM, LOW;

        private eNOTIFICATION_PRIORITY()
        {
        }
    }

    private static final long serialVersionUID = 1L;


    private long id;
    private eNOTIFICATION_LEVEL level = eNOTIFICATION_LEVEL.ERROR;
    private eNOTIFICATION_PRIORITY priority = eNOTIFICATION_PRIORITY.HIGH;
    private String category;
    private String userName;
    private Date triggerTime;
    private String source;
    private String details;
    private String resourceType;
    private String resourceId;
    private String eventName;
    private String status;
    @Override
    public int compareTo( AuditLog auditLog )
    {
        return (int) ( this.id-auditLog.getId());
    }
}
