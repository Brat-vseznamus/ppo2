package com.vihnin.ppo2.repository;

import com.vihnin.ppo2.model.UserEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserEventRepository extends JpaRepository<UserEvent, Long> {
    List<UserEvent> getAllByUserId(long id);
}
