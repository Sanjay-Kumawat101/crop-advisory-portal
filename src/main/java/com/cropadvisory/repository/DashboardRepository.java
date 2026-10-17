package com.cropadvisory.repository;

import com.cropadvisory.model.AdvisoryRecord;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.util.List;

public interface DashboardRepository extends Repository<AdvisoryRecord, Long> {

    @Query("select count(r) from AdvisoryRecord r")
    long totalRecords();

    @Query("select r.status, count(r) from AdvisoryRecord r group by r.status")
    List<Object[]> countByStatus();

    @Query("select r.crop, count(r) from AdvisoryRecord r group by r.crop order by count(r) desc")
    List<Object[]> countByCrop();
}