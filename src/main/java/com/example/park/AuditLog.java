/*
 * Copyright(C) 2015-16. Nuvepro Ltd. All rights reserved.
 */

package com.example.park;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Date;

@Table(name = "auditLog")
@Entity
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

    @Id
    @Column( name = "id" )
    private long id;

    @Column( name = "level" )
    @Enumerated( EnumType.STRING )
    private eNOTIFICATION_LEVEL level = eNOTIFICATION_LEVEL.ERROR;

    @Column( name = "priority" )
    @Enumerated( EnumType.STRING )
    private eNOTIFICATION_PRIORITY priority = eNOTIFICATION_PRIORITY.HIGH;

    @Column( name = "category" )
    private String category;

    @Column( name = "username" )
    private String userName;

    @Column( name = "triggertime" )
    private Date triggerTime;

    @Column( name = "source" )
    private String source;

    @Column( name = "details" )
    private String details;

    @Column( name = "resourcetype" )
    private String resourceType;

    @Column( name = "resourceid" )
    private String resourceId;

    @Column( name = "eventname" )
    private String eventName;

    @Column( name = "status" )
    private String status;

    @Override
    public int compareTo( AuditLog auditLog )
    {
        return (int) ( this.id-auditLog.getId());
    }
}
