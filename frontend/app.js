const API_BASE = 'http://localhost:8080/api';
const TOKEN_KEY = 'jwt_token';

function guardarToken(token) {
    sessionStorage.setItem(TOKEN_KEY, token);
}

function obtenerToken() {
    return sessionStorage.getItem(TOKEN_KEY);
}

function limpiarSesion() {
    sessionStorage.removeItem(TOKEN_KEY);
}

function decodificarToken(token) {
    try {
        const payload = token.split('.')[1];
        const base64 = payload.replace(/-/g, '+').replace(/_/g, '/');
        const json = decodeURIComponent(
            atob(base64)
                .split('')
                .map(c => '%' + c.charCodeAt(0).toString(16).padStart(2, '0'))
                .join('')
        );
        return JSON.parse(json);
    } catch (error) {
        return null;
    }
}

function obtenerClaims() {
    const token = obtenerToken();
    if (!token) return null;
    return decodificarToken(token);
}

function obtenerRoles() {
    const claims = obtenerClaims();
    return claims && Array.isArray(claims.roles) ? claims.roles : [];
}

function tieneRol(rol) {
    return obtenerRoles().includes(rol);
}

function redirigirALogin() {
    limpiarSesion();
    window.location.href = 'index.html';
}

async function fetchAutenticado(url, options = {}) {
    const token = obtenerToken();

    const headers = {
        'Content-Type': 'application/json',
        ...options.headers,
        ...(token ? { 'Authorization': `Bearer ${token}` } : {})
    };

    const respuesta = await fetch(url, { ...options, headers });

    if (respuesta.status === 401 || respuesta.status === 403) {
        redirigirALogin();
        throw new Error('Sesión expirada o sin permisos suficientes.');
    }

    return respuesta;
}

async function extraerErrores(respuesta) {
    try {
        const cuerpo = await respuesta.json();
        if (cuerpo.errors) return Object.values(cuerpo.errors);
        if (cuerpo.mensaje) return [cuerpo.mensaje];
        if (typeof cuerpo === 'object') return Object.values(cuerpo);
        return ['Ocurrió un error inesperado.'];
    } catch {
        return ['Ocurrió un error inesperado.'];
    }
}

async function manejarLogin(event) {
    event.preventDefault();

    const username = document.getElementById('username').value.trim();
    const password = document.getElementById('password').value;
    const mensajeError = document.getElementById('loginError');
    mensajeError.textContent = '';

    try {
        const respuesta = await fetch(`${API_BASE}/auth/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username, password })
        });

        if (!respuesta.ok) {
            throw new Error('Usuario o contraseña incorrectos.');
        }

        const datos = await respuesta.json();
        guardarToken(datos.token);
        window.location.href = 'dashboard.html';
    } catch (error) {
        mensajeError.textContent = error.message || 'No se pudo iniciar sesión.';
    }
}

let todosLosEnvios = [];
let filtroActual = 'TODOS';

function inicializarDashboard() {
    if (!obtenerToken()) {
        redirigirALogin();
        return;
    }

    const claims = obtenerClaims();
    document.getElementById('nombreUsuario').textContent = claims?.sub || '';
    document.getElementById('btnLogout').addEventListener('click', redirigirALogin);

    aplicarPermisosPorRol();
    cargarEnvios();

    document.querySelectorAll('.filtro-btn').forEach(btn => {
        btn.addEventListener('click', () => {
            document.querySelectorAll('.filtro-btn').forEach(b => b.classList.remove('activo'));
            btn.classList.add('activo');
            filtroActual = btn.dataset.estado;
            renderizarEnvios();
        });
    });
}

function aplicarPermisosPorRol() {
    const asideBitacora = document.getElementById('bitacoraAside');
    if (tieneRol('ROLE_ADMIN')) {
        asideBitacora.hidden = false;
    }
}

async function cargarEnvios() {
    const mensaje = document.getElementById('mensajeGeneral');
    mensaje.textContent = '';

    try {
        const respuesta = await fetchAutenticado(`${API_BASE}/envios/optimizados`);
        if (!respuesta.ok) throw new Error('No se pudieron obtener los envíos.');
        todosLosEnvios = await respuesta.json();
        actualizarKpis();
        renderizarEnvios();
    } catch (error) {
        mensaje.textContent = error.message;
    }
}

function actualizarKpis() {
    const vehiculosUnicos = new Set(
        todosLosEnvios.map(e => e.placaVehiculo).filter(Boolean)
    );
    const entregados = todosLosEnvios.filter(e => e.estadoEnvio === 'ENTREGADO').length;

    document.getElementById('kpiTotal').textContent = todosLosEnvios.length;
    document.getElementById('kpiVehiculos').textContent = vehiculosUnicos.size;
    document.getElementById('kpiEntregados').textContent = entregados;
}

function renderizarEnvios() {
    const grid = document.getElementById('enviosGrid');
    grid.innerHTML = '';

    const enviosFiltrados = filtroActual === 'TODOS'
        ? todosLosEnvios
        : todosLosEnvios.filter(e => e.estadoEnvio === filtroActual);

    if (enviosFiltrados.length === 0) {
        grid.innerHTML = '<p>No hay envíos para este filtro.</p>';
        return;
    }

    enviosFiltrados.forEach(envio => grid.appendChild(crearTarjetaEnvio(envio)));
}

function crearTarjetaEnvio(envio) {
    const card = document.createElement('article');
    card.className = 'envio-card';

    let botonesAccion = '';
    if (tieneRol('ROLE_ADMIN') || tieneRol('ROLE_OPERADOR')) {
        botonesAccion += `<button data-accion="EN_TRANSITO">Marcar En Tránsito</button>`;
    }
    if (tieneRol('ROLE_ADMIN') || tieneRol('ROLE_CONDUCTOR')) {
        botonesAccion += `<button data-accion="ENTREGADO">Marcar Entregado</button>`;
    }
    if (tieneRol('ROLE_ADMIN')) {
        botonesAccion += `<button data-accion="bitacora">Ver Bitácora</button>`;
    }

    card.innerHTML = `
        <span class="pill-status ${envio.estadoEnvio}">${envio.estadoEnvio}</span>
        <h3>${envio.codigoRastreo}</h3>
        <p>${envio.direccionDestino}</p>
        <p>Peso: ${envio.pesoKg} kg — Costo: ₡${envio.costo}</p>
        <p>Vehículo: ${envio.placaVehiculo ?? 'Sin asignar'}</p>
        <p>Conductor: ${envio.nombreConductor ?? 'Sin asignar'}</p>
        <div class="acciones">${botonesAccion}</div>
    `;

    const btnTransito = card.querySelector('[data-accion="EN_TRANSITO"]');
    if (btnTransito) btnTransito.addEventListener('click', () => actualizarEstado(envio.id, 'EN_TRANSITO'));

    const btnEntregado = card.querySelector('[data-accion="ENTREGADO"]');
    if (btnEntregado) btnEntregado.addEventListener('click', () => actualizarEstado(envio.id, 'ENTREGADO'));

    const btnBitacora = card.querySelector('[data-accion="bitacora"]');
    if (btnBitacora) btnBitacora.addEventListener('click', () => cargarBitacora(envio.id, envio.codigoRastreo));

    return card;
}

async function actualizarEstado(envioId, nuevoEstado) {
    const mensaje = document.getElementById('mensajeGeneral');
    mensaje.textContent = '';

    try {
        const respuesta = await fetchAutenticado(`${API_BASE}/envios/${envioId}/estado`, {
            method: 'PATCH',
            body: JSON.stringify({ nuevoEstado, observaciones: `Cambio a ${nuevoEstado} desde la consola` })
        });

        if (respuesta.status === 400) {
            const errores = await extraerErrores(respuesta);
            mensaje.textContent = errores.join(' | ');
            return;
        }
        if (!respuesta.ok) throw new Error('No se pudo actualizar el estado del envío.');

        cargarEnvios();
    } catch (error) {
        mensaje.textContent = error.message;
    }
}

let bitacoraActual = [];

async function cargarBitacora(envioId, codigo) {
    const lista = document.getElementById('bitacoraLista');
    try {
        const respuesta = await fetchAutenticado(`${API_BASE}/envios/${envioId}/bitacora`);
        if (!respuesta.ok) throw new Error('No se pudo obtener la bitácora.');

        bitacoraActual = await respuesta.json();
        document.querySelector('#bitacoraAside h2').textContent = `Bitácora — ${codigo}`;
        renderizarBitacora(bitacoraActual);
        document.getElementById('bitacoraAside').scrollIntoView({ behavior: 'smooth' });
    } catch (error) {
        lista.innerHTML = `<p class="mensaje-error">${error.message}</p>`;
    }
}

function renderizarBitacora(registros) {
    const lista = document.getElementById('bitacoraLista');
    lista.innerHTML = '';

    if (registros.length === 0) {
        lista.innerHTML = '<p>No hay registros de auditoría para este envío.</p>';
        return;
    }

    registros.forEach(registro => {
        const item = document.createElement('div');
        item.className = 'bitacora-item';
        item.innerHTML = `
            <p><strong>${registro.estadoAnterior} → ${registro.estadoNuevo}</strong></p>
            <p>${new Date(registro.fechaCambio).toLocaleString('es-CR')}</p>
            <p>Usuario: ${registro.usuario}</p>
            <p>${registro.observaciones || 'Sin observaciones'}</p>
        `;
        lista.appendChild(item);
    });
}

function filtrarBitacoraPorFecha() {
    const desde = document.getElementById('fechaDesde').value;
    const hasta = document.getElementById('fechaHasta').value;
    let filtrada = bitacoraActual;

    if (desde) filtrada = filtrada.filter(b => b.fechaCambio.slice(0, 10) >= desde);
    if (hasta) filtrada = filtrada.filter(b => b.fechaCambio.slice(0, 10) <= hasta);

    renderizarBitacora(filtrada);
}

document.addEventListener('DOMContentLoaded', () => {
    const formLogin = document.getElementById('loginForm');
    if (formLogin) {
        formLogin.addEventListener('submit', manejarLogin);
    }

    const kpiSection = document.getElementById('kpiSection');
    if (kpiSection) {
        inicializarDashboard();
        document.getElementById('fechaDesde')?.addEventListener('change', filtrarBitacoraPorFecha);
        document.getElementById('fechaHasta')?.addEventListener('change', filtrarBitacoraPorFecha);
    }
});