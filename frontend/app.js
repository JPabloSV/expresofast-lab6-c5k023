const ENVIOS_API_BASE = 'http://localhost:8080/api/envios';

let todosLosEnvios = [];
let filtroActual = 'TODOS';

document.addEventListener('DOMContentLoaded', () => {
    aplicarPermisosPorRol();
    cargarEnvios();
    document.getElementById('form-envio').addEventListener('submit', registrarEnvio);
    document.querySelectorAll('.filtro-btn').forEach(btn => {
        btn.addEventListener('click', () => aplicarFiltro(btn));
    });
    document.getElementById('modal-cerrar').addEventListener('click', cerrarModal);
    document.getElementById('fecha-desde').addEventListener('change', filtrarBitacoraPorFecha);
    document.getElementById('fecha-hasta').addEventListener('change', filtrarBitacoraPorFecha);
});

function aplicarPermisosPorRol() {
    const username = localStorage.getItem('jwt_username');
    document.getElementById('usuario-actual').textContent = username;
    document.getElementById('btn-logout').addEventListener('click', logout);

    if (tieneRol('ROLE_CONDUCTOR') && !tieneRol('ROLE_ADMIN') && !tieneRol('ROLE_OPERADOR')) {
        document.getElementById('form-section').style.display = 'none';
    }
}

async function cargarEnvios() {
    try {
        const response = await fetchWithAuth(`${ENVIOS_API_BASE}/optimizados`);
        if (!response.ok) throw new Error('Error al obtener envíos');
        todosLosEnvios = await response.json();
        renderizarEnvios();
    } catch (error) {
        console.error(error);
    }
}

function aplicarFiltro(btn) {
    document.querySelectorAll('.filtro-btn').forEach(b => b.classList.remove('activo'));
    btn.classList.add('activo');
    filtroActual = btn.dataset.estado;
    renderizarEnvios();
}

function renderizarEnvios() {
    const grid = document.getElementById('envios-grid');
    const contador = document.getElementById('contador-envios');
    grid.innerHTML = '';

    const enviosFiltrados = filtroActual === 'TODOS'
        ? todosLosEnvios
        : todosLosEnvios.filter(e => e.estadoEnvio === filtroActual);

    contador.textContent = `${enviosFiltrados.length} envíos`;

    enviosFiltrados.forEach(envio => {
        grid.appendChild(crearTarjeta(envio));
    });
}

function crearTarjeta(envio) {
    const card = document.createElement('article');
    card.className = 'envio-card';

    const botonBitacora = (tieneRol('ROLE_ADMIN') || tieneRol('ROLE_OPERADOR'))
        ? `<button data-accion="bitacora">Ver Bitácora</button>`
        : '';

    card.innerHTML = `
        <span class="pill-status ${envio.estadoEnvio}">${envio.estadoEnvio}</span>
        <h3>${envio.codigoRastreo}</h3>
        <p>${envio.direccionDestino}</p>
        <p>Peso: ${envio.pesoKg} kg — Costo: ₡${envio.costo}</p>
        <p>Vehículo: ${envio.placaVehiculo}</p>
        <p>Conductor: ${envio.nombreConductor}</p>
        <div class="acciones">
            <button data-accion="EN_TRANSITO">Marcar en Tránsito</button>
            <button data-accion="ENTREGADO">Marcar Entregado</button>
            ${botonBitacora}
        </div>
    `;

    card.querySelector('[data-accion="EN_TRANSITO"]').addEventListener('click', () =>
        actualizarEstado(envio.id, 'EN_TRANSITO'));
    card.querySelector('[data-accion="ENTREGADO"]').addEventListener('click', () =>
        actualizarEstado(envio.id, 'ENTREGADO'));
    const btnBitacora = card.querySelector('[data-accion="bitacora"]');
    if (btnBitacora) {
        btnBitacora.addEventListener('click', () => abrirBitacora(envio.id, envio.codigoRastreo));
    }

    return card;
}

async function registrarEnvio(event) {
    event.preventDefault();
    const mensaje = document.getElementById('form-mensaje');

    const payload = {
        codigoRastreo: document.getElementById('codigoRastreo').value,
        direccionDestino: document.getElementById('direccionDestino').value,
        pesoKg: parseFloat(document.getElementById('pesoKg').value),
        costo: parseFloat(document.getElementById('costo').value),
        vehiculoId: parseInt(document.getElementById('vehiculoId').value),
        conductorId: parseInt(document.getElementById('conductorId').value)
    };

    try {
        const response = await fetchWithAuth(ENVIOS_API_BASE, {
            method: 'POST',
            body: JSON.stringify(payload)
        });

        if (!response.ok) {
            const error = await response.json();
            const primerError = Object.values(error)[0] || 'Error al registrar el envío';
            throw new Error(primerError);
        }

        mensaje.textContent = 'Envío registrado con éxito.';
        mensaje.style.color = 'green';
        event.target.reset();
        cargarEnvios();
    } catch (error) {
        mensaje.textContent = `Error: ${error.message}`;
        mensaje.style.color = 'red';
    }
}

async function actualizarEstado(envioId, nuevoEstado) {
    try {
        const response = await fetchWithAuth(`${ENVIOS_API_BASE}/${envioId}/estado`, {
            method: 'PATCH',
            body: JSON.stringify({ nuevoEstado, observaciones: `Cambio a ${nuevoEstado} desde el tablero` })
        });

        if (!response.ok) {
            const error = await response.json();
            throw new Error(error.mensaje || 'Error al actualizar estado');
        }
        cargarEnvios();
    } catch (error) {
        alert(error.message);
    }
}

let bitacoraActual = [];

async function abrirBitacora(envioId, codigo) {
    try {
        const response = await fetchWithAuth(`${ENVIOS_API_BASE}/${envioId}/bitacora`);
        if (!response.ok) throw new Error('Error al obtener bitácora');
        bitacoraActual = await response.json();

        document.getElementById('modal-titulo').textContent = `Bitácora — ${codigo}`;
        document.getElementById('fecha-desde').value = '';
        document.getElementById('fecha-hasta').value = '';
        renderizarBitacora(bitacoraActual);
        document.getElementById('modal-bitacora').style.display = 'flex';
    } catch (error) {
        console.error(error);
    }
}

// Reto autónomo: filtra las entradas de la bitácora entre una fecha
// inicial y una final seleccionadas en el modal de auditoría.
function filtrarBitacoraPorFecha() {
    const desde = document.getElementById('fecha-desde').value;
    const hasta = document.getElementById('fecha-hasta').value;

    let filtrada = bitacoraActual;

    if (desde) {
        filtrada = filtrada.filter(b => b.fechaCambio.slice(0, 10) >= desde);
    }
    if (hasta) {
        filtrada = filtrada.filter(b => b.fechaCambio.slice(0, 10) <= hasta);
    }

    renderizarBitacora(filtrada);
}

function renderizarBitacora(registros) {
    const lista = document.getElementById('bitacora-lista');
    lista.innerHTML = '';

    if (registros.length === 0) {
        lista.innerHTML = '<p>No hay registros en este rango.</p>';
        return;
    }

    registros.forEach(b => {
        const item = document.createElement('div');
        item.className = 'bitacora-item';
        item.innerHTML = `
            <p><strong>${b.estadoAnterior} → ${b.estadoNuevo}</strong></p>
            <p>${new Date(b.fechaCambio).toLocaleString('es-CR')}</p>
            <p>Usuario: ${b.usuario}</p>
            <p>${b.observaciones || 'Sin observaciones'}</p>
        `;
        lista.appendChild(item);
    });
}

function cerrarModal() {
    document.getElementById('modal-bitacora').style.display = 'none';
}