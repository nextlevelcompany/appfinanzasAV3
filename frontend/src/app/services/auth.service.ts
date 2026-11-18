import { Injectable, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { AuthRequest, AuthResponse } from '../models/auth.model';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = environment.apiUrl + '/auth';
  private readonly TOKEN_KEY = 'access_token';
  private readonly USER_KEY = 'current_user';

  readonly currentUser = signal<string | null>(this.obtenerUsuario());
  readonly isAuthenticated = signal<boolean>(this.estaAutenticado());

  guardarToken(token: string): void {
    if (typeof sessionStorage !== 'undefined') {
      sessionStorage.setItem(this.TOKEN_KEY, token);
    }
  }

  obtenerToken(): string | null {
    if (typeof sessionStorage !== 'undefined') {
      return sessionStorage.getItem(this.TOKEN_KEY);
    }
    return null;
  }

  guardarUsuario(usuario: string): void {
    if (typeof sessionStorage !== 'undefined') {
      sessionStorage.setItem(this.USER_KEY, usuario);
    }
    this.currentUser.set(usuario);
    this.isAuthenticated.set(true);
  }

  obtenerUsuario(): string | null {
    if (typeof sessionStorage !== 'undefined') {
      return sessionStorage.getItem(this.USER_KEY);
    }
    return null;
  }

  estaAutenticado(): boolean {
    return !!this.obtenerToken();
  }

  login(credenciales: AuthRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.baseUrl}/login`, credenciales).pipe(
      tap(res => {
        this.guardarToken(res.token);
        this.guardarUsuario(res.username);
      })
    );
  }

  register(credenciales: AuthRequest): Observable<string> {
    return this.http.post(`${this.baseUrl}/register`, credenciales, { responseType: 'text' });
  }

  logout(): void {
    if (typeof sessionStorage !== 'undefined') {
      sessionStorage.removeItem(this.TOKEN_KEY);
      sessionStorage.removeItem(this.USER_KEY);
    }
    this.currentUser.set(null);
    this.isAuthenticated.set(false);
  }
}
