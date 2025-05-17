package com.ticketing.service;

import com.ticketing.domain.Ticket;
import com.ticketing.domain.TicketHistory;
import com.ticketing.domain.User;
import com.ticketing.repository.TicketHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TicketHistoryService {

    private final TicketHistoryRepository ticketHistoryRepository;
    
    private final TicketService ticketService;
    
    private final UserService userService;

    @Autowired
    public TicketHistoryService(TicketHistoryRepository ticketHistoryRepository) {
        this.ticketHistoryRepository = ticketHistoryRepository;
		this.ticketService = null;
		this.userService = null;
    }

    public TicketHistory save(TicketHistory ticketHistory) {
        return ticketHistoryRepository.save(ticketHistory);
    }

    public Optional<TicketHistory> findById(Long id) {
        return ticketHistoryRepository.findById(id);
    }

    public List<TicketHistory> findAll() {
        return ticketHistoryRepository.findAll();
    }

    public void deleteById(Long id) {
        ticketHistoryRepository.deleteById(id);
    }

    public List<TicketHistory> findByTicket_TicketId(Long ticketId) {
        return ticketHistoryRepository.findByTicket_ticketId(ticketId);
    }

    public List<TicketHistory> findByActionBy_userId(Long actionByUserId) {
        return ticketHistoryRepository.findByActionBy_userId(actionByUserId);
    }

    public List<TicketHistory> findByAction(String action) {
        return ticketHistoryRepository.findByAction(action);
    }

    // You can add more service methods as needed, for example:
    // - Finding history within a date range
    // - Finding history by a specific user and action

    public TicketHistory addHistory(Long ticketId, String action, Long actionByUserId, String comments) {
        // Assuming you have TicketService and UserService injected
        Optional<Ticket> ticketOptional = ticketService.findById(ticketId);
        Optional<User> userOptional = userService.findById(actionByUserId);

        if (ticketOptional.isPresent() && userOptional.isPresent()) {
            Ticket ticket = ticketOptional.get();
            User actionBy = userOptional.get();

            TicketHistory newHistory = new TicketHistory();
            newHistory.setTicket(ticket);
            newHistory.setAction(action);
            newHistory.setActionBy(actionBy);
//            newHistory.setActionDate(new Date());
            newHistory.setComments(comments);

            return ticketHistoryRepository.save(newHistory);
        }
        return null; // Or throw an exception if ticket or user not found
    }
}