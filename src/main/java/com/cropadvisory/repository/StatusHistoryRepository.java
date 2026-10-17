package com.cropadvisory.repository;

import com.cropadvisory.model.StatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface StatusHistoryRepository extends JpaRepository<StatusHistory, Long> {

    @Query("select h from StatusHistory h join fetch h.changedBy where h.record.id = :recordId "
            + "order by h.changedAt")
    List<StatusHistory> findByRecordIdWithChangedBy(@Param("recordId") Long recordId);
}
