package com.cropadvisory.service;

import com.cropadvisory.model.Status;
import com.cropadvisory.repository.DashboardRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class DashboardService {

    public record Bar(String label, long count, int percent) { }
    public record Summary(long total, long open, long closed, List<Bar> byStatus, List<Bar> byCrop) { }

    private final DashboardRepository repo;

    public DashboardService(DashboardRepository repo) {
        this.repo = repo;
    }

    @Transactional(readOnly = true)
    public Summary summary() {
        long total = repo.totalRecords();

        Map<Status, Long> statusCounts = new EnumMap<>(Status.class);
        for (Object[] row : repo.countByStatus()) {
            statusCounts.put((Status) row[0], (Long) row[1]);
        }
        List<Bar> byStatus = new ArrayList<>();
        for (Status s : Status.values()) {
            byStatus.add(bar(s.getLabel(), statusCounts.getOrDefault(s, 0L), total));
        }

        List<Bar> byCrop = new ArrayList<>();
        for (Object[] row : repo.countByCrop()) {
            byCrop.add(bar((String) row[0], (Long) row[1], total));
        }

        long closed = statusCounts.getOrDefault(Status.CLOSED, 0L);
        return new Summary(total, total - closed, closed, byStatus, byCrop);
    }

    private Bar bar(String label, long count, long total) {
        int pct = total == 0 ? 0 : (int) Math.round(count * 100.0 / total);
        return new Bar(label, count, pct);
    }
}