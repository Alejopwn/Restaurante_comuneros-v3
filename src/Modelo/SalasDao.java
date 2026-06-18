package Modelo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SalasDao {

    Conexion cn = new Conexion();

    public boolean RegistrarSala(Salas sl) {
        String sql = "INSERT INTO salas(nombre, mesas) VALUES (?,?)";
        try (Connection con = cn.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, sl.getNombre());
            ps.setInt(2, sl.getMesas());
            ps.execute();
            return true;
        } catch (SQLException e) {
            System.out.println("Error al registrar sala: " + e.toString());
            return false;
        }
    }

    public List<Salas> Listar() {
        List<Salas> Lista = new ArrayList<>();
        String sql = "SELECT * FROM salas";
        try (Connection con = cn.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Salas sl = new Salas();
                sl.setId(rs.getInt("id"));
                sl.setNombre(rs.getString("nombre"));
                sl.setMesas(rs.getInt("mesas"));
                Lista.add(sl);
            }
        } catch (SQLException e) {
            System.out.println("Error al listar salas: " + e.toString());
        }
        return Lista;
    }

    public boolean Eliminar(int id) {
        String sql = "DELETE FROM salas WHERE id = ? ";
        try (Connection con = cn.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.execute();
            return true;
        } catch (SQLException e) {
            System.out.println("Error al eliminar sala: " + e.toString());
            return false;
        }
    }

    public boolean Modificar(Salas sl) {
        String sql = "UPDATE salas SET nombre=?, mesas=? WHERE id=?";
        try (Connection con = cn.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, sl.getNombre());
            ps.setInt(2, sl.getMesas());
            ps.setInt(3, sl.getId());
            ps.execute();
            return true;
        } catch (SQLException e) {
            System.out.println("Error al modificar sala: " + e.toString());
            return false;
        }
    }

    public int buscarIdSalaPorNombre(String nombre) {
        String sql = "SELECT id FROM salas WHERE nombre = ?";
        try (Connection con = cn.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nombre);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        } catch (SQLException e) {
            System.out.println("Error al buscar id de sala por nombre: " + e.toString());
        }
        return 0;
    }
}
