export interface Transaccion {
  id?: number;
  descripcion: string;
  monto: number;
  tipo: 'INGRESO' | 'GASTO' | string;
  categoria?: string;
  fecha: string; // YYYY-MM-DD
}
