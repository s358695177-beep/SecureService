package com.sunjintong.secureservice.controller.ticket;

import com.sunjintong.secureservice.common.Result;
import com.sunjintong.secureservice.common.security.AuthPrincipal;
import com.sunjintong.secureservice.entity.ticket.Ticket;
import com.sunjintong.secureservice.service.ticket.TicketService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/tickets")
public class TicketController {
    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @PostMapping
    public ResponseEntity<Result<TicketResponse>> submit(
            @AuthenticationPrincipal AuthPrincipal principal,
            @Valid @RequestBody SubmitTicketRequest request) {
        Ticket ticket = ticketService.submit(principal.userId(), request.requestId(), request.draft());
        if (!ticket.getDraft().equals(request.draft())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        TicketResponse response = new TicketResponse(
                ticket.getId(), ticket.getStatus(), ticket.getCreatedAt());
        return ResponseEntity.status(HttpStatus.CREATED).body(Result.ok(response));
    }

    public record SubmitTicketRequest(
            @NotNull UUID requestId,
            @NotBlank @Size(max = 4000) String draft) {
    }

    public record TicketResponse(Long ticketId, String status, Instant createdAt) {
    }
}
