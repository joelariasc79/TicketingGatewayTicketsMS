package com.ticketing.service;

import com.ticketing.domain.Ticket;
import com.ticketing.domain.TicketHistory;
import com.ticketing.domain.User;
import com.ticketing.repository.TicketHistoryRepository;
//import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TicketHistoryService {

    private final TicketHistoryRepository ticketHistoryRepository;
    
    // Autowire these services to resolve them properly, as they are used in addHistory
    private final TicketService ticketService; 
    
    private final UserService userService;

//    @Autowired
    public TicketHistoryService(TicketHistoryRepository ticketHistoryRepository, 
                                TicketService ticketService, 
                                UserService userService) {
        this.ticketHistoryRepository = ticketHistoryRepository;
        this.ticketService = ticketService; // Initialize
        this.userService = userService;     // Initialize
    }

    // When a new history entry is saved, we don't necessarily update a single existing cache entry.
    // However, if we were caching lists (e.g., findByTicket_TicketId), this save operation
    // would invalidate those lists. For now, we can use @CachePut to put the *new* history item
    // into the cache if we intend to retrieve it by its ID immediately after saving.
    @CachePut(value = "ticketHistory", key = "#ticketHistory.id")
    public TicketHistory save(TicketHistory ticketHistory) {
        System.out.println("Saving/Updating TicketHistory in DB and Cache: " + ticketHistory.getTicketHistoryId());
        return ticketHistoryRepository.save(ticketHistory);
    }

    @Cacheable("ticketHistory") // Cache individual history entries by ID
    public Optional<TicketHistory> findById(Long id) {
        System.out.println("Fetching TicketHistory by ID from DB or Cache: " + id);
        return ticketHistoryRepository.findById(id);
    }

    @Cacheable("allTicketHistory") // Cache all history entries (be cautious with large datasets)
    public List<TicketHistory> findAll() {
        System.out.println("Fetching all TicketHistory from DB or Cache...");
        return ticketHistoryRepository.findAll();
    }

    // When deleting, remove the item from the cache
    @CacheEvict(value = "ticketHistory", key = "#id")
    public void deleteById(Long id) {
        System.out.println("Deleting TicketHistory from DB and Cache: " + id);
        ticketHistoryRepository.deleteById(id);
    }

    // Cache by ticket ID. When a new history is added for a ticket, this cache should be invalidated.
    // If addHistory uses save(), that @CachePut won't directly invalidate this list.
    // Consider using @CacheEvict(value="ticketHistoryByTicketId", key="#ticketId") on addHistory
    // or on save if the history is always associated with a ticket.
    @Cacheable(value = "ticketHistoryByTicketId", key = "#ticketId")
    public List<TicketHistory> findByTicket_TicketId(Long ticketId) {
        System.out.println("Fetching TicketHistory by Ticket ID from DB or Cache: " + ticketId);
        return ticketHistoryRepository.findByTicket_ticketId(ticketId);
    }

    @Cacheable(value = "ticketHistoryByActionByUserId", key = "#actionByUserId")
    public List<TicketHistory> findByActionBy_userId(Long actionByUserId) {
        System.out.println("Fetching TicketHistory by ActionBy User ID from DB or Cache: " + actionByUserId);
        return ticketHistoryRepository.findByActionBy_userId(actionByUserId);
    }

    @Cacheable(value = "ticketHistoryByAction", key = "#action")
    public List<TicketHistory> findByAction(String action) {
        System.out.println("Fetching TicketHistory by Action from DB or Cache: " + action);
        return ticketHistoryRepository.findByAction(action);
    }

    @CacheEvict(value = {"ticketHistoryByTicketId", "allTicketHistory"}, allEntries = true) // Invalidate relevant caches
    // Consider adding @CachePut if you want to cache the *newly added* single history item by its ID immediately
    public TicketHistory addHistory(Long ticketId, String action, Long actionByUserId, String comments) {
        System.out.println("Adding new TicketHistory for Ticket ID: " + ticketId);
        Optional<Ticket> ticketOptional = ticketService.findById(ticketId);
        Optional<User> userOptional = userService.findById(actionByUserId);

        if (ticketOptional.isPresent() && userOptional.isPresent()) {
            Ticket ticket = ticketOptional.get();
            User actionBy = userOptional.get();

            TicketHistory newHistory = new TicketHistory();
            newHistory.setTicket(ticket);
            newHistory.setAction(action);
            newHistory.setActionBy(actionBy);
            newHistory.setComments(comments);

            // The save method is already @CachePut-annotated, so it will update the cache for the individual item.
            // However, it won't automatically update or evict lists.
            TicketHistory savedHistory = ticketHistoryRepository.save(newHistory);
            
            // Explicitly evict the list caches that might contain this new history
            // This is crucial because addHistory modifies the underlying data for these lists.
            evictTicketHistoryCaches(ticketId, actionByUserId, action); // Custom eviction logic
            
            return savedHistory;
        }
        return null; // Or throw an exception if ticket or user not found
    }

    // A helper method to perform targeted cache evictions after an addHistory operation.
    // This addresses the issue of list caches not being automatically updated by @CachePut on 'save'.
    @CacheEvict(value = {"ticketHistoryByTicketId", "ticketHistoryByActionByUserId", "ticketHistoryByAction", "allTicketHistory"}, allEntries = true)
    public void evictTicketHistoryCaches(Long ticketId, Long actionByUserId, String action) {
        // This method exists purely to trigger cache eviction via annotations.
        // The 'allEntries = true' on "allTicketHistory" will clear that entire cache.
        // For "ticketHistoryByTicketId", "ticketHistoryByActionByUserId", and "ticketHistoryByAction",
        // we're using allEntries = true for simplicity, but in a highly optimized scenario,
        // you might want to use specific keys (e.g., key = "#ticketId") if you only expect
        // a small subset of these lists to change often.
        // However, given that history is generally appended, invalidating all entries
        // for these lists and letting them repopulate on next access is often acceptable.
        System.out.println("Evicting relevant TicketHistory caches due to new history entry.");
    }
}