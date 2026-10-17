package com.cropadvisory.service;

import com.cropadvisory.dto.RecordForm;
import com.cropadvisory.model.AdvisoryRecord;
import com.cropadvisory.model.User;
import com.cropadvisory.repository.RecordRepository;
import com.cropadvisory.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class RecordService {

    private final RecordRepository recordRepository;
    private final UserRepository userRepository;

    public RecordService(RecordRepository recordRepository, UserRepository userRepository) {
        this.recordRepository = recordRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public AdvisoryRecord create(String username, RecordForm form) {
        User farmer = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("User not found: " + username));
        AdvisoryRecord record = new AdvisoryRecord(
                farmer,
                form.getVillage().trim(),
                form.getCrop().trim(),
                form.getProblemTitle().trim(),
                clean(form.getDescription()));
        return recordRepository.save(record);
    }

    /** Farmers see only their own records. Experts and admins see all records. */
    @Transactional(readOnly = true)
    public List<AdvisoryRecord> listFor(String username, boolean farmerOnly) {
        return farmerOnly
                ? recordRepository.findByFarmerUsername(username)
                : recordRepository.findAllWithUsers();
    }

    @Transactional(readOnly = true)
    public AdvisoryRecord getFor(Long id, String username, boolean farmerOnly) {
        AdvisoryRecord record = recordRepository.findByIdWithUsers(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Record not found"));
        if (farmerOnly && !record.getFarmer().getUsername().equals(username)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only view your own records");
        }
        return record;
    }

    private String clean(String value) {
        return (value == null || value.isBlank()) ? null : value.trim();
    }
}
