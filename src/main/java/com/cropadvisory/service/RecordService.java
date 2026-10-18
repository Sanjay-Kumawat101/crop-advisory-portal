package com.cropadvisory.service;

import com.cropadvisory.dto.RecordForm;
import com.cropadvisory.model.AdvisoryRecord;
import com.cropadvisory.model.Role;
import com.cropadvisory.model.Status;
import com.cropadvisory.model.StatusHistory;
import com.cropadvisory.model.User;
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
    public List<AdvisoryRecord> search(User viewer, String query, Status status) {
        Long farmerId = viewer.getRole() == Role.FARMER ? viewer.getId() : null;
        return recordRepository.search(farmerId, status, query == null ? "" : query.trim());
    }

    @Transactional(readOnly = true)
    public AdvisoryRecord get(User viewer, Long id) {
        AdvisoryRecord record = recordRepository.findDetailById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Record not found"));
        if (viewer.getRole() == Role.FARMER && !record.getFarmer().getId().equals(viewer.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your record");
        }
        return record;
    }

    @Transactional(readOnly = true)
    public List<StatusHistory> history(Long recordId) {
        return historyRepository.findByRecordId(recordId);
    }

    @Transactional
    public AdvisoryRecord create(String username, RecordForm form) {
        return create(currentUser(username), form);
    }

    @Transactional
    public AdvisoryRecord create(User farmer, RecordForm form) {
        if (farmer.getRole() != Role.FARMER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only farmers can raise queries");
        }
        AdvisoryRecord record = recordRepository.save(new AdvisoryRecord(farmer,
                form.getVillage().trim(), form.getCrop().trim(),
                form.getProblemTitle().trim(), clean(form.getDescription())));
        historyRepository.save(new StatusHistory(record, null, Status.SUBMITTED, farmer));
        return record;
    }

    @Transactional(readOnly = true)
    public boolean canEdit(User user, AdvisoryRecord record) {
        return user.getRole() == Role.FARMER
                && record.getFarmer().getId().equals(user.getId())
                && record.getStatus() == Status.SUBMITTED;
    }

    @Transactional
    public void update(User viewer, Long id, RecordForm form) {
        AdvisoryRecord record = get(viewer, id);
        if (!canEdit(viewer, record)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Record can no longer be edited");
        }
        record.setVillage(form.getVillage().trim());
        record.setCrop(form.getCrop().trim());
        record.setProblemTitle(form.getProblemTitle().trim());
        record.setDescription(clean(form.getDescription()));
    }

    @Transactional(readOnly = true)
    public List<Status> allowedNext(User user, AdvisoryRecord record) {
        boolean owner = user.getRole() == Role.FARMER
                && record.getFarmer().getId().equals(user.getId());
        return switch (record.getStatus()) {
            case SUBMITTED -> user.getRole() == Role.EXPERT ? List.of(Status.UNDER_REVIEW) : List.of();
            case UNDER_REVIEW -> user.getRole() == Role.EXPERT ? List.of(Status.ADVISED) : List.of();
            case ADVISED -> owner || user.getRole() == Role.ADMIN ? List.of(Status.CLOSED) : List.of();
            case CLOSED -> List.of();
        };
    }

    @Transactional
    public void changeStatus(User actor, Long id, Status target, String advice) {
        AdvisoryRecord record = get(actor, id);
        if (!allowedNext(actor, record).contains(target)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This status change is not allowed");
        }
        if (target == Status.ADVISED) {
            if (advice == null || advice.isBlank()) {
                throw new IllegalArgumentException("Advice text is required.");
            }
            if (advice.length() > 2000) {
                throw new IllegalArgumentException("Advice must be 2000 characters or less.");
            }
            record.setAdvice(advice.trim());
        }
        if (target == Status.UNDER_REVIEW) {
            record.setExpert(actor);
        }
        Status previousStatus = record.getStatus();
        record.setStatus(target);
        historyRepository.save(new StatusHistory(record, previousStatus, target, actor));
    }

    private String clean(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
