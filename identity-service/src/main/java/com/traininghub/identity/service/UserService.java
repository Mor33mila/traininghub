package com.traininghub.identity.service;

import com.traininghub.identity.dto.UserRequest;
import com.traininghub.identity.entity.UserRole;
import com.traininghub.identity.entity.User;
import com.traininghub.identity.exception.DuplicateUserException;
import com.traininghub.identity.exception.UserNotFoundException;
import com.traininghub.identity.mapper.UserMapper;
import com.traininghub.identity.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Service
public class UserService {
    private final UserRepository repository;
    private final UserMapper mapper;
    private final PasswordEncoder passwordEncoder;

    /** Crea il servizio utenti con repository e mapper dei DTO. */
    public UserService(UserRepository repository, UserMapper mapper, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.mapper = mapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    /** Normalizza, verifica l'unicita' e salva un nuovo profilo utente. */
    public User create(UserRequest request) {
        String username = request.getUsername().trim().toLowerCase();
        String email = request.getEmail().trim().toLowerCase();
        if (repository.existsByUsernameIgnoreCase(username)) throw new DuplicateUserException("username");
        if (repository.existsByEmailIgnoreCase(email)) throw new DuplicateUserException("email");
        User user = mapper.toEntity(request);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        return repository.save(user);
    }

    @Transactional(readOnly = true)
    /** Carica un utente oppure solleva l'eccezione di risorsa non trovata. */
    public User getById(UUID id) {
        return repository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
    }

    @Transactional(readOnly = true)
    /** Cerca gli utenti per username, cognome o e-mail con paginazione. */
    public Page<User> search(String query, Pageable pageable) {
        if (query == null || query.isBlank()) return repository.findAll(pageable);
        String value = query.trim();
        return repository.findByUsernameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                value, value, value, pageable);
    }

    @Transactional
    /** Aggiorna il profilo mantenendo l'unicita' di username ed e-mail. */
    public User update(UUID id, UserRequest request) {
        User user = getById(id);
        String username = request.getUsername().trim().toLowerCase();
        String email = request.getEmail().trim().toLowerCase();
        if (repository.existsByUsernameIgnoreCaseAndIdNot(username, id))
            throw new DuplicateUserException("username");
        if (repository.existsByEmailIgnoreCaseAndIdNot(email, id))
            throw new DuplicateUserException("email");
        mapper.updateEntity(user, request);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        return repository.save(user);
    }

    @Transactional
    /** Contrassegna l'utente come inattivo senza rimuovere il record d'identita'. */
    public void deactivate(UUID id) {
        setActive(id, false);
    }

    /** Aggiorna lo stato attivo dell'utente senza cancellarne l'identita'. */
    @Transactional
    public User setActive(UUID id, boolean active) {
        User user = getById(id);
        user.setActive(active);
        repository.save(user);
        return user;
    }

    /** Cambia il ruolo senza richiedere una nuova password. */
    @Transactional
    public User setRole(UUID id, UserRole role) {
        User user = getById(id);
        user.setRole(role);
        return repository.save(user);
    }
}