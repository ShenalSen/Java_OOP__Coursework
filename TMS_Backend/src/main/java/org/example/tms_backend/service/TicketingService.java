package org.example.tms_backend.service;

import org.example.tms_backend.model.Configuration;
import org.example.tms_backend.repository.ConfigurationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class TicketingService {

    @Autowired
    private ConfigurationRepository configurationRepository;

    private final Queue<Integer> ticketPool = new LinkedList<>();
    private boolean isRunning = false;

    private ExecutorService executorService;

    public void saveConfiguration(Configuration configuration) {
        configurationRepository.save(configuration);
    }

    public void startSystem(Configuration configuration) {
        if (isRunning) {
            throw new IllegalStateException("System is already running!");
        }

        isRunning = true;
        executorService = Executors.newFixedThreadPool(2);

        // Start Vendor Thread
        executorService.execute(() -> {
            while (isRunning) {
                synchronized (ticketPool) {
                    while (ticketPool.size() >= configuration.getMaxTicketCapacity()) {
                        try {
                            ticketPool.wait();
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }
                    for (int i = 0; i < configuration.getTicketReleaseRate(); i++) {
                        if (ticketPool.size() < configuration.getMaxTicketCapacity()) {
                            ticketPool.add(1); // Add a ticket
                        }
                    }
                    ticketPool.notifyAll();
                }
            }
        });

        // Start Customer Thread
        executorService.execute(() -> {
            while (isRunning) {
                synchronized (ticketPool) {
                    while (ticketPool.isEmpty()) {
                        try {
                            ticketPool.wait();
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }
                    for (int i = 0; i < configuration.getCustomerRetrievalRate(); i++) {
                        if (!ticketPool.isEmpty()) {
                            ticketPool.poll(); // Remove a ticket
                        }
                    }
                    ticketPool.notifyAll();
                }
            }
        });
    }

    public void stopSystem() {
        isRunning = false;
        if (executorService != null) {
            executorService.shutdownNow();
        }
    }

    public void resetSystem() {
        synchronized (ticketPool) {
            ticketPool.clear();
        }
    }

    public int getTicketCount() {
        synchronized (ticketPool) {
            return ticketPool.size();
        }
    }
}

