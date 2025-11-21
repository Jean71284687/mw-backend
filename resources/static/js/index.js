document.querySelectorAll('a[href^="#"]:not([data-bs-toggle]):not([href="#"])').forEach((anchor) => {
	anchor.addEventListener('click', function (e) {
		e.preventDefault();
		const href = this.getAttribute('href');
		if (href && href !== '#') {
			const target = document.querySelector(href);
			if (target) {
				target.scrollIntoView({
					behavior: 'smooth',
					block: 'start',
				});
			}
		}
	});
});

// Animación de aparición en scroll
const observerOptions = {
	threshold: 0.1,
	rootMargin: '0px 0px -50px 0px',
};

const observer = new IntersectionObserver((entries) => {
	entries.forEach((entry) => {
		if (entry.isIntersecting) {
			entry.target.style.opacity = '1';
			entry.target.style.transform = 'translateY(0)';
		}
	});
}, observerOptions);

// Aplicar animación a elementos con clase fade-in
document.querySelectorAll('.fade-in').forEach((el) => {
	el.style.opacity = '0';
	el.style.transform = 'translateY(30px)';
	el.style.transition = 'all 0.6s ease-out';
	observer.observe(el);
});

// Contador animado para las estadísticas del hero
function animateCounters() {
	const counters = document.querySelectorAll('.hero-content h3');
	counters.forEach((counter) => {
		const target = parseInt(counter.textContent.replace(/[^0-9]/g, ''));
		const increment = target / 100;
		let current = 0;

		const timer = setInterval(() => {
			current += increment;
			if (current >= target) {
				counter.textContent = counter.textContent.replace(/[0-9]+/, target);
				clearInterval(timer);
			} else {
				counter.textContent = counter.textContent.replace(/[0-9]+/, Math.ceil(current));
			}
		}, 20);
	});
}

// Ejecutar contador cuando la sección hero sea visible
const heroObserver = new IntersectionObserver((entries) => {
	entries.forEach((entry) => {
		if (entry.isIntersecting) {
			animateCounters();
			heroObserver.unobserve(entry.target);
		}
	});
});

const heroSection = document.querySelector('.hero-section');
if (heroSection) {
	heroObserver.observe(heroSection);
}

// Efecto de navbar al hacer scroll
window.addEventListener('scroll', function () {
	const navbar = document.querySelector('.navbar');
	if (window.scrollY > 50) {
		navbar.style.background = 'linear-gradient(135deg, rgba(13, 110, 253, 0.95), rgba(0, 86, 179, 0.95))';
		navbar.style.backdropFilter = 'blur(10px)';
	} else {
		navbar.style.background = 'linear-gradient(135deg, #0d6efd, #0056b3)';
		navbar.style.backdropFilter = 'none';
	}
});

// Inicializar tooltips cuando el DOM esté listo
document.addEventListener('DOMContentLoaded', function () {
	// Inicializar tooltips de Bootstrap
	const tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
	const tooltipList = tooltipTriggerList.map(function (tooltipTriggerEl) {
		return new bootstrap.Tooltip(tooltipTriggerEl);
	});
});

// Funcionalidad para botón "Agregar al Carrito"
document.addEventListener('DOMContentLoaded', function () {
	// Inicializar los botones de agregar al carrito
	initializeAddToCartButtons();
});

function initializeAddToCartButtons() {
	document.querySelectorAll('.btn-add-to-cart').forEach((button) => {
		button.addEventListener('click', function (e) {
			e.preventDefault();
			const productId = this.dataset.productId;
			if (productId) {
				addToCart(productId, this);
			}
		});
	});
}

function addToCart(productId, button) {
	const originalText = button.innerHTML;
	const productCard = button.closest('.product-card');
	const productName = productCard ? productCard.querySelector('.card-title').textContent : 'Producto';

	// Mostrar loading
	button.innerHTML = '<i class="bi bi-arrow-clockwise spinner-border spinner-border-sm me-2"></i>Agregando...';
	button.disabled = true;

	// Petición AJAX al endpoint
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
				console.log('Producto agregado exitosamente');
				console.log('Cart count recibido:', data.cartCount);

				button.innerHTML = '<i class="bi bi-check me-2"></i>¡Agregado!';
				button.classList.remove('btn-outline-success');
				button.classList.add('btn-success');

				// Actualizar contador del carrito en el navbar
				const cartBadge = document.querySelector('.cart-count');
				console.log('Cart badge encontrado:', cartBadge);

				if (cartBadge && data.cartCount !== undefined) {
					console.log('Actualizando badge a:', data.cartCount);
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
				showNotification('Debes iniciar sesión para agregar productos al carrito', 'warning');
				setTimeout(() => {
					const loginModal = document.getElementById('loginModal');
					if (loginModal) {
						const modal = new bootstrap.Modal(loginModal);
						modal.show();
					}
				}, 1000);
			} else {
				showNotification(error.message || 'Error al agregar al carrito', 'danger');
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

// Función para mostrar notificaciones
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

// Efecto hover mejorado para cards de productos
document.querySelectorAll('.product-card').forEach((card) => {
	card.addEventListener('mouseenter', function () {
		this.style.transform = 'translateY(-15px)';
	});

	card.addEventListener('mouseleave', function () {
		this.style.transform = 'translateY(0)';
	});
});

// Validación simple del formulario de newsletter
document.addEventListener('DOMContentLoaded', function () {
	const newsletterForm = document.querySelector('footer form');
	if (newsletterForm) {
		newsletterForm.addEventListener('submit', function (e) {
			e.preventDefault();
			const emailInput = this.querySelector('input[type="email"]');
			const submitBtn = this.querySelector('button');

			if (emailInput && emailInput.value && emailInput.checkValidity()) {
				// Simular envío exitoso
				const originalBtnContent = submitBtn.innerHTML;
				submitBtn.innerHTML = '<i class="bi bi-check"></i>';
				submitBtn.classList.remove('btn-warning');
				submitBtn.classList.add('btn-success');
				submitBtn.disabled = true;
				emailInput.value = '';

				// Mostrar mensaje temporal
				const message = document.createElement('small');
				message.className = 'text-success d-block mt-2';
				message.textContent = '¡Suscripción exitosa!';
				this.appendChild(message);

				setTimeout(() => {
					submitBtn.innerHTML = originalBtnContent;
					submitBtn.classList.remove('btn-success');
					submitBtn.classList.add('btn-warning');
					submitBtn.disabled = false;
					if (message.parentNode) {
						message.remove();
					}
				}, 3000);
			} else if (emailInput) {
				emailInput.classList.add('is-invalid');
				setTimeout(() => {
					emailInput.classList.remove('is-invalid');
				}, 3000);
			}
		});
	}
});

// Mejorar la experiencia de carga
window.addEventListener('load', function () {
	document.body.classList.add('loaded');

	// Reinicializar tooltips si es necesario
	const tooltips = document.querySelectorAll('[title]:not([data-bs-original-title])');
	tooltips.forEach((tooltip) => {
		try {
			new bootstrap.Tooltip(tooltip);
		} catch (e) {
			console.log('Tooltip ya inicializado o error:', e);
		}
	});
});

// Efecto parallax sutil para el hero (con throttling para mejor rendimiento)
let parallaxTicking = false;

function updateParallax() {
	const heroSection = document.querySelector('.hero-section');
	if (heroSection) {
		const scrolled = window.pageYOffset;
		const parallax = scrolled * 0.3;

		if (scrolled < heroSection.offsetHeight) {
			heroSection.style.transform = `translateY(${parallax}px)`;
		}
	}
	parallaxTicking = false;
}

window.addEventListener('scroll', function () {
	if (!parallaxTicking) {
		requestAnimationFrame(updateParallax);
		parallaxTicking = true;
	}
});

// Mejorar accesibilidad con navegación por teclado
document.addEventListener('keydown', function (e) {
	if (e.key === 'Tab') {
		document.body.classList.add('keyboard-navigation');
	}
});

document.addEventListener('mousedown', function () {
	document.body.classList.remove('keyboard-navigation');
});

function renderProductos() {
	const contenedor = document.getElementById('listaProductos');
	contenedor.innerHTML = '';
	productos.forEach((p) => {
		const div = document.createElement('div');
		div.className = 'producto';
		div.innerHTML = `
          <img src="${p.img}" alt="${p.nombre}" />
          <h3>${p.nombre}</h3>
          <p>S/ ${p.precio}</p>
          <button onclick="agregarAlCarrito(${p.id})">Agregar al carrito</button>
        `;
		contenedor.appendChild(div);
	});
}

function agregarAlCarrito(id) {
	const prod = productos.find((p) => p.id === id);
	carrito.push(prod);
	actualizarIconoCarrito();
}

function abrirCarrito() {
	document.getElementById('carritoModal').style.display = 'block';
	const lista = document.getElementById('listaCarrito');
	lista.innerHTML = '';
	let total = 0;
	carrito.forEach((p) => {
		const li = document.createElement('li');
		li.textContent = `${p.nombre} - S/ ${p.precio}`;
		total += p.precio;
		lista.appendChild(li);
	});
	document.getElementById('totalCarrito').innerText = total.toFixed(2);
}

function actualizarIconoCarrito() {
	const icono = document.getElementById('iconoCarrito');
	icono.setAttribute('data-count', carrito.length);
}

function cerrarCarrito() {
	document.getElementById('carritoModal').style.display = 'none';
}

function abrirRegistro() {
	document.getElementById('registroModal').style.display = 'block';
}

function cerrarRegistro() {
	document.getElementById('registroModal').style.display = 'none';
}

// Proxy para verificar autenticación antes de pagar
const servicioPago = {
	procesarPago: () => alert('✅ Pago realizado con éxito.'),
};

const proxyPago = {
	pagar: () => {
		if (!usuario.autenticado) {
			alert('🔒 Debes iniciar sesión para pagar.');
			return;
		}
		servicioPago.procesarPago();
	},
};

function pagar() {
	proxyPago.pagar();
}

renderProductos();
