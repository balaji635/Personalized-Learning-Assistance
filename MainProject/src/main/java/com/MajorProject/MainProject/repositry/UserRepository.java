package com.MajorProject.MainProject.repositry;

import com.MajorProject.MainProject.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {  // ← UUID not Long
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
}