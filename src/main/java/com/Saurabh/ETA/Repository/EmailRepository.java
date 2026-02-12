package com.Saurabh.ETA.Repository;

import com.Saurabh.ETA.Entity.EmailEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmailRepository extends JpaRepository<EmailEntity, Long> {

    Optional<EmailEntity> findByEmail(String email);

    boolean existsByEmail(String email);

}