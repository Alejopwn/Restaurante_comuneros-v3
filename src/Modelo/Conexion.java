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
        // Verificar y migrar base de datos
        inicializarBaseDatos();
    }

    private static void inicializarBaseDatos() {
        String url = "jdbc:sqlite:" + DB_PATH;
        try (Connection conn = DriverManager.getConnection(url)) {
            // Verificar columnas de pedidos
            if (!columnaExiste(conn, "pedidos", "pago_efectivo")) {
                try (java.sql.Statement stmt = conn.createStatement()) {
                    stmt.execute("ALTER TABLE pedidos ADD COLUMN pago_efectivo REAL DEFAULT 0.0");
                    System.out.println("✅ Columna 'pago_efectivo' agregada a la tabla 'pedidos'.");
                }
            }
            if (!columnaExiste(conn, "pedidos", "pago_transaccion")) {
                try (java.sql.Statement stmt = conn.createStatement()) {
                    stmt.execute("ALTER TABLE pedidos ADD COLUMN pago_transaccion REAL DEFAULT 0.0");
                    System.out.println("✅ Columna 'pago_transaccion' agregada a la tabla 'pedidos'.");
                }
            }
            // Verificar columnas de detalle_pedidos
            if (!columnaExiste(conn, "detalle_pedidos", "comentario")) {
                try (java.sql.Statement stmt = conn.createStatement()) {
                    stmt.execute("ALTER TABLE detalle_pedidos ADD COLUMN comentario TEXT DEFAULT ''");
                    System.out.println("✅ Columna 'comentario' agregada a la tabla 'detalle_pedidos'.");
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Error al inicializar/migrar la base de datos: " + e.getMessage());
        }
    }

    private static boolean columnaExiste(Connection conn, String tabla, String columna) {
        String sql = "PRAGMA table_info(" + tabla + ")";
        try (java.sql.Statement stmt = conn.createStatement();
             java.sql.ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                if (columna.equalsIgnoreCase(rs.getString("name"))) {
                    return true;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al verificar columna " + columna + " en " + tabla + ": " + e.getMessage());
        }
        return false;
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
