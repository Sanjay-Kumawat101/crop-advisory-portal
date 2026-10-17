package com.cropadvisory.service;

import com.cropadvisory.dto.RecordForm;
import com.cropadvisory.model.AdvisoryRecord;
import com.cropadvisory.model.Role;
import com.cropadvisory.model.Status;
import com.cropadvisory.model.StatusHistory;
import com.cropadvisory.model.User;
import com.cropadvisory.controller.RecordForm;
import com.cropadvisory.model.*;
import com.cropadvisory.repository.RecordRepository;
import com.cropadvisory.repository.StatusHistoryRepository;
import com.cropadvisory.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class RecordService {

    private final RecordRepository recordRepository;
    private final StatusHistoryRepository statusHistoryRepository;
    private final UserRepository userRepository;

    public RecordService(RecordRepository recordRepository,
                         StatusHistoryRepository statusHistoryRepository,
                         UserRepository userRepository) {
        this.recordRepository = recordRepository;
        this.statusHistoryRepository = statusHistoryRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public AdvisoryRecord create(String username, RecordForm form) {
        User farmer = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("User not found: " + username));
        return create(farmer, form);
    }

    @Transactional
    public AdvisoryRecord create(User farmer, RecordForm form) {
        if (farmer.getRole() != Role.FARMER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only farmers can submit queries");
        }
        AdvisoryRecord record = new AdvisoryRecord(
                farmer,
                form.getVillage().trim(),
                form.getCrop().trim(),
                form.getProblemTitle().trim(),
                clean(form.getDescription()));
        AdvisoryRecord saved = recordRepository.save(record);
        statusHistoryRepository.save(new StatusHistory(saved, null, saved.getStatus(), farmer));
        return saved;
    }

    @Transactional(readOnly = true)
    public User currentUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    @Transactional(readOnly = true)
    public List<AdvisoryRecord> search(User user, String query, Status status) {
        Long farmerId = user.getRole() == Role.FARMER ? user.getId() : null;
        return recordRepository.searchRecords(farmerId, clean(query), status);
    }

    @Transactional(readOnly = true)
    public AdvisoryRecord get(User user, Long id) {
        AdvisoryRecord record = recordRepository.findByIdWithUsers(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Record not found"));
        if (user.getRole() == Role.FARMER && !record.getFarmer().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only view your own records");
    private final StatusHistoryRepository historyRepository;
    private final UserRepository userRepository;

    public RecordService(RecordRepository recordRepository,
                         StatusHistoryRepository historyRepository,
                         UserRepository userRepository) {
        this.recordRepository = recordRepository;
        this.historyRepository = historyRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public User currentUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
    }

    @Transactional(readOnly = true)
    public List<AdvisoryRecord> search(User viewer, String q, Status status) {
        Long farmerId = viewer.getRole() == Role.FARMER ? viewer.getId() : null;
        return recordRepository.search(farmerId, status, q == null ? "" : q.trim());
    }

    @Transactional(readOnly = true)
    public AdvisoryRecord get(User viewer, Long id) {
        AdvisoryRecord r = recordRepository.findDetailById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Record not found"));
        if (viewer.getRole() == Role.FARMER && !r.getFarmer().getId().equals(viewer.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your record");
        }
        return r;
    }

    @Transactional(readOnly = true)
    public List<StatusHistory> history(Long recordId) {
        return historyRepository.findByRecordId(recordId);
    }

    @Transactional
    public AdvisoryRecord create(User farmer, RecordForm f) {
        if (farmer.getRole() != Role.FARMER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only farmers can raise queries");
        }
        AdvisoryRecord r = recordRepository.save(new AdvisoryRecord(farmer,
                f.getVillage().trim(), f.getCrop().trim(),
                f.getProblemTitle().trim(), f.getDescription()));
        historyRepository.save(new StatusHistory(r, null, Status.SUBMITTED, farmer));
        return r;
    }

    @Transactional(readOnly = true)
    public List<StatusHistory> history(Long recordId) {
        return statusHistoryRepository.findByRecordIdWithChangedBy(recordId);
    }

    @Transactional(readOnly = true)
    public List<Status> allowedNext(User user, AdvisoryRecord record) {
        if (user.getRole() == Role.EXPERT && record.getStatus() == Status.SUBMITTED) {
            return List.of(Status.UNDER_REVIEW);
        }
        if (user.getRole() == Role.EXPERT && record.getStatus() == Status.UNDER_REVIEW) {
            return List.of(Status.ADVISED);
        }
        if (user.getRole() == Role.ADMIN && record.getStatus() == Status.ADVISED) {
            return List.of(Status.CLOSED);
        }
        return List.of();
    }

    @Transactional(readOnly = true)
    public boolean canEdit(User user, AdvisoryRecord record) {
        return user.getRole() == Role.FARMER
                && record.getFarmer().getId().equals(user.getId())
                && record.getStatus() == Status.SUBMITTED;
    }

    @Transactional
    public void update(User user, Long id, RecordForm form) {
        AdvisoryRecord record = get(user, id);
        if (!canEdit(user, record)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This query cannot be edited");
        }
        record.setVillage(form.getVillage().trim());
        record.setCrop(form.getCrop().trim());
        record.setProblemTitle(form.getProblemTitle().trim());
        record.setDescription(clean(form.getDescription()));
        recordRepository.save(record);
    }

    @Transactional
    public void changeStatus(User user, Long id, Status target, String advice) {
        AdvisoryRecord record = get(user, id);
        if (!allowedNext(user, record).contains(target)) {
            throw new IllegalArgumentException("You cannot move this query to that status.");
        }
        if (target == Status.ADVISED && (advice == null || advice.isBlank())) {
            throw new IllegalArgumentException("Advice is required before marking a query as advised.");
        }

        Status previous = record.getStatus();
        record.setStatus(target);
        if (target == Status.UNDER_REVIEW) {
            record.setExpert(user);
        }
        if (target == Status.ADVISED) {
            record.setAdvice(clean(advice));
        }
        recordRepository.save(record);
        statusHistoryRepository.save(new StatusHistory(record, previous, target, user));
    }

    private String clean(String value) {
        return (value == null || value.isBlank()) ? null : value.trim();
    public boolean canEdit(User u, AdvisoryRecord r) {
        return u.getRole() == Role.FARMER
                && r.getFarmer().getId().equals(u.getId())
                && r.getStatus() == Status.SUBMITTED;
    }

    @Transactional
    public void update(User viewer, Long id, RecordForm f) {
        AdvisoryRecord r = get(viewer, id);
        if (!canEdit(viewer, r)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Record can no longer be edited");
        }
        r.setVillage(f.getVillage().trim());
        r.setCrop(f.getCrop().trim());
        r.setProblemTitle(f.getProblemTitle().trim());
        r.setDescription(f.getDescription());
    }

    /** Role-based workflow rules. */
    public List<Status> allowedNext(User u, AdvisoryRecord r) {
        boolean owner = u.getRole() == Role.FARMER && r.getFarmer().getId().equals(u.getId());
        return switch (r.getStatus()) {
            case SUBMITTED -> u.getRole() == Role.EXPERT ? List.of(Status.UNDER_REVIEW) : List.of();
            case UNDER_REVIEW -> u.getRole() == Role.EXPERT ? List.of(Status.ADVISED) : List.of();
            case ADVISED -> (owner || u.getRole() == Role.ADMIN) ? List.of(Status.CLOSED) : List.of();
            case CLOSED -> List.of();
        };
    }

    @Transactional
    public void changeStatus(User actor, Long id, Status target, String advice) {
        AdvisoryRecord r = get(actor, id);
        if (!allowedNext(actor, r).contains(target)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This status change is not allowed");
        }
        if (target == Status.ADVISED) {
            if (advice == null || advice.isBlank()) {
                throw new IllegalArgumentException("Advice text is required.");
            }
            if (advice.length() > 2000) {
                throw new IllegalArgumentException("Advice must be 2000 characters or less.");
            }
            r.setAdvice(advice.trim());
        }
        if (target == Status.UNDER_REVIEW) {
            r.setExpert(actor);
        }
        Status old = r.getStatus();
        r.setStatus(target);
        historyRepository.save(new StatusHistory(r, old, target, actor));
    }
}