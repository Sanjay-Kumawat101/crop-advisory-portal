package com.cropadvisory.config;

import com.cropadvisory.model.AdvisoryRecord;
import com.cropadvisory.model.Role;
import com.cropadvisory.model.User;
import com.cropadvisory.repository.RecordRepository;
import com.cropadvisory.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/** Loads dummy demo data on first start. No real farmer data is used. */
@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RecordRepository recordRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository,
                      RecordRepository recordRepository,
                      PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.recordRepository = recordRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            return;
        }
        User farmer = userRepository.save(new User("farmer1", passwordEncoder.encode("Farmer@123"), "Ramesh Patil", Role.FARMER));
        userRepository.save(new User("expert1", passwordEncoder.encode("Expert@123"), "Dr. Sunita Rao", Role.EXPERT));
        userRepository.save(new User("admin1", passwordEncoder.encode("Admin@123"), "Portal Admin", Role.ADMIN));

        recordRepository.save(new AdvisoryRecord(farmer, "Rampur", "Wheat",
                "Yellow stripes on leaves", "Yellow stripes visible on leaves in two fields since last week."));
        recordRepository.save(new AdvisoryRecord(farmer, "Rampur", "Tomato",
                "Leaf curling", "Young plants show curled leaves and slow growth."));
    }
}
