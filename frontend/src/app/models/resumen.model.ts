export interface ResumenFinanciero {
  periodo: string;
  totalIngresos: number;
  totalGastos: number;
  balanceNeto: number;
  estado: string; // 'SUPERAVIT' | 'DEFICIT' | 'EQUILIBRADO'
}
