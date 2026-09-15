const API_BASE = 'http://localhost:8080/api';

document.addEventListener('DOMContentLoaded', () => {
    const formLogin = document.getElementById('form-login');
    if (formLogin) {
        formLogin.addEventListener('submit', login);
    }
});

async function login(event) {
    event.preventDefault();
    const mensaje = document.getElementById('login-mensaje');
    const username = document.getElementById('username').value;
    const password = document.getElementById('password').value;

    try {
        const response = await fetch(`${API_BASE}/auth/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username, password })
        });

        if (!response.ok) {
            throw new Error('Usuario o contraseña incorrectos');
        }

        const data = await response.json();
        localStorage.setItem('jwt_token', data.token);
        localStorage.setItem('jwt_username', data.username);
        localStorage.setItem('jwt_roles', JSON.stringify(data.roles));

        window.location.href = 'index.html';
    } catch (error) {
        mensaje.textContent = error.message;
        mensaje.style.color = 'red';
    }
}

function logout() {
    localStorage.removeItem('jwt_token');
    localStorage.removeItem('jwt_username');
    localStorage.removeItem('jwt_roles');
    window.location.href = 'login.html';
}

function obtenerRoles() {
    return JSON.parse(localStorage.getItem('jwt_roles') || '[]');
}

function tieneRol(rol) {
    return obtenerRoles().includes(rol);
}

async function fetchWithAuth(url, options = {}) {
    const token = localStorage.getItem('jwt_token');

    const headers = {
        'Content-Type': 'application/json',
        ...options.headers,
        'Authorization': `Bearer ${token}`
    };

    const response = await fetch(url, { ...options, headers });

    if (response.status === 401 || response.status === 403) {
        logout();
        throw new Error('Sesión expirada o sin permisos');
    }

    return response;
}