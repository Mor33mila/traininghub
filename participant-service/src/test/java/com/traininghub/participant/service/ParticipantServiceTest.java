package com.traininghub.participant.service;

import com.traininghub.participant.dto.ParticipantRequest;
import com.traininghub.participant.entity.Participant;
import com.traininghub.participant.exception.DuplicateParticipantException;
import com.traininghub.participant.mapper.ParticipantMapper;
import com.traininghub.participant.repository.ParticipantRepository;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ParticipantServiceTest {
    private final ParticipantRepository repository = mock(ParticipantRepository.class);
    private final ParticipantService service = new ParticipantService(repository, new ParticipantMapper());

    @Test
    void rejectsDuplicateTaxCode() {
        ParticipantRequest request = validRequest();
        when(repository.existsByTaxCodeIgnoreCase("RSSMRA80A01H501Z")).thenReturn(true);
        assertThrows(DuplicateParticipantException.class, () -> service.create(request));
    }

    @Test
    void createsParticipantWithNormalizedValues() {
        ParticipantRequest request = validRequest();
        when(repository.existsByTaxCodeIgnoreCase("RSSMRA80A01H501Z")).thenReturn(false);
        when(repository.existsByEmailIgnoreCase("mario@example.com")).thenReturn(false);
        when(repository.save(any(Participant.class))).thenAnswer(invocation -> invocation.getArgument(0));
        Participant participant = service.create(request);
        assertEquals("RSSMRA80A01H501Z", participant.getTaxCode());
        assertEquals("mario@example.com", participant.getEmail());
        assertEquals(true, participant.getActive());
    }

    private ParticipantRequest validRequest() {
        ParticipantRequest request = new ParticipantRequest();
        request.setFirstName("Mario");
        request.setLastName("Rossi");
        request.setTaxCode("rssmra80a01h501z");
        request.setBirthDate(LocalDate.of(1980, 1, 1));
        request.setEmail("MARIO@EXAMPLE.COM");
        return request;
    }
}