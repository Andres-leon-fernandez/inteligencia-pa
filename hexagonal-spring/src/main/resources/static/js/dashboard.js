(() => {
  'use strict';

  const API = {
    emergencies: '/api/v1/emergencias',
    summary: '/api/v1/indicadores/resumen'
  };
  const PAGE_SIZE = 10;
  const STATE_COLORS = {
    INTERRUMPIDO: '#dc5e5b',
    RESTRINGIDO: '#dfaa42',
    NORMAL: '#318c79',
    DESCONOCIDO: '#9ba8ae'
  };
  const state = {
    page: 0,
    pageSize: PAGE_SIZE,
    totalPages: 0,
    totalElements: 0,
    cutoff: null,
    allSummary: null,
    debounce: null
  };

  const $ = (selector) => document.querySelector(selector);
  const numberFormat = new Intl.NumberFormat('es-PE');

  async function getJson(url) {
    const response = await fetch(url, { headers: { Accept: 'application/json' } });
    if (!response.ok) {
      let detail = `Error ${response.status}`;
      try {
        const payload = await response.json();
        if (payload.mensaje) detail = payload.mensaje;
      } catch (_) {
        // Conserva el mensaje HTTP si la respuesta no contiene JSON.
      }
      throw new Error(detail);
    }
    return response.json();
  }

  function formatNumber(value) {
    return numberFormat.format(Number(value || 0));
  }

  function formatDate(value, options = { day: '2-digit', month: 'short', year: 'numeric' }) {
    if (!value) return '—';
    const dateText = String(value).slice(0, 10);
    const parts = dateText.split('-').map(Number);
    if (parts.length !== 3 || parts.some(Number.isNaN)) return String(value);
    const date = new Date(parts[0], parts[1] - 1, parts[2], 12);
    return new Intl.DateTimeFormat('es-PE', options).format(date).replace('.', '');
  }

  function escapeHtml(value) {
    return String(value ?? '').replace(/[&<>"']/g, (character) => ({
      '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;'
    })[character]);
  }

  function setConnection(connected) {
    const indicator = $('#connection-indicator');
    indicator.classList.toggle('offline', !connected);
    $('#connection-label').textContent = connected ? 'API conectada' : 'Sin conexión';
  }

  function showNotice(message) {
    $('#notice-message').textContent = message;
    $('#notice-bar').hidden = false;
  }

  function hideNotice() {
    $('#notice-bar').hidden = true;
  }

  function makeUrl(base, params) {
    const query = new URLSearchParams();
    Object.entries(params).forEach(([key, value]) => {
      if (value !== null && value !== undefined && String(value).trim() !== '') {
        query.set(key, value);
      }
    });
    const text = query.toString();
    return text ? `${base}?${text}` : base;
  }

  function periodParams() {
    return { desde: $('#date-from').value, hasta: $('#date-to').value };
  }

  function setStatusBadge(value) {
    const status = String(value || 'DESCONOCIDO').toUpperCase();
    const className = ['INTERRUMPIDO', 'RESTRINGIDO', 'NORMAL'].includes(status)
      ? status.toLowerCase() : 'unknown';
    const label = status === 'DESCONOCIDO' ? 'Sin dato' : status.toLowerCase();
    return `<span class="status-badge ${className}">${escapeHtml(label)}</span>`;
  }

  function statusCount(summary, expected) {
    const item = (summary.porEstado || []).find((row) =>
      String(row.categoria || '').toUpperCase() === expected);
    return item ? Number(item.cantidad || 0) : 0;
  }

  function renderKpis(summary) {
    const total = Number(summary.totalReportes || 0);
    const interrupted = statusCount(summary, 'INTERRUMPIDO');
    const restricted = statusCount(summary, 'RESTRINGIDO');
    const departments = (summary.porDepartamento || []).filter((item) => item.categoria).length;
    $('#kpi-total').textContent = formatNumber(total);
    $('#kpi-interrupted').textContent = formatNumber(interrupted);
    $('#kpi-restricted').textContent = formatNumber(restricted);
    $('#kpi-departments').textContent = formatNumber(departments);
    $('#kpi-interrupted-share').textContent = total
      ? `${((interrupted / total) * 100).toFixed(1)}% de los reportes`
      : 'Sin reportes en el periodo';
    $('#kpi-restricted-share').textContent = total
      ? `${((restricted / total) * 100).toFixed(1)}% de los reportes`
      : 'Sin reportes en el periodo';
    $('#donut-total').textContent = formatNumber(total);
  }

  function renderStatusChart(summary) {
    const items = (summary.porEstado || []).filter((item) => Number(item.cantidad) > 0);
    const total = Number(summary.totalReportes || 0);
    const donut = $('#status-donut');
    const legend = $('#status-legend');

    if (!items.length || !total) {
      donut.style.background = 'conic-gradient(#e7ecee 0deg 360deg)';
      legend.innerHTML = '<div class="legend-empty">No hay registros para este periodo.</div>';
      return;
    }

    let current = 0;
    const segments = items.map((item) => {
      const key = String(item.categoria || 'DESCONOCIDO').toUpperCase();
      const share = Math.max(0, Number(item.cantidad)) / total * 360;
      const start = current;
      current += share;
      return `${STATE_COLORS[key] || STATE_COLORS.DESCONOCIDO} ${start}deg ${current}deg`;
    });
    donut.style.background = `conic-gradient(${segments.join(', ')})`;
    legend.innerHTML = items.map((item) => {
      const key = String(item.categoria || 'DESCONOCIDO').toUpperCase();
      const color = STATE_COLORS[key] || STATE_COLORS.DESCONOCIDO;
      const percentage = (Number(item.cantidad) / total * 100).toFixed(1);
      const label = key === 'DESCONOCIDO' ? 'Sin dato' : key.toLowerCase();
      return `<div class="legend-item"><span class="legend-swatch" style="background:${color}"></span><span class="legend-name">${escapeHtml(label)}</span><strong class="legend-value">${formatNumber(item.cantidad)}</strong><span class="legend-percent">${percentage}%</span></div>`;
    }).join('');
  }

  function renderBars(container, items, limit, emptyText) {
    const topItems = (items || []).filter((item) => item.categoria).slice(0, limit);
    const max = Math.max(1, ...topItems.map((item) => Number(item.cantidad || 0)));
    if (!topItems.length) {
      container.innerHTML = `<div class="chart-empty">${escapeHtml(emptyText)}</div>`;
      return;
    }
    container.innerHTML = topItems.map((item) => {
      const width = Math.max(2, Number(item.cantidad) / max * 100);
      return `<div class="bar-row"><span class="bar-name" title="${escapeHtml(item.categoria)}">${escapeHtml(item.categoria)}</span><div class="bar-track"><div class="bar-fill" style="width:${width}%"></div></div><strong class="bar-value">${formatNumber(item.cantidad)}</strong></div>`;
    }).join('');
  }

  function renderEvents(items) {
    const container = $('#event-chart');
    const topItems = (items || []).filter((item) => item.categoria).slice(0, 5);
    const max = Math.max(1, ...topItems.map((item) => Number(item.cantidad || 0)));
    if (!topItems.length) {
      container.innerHTML = '<div class="chart-empty">No hay eventos para mostrar.</div>';
      return;
    }
    container.innerHTML = topItems.map((item, index) => {
      const width = Math.max(3, Number(item.cantidad) / max * 100);
      return `<div class="event-row"><span class="event-rank-number">0${index + 1}</span><span class="event-name" title="${escapeHtml(item.categoria)}">${escapeHtml(item.categoria)}</span><div class="event-track"><div class="event-fill" style="width:${width}%"></div></div><strong class="event-count">${formatNumber(item.cantidad)}</strong></div>`;
    }).join('');
  }

  function renderInsight(summary) {
    const total = Number(summary.totalReportes || 0);
    const interrupted = statusCount(summary, 'INTERRUMPIDO');
    const restricted = statusCount(summary, 'RESTRINGIDO');
    if (!total) {
      $('#insight-title').textContent = 'Sin reportes en este periodo';
      $('#insight-copy').textContent = 'Prueba otro rango de fechas o limpia el filtro para volver a consultar todos los reportes disponibles.';
      return;
    }
    const share = ((interrupted / total) * 100).toFixed(1);
    $('#insight-title').textContent = interrupted
      ? `${formatNumber(interrupted)} reportes figuran interrumpidos`
      : 'No hay reportes interrumpidos';
    $('#insight-copy').textContent = `${share}% de los ${formatNumber(total)} reportes del periodo aparecen como interrumpidos al corte de la fuente. ${formatNumber(restricted)} figuran como restringidos.`;
  }

  function renderSummary(summary) {
    renderKpis(summary);
    renderStatusChart(summary);
    renderBars($('#department-chart'), summary.porDepartamento, 6, 'No hay departamentos para mostrar.');
    renderEvents(summary.porEvento);
    renderInsight(summary);
    const range = periodParams();
    const periodText = range.desde || range.hasta
      ? `${range.desde ? formatDate(range.desde) : 'Inicio'} — ${range.hasta ? formatDate(range.hasta) : 'Hoy'}`
      : 'Todo el periodo disponible';
    $('#table-count').textContent = `${periodText} · indicadores basados en reportes registrados`;
  }

  function populateOptions(select, items, defaultLabel) {
    const current = select.value;
    const unique = [...new Set((items || []).map((item) => item.categoria).filter(Boolean))];
    select.innerHTML = `<option value="">${defaultLabel}</option>` + unique.map((value) =>
      `<option value="${escapeHtml(value)}">${escapeHtml(value)}</option>`).join('');
    if (unique.includes(current)) select.value = current;
  }

  function populateFilterOptions(summary) {
    populateOptions($('#department-filter'), summary.porDepartamento, 'Todos los departamentos');
    populateOptions($('#event-filter'), summary.porEvento, 'Todos los eventos');
  }

  function locationText(ubicacion) {
    if (!ubicacion) return { primary: 'Ubicación no informada', secondary: '' };
    const primary = [ubicacion.distrito, ubicacion.provincia].filter(Boolean).join(', ') || 'Ubicación no informada';
    return { primary, secondary: ubicacion.departamento || '' };
  }

  function renderRows(pageData) {
    const body = $('#reports-body');
    const rows = pageData.contenido || [];
    state.totalElements = Number(pageData.totalElementos || 0);
    state.totalPages = Number(pageData.totalPaginas || 0);

    if (!rows.length) {
      body.innerHTML = '<tr><td class="table-state" colspan="6"><span class="empty-icon">⌕</span>No se encontraron reportes con estos filtros.</td></tr>';
    } else {
      body.innerHTML = rows.map((item) => {
        const location = locationText(item.ubicacion);
        const route = [item.tramoAfectado, item.sectorAfectado].filter(Boolean).join(' · ');
        return `<tr>
          <td class="date-cell">${escapeHtml(formatDate(item.fechaOcurrencia))}</td>
          <td class="event-cell" title="${escapeHtml(item.evento || item.fenomeno || 'Sin clasificar')}">${escapeHtml(item.evento || item.fenomeno || 'Sin clasificar')}</td>
          <td class="location-cell"><strong title="${escapeHtml(location.primary)}">${escapeHtml(location.primary)}</strong><small>${escapeHtml(location.secondary)}</small></td>
          <td class="road-cell" title="${escapeHtml(route)}"><span class="road-code">${escapeHtml(item.redAfectada || '—')}</span>${escapeHtml(route || 'Tramo no informado')}</td>
          <td>${setStatusBadge(item.estadoActual)}</td>
          <td><button class="row-action" type="button" data-detail-id="${escapeHtml(item.idEmergencia)}" aria-label="Ver detalle de emergencia ${escapeHtml(item.idEmergencia)}">›</button></td>
        </tr>`;
      }).join('');
    }

    const start = state.totalElements ? state.page * state.pageSize + 1 : 0;
    const end = Math.min((state.page + 1) * state.pageSize, state.totalElements);
    $('#page-summary').textContent = state.totalElements
      ? `Mostrando ${formatNumber(start)}–${formatNumber(end)} de ${formatNumber(state.totalElements)} reportes`
      : '0 reportes';
    $('#page-number').textContent = state.totalPages ? `${state.page + 1} / ${state.totalPages}` : '0 / 0';
    $('#previous-page').disabled = state.page <= 0 || state.totalPages === 0;
    $('#next-page').disabled = state.page + 1 >= state.totalPages;

    if (rows.length && rows[0].fechaCorte) {
      state.cutoff = rows[0].fechaCorte;
      $('#source-cutoff').textContent = formatDate(state.cutoff);
    }
  }

  function rowParams() {
    const range = periodParams();
    return {
      ...range,
      departamento: $('#department-filter').value,
      estado: $('#status-filter').value,
      evento: $('#event-filter').value,
      busqueda: $('#search-text').value.trim(),
      pagina: state.page,
      tamano: state.pageSize
    };
  }

  async function loadRows() {
    const body = $('#reports-body');
    body.innerHTML = '<tr><td class="table-state" colspan="6"><span class="loading-spinner"></span> Cargando emergencias…</td></tr>';
    const result = await getJson(makeUrl(API.emergencies, rowParams()));
    renderRows(result);
  }

  async function loadSummary() {
    const result = await getJson(makeUrl(API.summary, periodParams()));
    renderSummary(result);
  }

  async function refreshDashboard({ reloadOptions = false } = {}) {
    hideNotice();
    const summaryUrl = makeUrl(API.summary, reloadOptions ? {} : periodParams());
    try {
      const summary = await getJson(summaryUrl);
      if (reloadOptions) {
        state.allSummary = summary;
        populateFilterOptions(summary);
        await loadSummary();
      } else {
        renderSummary(summary);
      }
      await loadRows();
      setConnection(true);
    } catch (error) {
      setConnection(false);
      showNotice(`No se pudieron cargar los datos de la API. ${error.message}. Verifica que Spring Boot esté ejecutándose.`);
      $('#reports-body').innerHTML = '<tr><td class="table-state" colspan="6">No se pudo conectar con la API.</td></tr>';
      throw error;
    }
  }

  function addDetailField(container, label, value, full = false) {
    const field = document.createElement('div');
    field.className = `detail-field${full ? ' full' : ''}`;
    const heading = document.createElement('span');
    heading.textContent = label;
    const text = document.createElement('strong');
    text.textContent = value === null || value === undefined || value === '' ? 'No informado' : String(value);
    field.append(heading, text);
    container.append(field);
  }

  async function openDetail(id) {
    const dialog = $('#detail-dialog');
    const container = $('#detail-content');
    container.innerHTML = '<div class="table-state"><span class="loading-spinner"></span> Cargando detalle…</div>';
    if (!dialog.open) dialog.showModal();
    try {
      const item = await getJson(`${API.emergencies}/${encodeURIComponent(id)}`);
      const location = item.ubicacion || {};
      const coordinates = location.latitud != null && location.longitud != null
        ? `${location.latitud}, ${location.longitud}` : 'No informadas';
      container.replaceChildren();

      const statusLine = document.createElement('div');
      statusLine.className = 'detail-status-line';
      const code = document.createElement('span');
      code.className = 'detail-code';
      code.textContent = `${item.codigoReporte || 'Reporte'} · ID ${item.idEmergencia}`;
      statusLine.append(code);
      const badgeSlot = document.createElement('span');
      badgeSlot.innerHTML = setStatusBadge(item.estadoActual);
      statusLine.append(badgeSlot.firstElementChild);
      container.append(statusLine);

      const grid = document.createElement('div');
      grid.className = 'detail-grid';
      addDetailField(grid, 'Evento', item.evento);
      addDetailField(grid, 'Fenómeno', item.fenomeno);
      addDetailField(grid, 'Fecha de ocurrencia', formatDate(item.fechaOcurrencia));
      addDetailField(grid, 'Fecha de cierre', formatDate(item.fechaCierre));
      addDetailField(grid, 'Departamento', location.departamento);
      addDetailField(grid, 'Provincia / distrito', [location.provincia, location.distrito].filter(Boolean).join(' / '));
      addDetailField(grid, 'Carretera', item.redAfectada);
      addDetailField(grid, 'Tramo', item.tramoAfectado);
      addDetailField(grid, 'Sector afectado', item.sectorAfectado);
      addDetailField(grid, 'Coordenadas', coordinates);
      addDetailField(grid, 'Afectación reportada', item.afectacion, true);
      addDetailField(grid, 'Hechos', item.hechos, true);
      addDetailField(grid, 'Novedades', item.novedades, true);
      addDetailField(grid, 'Fuente del reporte', item.fuente, true);
      addDetailField(grid, 'Fecha de corte de los datos', formatDate(item.fechaCorte), true);
      container.append(grid);
    } catch (error) {
      container.textContent = `No se pudo cargar el detalle: ${error.message}`;
    }
  }

  function bindEvents() {
    $('#apply-period').addEventListener('click', () => {
      const { desde, hasta } = periodParams();
      if (desde && hasta && desde > hasta) {
        showNotice('La fecha inicial debe ser anterior o igual a la fecha final.');
        return;
      }
      state.page = 0;
      refreshDashboard().catch(() => {});
    });
    $('#clear-period').addEventListener('click', () => {
      $('#date-from').value = '';
      $('#date-to').value = '';
      state.page = 0;
      refreshDashboard().catch(() => {});
    });
    $('#refresh-data').addEventListener('click', () => refreshDashboard().catch(() => {}));
    $('#notice-close').addEventListener('click', hideNotice);
    ['department-filter', 'status-filter', 'event-filter'].forEach((id) => {
      $(`#${id}`).addEventListener('change', () => {
        state.page = 0;
        loadRows().catch((error) => showNotice(`No se pudo filtrar la tabla. ${error.message}`));
      });
    });
    $('#search-text').addEventListener('input', () => {
      window.clearTimeout(state.debounce);
      state.debounce = window.setTimeout(() => {
        state.page = 0;
        loadRows().catch((error) => showNotice(`No se pudo buscar. ${error.message}`));
      }, 320);
    });
    $('#reports-body').addEventListener('click', (event) => {
      const button = event.target.closest('[data-detail-id]');
      if (button) openDetail(button.dataset.detailId);
    });
    $('#previous-page').addEventListener('click', () => {
      if (state.page > 0) {
        state.page -= 1;
        loadRows().catch((error) => showNotice(`No se pudo cargar la página. ${error.message}`));
      }
    });
    $('#next-page').addEventListener('click', () => {
      if (state.page + 1 < state.totalPages) {
        state.page += 1;
        loadRows().catch((error) => showNotice(`No se pudo cargar la página. ${error.message}`));
      }
    });
    $('#detail-close').addEventListener('click', () => $('#detail-dialog').close());
    $('#detail-dialog').addEventListener('click', (event) => {
      if (event.target === $('#detail-dialog')) $('#detail-dialog').close();
    });
    $('#menu-toggle').addEventListener('click', () => {
      const sidebar = $('#sidebar');
      const open = sidebar.classList.toggle('open');
      $('#menu-toggle').setAttribute('aria-expanded', String(open));
      $('#menu-toggle').setAttribute('aria-label', open ? 'Cerrar menú' : 'Abrir menú');
    });
    document.addEventListener('keydown', (event) => {
      if (event.key === '/' && !['INPUT', 'TEXTAREA', 'SELECT'].includes(document.activeElement.tagName)) {
        event.preventDefault();
        $('#search-text').focus();
      }
      if (event.key === 'Escape') $('#sidebar').classList.remove('open');
    });
  }

  async function initialize() {
    bindEvents();
    $('#reports-body').innerHTML = '<tr><td class="table-state" colspan="6"><span class="loading-spinner"></span> Conectando con la API…</td></tr>';
    try {
      await refreshDashboard({ reloadOptions: true });
    } catch (_) {
      // La barra de aviso ya muestra cómo recuperar la conexión.
    }
  }

  document.addEventListener('DOMContentLoaded', initialize);
})();
