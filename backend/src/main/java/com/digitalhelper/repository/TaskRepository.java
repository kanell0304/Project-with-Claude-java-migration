package com.digitalhelper.repository;

import com.digitalhelper.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<Task, Long> {
    boolean existsByName(String name);
}
