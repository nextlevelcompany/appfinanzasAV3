import { Component, inject, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { CuentaService } from '../../services/cuenta.service';
import { TransaccionService } from '../../services/transaccion.service';
import { AuthService } from '../../services/auth.service';
import { ResumenFinanciero } from '../../models/resumen.model';
import { Cuenta } from '../../models/cuenta.model';
import { Transaccion } from '../../models/transaccion.model';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css'
})
export class DashboardComponent implements OnInit {
  readonly cuentaService = inject(CuentaService);
  readonly transaccionService = inject(TransaccionService);
  readonly authService = inject(AuthService);
  private readonly cdr = inject(ChangeDetectorRef);

  resumen?: ResumenFinanciero;
  cuentas: Cuenta[] = [];
  ultimasTransacciones: Transaccion[] = [];
  isLoading = true;
  errorMessage = '';

  ngOnInit(): void {
    this.cargarDatos();
  }

  cargarDatos(): void {
    this.isLoading = true;
    this.errorMessage = '';
    this.cdr.detectChanges();

    // Cargar Resumen Financiero (GET /api/cuentas/resumen)
    this.cuentaService.obtenerResumen().subscribe({
      next: (res) => {
        this.resumen = res;
        this.cdr.detectChanges();
      },
      error: (err) => {
        if (err.status === 401) {
          this.errorMessage = 'Debe iniciar sesión para ver los datos financieros protegidos.';
        } else {
          this.errorMessage = 'Error al cargar el resumen financiero.';
        }
        this.cdr.detectChanges();
      }
    });

    // Cargar Cuentas (GET /api/cuentas)
    this.cuentaService.listar().subscribe({
      next: (lista) => {
        this.cuentas = lista;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Error cargando cuentas', err);
      }
    });

    // Cargar Transacciones (GET /api/transacciones)
    this.transaccionService.listar().subscribe({
      next: (lista) => {
        this.ultimasTransacciones = lista.slice(-5).reverse();
        this.isLoading = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Error cargando transacciones', err);
        this.isLoading = false;
        this.cdr.detectChanges();
      }
    });
  }

  getSaldoTotal(): number {
    return this.cuentas.reduce((sum, c) => sum + (c.saldo || 0), 0);
  }
}
