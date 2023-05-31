//package com.example.park;
//
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Query;
//
//import java.util.Date;
//import java.util.List;
//
//public interface AuditlogReportsitory extends JpaRepository<AuditLog, Long> {
//
//    @Query( value = "( select * from AuditLog where triggerTime < :startDate and category = 'RESOURCE_MANAGER' "
//            + "and resourceType = 'Deployment' and resourceId = :resourceId " + "and ((eventName in ('Stop','Park') "
//            + "and status = 'Failed') or (eventName in ('Create','Redeploy','Stop','Start','Delete','Park','Unpark') "
//            + "and status = 'Complete')) order by triggerTime desc limit 1) " + "union all "
//            + "(select * from AuditLog where triggerTime >= :startDate and triggerTime < :endDate "
//            + "and category = 'RESOURCE_MANAGER' and resourceType = 'Deployment' and resourceId= :resourceId "
//            + "and ((eventName in ('Stop','Park')and status = 'Failed') "
//            + "or (eventName in ('Create','Redeploy','Stop','Start','Delete','Park','Unpark') and status = "
//            + "'Complete')) "
//            + "order by triggerTime)", nativeQuery = true )
//    public List<AuditLog> getAuditLogs( Date startDate, Date endDate, String resourceId );
//
//    @Query(value = "select de.guid from Deployment de left join DeploymentStats stat on de.id = stat.resId_fk "
//            + "left join DeploymentResource der on de.id = der.deploymentId_fk where de.id in "
//            + "(select resid_fk from DeploymentUserMap where nlUserId in "
//            + "(select id from userdetail where tenantid = :tenantId)) and "
//            + "de.createdon is not null and de.createdon <= :endDate and "
//            + "(de.expiredon is null or de.expiredon >= :startDate) order by de.id",nativeQuery = true)
//    public List<String> getAllResource(Integer tenantId, Date startDate, Date endDate);
//
//}
