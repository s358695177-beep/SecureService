package com.sunjintong.secureservice.service.ticket;

import com.sunjintong.secureservice.entity.ticket.Ticket;
import com.sunjintong.secureservice.repository.TicketRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class TicketService {
    private final TicketRepository tickets;

    public TicketService(TicketRepository tickets) {
        this.tickets = tickets;
    }

    public Ticket submit(Long reporterId, UUID requestId, String draft) {
        String key = requestId.toString();
        return tickets.findByReporterIdAndRequestId(reporterId, key)
                .orElseGet(() -> createTicket(reporterId, key, draft));
    }

    private Ticket createTicket(Long reporterId, String requestId, String draft) {
        try {
            return tickets.saveAndFlush(new Ticket(reporterId, requestId, draft));
        } catch (DataIntegrityViolationException exception) {
            return tickets.findByReporterIdAndRequestId(reporterId, requestId)
                    .orElseThrow(() -> exception);
        }
    }
}
