package com.finrizika.app;

import org.springframework.data.repository.Repository;

public interface UserRepository extends Repository<User, Long> {
    User save(User user);
    User findOne(Long id);
    
    User findByEmail(String email);
}