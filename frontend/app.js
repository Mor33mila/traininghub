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
let auditCurrentPage = 0;
let auditPageData;
const overviewCharts = {};
let auditSearchTimer;
const exportColumns = {
	courses: { id: 'ID', courseCode: 'Codice', title: 'Titolo', description: 'Descrizione', trainingArea: 'Area', totalHours: 'Ore totali', startDate: 'Inizio', endDate: 'Fine', maximumCapacity: 'Capienza', mode: 'Modalita', status: 'Stato', instructorId: 'Docente ID' },
	participants: { id: 'ID', firstName: 'Nome', lastName: 'Cognome', taxCode: 'Codice fiscale', birthDate: 'Data di nascita', email: 'E-mail', phone: 'Telefono', educationLevel: 'Titolo di studio', employmentStatus: 'Stato occupazionale', active: 'Attivo' },
	enrollments: { id: 'ID', courseCode: 'Codice corso', courseTitle: 'Corso', courseId: 'Corso ID', participantId: 'Partecipante ID', enrollmentDate: 'Data iscrizione', status: 'Stato' },
	attendance: { id: 'ID', courseCode: 'Codice corso', courseTitle: 'Corso', enrollmentId: 'Iscrizione ID', lessonDate: 'Data lezione', entryTime: 'Entrata', exitTime: 'Uscita', attendedHours: 'Ore frequentate', absent: 'Assente', justification: 'Giustificazione' },
	lessons: { id: 'ID', courseCode: 'Codice corso', courseTitle: 'Corso', courseId: 'Corso ID', title: 'Lezione', lessonDate: 'Data', startTime: 'Inizio', endTime: 'Fine', notes: 'Note' },
	users: { id: 'ID', username: 'Nome utente', firstName: 'Nome', lastName: 'Cognome', email: 'E-mail', role: 'Ruolo', active: 'Attivo' },
	audit: { id: 'ID', actor: 'Utente', action: 'Azione', resource: 'Risorsa', recordId: 'Elemento ID', occurredAt: 'Data e ora' }
};

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
	const method = (options.method || 'GET').toUpperCase();
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
	const result = response.status === 204 ? null : await response.json();
	if (!['GET', 'HEAD'].includes(method)) await recordAuditEvent(url, method, result);
	return result;
}

async function recordAuditEvent(url, method, result) {
	const path = new URL(url).pathname;
	const segments = path.split('/').filter(Boolean);
	const resources = {
		courses: 'COURSE',
		participants: 'PARTICIPANT',
		enrollments: 'ENROLLMENT',
		attendance: 'ATTENDANCE',
		lessons: 'LESSON',
		users: 'USER'
	};
	const resource = resources[segments[1]];
	if (!resource) return;

	const action = method === 'POST' ? 'CREATE'
		: method === 'DELETE' ? 'DELETE'
			: method === 'PATCH' && path.endsWith('/role') ? 'ROLE_CHANGE'
				: method === 'PATCH' && path.endsWith('/status') ? 'STATUS_CHANGE'
					: 'UPDATE';
	const recordId = result?.id || (segments.length > 2 ? segments[2] : null);
	try {
		await fetch(`${API.identity}/api/audit/events`, {
			method: 'POST',
			headers: { ...authHeaders(), 'Content-Type': 'application/json' },
			body: JSON.stringify({ action, resource, recordId })
		});
	} catch { }
}

function renderAuditEvents() {
	const resourceLabels = {
		COURSE: 'Corso', PARTICIPANT: 'Partecipante', ENROLLMENT: 'Iscrizione',
		ATTENDANCE: 'Presenza', LESSON: 'Lezione', USER: 'Utente'
	};
	const actionLabels = {
		CREATE: 'Creazione', UPDATE: 'Modifica', DELETE: 'Eliminazione',
		STATUS_CHANGE: 'Cambio stato', ROLE_CHANGE: 'Cambio ruolo'
	};
	const events = auditPageData?.content || [];
	const hasFilters = $('audit-search').value.trim() || $('audit-resource-filter').value
		|| $('audit-action-filter').value || $('audit-date-from').value || $('audit-date-to').value;
	$('audit-rows').innerHTML = events.length ? events.map(event => `
		<tr>
			<td>${escapeHtml(new Date(event.occurredAt).toLocaleString('it-IT'))}</td>
			<td>${escapeHtml(event.actor)}</td>
			<td>${escapeHtml(actionLabels[event.action] || event.action)}</td>
			<td>${escapeHtml(resourceLabels[event.resource] || event.resource)}</td>
			<td>${escapeHtml(event.recordId || '-')}</td>
		</tr>`).join('') : `<tr><td colspan="5">${hasFilters ? 'Nessuna modifica corrisponde ai filtri.' : 'Nessuna modifica registrata.'}</td></tr>`;
	const totalPages = auditPageData?.totalPages || 0;
	const totalElements = auditPageData?.totalElements || 0;
	$('audit-page-label').textContent = `Pagina ${auditCurrentPage + 1} di ${Math.max(totalPages, 1)} · ${totalElements} modifiche`;
	$('audit-previous').disabled = auditCurrentPage <= 0;
	$('audit-next').disabled = auditCurrentPage + 1 >= totalPages;
}

async function loadAudit() {
	$('audit-rows').innerHTML = '<tr><td colspan="5">Caricamento registro...</td></tr>';
	try {
		const params = new URLSearchParams({ page: String(auditCurrentPage), size: '50' });
		const query = $('audit-search').value.trim();
		const resource = $('audit-resource-filter').value;
		const action = $('audit-action-filter').value;
		const from = $('audit-date-from').value;
		const to = $('audit-date-to').value;
		if (query) params.set('query', query);
		if (resource) params.set('resource', resource);
		if (action) params.set('action', action);
		if (from) params.set('from', from);
		if (to) params.set('to', to);
		auditPageData = await api(`${API.identity}/api/audit/events?${params}`);
		renderAuditEvents();
	} catch (error) {
		$('audit-rows').innerHTML = `<tr><td colspan="5">${escapeHtml(error.message)}</td></tr>`;
		$('audit-page-label').textContent = '';
	}
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

const exportDatasetLabels = {
	courses: 'Corsi', participants: 'Partecipanti', enrollments: 'Iscrizioni',
	attendance: 'Presenze', lessons: 'Lezioni', users: 'Utenti', audit: 'Audit modifiche'
};

async function collectExportData(datasetIds) {
	const needsCourses = datasetIds.some(id => ['courses', 'enrollments', 'attendance', 'lessons'].includes(id));
	const courses = needsCourses ? await getAllPages(`${API.course}/api/courses`) : [];
	const data = {};
	await Promise.all(datasetIds.map(async id => {
		switch (id) {
			case 'courses':
				data[id] = courses;
				break;
			case 'participants':
				data[id] = await getAllPages(`${API.participant}/api/participants`);
				break;
			case 'enrollments':
				data[id] = (await Promise.all(courses.map(async course =>
					(await getAllPages(`${API.enrollment}/api/enrollments/course/${course.id}`))
						.map(enrollment => ({ ...enrollment, courseCode: course.courseCode, courseTitle: course.title }))
				))).flat();
				break;
			case 'attendance':
				data[id] = (await Promise.all(courses.map(async course =>
					(await api(`${API.enrollment}/api/attendance/course/${course.id}`))
						.map(record => ({ ...record, courseCode: course.courseCode, courseTitle: course.title }))
				))).flat();
				break;
			case 'lessons':
				data[id] = (await Promise.all(courses.map(async course =>
					(await api(`${API.enrollment}/api/lessons/course/${course.id}?from=${course.startDate}&to=${course.endDate}`))
						.map(lesson => ({ ...lesson, courseCode: course.courseCode, courseTitle: course.title }))
				))).flat();
				break;
			case 'users':
				data[id] = await getAllPages(`${API.identity}/api/users`);
				break;
			case 'audit':
				data[id] = await getAllPages(`${API.identity}/api/audit/events`);
				break;
		}
	}));
	return data;
}

function exportCell(value) {
	if (value == null) return '';
	return typeof value === 'object' ? JSON.stringify(value) : String(value);
}

function csvForDataset(datasetId, rows) {
	const columns = exportColumns[datasetId];
	const fields = Object.keys(columns);
	const quote = value => {
		let text = exportCell(value);
		if (/^[\t\r=+@-]/.test(text)) text = `'${text}`;
		return `"${text.replace(/"/g, '""')}"`;
	};
	return '\uFEFF' + [
		fields.map(field => quote(columns[field])).join(';'),
		...rows.map(row => fields.map(field => quote(row[field])).join(';'))
	].join('\r\n');
}

function downloadFile(blob, filename) {
	const link = document.createElement('a');
	const objectUrl = URL.createObjectURL(blob);
	link.href = objectUrl;
	link.download = filename;
	document.body.append(link);
	link.click();
	link.remove();
	setTimeout(() => URL.revokeObjectURL(objectUrl), 1000);
}

function exportFilename(datasetIds, extension) {
	const label = datasetIds.length === 1 ? datasetIds[0] : 'selezione';
	return `traininghub_${label}_${new Date().toISOString().slice(0, 10)}.${extension}`;
}

function exportAsJson(data, datasetIds) {
	const report = {
		generatedAt: new Date().toISOString(),
		datasets: Object.fromEntries(datasetIds.map(id => [id, data[id]]))
	};
	downloadFile(new Blob([JSON.stringify(report, null, 2)], { type: 'application/json;charset=utf-8' }),
		exportFilename(datasetIds, 'json'));
}

async function exportAsCsv(data, datasetIds) {
	if (datasetIds.length === 1) {
		downloadFile(new Blob([csvForDataset(datasetIds[0], data[datasetIds[0]])], { type: 'text/csv;charset=utf-8' }),
			exportFilename(datasetIds, 'csv'));
		return;
	}
	if (!window.JSZip) throw new Error('Libreria ZIP non disponibile. Riprova tra qualche istante.');
	const archive = new window.JSZip();
	datasetIds.forEach(id => archive.file(`${id}.csv`, csvForDataset(id, data[id])));
	const blob = await archive.generateAsync({ type: 'blob', compression: 'DEFLATE' });
	downloadFile(blob, exportFilename(datasetIds, 'zip'));
}

function exportAsExcel(data, datasetIds) {
	if (!window.XLSX) throw new Error('Libreria Excel non disponibile. Riprova tra qualche istante.');
	const workbook = window.XLSX.utils.book_new();
	datasetIds.forEach(id => {
		const columns = exportColumns[id];
		const fields = Object.keys(columns);
		const rows = data[id].map(row => fields.map(field => exportCell(row[field])));
		const sheet = window.XLSX.utils.aoa_to_sheet([[...Object.values(columns)], ...rows]);
		const sheetName = exportDatasetLabels[id].replace(/[\\/?*:[\]]/g, ' ').slice(0, 31);
		window.XLSX.utils.book_append_sheet(workbook, sheet, sheetName);
	});
	window.XLSX.writeFile(workbook, exportFilename(datasetIds, 'xlsx'));
}

function exportAsPdf(data, datasetIds) {
	const JsPDF = window.jspdf?.jsPDF;
	if (!JsPDF) throw new Error('Libreria PDF non disponibile. Riprova tra qualche istante.');
	const pdf = new JsPDF({ orientation: 'landscape', unit: 'mm', format: 'a4' });
	datasetIds.forEach((id, index) => {
		if (index > 0) pdf.addPage();
		pdf.setFont('helvetica', 'bold');
		pdf.setFontSize(16);
		pdf.text(`TrainingHub · ${exportDatasetLabels[id]}`, 14, 16);
		pdf.setFont('helvetica', 'normal');
		pdf.setFontSize(9);
		pdf.text(`Generato il ${new Date().toLocaleString('it-IT')}`, 14, 22);
		const columns = exportColumns[id];
		const fields = Object.keys(columns);
		if (!data[id].length) {
			pdf.text('Nessun dato disponibile.', 14, 32);
			return;
		}
		pdf.autoTable({
			startY: 27,
			head: [Object.values(columns)],
			body: data[id].map(row => fields.map(field => exportCell(row[field]))),
			styles: { font: 'helvetica', fontSize: 7, cellPadding: 2, overflow: 'linebreak' },
			headStyles: { fillColor: [25, 53, 43] },
			alternateRowStyles: { fillColor: [242, 246, 242] },
			margin: { left: 14, right: 14 }
		});
	});
	pdf.save(exportFilename(datasetIds, 'pdf'));
}

async function exportReport(format) {
	if (!hasRole('ADMINISTRATOR')) {
		showToast('Solo gli amministratori possono esportare dati.');
		return;
	}
	const datasetIds = [...document.querySelectorAll('[data-export-dataset]:checked')]
		.filter(input => !input.closest('.export-dataset-option').hidden)
		.map(input => input.dataset.exportDataset);
	if (!datasetIds.length) {
		showToast('Seleziona almeno un dataset da esportare.');
		return;
	}
	const buttons = [...document.querySelectorAll('[data-export-format]')];
	buttons.forEach(button => { button.disabled = true; });
	$('export-status').textContent = 'Preparazione del report…';
	try {
		const data = await collectExportData(datasetIds);
		if (format === 'json') exportAsJson(data, datasetIds);
		else if (format === 'csv') await exportAsCsv(data, datasetIds);
		else if (format === 'xlsx') exportAsExcel(data, datasetIds);
		else exportAsPdf(data, datasetIds);
		const total = Object.values(data).reduce((sum, rows) => sum + rows.length, 0);
		$('export-status').textContent = `Report pronto · ${total} record esportati.`;
		showToast('Esportazione completata.');
	} catch (error) {
		$('export-status').textContent = error.message;
		showToast(error.message);
	} finally {
		buttons.forEach(button => { button.disabled = false; });
	}
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
	const auditButton = document.querySelector('[data-section="audit"]');
	const exportsButton = document.querySelector('[data-section="exports"]');
	const courseCreateButton = $('new-course-toggle');
	const participantCreateButton = $('new-participant-toggle');
	participantButton.classList.toggle('hidden', teacher === true);
	enrollmentButton.classList.toggle('hidden', teacher === true);
	attendanceButton.classList.toggle('hidden', false);
	auditButton.classList.toggle('hidden', administrator !== true);
	exportsButton.classList.toggle('hidden', administrator !== true);
	courseCreateButton.classList.toggle('hidden', teacher === true);
	participantCreateButton.classList.toggle('hidden', administrator !== true);
	$('new-lesson-toggle').classList.toggle('hidden', teacher === true);
	$('attendance-form').classList.toggle('hidden', teacher === true);
	$('attendance-heading').textContent = teacher ? 'Presenze dei corsi assegnati' : 'Registra presenza';
	if (teacher) $('attendance-list').classList.remove('hidden');
	configureExportPermissions();
}

function updateExportSelectAll() {
	const options = [...document.querySelectorAll('[data-export-dataset]')]
		.filter(input => !input.closest('.export-dataset-option').hidden);
	const selected = options.filter(input => input.checked).length;
	const selectAll = $('export-select-all');
	selectAll.checked = selected === options.length;
	selectAll.indeterminate = selected > 0 && selected < options.length;
}

function configureExportPermissions() {
	const administrator = hasRole('ADMINISTRATOR');
	document.querySelectorAll('[data-export-dataset]').forEach(input => {
		input.closest('.export-dataset-option').hidden = !administrator;
		input.checked = administrator;
	});
	updateExportSelectAll();
}

function switchSection(name) {
	if (['audit', 'exports'].includes(name) && !hasRole('ADMINISTRATOR')) return;
	document.querySelectorAll('.page-section').forEach(section => section.classList.add('hidden'));
	$(`${name}-section`).classList.remove('hidden');
	document.querySelectorAll('.nav-button').forEach(button => button.classList.toggle('active', button.dataset.section === name));
	$('page-title').textContent = { overview: 'Panoramica', users: 'Utenti', courses: 'Corsi', participants: 'Partecipanti', enrollments: 'Iscrizioni', calendar: 'Calendario', attendance: 'Presenze', audit: 'Audit modifiche', exports: 'Esportazioni' }[name];
	if (name === 'overview') loadOverview();
	if (name === 'courses') loadCourses();
	if (name === 'participants') loadParticipants();
	if (name === 'enrollments') loadEnrollments();
	if (name === 'calendar') loadCalendar();
	if (name === 'attendance') loadAttendance();
	if (name === 'audit') loadAudit();
}

function card(title, meta, badge = '') {
	return `<article class="record-card"><div><strong>${escapeHtml(title)}</strong><small>${escapeHtml(meta)}</small></div>${badge ? `<span class="badge">${escapeHtml(badge)}</span>` : ''}</article>`;
}

function renderChart(canvasId, emptyId, config) {
	const empty = $(emptyId);
	const canvas = $(canvasId);
	if (overviewCharts[canvasId]) {
		overviewCharts[canvasId].destroy();
		delete overviewCharts[canvasId];
	}
	if (!config || typeof Chart === 'undefined') {
		canvas.classList.add('hidden');
		empty.classList.remove('hidden');
		return;
	}
	canvas.classList.remove('hidden');
	empty.classList.add('hidden');
	overviewCharts[canvasId] = new Chart(canvas, config);
}

function countBy(items, key) {
	const counts = new Map();
	items.forEach(item => counts.set(item[key], (counts.get(item[key]) || 0) + 1));
	return counts;
}

const chartColors = {
	primary: '#16735b', accent: '#df855e', deep: '#19352b', danger: '#b84f50', muted: '#66756c'
};

function dashboardChartOptions(showLegend = false, cartesian = false) {
	const reduceMotion = window.matchMedia?.('(prefers-reduced-motion: reduce)').matches;
	const options = {
		responsive: true,
		maintainAspectRatio: false,
		animation: { duration: reduceMotion ? 0 : 850, easing: 'easeOutQuart' },
		interaction: { intersect: false, mode: 'index' },
		plugins: {
			legend: {
				display: showLegend,
				position: 'bottom',
				labels: {
					usePointStyle: true, pointStyle: 'circle', boxWidth: 8, padding: 18,
					color: chartColors.muted, font: { family: 'DM Sans', size: 11 }
				}
			},
			tooltip: {
				backgroundColor: '#19352b', titleColor: '#ffffff', bodyColor: '#edf3ee',
				padding: 12, cornerRadius: 7, boxPadding: 5,
				titleFont: { family: 'DM Sans', size: 12, weight: '600' },
				bodyFont: { family: 'DM Sans', size: 12 },
				callbacks: {
					label: context => ` ${context.dataset.label ? `${context.dataset.label}: ` : ''}${context.parsed?.y ?? context.parsed}`
				}
			}
		}
	};
	if (cartesian) {
		options.scales = {
			x: {
				grid: { display: false }, border: { display: false },
				ticks: { color: chartColors.muted, font: { family: 'DM Sans', size: 11 } }
			},
			y: {
				beginAtZero: true, grid: { color: 'rgb(27 44 36 / 8%)' }, border: { display: false },
				ticks: { precision: 0, color: chartColors.muted, padding: 8, font: { family: 'DM Sans', size: 11 } }
			}
		};
	}
	return options;
}

const doughnutCenterPlugin = {
	id: 'doughnut-center-label',
	beforeDraw(chart) {
		if (!chart.chartArea) return;
		const total = chart.data.datasets[0].data.reduce((sum, value) => sum + value, 0);
		const { left, right, top, bottom } = chart.chartArea;
		const centerX = (left + right) / 2;
		const centerY = (top + bottom) / 2;
		const { ctx } = chart;
		ctx.save();
		ctx.textAlign = 'center';
		ctx.fillStyle = '#1b2c24';
		ctx.font = '600 25px DM Sans';
		ctx.fillText(total, centerX, centerY + 2);
		ctx.fillStyle = '#66756c';
		ctx.font = '600 9px DM Sans';
		ctx.fillText('CORSI', centerX, centerY + 19);
		ctx.restore();
	}
};

function renderCoursesStatusChart(courses) {
	const labels = { SCHEDULED: 'Programmato', IN_PROGRESS: 'In svolgimento', COMPLETED: 'Concluso', CANCELLED: 'Annullato' };
	const colors = { SCHEDULED: chartColors.primary, IN_PROGRESS: chartColors.accent, COMPLETED: chartColors.deep, CANCELLED: chartColors.danger };
	const counts = countBy(courses, 'status');
	const statuses = Object.keys(labels).filter(status => counts.get(status));
	renderChart('chart-courses-status', 'chart-courses-status-empty', statuses.length ? {
		type: 'doughnut',
		data: {
			labels: statuses.map(status => labels[status]),
			datasets: [{
				data: statuses.map(status => counts.get(status)),
				backgroundColor: statuses.map(status => colors[status]),
				borderColor: '#ffffff', borderWidth: 3, borderRadius: 5, hoverOffset: 7
			}]
		},
		options: { ...dashboardChartOptions(true), cutout: '72%' },
		plugins: [doughnutCenterPlugin]
	} : null);
}

function renderEnrollmentsStatusChart(enrollments) {
	const labels = { REQUESTED: 'Richiesta', CONFIRMED: 'Confermata', WITHDRAWN: 'Ritirata', COMPLETED: 'Completata' };
	const colors = { REQUESTED: chartColors.accent, CONFIRMED: chartColors.primary, WITHDRAWN: chartColors.danger, COMPLETED: chartColors.deep };
	const counts = countBy(enrollments, 'status');
	const statuses = Object.keys(labels).filter(status => counts.get(status));
	renderChart('chart-enrollments-status', 'chart-enrollments-status-empty', statuses.length ? {
		type: 'bar',
		data: {
			labels: statuses.map(status => labels[status]),
			datasets: [{
				label: 'Iscrizioni', data: statuses.map(status => counts.get(status)),
				backgroundColor: statuses.map(status => colors[status]), borderRadius: 7,
				borderSkipped: false, maxBarThickness: 48
			}]
		},
		options: dashboardChartOptions(false, true)
	} : null);
}

function renderAttendanceDistributionChart(frequencies) {
	const measured = frequencies.filter(item => Number(item.frequency.totalHours) > 0);
	const buckets = ['0-59%', '60-79%', '80-100%'];
	const counts = [0, 0, 0];
	measured.forEach(item => {
		const percentage = Number(item.frequency.percentage);
		if (percentage < 60) counts[0] += 1;
		else if (percentage < 80) counts[1] += 1;
		else counts[2] += 1;
	});
	renderChart('chart-attendance-distribution', 'chart-attendance-distribution-empty', measured.length ? {
		type: 'bar',
		data: {
			labels: buckets,
			datasets: [{
				label: 'Partecipanti', data: counts,
				backgroundColor: [chartColors.danger, chartColors.accent, chartColors.primary],
				borderRadius: 7, borderSkipped: false, maxBarThickness: 52
			}]
		},
		options: dashboardChartOptions(false, true)
	} : null);
}

async function loadCourses() {
	const query = $('course-search').value.trim();
	const url = `${API.course}/api/courses${query ? `?query=${encodeURIComponent(query)}` : ''}`;
	const courses = await getAllPages(url);
	const status = $('course-filter-status').value;
	const mode = $('course-filter-mode').value;
	const area = $('course-filter-area').value.trim().toLocaleLowerCase('it');
	const dateFrom = $('course-filter-from').value;
	const dateTo = $('course-filter-to').value;
	const minCapacity = Number($('course-filter-capacity').value) || 0;
	const minHours = Number($('course-filter-hours').value) || 0;
	const normalizedQuery = query.toLocaleLowerCase('it');
	state.courses = courses.filter(course => {
		const searchText = `${course.courseCode} ${course.title}`.toLocaleLowerCase('it');
		return (!normalizedQuery || searchText.includes(normalizedQuery))
			&& (!status || course.status === status)
			&& (!mode || course.mode === mode)
			&& (!area || String(course.trainingArea || '').toLocaleLowerCase('it').includes(area))
			&& (!dateFrom || course.endDate >= dateFrom)
			&& (!dateTo || course.startDate <= dateTo)
			&& Number(course.maximumCapacity) >= minCapacity
			&& Number(course.totalHours) >= minHours;
	});
	$('course-filter-count').textContent = `${state.courses.length} di ${courses.length} corsi`;
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
	const participants = await getAllPages(url);
	const active = $('participant-filter-active').value;
	const education = $('participant-filter-education').value.trim().toLocaleLowerCase('it');
	const employment = $('participant-filter-employment').value.trim().toLocaleLowerCase('it');
	const birthFrom = $('participant-filter-birth-from').value;
	const birthTo = $('participant-filter-birth-to').value;
	state.participants = participants.filter(person =>
		(!active || String(person.active) === active)
		&& (!education || String(person.educationLevel || '').toLocaleLowerCase('it').includes(education))
		&& (!employment || String(person.employmentStatus || '').toLocaleLowerCase('it').includes(employment))
		&& (!birthFrom || person.birthDate >= birthFrom)
		&& (!birthTo || person.birthDate <= birthTo));
	$('participant-filter-count').textContent = `${state.participants.length} di ${participants.length} partecipanti`;
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
			renderCoursesStatusChart(courses);
			renderEnrollmentsStatusChart([]);
			renderAttendanceDistributionChart([]);
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
		renderCoursesStatusChart(courses);
		renderEnrollmentsStatusChart(enrollments);
		renderAttendanceDistributionChart(frequencies);
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
$('export-select-all').addEventListener('change', () => {
	document.querySelectorAll('[data-export-dataset]').forEach(input => {
		if (!input.closest('.export-dataset-option').hidden) input.checked = $('export-select-all').checked;
	});
	updateExportSelectAll();
});
document.querySelectorAll('[data-export-dataset]').forEach(input => {
	input.addEventListener('change', updateExportSelectAll);
});
document.querySelectorAll('[data-export-format]').forEach(button => {
	button.addEventListener('click', () => exportReport(button.dataset.exportFormat));
});
document.querySelectorAll('#audit-resource-filter, #audit-action-filter, #audit-date-from, #audit-date-to').forEach(filter => {
	filter.addEventListener('change', () => {
		auditCurrentPage = 0;
		loadAudit();
	});
});
$('audit-filter-reset').addEventListener('click', () => {
	$('audit-search').value = '';
	$('audit-resource-filter').value = '';
	$('audit-action-filter').value = '';
	$('audit-date-from').value = '';
	$('audit-date-to').value = '';
	auditCurrentPage = 0;
	loadAudit();
});
$('audit-search').addEventListener('input', () => {
	clearTimeout(auditSearchTimer);
	auditSearchTimer = setTimeout(() => {
		auditCurrentPage = 0;
		loadAudit();
	}, 250);
});
$('audit-refresh').addEventListener('click', loadAudit);
$('audit-previous').addEventListener('click', async () => {
	if (auditCurrentPage > 0) {
		auditCurrentPage -= 1;
		await loadAudit();
	}
});
$('audit-next').addEventListener('click', async () => {
	if (auditCurrentPage + 1 < (auditPageData?.totalPages || 0)) {
		auditCurrentPage += 1;
		await loadAudit();
	}
});
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

function scheduleCourseLoad() {
	clearTimeout(courseSearchTimer);
	courseSearchTimer = setTimeout(() => loadCourses().catch(error => showToast(error.message)), 250);
}

function scheduleParticipantLoad() {
	clearTimeout(participantSearchTimer);
	participantSearchTimer = setTimeout(() => loadParticipants().catch(error => showToast(error.message)), 250);
}

document.querySelectorAll('#course-search, #course-filter-status, #course-filter-mode, #course-filter-area, #course-filter-from, #course-filter-to, #course-filter-capacity, #course-filter-hours').forEach(field => {
	field.addEventListener('input', scheduleCourseLoad);
	field.addEventListener('change', scheduleCourseLoad);
});
document.querySelectorAll('#participant-search, #participant-filter-active, #participant-filter-education, #participant-filter-employment, #participant-filter-birth-from, #participant-filter-birth-to').forEach(field => {
	field.addEventListener('input', scheduleParticipantLoad);
	field.addEventListener('change', scheduleParticipantLoad);
});
$('course-filter-reset').addEventListener('click', () => {
	$('course-search').value = '';
	document.querySelectorAll('#courses-section .advanced-filters input, #courses-section .advanced-filters select').forEach(field => { field.value = ''; });
	scheduleCourseLoad();
});
$('participant-filter-reset').addEventListener('click', () => {
	$('participant-search').value = '';
	document.querySelectorAll('#participants-section .advanced-filters input, #participants-section .advanced-filters select').forEach(field => { field.value = ''; });
	scheduleParticipantLoad();
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
