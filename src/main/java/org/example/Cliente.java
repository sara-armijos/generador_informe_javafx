package org.example;

public class Cliente {

        private final int id;
        private final String nombre;
        private final String email;
        private final String ciudad;

    public Cliente(int id, String nombre, String email, String ciudad) {
        this.id = id;
        this.nombre = nombre;
        this.email = email;
        this.ciudad = ciudad;
    }

        public int getId() { return id; }
        public String getNombre() { return nombre; }
        public String getEmail() { return email; }
        public String getCiudad() { return ciudad; }
}

