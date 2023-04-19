package com.vihnin.ppo2.repository;

import com.vihnin.ppo2.model.GymUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GymUserRepository extends JpaRepository<GymUser, Long> {
}
