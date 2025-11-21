document.addEventListener('DOMContentLoaded', function () {
	const btnEdit = document.getElementById('btnEditProfile');
	const btnSave = document.getElementById('btnSaveProfile');
	const btnCancel = document.getElementById('btnCancelEdit');
	const form = document.getElementById('profileForm');

	if (!form || !btnEdit) return;

	// Save original values to allow cancel
	const originalValues = {};
	Array.from(form.elements).forEach((el) => {
		if (el.name) originalValues[el.name] = el.value;
	});

	const toggleEdit = (editing) => {
		Array.from(form.elements).forEach((el) => {
			// don't enable disabled fields like email
			if (el.id === 'email') return;
			if (el.type === 'submit' || el.type === 'button') return;
			if (el.name) {
				if (editing) {
					el.removeAttribute('readonly');
					el.removeAttribute('disabled');
				} else {
					el.setAttribute('readonly', 'readonly');
				}
			}
		});

		// Toggle buttons
		if (editing) {
			btnEdit.classList.add('d-none');
			btnSave.classList.remove('d-none');
			btnCancel.classList.remove('d-none');
		} else {
			btnEdit.classList.remove('d-none');
			btnSave.classList.add('d-none');
			btnCancel.classList.add('d-none');
		}
	};

	btnEdit.addEventListener('click', function (e) {
		e.preventDefault();
		toggleEdit(true);
	});

	btnCancel.addEventListener('click', function (e) {
		e.preventDefault();
		// restore original values
		Array.from(form.elements).forEach((el) => {
			if (el.name && originalValues[el.name] !== undefined) el.value = originalValues[el.name];
		});
		toggleEdit(false);
	});

	// Optional: when saving, we leave behavior to form submit; after submit the page reloads
});
