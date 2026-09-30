# Checklist requisiti

| Requisito | Implementazione / verifica | Stato |
| --- | --- | --- |
| Quattro servizi e responsabilita' separate | Moduli Maven `identity-service`, `course-service`, `participant-service`, `enrollment-service` | Implementato |
| Java 21, Spring Boot 3, JPA, MySQL | POM e properties di ciascun servizio | Implementato |
| Login, password cifrate, ruoli e protezione API | Identity JWT/BCrypt e SecurityFilterChain dei servizi | Login admin verificato; API utenti con token OK e senza token 401; estendere i test automatici di autorizzazione |
| CRUD utenti e corsi | REST API Identity e Course; form frontend con modifica, stato e ruolo | Implementato |
| CRUD, ricerca avanzata e disattivazione partecipanti | REST API Participant e UI con ricerca per cognome/codice fiscale/e-mail, stato, formazione e nascita | Implementato; filtri manualmente verificati |
| Ricerca e filtri avanzati corsi | Ricerca testuale e filtri per stato, modalita', area, periodo, capienza e ore in `frontend/app.js` | Implementato; combinazione/reset verificati |
| Iscrizioni, stati, duplicati e capienza | Enrollment API; UI elenco e aggiornamento stato | Implementato |
| Calendario lezioni per ruolo | API CRUD lezioni, calendario mensile e filtro sui corsi assegnati ai docenti | Implementato; test API/runtime e regole servizio verificati |
| Presenze, assenze, ore e frequenza | Attendance API e validazioni; soglia da `ATTENDANCE_MINIMUM_THRESHOLD` | Backend implementato; verificare i casi e i report in demo |
| Dashboard con grafici | `frontend/app.js`, Chart.js e dati dei servizi | Grafici corsi, iscrizioni e fasce frequenza verificati nel browser |
| Audit modifiche | `AuditController`, `AuditService`, tabella `audit_events` e pagina dedicata | Ricerca/filtri azione, risorsa e date verificati; limitato alle mutazioni inviate dalla UI |
| Esportazione dati | Pagina frontend per PDF, Excel, CSV e JSON | Tutti i formati verificati; CSV multipli generano uno ZIP; solo admin |
| Frontend end-to-end | `frontend/index.html`, `app.js`, `relations.js` | Login, filtri, dashboard, audit e download provati manualmente; suite browser automatizzata assente |
| Validazione, DTO, errori centralizzati | DTO `@Valid` e `@RestControllerAdvice` nei servizi | Implementato |
| OpenAPI | Dipendenza springdoc e configurazione per ciascun servizio | Implementato; `/v3/api-docs` verificato su tutti e quattro i servizi |
| Docker e Compose | Dockerfile servizi, Nginx frontend, `docker-compose.yml` e SQL di bootstrap | Build e avvio Compose verificati il 30/09/2026; sei container attivi e MySQL healthy |
| README e manuale demo | `README.md` e `docs/user-manual.md` | Aggiornati e verificati nell'ambiente corrente; prova indipendente da ambiente pulito ancora richiesta |
| Diagrammi architettura e dati | `docs/architecture.md`, `docs/data-model.md` | Aggiornati per Identity audit e responsabilita' dei servizi |
| Documentazione API | Swagger UI e `/v3/api-docs` per i quattro servizi | Tutti i quattro endpoint OpenAPI hanno risposto HTTP 200 |
| Credenziali demo | `.env.example` e istruzioni README | Utente e password iniziali indicati; solo per sviluppo locale |
| Test automatici | 30 test Maven: Identity 7, Course 8, Participant 3, Enrollment 12 | Tutte le suite superate nell'ambiente corrente |
| Git con branch e commit di feature | Cronologia attuale del repository | Non verificato/da curare dal gruppo secondo le indicazioni del docente |
| Presentazione e relazione AI | `docs/presentation.md`, `docs/ai-usage.md` | Bozze redatte; completare dati del gruppo e approvazione |

## Checklist consegna

- [ ] **Repository aggiornato** — i documenti aggiornati sono nel worktree; il gruppo deve revisionarli, committarli e sincronizzare il repository remoto.
- [x] **Docker Compose funzionante** — build completato, sei container attivi e MySQL healthy nell'ambiente corrente.
- [x] **Diagrammi aggiornati** — sorgenti Mermaid in architettura e modello dati allineate alle funzioni presenti.
- [x] **Relazione AI presente** — `docs/ai-usage.md` aggiornata; lettura e approvazione spettano al gruppo.
- [ ] **README verificato da un altro corsista** — registrare qui nome e data dopo la revisione indipendente.
- [x] **Documentazione API disponibile** — `/v3/api-docs` verificato HTTP 200 sui quattro servizi.
- [x] **Credenziali demo indicate** — README e `.env.example` riportano i valori locali predefiniti.
- [ ] **Presentazione pronta** — scaletta aggiornata; aggiungere nomi/ruoli dei relatori e provare il flusso completo.

Questa checklist descrive verifiche reali dell'ambiente corrente, non sostituisce una prova da installazione pulita. Non segnare come svolte le attivita' che richiedono revisione o approvazione del gruppo.