const USER_ROLES = ['ADMINISTRATOR', 'TUTOR', 'TEACHER'];
const IDENTITY_API = 'http://localhost:8080';

function isAdministrator() {
    const user = JSON.parse(localStorage.getItem('traininghub_user') || 'null');
    return user?.roles?.includes('ADMINISTRATOR');
}

function userRoleLabel(role) {
    return { ADMINISTRATOR: 'Amministratore', TUTOR: 'Tutor', TEACHER: 'Docente' }[role] || role;
}

function renderUsers(users) {
    $('user-list').innerHTML = users.length ? users.map(user => `
        <article class="record-card user-record" data-user-id="${user.id}">
            <div>
                <strong>${user.firstName} ${user.lastName}</strong>
                <small>${user.username} · ${user.email}</small>
                <div class="user-controls">
                    <select class="user-role-select" aria-label="Ruolo di ${user.username}">
                        ${USER_ROLES.map(role => `<option value="${role}" ${role === user.role ? 'selected' : ''}>${userRoleLabel(role)}</option>`).join('')}
                    </select>
                    <button class="secondary-button role-save-button" type="button">Salva ruolo</button>
                    <button class="secondary-button status-toggle-button" type="button">${user.active ? 'Disattiva' : 'Riattiva'}</button>
                </div>
            </div>
            <span class="badge">${user.active ? 'ATTIVO' : 'INATTIVO'}</span>
        </article>`).join('') : '<p class="muted">Nessun utente trovato.</p>';
}

async function loadUsers() {
    if (!isAdministrator()) return;
    try {
        const response = await api(`${IDENTITY_API}/api/users`);
        renderUsers(response.content || []);
    } catch (error) {
        $('user-list').innerHTML = `<p class="form-error">${error.message}</p>`;
    }
}

function showUsersSection() {
    document.querySelectorAll('.page-section').forEach(section => section.classList.add('hidden'));
    $('users-section').classList.remove('hidden');
    document.querySelectorAll('.nav-button').forEach(button => button.classList.toggle('active', button.dataset.section === 'users'));
    $('page-title').textContent = 'Utenti';
    loadUsers();
}

let usersEventsReady = false;

function configureUsersArea() {
    const usersButton = $('users-nav-button');
    if (!isAdministrator()) {
        usersButton.classList.add('hidden');
        return;
    }
    usersButton.classList.remove('hidden');
    if (usersEventsReady) return;
    usersEventsReady = true;
    $('users-nav-button').addEventListener('click', showUsersSection);
    $('new-user-toggle').addEventListener('click', () => $('user-form').classList.toggle('hidden'));
    $('cancel-user').addEventListener('click', () => $('user-form').classList.add('hidden'));

    $('user-form').addEventListener('submit', async event => {
        event.preventDefault();
        try {
            await api(`${IDENTITY_API}/api/users`, json({
                username: $('user-username').value,
                password: $('user-password').value,
                firstName: $('user-first-name').value,
                lastName: $('user-last-name').value,
                email: $('user-email').value,
                role: $('user-role-select').value
            }));
            $('user-form').reset();
            $('user-form').classList.add('hidden');
            showToast('Utente creato');
            loadUsers();
        } catch (error) {
            $('user-error').textContent = error.message;
        }
    });

    $('user-list').addEventListener('click', async event => {
        const card = event.target.closest('.user-record');
        if (!card) return;
        const userId = card.dataset.userId;
        try {
            if (event.target.classList.contains('role-save-button')) {
                const role = card.querySelector('.user-role-select').value;
                await api(`${IDENTITY_API}/api/users/${userId}/role`, {
                    method: 'PATCH',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ role })
                });
                showToast('Ruolo aggiornato');
                loadUsers();
            }
            if (event.target.classList.contains('status-toggle-button')) {
                const isActive = card.querySelector('.badge').textContent === 'ATTIVO';
                await api(`${IDENTITY_API}/api/users/${userId}/status`, {
                    method: 'PATCH',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ active: !isActive })
                });
                showToast(isActive ? 'Utente disattivato' : 'Utente riattivato');
                loadUsers();
            }
        } catch (error) {
            showToast(error.message);
        }
    });
}

// Il file viene caricato prima del login: riprova dopo che app.js ha salvato il JWT.
configureUsersArea();
$('login-form').addEventListener('submit', () => setTimeout(configureUsersArea, 150));
