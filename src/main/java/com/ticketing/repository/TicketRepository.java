package com.ticketing.repository;

import com.ticketing.domain.Ticket;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;


@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {
	String queryTicketsNotUpdatedinDays = "SELECT t FROM Ticket t LEFT JOIN t.history th " // <--- CHANGED THIS LINE
    		+ "GROUP BY t.ticketId HAVING COALESCE(MAX(th.actionDate), t.creationDate) < :thresholdDate";



    List<Ticket> findByCreatedBy_UserId(Long createdByUserId);
    List<Ticket> findByAssignee_UserId(Long assigneeUserId);
    List<Ticket> findByStatus(String status);
    List<Ticket> findByPriority(String priority);
    List<Ticket> findByTitleContainingIgnoreCase(String title);
    List<Ticket> findByDescriptionContainingIgnoreCase(String description);
    List<Ticket> findByCategoryIgnoreCase(String category);
    List<Ticket> findByCreationDateBetween(java.util.Date startDate, java.util.Date endDate);
   
    List<Ticket> findByCreatedBy_Manager_UserId(Long managerUserId);
    
    @Query(queryTicketsNotUpdatedinDays)
    List<Ticket> findTicketsNotUpdatedSinceConsideringCreation(@Param("thresholdDate") Date thresholdDate);


}