(() => {
    const API_V1 = `${API_BASE}/v1/envios`;
    const formatoMoneda = new Intl.NumberFormat('es-CR', { style: 'currency', currency: 'CRC' });

    const estado = { pagina: 0, tamanio: 5, busqueda: '', procedimiento: '', ultimaPagina: 0 };
    let solicitudActual = 0;

    const el = {
        form: document.getElementById('filtrosForm'),
        busqueda: document.getElementById('busqueda'),
        consulta: document.getElementById('consulta'),
        tamanio: document.getElementById('tamanio'),
        btnBuscar: document.getElementById('btnBuscar'),
        body: document.getElementById('enviosBody'),
        modo: document.getElementById('modoConsulta'),
        info: document.getElementById('paginaInfo'),
        primera: document.getElementById('btnPrimera'),
        anterior: document.getElementById('btnAnterior'),
        siguiente: document.getElementById('btnSiguiente'),
        ultima: document.getElementById('btnUltima'),
        mensaje: document.getElementById('mensajeGeneral')
    };

    function renderizarFilas(envios) {
        el.body.replaceChildren();

        if (envios.length === 0) {
            const fila = el.body.insertRow();
            const celda = fila.insertCell();
            celda.colSpan = 5;
            celda.className = 'tabla-vacia';
            celda.textContent = 'No hay envíos que coincidan con la búsqueda.';
            return;
        }

        envios.forEach(envio => {
            const fila = el.body.insertRow();
            fila.insertCell().textContent = envio.codigoRastreo;
            fila.insertCell().textContent = envio.destinatario ?? '—';
            fila.insertCell().textContent = envio.direccionDestino;

            const flete = fila.insertCell();
            flete.className = 'num';
            flete.textContent = formatoMoneda.format(envio.costo);

            const pill = document.createElement('span');
            pill.className = `pill-status ${envio.estadoEnvio}`;
            pill.textContent = envio.estadoEnvio;
            fila.insertCell().appendChild(pill);
        });
    }

    function actualizarPaginador(data) {
        // Spring Data es base 0: a la API se le envía data.number, al usuario se le muestra data.number + 1
        const paginaMostrada = data.totalPages === 0 ? 0 : data.number + 1;
        estado.ultimaPagina = Math.max(data.totalPages - 1, 0);

        el.modo.hidden = true;
        el.info.textContent = `Página ${paginaMostrada} de ${data.totalPages} (Total: ${data.totalElements} envíos)`;
        el.primera.disabled = data.first;
        el.anterior.disabled = data.first;
        el.siguiente.disabled = data.last;
        el.ultima.disabled = data.last;
    }

    function mostrarModoProcedimiento(total) {
        el.modo.hidden = false;
        el.modo.textContent = `Resultado de SP_OBTENER_ENVIOS_POR_ESTADO('${estado.procedimiento}'): ${total} envíos`;
        el.info.textContent = `Stored Procedure — ${total} envíos`;
        [el.primera, el.anterior, el.siguiente, el.ultima].forEach(boton => { boton.disabled = true; });
    }

    async function cargar() {
        const id = ++solicitudActual;
        el.mensaje.textContent = '';

        try {
            if (estado.procedimiento) {
                const respuesta = await fetchAutenticado(`${API_V1}/procedimiento/${encodeURIComponent(estado.procedimiento)}`);
                if (!respuesta.ok) throw new Error('No se pudo ejecutar el procedimiento almacenado.');
                const envios = await respuesta.json();
                if (id !== solicitudActual) return;
                renderizarFilas(envios);
                mostrarModoProcedimiento(envios.length);
            } else {
                const params = new URLSearchParams({ page: estado.pagina, size: estado.tamanio });
                if (estado.busqueda) params.set('busqueda', estado.busqueda);

                const respuesta = await fetchAutenticado(`${API_V1}?${params}`);
                if (!respuesta.ok) throw new Error('No se pudieron obtener los envíos.');
                const data = await respuesta.json();
                if (id !== solicitudActual) return;
                renderizarFilas(data.content);
                actualizarPaginador(data);
            }
        } catch (error) {
            if (id === solicitudActual) el.mensaje.textContent = error.message;
        }
    }

    function irAPagina(numero) {
        estado.pagina = Math.max(0, numero);
        cargar();
    }

    function alternarControles() {
        const modoProcedimiento = Boolean(estado.procedimiento);
        el.busqueda.disabled = modoProcedimiento;
        el.tamanio.disabled = modoProcedimiento;
        el.btnBuscar.disabled = modoProcedimiento;
    }

    function iniciar() {
        if (!obtenerToken()) {
            redirigirALogin();
            return;
        }

        document.getElementById('nombreUsuario').textContent = obtenerClaims()?.sub || '';
        document.getElementById('btnLogout').addEventListener('click', redirigirALogin);

        el.form.addEventListener('submit', (evento) => {
            evento.preventDefault();
            estado.busqueda = el.busqueda.value.trim();
            estado.pagina = 0;
            cargar();
        });

        el.tamanio.addEventListener('change', () => {
            estado.tamanio = Number(el.tamanio.value);
            estado.pagina = 0;
            cargar();
        });

        el.consulta.addEventListener('change', () => {
            estado.procedimiento = el.consulta.value;
            estado.pagina = 0;
            alternarControles();
            cargar();
        });

        el.primera.addEventListener('click', () => irAPagina(0));
        el.anterior.addEventListener('click', () => irAPagina(estado.pagina - 1));
        el.siguiente.addEventListener('click', () => irAPagina(estado.pagina + 1));
        el.ultima.addEventListener('click', () => irAPagina(estado.ultimaPagina));

        cargar();
    }

    iniciar();
})();