package org.example.tms_backend.controller;

import org.example.tms_backend.model.Configuration;
import org.example.tms_backend.service.TicketingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ticketing")
@CrossOrigin(origins = "*") // Allow communication with the frontend
public class TicketingController {

    @Autowired
    private TicketingService ticketingService;

    @PostMapping("/configure")
    public String configureSystem(@RequestBody Configuration configuration) {
        ticketingService.saveConfiguration(configuration);
        return "Configuration saved successfully!";
    }

    @PostMapping("/start")
    public String startSystem(@RequestBody Configuration configuration) {
        ticketingService.startSystem(configuration);
        return "System started!";
    }

    @PostMapping("/stop")
    public String stopSystem() {
        ticketingService.stopSystem();
        return "System stopped!";
    }

    @PostMapping("/reset")
    public String resetSystem() {
        ticketingService.resetSystem();
        return "System reset!";
    }

    @GetMapping("/status")
    public int getTicketCount() {
        return ticketingService.getTicketCount();
    }
}
