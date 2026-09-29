const USER_ROLES = ['ADMINISTRATOR', 'TUTOR', 'TEACHER'];
const IDENTITY_API = 'http://localhost:8080';
let loadedUsers = [];

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
                <strong>${escapeHtml(user.firstName)} ${escapeHtml(user.lastName)}</strong>
                <small>${escapeHtml(user.username)} · ${escapeHtml(user.email)}</small>
                <div class="user-controls">
                    <button class="secondary-button user-edit-button" type="button">Modifica</button>
                    <select class="user-role-select" aria-label="Ruolo di ${escapeHtml(user.username)}">
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
        loadedUsers = await getAllPages(`${IDENTITY_API}/api/users`);
        renderUsers(loadedUsers);
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
    $('new-user-toggle').addEventListener('click', () => {
        $('user-form').reset();
        $('user-id').value = '';
        $('user-submit').textContent = 'Crea utente';
        $('user-form').classList.remove('hidden');
    });
    $('cancel-user').addEventListener('click', () => $('user-form').classList.add('hidden'));

    $('user-form').addEventListener('submit', async event => {
        event.preventDefault();
        try {
            const userId = $('user-id').value;
            const body = {
                username: $('user-username').value,
                password: $('user-password').value,
                firstName: $('user-first-name').value,
                lastName: $('user-last-name').value,
                email: $('user-email').value,
                role: $('user-role-select').value
            };
            await api(`${IDENTITY_API}/api/users${userId ? `/${userId}` : ''}`, {
                ...json(body),
                method: userId ? 'PUT' : 'POST'
            });
            $('user-form').reset();
            $('user-form').classList.add('hidden');
            $('user-id').value = '';
            showToast(userId ? 'Utente aggiornato' : 'Utente creato');
            loadUsers();
        } catch (error) {
            $('user-error').textContent = error.message;
        }
    });

    $('user-list').addEventListener('click', async event => {
        const card = event.target.closest('.user-record');
        if (!card) return;
        const userId = card.dataset.userId;
        if (event.target.classList.contains('user-edit-button')) {
            const user = loadedUsers.find(item => item.id === userId);
            $('user-id').value = user.id;
            $('user-username').value = user.username;
            $('user-password').value = '';
            $('user-first-name').value = user.firstName;
            $('user-last-name').value = user.lastName;
            $('user-email').value = user.email;
            $('user-role-select').value = user.role;
            $('user-submit').textContent = 'Aggiorna utente';
            $('user-error').textContent = '';
            $('user-form').classList.remove('hidden');
            return;
        }
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
