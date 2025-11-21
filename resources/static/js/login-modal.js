// JavaScript para manejar el modal de login

document.addEventListener('DOMContentLoaded', function () {
	initializeLoginModal();
});

function initializeLoginModal() {
	const loginForm = document.getElementById('loginForm');
	const togglePasswordBtn = document.getElementById('toggleLoginPassword');

	if (loginForm) {
		loginForm.addEventListener('submit', handleLoginSubmit);
	}

	if (togglePasswordBtn) {
		togglePasswordBtn.addEventListener('click', toggleLoginPasswordVisibility);
	}
}

async function handleLoginSubmit(e) {
	e.preventDefault();

	console.log('Formulario de login enviado');

	const form = e.target;
	const submitButton = form.querySelector('button[type="submit"]');
	const originalText = submitButton.innerHTML;

	// Limpiar errores previos
	clearLoginErrors();

	// Mostrar estado de carga
	submitButton.innerHTML = '<i class="bi bi-hourglass-split me-2"></i>Iniciando sesión...';
	submitButton.disabled = true;

	try {
		// Recopilar datos del formulario
		const formData = new FormData(form);

		// Enviar con fetch
		const response = await fetch('/web/auth/login', {
			method: 'POST',
			body: formData,
		});

		const result = await response.json();

		if (response.ok && result.success === true) {
			// Login exitoso
			showLoginSuccess(result.message || '¡Login exitoso!');

			// Limpiar formulario
			form.reset();

			// Sincronizar carrito del localStorage con la BD
			if (typeof CartLocal !== 'undefined') {
				await CartLocal.syncWithServer();
			}

			// Cerrar modal después de un breve delay
			setTimeout(() => {
				const loginModal = bootstrap.Modal.getInstance(document.getElementById('loginModal'));
				if (loginModal) {
					loginModal.hide();
				}

				// Recargar la página para actualizar el navbar
				setTimeout(() => {
					window.location.reload();
				}, 500);
			}, 1500);
		} else {
			// Manejar errores de validación o de autenticación
			if (result.errors) {
				// Errores de validación por campo
				displayFieldErrors(result.errors);
			} else if (result.error) {
				// Error general (por ejemplo, credenciales incorrectas)
				showLoginError(result.error);
			} else {
				throw new Error('Error en el login. Por favor intenta nuevamente.');
			}
		}
	} catch (error) {
		console.error('Error en login:', error);
		showLoginError(error.message || 'Error al iniciar sesión. Por favor intenta nuevamente.');
	} finally {
		// Restaurar botón
		submitButton.innerHTML = originalText;
		submitButton.disabled = false;
	}
}

function clearLoginErrors() {
	// Limpiar error general
	const errorDiv = document.getElementById('loginError');
	if (errorDiv) {
		errorDiv.classList.add('d-none');
	}

	// Limpiar errores de campos
	const emailInput = document.getElementById('loginEmail');
	const passwordInput = document.getElementById('loginPassword');
	const emailError = document.getElementById('emailError');
	const passwordError = document.getElementById('passwordError');

	if (emailInput) {
		emailInput.classList.remove('is-invalid');
	}
	if (passwordInput) {
		passwordInput.classList.remove('is-invalid');
	}
	if (emailError) {
		emailError.textContent = '';
		emailError.style.display = 'none';
	}
	if (passwordError) {
		passwordError.textContent = '';
		passwordError.style.display = 'none';
	}
}

function displayFieldErrors(errors) {
	// Mostrar errores específicos por campo
	if (errors.email) {
		const emailInput = document.getElementById('loginEmail');
		const emailError = document.getElementById('emailError');
		if (emailInput && emailError) {
			emailInput.classList.add('is-invalid');
			emailError.textContent = errors.email;
			emailError.style.display = 'block';
		}
	}

	if (errors.password) {
		const passwordInput = document.getElementById('loginPassword');
		const passwordError = document.getElementById('passwordError');
		if (passwordInput && passwordError) {
			passwordInput.classList.add('is-invalid');
			passwordError.textContent = errors.password;
			passwordError.style.display = 'block';
		}
	}
}

function toggleLoginPasswordVisibility() {
	const passwordInput = document.getElementById('loginPassword');
	const icon = document.querySelector('#toggleLoginPassword i');

	if (passwordInput.type === 'password') {
		passwordInput.type = 'text';
		icon.classList.remove('bi-eye');
		icon.classList.add('bi-eye-slash');
	} else {
		passwordInput.type = 'password';
		icon.classList.remove('bi-eye-slash');
		icon.classList.add('bi-eye');
	}
}

function showLoginError(message) {
	const errorDiv = document.getElementById('loginError');
	const errorMessage = document.getElementById('loginErrorMessage');

	if (errorDiv && errorMessage) {
		errorMessage.textContent = message;
		errorDiv.classList.remove('d-none');

		setTimeout(() => {
			errorDiv.classList.add('d-none');
		}, 5000);
	}
}

function showLoginSuccess(message) {
	// Crear o actualizar el elemento de éxito
	let successDiv = document.getElementById('loginSuccess');

	if (!successDiv) {
		// Crear elemento de éxito si no existe
		successDiv = document.createElement('div');
		successDiv.id = 'loginSuccess';
		successDiv.className = 'alert alert-success d-flex align-items-center';
		successDiv.innerHTML = `
            <i class="bi bi-check-circle-fill me-2"></i>
            <span id="loginSuccessMessage"></span>
        `;

		// Insertar antes del formulario
		const loginForm = document.getElementById('loginForm');
		loginForm.parentNode.insertBefore(successDiv, loginForm);
	}

	const successMessage = document.getElementById('loginSuccessMessage');
	if (successMessage) {
		successMessage.textContent = message;
		successDiv.classList.remove('d-none');

		setTimeout(() => {
			successDiv.classList.add('d-none');
		}, 8000);
	}
}

function isValidEmail(email) {
	const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
	return emailRegex.test(email);
}
