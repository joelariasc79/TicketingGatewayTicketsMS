package com.ticketing.repository;

import com.ticketing.domain.Ticket;
import com.ticketing.domain.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    // Spring Data JPA automatically provides basic CRUD operations:
    // save(), findById(), findAll(), deleteById(), delete(), count(), existsById()

    // You can define custom query methods here by following Spring Data JPA conventions.
    // For example:

    List<Ticket> findByCreatedBy_UserId(Long createdByUserId);
    List<Ticket> findByAssignee_UserId(Long assigneeUserId);
    List<Ticket> findByStatus(String status);
    List<Ticket> findByPriority(String priority);
    List<Ticket> findByTitleContainingIgnoreCase(String title);
    List<Ticket> findByDescriptionContainingIgnoreCase(String description);
    List<Ticket> findByCategoryIgnoreCase(String category);
    List<Ticket> findByCreationDateBetween(java.util.Date startDate, java.util.Date endDate);
    
    
    List<Ticket> findByCreatedBy_Manager_UserId(Long managerUserId);
//
//    Optional<Ticket> findByTicketId(Long ticketId);
    

    // Example of a more complex query using @Query (optional, for more control)
    /*
    @Query("SELECT t FROM Ticket t WHERE t.priority = :priority AND t.status = :status")
    List<Ticket> findByPriorityAndStatus(@Param("priority") String priority, @Param("status") String status);
    */

    // You can add more query methods based on your application's search and filtering requirements.
}