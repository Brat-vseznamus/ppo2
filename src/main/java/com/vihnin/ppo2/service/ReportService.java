package com.vihnin.ppo2.service;

import com.vihnin.ppo2.model.UserEvent;
import com.vihnin.ppo2.repository.UserEventRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReportService {
    private final Map<Long, List<UserEvent>> eventsByUserId;

    public ReportService(UserEventRepository userEventRepository) {
        eventsByUserId = userEventRepository.findAll().stream().collect(
            Collectors.groupingBy(
                    event -> event.userId,
                    Collectors.toList()
            ));
    }

    public void registerEvent(UserEvent event) {
        eventsByUserId.computeIfAbsent(event.userId, (key) -> new ArrayList<>()).add(event);
    }

    public Map<Long, Double> getAvgDuration() {
        Map<Long, Double> avgByUser = new HashMap<>();

        eventsByUserId.forEach((userId, events) -> {
            long minutesSum = 0;
            var enters = events.stream().filter(ue -> ue.type == UserEvent.Event.ENTER).toList();
            var exits = events.stream().filter(ue -> ue.type == UserEvent.Event.EXIT).toList();

            var numberOfIntervals = Math.min(enters.size(), exits.size());
            for (int i = 0; i < numberOfIntervals; i++) {
                minutesSum += Duration.between(enters.get(i).created, exits.get(i).created).toMinutes();
            }

            double avgForUser = minutesSum / (numberOfIntervals * 1d);
            avgByUser.put(userId, avgForUser);
        });

        return avgByUser;
    }

}
