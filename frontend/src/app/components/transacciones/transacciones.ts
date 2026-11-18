import { Component, inject, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, NgForm, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { TransaccionService } from '../../services/transaccion.service';
import { CuentaService } from '../../services/cuenta.service';
import { Transaccion } from '../../models/transaccion.model';
import { Categoria } from '../../models/categoria.model';

@Component({
  selector: 'app-transacciones',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule],
  templateUrl: './transacciones.html',
  styleUrl: './transacciones.css'
})
export class TransaccionesComponent implements OnInit {
  private readonly transaccionService = inject(TransaccionService);
  private readonly cuentaService = inject(CuentaService);
  private readonly fb = inject(FormBuilder);
  private readonly cdr = inject(ChangeDetectorRef);

  transacciones: Transaccion[] = [];
  categorias: Categoria[] = [];
  isLoading = false;
  isCreando = false;
  isFiltrando = false;
  successMessage = '';
  errorMessage = '';

  // Formulario 5: Reactive Form (Crear Transacción)
  crearTxForm!: FormGroup;

  // Formulario 6: Template-driven Form (Filtro por Rango de Fechas)
  filtroRango = {
    desde: this.obtenerPrimerDiaMes(),
    hasta: this.obtenerFechaHoy()
  };

  // Formulario 7: Reactive Form (Filtro por Tipo y Categoría)
  filtroTipoCatForm!: FormGroup;

  ngOnInit(): void {
    this.initForms();
    this.cargarTransacciones();
    this.cargarCategorias();
  }

  obtenerFechaHoy(): string {
    return new Date().toISOString().substring(0, 10);
  }

  obtenerPrimerDiaMes(): string {
    const d = new Date();
    d.setDate(1);
    return d.toISOString().substring(0, 10);
  }

  initForms(): void {
    // Formulario 5: Reactive Form (Registro)
    this.crearTxForm = this.fb.group({
      descripcion: ['', [Validators.required, Validators.minLength(3)]],
      monto: [null, [Validators.required, Validators.min(0.01)]],
      tipo: ['INGRESO', [Validators.required]],
      categoria: ['Salario', [Validators.required]],
      fecha: [this.obtenerFechaHoy(), [Validators.required]]
    });

    // Formulario 7: Reactive Form (Filtro por Tipo y Categoría)
    this.filtroTipoCatForm = this.fb.group({
      tipo: ['TODOS'],
      categoriaTexto: ['']
    });
  }

  // GET /api/cuentas/categorias (para llenar opciones)
  cargarCategorias(): void {
    this.cuentaService.listarCategorias().subscribe({
      next: (data) => {
        this.categorias = data;
        this.cdr.detectChanges();
      },
      error: (err) => console.error('Error cargando categorías', err)
    });
  }

  // GET /api/transacciones
  cargarTransacciones(): void {
    this.isLoading = true;
    this.cdr.detectChanges();
    this.transaccionService.listar().subscribe({
      next: (data) => {
        this.transacciones = data;
        this.isLoading = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.errorMessage = 'Error al listar transacciones: ' + (err.error || err.message);
        this.isLoading = false;
        this.cdr.detectChanges();
      }
    });
  }

  // Formulario 5: POST /api/transacciones
  onCrearTxSubmit(): void {
    if (this.crearTxForm.invalid) {
      this.errorMessage = 'Por favor complete todos los datos de la transacción.';
      this.crearTxForm.markAllAsTouched();
      return;
    }

    this.limpiarMensajes();
    this.isCreando = true;
    this.cdr.detectChanges();

    const nueva: Transaccion = {
      descripcion: this.crearTxForm.value.descripcion,
      monto: Number(this.crearTxForm.value.monto),
      tipo: this.crearTxForm.value.tipo,
      categoria: this.crearTxForm.value.categoria,
      fecha: this.crearTxForm.value.fecha
    };

    this.transaccionService.crear(nueva).subscribe({
      next: (tx) => {
        this.isCreando = false;
        this.successMessage = `Transacción "${tx.descripcion}" registrada exitosamente (ID: ${tx.id}).`;
        this.crearTxForm.reset({
          descripcion: '',
          monto: null,
          tipo: 'INGRESO',
          categoria: 'Salario',
          fecha: this.obtenerFechaHoy()
        });
        this.cargarTransacciones();
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.isCreando = false;
        this.errorMessage = 'Error al registrar transacción: ' + (err.error || err.message);
        this.cdr.detectChanges();
      }
    });
  }

  // Formulario 6: GET /api/transacciones/rango?desde=...&hasta=... (Template-driven Form)
  onFiltroRangoSubmit(form: NgForm): void {
    if (form.invalid) return;

    if (this.filtroRango.desde > this.filtroRango.hasta) {
      this.errorMessage = 'La fecha "Desde" debe ser anterior o igual a la fecha "Hasta".';
      this.cdr.detectChanges();
      return;
    }

    this.limpiarMensajes();
    this.isLoading = true;
    this.cdr.detectChanges();

    this.transaccionService.filtrarPorRango(this.filtroRango.desde, this.filtroRango.hasta).subscribe({
      next: (data) => {
        this.transacciones = data;
        this.isLoading = false;
        this.successMessage = `Filtro por rango aplicado (${this.filtroRango.desde} a ${this.filtroRango.hasta}): ${data.length} resultado(s).`;
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.errorMessage = 'Error en consulta de rango: ' + (err.error || err.message);
        this.isLoading = false;
        this.cdr.detectChanges();
      }
    });
  }

  // Formulario 7: GET /api/transacciones/tipo/{tipo} y GET /api/transacciones/categoria/{nombre}
  onFiltroTipoCatSubmit(): void {
    this.limpiarMensajes();
    const { tipo, categoriaTexto } = this.filtroTipoCatForm.value;

    this.isLoading = true;
    this.cdr.detectChanges();

    if (categoriaTexto && categoriaTexto.trim() !== '') {
      this.transaccionService.filtrarPorCategoria(categoriaTexto.trim()).subscribe({
        next: (data) => {
          if (tipo && tipo !== 'TODOS') {
            this.transacciones = data.filter(t => t.tipo.toUpperCase() === tipo);
          } else {
            this.transacciones = data;
          }
          this.isLoading = false;
          this.successMessage = `Filtro por categoría "${categoriaTexto}" aplicado: ${this.transacciones.length} resultado(s).`;
          this.cdr.detectChanges();
        },
        error: (err) => {
          this.errorMessage = 'Error al filtrar por categoría: ' + (err.error || err.message);
          this.isLoading = false;
          this.cdr.detectChanges();
        }
      });
    } else if (tipo && tipo !== 'TODOS') {
      this.transaccionService.filtrarPorTipo(tipo).subscribe({
        next: (data) => {
          this.transacciones = data;
          this.isLoading = false;
          this.successMessage = `Filtro por tipo ${tipo} aplicado: ${data.length} resultado(s).`;
          this.cdr.detectChanges();
        },
        error: (err) => {
          this.errorMessage = 'Error al filtrar por tipo: ' + (err.error || err.message);
          this.isLoading = false;
          this.cdr.detectChanges();
        }
      });
    } else {
      this.cargarTransacciones();
    }
  }

  limpiarFiltros(): void {
    this.filtroTipoCatForm.reset({ tipo: 'TODOS', categoriaTexto: '' });
    this.filtroRango = { desde: this.obtenerPrimerDiaMes(), hasta: this.obtenerFechaHoy() };
    this.limpiarMensajes();
    this.cargarTransacciones();
  }

  // DELETE /api/transacciones/{id}
  eliminarTransaccion(tx: Transaccion): void {
    if (!tx.id) return;
    if (!confirm(`¿Eliminar la transacción "${tx.descripcion}" por S/ ${tx.monto}?`)) {
      return;
    }

    this.limpiarMensajes();
    this.transaccionService.eliminar(tx.id).subscribe({
      next: () => {
        this.successMessage = `Transacción eliminada con éxito.`;
        this.cargarTransacciones();
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.errorMessage = 'Error al eliminar transacción: ' + (err.error || err.message);
        this.cdr.detectChanges();
      }
    });
  }

  private limpiarMensajes(): void {
    this.successMessage = '';
    this.errorMessage = '';
  }
}
