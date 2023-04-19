package com.vihnin.ppo2.service;

import com.vihnin.ppo2.model.GymUser;
import com.vihnin.ppo2.model.UserEvent;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import javax.security.auth.login.AccountExpiredException;
import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ReportServiceTest {
    private static final String USERNAME_1 = "username1";
    private static final String DATE_1 = "2023-04-03 12:00";

    @Autowired
    GymUserService gymUserService;

    @Autowired
    EntranceService entranceService;

    @Autowired
    ReportService reportService;

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Test
    void getAvgDuration() {
        LocalDateTime expirationDate = LocalDateTime.parse("2023-04-03 12:00", formatter);
        gymUserService.createUser("username", expirationDate);
        List<GymUser> all = gymUserService.getAll();
        Assertions.assertEquals(1, all.size());
        Assertions.assertEquals("username", all.get(0).username);
        Assertions.assertEquals(expirationDate, all.get(0).expiration);
    }

    @Test
    void registerEntry() throws AccessDeniedException, AccountExpiredException {
        LocalDateTime expirationDate = LocalDateTime.parse("2023-04-03 12:00", formatter);
        Long memId = gymUserService.createUser(USERNAME_1, expirationDate);

        LocalDateTime enterTime = LocalDateTime.parse("2023-04-03 10:00", formatter);
        entranceService.registerEntry(memId, enterTime);
        LocalDateTime exitTime = LocalDateTime.parse("2023-04-03 11:00", formatter);
        entranceService.registerExit(memId, exitTime);

        List<UserEvent> allById = entranceService.getAllById(memId);
        assertEquals(2, allById.size());
        assertEquals(UserEvent.Event.ENTER, allById.get(0).type);
        assertEquals(enterTime, allById.get(0).created);
        assertEquals(UserEvent.Event.EXIT, allById.get(1).type);
        assertEquals(exitTime, allById.get(1).created);
    }

    @Test
    void registerEntryExpired() {
        LocalDateTime expirationDate = LocalDateTime.parse("2023-04-03 12:00", formatter);
        Long memId = gymUserService.createUser(USERNAME_1, expirationDate);

        LocalDateTime enterTime = LocalDateTime.parse("2023-04-03 13:00", formatter);
        assertThrows(AccountExpiredException.class, () -> entranceService.registerEntry(memId, enterTime));
    }

    @Test
    void registerEntryExpirationExtension() throws AccessDeniedException, AccountExpiredException {
        LocalDateTime expirationDate = LocalDateTime.parse(DATE_1, formatter);
        Long userId = gymUserService.createUser(USERNAME_1, expirationDate);

        LocalDateTime enterTime = LocalDateTime.parse("2023-04-03 13:00", formatter);
        assertThrows(AccountExpiredException.class, () -> entranceService.registerEntry(userId, enterTime));

        gymUserService.setMembershipExpiration(userId, LocalDateTime.parse("2023-06-03 12:00", formatter));
        entranceService.registerEntry(userId, enterTime);
    }

    @Test
    void registerEntryNotFound() {
        LocalDateTime enterTime = LocalDateTime.parse("2023-04-03 13:00", formatter);
        assertThrows(AccessDeniedException.class, () -> entranceService.registerEntry(123L, enterTime));
    }

    @Test
    void testReport() throws AccessDeniedException, AccountExpiredException {
        LocalDateTime expirationDate = LocalDateTime.parse("2023-05-03 12:00", formatter);
        Long memId = gymUserService.createUser(USERNAME_1, expirationDate);

        // 2 hours
        entranceService.registerEntry(memId, LocalDateTime.parse("2023-04-03 06:00", formatter));
        entranceService.registerExit(memId, LocalDateTime.parse("2023-04-03 08:00", formatter));

        // 1 hour
        entranceService.registerEntry(memId, LocalDateTime.parse("2023-04-03 09:00", formatter));
        entranceService.registerExit(memId, LocalDateTime.parse("2023-04-03 10:00", formatter));

        Map<Long, Double> avgDuration = reportService.getAvgDuration();
        assertEquals(90d, avgDuration.get(memId));

        // 3 hour
        entranceService.registerEntry(memId, LocalDateTime.parse("2023-04-03 11:00", formatter));
        entranceService.registerExit(memId, LocalDateTime.parse("2023-04-03 14:00", formatter));

        avgDuration = reportService.getAvgDuration();
        assertEquals(120d, avgDuration.get(memId));
    }
}