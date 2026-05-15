package com.webempresarial.store.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.webempresarial.store.dto.LeadRequestDTO;
import com.webempresarial.store.entity.Lead;
import com.webempresarial.store.service.LeadService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/leads")
public class LeadController {

    private final LeadService leadService;

    public LeadController(LeadService leadService) {
        this.leadService = leadService;
    }

    @PostMapping
    public ResponseEntity<?> createLead(@Valid @RequestBody LeadRequestDTO dto) {

        Lead lead = leadService.createLead(dto);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of(
                        "success", true,
                        "id", lead.getId(),
                        "message", "Lead creado correctamente"
                ));
    }
}