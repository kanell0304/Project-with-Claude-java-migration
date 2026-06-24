package com.digitalhelper.repository;

import com.digitalhelper.entity.Guide;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface GuideRepository extends JpaRepository<Guide, Long> {

    @Query("SELECT g FROM Guide g WHERE g.appName = :appName AND g.task = :task AND g.expiresAt > :now")
    Optional<Guide> findValidGuide(@Param("appName") String appName,
                                   @Param("task") String task,
                                   @Param("now") LocalDateTime now);

    Optional<Guide> findByAppNameAndTask(String appName, String task);
}
