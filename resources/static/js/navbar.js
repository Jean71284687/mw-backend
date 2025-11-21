// Debug y mejoras para el navbar y modales

document.addEventListener('DOMContentLoaded', function () {
	console.log('Navbar script loaded');

	// Verificar si Bootstrap está cargado
	if (typeof bootstrap === 'undefined') {
		console.error('Bootstrap no está cargado!');
		return;
	} else {
		console.log('Bootstrap está cargado correctamente');
	}

	// Debug del modal (sin crear instancia manual)
	const loginModal = document.getElementById('loginModal');
	if (loginModal) {
		console.log('Login modal encontrado');

		// Event listeners para debug solamente
		loginModal.addEventListener('show.bs.modal', function () {
			console.log('Modal está a punto de mostrarse');
		});

		loginModal.addEventListener('shown.bs.modal', function () {
			console.log('Modal se ha mostrado completamente');
		});

		loginModal.addEventListener('hide.bs.modal', function () {
			console.log('Modal está a punto de ocultarse');
		});
	} else {
		console.error('Login modal NO encontrado');
	}

	// Verificar botones de login
	const loginButtons = document.querySelectorAll('[data-bs-target="#loginModal"]');
	console.log('Botones de login encontrados:', loginButtons.length);

	loginButtons.forEach((btn, index) => {
		console.log(`Botón ${index + 1}:`, btn);

		// Agregar event listener adicional para debug
		btn.addEventListener('click', function (e) {
			console.log('Click en botón de login detectado');
			console.log('Target:', e.target);
			console.log('Data attributes:', this.dataset);
		});
	});

	// Inicializar funcionalidad del modal de login
	initializeLoginModal();

	// Inicializar funcionalidad del modal de registro
	initializeRegisterModal();

	// Función manual para abrir el modal (para testing)
	window.openLoginModal = function () {
		const loginModal = document.getElementById('loginModal');
		if (loginModal) {
			const modal = new bootstrap.Modal(loginModal);
			modal.show();
			console.log('Modal abierto manualmente');
		} else {
			console.error('No se puede abrir el modal: elemento no encontrado');
		}
	};

	// Mejorar el navbar collapse en móviles
	const navbarToggler = document.querySelector('.navbar-toggler');
	const navbarCollapse = document.querySelector('.navbar-collapse');

	if (navbarToggler && navbarCollapse) {
		navbarToggler.addEventListener('click', function () {
			console.log('Toggle navbar clicked');
		});
	}

	// Auto-cerrar navbar en móviles al hacer click en un enlace
	const navLinks = document.querySelectorAll('.navbar-nav .nav-link');
	navLinks.forEach((link) => {
		link.addEventListener('click', function () {
			const navbarCollapse = document.querySelector('.navbar-collapse');
			if (navbarCollapse && navbarCollapse.classList.contains('show')) {
				const bsCollapse = new bootstrap.Collapse(navbarCollapse);
				bsCollapse.hide();
			}
		});
	});
});

// Funcionalidad del modal de login
function initializeLoginModal() {
	// Toggle password visibility
	const togglePassword = document.getElementById('togglePassword');
	const passwordInput = document.getElementById('loginPassword');

	if (togglePassword && passwordInput) {
		togglePassword.addEventListener('click', function () {
			const type = passwordInput.getAttribute('type') === 'password' ? 'text' : 'password';
			passwordInput.setAttribute('type', type);

			const icon = this.querySelector('i');
			if (type === 'password') {
				icon.classList.remove('bi-eye-slash');
				icon.classList.add('bi-eye');
			} else {
				icon.classList.remove('bi-eye');
				icon.classList.add('bi-eye-slash');
			}
		});
	}

	// Form validation
	const loginForm = document.getElementById('loginForm');
	if (loginForm) {
		loginForm.addEventListener('submit', function (e) {
			const email = document.getElementById('loginEmail').value;
			const password = document.getElementById('loginPassword').value;

			if (!email || !password) {
				e.preventDefault();
				showLoginError('Por favor, completa todos los campos');
			}
		});
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

// Función global para testing
window.testModal = function () {
	console.log('=== TEST MODAL ===');
	console.log('Bootstrap disponible:', typeof bootstrap !== 'undefined');
	console.log('Modal element:', document.getElementById('loginModal'));
	console.log('Login buttons:', document.querySelectorAll('[data-bs-target="#loginModal"]'));

	// Intentar abrir el modal directamente
	const loginModal = document.getElementById('loginModal');
	if (loginModal) {
		try {
			const modal = new bootstrap.Modal(loginModal);
			modal.show();
			console.log('Modal abierto exitosamente');
		} catch (error) {
			console.error('Error al abrir modal:', error);
		}
	}
};

// Funcionalidad para el modal de registro
function initializeRegisterModal() {
	// Toggle password visibility para register
	const toggleRegisterPassword = document.getElementById('toggleRegisterPassword');
	const registerPasswordInput = document.getElementById('registerPassword');

	if (toggleRegisterPassword && registerPasswordInput) {
		toggleRegisterPassword.addEventListener('click', function () {
			const type = registerPasswordInput.getAttribute('type') === 'password' ? 'text' : 'password';
			registerPasswordInput.setAttribute('type', type);

			const icon = this.querySelector('i');
			if (type === 'password') {
				icon.classList.remove('bi-eye-slash');
				icon.classList.add('bi-eye');
			} else {
				icon.classList.remove('bi-eye');
				icon.classList.add('bi-eye-slash');
			}
		});
	}

	// Toggle confirm password visibility
	const toggleConfirmPassword = document.getElementById('toggleConfirmPassword');
	const confirmPasswordInput = document.getElementById('registerConfirmPassword');

	if (toggleConfirmPassword && confirmPasswordInput) {
		toggleConfirmPassword.addEventListener('click', function () {
			const type = confirmPasswordInput.getAttribute('type') === 'password' ? 'text' : 'password';
			confirmPasswordInput.setAttribute('type', type);

			const icon = this.querySelector('i');
			if (type === 'password') {
				icon.classList.remove('bi-eye-slash');
				icon.classList.add('bi-eye');
			} else {
				icon.classList.remove('bi-eye');
				icon.classList.add('bi-eye-slash');
			}
		});
	}

	// Password strength validation
	if (registerPasswordInput) {
		registerPasswordInput.addEventListener('input', function () {
			const password = this.value;
			updatePasswordStrength(password);
		});
	}

	// Password confirmation validation
	if (confirmPasswordInput && registerPasswordInput) {
		confirmPasswordInput.addEventListener('input', function () {
			validatePasswordMatch();
		});
		registerPasswordInput.addEventListener('input', function () {
			validatePasswordMatch();
		});
	}

	// Form validation
	const registerForm = document.getElementById('registerForm');
	if (registerForm) {
		registerForm.addEventListener('submit', function (e) {
			if (!validateRegisterForm()) {
				e.preventDefault();
			}
		});
	}
}

// Función para validar la fuerza de la contraseña
function updatePasswordStrength(password) {
	const strengthBar = document.querySelector('.password-strength-bar');
	if (!strengthBar) return;

	let strength = 0;

	if (password.length >= 8) strength++;
	if (/[a-z]/.test(password)) strength++;
	if (/[A-Z]/.test(password)) strength++;
	if (/[0-9]/.test(password)) strength++;
	if (/[^A-Za-z0-9]/.test(password)) strength++;

	strengthBar.classList.remove('weak', 'medium', 'strong');

	if (strength <= 2) {
		strengthBar.classList.add('weak');
	} else if (strength <= 4) {
		strengthBar.classList.add('medium');
	} else {
		strengthBar.classList.add('strong');
	}
}

// Función para validar que las contraseñas coincidan
function validatePasswordMatch() {
	const password = document.getElementById('registerPassword');
	const confirmPassword = document.getElementById('registerConfirmPassword');
	const matchInfo = document.getElementById('passwordMatchInfo');

	if (!password || !confirmPassword || !matchInfo) return;

	if (confirmPassword.value && password.value !== confirmPassword.value) {
		matchInfo.textContent = 'Las contraseñas no coinciden';
		matchInfo.className = 'info-text text-danger';
		confirmPassword.classList.add('is-invalid');
	} else if (confirmPassword.value && password.value === confirmPassword.value) {
		matchInfo.textContent = 'Las contraseñas coinciden';
		matchInfo.className = 'info-text text-success';
		confirmPassword.classList.remove('is-invalid');
	} else {
		matchInfo.textContent = '';
		confirmPassword.classList.remove('is-invalid');
	}
}

// Función para validar todo el formulario de registro
function validateRegisterForm() {
	const firstName = document.getElementById('firstName');
	const lastName = document.getElementById('lastName');
	const email = document.getElementById('registerEmail');
	const password = document.getElementById('registerPassword');
	const confirmPassword = document.getElementById('registerConfirmPassword');
	const terms = document.getElementById('termsCheck');

	let isValid = true;

	// Validar campos requeridos
	[firstName, lastName, email, password, confirmPassword].forEach((field) => {
		if (field && !field.value.trim()) {
			field.classList.add('is-invalid');
			isValid = false;
		} else if (field) {
			field.classList.remove('is-invalid');
		}
	});

	// Validar email
	if (email && email.value && !isValidEmail(email.value)) {
		email.classList.add('is-invalid');
		showRegisterError('Por favor, ingresa un email válido');
		isValid = false;
	}

	// Validar contraseñas
	if (password && confirmPassword && password.value !== confirmPassword.value) {
		confirmPassword.classList.add('is-invalid');
		showRegisterError('Las contraseñas no coinciden');
		isValid = false;
	}

	// Validar términos
	if (terms && !terms.checked) {
		showRegisterError('Debes aceptar los términos y condiciones');
		isValid = false;
	}

	return isValid;
}

// Función para validar email
function isValidEmail(email) {
	const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
	return emailRegex.test(email);
}

// Función para mostrar errores de registro
function showRegisterError(message) {
	const errorDiv = document.getElementById('registerError');
	const errorMessage = document.getElementById('registerErrorMessage');

	if (errorDiv && errorMessage) {
		errorMessage.textContent = message;
		errorDiv.classList.remove('d-none');

		setTimeout(() => {
			errorDiv.classList.add('d-none');
		}, 5000);
	}
}

// Manejar el envío del formulario de registro
function handleRegisterSubmit() {
	const registerForm = document.getElementById('registerForm');

	if (registerForm) {
		registerForm.addEventListener('submit', async function (e) {
			e.preventDefault();

			console.log('Formulario de registro enviado');

			// Limpiar errores previos
			clearRegisterErrors();

			// Validar que las contraseñas coincidan
			const password = document.getElementById('registerPassword').value;
			const confirmPassword = document.getElementById('confirmPassword')?.value;

			if (confirmPassword && password !== confirmPassword) {
				showRegisterError('Las contraseñas no coinciden');
				return;
			}

			// Mostrar estado de carga
			const submitButton = registerForm.querySelector('button[type="submit"]');
			const originalText = submitButton.innerHTML;
			submitButton.innerHTML = '<i class="bi bi-hourglass-split me-2"></i>Creando cuenta...';
			submitButton.disabled = true;

			try {
				// Recopilar datos del formulario
				const formData = new FormData(registerForm);

				// Enviar con fetch
				const response = await fetch('/web/auth/register', {
					method: 'POST',
					body: formData,
				});

				const result = await response.json();

				if (response.ok && result.success === true) {
					// Registro exitoso
					showRegisterSuccess(result.message || '¡Cuenta creada exitosamente! Ahora puedes iniciar sesión.');

					// Limpiar formulario
					registerForm.reset();

					// Sincronizar carrito del localStorage con la BD
					if (typeof CartLocal !== 'undefined') {
						await CartLocal.syncWithServer();
					}

					// Cerrar modal de registro después de un breve delay
					setTimeout(() => {
						const registerModal = bootstrap.Modal.getInstance(document.getElementById('registerModal'));
						if (registerModal) {
							registerModal.hide();
						}

						// Recargar la página para actualizar el navbar con la sesión iniciada
						setTimeout(() => {
							window.location.reload();
						}, 500);
					}, 2000);
				} else {
					// Manejar errores de validación o de registro
					if (result.errors) {
						// Errores de validación por campo
						displayRegisterFieldErrors(result.errors);
					} else if (result.error) {
						// Error general
						showRegisterError(result.error);
					} else {
						throw new Error('Error en el registro. Por favor intenta nuevamente.');
					}
				}
			} catch (error) {
				console.error('Error en registro:', error);
				showRegisterError(error.message || 'Error al crear la cuenta. Por favor intenta nuevamente.');
			} finally {
				// Restaurar botón
				submitButton.innerHTML = originalText;
				submitButton.disabled = false;
			}
		});
	}
}

function clearRegisterErrors() {
	// Limpiar error general
	const errorDiv = document.getElementById('registerError');
	if (errorDiv) {
		errorDiv.classList.add('d-none');
	}

	// Limpiar errores de campos
	const fields = ['name', 'lastName', 'email', 'cel', 'address', 'password'];
	fields.forEach((field) => {
		const input = document.getElementById(
			field === 'email'
				? 'registerEmail'
				: field === 'name'
				? 'registerFirstName'
				: field === 'lastName'
				? 'registerLastName'
				: field === 'cel'
				? 'registerPhone'
				: field === 'address'
				? 'registerAddress'
				: 'registerPassword'
		);

		const errorDiv = document.getElementById(
			field === 'email' ? 'emailErrorRegister' : field === 'password' ? 'passwordErrorRegister' : field + 'Error'
		);

		if (input) {
			input.classList.remove('is-invalid');
		}
		if (errorDiv) {
			errorDiv.textContent = '';
			errorDiv.style.display = 'none';
		}
	});
}

function displayRegisterFieldErrors(errors) {
	// Mapeo de campos del backend a IDs del frontend
	const fieldMapping = {
		name: { input: 'registerFirstName', error: 'nameError' },
		lastName: { input: 'registerLastName', error: 'lastNameError' },
		email: { input: 'registerEmail', error: 'emailErrorRegister' },
		cel: { input: 'registerPhone', error: 'celError' },
		address: { input: 'registerAddress', error: 'addressError' },
		password: { input: 'registerPassword', error: 'passwordErrorRegister' },
	};

	// Mostrar errores específicos por campo
	Object.keys(errors).forEach((fieldName) => {
		const mapping = fieldMapping[fieldName];
		if (mapping) {
			const input = document.getElementById(mapping.input);
			const errorDiv = document.getElementById(mapping.error);

			if (input && errorDiv) {
				input.classList.add('is-invalid');
				errorDiv.textContent = errors[fieldName];
				errorDiv.style.display = 'block';
			}
		}
	});
}

// Función para mostrar mensaje de éxito en registro
function showRegisterSuccess(message) {
	// Crear o actualizar el elemento de éxito
	let successDiv = document.getElementById('registerSuccess');

	if (!successDiv) {
		// Crear elemento de éxito si no existe
		successDiv = document.createElement('div');
		successDiv.id = 'registerSuccess';
		successDiv.className = 'alert alert-success d-flex align-items-center';
		successDiv.innerHTML = `
			<i class="bi bi-check-circle-fill me-2"></i>
			<span id="registerSuccessMessage"></span>
		`;

		// Insertar antes del formulario
		const registerForm = document.getElementById('registerForm');
		registerForm.parentNode.insertBefore(successDiv, registerForm);
	}

	const successMessage = document.getElementById('registerSuccessMessage');
	if (successMessage) {
		successMessage.textContent = message;
		successDiv.classList.remove('d-none');

		setTimeout(() => {
			successDiv.classList.add('d-none');
		}, 10000);
	}
}

// Inicializar el manejo del registro
document.addEventListener('DOMContentLoaded', function () {
	handleRegisterSubmit();
});

// Función para manejar logout
function logout() {
	if (confirm('¿Estás seguro de que quieres cerrar sesión?')) {
		// Mostrar indicador de carga
		const userDropdown = document.querySelector('.user-profile-bubble');
		if (userDropdown) {
			userDropdown.innerHTML = '<i class="bi bi-hourglass-split me-2"></i>Cerrando sesión...';
		}

		// Enviar solicitud de logout
		fetch('/web/auth/logout', {
			method: 'POST',
			credentials: 'include', // Incluir cookies
			cache: 'no-cache', // No usar cache
		})
			.then((response) => response.json())
			.then((data) => {
				if (data.success === 'true') {
					// Logout exitoso
					console.log('Logout exitoso, redirigiendo...');

					// Limpiar cualquier dato del localStorage
					localStorage.clear();
					sessionStorage.clear();

					// Forzar recarga completa sin cache
					setTimeout(() => {
						window.location.href = window.location.pathname + '?_=' + new Date().getTime();
					}, 100);
				} else {
					throw new Error(data.error || 'Error al cerrar sesión');
				}
			})
			.catch((error) => {
				console.error('Error en logout:', error);
				alert('Error al cerrar sesión. Por favor intenta nuevamente.');

				// Restaurar el contenido original del dropdown
				if (userDropdown) {
					// Forzar recarga en caso de error también
					window.location.reload(true);
				}
			});
	}
}
