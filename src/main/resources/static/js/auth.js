// auth.js — Shared helper functions for session management
// We store logged-in user info in sessionStorage (cleared when browser closes)

const BASE_URL = 'http://localhost:8080/api';

// Save user info after login
function saveSession(user) {
    sessionStorage.setItem('user', JSON.stringify(user));
}

// Get current logged-in user (or null)
function getUser() {
    const data = sessionStorage.getItem('user');
    return data ? JSON.parse(data) : null;
}

// Log out — clear session and go to login page
function logout() {
    sessionStorage.removeItem('user');
    window.location.href = '/login.html';
}

// Redirect to login if not logged in
function requireLogin() {
    const user = getUser();
    if (!user) {
        window.location.href = '/login.html';
        return null;
    }
    return user;
}

// Redirect to login if not logged in, AND check role
function requireRole(role) {
    const user = requireLogin();
    if (!user) return null;
    if (user.role !== role) {
        alert('Access denied. This page is for ' + role.toUpperCase() + ' only.');
        window.location.href = '/index.html';
        return null;
    }
    return user;
}

// Build the nav HTML based on current user's role
function buildNav(activePage) {
    const user = getUser();
    if (!user) return;

    const pages = [
        { href: '/index.html',           label: 'Dashboard',    id: 'dashboard' },
        { href: '/add-question.html',    label: 'Add Question', id: 'add' },
        { href: '/view-questions.html',  label: 'Questions',    id: 'view' },
        { href: '/generate-paper.html',  label: 'Generate',     id: 'generate' },
    ];

    // Only COE sees the Pending Approvals page
    if (user.role === 'coe') {
        pages.push({ href: '/pending-approvals.html', label: '⏳ Approvals', id: 'pending' });
    }

    // My Submissions page is only for staff
    if (user.role === 'staff') {
        pages.push({ href: '/my-questions.html', label: 'My Questions', id: 'my' });
    }

    const roleClass = user.role === 'coe' ? 'nav-role-coe' : 'nav-role-staff';
    const roleLabel = user.role === 'coe' ? 'COE' : 'Staff';

    const linksHtml = pages.map(p => `
        <a href="${p.href}" ${p.id === activePage ? 'class="active"' : ''}>${p.label}</a>
    `).join('');

    const navEl = document.querySelector('nav');
    navEl.innerHTML = `
        <a href="/index.html" class="nav-brand">📄 <span>QPGen</span></a>
        <div class="nav-links">
            ${linksHtml}
            <span class="nav-role ${roleClass}">${roleLabel}</span>
            <div class="nav-user-info">
                <span>${user.name}</span>
                <a href="#" onclick="logout()" style="color:rgba(255,255,255,0.5);font-size:0.8rem;text-decoration:none;">Logout</a>
            </div>
        </div>
    `;
}
