package com.example.springboot.mvc;

import org.springframework.data.jpa.repository.JpaRepository;

public interface GenericEntityRepository extends JpaRepository<GenericEntity, Long> {
}
