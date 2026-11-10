package com.sunjintong.secureservice.repository;

import com.sunjintong.secureservice.entity.ticket.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findTop5ByReporterIdOrderByCreatedAtDescIdDesc(Long reporterId);

    Optional<Ticket> findByReporterIdAndRequestId(Long reporterId, String requestId);
}
