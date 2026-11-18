import { Component, inject, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CuentaService } from '../../services/cuenta.service';
import { TransaccionService } from '../../services/transaccion.service';
import { AuthService } from '../../services/auth.service';

interface OperacionRestItem {
  id: number;
  controlador: string;
  metodo: string;
  ruta: string;
  descripcion: string;
  formularioAsociado: string;
  servicioAngular: string;
  estadoPrueba?: string;
  respuestaPrueba?: string;
}

@Component({
  selector: 'app-operaciones-rest',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './operaciones-rest.html',
  styleUrl: './operaciones-rest.css'
})
export class OperacionesRestComponent {
  private readonly cuentaService = inject(CuentaService);
  private readonly transaccionService = inject(TransaccionService);
  readonly authService = inject(AuthService);
  private readonly cdr = inject(ChangeDetectorRef);

  operaciones: OperacionRestItem[] = [
    {
      id: 1,
      controlador: 'RestCuentaController',
      metodo: 'POST',
      ruta: '/api/cuentas',
      descripcion: 'Registra una nueva cuenta bancaria o financiera validando saldo no negativo.',
      formularioAsociado: 'Formulario 3 (Reactive Form)',
      servicioAngular: 'CuentaService.crear(cuenta)'
    },
    {
      id: 2,
      controlador: 'RestCuentaController',
      metodo: 'GET',
      ruta: '/api/cuentas',
      descripcion: 'Obtiene el listado completo de cuentas del usuario.',
      formularioAsociado: 'Listado en Cuentas y Dashboard',
      servicioAngular: 'CuentaService.listar()'
    },
    {
      id: 3,
      controlador: 'RestCuentaController',
      metodo: 'GET',
      ruta: '/api/cuentas/{id}',
      descripcion: 'Consulta el detalle de una cuenta por su identificador primario.',
      formularioAsociado: 'Carga datos en Formulario 4',
      servicioAngular: 'CuentaService.obtenerPorId(id)'
    },
    {
      id: 4,
      controlador: 'RestCuentaController',
      metodo: 'PUT',
      ruta: '/api/cuentas/{id}',
      descripcion: 'Actualiza los datos de una cuenta existente.',
      formularioAsociado: 'Formulario 4 (Template-driven)',
      servicioAngular: 'CuentaService.actualizar(id, cuenta)'
    },
    {
      id: 5,
      controlador: 'RestCuentaController',
      metodo: 'DELETE',
      ruta: '/api/cuentas/{id}',
      descripcion: 'Elimina lógicamente/físicamente una cuenta por ID.',
      formularioAsociado: 'Botón de eliminación en tabla',
      servicioAngular: 'CuentaService.eliminar(id)'
    },
    {
      id: 6,
      controlador: 'RestCuentaController',
      metodo: 'POST',
      ruta: '/api/cuentas/categorias',
      descripcion: 'Registra una nueva categoría de flujo (INGRESO o GASTO).',
      formularioAsociado: 'Formulario 8 (Template-driven)',
      servicioAngular: 'CuentaService.crearCategoria(cat)'
    },
    {
      id: 7,
      controlador: 'RestCuentaController',
      metodo: 'GET',
      ruta: '/api/cuentas/categorias',
      descripcion: 'Lista todas las categorías financieras configuradas.',
      formularioAsociado: 'Dropdown en Formulario 5 y Vista Categorías',
      servicioAngular: 'CuentaService.listarCategorias()'
    },
    {
      id: 8,
      controlador: 'RestCuentaController',
      metodo: 'GET',
      ruta: '/api/cuentas/resumen',
      descripcion: 'Calcula totales de ingresos, gastos, balance neto y estado financiero.',
      formularioAsociado: 'Tarjetas métricas en Dashboard',
      servicioAngular: 'CuentaService.obtenerResumen()'
    },
    {
      id: 9,
      controlador: 'RestTransaccionController',
      metodo: 'POST',
      ruta: '/api/transacciones',
      descripcion: 'Registra una transacción validando monto > 0, tipo y fecha.',
      formularioAsociado: 'Formulario 5 (Reactive Form)',
      servicioAngular: 'TransaccionService.crear(tx)'
    },
    {
      id: 10,
      controlador: 'RestTransaccionController',
      metodo: 'GET',
      ruta: '/api/transacciones',
      descripcion: 'Obtiene el historial de todas las transacciones financieras.',
      formularioAsociado: 'Tabla de movimientos en Transacciones',
      servicioAngular: 'TransaccionService.listar()'
    },
    {
      id: 11,
      controlador: 'RestTransaccionController',
      metodo: 'GET',
      ruta: '/api/transacciones/{id}',
      descripcion: 'Consulta los datos específicos de una transacción por ID.',
      formularioAsociado: 'Inspección de detalle',
      servicioAngular: 'TransaccionService.obtenerPorId(id)'
    },
    {
      id: 12,
      controlador: 'RestTransaccionController',
      metodo: 'DELETE',
      ruta: '/api/transacciones/{id}',
      descripcion: 'Elimina una transacción registrada del historial.',
      formularioAsociado: 'Botón eliminar en tabla',
      servicioAngular: 'TransaccionService.eliminar(id)'
    },
    {
      id: 13,
      controlador: 'RestTransaccionController',
      metodo: 'GET',
      ruta: '/api/transacciones/tipo/{tipo}',
      descripcion: 'Filtra transacciones por tipo (INGRESO o GASTO).',
      formularioAsociado: 'Formulario 7 (Reactive Form)',
      servicioAngular: 'TransaccionService.filtrarPorTipo(tipo)'
    },
    {
      id: 14,
      controlador: 'RestTransaccionController',
      metodo: 'GET',
      ruta: '/api/transacciones/categoria/{nombre}',
      descripcion: 'Filtra transacciones por coincidencia en el nombre de categoría.',
      formularioAsociado: 'Formulario 7 (Reactive Form)',
      servicioAngular: 'TransaccionService.filtrarPorCategoria(nom)'
    },
    {
      id: 15,
      controlador: 'RestTransaccionController',
      metodo: 'GET',
      ruta: '/api/transacciones/rango',
      descripcion: 'Filtra transacciones dentro de un intervalo de fechas (desde, hasta).',
      formularioAsociado: 'Formulario 6 (Template-driven)',
      servicioAngular: 'TransaccionService.filtrarPorRango(d, h)'
    },
    {
      id: 16,
      controlador: 'AuthController',
      metodo: 'POST',
      ruta: '/auth/login',
      descripcion: 'Autentica credenciales de usuario y emite token Bearer JWT.',
      formularioAsociado: 'Formulario 1 (Template-driven)',
      servicioAngular: 'AuthService.login(request)'
    },
    {
      id: 17,
      controlador: 'AuthController',
      metodo: 'POST',
      ruta: '/auth/register',
      descripcion: 'Registra una nueva cuenta de usuario en el sistema.',
      formularioAsociado: 'Formulario 2 (Reactive Form)',
      servicioAngular: 'AuthService.register(request)'
    }
  ];

  probarOperacion(op: OperacionRestItem): void {
    op.estadoPrueba = 'probando';
    op.respuestaPrueba = undefined;
    this.cdr.detectChanges();

    switch (op.id) {
      case 2: // GET /api/cuentas
        this.cuentaService.listar().subscribe({
          next: (res) => {
            op.estadoPrueba = 'ok';
            op.respuestaPrueba = JSON.stringify(res, null, 2);
            this.cdr.detectChanges();
          },
          error: (err) => {
            op.estadoPrueba = 'error';
            op.respuestaPrueba = JSON.stringify(err.error || err, null, 2);
            this.cdr.detectChanges();
          }
        });
        break;

      case 7: // GET /api/cuentas/categorias
        this.cuentaService.listarCategorias().subscribe({
          next: (res) => {
            op.estadoPrueba = 'ok';
            op.respuestaPrueba = JSON.stringify(res, null, 2);
            this.cdr.detectChanges();
          },
          error: (err) => {
            op.estadoPrueba = 'error';
            op.respuestaPrueba = JSON.stringify(err.error || err, null, 2);
            this.cdr.detectChanges();
          }
        });
        break;

      case 8: // GET /api/cuentas/resumen
        this.cuentaService.obtenerResumen().subscribe({
          next: (res) => {
            op.estadoPrueba = 'ok';
            op.respuestaPrueba = JSON.stringify(res, null, 2);
            this.cdr.detectChanges();
          },
          error: (err) => {
            op.estadoPrueba = 'error';
            op.respuestaPrueba = JSON.stringify(err.error || err, null, 2);
            this.cdr.detectChanges();
          }
        });
        break;

      case 10: // GET /api/transacciones
        this.transaccionService.listar().subscribe({
          next: (res) => {
            op.estadoPrueba = 'ok';
            op.respuestaPrueba = JSON.stringify(res, null, 2);
            this.cdr.detectChanges();
          },
          error: (err) => {
            op.estadoPrueba = 'error';
            op.respuestaPrueba = JSON.stringify(err.error || err, null, 2);
            this.cdr.detectChanges();
          }
        });
        break;

      case 13: // GET /api/transacciones/tipo/INGRESO
        this.transaccionService.filtrarPorTipo('INGRESO').subscribe({
          next: (res) => {
            op.estadoPrueba = 'ok';
            op.respuestaPrueba = JSON.stringify(res, null, 2);
            this.cdr.detectChanges();
          },
          error: (err) => {
            op.estadoPrueba = 'error';
            op.respuestaPrueba = JSON.stringify(err.error || err, null, 2);
            this.cdr.detectChanges();
          }
        });
        break;

      default:
        op.estadoPrueba = 'info';
        op.respuestaPrueba = `Esta operación (${op.metodo} ${op.ruta}) se prueba interactivamente desde su formulario asignado: ${op.formularioAsociado}.`;
        this.cdr.detectChanges();
        break;
    }
  }
}
