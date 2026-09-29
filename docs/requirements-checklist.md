# Checklist requisiti

| Requisito | Implementazione / verifica | Stato |
| --- | --- | --- |
| Quattro servizi e responsabilita' separate | Moduli Maven `identity-service`, `course-service`, `participant-service`, `enrollment-service` | Implementato |
| Java 21, Spring Boot 3, JPA, MySQL | POM e properties di ciascun servizio | Implementato |
| Login, password cifrate, ruoli e protezione API | Identity JWT/BCrypt e SecurityFilterChain dei servizi | Login admin verificato; API utenti con token OK e senza token 401; estendere i test automatici di autorizzazione |
| CRUD utenti e corsi | REST API Identity e Course; form frontend con modifica, stato e ruolo | Implementato |
| CRUD, ricerca e disattivazione partecipanti | REST API Participant e query per cognome/codice fiscale/e-mail; form frontend | Implementato |
| Iscrizioni, stati, duplicati e capienza | Enrollment API; UI elenco e aggiornamento stato | Implementato |
| Presenze, assenze, ore e frequenza | Attendance API e validazioni; soglia da `ATTENDANCE_MINIMUM_THRESHOLD` | Backend implementato; verificare i casi e i report in demo |
| Dashboard: corsi, partecipanti, iscrizioni, frequenza, capienza, rischio | `frontend/app.js` e API Enrollment | Implementato per amministratore/tutor; dati limitati ai permessi del docente |
| Frontend end-to-end | `frontend/index.html`, `app.js`, `relations.js` | Login/UI serviti; CRUD, iscrizione e presenza collegati; percorso con dati reali da provare manualmente; test browser automatizzati assenti |
| Validazione, DTO, errori centralizzati | DTO `@Valid` e `@RestControllerAdvice` nei servizi | Implementato |
| OpenAPI | Dipendenza springdoc e configurazione per ciascun servizio | Implementato; `/v3/api-docs` verificato su tutti e quattro i servizi |
| Docker e Compose | Dockerfile servizi, Nginx frontend, `docker-compose.yml` e SQL di bootstrap | Build e avvio Compose verificati; MySQL healthy e sei container attivi |
| README e istruzioni demo | `README.md` e `docs/user-manual.md` | Redatti; prova da ambiente pulito ancora richiesta |
| Diagrammi architettura e dati | `docs/architecture.md`, `docs/data-model.md` | Redatti; confrontare con la release finale |
| Test automatici | 23 test Maven esistenti | Passati nell'ambiente di sviluppo; ampliare copertura di security e integrazione |
| Git con branch e commit di feature | Cronologia attuale del repository | Non verificato/da curare dal gruppo secondo le indicazioni del docente |
| Presentazione e relazione AI | `docs/presentation.md`, `docs/ai-usage.md` | Bozze redatte; completare dati del gruppo e approvazione |

La checklist descrive il codice osservato al momento della redazione. Prima della consegna, ripetere i test, verificare Compose, aggiornare gli stati e non dichiarare completati elementi non provati sul commit finale.