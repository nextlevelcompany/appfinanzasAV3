import { Component, inject, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, NgForm } from '@angular/forms';
import { CuentaService } from '../../services/cuenta.service';
import { Categoria } from '../../models/categoria.model';

@Component({
  selector: 'app-categorias',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './categorias.html',
  styleUrl: './categorias.css'
})
export class CategoriasComponent implements OnInit {
  private readonly cuentaService = inject(CuentaService);
  private readonly cdr = inject(ChangeDetectorRef);

  categorias: Categoria[] = [];
  isLoading = false;
  isGuardando = false;
  successMessage = '';
  errorMessage = '';

  // Formulario 8: Template-driven Form
  nuevaCategoria: Categoria = {
    nombre: '',
    tipo: 'GASTO'
  };

  tipos = ['INGRESO', 'GASTO'];

  ngOnInit(): void {
    this.cargarCategorias();
  }

  // GET /api/cuentas/categorias
  cargarCategorias(): void {
    this.isLoading = true;
    this.cdr.detectChanges();
    this.cuentaService.listarCategorias().subscribe({
      next: (data) => {
        this.categorias = data;
        this.isLoading = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.errorMessage = 'Error al listar categorías: ' + (err.error || err.message);
        this.isLoading = false;
        this.cdr.detectChanges();
      }
    });
  }

  // Formulario 8: POST /api/cuentas/categorias
  onSubmit(form: NgForm): void {
    if (form.invalid) {
      this.errorMessage = 'Por favor ingrese el nombre y tipo de la categoría.';
      this.cdr.detectChanges();
      return;
    }

    this.limpiarMensajes();
    this.isGuardando = true;
    this.cdr.detectChanges();

    this.cuentaService.crearCategoria(this.nuevaCategoria).subscribe({
      next: (creada) => {
        this.isGuardando = false;
        this.successMessage = `Categoría "${creada.nombre}" registrada con éxito (ID: ${creada.id}).`;
        this.nuevaCategoria = { nombre: '', tipo: 'GASTO' };
        form.resetForm({ tipo: 'GASTO' });
        this.cargarCategorias();
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.isGuardando = false;
        this.errorMessage = 'Error al registrar categoría: ' + (err.error || err.message);
        this.cdr.detectChanges();
      }
    });
  }

  private limpiarMensajes(): void {
    this.successMessage = '';
    this.errorMessage = '';
  }
}
