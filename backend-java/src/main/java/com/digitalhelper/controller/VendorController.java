package com.digitalhelper.controller;

import com.digitalhelper.entity.Vendor;
import com.digitalhelper.repository.VendorRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/vendors")
public class VendorController {

    private final VendorRepository vendorRepository;

    public VendorController(VendorRepository vendorRepository) {
        this.vendorRepository = vendorRepository;
    }

    @GetMapping
    public List<Map<String, String>> getVendors(@RequestParam(required = false) String category) {
        List<Vendor> vendors = category != null
                ? vendorRepository.findByCategoryOrderByName(category)
                : vendorRepository.findAllByOrderByCategoryAscNameAsc();

        return vendors.stream()
                .map(v -> Map.of("name", v.getName(), "category", v.getCategory()))
                .toList();
    }
}
