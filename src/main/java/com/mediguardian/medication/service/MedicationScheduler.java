package com.mediguardian.medication.service;

import com.mediguardian.medication.entity.MedicationSchedule;
import com.mediguardian.medication.repository.MedicationScheduleRepository;
import com.mediguardian.notification.service.NotificationService;
import com.mediguardian.profile.entity.Profile;
import com.mediguardian.profile.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MedicationScheduler {

    private final MedicationScheduleRepository scheduleRepository;
    private final NotificationService notificationService;
    private final ProfileRepository profileRepository;

    // Runs every day at 8:00 AM
    @Scheduled(cron = "0 0 8 * * ?")
    public void scheduleMedicationReminders() {
        log.info("Running daily medication reminder job");
        List<MedicationSchedule> activeSchedules = scheduleRepository.findActiveSchedules(LocalDate.now());
        
        for (MedicationSchedule schedule : activeSchedules) {
            Profile profile = profileRepository.findById(schedule.getProfileId()).orElse(null);
            if (profile != null && profile.getAccountId() != null) {
                String message = String.format("Reminder: Time to take your medication - %s (%s, %s)",
                        schedule.getMedicineName(), schedule.getDosage(), schedule.getFrequency());
                
                notificationService.createNotification(profile.getAccountId(), message);
            }
        }
    }
}
