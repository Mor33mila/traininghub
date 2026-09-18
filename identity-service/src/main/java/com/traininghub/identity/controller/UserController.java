package com.traininghub.identity.controller;

import com.traininghub.identity.dto.UserRequest;
import com.traininghub.identity.dto.UserResponse;
import com.traininghub.identity.dto.UserStatusRequest;
import com.traininghub.identity.dto.UserRoleRequest;
import com.traininghub.identity.mapper.UserMapper;
import com.traininghub.identity.service.UserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService service;
    private final UserMapper mapper;

    /** Crea l'adapter REST con servizio utenti e mapper. */
    public UserController(UserService service, UserMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @PostMapping
    /** Gestisce la creazione del profilo utente e restituisce HTTP 201. */
    public ResponseEntity<UserResponse> create(@Valid @RequestBody UserRequest request) {
        return ResponseEntity.status(201).body(mapper.toResponse(service.create(request)));
    }

    @GetMapping
    /** Gestisce la ricerca paginata degli utenti. */
    public org.springframework.data.domain.Page<UserResponse> search(
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100),
                Sort.by("lastName").ascending());
        return service.search(query, pageable).map(mapper::toResponse);
    }

    @GetMapping("/{id}")
    /** Gestisce la lettura del dettaglio di un utente. */
    public UserResponse getById(@PathVariable UUID id) {
        return mapper.toResponse(service.getById(id));
    }

    @PutMapping("/{id}")
    /** Gestisce l'aggiornamento completo del profilo utente. */
    public UserResponse update(@PathVariable UUID id, @Valid @RequestBody UserRequest request) {
        return mapper.toResponse(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    /** Gestisce la disattivazione logica e restituisce HTTP 204. */
    public ResponseEntity<Void> deactivate(@PathVariable UUID id) {
        service.deactivate(id);
        return ResponseEntity.noContent().build();
    }

    /** Gestisce l'attivazione o disattivazione esplicita di un utente. */
    @PatchMapping("/{id}/status")
    public UserResponse updateStatus(@PathVariable UUID id,
                                     @Valid @RequestBody UserStatusRequest request) {
        return mapper.toResponse(service.setActive(id, request.active()));
    }

    /** Gestisce il cambio ruolo dell'utente da parte dell'amministratore. */
    @PatchMapping("/{id}/role")
    public UserResponse updateRole(@PathVariable UUID id,
                                   @Valid @RequestBody UserRoleRequest request) {
        return mapper.toResponse(service.setRole(id, request.role()));
    }
}