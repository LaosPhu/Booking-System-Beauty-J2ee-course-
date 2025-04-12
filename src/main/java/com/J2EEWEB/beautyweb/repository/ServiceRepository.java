package com.J2EEWEB.beautyweb.repository;

import com.J2EEWEB.beautyweb.entity.Service;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ServiceRepository extends JpaRepository<Service,Long> {
    Optional<Service> findByName(String name);
}

