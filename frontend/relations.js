const RELATIONS_API = { identity: 'http://localhost:8080', course: 'http://localhost:8081', participant: 'http://localhost:8082', enrollment: 'http://localhost:8083' };
const participantLabels = new Map();

function relationHeaders() {
    const token = localStorage.getItem('traininghub_token');
    return token ? { Authorization: `Bearer ${token}` } : {};
}

async function relationGet(url) {
    const response = await fetch(url, { headers: relationHeaders() });
    if (!response.ok) throw new Error(`Errore HTTP ${response.status}`);
    return response.json();
}

async function relationGetAll(url) {
    const items = [];
    const separator = url.includes('?') ? '&' : '?';
    let page = 0;
    let total = Infinity;
    while (items.length < total) {
        const result = await relationGet(`${url}${separator}page=${page}&size=100`);
        const content = result.content || [];
        items.push(...content);
        total = result.totalElements ?? items.length;
        if (!content.length) break;
        page += 1;
    }
    return items;
}

function escapeRelationHtml(value) {
    return String(value ?? '').replace(/[&<>"']/g, character => ({
        '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;'
    })[character]);
}

function setOptions(elementId, options, placeholder) {
    const element = document.getElementById(elementId);
    if (!element) return;
    element.innerHTML = `<option value="">${escapeRelationHtml(placeholder)}</option>` + options.map(option =>
        `<option value="${escapeRelationHtml(option.value)}">${escapeRelationHtml(option.label)}</option>`).join('');
}

async function loadRelationOptions() {
    if (!localStorage.getItem('traininghub_token')) return;
    try {
        const storedUser = JSON.parse(localStorage.getItem('traininghub_user') || 'null');
        const isTeacher = storedUser?.roles?.includes('TEACHER');
        const courses = await relationGetAll(`${RELATIONS_API.course}/api/courses`);
        const courseOptions = courses.map(course => ({
            value: course.id,
            label: `${course.courseCode} - ${course.title}`
        }));
        setOptions('enrollment-course-id', courseOptions, 'Seleziona un corso');
        setOptions('attendance-course-id', courseOptions, 'Seleziona un corso');

        if (!isTeacher) {
            const participants = await relationGetAll(`${RELATIONS_API.participant}/api/participants`);
            const participantOptions = participants.map(participant => ({
                value: participant.id,
                label: `${participant.firstName} ${participant.lastName} - ${participant.email}`
            }));
            participantOptions.forEach(option => participantLabels.set(option.value, option.label));
            setOptions('enrollment-participant-id', participantOptions, 'Seleziona un partecipante');
        }

        if (storedUser?.roles?.includes('ADMINISTRATOR')) try {
            const users = await relationGetAll(`${RELATIONS_API.identity}/api/users`);
            setOptions('course-instructor', users
                .filter(user => user.role === 'TEACHER' && user.active)
                .map(user => ({ value: user.id, label: `${user.firstName} ${user.lastName} - ${user.email}` })),
                'Nessun docente assegnato');
        } catch {
            // Il tutor puo' usare il form senza visualizzare la gestione utenti admin.
        }
    } catch (error) {
        showToast(`Impossibile caricare le opzioni: ${error.message}`);
    }
}

async function loadEnrollmentOptions(event) {
    const select = document.getElementById('attendance-enrollment-id');
    if (!select) return;
    const storedUser = JSON.parse(localStorage.getItem('traininghub_user') || 'null');
    if (storedUser?.roles?.includes('TEACHER')) {
        if (typeof loadAttendance === 'function') await loadAttendance();
        return;
    }
    const attendanceCourseSelect = document.getElementById('attendance-course-id');
    const enrollmentCourseSelect = document.getElementById('enrollment-course-id');
    const selectedCourseId = event?.currentTarget?.id === 'enrollment-course-id'
        ? enrollmentCourseSelect?.value
        : attendanceCourseSelect?.value || enrollmentCourseSelect?.value;
    if (!selectedCourseId) {
        setOptions('attendance-enrollment-id', [], 'Prima seleziona un corso');
        return;
    }
    try {
        const enrollments = await relationGetAll(`${RELATIONS_API.enrollment}/api/enrollments/course/${selectedCourseId}`);
        setOptions('attendance-enrollment-id', enrollments
            .filter(enrollment => ['CONFIRMED', 'COMPLETED'].includes(enrollment.status)).map(enrollment => ({
            value: enrollment.id,
            label: `${participantLabels.get(enrollment.participantId) || 'Partecipante'} - ${enrollment.status}`
        })), 'Seleziona un\'iscrizione');
    } catch (error) {
        showToast(`Impossibile caricare le iscrizioni: ${error.message}`);
    }
}

document.getElementById('enrollment-course-id')?.addEventListener('change', loadEnrollmentOptions);
document.getElementById('attendance-course-id')?.addEventListener('change', loadEnrollmentOptions);
document.getElementById('new-course-toggle')?.addEventListener('click', loadRelationOptions);
document.getElementById('new-participant-toggle')?.addEventListener('click', loadRelationOptions);
document.querySelector('[data-section="enrollments"]')?.addEventListener('click', async () => {
    await loadRelationOptions();
    if (typeof loadEnrollments === 'function') await loadEnrollments();
});
document.querySelector('[data-section="attendance"]')?.addEventListener('click', async () => {
    await loadRelationOptions();
    await loadEnrollmentOptions();
});

// Il token viene creato dal login: carica le opzioni solo dopo l'autenticazione.
document.getElementById('login-form')?.addEventListener('submit', () => setTimeout(loadRelationOptions, 250));
setTimeout(loadRelationOptions, 250);
