# TrainingHub

TrainingHub e' una piattaforma web per gestire utenti, corsi, partecipanti, iscrizioni e presenze. Il backend e' composto da quattro microservizi Spring Boot; il frontend e' HTML, CSS e JavaScript senza build step.

## Requisiti

- Docker Desktop con Docker Compose v2, per l'avvio consigliato.
- In alternativa: Java 21, MySQL 8.x e PowerShell; ogni servizio include il Maven Wrapper.

## Avvio con Docker Compose

Dalla directory principale del repository:

```powershell
Copy-Item .env.example .env
docker compose up --build
```

La prima compilazione scarica le immagini e le dipendenze Maven. Attendere che i quattro servizi Spring siano avviati, poi aprire <http://localhost:8088>. L'healthcheck attende MySQL prima di avviare i servizi applicativi. Il frontend nel browser chiama le API pubblicate sulle porte 8080-8083. MySQL resta accessibile solo sulla rete interna Compose e non occupa una porta Windows.

Credenziali locali iniziali: `admin` / `TrainingHubAdmin123!`. Il valore iniziale e' solo per sviluppo e va cambiato prima di qualsiasi esposizione non locale. Le variabili si configurano nel file `.env`; non committare il file.

Per arrestare i container usare `Ctrl+C` e `docker compose down`. I dati sono conservati nel volume `mysql-data`. Per ricreare i database da zero, solo quando si desidera cancellare i dati locali, usare `docker compose down -v` e poi ripetere l'avvio. Lo script `database/init/01-create-databases.sql` crea i quattro database; Hibernate crea/aggiorna le tabelle all'avvio (`ddl-auto=update`).

## Avvio locale

1. Avviare MySQL 8 e creare i database `traininghub_identity`, `traininghub_courses`, `traininghub_participants` e `traininghub_enrollments` eseguendo `database/init/01-create-databases.sql`.
2. In ciascun terminale PowerShell, impostare le variabili necessarie. Esempio per identity-service:

```powershell
$env:DB_PASSWORD = 'password-mysql'
$env:JWT_SECRET = 'dHJhaW5pbmdodWItZGVtby1qd3Qtc2VjcmV0LTIwMjY='
$env:ADMIN_PASSWORD = 'TrainingHubAdmin123!'
Set-Location .\identity-service
.\mvnw.cmd spring-boot:run
```

3. Ripetere per `course-service`, `participant-service` ed `enrollment-service`, mantenendo lo stesso `JWT_SECRET` e `DB_PASSWORD`. Per enrollment impostare anche `COURSE_SERVICE_URL=http://localhost:8081` e `PARTICIPANT_SERVICE_URL=http://localhost:8082`. Le porte predefinite sono rispettivamente 8081, 8082 e 8083.
4. Servire il frontend sulla porta CORS consentita, ad esempio da un quinto terminale alla root:

```powershell
python -m http.server 5500 --directory frontend
```

Aprire <http://localhost:5500>.

## Flusso demo

1. Accedere come amministratore.
2. Creare un corso con date coerenti, capienza e ore totali positive.
3. Creare un partecipante.
4. In Iscrizioni, selezionare corso e partecipante e creare l'iscrizione.
5. Nell'elenco iscrizioni dello stesso corso, impostare lo stato su `CONFIRMED`.
6. In Presenze, scegliere corso e iscrizione confermata, selezionare data e registrare entrata/uscita. Per un'assenza selezionare la casella dedicata e inserire facoltativamente la giustificazione.
7. Tornare alla Panoramica per vedere conteggi, disponibilita' e frequenze aggiornate.

Il primo amministratore e' creato all'avvio da `ADMIN_USERNAME`, `ADMIN_PASSWORD` e `ADMIN_EMAIL`. Altri dati demo si possono inserire dalla UI seguendo la procedura sopra.

## Servizi e API

| Servizio | Porta | Responsabilita' | Swagger UI |
| --- | ---: | --- | --- |
| Identity | 8080 | Utenti, login JWT, ruoli | `/swagger-ui/index.html` |
| Course | 8081 | Corsi, stati, capienza e docenti | `/swagger-ui/index.html` |
| Participant | 8082 | Anagrafica e ricerca partecipanti | `/swagger-ui/index.html` |
| Enrollment | 8083 | Iscrizioni, presenze e frequenza | `/swagger-ui/index.html` |
| Frontend | 8088 (Compose) / 5500 (locale) | Interfaccia web | `/` |

Gli endpoint OpenAPI sono disponibili su `/v3/api-docs` per ciascun servizio. Le porte applicative sono pubblicate sul loopback; non esporre questo setup demo in rete senza rivedere autenticazione, segreti, CORS e credenziali MySQL.

## Configurazione

Le configurazioni Spring leggono le variabili d'ambiente; `.env.example` documenta i valori per Compose. `ATTENDANCE_MINIMUM_THRESHOLD` imposta la soglia di frequenza (predefinita 80%). La chiave JWT e' una stringa Base64 condivisa da tutti i servizi.

## Test

Eseguire in ogni directory di servizio:

```powershell
.\mvnw.cmd test
```

I test usano H2 e non richiedono un MySQL attivo. Il frontend non ha ancora una suite automatizzata browser; la demo end-to-end e' descritta in `docs/user-manual.md`.

## Documentazione di consegna

- `docs/architecture.md`: servizi, comunicazioni e decisioni architetturali.
- `docs/data-model.md`: modello dati e vincoli.
- `docs/project-analysis.md` e `docs/backlog.md`: analisi e storie di lavoro.
- `docs/project-diary.md`: modello da compilare con le attivita' reali del gruppo.
- `docs/user-manual.md`: guida operativa e verifica della demo.
- `docs/requirements-checklist.md`: mappatura dei requisiti al codice e ai test.
- `docs/ai-usage.md`: registro dell'assistenza AI da rivedere e approvare dal gruppo.
- `docs/presentation.md`: scaletta per la presentazione tecnica.

Diagrammi e documenti sono sorgenti Markdown: il gruppo deve verificare che rappresentino la release finale, aggiungere i nomi dei corsisti e provare la procedura da un ambiente pulito prima della consegna.