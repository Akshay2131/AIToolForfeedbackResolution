package com.example.aitoolforfeedbackresolution.repositories;

import com.example.aitoolforfeedbackresolution.model.LOG_ERROR;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LogErrorRepository extends JpaRepository<LOG_ERROR, Long> {

}
