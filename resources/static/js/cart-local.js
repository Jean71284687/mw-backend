/**
 * Gestor de Carrito de Compras con localStorage
 * Maneja el carrito para usuarios no autenticados
 */

const CartLocal = {
	STORAGE_KEY: 'modatec_cart',

	/**
	 * Obtiene el carrito del localStorage
	 */
	getCart() {
		try {
			const cart = localStorage.getItem(this.STORAGE_KEY);
			return cart ? JSON.parse(cart) : [];
		} catch (error) {
			console.error('Error al obtener carrito:', error);
			return [];
		}
	},

	/**
	 * Guarda el carrito en localStorage
	 */
	saveCart(cart) {
		try {
			localStorage.setItem(this.STORAGE_KEY, JSON.stringify(cart));
			this.updateCartBadge();
			return true;
		} catch (error) {
			console.error('Error al guardar carrito:', error);
			return false;
		}
	},

	/**
	 * Agrega un producto al carrito
	 */
	addItem(productId, productName, price, discount, imageUrl, quantity = 1) {
		let cart = this.getCart();

		// Buscar si el producto ya existe
		const existingItem = cart.find((item) => item.productId === productId);

		if (existingItem) {
			// Actualizar cantidad
			existingItem.quantity += quantity;
		} else {
			// Agregar nuevo item
			cart.push({
				productId,
				productName,
				price,
				discount: discount || 0,
				imageUrl,
				quantity,
				addedAt: new Date().toISOString(),
			});
		}

		this.saveCart(cart);
		return true;
	},

	/**
	 * Actualiza la cantidad de un item
	 */
	updateQuantity(productId, quantity) {
		let cart = this.getCart();
		const item = cart.find((item) => item.productId === productId);

		if (item) {
			if (quantity <= 0) {
				return this.removeItem(productId);
			}
			item.quantity = quantity;
			this.saveCart(cart);
			return true;
		}
		return false;
	},

	/**
	 * Elimina un item del carrito
	 */
	removeItem(productId) {
		let cart = this.getCart();
		cart = cart.filter((item) => item.productId !== productId);
		this.saveCart(cart);
		return true;
	},

	/**
	 * Limpia todo el carrito
	 */
	clearCart() {
		localStorage.removeItem(this.STORAGE_KEY);
		this.updateCartBadge();
		return true;
	},

	/**
	 * Obtiene el número total de items
	 */
	getTotalItems() {
		const cart = this.getCart();
		return cart.reduce((total, item) => total + item.quantity, 0);
	},

	/**
	 * Calcula el total del carrito
	 */
	getTotal() {
		const cart = this.getCart();
		return cart.reduce((total, item) => {
			const itemPrice = item.discount > 0 ? item.price * (1 - item.discount / 100) : item.price;
			return total + itemPrice * item.quantity;
		}, 0);
	},

	/**
	 * Actualiza el badge del carrito en el navbar
	 */
	updateCartBadge() {
		const totalItems = this.getTotalItems();
		const badge = document.querySelector('.cart-badge');

		if (badge) {
			badge.textContent = totalItems;
			badge.style.display = totalItems > 0 ? 'inline-block' : 'none';
		}
	},

	/**
	 * Obtiene los datos del carrito para enviar al servidor
	 * Formato esperado por el backend
	 */
	getCartForServer() {
		const cart = this.getCart();
		return cart.map((item) => ({
			productId: item.productId,
			quantity: item.quantity,
		}));
	},

	/**
	 * Sincroniza el carrito local con el servidor
	 * Se llama después del login/registro
	 */
	async syncWithServer() {
		const cart = this.getCartForServer();

		if (cart.length === 0) {
			return { success: true, message: 'No hay items para sincronizar' };
		}

		try {
			const response = await fetch('/web/cart/sync', {
				method: 'POST',
				headers: {
					'Content-Type': 'application/json',
				},
				body: JSON.stringify(cart),
			});

			if (response.ok) {
				// Limpiar localStorage después de sincronizar
				this.clearCart();
				return { success: true, message: 'Carrito sincronizado exitosamente' };
			} else {
				const error = await response.text();
				return { success: false, message: error };
			}
		} catch (error) {
			console.error('Error al sincronizar carrito:', error);
			return { success: false, message: 'Error al sincronizar el carrito' };
		}
	},
};

// Inicializar al cargar la página
document.addEventListener('DOMContentLoaded', function () {
	CartLocal.updateCartBadge();
});

// Exportar para uso global
window.CartLocal = CartLocal;
