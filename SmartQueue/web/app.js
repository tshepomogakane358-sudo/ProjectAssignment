const API = 'http://localhost:7070/api';
let currentPatient = null;

// ─── Section Navigation (Home page About/Services/Contact) ─

function showSection(section) {
    // Make sure home page is visible
    document.querySelectorAll('.page').forEach(p => p.classList.remove('active'));
    document.getElementById('page-home').classList.add('active');

    // Hide all toggleable sections
    const sections = ['section-about', 'section-services', 'section-contact'];
    sections.forEach(id => {
        const el = document.getElementById(id);
        if (el) el.style.display = 'none';
    });

    // Show hero + features when going home
    const hero     = document.getElementById('section-home');
    const features = document.getElementById('section-features');
    const footer   = document.querySelector('#page-home .site-footer');

    if (section === 'home') {
        if (hero)     hero.style.display     = '';
        if (features) features.style.display = '';
        if (footer)   footer.style.display   = '';
        // Highlight Home nav link
        document.querySelectorAll('.nav-link').forEach((l, i) => {
            l.classList.toggle('active', i === 0);
        });
    } else {
        // Hide hero + features when viewing sub-sections
        if (hero)     hero.style.display     = 'none';
        if (features) features.style.display = 'none';
        if (footer)   footer.style.display   = '';

        const target = document.getElementById('section-' + section);
        if (target) target.style.display = '';

        // Highlight matching nav link
        const map = { about: 1, services: 2, contact: 3 };
        document.querySelectorAll('.nav-link').forEach((l, i) => {
            l.classList.toggle('active', i === map[section]);
        });
    }
    window.scrollTo({ top: 0, behavior: 'smooth' });
}

// Contact form submit
function sendContact() {
    const name    = document.getElementById('contact-name')?.value.trim();
    const email   = document.getElementById('contact-email')?.value.trim();
    const message = document.getElementById('contact-message')?.value.trim();
    const msgEl   = document.getElementById('contact-msg');

    if (!name || !email || !message) {
        if (msgEl) { msgEl.textContent = 'Please fill in all required fields.'; msgEl.className = 'msg error'; }
        return;
    }
    if (msgEl) { msgEl.textContent = 'Message sent! We will get back to you within 24 hours.'; msgEl.className = 'msg success'; }
    document.getElementById('contact-name').value    = '';
    document.getElementById('contact-email').value   = '';
    document.getElementById('contact-message').value = '';
}

// ─── Page Navigation ───────────────────────────────────────

function showPage(id) {
    document.querySelectorAll('.page').forEach(p => p.classList.remove('active'));
    document.getElementById(id).classList.add('active');

    if (id === 'page-home') {
        showSection('home');
        return;
    }
    if (id === 'page-patient') {
        loadDepartments();
        loadAppointments();
        updateDashboardDate();
    }
    if (id === 'page-queue-display') {
        loadQueueDisplay();
        startDisplayClock();
    }
    if (id === 'page-admin') {
        const el = document.getElementById('admin-date');
        if (el) el.textContent = todayStr() + ' | ' + nowStr();
    }
}

// ─── Patient Tab Navigation ────────────────────────────────

function showPatientTab(tabId, el) {
    document.querySelectorAll('.ptab').forEach(t => t.classList.remove('active'));
    document.querySelectorAll('#page-patient .side-link').forEach(l => l.classList.remove('active'));
    const tab = document.getElementById(tabId);
    if (tab) tab.classList.add('active');
    if (el)  el.classList.add('active');

    if (tabId === 'tab-history')      loadAppointments();
    if (tabId === 'tab-queue-status') loadMyQueueStatus();
    if (tabId === 'tab-book')         loadDepartments();
    if (tabId === 'tab-profile')      loadProfile();
}

// ─── Staff Tab Navigation ──────────────────────────────────
function showStaffTab(tabId, el) {
    document.querySelectorAll('.stab').forEach(t => t.classList.add('hidden'));
    document.querySelectorAll('#staff-app .side-link').forEach(l => l.classList.remove('active'));
    document.getElementById(tabId).classList.remove('hidden');
    if (el) el.classList.add('active');
    if (tabId === 'stab-queue')        loadDepartments();
    if (tabId === 'stab-dashboard')    loadStaffStats();
    if (tabId === 'stab-appointments') loadStaffAppointments();
    if (tabId === 'stab-patients')     loadStaffPatients();
    if (tabId === 'stab-reports')      loadStaffReports();
}

async function loadStaffPatients() {
    const container = document.getElementById('staff-patients-list');
    if (!container) return;

    try {
        const res      = await apiFetch('/patients');
        const patients = await res.json();

        if (!patients.length) {
            container.innerHTML =
                '<div class="empty-state">No patients registered.</div>';
            return;
        }

        container.innerHTML = `
            <table>
                <thead>
                    <tr>
                        <th>ID</th><th>Name</th>
                        <th>Contact</th><th>Email</th>
                    </tr>
                </thead>
                <tbody>
                    ${patients.map(p => `
                    <tr>
                        <td>${p.patientId}</td>
                        <td>${p.name}</td>
                        <td>${p.contact}</td>
                        <td>${p.email}</td>
                    </tr>`).join('')}
                </tbody>
            </table>`;
    } catch {
        container.innerHTML =
            '<div class="empty-state">Could not load patients.</div>';
    }
}

async function loadStaffAppointments() {
    const container = document.getElementById('staff-appointments-list');
    const scopeSel  = document.getElementById('staff-appt-scope');
    const scope     = scopeSel ? scopeSel.value : 'today';
    if (!container) return;

    try {
        const res  = await apiFetch('/appointments?scope=' + scope);
        const list = await res.json();

        if (!list.length) {
            container.innerHTML =
                '<div class="empty-state">No appointments found.</div>';
            return;
        }

        container.innerHTML = `
            <table>
                <thead>
                    <tr>
                        <th>ID</th><th>Patient ID</th><th>Department ID</th>
                        <th>Date</th><th>Time</th><th>Status</th><th>Action</th>
                    </tr>
                </thead>
                <tbody>
                    ${list.map(a => `
                    <tr>
                        <td>${a.appointmentId}</td>
                        <td>${a.patientId}</td>
                        <td>${a.departmentId}</td>
                        <td>${a.appointmentDate}</td>
                        <td>${a.appointmentTime}</td>
                        <td>${badgeHtml(a.status)}</td>
                        <td style="display:flex;gap:6px">
                            ${a.status !== 'Done' && a.status !== 'Cancelled' ? `
                                <button class="btn btn-sm btn-success"
                                    onclick="updateAppointmentStatus(${a.appointmentId}, 'Confirmed')">
                                    Confirm
                                </button>
                                <button class="btn btn-sm btn-success"
                                    onclick="updateAppointmentStatus(${a.appointmentId}, 'Done')">
                                    Done
                                </button>
                                <button class="btn btn-sm btn-danger"
                                    onclick="updateAppointmentStatus(${a.appointmentId}, 'Cancelled')">
                                    Cancel
                                </button>
                            ` : '—'}
                        </td>
                    </tr>`).join('')}
                </tbody>
            </table>`;
    } catch {
        container.innerHTML =
            '<div class="empty-state">Could not load appointments.</div>';
    }
}

async function updateAppointmentStatus(id, status) {
    try {
        const res = await apiFetch('/appointments/' + id + '/status', {
            method: 'PUT',
            body: JSON.stringify({ status })
        });
        if (res.ok) {
            showMsg('staff-appt-msg', 'Appointment ' + id + ' marked as ' + status + '.', 'success');
            loadStaffAppointments();
        } else {
            showMsg('staff-appt-msg', 'Could not update appointment.', 'error');
        }
    } catch {
        showMsg('staff-appt-msg', 'Cannot connect to server.', 'error');
    }
}

async function loadStaffReports() {
    const perfEl = document.getElementById('staff-dept-performance');

    try {
        const res  = await apiFetch('/reports/staff-summary');
        const data = await res.json();

        const totalEl     = document.getElementById('rep-total');
        const completedEl = document.getElementById('rep-completed');
        const cancelledEl = document.getElementById('rep-cancelled');
        const noshowEl    = document.getElementById('rep-noshow');
        if (totalEl)     totalEl.textContent     = data.totalAppointments;
        if (completedEl) completedEl.textContent = data.completed;
        if (cancelledEl) cancelledEl.textContent = data.cancelled;
        if (noshowEl)    noshowEl.textContent    = data.noShow;

        if (!perfEl) return;

        if (!data.departments || !data.departments.length) {
            perfEl.innerHTML = '<div class="empty-state">No department data available.</div>';
            return;
        }

        perfEl.innerHTML = `
            <table>
                <thead>
                    <tr>
                        <th>Department</th><th>Waiting</th><th>Served Today</th><th>No-show Today</th>
                    </tr>
                </thead>
                <tbody>
                    ${data.departments.map(d => `
                    <tr>
                        <td>${d.name}</td>
                        <td>${d.waiting}</td>
                        <td>${d.served}</td>
                        <td>${d.noShow}</td>
                    </tr>`).join('')}
                </tbody>
            </table>`;
    } catch {
        if (perfEl) perfEl.innerHTML = '<div class="empty-state">Could not load reports.</div>';
    }
}


// ─── Admin Tab Navigation ──────────────────────────────────
function showAdminTab(tabId, el) {
    document.querySelectorAll('.atab').forEach(t => t.classList.add('hidden'));
    document.querySelectorAll('#admin-app .side-link').forEach(l => l.classList.remove('active'));
    document.getElementById(tabId).classList.remove('hidden');
    if (el) el.classList.add('active');
    if (tabId === 'atab-users')        loadPatients();
    if (tabId === 'atab-settings')     loadAdminDepts();
    if (tabId === 'atab-dashboard')    loadAdminStats();
    if (tabId === 'atab-appointments') loadAdminAppointments();
    if (tabId === 'atab-reports')      populateReportDeptFilter();
}

async function loadAdminAppointments() {
    const container = document.getElementById('admin-appointments-list');
    const scopeSel  = document.getElementById('admin-appt-scope');
    const scope     = scopeSel ? scopeSel.value : 'today';
    if (!container) return;

    try {
        const res  = await apiFetch('/appointments?scope=' + scope);
        const list = await res.json();

        if (!list.length) {
            container.innerHTML =
                '<div class="empty-state">No appointments found.</div>';
            return;
        }

        container.innerHTML = `
            <table>
                <thead>
                    <tr>
                        <th>ID</th><th>Patient ID</th><th>Department ID</th>
                        <th>Date</th><th>Time</th><th>Status</th><th>Action</th>
                    </tr>
                </thead>
                <tbody>
                    ${list.map(a => `
                    <tr>
                        <td>${a.appointmentId}</td>
                        <td>${a.patientId}</td>
                        <td>${a.departmentId}</td>
                        <td>${a.appointmentDate}</td>
                        <td>${a.appointmentTime}</td>
                        <td>${badgeHtml(a.status)}</td>
                        <td style="display:flex;gap:6px">
                            ${a.status !== 'Done' && a.status !== 'Cancelled' ? `
                                <button class="btn btn-sm btn-success"
                                    onclick="updateAdminAppointmentStatus(${a.appointmentId}, 'Confirmed')">
                                    Confirm
                                </button>
                                <button class="btn btn-sm btn-success"
                                    onclick="updateAdminAppointmentStatus(${a.appointmentId}, 'Done')">
                                    Done
                                </button>
                                <button class="btn btn-sm btn-danger"
                                    onclick="updateAdminAppointmentStatus(${a.appointmentId}, 'Cancelled')">
                                    Cancel
                                </button>
                            ` : '—'}
                        </td>
                    </tr>`).join('')}
                </tbody>
            </table>`;
    } catch {
        container.innerHTML =
            '<div class="empty-state">Could not load appointments.</div>';
    }
}

async function updateAdminAppointmentStatus(id, status) {
    try {
        const res = await apiFetch('/appointments/' + id + '/status', {
            method: 'PUT',
            body: JSON.stringify({ status })
        });
        if (res.ok) {
            showMsg('admin-appt-msg', 'Appointment ' + id + ' marked as ' + status + '.', 'success');
            loadAdminAppointments();
        } else {
            showMsg('admin-appt-msg', 'Could not update appointment.', 'error');
        }
    } catch {
        showMsg('admin-appt-msg', 'Cannot connect to server.', 'error');
    }
}

// ─── Admin Reports ─────────────────────────────────────────

// Holds the most recently generated report data so Export can reuse it
// without making another network request.
let lastReportData = null;

function showReportTab(tab) {
    document.querySelectorAll('.report-subtab').forEach(el => el.classList.add('hidden'));
    document.querySelectorAll('.tabs-bar .tab-btn').forEach(btn => btn.classList.remove('active'));

    const target = document.getElementById('rtab-' + tab);
    const btn    = document.getElementById('rtab-btn-' + tab);
    if (target) target.classList.remove('hidden');
    if (btn)    btn.classList.add('active');
}

async function populateReportDeptFilter() {
    const sel = document.getElementById('report-dept');
    if (!sel || sel.dataset.loaded === 'true') return;
    try {
        const res   = await apiFetch('/departments');
        const depts = await res.json();
        depts.forEach(d => {
            const opt = document.createElement('option');
            opt.value = d.departmentId;
            opt.textContent = d.name;
            sel.appendChild(opt);
        });
        sel.dataset.loaded = 'true';
    } catch {
        console.error('Could not load department filter options');
    }
}

async function generateReport() {
    hideMsg('report-msg');

    const deptFilter = document.getElementById('report-dept')?.value || '';
    const dateFrom    = document.getElementById('report-from')?.value || '';

    try {
        // --- Summary (Overview + Queue Performance) ---
        const summaryRes = await apiFetch('/reports/staff-summary');
        const summary     = await summaryRes.json();

        // --- Appointments (filtered client-side by department/date if set) ---
        const apptRes = await apiFetch('/appointments?scope=all');
        let appts      = await apptRes.json();

        if (deptFilter) {
            appts = appts.filter(a => String(a.departmentId) === String(deptFilter));
        }
        if (dateFrom) {
            appts = appts.filter(a => a.appointmentDate >= dateFrom);
        }

        let deptPerf = summary.departments || [];
        if (deptFilter) {
            deptPerf = deptPerf.filter(d => String(d.departmentId) === String(deptFilter));
        }

        lastReportData = { summary, appts, deptPerf };

        renderOverview(summary);
        renderAppointmentsReport(appts);
        renderQueuePerformance(deptPerf);

        showMsg('report-msg', 'Report generated successfully!', 'success');
    } catch {
        showMsg('report-msg', 'Could not generate report. Cannot connect to server.', 'error');
    }
}

function renderOverview(summary) {
    const totalEl     = document.getElementById('rep-total');
    const completedEl = document.getElementById('rep-completed');
    const cancelledEl = document.getElementById('rep-cancelled');
    const noshowEl    = document.getElementById('rep-noshow');
    if (totalEl)     totalEl.textContent     = summary.totalAppointments;
    if (completedEl) completedEl.textContent = summary.completed;
    if (cancelledEl) cancelledEl.textContent = summary.cancelled;
    if (noshowEl)    noshowEl.textContent    = summary.noShow;
}

function renderAppointmentsReport(appts) {
    const container = document.getElementById('report-appointments-list');
    if (!container) return;

    if (!appts.length) {
        container.innerHTML = '<div class="empty-state">No appointments match the selected filters.</div>';
        return;
    }

    container.innerHTML = `
        <table>
            <thead>
                <tr>
                    <th>ID</th><th>Patient ID</th><th>Department ID</th>
                    <th>Date</th><th>Time</th><th>Status</th>
                </tr>
            </thead>
            <tbody>
                ${appts.map(a => `
                <tr>
                    <td>${a.appointmentId}</td>
                    <td>${a.patientId}</td>
                    <td>${a.departmentId}</td>
                    <td>${a.appointmentDate}</td>
                    <td>${a.appointmentTime}</td>
                    <td>${badgeHtml(a.status)}</td>
                </tr>`).join('')}
            </tbody>
        </table>`;
}

function renderQueuePerformance(deptPerf) {
    const container = document.getElementById('report-queue-list');
    if (!container) return;

    if (!deptPerf.length) {
        container.innerHTML = '<div class="empty-state">No department data available.</div>';
        return;
    }

    container.innerHTML = `
        <table>
            <thead>
                <tr>
                    <th>Department</th><th>Waiting</th><th>Served Today</th><th>No-show Today</th>
                </tr>
            </thead>
            <tbody>
                ${deptPerf.map(d => `
                <tr>
                    <td>${d.name}</td>
                    <td>${d.waiting}</td>
                    <td>${d.served}</td>
                    <td>${d.noShow}</td>
                </tr>`).join('')}
            </tbody>
        </table>`;
}

function exportReportCsv(type) {
    if (!lastReportData) {
        showMsg('report-msg', 'Please click Generate Report first.', 'error');
        showReportTab('export');
        return;
    }

    let rows = [];
    let filename = 'report.csv';

    if (type === 'appointments') {
        rows = [['Appointment ID', 'Patient ID', 'Department ID', 'Date', 'Time', 'Status']];
        lastReportData.appts.forEach(a => {
            rows.push([a.appointmentId, a.patientId, a.departmentId, a.appointmentDate, a.appointmentTime, a.status]);
        });
        filename = 'appointments_report.csv';
    } else if (type === 'queue') {
        rows = [['Department', 'Waiting', 'Served Today', 'No-show Today']];
        lastReportData.deptPerf.forEach(d => {
            rows.push([d.name, d.waiting, d.served, d.noShow]);
        });
        filename = 'queue_performance_report.csv';
    }

    const csvContent = rows.map(r => r.map(cell => `"${String(cell).replace(/"/g, '""')}"`).join(',')).join('\n');
    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
    const url  = URL.createObjectURL(blob);

    const link = document.createElement('a');
    link.href = url;
    link.download = filename;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    URL.revokeObjectURL(url);
}

// ─── Helpers ───────────────────────────────────────────────

function todayStr() {
    return new Date().toLocaleDateString('en-ZA', {
        year: 'numeric', month: 'long', day: 'numeric'
    });
}

function nowStr() {
    return new Date().toLocaleTimeString('en-ZA', {
        hour: '2-digit', minute: '2-digit'
    });
}

function updateDashboardDate() {
    const d = todayStr() + ' | ' + nowStr();
    ['dash-date', 'book-date-display', 'staff-queue-date', 'staff-date'].forEach(id => {
        const el = document.getElementById(id);
        if (el) el.textContent = d;
    });
}

function showMsg(id, text, type) {
    const el = document.getElementById(id);
    if (!el) return;
    el.textContent = text;
    el.className   = 'msg ' + type;
}

function hideMsg(id) {
    const el = document.getElementById(id);
    if (el) el.className = 'msg hidden';
}

function badgeHtml(status) {
    const cls = {
        Waiting:      'badge-waiting',
        Called:       'badge-called',
        Serving:      'badge-serving',
        'In-Progress':'badge-serving',
        Done:         'badge-done',
        Pending:      'badge-pending',
        Confirmed:    'badge-confirmed',
        Cancelled:    'badge-cancelled'
    }[status] || '';
    return `<span class="badge ${cls}">${status}</span>`;
}

async function apiFetch(path, options = {}) {
    const res = await fetch(API + path, {
        headers: { 'Content-Type': 'application/json' },
        ...options
    });
    return res;
}

// ─── Auth ──────────────────────────────────────────────────

async function login() {
    hideMsg('login-error');
    const email    = document.getElementById('login-email').value.trim();
    const password = document.getElementById('login-password').value.trim();

    if (!email || !password) {
        showMsg('login-error', 'Please enter email and password.', 'error');
        return;
    }

    // Staff shortcut
    if (email === 'staff' && password === 'staff123') {
        showPage('page-staff');
        staffLogin(true);
        return;
    }

    // Admin shortcut
    if (email === 'admin' && password === 'admin123') {
        showPage('page-admin');
        adminLogin(true);
        return;
    }

    // Block wrong password for reserved usernames
    if (email === 'staff' || email === 'admin') {
        showMsg('login-error', 'Invalid credentials.', 'error');
        return;
    }

    // Patient login via API
    try {
        const res  = await apiFetch('/login', {
            method: 'POST',
            body: JSON.stringify({ email, password })
        });
        const data = await res.json();

        if (res.ok) {
            currentPatient = data;
            const firstName = data.name.split(' ')[0];
            const greeting  = document.getElementById('dash-greeting');
            if (greeting) greeting.textContent = 'Hello, ' + firstName + '!';

            const sidebarName = document.getElementById('sidebar-name');
            if (sidebarName) sidebarName.textContent = data.name;

            const initials = data.name.split(' ').map(n => n[0]).join('').toUpperCase();
            document.querySelectorAll('.user-avatar').forEach(el => {
                if (el.closest('#page-patient')) el.textContent = initials;
            });
            const pa = document.getElementById('profile-avatar');
            const pn = document.getElementById('profile-name');
            const pe = document.getElementById('profile-email');
            if (pa) pa.textContent = initials;
            if (pn) pn.textContent = data.name;
            if (pe) pe.textContent = data.email;

            showPage('page-patient');
        } else {
            showMsg('login-error', data.error || 'Login failed.', 'error');
        }
    } catch {
        showMsg('login-error', 'Cannot connect to server. Is Java running?', 'error');
    }
}

async function register() {
    hideMsg('reg-msg');
    const name     = document.getElementById('reg-name').value.trim();
    const contact  = document.getElementById('reg-contact').value.trim();
    const email    = document.getElementById('reg-email').value.trim();
    const password = document.getElementById('reg-password').value.trim();
    const confirm  = document.getElementById('reg-confirm').value.trim();
    const terms    = document.getElementById('reg-terms').checked;

    if (!terms) {
        showMsg('reg-msg', 'Please accept the terms and conditions.', 'error');
        return;
    }
    if (password !== confirm) {
        showMsg('reg-msg', 'Passwords do not match.', 'error');
        return;
    }

    try {
        const res  = await apiFetch('/register', {
            method: 'POST',
            body: JSON.stringify({ name, contact, email, password })
        });
        const data = await res.json();

        if (res.ok) {
            showMsg('reg-msg', 'Account created successfully! Redirecting to login...', 'success');
            setTimeout(() => showPage('page-login'), 1800);
        } else {
            showMsg('reg-msg', data.error || 'Registration failed.', 'error');
        }
    } catch {
        showMsg('reg-msg', 'Cannot connect to server.', 'error');
    }
}

function logout() {
    currentPatient = null;
    showPage('page-logout');
}

// ─── Departments ───────────────────────────────────────────

async function loadDepartments() {
    try {
        const res   = await apiFetch('/departments');
        const depts = await res.json();

        ['book-dept', 'qs-dept', 'staff-dept'].forEach(id => {
            const sel = document.getElementById(id);
            if (!sel) return;
            sel.innerHTML = depts.map(d =>
                `<option value="${d.departmentId}">${d.name} (limit: ${d.dailyLimit})</option>`
            ).join('');
        });
    } catch {
        console.error('Could not load departments');
    }
}

function updateSummary() {
    const deptSel = document.getElementById('book-dept');
    const dateEl  = document.getElementById('book-date');
    const timeEl  = document.getElementById('book-time');
    if (!deptSel) return;
    const deptText = deptSel.options[deptSel.selectedIndex]?.text.split(' (')[0] || '—';
    const sd    = document.getElementById('sum-dept');
    const sdate = document.getElementById('sum-date');
    const stime = document.getElementById('sum-time');
    if (sd)    sd.textContent    = deptText;
    if (sdate) sdate.textContent = dateEl?.value || '—';
    if (stime) stime.textContent = timeEl?.value || '—';
}

// ─── Appointments ──────────────────────────────────────────

async function bookAppointment() {
    if (!currentPatient) return;
    hideMsg('book-msg');

    const deptId = document.getElementById('book-dept').value;
    const date   = document.getElementById('book-date').value;
    const time   = document.getElementById('book-time').value;

    if (!deptId || !date || !time) {
        showMsg('book-msg', 'Please fill in all fields.', 'error');
        return;
    }

    try {
        const res  = await apiFetch('/appointments', {
            method: 'POST',
            body: JSON.stringify({
                patientId:    currentPatient.patientId,
                departmentId: parseInt(deptId),
                date, time
            })
        });
        const data = await res.json();

        if (res.ok) {
            const deptText = document.getElementById('book-dept')
                .options[document.getElementById('book-dept').selectedIndex]
                .text.split(' (')[0];

            // Store last booked department for queue status
            try { localStorage.setItem('lastDeptId', deptId); } catch {}

            const queueNum = data.queueNumber || '—';
            const cnEl = document.getElementById('confirm-number');
            const cdEl = document.getElementById('confirm-details');
            if (cnEl) cnEl.textContent = queueNum;
            if (cdEl) cdEl.innerHTML =
                `<div>Date: ${date}</div><div>Time: ${time}</div><div>Department: ${deptText}</div>`;

            showPage('page-confirmation');
        } else {
            showMsg('book-msg', data.error || 'Booking failed.', 'error');
        }
    } catch {
        showMsg('book-msg', 'Cannot connect to server.', 'error');
    }
}

async function loadAppointments() {
    if (!currentPatient) return;
    const container = document.getElementById('appointments-list');
    if (!container) return;

    try {
        const res  = await apiFetch('/appointments/' + currentPatient.patientId);
        const list = await res.json();

        if (!list.length) {
            container.innerHTML = '<div class="empty-state">No appointments found.</div>';
            return;
        }

        const upcoming = list.find(a => a.status !== 'Cancelled');
        if (upcoming) {
            const ud = document.getElementById('upcoming-date');
            if (ud) ud.textContent = upcoming.appointmentDate + ' | ' + upcoming.appointmentTime;
        }

        container.innerHTML = `
            <table>
                <thead>
                    <tr><th>ID</th><th>Department</th><th>Date</th><th>Time</th><th>Status</th><th>Action</th></tr>
                </thead>
                <tbody>
                    ${list.map(a => `
                    <tr>
                        <td>${a.appointmentId}</td>
                        <td>Dept ${a.departmentId}</td>
                        <td>${a.appointmentDate}</td>
                        <td>${a.appointmentTime}</td>
                        <td>${badgeHtml(a.status)}</td>
                        <td>${a.status !== 'Cancelled'
            ? `<button class="btn btn-sm btn-danger" onclick="cancelAppointment(${a.appointmentId})">Cancel</button>`
            : '—'}</td>
                    </tr>`).join('')}
                </tbody>
            </table>`;
    } catch {
        container.innerHTML = '<div class="empty-state">Could not load appointments.</div>';
    }
}

async function cancelAppointment(id) {
    if (!confirm('Cancel this appointment?')) return;
    await apiFetch('/appointments/' + id, { method: 'DELETE' });
    loadAppointments();
}

// ─── Queue Status ──────────────────────────────────────────

async function loadMyQueueStatus() {
    if (!currentPatient) return;

    const qsDeptSel = document.getElementById('qs-dept');
    let deptId = qsDeptSel?.value;

    if (!deptId) {
        try { const last = localStorage.getItem('lastDeptId'); if (last && qsDeptSel) { qsDeptSel.value = last; deptId = last; } } catch {}
    }

    if (!deptId) {
        const els = ['qs-my-number','qs-position','qs-wait','qs-now-serving'];
        els.forEach(id => { const el = document.getElementById(id); if (el) el.textContent = '—'; });
        const nl = document.getElementById('qs-next-list');
        if (nl) nl.innerHTML = '<div class="empty-state" style="padding:20px">Select a department to view queue status</div>';
        return;
    }

    try {
        const res   = await apiFetch('/queue/' + deptId);
        const queue = await res.json();

        const waiting = queue.filter(q => q.status === 'Waiting');
        const myEntry = queue.find(q =>
            q.patientId === currentPatient.patientId &&
            ['Waiting','Called','Serving'].includes(q.status));

        const serving = queue.find(q => q.status === 'Called' || q.status === 'Serving') || queue[0];
        const nsEl = document.getElementById('qs-now-serving');
        if (nsEl) nsEl.textContent = serving ? serving.queueNumber : '—';

        const mnEl  = document.getElementById('qs-my-number');
        const posEl = document.getElementById('qs-position');
        const wEl   = document.getElementById('qs-wait');

        if (myEntry) {
            if (mnEl) mnEl.textContent = myEntry.queueNumber;
            if (myEntry.status === 'Waiting') {
                const pos = waiting.indexOf(myEntry) + 1;
                if (posEl) posEl.textContent = pos;
                const wait = myEntry.estimatedWaitMin ?? (pos * 10);
                if (wEl) wEl.textContent = wait + ' - ' + (wait + 10) + ' mins';
            } else {
                if (posEl) posEl.textContent = '0';
                if (wEl)   wEl.textContent   = 'Now';
            }
        } else {
            if (mnEl)  mnEl.textContent  = '—';
            if (posEl) posEl.textContent = '—';
            if (wEl)   wEl.textContent   = '—';
        }

        const nextList = document.getElementById('qs-next-list');
        if (nextList) {
            nextList.innerHTML = waiting.length
                ? waiting.slice(0, 5).map(q => `
                    <div class="next-item">
                        <span class="next-number">${q.queueNumber}</span>
                        <span style="font-size:12px;color:#5f6368">Counter 1</span>
                    </div>`).join('')
                : '<div class="empty-state" style="padding:16px">No patients waiting</div>';
        }
    } catch {
        console.error('Could not load queue status');
    }
}

// ─── Staff ─────────────────────────────────────────────────

function staffLogin(skipCheck = false) {
    if (!skipCheck) {
        const user = document.getElementById('staff-user').value.trim();
        const pass = document.getElementById('staff-pass').value.trim();
        if (user !== 'staff' || pass !== 'staff123') {
            showMsg('staff-login-msg', 'Invalid credentials.', 'error');
            return;
        }
    }
    const ls = document.getElementById('staff-login-screen');
    const sa = document.getElementById('staff-app');
    if (ls) ls.classList.add('hidden');
    if (sa) sa.classList.remove('hidden');
    loadDepartments();
    const sd = document.getElementById('staff-date');
    if (sd) sd.textContent = todayStr() + ' | ' + nowStr();
    showStaffTab('stab-queue');
}

function staffLogout() {
    const ls = document.getElementById('staff-login-screen');
    const sa = document.getElementById('staff-app');
    if (ls) ls.classList.remove('hidden');
    if (sa) sa.classList.add('hidden');
    showPage('page-logout');
}

async function loadStaffStats() {
    try {
        const res   = await apiFetch('/departments');
        const depts = await res.json();

        // Count waiting across all departments
        let totalWaiting = 0;
        let totalDone    = 0;

        for (const d of depts) {
            const qRes  = await apiFetch('/queue/' + d.departmentId);
            const queue = await qRes.json();
            totalWaiting += queue.filter(q => q.status === 'Waiting').length;
            totalDone    += queue.filter(q => q.status === 'Done').length;
        }

        const totalEl   = document.getElementById('staff-total-patients');
        const waitingEl = document.getElementById('staff-waiting');
        const servedEl  = document.getElementById('staff-served');
        if (totalEl)   totalEl.textContent   = totalWaiting + totalDone;
        if (waitingEl) waitingEl.textContent = totalWaiting;
        if (servedEl)  servedEl.textContent  = totalDone;
    } catch {
        console.error('Could not load staff stats');
    }
}

async function loadStaffQueue() {
    const deptId    = document.getElementById('staff-dept').value;
    const container = document.getElementById('staff-queue-list');
    if (!container) return;

    try {
        const res   = await apiFetch('/queue/' + deptId);
        const queue = await res.json();

        if (!queue.length) {
            container.innerHTML = '<div class="empty-state">No patients in queue.</div>';
            return;
        }

        container.innerHTML = `
            <table>
                <thead>
                    <tr><th>Queue No.</th><th>Patient ID</th><th>Department</th><th>Status</th><th>Action</th></tr>
                </thead>
                <tbody>
                    ${queue.map(q => `
                    <tr>
                        <td><span class="queue-number">${q.queueNumber}</span></td>
                        <td>Patient ${q.patientId}</td>
                        <td>Dept ${q.departmentId}</td>
                        <td>${badgeHtml(q.status)}</td>
                        <td style="display:flex;gap:6px">
                            <button class="btn btn-sm btn-success" onclick="markDone(${q.queueId})">Call</button>
                            <button class="btn btn-sm btn-danger" onclick="markNoShow(${q.queueId})">No-show</button>
                        </td>
                    </tr>`).join('')}
                </tbody>
            </table>`;
    } catch {
        container.innerHTML = '<div class="empty-state">Could not load queue.</div>';
    }
}

async function callNext() {
    const deptId = document.getElementById('staff-dept').value;
    try {
        const res  = await apiFetch('/queue/' + deptId + '/next', { method: 'POST' });
        const data = await res.json();
        if (res.ok) {
            showMsg('staff-msg', 'Now calling: Queue #' + data.queueNumber, 'success');
            loadStaffQueue();
        } else {
            showMsg('staff-msg', 'No patients waiting.', 'error');
        }
    } catch {
        showMsg('staff-msg', 'Cannot connect to server.', 'error');
    }
}

async function markDone(queueId) {
    await apiFetch('/queue/' + queueId + '/done', { method: 'PUT' });
    loadStaffQueue();
}

async function markNoShow(queueId) {
    await apiFetch('/queue/' + queueId + '/noshow', { method: 'PUT' });
    loadStaffQueue();
}

// ─── Admin ─────────────────────────────────────────────────

function adminLogin(skipCheck = false) {
    if (!skipCheck) {
        const user = document.getElementById('admin-user').value.trim();
        const pass = document.getElementById('admin-pass').value.trim();
        if (user !== 'admin' || pass !== 'admin123') {
            showMsg('admin-login-msg', 'Invalid credentials.', 'error');
            return;
        }
    }
    const als = document.getElementById('admin-login-screen');
    const aa  = document.getElementById('admin-app');
    if (als) als.classList.add('hidden');
    if (aa)  aa.classList.remove('hidden');
    const ad = document.getElementById('admin-date');
    if (ad) ad.textContent = todayStr() + ' | ' + nowStr();
    showAdminTab('atab-users');
    loadAdminStats();
}

function adminLogout() {
    const als = document.getElementById('admin-login-screen');
    const aa  = document.getElementById('admin-app');
    if (als) als.classList.remove('hidden');
    if (aa)  aa.classList.add('hidden');
    showPage('page-logout');
}

async function loadAdminStats() {
    try {
        const res      = await apiFetch('/patients');
        const patients = await res.json();
        const el = document.getElementById('stat-patients');
        if (el) el.textContent = patients.length;

        const dRes  = await apiFetch('/departments');
        const depts = await dRes.json();
        const perfEl = document.getElementById('dept-perf-list');
        if (perfEl) {
            const colors = ['#1a73e8','#34a853','#fa7b17','#ea4335','#9334e6'];
            perfEl.innerHTML = depts.map((d, i) => {
                const pct = Math.round(Math.random() * 60 + 20);
                return `<div class="dept-perf-item">
                    <span style="width:120px;font-size:13px">${d.name}</span>
                    <div class="perf-bar"><div class="perf-fill" style="width:${pct}%;background:${colors[i % colors.length]}"></div></div>
                    <span style="font-size:12px;width:36px;text-align:right">${pct}%</span>
                </div>`;
            }).join('');
        }
    } catch {
        console.error('Could not load admin stats');
    }
}

async function loadPatients() {
    const container = document.getElementById('patients-list');
    if (!container) return;
    try {
        const res      = await apiFetch('/patients');
        const patients = await res.json();

        if (!patients.length) {
            container.innerHTML = '<div class="empty-state">No patients registered.</div>';
            return;
        }
        container.innerHTML = `
            <table>
                <thead><tr><th>ID</th><th>Name</th><th>Contact</th><th>Email</th></tr></thead>
                <tbody>
                    ${patients.map(p => `
                    <tr>
                        <td>${p.patientId}</td>
                        <td>${p.name}</td>
                        <td>${p.contact}</td>
                        <td>${p.email}</td>
                    </tr>`).join('')}
                </tbody>
            </table>`;
    } catch {
        container.innerHTML = '<div class="empty-state">Could not load patients.</div>';
    }
}

async function loadAdminDepts() {
    const container = document.getElementById('admin-depts-list');
    if (!container) return;
    try {
        const res   = await apiFetch('/departments');
        const depts = await res.json();
        container.innerHTML = `
            <table>
                <thead><tr><th>ID</th><th>Name</th><th>Daily Limit</th></tr></thead>
                <tbody>
                    ${depts.map(d => `
                    <tr><td>${d.departmentId}</td><td>${d.name}</td><td>${d.dailyLimit}</td></tr>
                    `).join('')}
                </tbody>
            </table>`;
    } catch {
        container.innerHTML = '<div class="empty-state">Could not load departments.</div>';
    }
}

async function updateLimit() {
    const deptId = document.getElementById('update-dept-id').value;
    const limit  = document.getElementById('update-limit').value;
    try {
        const res  = await apiFetch('/departments/' + deptId + '/limit', {
            method: 'PUT',
            body: JSON.stringify({ limit: parseInt(limit) })
        });
        const data = await res.json();
        if (res.ok) {
            showMsg('admin-dept-msg', 'Limit updated successfully.', 'success');
            loadAdminDepts();
        } else {
            showMsg('admin-dept-msg', data.error || 'Update failed.', 'error');
        }
    } catch {
        showMsg('admin-dept-msg', 'Cannot connect to server.', 'error');
    }
}

// ─── Queue Display ─────────────────────────────────────────

async function loadQueueDisplay() {
    try {
        const res   = await apiFetch('/departments');
        const depts = await res.json();
        if (!depts.length) return;

        const firstDept = depts[0];
        const qRes  = await apiFetch('/queue/' + firstDept.departmentId);
        const queue = await qRes.json();

        const serving = queue.find(q => q.status === 'Called' || q.status === 'Serving') || queue[0];
        const waiting = queue.filter(q => q.status === 'Waiting');

        const dnEl = document.getElementById('display-now');
        if (dnEl) dnEl.textContent = serving ? serving.queueNumber : '—';

        const nextEl = document.getElementById('display-next-list');
        if (nextEl) {
            nextEl.innerHTML = waiting.length
                ? waiting.slice(0, 5).map((q, i) => `
                    <div class="display-next-item">
                        <span class="next-num-big">${q.queueNumber}</span>
                        <span class="next-counter-text">Counter ${(i % 3) + 1}</span>
                    </div>`).join('')
                : '<div style="color:#94a3b8;padding:20px;text-align:center">No patients waiting</div>';
        }
    } catch {
        console.error('Could not load queue display');
    }
}

function startDisplayClock() {
    const el = document.getElementById('display-datetime');
    if (!el) return;
    function tick() {
        el.textContent = new Date().toLocaleDateString('en-ZA', {
            month: 'long', day: 'numeric', year: 'numeric'
        }) + ' | ' + nowStr();
    }
    tick();
    setInterval(tick, 60000);
}

// ─── Profile ───────────────────────────────────────────────

function loadProfile() {
    if (!currentPatient) return;
    const n  = document.getElementById('profile-name');
    const e  = document.getElementById('profile-email');
    const av = document.getElementById('profile-avatar');
    const initials = currentPatient.name.split(' ').map(x => x[0]).join('').toUpperCase();
    if (n)  n.textContent  = currentPatient.name;
    if (e)  e.textContent  = currentPatient.email;
    if (av) av.textContent = initials;

    const nameInput    = document.getElementById('profile-edit-name');
    const contactInput = document.getElementById('profile-edit-contact');
    const emailInput   = document.getElementById('profile-edit-email');
    if (nameInput)    nameInput.value    = currentPatient.name    || '';
    if (contactInput) contactInput.value = currentPatient.contact || '';
    if (emailInput)   emailInput.value   = currentPatient.email   || '';
    hideMsg('profile-edit-msg');
}

async function updateProfile() {
    if (!currentPatient) return;
    hideMsg('profile-edit-msg');

    const name    = document.getElementById('profile-edit-name')?.value.trim();
    const contact = document.getElementById('profile-edit-contact')?.value.trim();
    const email   = document.getElementById('profile-edit-email')?.value.trim();

    if (!name || !contact || !email) {
        showMsg('profile-edit-msg', 'Please fill in all fields.', 'error');
        return;
    }

    try {
        const res  = await apiFetch('/patients/' + currentPatient.patientId, {
            method: 'PUT',
            body: JSON.stringify({ name, contact, email })
        });
        const data = await res.json();

        if (res.ok) {
            currentPatient = { ...currentPatient, name: data.name, contact: data.contact, email: data.email };
            const initials = data.name.split(' ').map(n => n[0]).join('').toUpperCase();
            const sn = document.getElementById('sidebar-name');
            if (sn) sn.textContent = data.name;
            document.querySelectorAll('.user-avatar').forEach(el => {
                if (el.closest('#page-patient')) el.textContent = initials;
            });
            const pn = document.getElementById('profile-name');
            const pe = document.getElementById('profile-email');
            const pa = document.getElementById('profile-avatar');
            if (pn) pn.textContent = data.name;
            if (pe) pe.textContent = data.email;
            if (pa) pa.textContent = initials;
            const gr = document.getElementById('dash-greeting');
            if (gr) gr.textContent = 'Hello, ' + data.name.split(' ')[0] + '!';
            showMsg('profile-edit-msg', 'Profile updated successfully!', 'success');
        } else {
            showMsg('profile-edit-msg', data.error || 'Update failed.', 'error');
        }
    } catch {
        showMsg('profile-edit-msg', 'Cannot connect to server.', 'error');
    }
}

async function changePassword() {
    if (!currentPatient) return;
    hideMsg('profile-password-msg');

    const currentPassword = document.getElementById('profile-current-password')?.value.trim();
    const newPassword     = document.getElementById('profile-new-password')?.value.trim();
    const confirmPassword = document.getElementById('profile-confirm-password')?.value.trim();

    if (!currentPassword || !newPassword || !confirmPassword) {
        showMsg('profile-password-msg', 'Please fill in all password fields.', 'error');
        return;
    }
    if (newPassword !== confirmPassword) {
        showMsg('profile-password-msg', 'New passwords do not match.', 'error');
        return;
    }
    if (newPassword.length < 6) {
        showMsg('profile-password-msg', 'New password must be at least 6 characters.', 'error');
        return;
    }

    try {
        const res  = await apiFetch('/patients/' + currentPatient.patientId + '/password', {
            method: 'PUT',
            body: JSON.stringify({ currentPassword, newPassword })
        });
        const data = await res.json();

        if (res.ok) {
            showMsg('profile-password-msg', data.message || 'Password updated successfully!', 'success');
            ['profile-current-password','profile-new-password','profile-confirm-password']
                .forEach(id => { const el = document.getElementById(id); if (el) el.value = ''; });
        } else {
            showMsg('profile-password-msg', data.error || 'Could not update password.', 'error');
        }
    } catch {
        showMsg('profile-password-msg', 'Cannot connect to server.', 'error');
    }
}

// ─── Init ──────────────────────────────────────────────────

document.addEventListener('DOMContentLoaded', () => {
    updateDashboardDate();
    // Ensure home page starts correctly
    showSection('home');
});
// (end of file marker)