// ============================================
// DASHBOARD - GESTIÓN DE ÓRDENES
// ============================================

document.addEventListener('DOMContentLoaded', function () {
	console.log('Dashboard de Órdenes cargado');

	// Inicializar funcionalidades
	initializeSearch();
	initializeFilters();
	initializeOrderActions();
	initializeModals();
});

// ============================================
// BÚSQUEDA DE ÓRDENES
// ============================================
function initializeSearch() {
	const searchInput = document.getElementById('searchOrders');

	if (searchInput) {
		searchInput.addEventListener('input', function (e) {
			const searchTerm = e.target.value.toLowerCase();
			filterOrders(searchTerm);
		});
	}
}

function filterOrders(searchTerm) {
	const tableRows = document.querySelectorAll('tbody tr');

	tableRows.forEach((row) => {
		const orderText = row.textContent.toLowerCase();
		if (orderText.includes(searchTerm)) {
			row.style.display = '';
		} else {
			row.style.display = 'none';
		}
	});
}

// ============================================
// FILTROS
// ============================================
function initializeFilters() {
	const filterStatus = document.getElementById('filterStatus');
	const filterPayment = document.getElementById('filterPayment');
	const filterDateFrom = document.getElementById('filterDateFrom');
	const filterDateTo = document.getElementById('filterDateTo');

	if (filterStatus) {
		filterStatus.addEventListener('change', applyFilters);
	}

	if (filterPayment) {
		filterPayment.addEventListener('change', applyFilters);
	}

	if (filterDateFrom) {
		filterDateFrom.addEventListener('change', applyFilters);
	}

	if (filterDateTo) {
		filterDateTo.addEventListener('change', applyFilters);
	}
}

function applyFilters() {
	const status = document.getElementById('filterStatus')?.value || '';
	const payment = document.getElementById('filterPayment')?.value || '';
	const dateFrom = document.getElementById('filterDateFrom')?.value || '';
	const dateTo = document.getElementById('filterDateTo')?.value || '';

	console.log('Aplicando filtros:', { status, payment, dateFrom, dateTo });

	// Aquí implementarías la lógica de filtrado real
	// Por ahora solo mostramos en consola
}

// ============================================
// ACCIONES DE ÓRDENES
// ============================================
function initializeOrderActions() {
	// Botones de ver detalles
	const viewButtons = document.querySelectorAll('[title="Ver detalles"]');
	viewButtons.forEach((btn) => {
		btn.addEventListener('click', function (e) {
			e.stopPropagation();
			const orderId = this.closest('tr').querySelector('strong').textContent;
			viewOrderDetails(orderId);
		});
	});

	// Botones de actualizar estado
	const updateButtons = document.querySelectorAll('[title="Actualizar estado"]');
	updateButtons.forEach((btn) => {
		btn.addEventListener('click', function (e) {
			e.stopPropagation();
			const orderId = this.closest('tr').querySelector('strong').textContent;
			updateOrderStatus(orderId);
		});
	});

	// Botones de imprimir
	const printButtons = document.querySelectorAll('[title="Imprimir"]');
	printButtons.forEach((btn) => {
		btn.addEventListener('click', function (e) {
			e.stopPropagation();
			const orderId = this.closest('tr').querySelector('strong').textContent;
			printOrder(orderId);
		});
	});

	// Click en fila para ver detalles
	const tableRows = document.querySelectorAll('tbody tr');
	tableRows.forEach((row) => {
		row.style.cursor = 'pointer';
		row.addEventListener('click', function () {
			const orderId = this.querySelector('strong').textContent;
			viewOrderDetails(orderId);
		});
	});
}

function viewOrderDetails(orderId) {
	console.log('Ver detalles de orden:', orderId);
	// Aquí cargarías los detalles reales de la orden
	const modal = new bootstrap.Modal(document.getElementById('orderDetailModal'));
	modal.show();
}

function updateOrderStatus(orderId) {
	console.log('Actualizar estado de orden:', orderId);
	// Aquí implementarías la lógica de actualización
}

function printOrder(orderId) {
	console.log('Imprimir orden:', orderId);
	window.print();
}

// ============================================
// MODALES
// ============================================
function initializeModals() {
	// Modal de detalles
	const orderDetailModal = document.getElementById('orderDetailModal');
	if (orderDetailModal) {
		orderDetailModal.addEventListener('show.bs.modal', function (event) {
			console.log('Mostrando modal de detalles');
			// Aquí cargarías los datos de la orden
		});
	}

	// Guardar cambios en modal
	const saveButton = orderDetailModal?.querySelector('.btn-primary[type="button"]');
	if (saveButton && saveButton.textContent.includes('Guardar')) {
		saveButton.addEventListener('click', function () {
			saveOrderChanges();
		});
	}
}

function saveOrderChanges() {
	console.log('Guardando cambios de la orden');

	// Obtener valores de los selects
	const orderStatus = document.querySelector('#orderDetailModal select').value;
	const paymentStatus = document.querySelectorAll('#orderDetailModal select')[1].value;
	const shippingStatus = document.querySelectorAll('#orderDetailModal select')[2].value;
	const notes = document.querySelector('#orderDetailModal textarea').value;

	console.log('Datos a guardar:', {
		orderStatus,
		paymentStatus,
		shippingStatus,
		notes,
	});

	// Aquí harías la petición AJAX al backend
	// fetch('/api/orders/update', { ... })

	// Mostrar mensaje de éxito
	alert('Cambios guardados correctamente');

	// Cerrar modal
	const modal = bootstrap.Modal.getInstance(document.getElementById('orderDetailModal'));
	modal.hide();
}

// ============================================
// EXPORTAR ÓRDENES
// ============================================
function exportOrders() {
	console.log('Exportando órdenes...');

	// Aquí implementarías la exportación a CSV/Excel
	const exportData = gatherOrdersData();
	downloadCSV(exportData, 'ordenes.csv');
}

function gatherOrdersData() {
	const rows = document.querySelectorAll('tbody tr');
	const data = [];

	rows.forEach((row) => {
		if (row.style.display !== 'none') {
			const cells = row.querySelectorAll('td');
			data.push({
				id: cells[0].textContent.trim(),
				cliente: cells[1].textContent.trim(),
				fecha: cells[2].textContent.trim(),
				total: cells[3].textContent.trim(),
				estado: cells[4].textContent.trim(),
				pago: cells[5].textContent.trim(),
				envio: cells[6].textContent.trim(),
			});
		}
	});

	return data;
}

function downloadCSV(data, filename) {
	if (data.length === 0) {
		alert('No hay datos para exportar');
		return;
	}

	// Crear CSV
	const headers = Object.keys(data[0]);
	let csv = headers.join(',') + '\n';

	data.forEach((row) => {
		csv += headers.map((header) => `"${row[header]}"`).join(',') + '\n';
	});

	// Descargar
	const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
	const link = document.createElement('a');
	const url = URL.createObjectURL(blob);

	link.setAttribute('href', url);
	link.setAttribute('download', filename);
	link.style.visibility = 'hidden';

	document.body.appendChild(link);
	link.click();
	document.body.removeChild(link);
}

// ============================================
// ACTUALIZACIÓN EN TIEMPO REAL (OPCIONAL)
// ============================================
function startRealtimeUpdates() {
	// Simular actualizaciones en tiempo real
	setInterval(function () {
		// Aquí harías fetch al backend para obtener nuevas órdenes
		console.log('Verificando nuevas órdenes...');
	}, 30000); // Cada 30 segundos
}

// Iniciar actualizaciones (descomenta si lo necesitas)
// startRealtimeUpdates();
