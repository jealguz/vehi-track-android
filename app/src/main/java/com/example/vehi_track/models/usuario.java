package com.example.vehi_track.models;// Asegúrarnos de que coincida con el paquete de Android



//definimos la clase usuario
public class usuario {


    // Firebase usa cadenas de texto únicas para identificar a los usuarios
    private String uid;
    private String nombre;
    private String apellido;
    private String email;
    // Nota: En Firebase Auth la contraseña no se suele guardar en la base de datos
    // por seguridad, pero la dejamos si la necesitas para la lógica interna.
    private String contrasena;

    /**
     * Constructor vacío: ¡REGLA DE ORO! ya que Firebase lo necesita para convertir los datos de la nube en este objeto.
     */
    public usuario() {}

    // --- MÉTODOS GETTERS Y SETTERS ---

    

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contraseña) {
        this.contrasena = contraseña;
    }
}