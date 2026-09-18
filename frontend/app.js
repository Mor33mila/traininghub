const API = {
	identity: 'http://localhost:8080',
	course: 'http://localhost:8081',
	participant: 'http://localhost:8082',
	enrollment: 'http://localhost:8083'
};

const state = {
	token: localStorage.getItem('traininghub_token'),
	user: JSON.parse(localStorage.getItem('traininghub_user') || 'null'),
	courses: [],
	participants: []
};

const $ = id => document.getElementById(id);
const json = body => ({ method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(body) });

function authHeaders() {
	return state.token ? { Authorization: `Bearer ${state.token}` } : {};
}

function showToast(message) {
	const toast = $('toast');
	toast.textContent = message;
	toast.classList.add('visible');
	setTimeout(() => toast.classList.remove('visible'), 2800);
}

async function api(url, options = {}) {
	const response = await fetch(url, { ...options, headers: { ...authHeaders(), ...(options.headers || {}) } });
	if (response.status === 401) {
		logout();
		throw new Error('Sessione scaduta. Accedi di nuovo.');
	}
	if (!response.ok) {
		let message = `Errore HTTP ${response.status}`;
		try {
			const body = await response.json();
			message = body.fieldErrors
				? Object.entries(body.fieldErrors).map(([field, detail]) => `${field}: ${detail}`).join(' | ')
				: body.message || message;
		} catch { }
		throw new Error(message);
	}
	if (response.status === 204) return null;
	return response.json();
}

function setView(logged) {
	$('login-view').classList.toggle('hidden', logged);
	$('app-view').classList.toggle('hidden', !logged);
}

function logout() {
	state.token = null;
	state.user = null;
	localStorage.removeItem('traininghub_token');
	localStorage.removeItem('traininghub_user');
	setView(false);
}

function renderUser() {
	const user = state.user || {};
	$('user-name').textContent = user.username || 'Utente';
	$('user-role').textContent = user.roles?.join(', ') || 'AUTHENTICATED';
	$('user-initials').textContent = (user.username || 'U').slice(0, 1).toUpperCase();
	applyRoleExperience();
}

function hasRole(role) {
	return state.user?.roles?.includes(role);
}

function applyRoleExperience() {
	const administrator = hasRole('ADMINISTRATOR');
	const tutor = hasRole('TUTOR');
	const teacher = hasRole('TEACHER');
	const participantButton = document.querySelector('[data-section="participants"]');
	const enrollmentButton = document.querySelector('[data-section="enrollments"]');
	const attendanceButton = document.querySelector('[data-section="attendance"]');
	const courseCreateButton = $('new-course-toggle');
	const participantCreateButton = $('new-participant-toggle');
	participantButton.classList.toggle('hidden', teacher === true);
	enrollmentButton.classList.toggle('hidden', teacher === true);
	attendanceButton.classList.toggle('hidden', false);
	courseCreateButton.classList.toggle('hidden', teacher === true);
	participantCreateButton.classList.toggle('hidden', administrator !== true);
	$('attendance-form').classList.toggle('hidden', teacher === true);
	$('attendance-heading').textContent = teacher ? 'Presenze dei corsi assegnati' : 'Registra presenza';
	if (teacher) $('attendance-list').classList.remove('hidden');
}

function switchSection(name) {
	document.querySelectorAll('.page-section').forEach(section => section.classList.add('hidden'));
	$(`${name}-section`).classList.remove('hidden');
	document.querySelectorAll('.nav-button').forEach(button => button.classList.toggle('active', button.dataset.section === name));
	$('page-title').textContent = { overview: 'Panoramica', users: 'Utenti', courses: 'Corsi', participants: 'Partecipanti', enrollments: 'Iscrizioni', attendance: 'Presenze' }[name];
	if (name === 'overview') loadOverview();
	if (name === 'courses') loadCourses();
	if (name === 'participants') loadParticipants();
	if (name === 'enrollments') loadEnrollments();
	if (name === 'attendance') loadAttendance();
}

function card(title, meta, badge = '') {
	return `<article class="record-card"><div><strong>${title}</strong><small>${meta}</small></div>${badge ? `<span class="badge">${badge}</span>` : ''}</article>`;
}

async function loadCourses() {
	const data = await api(`${API.course}/api/courses`);
	state.courses = data.content || [];
	$('course-list').innerHTML = state.courses.length
		? state.courses.map(course => card(course.title, `${course.courseCode} · ${course.startDate} - ${course.endDate}`, course.status)).join('')
		: '<p class="muted">Nessun corso trovato.</p>';
}

async function loadParticipants() {
	const data = await api(`${API.participant}/api/participants`);
	state.participants = data.content || [];
	$('participant-list').innerHTML = state.participants.length
		? state.participants.map(person => card(`${person.firstName} ${person.lastName}`, `${person.email} · ${person.taxCode}`, person.active ? 'ATTIVO' : 'INATTIVO')).join('')
		: '<p class="muted">Nessun partecipante trovato.</p>';
}

async function loadEnrollments() {
	$('enrollment-list').innerHTML = '<p class="muted">Seleziona un corso per consultare le iscrizioni.</p>';
}

async function loadAttendance() {
	const courseSelect = $('attendance-course-id');
	if (!courseSelect?.value) {
		$('attendance-list').innerHTML = '<p class="muted">Seleziona un corso per vedere le presenze.</p>';
		return;
	}
	try {
		const records = await api(`${API.enrollment}/api/attendance/course/${courseSelect.value}`);
		$('attendance-list').innerHTML = records.length
			? records.map(record => card(record.lessonDate, `${record.entryTime || '-'} - ${record.exitTime || '-'} · ${record.attendedHours} ore`, record.absent ? 'ASSENTE' : 'PRESENTE')).join('')
			: '<p class="muted">Nessuna presenza registrata.</p>';
	} catch (error) {
		$('attendance-list').innerHTML = `<p class="form-error">${error.message}</p>`;
	}
}

async function loadOverview() {
	try {
		const courses = await api(`${API.course}/api/courses`);
		const participants = hasRole('TEACHER') ? { totalElements: 0, content: [] } : await api(`${API.participant}/api/participants`);
		state.courses = courses.content || [];
		$('stat-courses').textContent = courses.totalElements ?? state.courses.length;
		$('stat-participants').textContent = participants.totalElements ?? (participants.content || []).length;
		$('overview-courses').innerHTML = state.courses.slice(0, 4).map(course => card(course.title, `${course.courseCode} · capienza ${course.maximumCapacity}`, course.status)).join('') || '<p class="muted">Nessun corso disponibile.</p>';
	} catch (error) {
		showToast(error.message);
	}
}

$('login-form').addEventListener('submit', async event => {
	event.preventDefault();
	$('login-error').textContent = '';
	try {
		const response = await fetch(`${API.identity}/api/auth/login`, { ...json({ username: $('login-username').value, password: $('login-password').value }) });
		if (!response.ok) throw new Error('Username o password non validi');
		const auth = await response.json();
		const payload = JSON.parse(atob(auth.accessToken.split('.')[1].replace(/-/g, '+').replace(/_/g, '/')));
		state.token = auth.accessToken;
		state.user = { username: payload.username, roles: payload.roles };
		localStorage.setItem('traininghub_token', state.token);
		localStorage.setItem('traininghub_user', JSON.stringify(state.user));
		setView(true);
		renderUser();
		switchSection('overview');
	} catch (error) {
		$('login-error').textContent = error.message;
	}
});

$('logout-button').addEventListener('click', logout);
document.querySelectorAll('.nav-button').forEach(button => button.addEventListener('click', () => switchSection(button.dataset.section)));
document.querySelectorAll('[data-section-link]').forEach(button => button.addEventListener('click', () => switchSection(button.dataset.sectionLink)));
$('new-course-toggle').addEventListener('click', () => $('course-form').classList.toggle('hidden'));
$('cancel-course').addEventListener('click', () => $('course-form').classList.add('hidden'));
$('new-participant-toggle').addEventListener('click', () => $('participant-form').classList.toggle('hidden'));
$('cancel-participant').addEventListener('click', () => $('participant-form').classList.add('hidden'));

$('course-form').addEventListener('submit', async event => {
	event.preventDefault();
	try {
		await api(`${API.course}/api/courses`, json({ courseCode: $('course-code').value, title: $('course-title').value, description: '', trainingArea: $('course-area').value, totalHours: Number($('course-hours').value), startDate: $('course-start').value, endDate: $('course-end').value, maximumCapacity: Number($('course-capacity').value), mode: $('course-mode').value, status: $('course-status').value, instructorId: $('course-instructor').value || null }));
		$('course-form').reset();
		$('course-form').classList.add('hidden');
		showToast('Corso creato');
		loadCourses();
	} catch (error) { $('course-error').textContent = error.message; }
});

$('participant-form').addEventListener('submit', async event => {
	event.preventDefault();
	try {
		await api(`${API.participant}/api/participants`, json({ firstName: $('participant-first-name').value, lastName: $('participant-last-name').value, taxCode: $('participant-tax-code').value, birthDate: $('participant-birth-date').value, email: $('participant-email').value, phone: $('participant-phone').value, educationLevel: $('participant-education').value, employmentStatus: $('participant-employment').value }));
		$('participant-form').reset();
		$('participant-form').classList.add('hidden');
		showToast('Partecipante creato');
		loadParticipants();
	} catch (error) { $('participant-error').textContent = error.message; }
});

$('enrollment-form').addEventListener('submit', async event => {
	event.preventDefault();
	try {
		await api(`${API.enrollment}/api/enrollments`, json({ courseId: $('enrollment-course-id').value, participantId: $('enrollment-participant-id').value }));
		showToast('Iscrizione creata');
	} catch (error) { $('enrollment-error').textContent = error.message; }
});

$('attendance-form').addEventListener('submit', async event => {
	event.preventDefault();
	try {
		await api(`${API.enrollment}/api/attendance`, json({ enrollmentId: $('attendance-enrollment-id').value, lessonDate: $('attendance-date').value, entryTime: $('attendance-entry').value, exitTime: $('attendance-exit').value, absent: $('attendance-absent').checked, justification: $('attendance-justification').value || null }));
		showToast('Presenza registrata');
	} catch (error) { $('attendance-error').textContent = error.message; }
});

if (state.token) { setView(true); renderUser(); switchSection('overview'); } else setView(false);
