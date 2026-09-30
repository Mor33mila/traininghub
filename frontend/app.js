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
let calendarMonth = new Date(new Date().getFullYear(), new Date().getMonth(), 1);
let selectedCalendarDate = toDateKey(new Date());
let calendarCourses = [];
let calendarLessons = [];
let toastTimeout;

const $ = id => document.getElementById(id);
const json = body => ({ method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(body) });

function authHeaders() {
	return state.token ? { Authorization: `Bearer ${state.token}` } : {};
}

function showToast(message) {
	const toast = $('toast');
	clearTimeout(toastTimeout);
	toast.textContent = message;
	toast.classList.add('visible');
	toastTimeout = setTimeout(() => toast.classList.remove('visible'), 5000);
}

function escapeHtml(value) {
	return String(value ?? '').replace(/[&<>"']/g, character => ({
		'&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;'
	})[character]);
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

async function getAllPages(url) {
	const separator = url.includes('?') ? '&' : '?';
	const items = [];
	let page = 0;
	let total = Infinity;
	while (items.length < total) {
		const result = await api(`${url}${separator}page=${page}&size=100`);
		const content = result.content || [];
		items.push(...content);
		total = result.totalElements ?? items.length;
		if (!content.length) break;
		page += 1;
	}
	return items;
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
	$('new-lesson-toggle').classList.toggle('hidden', teacher === true);
	$('attendance-form').classList.toggle('hidden', teacher === true);
	$('attendance-heading').textContent = teacher ? 'Presenze dei corsi assegnati' : 'Registra presenza';
	if (teacher) $('attendance-list').classList.remove('hidden');
}

function switchSection(name) {
	document.querySelectorAll('.page-section').forEach(section => section.classList.add('hidden'));
	$(`${name}-section`).classList.remove('hidden');
	document.querySelectorAll('.nav-button').forEach(button => button.classList.toggle('active', button.dataset.section === name));
	$('page-title').textContent = { overview: 'Panoramica', users: 'Utenti', courses: 'Corsi', participants: 'Partecipanti', enrollments: 'Iscrizioni', calendar: 'Calendario', attendance: 'Presenze' }[name];
	if (name === 'overview') loadOverview();
	if (name === 'courses') loadCourses();
	if (name === 'participants') loadParticipants();
	if (name === 'enrollments') loadEnrollments();
	if (name === 'calendar') loadCalendar();
	if (name === 'attendance') loadAttendance();
}

function card(title, meta, badge = '') {
	return `<article class="record-card"><div><strong>${escapeHtml(title)}</strong><small>${escapeHtml(meta)}</small></div>${badge ? `<span class="badge">${escapeHtml(badge)}</span>` : ''}</article>`;
}

async function loadCourses() {
	const query = $('course-search').value.trim();
	const url = `${API.course}/api/courses${query ? `?query=${encodeURIComponent(query)}` : ''}`;
	state.courses = await getAllPages(url);
	$('course-list').innerHTML = state.courses.length ? state.courses.map(course => `
		<article class="record-card course-record" data-course-id="${escapeHtml(course.id)}">
			<div><strong>${escapeHtml(course.title)}</strong><small>${escapeHtml(course.courseCode)} · ${escapeHtml(course.startDate)} - ${escapeHtml(course.endDate)}</small>
				<details><summary>Dettagli corso</summary><small>${escapeHtml(course.description || 'Nessuna descrizione')} · ${escapeHtml(course.totalHours)} ore · capienza ${escapeHtml(course.maximumCapacity)}</small></details>
			</div><span class="badge">${escapeHtml(course.status)}</span>
			${hasRole('TEACHER') ? '' : `<div class="user-controls"><button class="secondary-button course-edit" type="button">Modifica</button>${hasRole('ADMINISTRATOR') ? '<button class="secondary-button course-delete" type="button">Elimina</button>' : ''}</div>`}
		</article>`).join('') : '<p class="muted">Nessun corso trovato.</p>';
}

async function loadParticipants() {
	const query = $('participant-search').value.trim();
	const url = `${API.participant}/api/participants${query ? `?query=${encodeURIComponent(query)}` : ''}`;
	state.participants = await getAllPages(url);
	$('participant-list').innerHTML = state.participants.length ? state.participants.map(person => `
		<article class="record-card participant-record" data-participant-id="${escapeHtml(person.id)}">
			<div><strong>${escapeHtml(person.firstName)} ${escapeHtml(person.lastName)}</strong><small>${escapeHtml(person.email)} · ${escapeHtml(person.taxCode)}</small></div>
			<span class="badge">${person.active ? 'ATTIVO' : 'INATTIVO'}</span>
			${hasRole('ADMINISTRATOR') ? `<div class="user-controls"><button class="secondary-button participant-edit" type="button">Modifica</button>${person.active ? '<button class="secondary-button participant-deactivate" type="button">Disattiva</button>' : ''}</div>` : ''}
		</article>`).join('') : '<p class="muted">Nessun partecipante trovato.</p>';
}

async function openCourseForm(course = null) {
	$('course-form').reset();
	$('course-id').value = course?.id || '';
	$('course-code').value = course?.courseCode || '';
	$('course-title').value = course?.title || '';
	$('course-description').value = course?.description || '';
	$('course-area').value = course?.trainingArea || '';
	$('course-hours').value = course?.totalHours ?? '';
	$('course-start').value = course?.startDate || '';
	$('course-end').value = course?.endDate || '';
	$('course-capacity').value = course?.maximumCapacity ?? '';
	$('course-mode').value = course?.mode || 'PRESENCE';
	$('course-status').value = course?.status || 'SCHEDULED';
	$('course-submit').textContent = course ? 'Aggiorna corso' : 'Salva corso';
	$('course-error').textContent = '';
	$('course-form').classList.remove('hidden');
	if (typeof loadRelationOptions === 'function') await loadRelationOptions();
	if (course?.instructorId && !Array.from($('course-instructor').options)
		.some(option => option.value === course.instructorId)) {
		const assignedInstructor = document.createElement('option');
		assignedInstructor.value = course.instructorId;
		assignedInstructor.textContent = 'Docente attualmente assegnato';
		$('course-instructor').append(assignedInstructor);
	}
	$('course-instructor').value = course?.instructorId || '';
}

function openParticipantForm(participant = null) {
	$('participant-form').reset();
	$('participant-id').value = participant?.id || '';
	$('participant-first-name').value = participant?.firstName || '';
	$('participant-last-name').value = participant?.lastName || '';
	$('participant-tax-code').value = participant?.taxCode || '';
	$('participant-birth-date').value = participant?.birthDate || '';
	$('participant-email').value = participant?.email || '';
	$('participant-phone').value = participant?.phone || '';
	$('participant-education').value = participant?.educationLevel || '';
	$('participant-employment').value = participant?.employmentStatus || '';
	$('participant-submit').textContent = participant ? 'Aggiorna partecipante' : 'Salva partecipante';
	$('participant-error').textContent = '';
	$('participant-form').classList.remove('hidden');
}

async function loadEnrollments() {
	const courseId = $('enrollment-course-id').value;
	const list = $('enrollment-list');
	if (!courseId) {
		list.innerHTML = '<p class="muted">Seleziona un corso per consultare le iscrizioni.</p>';
		return;
	}
	try {
		const enrollments = await getAllPages(`${API.enrollment}/api/enrollments/course/${courseId}`);
		list.innerHTML = enrollments.length ? enrollments.map(enrollment => {
			const participant = typeof participantLabels !== 'undefined'
				? participantLabels.get(enrollment.participantId) || 'Partecipante'
				: 'Partecipante';
			return `<article class="record-card enrollment-record" data-enrollment-id="${escapeHtml(enrollment.id)}">
				<div><strong>${escapeHtml(participant)}</strong><small>Iscrizione ${escapeHtml(enrollment.enrollmentDate)} · ${escapeHtml(enrollment.id)}</small></div>
				<span class="badge">${escapeHtml(enrollment.status)}</span>
				<div class="user-controls"><select class="enrollment-status-select" aria-label="Nuovo stato iscrizione">
					${['REQUESTED', 'CONFIRMED', 'WITHDRAWN', 'COMPLETED'].map(status => `<option value="${status}" ${status === enrollment.status ? 'selected' : ''}>${status}</option>`).join('')}
				</select><button class="secondary-button enrollment-status-save" type="button">Aggiorna stato</button></div>
			</article>`;
		}).join('') : '<p class="muted">Nessuna iscrizione per questo corso.</p>';
	} catch (error) {
		list.innerHTML = `<p class="form-error">${escapeHtml(error.message)}</p>`;
	}
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

function toDateKey(date) {
	const year = date.getFullYear();
	const month = String(date.getMonth() + 1).padStart(2, '0');
	const day = String(date.getDate()).padStart(2, '0');
	return `${year}-${month}-${day}`;
}

function calendarCanManage() {
	return hasRole('ADMINISTRATOR') || hasRole('TUTOR');
}

function renderCalendar() {
	const monthName = new Intl.DateTimeFormat('it-IT', { month: 'long', year: 'numeric' }).format(calendarMonth);
	$('calendar-month').textContent = monthName.charAt(0).toUpperCase() + monthName.slice(1);
	const firstDay = new Date(calendarMonth.getFullYear(), calendarMonth.getMonth(), 1);
	const lastDay = new Date(calendarMonth.getFullYear(), calendarMonth.getMonth() + 1, 0);
	const monthStart = toDateKey(firstDay);
	const monthEnd = toDateKey(lastDay);
	const mondayOffset = (firstDay.getDay() + 6) % 7;
	const selectedCourseId = $('calendar-course-filter').value;
	const visibleLessons = selectedCourseId
		? calendarLessons.filter(lesson => lesson.courseId === selectedCourseId)
		: calendarLessons;
	const monthCourses = calendarCourses.filter(course =>
		(!selectedCourseId || course.id === selectedCourseId)
		&& course.startDate <= monthEnd && course.endDate >= monthStart);
	$('calendar-course-periods').innerHTML = monthCourses.length ? monthCourses.map(course => `
		<div class="calendar-course-period" data-course-period-id="${escapeHtml(course.id)}">
			<div><strong>${escapeHtml(course.courseCode)} · ${escapeHtml(course.title)}</strong><small>${escapeHtml(course.startDate)} — ${escapeHtml(course.endDate)}</small></div>
			<span class="badge">${escapeHtml(course.status)}</span>
		</div>`).join('') : '<p class="muted calendar-empty">Nessun corso nel mese selezionato.</p>';
	const cells = [];
	for (let index = 0; index < 42; index += 1) {
		const date = new Date(calendarMonth.getFullYear(), calendarMonth.getMonth(), 1 - mondayOffset + index);
		const dateKey = toDateKey(date);
		const count = visibleLessons.filter(lesson => lesson.lessonDate === dateKey).length;
		const outside = date.getMonth() !== calendarMonth.getMonth();
		const label = new Intl.DateTimeFormat('it-IT', { dateStyle: 'full' }).format(date);
		cells.push(`<button type="button" role="gridcell" class="calendar-day${outside ? ' outside-month' : ''}${dateKey === selectedCalendarDate ? ' selected' : ''}${count ? ' has-lessons' : ''}" data-date="${dateKey}" aria-label="${escapeHtml(label)}${count ? `, ${count} lezioni` : ''}" aria-selected="${dateKey === selectedCalendarDate}">
			<span class="calendar-day-number">${date.getDate()}</span>${count ? `<span class="calendar-day-count">${count}</span>` : ''}
		</button>`);
	}
	$('calendar-grid').innerHTML = cells.join('');
	renderCalendarAgenda(visibleLessons);
}

function renderCalendarAgenda(lessons = calendarLessons) {
	const date = new Date(`${selectedCalendarDate}T00:00:00`);
	$('calendar-day-title').textContent = new Intl.DateTimeFormat('it-IT', {
		weekday: 'long', day: 'numeric', month: 'long'
	}).format(date);
	const selectedCourseId = $('calendar-course-filter').value;
	const dayCourses = calendarCourses.filter(course =>
		(!selectedCourseId || course.id === selectedCourseId)
		&& course.startDate <= selectedCalendarDate && course.endDate >= selectedCalendarDate);
	const dayLessons = lessons.filter(lesson => lesson.lessonDate === selectedCalendarDate)
		.sort((left, right) => left.startTime.localeCompare(right.startTime));
	const courseRows = dayCourses.map(course => `
		<article class="record-card calendar-course-context">
			<div><strong>${escapeHtml(course.title)}</strong><small>${escapeHtml(course.courseCode)} · ${escapeHtml(course.startDate)} — ${escapeHtml(course.endDate)}</small></div>
			<span class="badge">CORSO NEL PERIODO</span>
		</article>`).join('');
	const lessonRows = dayLessons.map(lesson => `
		<article class="record-card calendar-lesson-card" data-lesson-id="${escapeHtml(lesson.id)}">
			<div><strong>${escapeHtml(lesson.title)}</strong><small>${escapeHtml(lesson.courseTitle)} · ${escapeHtml(lesson.startTime)}-${escapeHtml(lesson.endTime)}</small>${lesson.notes ? `<small>${escapeHtml(lesson.notes)}</small>` : ''}</div>
			${calendarCanManage() ? `<div class="user-controls"><button class="secondary-button lesson-edit" type="button">Modifica</button><button class="secondary-button lesson-delete" type="button">Elimina</button></div>` : ''}
		</article>`).join('');
	$('calendar-lesson-list').innerHTML = courseRows + lessonRows
		|| '<p class="muted calendar-empty">Nessun corso o lezione per questo giorno.</p>';
}

async function loadCalendar() {
	try {
		calendarCourses = await getAllPages(`${API.course}/api/courses`);
		const previousFilter = $('calendar-course-filter').value;
		const options = calendarCourses.map(course => `<option value="${escapeHtml(course.id)}">${escapeHtml(course.courseCode)} · ${escapeHtml(course.title)}</option>`).join('');
		$('calendar-course-filter').innerHTML = '<option value="">Tutti i corsi</option>' + options;
		if (calendarCourses.some(course => course.id === previousFilter)) $('calendar-course-filter').value = previousFilter;
		$('lesson-course-id').innerHTML = '<option value="">Seleziona un corso</option>' + calendarCourses
			.map(course => `<option value="${escapeHtml(course.id)}">${escapeHtml(course.courseCode)} · ${escapeHtml(course.title)}</option>`).join('');

		const first = new Date(calendarMonth.getFullYear(), calendarMonth.getMonth(), 1);
		const last = new Date(calendarMonth.getFullYear(), calendarMonth.getMonth() + 1, 0);
		const from = toDateKey(first);
		const to = toDateKey(last);
		const selectedCourseId = $('calendar-course-filter').value;
		const coursesToLoad = selectedCourseId
			? calendarCourses.filter(course => course.id === selectedCourseId)
			: calendarCourses;
		const results = await Promise.all(coursesToLoad.map(course =>
			api(`${API.enrollment}/api/lessons/course/${course.id}?from=${from}&to=${to}`)));
		calendarLessons = results.flatMap((lessons, index) => lessons.map(lesson => ({
			...lesson,
			courseTitle: coursesToLoad[index].title
		})));
		renderCalendar();
	} catch (error) {
		$('calendar-lesson-list').innerHTML = `<p class="form-error">${escapeHtml(error.message)}</p>`;
	}
}

function openLessonForm(lesson = null) {
	if (!calendarCanManage()) return;
	$('lesson-form').reset();
	$('lesson-id').value = lesson?.id || '';
	$('lesson-course-id').value = lesson?.courseId || '';
	$('lesson-title').value = lesson?.title || '';
	$('lesson-date').value = lesson?.lessonDate || selectedCalendarDate;
	$('lesson-start-time').value = lesson?.startTime || '';
	$('lesson-end-time').value = lesson?.endTime || '';
	$('lesson-notes').value = lesson?.notes || '';
	$('lesson-error').textContent = '';
	$('lesson-submit').textContent = lesson ? 'Aggiorna lezione' : 'Salva lezione';
	$('lesson-form').classList.remove('hidden');
	$('lesson-form').scrollIntoView({ behavior: 'smooth', block: 'start' });
}

async function loadOverview() {
	try {
		const courses = await getAllPages(`${API.course}/api/courses`);
		const teacher = hasRole('TEACHER');
		const participants = teacher ? [] : await getAllPages(`${API.participant}/api/participants`);
		state.courses = courses;
		$('stat-courses').textContent = courses.length;
		$('stat-participants').textContent = teacher ? '--' : participants.length;
		$('stat-scheduled-courses').textContent = courses.filter(course => course.status === 'SCHEDULED').length;
		$('stat-active-courses').textContent = courses.filter(course => course.status === 'IN_PROGRESS').length;
		const upcoming = courses.filter(course => ['SCHEDULED', 'IN_PROGRESS'].includes(course.status));
		$('overview-courses').innerHTML = upcoming.slice(0, 4)
			.map(course => card(course.title, `${course.courseCode} · ${course.startDate} - ${course.endDate}`, course.status)).join('')
			|| '<p class="muted">Nessun corso programmato o in svolgimento.</p>';

		if (teacher) {
			$('stat-enrollments').textContent = '--';
			$('stat-average-attendance').textContent = '--';
			$('overview-capacity').innerHTML = '<p class="muted">Dati non disponibili per questo ruolo.</p>';
			$('overview-at-risk').innerHTML = '<p class="muted">Dati non disponibili per questo ruolo.</p>';
			return;
		}

		const courseEnrollments = await Promise.all(courses.map(course =>
			getAllPages(`${API.enrollment}/api/enrollments/course/${course.id}`)));
		const enrollments = courseEnrollments.flat();
		$('stat-enrollments').textContent = enrollments.length;
		const capacityRows = courses.map((course, index) => {
			const occupied = courseEnrollments[index].filter(enrollment =>
				['REQUESTED', 'CONFIRMED'].includes(enrollment.status)).length;
			const remaining = Math.max(0, course.maximumCapacity - occupied);
			return { course, occupied, remaining };
		}).filter(row => row.remaining > 0 && row.course.status !== 'CANCELLED');
		$('overview-capacity').innerHTML = capacityRows.length
			? capacityRows.slice(0, 6).map(row => card(row.course.title,
				`${row.occupied}/${row.course.maximumCapacity} iscritti · ${row.remaining} posti liberi`, row.course.status)).join('')
			: '<p class="muted">Nessun corso con posti disponibili.</p>';

		const activeEnrollments = enrollments.filter(enrollment =>
			['CONFIRMED', 'COMPLETED'].includes(enrollment.status));
		const participantIds = [...new Set(activeEnrollments.map(enrollment => enrollment.participantId))];
		const participantById = new Map(participants.map(participant => [participant.id, participant]));
		const frequencies = await Promise.all(participantIds.map(async participantId => {
			const frequency = await api(`${API.enrollment}/api/attendance/participant/${participantId}/percentage`);
			return { participantId, frequency };
		}));
		const measured = frequencies.filter(item => Number(item.frequency.totalHours) > 0);
		const average = measured.length
			? measured.reduce((sum, item) => sum + Number(item.frequency.percentage), 0) / measured.length
			: null;
		$('stat-average-attendance').textContent = average === null ? '--' : `${average.toFixed(1)}%`;
		const atRisk = frequencies.filter(item => item.frequency.belowMinimumThreshold);
		$('overview-at-risk').innerHTML = atRisk.length
			? atRisk.map(item => {
				const participant = participantById.get(item.participantId);
				const name = participant ? `${participant.firstName} ${participant.lastName}` : 'Partecipante';
				return card(name, `${item.frequency.attendedHours}/${item.frequency.totalHours} ore · ${item.frequency.percentage}%`, 'A RISCHIO');
			}).join('')
			: '<p class="muted">Nessun partecipante sotto la soglia minima.</p>';
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
		if (typeof configureUsersArea === 'function') configureUsersArea();
		switchSection('overview');
	} catch (error) {
		$('login-error').textContent = error.message;
	}
});

$('logout-button').addEventListener('click', logout);
document.querySelectorAll('.nav-button').forEach(button => button.addEventListener('click', () => switchSection(button.dataset.section)));
const mobileMenuToggle = $('mobile-menu-toggle');
const sidebar = document.querySelector('.sidebar');

function setMobileMenuOpen(open) {
	sidebar.classList.toggle('menu-open', open);
	mobileMenuToggle.setAttribute('aria-expanded', String(open));
	mobileMenuToggle.setAttribute('aria-label', open ? 'Chiudi menu' : 'Apri menu');
}

mobileMenuToggle.addEventListener('click', () => {
	setMobileMenuOpen(mobileMenuToggle.getAttribute('aria-expanded') !== 'true');
});
document.querySelectorAll('#main-nav .nav-button').forEach(button => button.addEventListener('click', () => setMobileMenuOpen(false)));
document.addEventListener('keydown', event => {
	if (event.key === 'Escape') setMobileMenuOpen(false);
});
document.querySelectorAll('[data-section-link]').forEach(button => button.addEventListener('click', () => switchSection(button.dataset.sectionLink)));
$('enrollment-course-id').addEventListener('change', loadEnrollments);
$('enrollment-list').addEventListener('click', async event => {
	if (!event.target.classList.contains('enrollment-status-save')) return;
	const record = event.target.closest('.enrollment-record');
	const status = record.querySelector('.enrollment-status-select').value;
	try {
		await api(`${API.enrollment}/api/enrollments/${record.dataset.enrollmentId}/status`, {
			method: 'PATCH',
			headers: { 'Content-Type': 'application/json' },
			body: JSON.stringify({ status })
		});
		showToast('Stato iscrizione aggiornato');
		await loadEnrollments();
		if (typeof loadEnrollmentOptions === 'function') await loadEnrollmentOptions();
		await loadOverview();
	} catch (error) {
		showToast(error.message);
	}
});
$('calendar-prev').addEventListener('click', async () => {
	calendarMonth = new Date(calendarMonth.getFullYear(), calendarMonth.getMonth() - 1, 1);
	selectedCalendarDate = toDateKey(calendarMonth);
	await loadCalendar();
});
$('calendar-next').addEventListener('click', async () => {
	calendarMonth = new Date(calendarMonth.getFullYear(), calendarMonth.getMonth() + 1, 1);
	selectedCalendarDate = toDateKey(calendarMonth);
	await loadCalendar();
});
$('calendar-today').addEventListener('click', async () => {
	calendarMonth = new Date(new Date().getFullYear(), new Date().getMonth(), 1);
	selectedCalendarDate = toDateKey(new Date());
	await loadCalendar();
});
$('calendar-course-filter').addEventListener('change', loadCalendar);
$('calendar-grid').addEventListener('click', event => {
	const day = event.target.closest('.calendar-day');
	if (!day) return;
	selectedCalendarDate = day.dataset.date;
	const selectedDate = new Date(`${selectedCalendarDate}T00:00:00`);
	if (selectedDate.getMonth() !== calendarMonth.getMonth()
		|| selectedDate.getFullYear() !== calendarMonth.getFullYear()) {
		calendarMonth = new Date(selectedDate.getFullYear(), selectedDate.getMonth(), 1);
		loadCalendar();
	} else {
		renderCalendar();
	}
});
$('new-lesson-toggle').addEventListener('click', () => openLessonForm());
$('lesson-cancel').addEventListener('click', () => $('lesson-form').classList.add('hidden'));
$('lesson-form').addEventListener('submit', async event => {
	event.preventDefault();
	const lessonId = $('lesson-id').value;
	const body = {
		courseId: $('lesson-course-id').value,
		title: $('lesson-title').value,
		lessonDate: $('lesson-date').value,
		startTime: $('lesson-start-time').value,
		endTime: $('lesson-end-time').value,
		notes: $('lesson-notes').value || null
	};
	try {
		await api(`${API.enrollment}/api/lessons${lessonId ? `/${lessonId}` : ''}`, {
			...json(body),
			method: lessonId ? 'PUT' : 'POST'
		});
		calendarMonth = new Date(`${body.lessonDate}T00:00:00`);
		calendarMonth = new Date(calendarMonth.getFullYear(), calendarMonth.getMonth(), 1);
		selectedCalendarDate = body.lessonDate;
		$('lesson-form').classList.add('hidden');
		showToast(lessonId ? 'Lezione aggiornata' : 'Lezione pianificata');
		await loadCalendar();
	} catch (error) {
		$('lesson-error').textContent = error.message;
	}
});
$('calendar-lesson-list').addEventListener('click', async event => {
	const record = event.target.closest('.calendar-lesson-card');
	if (!record) return;
	const lesson = calendarLessons.find(item => item.id === record.dataset.lessonId);
	if (event.target.classList.contains('lesson-edit')) {
		openLessonForm(lesson);
		return;
	}
	if (event.target.classList.contains('lesson-delete') && confirm(`Eliminare la lezione "${lesson?.title || ''}"?`)) {
		try {
			await api(`${API.enrollment}/api/lessons/${record.dataset.lessonId}`, { method: 'DELETE' });
			showToast('Lezione eliminata');
			await loadCalendar();
		} catch (error) {
			showToast(error.message);
		}
	}
});
$('new-course-toggle').addEventListener('click', () => openCourseForm());
$('cancel-course').addEventListener('click', () => $('course-form').classList.add('hidden'));
$('new-participant-toggle').addEventListener('click', () => openParticipantForm());
$('cancel-participant').addEventListener('click', () => $('participant-form').classList.add('hidden'));

let courseSearchTimer;
let participantSearchTimer;
$('course-search').addEventListener('input', () => {
	clearTimeout(courseSearchTimer);
	courseSearchTimer = setTimeout(() => loadCourses().catch(error => showToast(error.message)), 250);
});
$('participant-search').addEventListener('input', () => {
	clearTimeout(participantSearchTimer);
	participantSearchTimer = setTimeout(() => loadParticipants().catch(error => showToast(error.message)), 250);
});

$('course-list').addEventListener('click', async event => {
	const record = event.target.closest('.course-record');
	if (!record) return;
	const course = state.courses.find(item => item.id === record.dataset.courseId);
	if (event.target.classList.contains('course-edit')) {
		await openCourseForm(course);
		return;
	}
	if (event.target.classList.contains('course-delete') && confirm(`Eliminare il corso ${course?.courseCode || ''}?`)) {
		try {
			await api(`${API.course}/api/courses/${record.dataset.courseId}`, { method: 'DELETE' });
			showToast('Corso eliminato');
			await loadCourses();
			await loadOverview();
		} catch (error) {
			showToast(error.message);
		}
	}
});

$('participant-list').addEventListener('click', async event => {
	const record = event.target.closest('.participant-record');
	if (!record) return;
	const participant = state.participants.find(item => item.id === record.dataset.participantId);
	if (event.target.classList.contains('participant-edit')) {
		openParticipantForm(participant);
		return;
	}
	if (event.target.classList.contains('participant-deactivate') && confirm('Disattivare questo partecipante?')) {
		try {
			await api(`${API.participant}/api/participants/${record.dataset.participantId}`, { method: 'DELETE' });
			showToast('Partecipante disattivato');
			await loadParticipants();
			await loadOverview();
		} catch (error) {
			showToast(error.message);
		}
	}
});

$('course-form').addEventListener('submit', async event => {
	event.preventDefault();
	try {
		const courseId = $('course-id').value;
		const body = { courseCode: $('course-code').value, title: $('course-title').value, description: $('course-description').value, trainingArea: $('course-area').value, totalHours: Number($('course-hours').value), startDate: $('course-start').value, endDate: $('course-end').value, maximumCapacity: Number($('course-capacity').value), mode: $('course-mode').value, status: $('course-status').value, instructorId: $('course-instructor').value || null };
		await api(`${API.course}/api/courses${courseId ? `/${courseId}` : ''}`, { ...json(body), method: courseId ? 'PUT' : 'POST' });
		$('course-form').reset();
		$('course-form').classList.add('hidden');
		showToast(courseId ? 'Corso aggiornato' : 'Corso creato');
		await loadCourses();
		await loadOverview();
	} catch (error) { $('course-error').textContent = error.message; }
});

$('participant-form').addEventListener('submit', async event => {
	event.preventDefault();
	try {
		const participantId = $('participant-id').value;
		const body = { firstName: $('participant-first-name').value, lastName: $('participant-last-name').value, taxCode: $('participant-tax-code').value, birthDate: $('participant-birth-date').value, email: $('participant-email').value, phone: $('participant-phone').value, educationLevel: $('participant-education').value, employmentStatus: $('participant-employment').value };
		await api(`${API.participant}/api/participants${participantId ? `/${participantId}` : ''}`, { ...json(body), method: participantId ? 'PUT' : 'POST' });
		$('participant-form').reset();
		$('participant-form').classList.add('hidden');
		showToast(participantId ? 'Partecipante aggiornato' : 'Partecipante creato');
		await loadParticipants();
		await loadOverview();
	} catch (error) { $('participant-error').textContent = error.message; }
});

$('enrollment-form').addEventListener('submit', async event => {
	event.preventDefault();
	try {
		await api(`${API.enrollment}/api/enrollments`, json({ courseId: $('enrollment-course-id').value, participantId: $('enrollment-participant-id').value }));
		showToast('Iscrizione creata');
		$('enrollment-participant-id').value = '';
		await loadEnrollments();
		await loadOverview();
	} catch (error) { $('enrollment-error').textContent = error.message; }
});

$('attendance-form').addEventListener('submit', async event => {
	event.preventDefault();
	try {
		const absent = $('attendance-absent').checked;
		await api(`${API.enrollment}/api/attendance`, json({ enrollmentId: $('attendance-enrollment-id').value, lessonDate: $('attendance-date').value, entryTime: absent ? null : $('attendance-entry').value, exitTime: absent ? null : $('attendance-exit').value, absent, justification: $('attendance-justification').value || null }));
		$('attendance-form').reset();
		syncAttendanceTimeFields();
		showToast('Presenza registrata');
		await loadAttendance();
		await loadOverview();
	} catch (error) { $('attendance-error').textContent = error.message; }
});

function syncAttendanceTimeFields() {
	const absent = $('attendance-absent').checked;
	for (const field of [$('attendance-entry'), $('attendance-exit')]) {
		field.required = !absent;
		field.disabled = absent;
		if (absent) field.value = '';
	}
}

$('attendance-absent').addEventListener('change', syncAttendanceTimeFields);
syncAttendanceTimeFields();

if (state.token) { setView(true); renderUser(); switchSection('overview'); } else setView(false);
