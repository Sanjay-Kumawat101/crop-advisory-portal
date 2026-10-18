package com.cropadvisory;

import com.cropadvisory.dto.RecordForm;
import com.cropadvisory.model.AdvisoryRecord;
import com.cropadvisory.model.Status;
import com.cropadvisory.model.User;
import com.cropadvisory.repository.UserRepository;
import com.cropadvisory.service.RecordService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class RecordWorkflowTest {

    @Autowired private RecordService service;
    @Autowired private UserRepository users;

    private RecordForm form() {
        RecordForm f = new RecordForm();
        f.setVillage("Rampur");
        f.setCrop("Rice");
        f.setProblemTitle("Brown spots on leaves");
        f.setDescription("Spots spreading after rain.");
        return f;
    }

    @Test
    void fullWorkflowReachesClosed() {
        User farmer = users.findByUsername("farmer1").orElseThrow();
        User expert = users.findByUsername("expert1").orElseThrow();

        AdvisoryRecord r = service.create(farmer, form());
        assertEquals(Status.SUBMITTED, r.getStatus());

        service.changeStatus(expert, r.getId(), Status.UNDER_REVIEW, null);
        service.changeStatus(expert, r.getId(), Status.ADVISED, "Spray a recommended fungicide.");
        service.changeStatus(farmer, r.getId(), Status.CLOSED, null);

        assertEquals(Status.CLOSED, service.get(farmer, r.getId()).getStatus());
        assertEquals(4, service.history(r.getId()).size());
    }

    @Test
    void farmerCannotSkipToAdvised() {
        User farmer = users.findByUsername("farmer1").orElseThrow();
        AdvisoryRecord r = service.create(farmer, form());
        assertThrows(ResponseStatusException.class,
                () -> service.changeStatus(farmer, r.getId(), Status.ADVISED, "self advice"));
    }

    @Test
    void searchFindsRecordByCrop() {
        User expert = users.findByUsername("expert1").orElseThrow();
        assertFalse(service.search(expert, "wheat", null).isEmpty());
        assertTrue(service.search(expert, "nosuchcrop", null).isEmpty());
    }

    @Test
    void farmerCannotOpenAnotherFarmersRecord() {
        User farmer = users.findByUsername("farmer1").orElseThrow();
        User admin = users.findByUsername("admin1").orElseThrow();
        AdvisoryRecord r = service.create(farmer, form());
        // admin can view, but a different farmer role user (not owner) must not
        User other = new User("farmer2", "x", "Other Farmer", com.cropadvisory.model.Role.FARMER);
        users.save(other);
        assertNotNull(service.get(admin, r.getId()));
        assertThrows(ResponseStatusException.class, () -> service.get(other, r.getId()));
    }
}