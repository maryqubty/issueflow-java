package com.att.tdp.issueflow.controller;

import com.att.tdp.issueflow.dto.request.CreateTicketRequest;
import com.att.tdp.issueflow.dto.request.UpdateTicketRequest;
import com.att.tdp.issueflow.dto.response.ImportResultResponse;
import com.att.tdp.issueflow.dto.response.TicketResponse;
import com.att.tdp.issueflow.service.TicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    @GetMapping
    public ResponseEntity<List<TicketResponse>> getByProject(@RequestParam Long projectId) {
        return ResponseEntity.ok(ticketService.getByProject(projectId));
    }

    @GetMapping("/deleted")
    public ResponseEntity<List<TicketResponse>> getDeleted(@RequestParam Long projectId) {
        return ResponseEntity.ok(ticketService.getDeleted(projectId));
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportCsv(@RequestParam Long projectId) throws IOException {
        byte[] csv = ticketService.exportCsv(projectId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"tickets.csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv);
    }

    @GetMapping("/{ticketId}")
    public ResponseEntity<TicketResponse> getById(@PathVariable Long ticketId) {
        return ResponseEntity.ok(ticketService.getById(ticketId));
    }

    @PostMapping
    public ResponseEntity<TicketResponse> create(@Valid @RequestBody CreateTicketRequest req) {
        return ResponseEntity.ok(ticketService.create(req));
    }

    @PatchMapping("/{ticketId}")
    public ResponseEntity<Void> update(@PathVariable Long ticketId, @RequestBody UpdateTicketRequest req) {
        ticketService.update(ticketId, req);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{ticketId}")
    public ResponseEntity<Void> softDelete(@PathVariable Long ticketId) {
        ticketService.softDelete(ticketId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{ticketId}/restore")
    public ResponseEntity<Void> restore(@PathVariable Long ticketId) {
        ticketService.restore(ticketId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/import")
    public ResponseEntity<ImportResultResponse> importCsv(
            @RequestParam("file") MultipartFile file,
            @RequestParam Long projectId) throws IOException {
        return ResponseEntity.ok(ticketService.importCsv(file, projectId));
    }
}
