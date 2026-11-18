import { Component, inject, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, NgForm, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule],
  templateUrl: './login.html',
  styleUrl: './login.css'
})
export class LoginComponent implements OnInit {
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  private readonly fb = inject(FormBuilder);
  private readonly cdr = inject(ChangeDetectorRef);

  // Estados de mensajes
  loginError = '';
  loginSuccess = '';
  registerError = '';
  registerSuccess = '';
  isLoadingLogin = false;
  isLoadingRegister = false;

  // Modelo para Formulario 1 (Template-driven Form)
  loginData = {
    username: '',
    password: ''
  };

  // Formulario 2 (Reactive Form)
  registroForm!: FormGroup;

  ngOnInit(): void {
    this.registroForm = this.fb.group({
      username: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(80)]],
      password: ['', [Validators.required, Validators.minLength(8)]],
      confirmPassword: ['', [Validators.required]]
    }, { validators: this.passwordsCoinciden });
  }

  // Validador personalizado para confirmación de contraseña
  passwordsCoinciden(form: FormGroup) {
    const password = form.get('password')?.value;
    const confirm = form.get('confirmPassword')?.value;
    return password === confirm ? null : { passwordMismatch: true };
  }

  // Formulario 1: Template-driven submit (POST /auth/login)
  onLoginSubmit(form: NgForm): void {
    if (form.invalid) {
      this.loginError = 'Por favor complete todos los campos requeridos correctamente.';
      this.cdr.detectChanges();
      return;
    }

    this.loginError = '';
    this.loginSuccess = '';
    this.isLoadingLogin = true;
    this.cdr.detectChanges();

    this.authService.login({
      username: this.loginData.username.trim(),
      password: this.loginData.password
    }).subscribe({
      next: (res) => {
        this.isLoadingLogin = false;
        this.loginSuccess = `¡Bienvenido, ${res.username}! Redirigiendo...`;
        this.cdr.detectChanges();
        // Redirección inmediata sin esperas innecesarias
        this.router.navigate(['/dashboard']);
      },
      error: (err) => {
        this.isLoadingLogin = false;
        if (err.status === 401) {
          this.loginError = 'Credenciales inválidas. Verifique su usuario y contraseña.';
        } else {
          this.loginError = 'Error al comunicarse con el servidor (' + (err.error || err.message) + ')';
        }
        this.cdr.detectChanges();
      }
    });
  }

  // Formulario 2: Reactive submit (POST /auth/register)
  onRegisterSubmit(): void {
    if (this.registroForm.invalid) {
      this.registerError = 'Por favor revise los campos del formulario de registro.';
      this.registroForm.markAllAsTouched();
      this.cdr.detectChanges();
      return;
    }

    this.registerError = '';
    this.registerSuccess = '';
    this.isLoadingRegister = true;
    this.cdr.detectChanges();

    const values = this.registroForm.value;
    this.authService.register({
      username: values.username.trim(),
      password: values.password
    }).subscribe({
      next: (msg) => {
        this.isLoadingRegister = false;
        this.registerSuccess = 'Usuario registrado con éxito. Ahora puede iniciar sesión.';
        this.loginData.username = values.username;
        this.registroForm.reset();
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.isLoadingRegister = false;
        if (err.status === 409) {
          this.registerError = 'El usuario ya se encuentra registrado.';
        } else {
          this.registerError = err.error || 'Ocurrió un error en el registro.';
        }
        this.cdr.detectChanges();
      }
    });
  }
}
