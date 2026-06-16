package Modelo;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Proveedor de conexiones SQLite directo.
 *
 * No usamos pool de conexiones (HikariCP) porque SQLite es una base de datos
 * de archivo de usuario único. Cada llamada a getConnection() abre una conexión
 * nueva que el DAO debe cerrar en su bloque finally.
 * WAL mode + busy_timeout gestionan la concurrencia internamente.
 */
public class Conexion {

    private static final String DB_PATH;

    static {
        DB_PATH = new File("restaurante.db").getAbsolutePath();
        // Registrar el driver (necesario en algunos entornos)
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.err.println("❌ Driver SQLite no encontrado: " + e.getMessage());
        }
    }

    /**
     * Crea y retorna una nueva conexión SQLite configurada.
     * El llamador ES RESPONSABLE de cerrarla en un bloque finally.
     */
    public Connection getConnection() {
        try {
            // Parámetros de conexión directamente en la URL
            String url = "jdbc:sqlite:" + DB_PATH
                    + "?journal_mode=WAL"
                    + "&busy_timeout=5000"
                    + "&foreign_keys=on"
                    + "&synchronous=NORMAL";
            return DriverManager.getConnection(url);
        } catch (SQLException e) {
            System.out.println("❌ Error al conectar con SQLite: " + e.getMessage());
            return null;
        }
    }

    /**
     * Método de compatibilidad: no-op ya que no hay pool que cerrar.
     */
    public static void cerrarPool() {
        System.out.println("✅ Aplicación cerrada correctamente.");
    }
}
