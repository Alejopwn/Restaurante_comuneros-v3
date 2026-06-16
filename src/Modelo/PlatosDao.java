package Modelo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PlatosDao {

    // Connection con;
    Conexion cn = new Conexion();
    // PreparedStatement ps;
    // ResultSet rs;

    public boolean Registrar(Platos pla) {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        String sql = "INSERT INTO platos (nombre, precio, fecha) VALUES (?,?,?)";
        try {
            con = cn.getConnection();
            ps = con.prepareStatement(sql);
            ps.setString(1, pla.getNombre());
            ps.setDouble(2, pla.getPrecio());
            ps.setString(3, pla.getFecha());
            ps.execute();
            return true;
        } catch (SQLException e) {
            System.out.println(e.toString());
            return false;
        } finally {
            try {
                con.close();
            } catch (SQLException ex) {
                System.out.println(ex.toString());
            }
        }
    }

    public List<Platos> Listar(String valor) {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<Platos> Lista = new ArrayList();
        String sql = "SELECT * FROM platos";
        String consulta = "SELECT * FROM platos WHERE nombre LIKE ? OR CAST(id AS TEXT) LIKE ?";
        try {
            con = cn.getConnection();
            if (valor.equalsIgnoreCase("")) {
                ps = con.prepareStatement(sql);
            } else {
                ps = con.prepareStatement(consulta);
                ps.setString(1, "%" + valor + "%"); // Búsqueda por nombre
                ps.setString(2, "%" + valor + "%"); // Búsqueda por ID
            }
            rs = ps.executeQuery();
            while (rs.next()) {
                Platos pl = new Platos();
                pl.setId(rs.getInt("id"));
                pl.setNombre(rs.getString("nombre"));
                pl.setPrecio(rs.getDouble("precio"));
                Lista.add(pl);
            }
        } catch (SQLException e) {
            System.out.println("Error al listar platos: " + e.toString());
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (ps != null) {
                    ps.close();
                }
                if (con != null) {
                    con.close();
                }
            } catch (SQLException e) {
                System.out.println("Error al cerrar recursos: " + e.toString());
            }
        }
        return Lista;
    }

    public boolean Eliminar(int id) {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        String sql = "DELETE FROM platos WHERE id = ?";

        try {
            con = cn.getConnection();
            if (con == null) {
                System.out.println("Error: No se pudo establecer la conexión a la base de datos");
                return false;
            }
            ps = con.prepareStatement(sql);
            ps.setInt(1, id);
            int filasAfectadas = ps.executeUpdate(); // Usar executeUpdate para contar filas afectadas
            if (filasAfectadas > 0) {
                System.out.println("Plato con ID " + id + " eliminado correctamente");
                return true;
            } else {
                System.out.println("No se encontró plato con ID " + id);
                return false;
            }
        } catch (SQLException e) {
            System.out.println("Error al eliminar plato con ID " + id + ": " + e.getMessage());
            return false;
        } finally {
            try {
                if (ps != null) {
                    ps.close();
                }
                if (con != null) {
                    con.close();
                }
            } catch (SQLException ex) {
                System.out.println("Error al cerrar recursos: " + ex.getMessage());
            }
        }
    }

    public boolean Modificar(Platos pla) {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        String sql = "UPDATE platos SET nombre=?, precio=? WHERE id=?";
        try {
            con = cn.getConnection(); // Inicializar la conexión
            ps = con.prepareStatement(sql);
            ps.setString(1, pla.getNombre());
            ps.setDouble(2, pla.getPrecio());
            ps.setInt(3, pla.getId());
            System.out.println("Intentando modificar plato: ID=" + pla.getId() + ", Nombre=" + pla.getNombre() + ", Precio=" + pla.getPrecio());
            int filasAfectadas = ps.executeUpdate(); // Usar executeUpdate para contar filas afectadas
            if (filasAfectadas > 0) {
                System.out.println("Plato modificado exitosamente: " + filasAfectadas + " fila(s) afectada(s)");
                return true;
            } else {
                System.out.println("No se encontró plato con ID: " + pla.getId());
                return false;
            }
        } catch (SQLException e) {
            System.out.println("Error al modificar plato: " + e.toString());
            return false;
        } finally {
            try {
                if (ps != null) {
                    ps.close();
                }
                if (con != null) {
                    con.close();
                }
            } catch (SQLException ex) {
                System.out.println("Error al cerrar conexión: " + ex.toString());
            }
        }
    }

    public Platos buscarPorId(int id) {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        Platos pl = null;
        String sql = "SELECT * FROM platos WHERE id = ?";
        try {
            con = cn.getConnection();
            ps = con.prepareStatement(sql);
            ps.setInt(1, id);
            rs = ps.executeQuery();
            if (rs.next()) {
                pl = new Platos();
                pl.setId(rs.getInt("id"));
                pl.setNombre(rs.getString("nombre"));
                pl.setPrecio(rs.getDouble("precio"));
            }
        } catch (SQLException e) {
            System.out.println(e.toString());
        } finally {
            try {
                con.close();
            } catch (SQLException ex) {
                System.out.println(ex.toString());
            }
        }
        return pl;
    }

}
