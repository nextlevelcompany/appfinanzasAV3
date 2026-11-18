export interface Categoria {
  id?: number;
  nombre: string;
  tipo: 'INGRESO' | 'GASTO' | string;
}
