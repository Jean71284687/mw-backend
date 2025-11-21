// FUNCIONES PARA LA LISTA PRINCIPAL DE PRODUCTOS

// Ver detalles del producto
function viewProductDetails(productId) {
    fetch(`/admin/dashboard/productos/${productId}`)
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                const producto = data.producto;
                // Llenar el modal de detalles
                document.getElementById('detailNombre').textContent = producto.nombre;
                document.getElementById('detailDescripcion').textContent = producto.descripcion || 'Sin descripción';
                document.getElementById('detailCategoria').textContent = producto.categoria;
                document.getElementById('detailCondicion').textContent = producto.condicion;
                document.getElementById('detailPrecio').textContent = `S/ ${producto.precio.toFixed(2)}`;
                document.getElementById('detailStock').textContent = producto.stock;
                document.getElementById('detailEstado').textContent = producto.activo ? 'ACTIVO' : 'INACTIVO';
                document.getElementById('detailEstado').className = producto.activo ? 'badge bg-success ms-2' : 'badge bg-danger ms-2';
                document.getElementById('detailMarca').textContent = producto.marca || '-';
                document.getElementById('detailModelo').textContent = producto.modelo || '-';
                document.getElementById('detailCodigo').textContent = producto.codigo || '-';

                const imagen = document.getElementById('detailImagen');
                if (producto.imagenUrl) {
                    imagen.src = producto.imagenUrl;
                    imagen.style.display = 'block';
                } else {
                    imagen.src = '/images/no-image.png';
                    imagen.style.display = 'block';
                }
            } else {
                alert('Error al cargar los detalles del producto');
            }
        })
        .catch(error => {
            console.error('Error:', error);
            alert('Error al cargar los detalles del producto');
        });
}

// Abrir modal de edición desde la lista principal
function abrirEdicionProducto(productId) {
    fetch(`/admin/dashboard/productos/${productId}`)
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                // Poblar formulario de edición
                document.getElementById('editProductoId').value = data.producto.id;
                document.getElementById('editNombre').value = data.producto.nombre;
                document.getElementById('editCategoria').value = data.producto.categoriaId;
                document.getElementById('editPrecio').value = data.producto.precio;
                document.getElementById('editStock').value = data.producto.stock;
                document.getElementById('editCondicion').value = data.producto.condicion;
                document.getElementById('editMarca').value = data.producto.marca;
                document.getElementById('editModelo').value = data.producto.modelo;
                document.getElementById('editDescripcion').value = data.producto.descripcion || '';
                document.getElementById('editImagenUrl').value = data.producto.imagenUrl || '';

                // Abrir modal de edición
                const modal = new bootstrap.Modal(document.getElementById('productoEdicionModal'));
                modal.show();
            } else {
                alert('Error al cargar el producto: ' + data.message);
            }
        })
        .catch(error => {
            console.error('Error:', error);
            alert('Error al cargar el producto');
        });
}

// Actualizar producto desde el modal de edición
function actualizarProducto() {
    const datos = {
        id: document.getElementById('editProductoId').value,
        nombre: document.getElementById('editNombre').value,
        categoriaId: document.getElementById('editCategoria').value,
        precio: parseFloat(document.getElementById('editPrecio').value),
        stock: parseInt(document.getElementById('editStock').value),
        condicion: document.getElementById('editCondicion').value,
        marca: document.getElementById('editMarca').value,
        modelo: document.getElementById('editModelo').value,
        descripcion: document.getElementById('editDescripcion').value,
        imagenUrl: document.getElementById('editImagenUrl').value,
        activo: true // Siempre activo cuando se edita desde aquí
    };

    if (!validarFormulario(datos)) return;

    fetch('/admin/dashboard/productos', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(datos)
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            alert('✅ ' + data.message);
            const modal = bootstrap.Modal.getInstance(document.getElementById('productoEdicionModal'));
            modal.hide();
            location.reload();
        } else {
            alert('❌ ' + data.message);
        }
    })
    .catch(error => {
        console.error('Error:', error);
        alert('❌ Error al actualizar el producto');
    });
}

// Cambiar estado del producto desde la lista principal
function cambiarEstadoProducto(productId, activar) {
    const accion = activar ? 'activar' : 'desactivar';

    if (confirm(`¿Estás seguro de que quieres ${accion} este producto?`)) {
        fetch(`/admin/dashboard/productos/${productId}/estado`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
            },
            body: `activo=${activar}`
        })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                alert('✅ ' + data.message);
                location.reload();
            } else {
                alert('❌ ' + data.message);
            }
        })
        .catch(error => {
            console.error('Error:', error);
            alert('❌ Error al cambiar estado');
        });
    }
}

// Exportar productos
function exportarProductos() {
    fetch('/admin/dashboard/productos/exportar')
        .then(response => response.text())
        .then(csvData => {
            // Crear y descargar archivo CSV
            const blob = new Blob([csvData], { type: 'text/csv' });
            const url = window.URL.createObjectURL(blob);
            const a = document.createElement('a');
            a.href = url;
            a.download = 'productos.csv';
            document.body.appendChild(a);
            a.click();
            document.body.removeChild(a);
            window.URL.revokeObjectURL(url);
        })
        .catch(error => {
            console.error('Error:', error);
            alert('Error al exportar productos');
        });
}

// FUNCIONES PARA EL MODAL CRUD COMPLETO

function abrirModalCRUD() {
    const modal = new bootstrap.Modal(document.getElementById('productoCrudModal'));
    modal.show();
    mostrarSeccion('agregar'); // Mostrar sección de agregar por defecto
}

function mostrarSeccion(seccion) {
    // Ocultar todas las secciones
    document.getElementById('seccionAgregar').style.display = 'none';
    document.getElementById('seccionGestionar').style.display = 'none';
    document.getElementById('botonesAgregar').style.display = 'none';
    document.getElementById('botonesGestionar').style.display = 'none';

    // Remover active de todas las pestañas
    document.getElementById('agregarTab').classList.remove('active');
    document.getElementById('gestionarTab').classList.remove('active');

    // Mostrar sección seleccionada
    if (seccion === 'agregar') {
        document.getElementById('seccionAgregar').style.display = 'block';
        document.getElementById('botonesAgregar').style.display = 'block';
        document.getElementById('agregarTab').classList.add('active');
        document.getElementById('formAgregarProducto').reset();
    } else if (seccion === 'gestionar') {
        document.getElementById('seccionGestionar').style.display = 'block';
        document.getElementById('botonesGestionar').style.display = 'block';
        document.getElementById('gestionarTab').classList.add('active');
        cargarProductosParaGestion();
    }
}

function guardarNuevoProducto() {
    const form = document.getElementById('formAgregarProducto');
    const formData = new FormData(form);
    const data = Object.fromEntries(formData.entries());

    // Convertir tipos
    data.precio = parseFloat(data.precio);
    data.stock = parseInt(data.stock);
    data.activo = data.activo === 'on';

    if (!validarFormulario(data)) return;

    fetch('/admin/dashboard/productos', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(data)
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            alert('✅ ' + data.message);
            form.reset();
            // Cambiar a sección de gestionar
            mostrarSeccion('gestionar');
        } else {
            alert('❌ ' + data.message);
        }
    })
    .catch(error => {
        console.error('Error:', error);
        alert('❌ Error al guardar el producto');
    });
}

function cargarProductosParaGestion() {
    fetch('/admin/dashboard/productos/listar')
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                renderizarTablaGestion(data.productos);
            } else {
                console.error('Error:', data.message);
            }
        })
        .catch(error => {
            console.error('Error:', error);
        });
}

function renderizarTablaGestion(productos) {
    const tbody = document.getElementById('tablaGestionProductos');
    tbody.innerHTML = '';

    productos.forEach(producto => {
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td><strong>${producto.id}</strong></td>
            <td>
                <div class="d-flex align-items-center">
                    <img src="${producto.imagenUrl || '/images/no-image.png'}"
                         class="rounded me-2" width="30" height="30" style="object-fit: cover;">
                    <div>
                        <div class="fw-bold">${producto.nombre}</div>
                        <small class="text-muted">${producto.marca} - ${producto.modelo}</small>
                    </div>
                </div>
            </td>
            <td>
                <span class="badge ${producto.activo ? 'bg-success' : 'bg-danger'}">
                    ${producto.activo ? 'ACTIVO' : 'INACTIVO'}
                </span>
            </td>
            <td class="text-center">
                <div class="btn-group btn-group-sm">
                    <button class="btn btn-outline-warning btn-sm"
                            onclick="editarDesdeGestion(${producto.id})">
                        <i class="icon ion-md-create"></i>
                    </button>
                    <button class="btn btn-outline-info btn-sm"
                            onclick="cambiarEstadoDesdeGestion(${producto.id}, ${!producto.activo})">
                        <i class="icon ion-md-power"></i>
                    </button>
                    <button class="btn btn-outline-danger btn-sm"
                            onclick="marcarInactivoDesdeGestion(${producto.id})">
                        <i class="icon ion-md-eye-off"></i>
                    </button>
                </div>
            </td>
        `;
        tbody.appendChild(tr);
    });
}

// Funciones CRUD para la sección de gestión
function editarDesdeGestion(productId) {
    // Cerrar modal actual y abrir edición específica
    const modal = bootstrap.Modal.getInstance(document.getElementById('productoCrudModal'));
    modal.hide();

    setTimeout(() => {
        abrirEdicionProducto(productId);
    }, 500);
}

function cambiarEstadoDesdeGestion(productId, nuevoEstado) {
    cambiarEstadoProducto(productId, nuevoEstado);
}

function marcarInactivoDesdeGestion(productId) {
    if (confirm('¿Estás seguro de que quieres marcar este producto como INACTIVO?')) {
        fetch(`/admin/dashboard/productos/${productId}/eliminar`, {
            method: 'POST'
        })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                alert('✅ ' + data.message);
                cargarProductosParaGestion(); // Recargar la lista
            } else {
                alert('❌ ' + data.message);
            }
        })
        .catch(error => {
            console.error('Error:', error);
            alert('❌ Error al marcar como inactivo');
        });
    }
}

function recargarListaGestion() {
    cargarProductosParaGestion();
}

// Función de validación común
function validarFormulario(datos) {
    if (!datos.nombre || !datos.categoriaId || !datos.precio || !datos.stock ||
        !datos.marca || !datos.modelo) {
        alert('❌ Por favor complete todos los campos obligatorios (*)');
        return false;
    }

    if (datos.precio <= 0) {
        alert('❌ El precio debe ser mayor a 0');
        return false;
    }

    if (datos.stock < 0) {
        alert('❌ El stock no puede ser negativo');
        return false;
    }

    return true;
}

// Inicialización
document.addEventListener('DOMContentLoaded', function() {
    console.log('✅ Sistema CRUD de productos cargado');
});