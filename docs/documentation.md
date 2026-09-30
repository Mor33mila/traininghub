# TrainingHub

> **Piattaforma web per la gestione di percorsi formativi, partecipanti,
> iscrizioni e presenze.**

TrainingHub è un'applicazione web progettata secondo un'architettura **a
microservizi**. Il sistema permette di gestire l'intero ciclo di vita di
un percorso formativo: dalla gestione degli utenti e dei corsi fino alle
iscrizioni, alla pianificazione delle lezioni, alla registrazione delle
presenze e all'analisi della frequenza.

Il progetto è composto da **quattro microservizi Spring Boot**, ciascuno
responsabile di un dominio funzionale, e da un **frontend web in HTML,
CSS e JavaScript**. Ogni microservizio possiede il proprio database
MySQL e comunica con gli altri servizi tramite API REST quando è
necessario recuperare informazioni appartenenti a un dominio differente.

------------------------------------------------------------------------

## Indice

-   [Panoramica](#panoramica)
-   [Funzionalità](#funzionalità)
-   [Architettura](#architettura)
-   [Tecnologie](#tecnologie)
-   [Struttura del progetto](#struttura-del-progetto)
-   [Microservizi](#microservizi)
-   [Modello dati](#modello-dati)
-   [Autenticazione e autorizzazioni](#autenticazione-e-autorizzazioni)
-   [Frontend](#frontend)
-   [API principali](#api-principali)
-   [Avvio con Docker](#avvio-con-docker)
-   [Avvio in locale](#avvio-in-locale)
-   [Configurazione](#configurazione)
-   [Test](#test)
-   [Documentazione API](#documentazione-api)
-   [Flusso demo](#flusso-demo)
-   [Struttura del codice](#struttura-del-codice)
-   [Considerazioni tecniche](#considerazioni-tecniche)
-   [Documentazione aggiuntiva](#documentazione-aggiuntiva)

------------------------------------------------------------------------

## Panoramica

TrainingHub centralizza la gestione di un ente che organizza attività
formative.

Il sistema permette di:

-   autenticare gli utenti e gestirne i ruoli;
-   creare e gestire corsi;
-   assegnare docenti ai corsi;
-   gestire l'anagrafica dei partecipanti;
-   creare e gestire iscrizioni;
-   controllare la capienza dei corsi;
-   pianificare le lezioni;
-   registrare presenze e assenze;
-   calcolare la frequenza dei partecipanti;
-   visualizzare dashboard e statistiche;
-   applicare filtri e ricerche avanzate;
-   mantenere uno storico delle modifiche effettuate dalla UI;
-   esportare i dati in diversi formati.

L'obiettivo architetturale è mantenere separati i diversi domini
applicativi, evitando che un servizio debba conoscere direttamente le
entità persistite da un altro servizio.

------------------------------------------------------------------------

## Funzionalità

### Gestione utenti

Il sistema permette agli amministratori di:

-   creare utenti;
-   visualizzare utenti;
-   modificare i dati degli utenti;
-   modificare ruolo e stato;
-   disattivare utenti;
-   autenticarsi tramite username e password.

I ruoli applicativi sono:

  -----------------------------------------------------------------------
  Ruolo                               Responsabilità
  ----------------------------------- -----------------------------------
  **ADMINISTRATOR (Amministratore)** Gestione completa del sistema,
                                      utenti, audit ed esportazioni

  **TUTOR**                           Gestione operativa di corsi,
                                      iscrizioni, lezioni e presenze

  **TEACHER (Docente)**               Consultazione dei corsi assegnati e
                                      delle relative informazioni
  -----------------------------------------------------------------------

### Gestione corsi

Per ogni corso possono essere gestiti:

-   codice univoco;
-   titolo;
-   ore totali;
-   data di inizio;
-   data di fine;
-   capienza massima;
-   modalità;
-   stato;
-   docente assegnato.

Sono presenti controlli di validazione sulle date, sulle ore e sulla
capienza.

### Gestione partecipanti

L'anagrafica permette di gestire:

-   nome e cognome;
-   codice fiscale;
-   data di nascita;
-   e-mail;
-   telefono;
-   livello di istruzione;
-   stato occupazionale;
-   stato attivo/disattivo.

Codice fiscale ed e-mail sono gestiti come valori univoci.

### Iscrizioni

Il modulo iscrizioni collega partecipanti e corsi.

Sono gestiti:

-   data di iscrizione;
-   corso;
-   partecipante;
-   stato dell'iscrizione;
-   controllo dei duplicati;
-   controllo della capienza del corso.

Le iscrizioni attive occupano i posti disponibili secondo le regole del
servizio.

### Lezioni

Amministratori e tutor possono:

-   creare una lezione;
-   modificarla;
-   eliminarla;
-   visualizzare le lezioni associate a un corso;
-   utilizzare il calendario per consultare gli appuntamenti.

La data della lezione deve appartenere al periodo del corso e l'orario
di fine deve essere successivo all'orario di inizio.

### Presenze

Per le iscrizioni ammesse possono essere registrate:

-   presenza;
-   assenza;
-   ora di entrata;
-   ora di uscita;
-   ore frequentate;
-   eventuale giustificazione.

Il sistema calcola la percentuale di frequenza e permette di individuare
i partecipanti sotto la soglia configurata.

La soglia predefinita è **80%** e può essere modificata tramite
`ATTENDANCE_MINIMUM_THRESHOLD`.

### Dashboard

La dashboard visualizza indicatori e grafici relativi a:

-   corsi per stato;
-   iscrizioni per stato;
-   distribuzione dei partecipanti per fascia di frequenza;
-   frequenza media;
-   posti disponibili;
-   partecipanti sotto soglia.

### Ricerca e filtri

La UI supporta filtri combinabili per corsi e partecipanti.

Per i corsi sono disponibili, tra gli altri:

-   codice/titolo;
-   stato;
-   modalità;
-   area formativa;
-   intervallo temporale;
-   capienza;
-   ore minime.

Per i partecipanti:

-   cognome;
-   codice fiscale;
-   e-mail;
-   stato attivo;
-   livello di istruzione;
-   stato occupazionale;
-   intervallo di nascita.

### Audit

Le operazioni di modifica effettuate dalla UI possono essere registrate
nell'audit.

Un evento contiene:

-   attore;
-   azione;
-   risorsa;
-   identificativo della risorsa;
-   timestamp.

L'audit non conserva il payload o una copia completa del record.

### Esportazioni

Gli amministratori possono esportare i dataset disponibili in:

-   PDF;
-   Excel;
-   CSV;
-   JSON.

Quando vengono selezionati più dataset per l'esportazione CSV, il
frontend crea un archivio ZIP contenente un CSV per ciascun dataset.

------------------------------------------------------------------------

# Architettura

## Vista generale

``` mermaid
flowchart TB
    Browser["Frontend<br/>HTML / CSS / JavaScript"]

    Identity["Identity Service<br/>:8080"]
    Course["Course Service<br/>:8081"]
    Participant["Participant Service<br/>:8082"]
    Enrollment["Enrollment Service<br/>:8083"]

    DBIdentity[("MySQL<br/>traininghub_identity")]
    DBCourse[("MySQL<br/>traininghub_courses")]
    DBParticipant[("MySQL<br/>traininghub_participants")]
    DBEnrollment[("MySQL<br/>traininghub_enrollments")]

    Browser -->|"REST + Bearer JWT"| Identity
    Browser -->|"REST + Bearer JWT"| Course
    Browser -->|"REST + Bearer JWT"| Participant
    Browser -->|"REST + Bearer JWT"| Enrollment

    Identity --> DBIdentity
    Course --> DBCourse
    Participant --> DBParticipant
    Enrollment --> DBEnrollment

    Enrollment -->|"REST"| Course
    Enrollment -->|"REST"| Participant
```

### Principio di separazione

Ogni microservizio:

1.  possiede il proprio dominio;
2.  possiede il proprio database;
3.  espone API REST;
4.  non accede direttamente alle tabelle degli altri servizi;
5.  utilizza DTO per i dati scambiati;
6.  mantiene le proprie entity e repository.

Quando Enrollment deve conoscere informazioni relative a un corso o a un
partecipante, utilizza client REST dedicati invece di condividere le
entity JPA degli altri servizi.

------------------------------------------------------------------------

# Tecnologie

## Backend

  Tecnologia                         Utilizzo
  ---------------------------------- ---------------------------------
  **Java 21**                        Linguaggio principale
  **Spring Boot 3.5.5**              Framework backend
  **Spring Web**                     API REST
  **Spring Data JPA**                Persistenza
  **Hibernate**                      ORM
  **Spring Security**                Sicurezza
  **JWT / OAuth2 Resource Server**   Autenticazione e protezione API
  **BCrypt**                         Hashing delle password
  **Bean Validation**                Validazione dei dati
  **Springdoc OpenAPI**              Documentazione API
  **Lombok**                         Riduzione del boilerplate
  **Maven**                          Build e gestione dipendenze
  **MySQL 8.4**                      Database applicativo
  **H2**                             Database per i test

## Frontend

  Tecnologia           Utilizzo
  -------------------- ----------------------------------------
  **HTML5**            Struttura dell'interfaccia
  **CSS3**             Layout e stile
  **JavaScript**       Logica applicativa e chiamate API
  **Chart.js**         Grafici della dashboard
  **jsPDF**            Esportazione PDF
  **SheetJS / XLSX**   Esportazione Excel
  **JSZip**            Archivio ZIP per esportazioni multiple

Le librerie frontend per grafici ed esportazioni vengono caricate
tramite CDN.

## DevOps

-   Docker
-   Docker Compose
-   Nginx
-   MySQL containerizzato

------------------------------------------------------------------------

# Struttura del progetto

``` text
TrainingHub/
│
├── database/
│   └── init/
│       └── 01-create-databases.sql
│
├── identity-service/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/traininghub/identity/
│   │   │   └── resources/
│   │   └── test/
│   ├── Dockerfile
│   └── pom.xml
│
├── course-service/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/traininghub/course/
│   │   │   └── resources/
│   │   └── test/
│   ├── Dockerfile
│   └── pom.xml
│
├── participant-service/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/traininghub/participant/
│   │   │   └── resources/
│   │   └── test/
│   ├── Dockerfile
│   └── pom.xml
│
├── enrollment-service/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/traininghub/enrollment/
│   │   │   └── resources/
│   │   └── test/
│   ├── Dockerfile
│   └── pom.xml
│
├── frontend/
│   ├── index.html
│   ├── app.js
│   ├── users.js
│   ├── relations.js
│   ├── styles.css
│   ├── dashboard.css
│   ├── users.css
│   ├── visual-refresh.css
│   └── Dockerfile
│
├── docs/
│   ├── architecture.md
│   ├── data-model.md
│   ├── project-analysis.md
│   ├── requirements-checklist.md
│   ├── user-manual.md
│   └── ...
│
├── docker-compose.yml
├── .env.example
├── .gitignore
└── README.md
```

------------------------------------------------------------------------

# Microservizi

## Identity Service

**Porta:** `8080`

Responsabilità:

-   login;
-   utenti;
-   ruoli;
-   stato degli utenti;
-   generazione e validazione JWT;
-   gestione password;
-   audit.

Package principali:

``` text
configuration/
controller/
dto/
entity/
exception/
mapper/
repository/
service/
```

### Endpoint principali

``` text
POST   /api/auth/login

POST   /api/users
GET    /api/users
GET    /api/users/{id}
PUT    /api/users/{id}
DELETE /api/users/{id}

PATCH  /api/users/{id}/status
PATCH  /api/users/{id}/role

POST   /api/audit/events
GET    /api/audit/events
```

------------------------------------------------------------------------

## Course Service

**Porta:** `8081`

Responsabilità:

-   gestione dei corsi;
-   stati dei corsi;
-   validazione;
-   capienza;
-   modalità;
-   assegnazione del docente;
-   ricerca dei corsi.

### Endpoint principali

``` text
POST   /api/courses
GET    /api/courses
GET    /api/courses/{id}
GET    /api/courses/status/{status}
PUT    /api/courses/{id}
DELETE /api/courses/{id}
```

------------------------------------------------------------------------

## Participant Service

**Porta:** `8082`

Responsabilità:

-   anagrafica partecipanti;
-   ricerca;
-   validazione;
-   unicità di codice fiscale ed e-mail;
-   attivazione/disattivazione.

### Endpoint principali

``` text
POST   /api/participants
GET    /api/participants
GET    /api/participants/{id}
PUT    /api/participants/{id}
DELETE /api/participants/{id}
```

------------------------------------------------------------------------

## Enrollment Service

**Porta:** `8083`

È il servizio che gestisce il dominio operativo delle iscrizioni e delle
presenze.

Responsabilità:

-   iscrizioni;
-   stati delle iscrizioni;
-   controllo capienza;
-   lezioni;
-   calendario;
-   presenze;
-   assenze;
-   calcolo della frequenza.

Enrollment comunica con Course Service e Participant Service tramite
REST.

### Iscrizioni

``` text
POST   /api/enrollments
GET    /api/enrollments/{id}
GET    /api/enrollments/course/{courseId}
GET    /api/enrollments/participant/{participantId}
PATCH  /api/enrollments/{id}/status
```

### Lezioni

``` text
GET    /api/lessons/course/{courseId}
POST   /api/lessons
PUT    /api/lessons/{id}
DELETE /api/lessons/{id}
```

### Presenze

``` text
POST   /api/attendance
GET    /api/attendance/course/{courseId}
GET    /api/attendance/participant/{participantId}/percentage
```

------------------------------------------------------------------------

# Modello dati

Ogni servizio possiede un database separato.

``` mermaid
erDiagram
    USER ||--o{ COURSE : teaches
    COURSE ||--o{ LESSON : contains
    COURSE ||--o{ ENROLLMENT : receives
    PARTICIPANT ||--o{ ENROLLMENT : has
    ENROLLMENT ||--o{ ATTENDANCE : records

    USER {
        UUID id PK
        string username UK
        string passwordHash
        string firstName
        string lastName
        string email UK
        string role
        boolean active
    }

    COURSE {
        UUID id PK
        string courseCode UK
        string title
        decimal totalHours
        date startDate
        date endDate
        int maximumCapacity
        string mode
        string status
        UUID instructorId
    }

    LESSON {
        UUID id PK
        UUID courseId
        string title
        date lessonDate
        time startTime
        time endTime
        string notes
    }

    PARTICIPANT {
        UUID id PK
        string firstName
        string lastName
        string taxCode UK
        date birthDate
        string email UK
        string phone
        string educationLevel
        string employmentStatus
        boolean active
    }

    ENROLLMENT {
        UUID id PK
        UUID courseId
        UUID participantId
        date enrollmentDate
        string status
    }

    ATTENDANCE {
        UUID id PK
        UUID enrollmentId
        date lessonDate
        time entryTime
        time exitTime
        decimal attendedHours
        boolean absent
        string justification
    }
```

I riferimenti tra servizi sono **riferimenti logici**, non foreign key
MySQL cross-database.

------------------------------------------------------------------------

# Autenticazione e autorizzazioni

Il flusso di autenticazione è basato su JWT.

``` text
Browser
   │
   │ username + password
   ▼
Identity Service
   │
   │ verifica password
   │
   │ genera JWT
   ▼
Browser
   │
   │ Authorization: Bearer <token>
   ▼
API protette
```

Le API dei quattro servizi validano:

-   firma del token;
-   issuer;
-   validità del token;
-   ruolo dell'utente.

Le password vengono memorizzate tramite hashing **BCrypt** e non in
chiaro.

Il frontend conserva il token nel `localStorage` e lo utilizza nelle
successive richieste alle API.

> **Nota:** le credenziali presenti nella configurazione Docker sono
> pensate per lo sviluppo locale. Non utilizzare i valori di default per
> un ambiente pubblico o di produzione.

------------------------------------------------------------------------

# Frontend

Il frontend non utilizza un framework JavaScript con build system: è
costituito da HTML, CSS e JavaScript.

File principali:

  File                   Responsabilità
  ---------------------- -------------------------------------------------
  `index.html`           Struttura dell'applicazione
  `app.js`               Logica principale, API, dashboard e gestione UI
  `users.js`             Gestione utenti
  `relations.js`         Relazioni e funzionalità collegate
  `styles.css`           Stili principali
  `dashboard.css`        Dashboard
  `users.css`            Interfaccia utenti
  `visual-refresh.css`   Personalizzazioni visuali

Le richieste al backend vengono effettuate direttamente dal browser
tramite API REST.

Le librerie esterne vengono caricate da CDN:

``` text
Chart.js
XLSX
jsPDF
jsPDF-AutoTable
JSZip
```

Di conseguenza, alcune funzionalità frontend richiedono una connessione
Internet durante l'esecuzione.

------------------------------------------------------------------------

# API principali

## Porte

  Componente                                      Porta
  -------------------------- --------------------------
  Identity Service                               `8080`
  Course Service                                 `8081`
  Participant Service                            `8082`
  Enrollment Service                             `8083`
  Frontend Docker/Nginx                          `8088`
  Frontend sviluppo locale                       `5500`
  MySQL                        `3306` interno a Compose

## Swagger UI

``` text
http://localhost:8080/swagger-ui/index.html
http://localhost:8081/swagger-ui/index.html
http://localhost:8082/swagger-ui/index.html
http://localhost:8083/swagger-ui/index.html
```

Specificamente:

-   `8080` → Identity
-   `8081` → Course
-   `8082` → Participant
-   `8083` → Enrollment

Gli OpenAPI JSON sono disponibili tramite:

``` text
/v3/api-docs
```

------------------------------------------------------------------------

# Avvio con Docker

## Requisiti

-   Docker Desktop
-   Docker Compose v2

## 1. Configurare l'ambiente

Dalla root del progetto:

``` powershell
if (-not (Test-Path .env)) {
    Copy-Item .env.example .env
}
```

Modificare `.env` se necessario.

**Non committare `.env` nel repository.**

## 2. Avviare l'applicazione

``` powershell
docker compose up --build -d
```

Controllare lo stato:

``` powershell
docker compose ps
```

Il sistema avvia:

-   MySQL;
-   Identity Service;
-   Course Service;
-   Participant Service;
-   Enrollment Service;
-   frontend Nginx.

## 3. Aprire il frontend

``` text
http://localhost:8088
```

## 4. Visualizzare i log

``` powershell
docker compose logs -f identity-service course-service participant-service enrollment-service frontend
```

## 5. Arrestare l'applicazione

``` powershell
docker compose down
```

I dati MySQL vengono mantenuti nel volume Docker.

Per eliminare anche il database:

``` powershell
docker compose down -v
```

> Usare `down -v` solo quando si vuole ricreare completamente
> l'ambiente.

------------------------------------------------------------------------

# Avvio in locale

In alternativa a Docker è possibile avviare manualmente i quattro
servizi.

## Requisiti

-   Java 21;
-   MySQL 8.x;
-   PowerShell;
-   Maven Wrapper incluso nei singoli servizi.

## Database

Eseguire:

``` text
database/init/01-create-databases.sql
```

Vengono creati:

``` text
traininghub_identity
traininghub_courses
traininghub_participants
traininghub_enrollments
```

Le tabelle vengono poi create/aggiornate da Hibernate tramite:

``` properties
spring.jpa.hibernate.ddl-auto=update
```

## Avviare Identity

``` powershell
$env:DB_PASSWORD = 'password-mysql'
$env:JWT_SECRET = 'chiave-base64'
$env:JWT_ISSUER = 'traininghub-identity'
$env:ADMIN_PASSWORD = 'TrainingHubAdmin123!'

Set-Location .\identity-service
.\mvnw.cmd spring-boot:run
```

## Avviare gli altri servizi

Ripetere la configurazione delle variabili d'ambiente e avviare:

``` text
course-service
participant-service
enrollment-service
```

Le porte predefinite sono:

``` text
8081
8082
8083
```

Enrollment utilizza:

``` text
Course Service      → http://localhost:8081
Participant Service → http://localhost:8082
```

## Avviare il frontend

Dalla root:

``` powershell
python -m http.server 5500 --directory frontend
```

Aprire:

``` text
http://localhost:5500
```

------------------------------------------------------------------------

# Configurazione

Le principali configurazioni vengono gestite tramite variabili
d'ambiente.

  Variabile                        Utilizzo
  -------------------------------- ----------------------------------
  `MYSQL_ROOT_PASSWORD`            Password MySQL in Docker
  `DB_URL`                         URL del database
  `DB_USERNAME`                    Utente database
  `DB_PASSWORD`                    Password database
  `JWT_SECRET`                     Chiave HMAC utilizzata per i JWT
  `JWT_ISSUER`                     Issuer dei JWT
  `ADMIN_USERNAME`                 Username amministratore iniziale
  `ADMIN_PASSWORD`                 Password amministratore iniziale
  `ADMIN_EMAIL`                    E-mail amministratore iniziale
  `ATTENDANCE_MINIMUM_THRESHOLD`   Soglia minima di frequenza
  `COURSE_SERVICE_URL`             URL di Course Service
  `PARTICIPANT_SERVICE_URL`        URL di Participant Service

Il valore predefinito della soglia di frequenza è:

``` text
80%
```

------------------------------------------------------------------------

# Test

Ogni microservizio dispone della propria suite di test.

Per eseguire i test di un servizio:

``` powershell
.\mvnw.cmd test
```

I test utilizzano H2 e non richiedono un'istanza MySQL attiva.

La suite documentata comprende **30 test Maven**:

  Servizio          Test
  ------------- --------
  Identity             7
  Course               8
  Participant          3
  Enrollment          12
  **Totale**      **30**

Sono presenti test di:

-   service layer;
-   repository;
-   controller;
-   validazioni;
-   gestione degli errori;
-   regole di dominio.

Il frontend non dispone attualmente di una suite browser automatizzata:
le funzionalità end-to-end vengono verificate tramite la procedura
manuale descritta nella documentazione del progetto.

------------------------------------------------------------------------

# Flusso demo

Una dimostrazione completa può essere eseguita seguendo questo ordine:

``` text
1. Login
      ↓
2. Creazione corso
      ↓
3. Creazione partecipante
      ↓
4. Creazione iscrizione
      ↓
5. Conferma iscrizione
      ↓
6. Creazione lezione
      ↓
7. Registrazione presenza/assenza
      ↓
8. Calcolo frequenza
      ↓
9. Dashboard
      ↓
10. Audit / esportazione
```

### Esempio

1.  Accedere con l'utente amministratore.
2.  Creare un corso con date valide, ore positive e capienza positiva.
3.  Creare un partecipante.
4.  Creare un'iscrizione tra partecipante e corso.
5.  Impostare l'iscrizione su `CONFIRMED`.
6.  Creare una lezione nel periodo del corso.
7.  Registrare una presenza oppure un'assenza.
8.  Consultare la dashboard.
9.  Controllare l'evento nell'audit.
10. Provare un'esportazione dei dati.

------------------------------------------------------------------------

# Struttura del codice

I microservizi seguono una struttura a livelli.

``` text
Controller
    │
    ▼
Service
    │
    ▼
Repository
    │
    ▼
Database
```

Sono inoltre presenti:

``` text
DTO
Mapper
Entity
Exception
Configuration
Security
Integration
```

### Controller

Espongono le API REST e gestiscono le richieste HTTP.

### Service

Contengono la logica applicativa e le regole di dominio.

### Repository

Gestiscono l'accesso ai dati tramite Spring Data JPA.

### Entity

Rappresentano i dati persistiti nel database del relativo servizio.

### DTO

Separano il modello API dal modello di persistenza.

### Mapper

Gestiscono la conversione tra Entity e DTO.

### Exception Handler

Uniformano le risposte di errore tramite `@RestControllerAdvice`.

### Security

Contiene la configurazione Spring Security e la validazione dei JWT.

### Integration

Presente soprattutto in Enrollment, contiene i client REST utilizzati
per comunicare con Course e Participant Service.

------------------------------------------------------------------------

# Considerazioni tecniche

## Database separati

La scelta di utilizzare un database per microservizio permette di
mantenere l'indipendenza dei domini.

Le relazioni tra domini differenti vengono rappresentate tramite UUID e
verificate attraverso API REST.

Non vengono utilizzate foreign key MySQL tra database appartenenti a
servizi differenti.

## Comunicazione tra servizi

Enrollment Service utilizza client REST per ottenere informazioni da:

``` text
Course Service
Participant Service
```

Questo evita l'accoppiamento diretto tra le entity JPA dei diversi
servizi.

## Validazione

La validazione viene effettuata sia a livello API sia nella logica
applicativa.

Tra i controlli presenti:

-   date dei corsi;
-   ore positive;
-   capienza positiva;
-   duplicati;
-   codice fiscale/e-mail univoci;
-   capienza delle iscrizioni;
-   validità delle lezioni;
-   validità delle presenze;
-   autorizzazioni per ruolo.

## Audit

L'audit è pensato come registro operativo delle modifiche effettuate
attraverso la UI.

Non è un sistema anti-manomissione e non registra automaticamente ogni
possibile scrittura effettuata direttamente sulle API da client esterni.

## Migrazioni database

Il progetto utilizza attualmente:

``` properties
spring.jpa.hibernate.ddl-auto=update
```

e uno script SQL iniziale per creare i database.

Per un ambiente produttivo sarebbe preferibile introdurre migrazioni
versionate, ad esempio tramite Flyway o Liquibase.

## Sicurezza

Il progetto è configurato per un ambiente locale/demo.

Prima di un eventuale deployment pubblico sarebbe necessario almeno:

-   utilizzare secret sicuri e non presenti nel repository;
-   sostituire le credenziali predefinite;
-   utilizzare TLS/HTTPS;
-   limitare i privilegi MySQL;
-   rivedere la configurazione CORS;
-   introdurre una gestione sicura dei secret;
-   definire una strategia di migrazione database;
-   aggiungere healthcheck applicativi;
-   valutare una suite E2E automatizzata.

------------------------------------------------------------------------

# Documentazione aggiuntiva

La directory `docs/` contiene documentazione più dettagliata:

  Documento                     Contenuto
  ----------------------------- ------------------------------------------
  `architecture.md`             Architettura, comunicazioni e sicurezza
  `data-model.md`               Modello dati e vincoli
  `project-analysis.md`         Analisi del problema e requisiti
  `requirements-checklist.md`   Mappatura requisiti/implementazione
  `user-manual.md`              Manuale operativo e procedura demo
  `backlog.md`                  Backlog e attività
  `project-diary.md`            Diario del progetto
  `ai-usage.md`                 Registro dell'utilizzo dell'AI
  `presentation.md`             Materiale di supporto alla presentazione

------------------------------------------------------------------------

# Stato del progetto

TrainingHub include attualmente:

-   [x] Architettura a microservizi
-   [x] Identity Service
-   [x] Course Service
-   [x] Participant Service
-   [x] Enrollment Service
-   [x] Autenticazione JWT
-   [x] Gestione ruoli
-   [x] BCrypt
-   [x] Database MySQL separati
-   [x] API REST
-   [x] OpenAPI / Swagger
-   [x] Validazione e gestione centralizzata degli errori
-   [x] Gestione corsi
-   [x] Gestione partecipanti
-   [x] Gestione iscrizioni
-   [x] Calendario lezioni
-   [x] Presenze e frequenza
-   [x] Dashboard
-   [x] Filtri avanzati
-   [x] Audit
-   [x] Esportazioni PDF / Excel / CSV / JSON
-   [x] Docker Compose
-   [x] Test backend
-   [ ] Suite E2E browser automatizzata
-   [ ] Migrazioni database versionate
-   [ ] Configurazione production-ready

------------------------------------------------------------------------

# Autore

**TrainingHub**

Progetto software sviluppato come applicazione web full-stack con
architettura a microservizi, finalizzata alla gestione di percorsi
formativi, partecipanti, iscrizioni e presenze.
