package com.wedknots.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.wedknots.model.Role;

public interface RoleRepository extends JpaRepository<Role, Long> {
}
