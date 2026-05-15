package com.webempresarial.store.controller.admin;

import com.webempresarial.store.repository.LeadRepository;
import com.webempresarial.store.model.LeadStatus;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/leads")
public class LeadAdminController {

    private final LeadRepository leadRepository;

    public LeadAdminController(LeadRepository leadRepository) {
        this.leadRepository = leadRepository;
    }

    @GetMapping
    public String index(Model model) {
        model.addAttribute("leads", leadRepository.findAll());
        model.addAttribute("statuses", LeadStatus.values());
        return "admin/leads/index";
    }

    @PostMapping("/{id}/status")
    public String updateStatus(
            @PathVariable Long id,
            @RequestParam LeadStatus status
    ) {
        var lead = leadRepository.findById(id)
                .orElseThrow();

        lead.setStatus(status);
        leadRepository.save(lead);

        return "redirect:/admin/leads";
    }
}