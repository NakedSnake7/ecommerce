package com.webempresarial.store.events;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class LeadAutomationListener {

    @EventListener
    public void onLeadCreated(LeadCreatedEvent event) {

        var lead = event.getLead();

        System.out.println("Nuevo lead: " + lead.getNombre());
        System.out.println("WhatsApp: " + lead.getWhatsapp());
        System.out.println("Servicio: " + lead.getServicio());

        // Aquí después conectamos:
        // WhatsApp
        // Email
        // CRM
        // Slack
    }
}