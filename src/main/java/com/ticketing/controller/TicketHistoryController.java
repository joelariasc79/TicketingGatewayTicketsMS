package com.ticketing.controller;

import com.ticketing.domain.TicketHistory;
import com.ticketing.dto.TicketHistoryDto;
import com.ticketing.service.TicketHistoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/api/tickets/history")
public class TicketHistoryController {

	private final TicketHistoryService ticketHistoryService;

    public TicketHistoryController(TicketHistoryService ticketHistoryService) {
        this.ticketHistoryService = ticketHistoryService;
    }
 
    
    @GetMapping("ticket/{ticketId}")
    public ResponseEntity<List<TicketHistoryDto>> getTicketHistory(@PathVariable Long ticketId) {
        // Fetch all history entries for the given ticketId
        // Assuming TicketHistoryService has a method like findByTicketTicketId
    	
        List<TicketHistory> historyList = ticketHistoryService.findByTicket_TicketId(ticketId);

        if (historyList.isEmpty()) {
            // Return 200 OK with an empty list if no history is found,
            // or 404 NOT FOUND if you consider no history an error for the specific ticket.
            // Returning empty list is generally more common for list endpoints.
            return ResponseEntity.ok(List.of()); // Return an empty list
        }

        // Map TicketHistory entities to TicketHistoryDto DTOs
        List<TicketHistoryDto> historyDtos = historyList.stream()
                // Sort by actionDate to ensure chronological order for display
                .sorted(Comparator.comparing(TicketHistory::getActionDate))
                .map(history -> new TicketHistoryDto(
                        history.getTicketHistoryId(),
                        history.getTicket().getTicketId(), // Get ticketId from the associated Ticket
                        history.getAction(),
//                        history.getOldStatus(), // Map oldStatus
//                        history.getNewStatus(), // Map newStatus
                        history.getActionBy() != null ? history.getActionBy().getUserId() : null,
                        history.getActionBy() != null ? history.getActionBy().getUserName() : null,
                        history.getActionDate(),
                        history.getComments()
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(historyDtos);
    }

}