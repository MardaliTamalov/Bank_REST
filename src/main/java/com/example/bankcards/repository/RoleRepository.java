package com.example.bankcards.repository;

import com.example.bankcards.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, String> {
    Optional<Role> findById(String id);
    
    @Query("SELECT r FROM Role r WHERE r.id = :id")
    Optional<Role> findByRoleId(@Param("id") String id);
    
    List<Role> findAll();
    
    boolean existsById(String id);
} 