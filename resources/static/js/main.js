// JavaScript global de la aplicación
document.addEventListener('DOMContentLoaded', function () {
	// Inicializar tooltips de Bootstrap
	var tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
	var tooltipList = tooltipTriggerList.map(function (tooltipTriggerEl) {
		return new bootstrap.Tooltip(tooltipTriggerEl);
	});

	// Mostrar mensajes de éxito/error
	showNotifications();

	// Configurar CSRF token para peticiones AJAX
	setupCSRFToken();
});

// Función para mostrar notificaciones
function showNotifications() {
	const alerts = document.querySelectorAll('.alert');
	alerts.forEach((alert) => {
		// Auto-hide alerts después de 5 segundos
		setTimeout(() => {
			alert.style.opacity = '0';
			setTimeout(() => {
				alert.remove();
			}, 300);
		}, 5000);
	});
}

// Configurar CSRF token para AJAX
function setupCSRFToken() {
	const token = document.querySelector('meta[name="_csrf"]');
	const header = document.querySelector('meta[name="_csrf_header"]');

	if (token && header) {
		// Configurar CSRF para todas las peticiones AJAX
		$.ajaxSetup({
			beforeSend: function (xhr) {
				xhr.setRequestHeader(header.getAttribute('content'), token.getAttribute('content'));
			},
		});
	}
}

// Función para mostrar loading spinner
function showLoading(element) {
	element.classList.add('loading');
}

function hideLoading(element) {
	element.classList.remove('loading');
}

// Función para formatear precios
function formatPrice(price) {
	return new Intl.NumberFormat('es-PE', {
		style: 'currency',
		currency: 'PEN',
	}).format(price);
}

// Función para mostrar confirmación
function showConfirmation(message, callback) {
	if (confirm(message)) {
		callback();
	}
}
