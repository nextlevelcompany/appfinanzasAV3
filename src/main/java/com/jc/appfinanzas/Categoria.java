package com.jc.appfinanzas;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "categorias")
public class Categoria {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	private String nombre;
	private String tipo; // "INGRESO" o "GASTO"
	private double presupuestoLimite;
	private String colorHex;

	public Categoria() {
	}

	public Categoria(int id, String nombre, String tipo, double presupuestoLimite, String colorHex) {
		this.id = id;
		this.nombre = nombre;
		this.tipo = tipo;
		this.presupuestoLimite = presupuestoLimite;
		this.colorHex = colorHex;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getTipo() {
		return tipo;
	}

	public void setTipo(String tipo) {
		this.tipo = tipo;
	}

	public double getPresupuestoLimite() {
		return presupuestoLimite;
	}

	public void setPresupuestoLimite(double presupuestoLimite) {
		this.presupuestoLimite = presupuestoLimite;
	}

	public String getColorHex() {
		return colorHex;
	}

	public void setColorHex(String colorHex) {
		this.colorHex = colorHex;
	}
}
