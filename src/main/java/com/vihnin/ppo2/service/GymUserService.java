package com.vihnin.ppo2.service;

import com.vihnin.ppo2.model.GymUser;
import com.vihnin.ppo2.repository.GymUserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class GymUserService {

    private final GymUserRepository gymUserRepository;

    public GymUserService(GymUserRepository gymUserRepository) {
        this.gymUserRepository = gymUserRepository;
    }

    public Long createUser(String username, LocalDateTime expiration) {
        GymUser gymUser = new GymUser();
        gymUser.username = username;
        gymUser.expiration = expiration;
        return gymUserRepository.save(gymUser).id;
    }

    public void setMembershipExpiration(Long membershipId, LocalDateTime expiration) {
        GymUser gymUser;
        try {
            gymUser = gymUserRepository.findById(membershipId).orElseThrow();
        } catch (EntityNotFoundException | NoSuchElementException e) {
            throw new IllegalArgumentException("User not found");
        }

        gymUser.expiration = expiration;
        gymUserRepository.save(gymUser);
    }

    public List<GymUser> getAll() {
        return gymUserRepository.findAll();
    }
}
