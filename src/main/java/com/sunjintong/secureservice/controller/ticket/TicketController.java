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
import java.util.List;
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

    @GetMapping("/me")
    public Result<List<RecentTicketResponse>> listRecentTickets(
            @AuthenticationPrincipal AuthPrincipal principal) {
        List<RecentTicketResponse> response = ticketService.listRecentTickets(principal.userId())
                .stream()
                .map(TicketController::toRecentTicketResponse)
                .toList();
        return Result.ok(response);
    }

    private static RecentTicketResponse toRecentTicketResponse(Ticket ticket) {
        String summary = ticket.getDraft().replaceAll("\\s+", " ").strip();
        int length = Math.min(240, summary.codePointCount(0, summary.length()));
        int end = summary.offsetByCodePoints(0, length);
        return new RecentTicketResponse(
                ticket.getId(), ticket.getStatus(), ticket.getCreatedAt(), summary.substring(0, end));
    }

    public record RecentTicketResponse(Long ticketId, String status, Instant createdAt, String summary) {
    }

    public record SubmitTicketRequest(
            @NotNull UUID requestId,
            @NotBlank @Size(max = 4000) String draft) {
    }

    public record TicketResponse(Long ticketId, String status, Instant createdAt) {
    }
}