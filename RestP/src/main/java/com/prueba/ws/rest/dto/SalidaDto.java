package com.prueba.ws.rest.dto;

/**
 * DTO para guardar toda la informacion resultante
 * @author ARCM
 */
public class SalidaDto {

	/** NOMBRE DEL ASTEROIDE */
	private String name;
	/** DIAMETRO DEL ASTEROIDE */
	private String diameter;
	/** VELOCIDAD DEL ASTEROIDE */
	private String speed;
	/** FECHA DEL ASTEROIDE */
	private String date;
	/** PLANETA DEL ASTEROIDE */
	private String planet;
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getDiameter() {
		return diameter;
	}
	
	public void setDiameter(String diameter) {
		this.diameter = diameter;
	}
	
	public String getSpeed() {
		return speed;
	}
	
	public void setSpeed(String speed) {
		this.speed = speed;
	}
	
	public String getDate() {
		return date;
	}
	
	public void setDate(String date) {
		this.date = date;
	}
	
	public String getPlanet() {
		return planet;
	}
	
	public void setPlanet(String planet) {
		this.planet = planet;
	}
	
	/**
	 * Constructor
	 */
	public SalidaDto() {
		super();
		this.name = "";
		this.diameter = "";
		this.speed = "";
		this.date = "";
		this.planet = "";
	}
	
}
