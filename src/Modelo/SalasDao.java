package Modelo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SalasDao {

    // Connection con;
    Conexion cn = new Conexion();
    // PreparedStatement ps;
    // ResultSet rs;

    public boolean RegistrarSala(Salas sl) {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        String sql = "INSERT INTO salas(nombre, mesas) VALUES (?,?)";

        boolean var4;
        try {
            con = this.cn.getConnection();
            ps = con.prepareStatement(sql);
            ps.setString(1, sl.getNombre());
            ps.setInt(2, sl.getMesas());
            ps.execute();
            boolean var3 = true;
            return var3;
        } catch (SQLException var14) {
            System.out.println(var14.toString());
            var4 = false;
        } finally {
            try {
                con.close();
            } catch (SQLException var13) {
                System.out.println(var13.toString());
            }

        }

        return var4;
    }

    public List Listar() {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List Lista = new ArrayList();
        String sql = "SELECT * FROM salas";

        try {
            con = this.cn.getConnection();
            ps = con.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                Salas sl = new Salas();
                sl.setId(rs.getInt("id"));
                sl.setNombre(rs.getString("nombre"));
                sl.setMesas(rs.getInt("mesas"));
                Lista.add(sl);
            }
        } catch (SQLException var4) {
            System.out.println(var4.toString());
        }

        return Lista;
    }

    public boolean Eliminar(int id) {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        String sql = "DELETE FROM salas WHERE id = ? ";

        boolean var4;
        try {
            con = this.cn.getConnection();
            ps = con.prepareStatement(sql);
            ps.setInt(1, id);
            ps.execute();
            boolean var3 = true;
            return var3;
        } catch (SQLException var14) {
            System.out.println(var14.toString());
            var4 = false;
        } finally {
            try {
                con.close();
            } catch (SQLException var13) {
                System.out.println(var13.toString());
            }

        }

        return var4;
    }

    public boolean Modificar(Salas sl) {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        String sql = "UPDATE salas SET nombre=?, mesas=? WHERE id=?";

        boolean var4;
        try {
            con = this.cn.getConnection();
            ps = con.prepareStatement(sql);
            ps.setString(1, sl.getNombre());
            ps.setInt(2, sl.getMesas());
            ps.setInt(3, sl.getId());
            ps.execute();
            boolean var3 = true;
            return var3;
        } catch (SQLException var14) {
            System.out.println(var14.toString());
            var4 = false;
        } finally {
            try {
                con.close();
            } catch (SQLException var13) {
                System.out.println(var13.toString());
            }

        }

        return var4;
    }

    // En SalasDao.java
    public int buscarIdSalaPorNombre(String nombre) {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        String sql = "SELECT id FROM salas WHERE nombre = ?";
        int idSala = 0;
        try {
            con = cn.getConnection();
            ps = con.prepareStatement(sql);
            ps.setString(1, nombre);
            rs = ps.executeQuery();
            if (rs.next()) {
                idSala = rs.getInt("id");
            }
        } catch (SQLException e) {
            System.out.println(e.toString());
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
                System.out.println(e.toString());
            }
        }
        return idSala;
    }

}
