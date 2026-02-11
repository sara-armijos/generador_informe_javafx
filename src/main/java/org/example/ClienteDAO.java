package org.example;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Clase encargada de gestionar el acceso a los datos de clientes.
 * <p>
 * Los datos se obtienen a partir de un archivo CSV que puede cargarse
 * desde los recursos de la aplicación o seleccionarse mediante la interfaz.
 *
 * @author Sara
 */
public class ClienteDAO {

    private File csvSeleccionado;

    public void setCsvSeleccionado(File file) {
        this.csvSeleccionado = file;
    }

    /**
     * Obtiene todos los clientes disponibles en el archivo CSV.
     *
     * @return lista de clientes leídos desde el CSV
     * @throws RuntimeException si el archivo no puede leerse
     */
    public List<Cliente> obtenerTodos() {
        return leerCSV();
    }

    public List<Cliente> filtrar(List<Cliente> base, String nombreContiene, String ciudad) {
        String texto = nombreContiene == null ? "" : nombreContiene.trim().toLowerCase();
        String ciudadFiltro = ciudad == null ? "Todas" : ciudad.trim();

        return base.stream()
                .filter(c -> texto.isEmpty() || c.getNombre().toLowerCase().contains(texto))
                .filter(c -> ciudadFiltro.equalsIgnoreCase("Todas") || c.getCiudad().equalsIgnoreCase(ciudadFiltro))
                .collect(Collectors.toList());
    }

    public Set<String> ciudadesDisponibles(List<Cliente> base) {
        return base.stream()
                .map(Cliente::getCiudad)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(TreeSet::new));
    }

    public Map<String, Integer> contarPorCiudad(List<Cliente> base) {
        Map<String, Integer> totales = new TreeMap<>();
        for (Cliente c : base) {
            totales.put(c.getCiudad(), totales.getOrDefault(c.getCiudad(), 0) + 1);
        }
        return totales;
    }

    private List<Cliente> leerCSV() {
        List<Cliente> clientes = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(abrirInputStreamCSV(), StandardCharsets.UTF_8))) {
            String linea;
            boolean primera = true;

            while ((linea = br.readLine()) != null) {
                if (primera) { primera = false; continue; }
                if (linea.isBlank()) continue;

                String[] campos = linea.split(",");
                int id = Integer.parseInt(campos[0].trim());
                String nombre = campos[1].trim();
                String email = campos[2].trim();
                String ciudad = campos[3].trim();

                clientes.add(new Cliente(id, nombre, email, ciudad));
            }
        } catch (Exception e) {
            throw new RuntimeException("Error leyendo el CSV. Revisa formato y ruta.", e);
        }

        return clientes;
    }

    private InputStream abrirInputStreamCSV() throws IOException {
        if (csvSeleccionado != null) {
            return new FileInputStream(csvSeleccionado);
        }
        InputStream is = getClass().getResourceAsStream("/clientes.csv");
        if (is == null) {
            throw new FileNotFoundException("No se encontró /clientes.csv en resources. Selecciona un CSV con el botón.");
        }
        return is;
    }
}
