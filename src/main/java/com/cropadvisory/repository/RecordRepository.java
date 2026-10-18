package com.cropadvisory.repository;

import com.cropadvisory.model.AdvisoryRecord;
import com.cropadvisory.model.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RecordRepository extends JpaRepository<AdvisoryRecord, Long> {

    @Query("""
        select r from AdvisoryRecord r
        join fetch r.farmer
        left join fetch r.expert
        where (:farmerId is null or r.farmer.id = :farmerId)
          and (:status is null or r.status = :status)
          and (:q = '' or lower(r.crop) like lower(concat('%', :q, '%'))
                       or lower(r.village) like lower(concat('%', :q, '%'))
                       or lower(r.problemTitle) like lower(concat('%', :q, '%')))
        order by r.createdAt desc
        """)
    List<AdvisoryRecord> search(@Param("farmerId") Long farmerId,
                                @Param("status") Status status,
                                @Param("q") String query);

    @Query("""
        select r from AdvisoryRecord r
        join fetch r.farmer
        left join fetch r.expert
        where r.id = :id
        """)
    Optional<AdvisoryRecord> findDetailById(@Param("id") Long id);

    @Query("select r from AdvisoryRecord r join fetch r.farmer f left join fetch r.expert "
            + "where f.username = :username order by r.createdAt desc")
    List<AdvisoryRecord> findByFarmerUsername(@Param("username") String username);

    @Query("select r from AdvisoryRecord r join fetch r.farmer left join fetch r.expert "
            + "where r.id = :id")
    Optional<AdvisoryRecord> findByIdWithUsers(@Param("id") Long id);
}
