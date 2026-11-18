import { Routes } from '@angular/router';
import { DashboardComponent } from './components/dashboard/dashboard';
import { CuentasComponent } from './components/cuentas/cuentas';
import { TransaccionesComponent } from './components/transacciones/transacciones';
import { CategoriasComponent } from './components/categorias/categorias';
import { LoginComponent } from './components/login/login';
import { OperacionesRestComponent } from './components/operaciones-rest/operaciones-rest';
import { NoEncontrado } from './no-encontrado/no-encontrado';

export const routes: Routes = [
  {
    path: '',
    redirectTo: 'dashboard',
    pathMatch: 'full'
  },
  {
    path: 'dashboard',
    component: DashboardComponent,
    title: 'AppFinanzas - Resumen Financiero'
  },
  {
    path: 'cuentas',
    component: CuentasComponent,
    title: 'AppFinanzas - Gestión de Cuentas'
  },
  {
    path: 'transacciones',
    component: TransaccionesComponent,
    title: 'AppFinanzas - Gestión de Transacciones'
  },
  {
    path: 'categorias',
    component: CategoriasComponent,
    title: 'AppFinanzas - Gestión de Categorías'
  },
  {
    path: 'login',
    component: LoginComponent,
    title: 'AppFinanzas - Iniciar Sesión / Registro'
  },
  {
    path: 'operaciones-rest',
    component: OperacionesRestComponent,
    title: 'AppFinanzas - Matriz REST y Formularios (APF3)'
  },
  {
    path: '**',
    component: NoEncontrado,
    title: 'Página no encontrada'
  }
];
