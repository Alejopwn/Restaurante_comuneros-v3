package Modelo;

import org.mindrot.jbcrypt.BCrypt;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class LoginDao {

    // Connection con;
    // PreparedStatement ps;
    // ResultSet rs;
    Conexion cn = new Conexion();

    public login log(String correo, String pass) {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        login l = new login();
        // Buscamos por correo y verificamos la contraseña con BCrypt
        String sql = "SELECT * FROM usuarios WHERE correo = ?";
        try {
            con = cn.getConnection();
            ps = con.prepareStatement(sql);
            ps.setString(1, correo);
            rs = ps.executeQuery();
            if (rs.next()) {
                String hashGuardado = rs.getString("pass");
                // Verificar si la contraseña es texto plano (migración) o hash BCrypt
                boolean passwordValida;
                if (hashGuardado != null && hashGuardado.startsWith("$2a$")) {
                    // Contraseña ya cifrada con BCrypt
                    passwordValida = BCrypt.checkpw(pass, hashGuardado);
                } else {
                    // Contraseña en texto plano (usuarios viejos): comparar y migrar
                    passwordValida = pass.equals(hashGuardado);
                    if (passwordValida) {
                        // Migrar automáticamente a BCrypt
                        migrarPasswordBCrypt(rs.getInt("id"), pass);
                        System.out.println("🔒 Contraseña migrada a BCrypt para usuario: " + correo);
                    }
                }
                if (passwordValida) {
                    l.setId(rs.getInt("id"));
                    l.setNombre(rs.getString("nombre"));
                    l.setCorreo(rs.getString("correo"));
                    l.setPass(rs.getString("pass"));
                    l.setRol(rs.getString("rol"));
                }
            }
        } catch (SQLException e) {
            System.out.println(e.toString());
        } finally {
            try {
                if (rs != null) rs.close();
                if (ps != null) ps.close();
                if (con != null) con.close();
            } catch (SQLException e) {
                System.out.println(e.toString());
            }
        }
        return l;
    }

    /** Migra una contraseña de texto plano a hash BCrypt en la BD */
    private void migrarPasswordBCrypt(int userId, String passPlano) {
        String hash = BCrypt.hashpw(passPlano, BCrypt.gensalt(12));
        String sql = "UPDATE usuarios SET pass = ? WHERE id = ?";
        try (Connection con = cn.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, hash);
            ps.setInt(2, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error migrando contraseña: " + e.getMessage());
        }
    }

    public boolean Registrar(login reg) {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        // Cifrar la contraseña con BCrypt antes de guardar
        String hashPassword = BCrypt.hashpw(reg.getPass(), BCrypt.gensalt(12));
        String sql = "INSERT INTO usuarios (nombre, correo, pass, rol) VALUES (?,?,?,?)";
        try {
            con = cn.getConnection();
            ps = con.prepareStatement(sql);
            ps.setString(1, reg.getNombre());
            ps.setString(2, reg.getCorreo());
            ps.setString(3, hashPassword); // Guardar el hash, no el texto plano
            ps.setString(4, reg.getRol());
            ps.execute();
            return true;
        } catch (SQLException e) {
            System.out.println(e.toString());
            return false;
        } finally {
            try {
                if (ps != null) ps.close();
                if (con != null) con.close();
            } catch (SQLException e) {
                System.out.println(e.toString());
            }
        }
    }

    public boolean eliminarUsuario(int id) {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        // Verificar si el usuario tiene pedidos asociados
        String checkQuery = "SELECT COUNT(*) FROM pedidos WHERE usuario_id = ?";
        try {
            con = cn.getConnection();
            ps = con.prepareStatement(checkQuery);
            ps.setInt(1, id);
            rs = ps.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                System.out.println("No se puede eliminar el usuario con ID " + id + " porque tiene pedidos asociados.");
                return false; // El usuario tiene pedidos
            }
        } catch (SQLException e) {
            System.out.println("Error al verificar pedidos: " + e.toString());
            return false;
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

        // Proceder con la eliminación
        String sql = "DELETE FROM usuarios WHERE id = ?";
        try {
            con = cn.getConnection();
            ps = con.prepareStatement(sql);
            ps.setInt(1, id);
            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.out.println("Error al eliminar usuario: " + e.toString());
            return false;
        } finally {
            try {
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
    }

    public List ListarUsuarios() {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<login> Lista = new ArrayList();
        String sql = "SELECT * FROM usuarios";
        try {
            con = cn.getConnection();
            ps = con.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                login lg = new login();
                lg.setId(rs.getInt("id"));
                lg.setNombre(rs.getString("nombre"));
                lg.setCorreo(rs.getString("correo"));
                lg.setRol(rs.getString("rol"));
                Lista.add(lg);
            }
        } catch (SQLException e) {
            System.out.println(e.toString());
        }
        return Lista;
    }

    public boolean ModificarDatos(Config conf) {
        Connection con = null;
        PreparedStatement ps = null;
        String sql = "UPDATE config SET ruc=?, nombre=?, telefono=?, direccion=?, mensaje=? WHERE id=?";
        try {
            con = cn.getConnection();
            ps = con.prepareStatement(sql);
            ps.setString(1, conf.getRuc());
            ps.setString(2, conf.getNombre());
            ps.setString(3, conf.getTelefono());
            ps.setString(4, conf.getDireccion());
            ps.setString(5, conf.getMensaje());
            ps.setInt(6, conf.getId());
            ps.execute();
            return true;
        } catch (SQLException e) {
            System.out.println(e.toString());
            return false;
        } finally {
            try {
                if (ps != null) ps.close();
                if (con != null) con.close();
            } catch (SQLException e) {
                System.out.println(e.toString());
            }
        }
    }

    public Config datosEmpresa() {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        Config conf = new Config();
        String sql = "SELECT * FROM config";
        try {
            con = cn.getConnection();
            ps = con.prepareStatement(sql);
            rs = ps.executeQuery();
            if (rs.next()) {
                conf.setId(rs.getInt("id"));
                conf.setRuc(rs.getString("ruc"));
                conf.setNombre(rs.getString("nombre"));
                conf.setTelefono(rs.getString("telefono"));
                conf.setDireccion(rs.getString("direccion"));
                conf.setMensaje(rs.getString("mensaje"));

            }
        } catch (SQLException e) {
            System.out.println(e.toString());
        }
        return conf;
    }

    public boolean actualizarUsuario(login lg) {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        // Si la contraseña viene sin cifrar (no inicia con $2a$), la ciframos
        String passParaGuardar = lg.getPass();
        if (passParaGuardar != null && !passParaGuardar.startsWith("$2a$")) {
            passParaGuardar = BCrypt.hashpw(passParaGuardar, BCrypt.gensalt(12));
        }
        String sql = "UPDATE usuarios SET nombre = ?, correo = ?, pass = ?, rol = ? WHERE id = ?";
        try {
            con = cn.getConnection();
            ps = con.prepareStatement(sql);
            ps.setString(1, lg.getNombre());
            ps.setString(2, lg.getCorreo());
            ps.setString(3, passParaGuardar);
            ps.setString(4, lg.getRol());
            ps.setInt(5, lg.getId());
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println(e.toString());
            return false;
        } finally {
            try {
                if (ps != null) ps.close();
                if (con != null) con.close();
            } catch (SQLException e) {
                System.out.println(e.toString());
            }
        }
    }

    public login buscarUsuarioPorId(int id) {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        login l = new login();
        String sql = "SELECT * FROM usuarios WHERE id = ?";
        try {
            con = cn.getConnection();
            ps = con.prepareStatement(sql);
            ps.setInt(1, id);
            rs = ps.executeQuery();
            if (rs.next()) {
                l.setId(rs.getInt("id"));
                l.setNombre(rs.getString("nombre"));
                l.setCorreo(rs.getString("correo"));
                l.setPass(rs.getString("pass"));
                l.setRol(rs.getString("rol"));
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
        return l.getId() == 0 ? null : l; // Devuelve null si no se encuentra el usuario
    }
}
