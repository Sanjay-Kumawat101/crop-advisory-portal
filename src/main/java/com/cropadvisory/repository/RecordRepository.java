package com.cropadvisory.repository;

import com.cropadvisory.model.AdvisoryRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecordRepository extends JpaRepository<AdvisoryRecord, Long> {
}
