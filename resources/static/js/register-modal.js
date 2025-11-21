document.addEventListener('DOMContentLoaded', function () {
	// Toggle password visibility para registro
	const toggleRegisterPassword = document.getElementById('toggleRegisterPassword');
	const registerPassword = document.getElementById('registerPassword');

	if (toggleRegisterPassword && registerPassword) {
		toggleRegisterPassword.addEventListener('click', function () {
			const type = registerPassword.type === 'password' ? 'text' : 'password';
			registerPassword.type = type;
			const icon = this.querySelector('i');
			icon.classList.toggle('bi-eye');
			icon.classList.toggle('bi-eye-slash');
		});
	}

	// Toggle confirm password visibility
	const toggleConfirmPassword = document.getElementById('toggleConfirmPassword');
	const confirmPassword = document.getElementById('registerConfirmPassword');

	if (toggleConfirmPassword && confirmPassword) {
		toggleConfirmPassword.addEventListener('click', function () {
			const type = confirmPassword.type === 'password' ? 'text' : 'password';
			confirmPassword.type = type;
			const icon = this.querySelector('i');
			icon.classList.toggle('bi-eye');
			icon.classList.toggle('bi-eye-slash');
		});
	}

	// Password strength indicator
	if (registerPassword) {
		registerPassword.addEventListener('input', function () {
			const password = this.value;
			const strengthBar = document.getElementById('passwordStrengthBar');
			const strengthText = document.getElementById('passwordStrengthText');

			let strength = 0;
			if (password.length >= 8) strength++;
			if (password.match(/[a-z]/) && password.match(/[A-Z]/)) strength++;
			if (password.match(/[0-9]/)) strength++;
			if (password.match(/[^a-zA-Z0-9]/)) strength++;

			strengthBar.className = 'password-strength-bar';
			if (strength <= 2) {
				strengthBar.classList.add('weak');
				strengthText.innerHTML = '<i class="bi bi-shield-x me-1"></i>Contraseña débil';
				strengthText.style.color = '#dc3545';
			} else if (strength === 3) {
				strengthBar.classList.add('medium');
				strengthText.innerHTML = '<i class="bi bi-shield-check me-1"></i>Contraseña media';
				strengthText.style.color = '#ffc107';
			} else {
				strengthBar.classList.add('strong');
				strengthText.innerHTML = '<i class="bi bi-shield-fill-check me-1"></i>Contraseña fuerte';
				strengthText.style.color = '#28a745';
			}
		});
	}

	// Password match validation
	if (confirmPassword) {
		confirmPassword.addEventListener('input', function () {
			const matchError = document.getElementById('passwordMatchError');
			if (this.value !== registerPassword.value && this.value.length > 0) {
				matchError.classList.remove('d-none');
			} else {
				matchError.classList.add('d-none');
			}
		});
	}

	// Form validation
	const registerForm = document.getElementById('registerForm');
	if (registerForm) {
		registerForm.addEventListener('submit', function (e) {
			if (registerPassword.value !== confirmPassword.value) {
				e.preventDefault();
				alert('Las contraseñas no coinciden. Por favor, verifica e intenta nuevamente.');
				confirmPassword.focus();
			}
		});
	}
});
