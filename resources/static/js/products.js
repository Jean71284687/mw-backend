// JavaScript específico para la página de productos
document.addEventListener('DOMContentLoaded', function () {
	initializeProductFilters();
	initializeProductSearch();
	initializeProductCards();
});

// Inicializar filtros de productos
function initializeProductFilters() {
	const filterForm = document.getElementById('filterForm');
	const categoryInputs = document.querySelectorAll('input[name="categoryId"]');

	if (!filterForm) return;

	// Auto-submit cuando cambia la categoría
	categoryInputs.forEach((input) => {
		input.addEventListener('change', function () {
			showLoading(filterForm);
			filterForm.submit();
		});
	});

	// Limpiar filtros
	const clearButton = document.querySelector('.btn-clear-filters');
	if (clearButton) {
		clearButton.addEventListener('click', function (e) {
			e.preventDefault();
			window.location.href = '/products';
		});
	}
}

// Inicializar búsqueda de productos
function initializeProductSearch() {
	const searchInput = document.getElementById('search');
	const filterForm = document.getElementById('filterForm');
	let searchTimeout;

	if (!searchInput || !filterForm) return;

	// Búsqueda en tiempo real
	searchInput.addEventListener('input', function () {
		clearTimeout(searchTimeout);
		const searchTerm = this.value.trim();

		searchTimeout = setTimeout(() => {
			if (searchTerm.length >= 3 || searchTerm.length === 0) {
				showLoading(filterForm);
				filterForm.submit();
			}
		}, 500);
	});

	// Búsqueda con Enter
	searchInput.addEventListener('keypress', function (e) {
		if (e.key === 'Enter') {
			e.preventDefault();
			clearTimeout(searchTimeout);
			showLoading(filterForm);
			filterForm.submit();
		}
	});
}

// Inicializar cards de productos
function initializeProductCards() {
	const productCards = document.querySelectorAll('.product-card');

	productCards.forEach((card) => {
		// Agregar efecto hover
		card.addEventListener('mouseenter', function () {
			this.style.transform = 'translateY(-5px)';
		});

		card.addEventListener('mouseleave', function () {
			this.style.transform = 'translateY(0)';
		});

		// Manejar clic en "Agregar al carrito"
		const addToCartBtn = card.querySelector('.btn-add-to-cart');
		if (addToCartBtn) {
			addToCartBtn.addEventListener('click', function (e) {
				e.preventDefault();
				const productId = this.dataset.productId;
				addToCart(productId);
			});
		}
	});
}

// Función para agregar al carrito
function addToCart(productId) {
	if (!productId) return;

	const button = document.querySelector(`[data-product-id="${productId}"]`);
	const originalText = button.innerHTML;
	const productCard = button.closest('.product-card');
	const productName = productCard.querySelector('.card-title').textContent;

	// Mostrar loading
	button.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Agregando...';
	button.disabled = true;

	// Petición AJAX al endpoint correcto
	fetch(`/web/cart/add-ajax?productId=${productId}&quantity=1`, {
		method: 'POST',
		headers: {
			'Content-Type': 'application/x-www-form-urlencoded',
		},
	})
		.then((response) => {
			if (!response.ok && response.status === 401) {
				throw new Error('NO_AUTH');
			}
			return response.json();
		})
		.then((data) => {
			if (data.success) {
				button.innerHTML = '<i class="fas fa-check"></i> Agregado';
				button.classList.remove('btn-outline-success');
				button.classList.add('btn-success');

				// Actualizar contador del carrito en el navbar
				const cartBadge = document.querySelector('.cart-count');

				if (cartBadge && data.cartCount !== undefined) {
					cartBadge.textContent = data.cartCount;
				} else {
					console.warn('No se pudo actualizar el badge. Badge:', cartBadge, 'Count:', data.cartCount);
				}

				// Mostrar modal de confirmación
				showAddToCartModal(productName);

				// Restaurar botón después de 3 segundos
				setTimeout(() => {
					button.innerHTML = originalText;
					button.classList.remove('btn-success');
					button.classList.add('btn-outline-success');
					button.disabled = false;
				}, 3000);
			} else {
				throw new Error(data.message || 'Error al agregar al carrito');
			}
		})
		.catch((error) => {
			console.error('Error:', error);
			button.innerHTML = originalText;
			button.disabled = false;

			if (error.message === 'NO_AUTH') {
				// Usuario no autenticado - mostrar modal de login
				showErrorNotification('Debes iniciar sesión para agregar productos al carrito');
				setTimeout(() => {
					const loginModal = new bootstrap.Modal(document.getElementById('loginModal'));
					loginModal.show();
				}, 1000);
			} else {
				showErrorNotification(error.message || 'Error al agregar al carrito');
			}
		});
}

// Mostrar modal de confirmación
function showAddToCartModal(productName) {
	const modal = document.getElementById('addToCartModal');
	const productNameElement = document.getElementById('productAddedName');

	if (modal && productNameElement) {
		productNameElement.textContent = productName;
		const modalInstance = new bootstrap.Modal(modal);
		modalInstance.show();

		// Auto-cerrar después de 5 segundos
		setTimeout(() => {
			modalInstance.hide();
		}, 5000);
	}
}

// Funciones de notificación
function showSuccessNotification(message) {
	showNotification(message, 'success');
}

function showErrorNotification(message) {
	showNotification(message, 'danger');
}

function showNotification(message, type = 'info') {
	const notification = document.createElement('div');
	notification.className = `alert alert-${type} alert-dismissible fade show position-fixed`;
	notification.style.cssText = 'top: 20px; right: 20px; z-index: 1050; min-width: 300px;';
	notification.innerHTML = `
        ${message}
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    `;

	document.body.appendChild(notification);

	// Auto-hide después de 5 segundos
	setTimeout(() => {
		notification.remove();
	}, 5000);
}

// Función para filtrar productos con AJAX (opcional)
function filterProductsAjax(categoryId, searchTerm, page = 0) {
	const container = document.querySelector('.product-grid');
	showLoading(container);

	const params = new URLSearchParams({
		page: page,
		size: 12,
	});

	if (categoryId) params.append('categoryId', categoryId);
	if (searchTerm) params.append('search', searchTerm);

	fetch(`/products/search?${params}`)
		.then((response) => response.json())
		.then((data) => {
			// Aquí actualizarías el DOM con los nuevos productos
			updateProductGrid(data);
			hideLoading(container);
		})
		.catch((error) => {
			console.error('Error:', error);
			hideLoading(container);
		});
}

function updateProductGrid(data) {
	// Implementar actualización del grid de productos
	// Esta función actualizaría el HTML con los nuevos productos
}
