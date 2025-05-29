package com.ticketing.repository;

import com.ticketing.domain.TicketHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TicketHistoryRepository extends JpaRepository<TicketHistory, Long> {

    // Spring Data JPA automatically provides basic CRUD operations:
    // save(), findById(), findAll(), deleteById(), delete(), count(), existsById()

    // Custom query methods based on fields in the TicketHistory entity:

	Optional<TicketHistory> findById(Long ticketHistoryId);
    List<TicketHistory> findByTicket_ticketId(Long ticket_ticketId);
    List<TicketHistory> findByActionBy_userId(Long actionBy_userId);
    List<TicketHistory> findByAction(String action);
    List<TicketHistory> findByCommentsContainingIgnoreCase(String comments);
    List<TicketHistory> findByActionDateBetween(java.util.Date startDate, java.util.Date endDate);

    // Example of a more complex query using @Query (optional)
    /*
    @Query("SELECT th FROM TicketHistory th WHERE th.ticket.ticketId = :ticketId AND th.action = :action")
    List<TicketHistory> findByTicketIdAndAction(@Param("ticketId") Long ticketId, @Param("action") String action);
    */

    // Add more query methods as needed based on your application's requirements.
}