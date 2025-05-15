package com.J2EEWEB.beautyweb.repository;

import com.J2EEWEB.beautyweb.entity.Service;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ServiceRepository extends JpaRepository<Service,Long> {
    Optional<Service> findByName(String name);
    List<Service> findByStatusTrue();
    long count();
}

