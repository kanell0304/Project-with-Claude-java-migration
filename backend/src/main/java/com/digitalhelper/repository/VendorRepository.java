package com.digitalhelper.repository;

import com.digitalhelper.entity.Vendor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VendorRepository extends JpaRepository<Vendor, Long> {
    List<Vendor> findByCategoryOrderByName(String category);
    List<Vendor> findAllByOrderByCategoryAscNameAsc();
    boolean existsByName(String name);
}
