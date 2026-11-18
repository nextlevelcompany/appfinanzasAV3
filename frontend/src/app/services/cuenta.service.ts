import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Cuenta } from '../models/cuenta.model';
import { Categoria } from '../models/categoria.model';
import { ResumenFinanciero } from '../models/resumen.model';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class CuentaService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = environment.apiUrl + '/api/cuentas';

  // 1. GET /api/cuentas
  listar(): Observable<Cuenta[]> {
    return this.http.get<Cuenta[]>(this.baseUrl);
  }

  // 2. GET /api/cuentas/{id}
  obtenerPorId(id: number): Observable<Cuenta> {
    return this.http.get<Cuenta>(`${this.baseUrl}/${id}`);
  }

  // 3. POST /api/cuentas
  crear(cuenta: Cuenta): Observable<Cuenta> {
    return this.http.post<Cuenta>(this.baseUrl, cuenta);
  }

  // 4. PUT /api/cuentas/{id}
  actualizar(id: number, cuenta: Cuenta): Observable<Cuenta> {
    return this.http.put<Cuenta>(`${this.baseUrl}/${id}`, cuenta);
  }

  // 5. DELETE /api/cuentas/{id}
  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }

  // 6. POST /api/cuentas/categorias
  crearCategoria(categoria: Categoria): Observable<Categoria> {
    return this.http.post<Categoria>(`${this.baseUrl}/categorias`, categoria);
  }

  // 7. GET /api/cuentas/categorias
  listarCategorias(): Observable<Categoria[]> {
    return this.http.get<Categoria[]>(`${this.baseUrl}/categorias`);
  }

  // 8. GET /api/cuentas/resumen
  obtenerResumen(): Observable<ResumenFinanciero> {
    return this.http.get<ResumenFinanciero>(`${this.baseUrl}/resumen`);
  }
}
