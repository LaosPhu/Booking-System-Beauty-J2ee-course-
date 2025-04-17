package com.J2EEWEB.beautyweb.repository;

import com.J2EEWEB.beautyweb.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository  extends JpaRepository<User,Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
    Optional<User> findByEmailContaining(String emailStart);  // Or findByEmailContaining


}
