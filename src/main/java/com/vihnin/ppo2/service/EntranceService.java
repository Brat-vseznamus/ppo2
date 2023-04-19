package com.vihnin.ppo2.service;

import com.vihnin.ppo2.model.GymUser;
import com.vihnin.ppo2.model.UserEvent;
import com.vihnin.ppo2.repository.GymUserRepository;
import com.vihnin.ppo2.repository.UserEventRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import javax.security.auth.login.AccountExpiredException;
import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class EntranceService {

    private final GymUserRepository gymUserRepository;
    private final UserEventRepository userEventRepository;
    private final ReportService reportService;

    public EntranceService(
            GymUserRepository gymUserRepository,
            UserEventRepository userEventRepository, 
            ReportService reportService
    ) {
        this.gymUserRepository = gymUserRepository;
        this.userEventRepository = userEventRepository;
        this.reportService = reportService;
    }

    public void registerEntry(Long userId, LocalDateTime time) throws AccessDeniedException, AccountExpiredException {
        registerEvent(userId, time, UserEvent.Event.ENTER);
    }

    public void registerExit(Long userId, LocalDateTime time) throws AccessDeniedException, AccountExpiredException {
        registerEvent(userId, time, UserEvent.Event.EXIT);
    }

    public void registerEvent(Long userId, LocalDateTime time, UserEvent.Event curEvent) throws AccessDeniedException, AccountExpiredException {
        checkUser(userId, time);

        UserEvent event = new UserEvent();
        event.userId = userId;
        event.type = curEvent;
        event.created = time;

        userEventRepository.save(event);
        reportService.registerEvent(event);
    }

    private void checkUser(Long userId, LocalDateTime time) throws AccessDeniedException, AccountExpiredException {
        GymUser gymUser;
        try {
            gymUser = gymUserRepository.findById(userId).orElseThrow();
        } catch (EntityNotFoundException | NoSuchElementException e) {
            throw new AccessDeniedException("Membership not found");
        }

        if (gymUser.expiration.isBefore(time)) {
            throw new AccountExpiredException("Membership expired");
        }
    }

    public List<UserEvent> getAllById(Long userId) {
        return userEventRepository.getAllByUserId(userId);
    }
}
