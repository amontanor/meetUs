package dotidapp.meetus;

import android.graphics.Bitmap;

public class Usuarios {
	String nombre;
	String id;
	Bitmap foto;
	String estado;
	
	public String getEstado() {
		return estado;
	}
	public void setEstado(String estado) {
		this.estado = estado;
	}
	public String getNombre() {
		return nombre;
	}
	public void setNombre(String nombre) {
		this.nombre = nombre;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public Bitmap getFoto() {
		return foto;
	}
	public void setFoto(Bitmap foto) {
		this.foto = foto;
	}
	public Usuarios(String nombre, String id, Bitmap foto) {
		super();
		this.nombre = nombre;
		this.id = id;
		this.foto = foto;
		this.estado = "offline";
	}
	public Usuarios(String nombre, String id) {
		super();
		this.nombre = nombre;
		this.id = id;
	}
	
	
	
}