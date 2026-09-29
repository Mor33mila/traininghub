# Modello dati

Ogni microservizio persiste le proprie entita' nel rispettivo database. Le relazioni tra domini distinti sono rappresentate con UUID e controllate tramite API REST.

```mermaid
erDiagram
    USER ||--o{ COURSE : teaches
    COURSE ||--o{ ENROLLMENT : has
    PARTICIPANT ||--o{ ENROLLMENT : joins
    ENROLLMENT ||--o{ ATTENDANCE : records
    USER {
        uuid id PK
        string username UK
        string passwordHash
        string firstName
        string lastName
        string email UK
        string role
        boolean active
    }
    COURSE {
        uuid id PK
        string courseCode UK
        string title
        decimal totalHours
        date startDate
        date endDate
        int maximumCapacity
        string mode
        string status
        uuid instructorId FK
    }
    PARTICIPANT {
        uuid id PK
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
        uuid id PK
        uuid courseId FK
        uuid participantId FK
        date enrollmentDate
        string status
    }
    ATTENDANCE {
        uuid id PK
        uuid enrollmentId FK
        date lessonDate
        time entryTime
        time exitTime
        decimal attendedHours
        boolean absent
        string justification
    }
```

`FK` indica un riferimento logico tra microservizi, non una foreign key MySQL cross-database. Course code, codice fiscale ed e-mail sono univoci. Una sola iscrizione tra `REQUESTED` e `CONFIRMED` e' ammessa per coppia partecipante-corso; le iscrizioni in tali stati occupano capienza. Le ore corso sono positive, le ore presenza non negative, e una lezione deve ricadere nel periodo del corso.

Il file `database/init/01-create-databases.sql` crea i database vuoti. Le tabelle sono create/aggiornate da Hibernate (`ddl-auto=update`); non sono ancora presenti migrazioni versionate.