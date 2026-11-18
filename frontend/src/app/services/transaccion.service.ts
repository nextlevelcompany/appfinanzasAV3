import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Transaccion } from '../models/transaccion.model';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class TransaccionService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = environment.apiUrl + '/api/transacciones';

  // 9. GET /api/transacciones
  listar(): Observable<Transaccion[]> {
    return this.http.get<Transaccion[]>(this.baseUrl);
  }

  // 10. GET /api/transacciones/{id}
  obtenerPorId(id: number): Observable<Transaccion> {
    return this.http.get<Transaccion>(`${this.baseUrl}/${id}`);
  }

  // 11. POST /api/transacciones
  crear(transaccion: Transaccion): Observable<Transaccion> {
    return this.http.post<Transaccion>(this.baseUrl, transaccion);
  }

  // 12. PUT /api/transacciones/{id}
  actualizar(id: number, transaccion: Transaccion): Observable<Transaccion> {
    return this.http.put<Transaccion>(`${this.baseUrl}/${id}`, transaccion);
  }

  // 13. DELETE /api/transacciones/{id}
  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }

  // 14. GET /api/transacciones/tipo/{tipo}
  filtrarPorTipo(tipo: string): Observable<Transaccion[]> {
    return this.http.get<Transaccion[]>(`${this.baseUrl}/tipo/${encodeURIComponent(tipo)}`);
  }

  // 15. GET /api/transacciones/categoria/{nombre}
  filtrarPorCategoria(nombre: string): Observable<Transaccion[]> {
    return this.http.get<Transaccion[]>(`${this.baseUrl}/categoria/${encodeURIComponent(nombre)}`);
  }

  // 16. GET /api/transacciones/rango?desde=...&hasta=...
  filtrarPorRango(desde: string, hasta: string): Observable<Transaccion[]> {
    const params = new HttpParams().set('desde', desde).set('hasta', hasta);
    return this.http.get<Transaccion[]>(`${this.baseUrl}/rango`, { params });
  }
}
