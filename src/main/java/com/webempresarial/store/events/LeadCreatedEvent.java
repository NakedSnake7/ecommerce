package com.webempresarial.store.events;

import com.webempresarial.store.entity.Lead;

public class LeadCreatedEvent {

    private final Lead lead;

    public LeadCreatedEvent(Lead lead) {
        this.lead = lead;
    }

    public Lead getLead() {
        return lead;
    }
}