package com.jc.appfinanzas;

public class ResumenFinanciero {

	private String periodo;
	private double totalIngresos;
	private double totalGastos;
	private double balanceNeto;
	private String estado; // "SUPERAVIT", "DEFICIT", "EQUILIBRADO"

	public ResumenFinanciero() {
	}

	public ResumenFinanciero(String periodo, double totalIngresos, double totalGastos, double balanceNeto, String estado) {
		this.periodo = periodo;
		this.totalIngresos = totalIngresos;
		this.totalGastos = totalGastos;
		this.balanceNeto = balanceNeto;
		this.estado = estado;
	}

	public String getPeriodo() {
		return periodo;
	}

	public void setPeriodo(String periodo) {
		this.periodo = periodo;
	}

	public double getTotalIngresos() {
		return totalIngresos;
	}

	public void setTotalIngresos(double totalIngresos) {
		this.totalIngresos = totalIngresos;
	}

	public double getTotalGastos() {
		return totalGastos;
	}

	public void setTotalGastos(double totalGastos) {
		this.totalGastos = totalGastos;
	}

	public double getBalanceNeto() {
		return balanceNeto;
	}

	public void setBalanceNeto(double balanceNeto) {
		this.balanceNeto = balanceNeto;
	}

	public String getEstado() {
		return estado;
	}

	public void setEstado(String estado) {
		this.estado = estado;
	}
}
