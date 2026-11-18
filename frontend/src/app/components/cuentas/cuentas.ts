import { Component, inject, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, NgForm, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { CuentaService } from '../../services/cuenta.service';
import { Cuenta } from '../../models/cuenta.model';

@Component({
  selector: 'app-cuentas',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule],
  templateUrl: './cuentas.html',
  styleUrl: './cuentas.css'
})
export class CuentasComponent implements OnInit {
  private readonly cuentaService = inject(CuentaService);
  private readonly fb = inject(FormBuilder);
  private readonly cdr = inject(ChangeDetectorRef);

  cuentas: Cuenta[] = [];
  isLoading = false;
  isCreando = false;
  isActualizando = false;
  successMessage = '';
  errorMessage = '';

  // Formulario 3: Reactive Form (Crear Cuenta)
  crearCuentaForm!: FormGroup;

  // Formulario 4: Template-driven Form (Actualizar Cuenta)
  cuentaEnEdicion: Cuenta = {
    id: 0,
    nombre: '',
    tipo: 'AHORROS',
    saldo: 0,
    moneda: 'PEN'
  };
  modoEdicion = false;

  tiposCuenta: string[] = ['AHORROS', 'CORRIENTE', 'EFECTIVO', 'TARJETA_CREDITO', 'INVERSION'];
  monedas: string[] = ['PEN', 'USD', 'EUR'];

  ngOnInit(): void {
    this.initForm();
    this.cargarCuentas();
  }

  initForm(): void {
    this.crearCuentaForm = this.fb.group({
      nombre: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(50)]],
      tipo: ['AHORROS', [Validators.required]],
      saldo: [0, [Validators.required, Validators.min(0)]],
      moneda: ['PEN', [Validators.required]]
    });
  }

  // GET /api/cuentas
  cargarCuentas(): void {
    this.isLoading = true;
    this.cdr.detectChanges();
    this.cuentaService.listar().subscribe({
      next: (data) => {
        this.cuentas = data;
        this.isLoading = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.errorMessage = 'Error al listar cuentas: ' + (err.error || err.message);
        this.isLoading = false;
        this.cdr.detectChanges();
      }
    });
  }

  // Formulario 3: POST /api/cuentas
  onCrearSubmit(): void {
    if (this.crearCuentaForm.invalid) {
      this.errorMessage = 'Por favor complete todos los datos de la cuenta.';
      this.crearCuentaForm.markAllAsTouched();
      return;
    }

    this.limpiarMensajes();
    this.isCreando = true;
    this.cdr.detectChanges();

    const nueva: Cuenta = this.crearCuentaForm.value;

    this.cuentaService.crear(nueva).subscribe({
      next: (creada) => {
        this.isCreando = false;
        this.successMessage = `Cuenta "${creada.nombre}" creada con éxito (ID: ${creada.id}).`;
        this.crearCuentaForm.reset({
          nombre: '',
          tipo: 'AHORROS',
          saldo: 0,
          moneda: 'PEN'
        });
        this.cargarCuentas();
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.isCreando = false;
        this.errorMessage = 'Error al crear cuenta: ' + (err.error || err.message);
        this.cdr.detectChanges();
      }
    });
  }

  // Cargar cuenta para Formulario 4 (GET /api/cuentas/{id})
  seleccionarParaEditar(cuenta: Cuenta): void {
    if (!cuenta.id) return;
    this.limpiarMensajes();

    this.cuentaService.obtenerPorId(cuenta.id).subscribe({
      next: (c) => {
        this.cuentaEnEdicion = { ...c };
        this.modoEdicion = true;
        this.cdr.detectChanges();
        const el = document.getElementById('formEdicionSection');
        if (el) el.scrollIntoView({ behavior: 'smooth' });
      },
      error: (err) => {
        this.errorMessage = 'Error al consultar cuenta: ' + (err.error || err.message);
        this.cdr.detectChanges();
      }
    });
  }

  cancelarEdicion(): void {
    this.modoEdicion = false;
    this.cuentaEnEdicion = {
      id: 0,
      nombre: '',
      tipo: 'AHORROS',
      saldo: 0,
      moneda: 'PEN'
    };
    this.cdr.detectChanges();
  }

  // Formulario 4: PUT /api/cuentas/{id}
  onActualizarSubmit(form: NgForm): void {
    if (form.invalid || !this.cuentaEnEdicion.id) {
      this.errorMessage = 'Verifique los datos del formulario de edición.';
      return;
    }

    this.limpiarMensajes();
    this.isActualizando = true;
    this.cdr.detectChanges();

    const id = this.cuentaEnEdicion.id;

    this.cuentaService.actualizar(id, this.cuentaEnEdicion).subscribe({
      next: (actualizada) => {
        this.isActualizando = false;
        this.successMessage = `Cuenta "${actualizada.nombre}" actualizada con éxito.`;
        this.cancelarEdicion();
        this.cargarCuentas();
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.isActualizando = false;
        this.errorMessage = 'Error al actualizar cuenta: ' + (err.error || err.message);
        this.cdr.detectChanges();
      }
    });
  }

  // DELETE /api/cuentas/{id}
  eliminarCuenta(cuenta: Cuenta): void {
    if (!cuenta.id) return;
    if (!confirm(`¿Está seguro de eliminar la cuenta "${cuenta.nombre}"?`)) {
      return;
    }

    this.limpiarMensajes();
    this.cuentaService.eliminar(cuenta.id).subscribe({
      next: () => {
        this.successMessage = `Cuenta "${cuenta.nombre}" eliminada exitosamente.`;
        this.cargarCuentas();
        if (this.cuentaEnEdicion.id === cuenta.id) {
          this.cancelarEdicion();
        }
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.errorMessage = 'Error al eliminar cuenta: ' + (err.error || err.message);
        this.cdr.detectChanges();
      }
    });
  }

  private limpiarMensajes(): void {
    this.successMessage = '';
    this.errorMessage = '';
  }
}
