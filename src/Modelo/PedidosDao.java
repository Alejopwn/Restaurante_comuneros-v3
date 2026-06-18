package Modelo;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileSystemView;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class PedidosDao {

    // Connection con;
    Conexion cn = new Conexion();
    // PreparedStatement ps;
    // ResultSet rs;
    int r;

    public int IdPedido() {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        int id = 0;
        String sql = "SELECT MAX(id) FROM pedidos";
        try {
            con = cn.getConnection();
            ps = con.prepareStatement(sql);
            rs = ps.executeQuery();
            if (rs.next()) {
                id = rs.getInt(1);
            }
        } catch (SQLException e) {
            System.out.println(e.toString());
        }
        return id;
    }

    public int verificarStado(int mesa, int id_sala) {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        int id_pedido = 0;
        String sql = "SELECT id FROM pedidos WHERE num_mesa=? AND id_sala=? AND estado = ?";
        try {
            con = cn.getConnection();
            ps = con.prepareStatement(sql);
            ps.setInt(1, mesa);
            ps.setInt(2, id_sala);
            ps.setString(3, "PENDIENTE");
            rs = ps.executeQuery();
            if (rs.next()) {
                id_pedido = rs.getInt("id");
            }
        } catch (SQLException e) {
            System.out.println(e.toString());
        }
        return id_pedido;
    }

    public double getTotalPedidosDia(String fechaInicio, String fechaFin, int idSala) {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        double total = 0.0;
        String sql = "SELECT SUM(total) AS total FROM pedidos WHERE id_sala = ? AND fecha BETWEEN ? AND ?";
        try {
            con = cn.getConnection();
            ps = con.prepareStatement(sql);
            ps.setInt(1, idSala);
            ps.setString(2, fechaInicio);
            ps.setString(3, fechaFin);
            rs = ps.executeQuery();
            if (rs.next()) {
                total = rs.getDouble("total");
                if (rs.wasNull()) {
                    total = 0.0;
                }
            }
            rs.close();
            ps.close();
            con.close();
        } catch (SQLException e) {
            System.out.println(e.toString());
            JOptionPane.showMessageDialog(null, "Error al consultar el total del día para sala ID " + idSala);
        }
        System.out.println("Total desde DAO para sala ID " + idSala + " (rango " + fechaInicio + " a " + fechaFin + "): " + total);
        return total;
    }

    public int RegistrarPedido(Pedidos ped) {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        int idGenerado = -1;
        String sql = "INSERT INTO pedidos (id_sala, num_mesa, total, usuario, fecha) VALUES (?,?,?,?,?)";
        try {
            con = cn.getConnection();
            ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS); // 👈 Esto es CLAVE
            ps.setInt(1, ped.getId_sala());
            ps.setInt(2, ped.getNum_mesa());
            ps.setDouble(3, ped.getTotal());
            ps.setString(4, ped.getUsuario());
            ps.setString(5, new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date()));
            ps.executeUpdate();

            rs = ps.getGeneratedKeys();
            if (rs.next()) {
                idGenerado = rs.getInt(1);
            }

        } catch (SQLException e) {
            System.out.println("❌ Error al registrar pedido: " + e.getMessage());
        } finally {
            try {
                if (ps != null) {
                    ps.close();
                }
                if (con != null) {
                    con.close();
                }
            } catch (SQLException e) {
                System.out.println("❌ Error cerrando conexión: " + e.getMessage());
            }
        }
        return idGenerado;
    }

    public List verPedidoDetalle(int id_pedido) {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List Lista = new ArrayList();
        String sql = "SELECT d.* FROM pedidos p INNER JOIN detalle_pedidos d ON p.id = d.id_pedido WHERE p.id = ?";
        try {
            con = cn.getConnection();
            ps = con.prepareStatement(sql);
            ps.setInt(1, id_pedido);
            rs = ps.executeQuery();
            while (rs.next()) {
                DetallePedido det = new DetallePedido();
                det.setId(rs.getInt("id"));
                det.setNombre(rs.getString("nombre"));
                det.setPrecio(rs.getDouble("precio"));
                det.setCantidad(rs.getInt("cantidad"));
                det.setComentario(rs.getString("comentario"));
                Lista.add(det);
            }
        } catch (SQLException e) {
            System.out.println(e.toString());
        }
        return Lista;
    }

    public Pedidos verPedido(int id_pedido) {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        Pedidos ped = new Pedidos();
        String sql = "SELECT p.*, s.nombre FROM pedidos p INNER JOIN salas s ON p.id_sala = s.id WHERE p.id = ?";
        try {
            con = cn.getConnection();
            ps = con.prepareStatement(sql);
            ps.setInt(1, id_pedido);
            rs = ps.executeQuery();
            if (rs.next()) {
                ped.setId(rs.getInt("id"));
                ped.setFecha(rs.getString("fecha"));
                ped.setSala(rs.getString("nombre"));
                ped.setNum_mesa(rs.getInt("num_mesa"));
                ped.setTotal(rs.getDouble("total"));
                ped.setPago_efectivo(rs.getDouble("pago_efectivo"));
                ped.setPago_transaccion(rs.getDouble("pago_transaccion"));
            }
        } catch (SQLException e) {
            System.out.println(e.toString());
        }
        return ped;
    }

    public List finalizarPedido(int id_pedido) {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List Lista = new ArrayList();
        String sql = "SELECT d.* FROM pedidos p INNER JOIN detalle_pedidos d ON p.id = d.id_pedido WHERE p.id = ?";
        try {
            con = cn.getConnection();
            ps = con.prepareStatement(sql);
            ps.setInt(1, id_pedido);
            rs = ps.executeQuery();
            while (rs.next()) {
                DetallePedido det = new DetallePedido();
                det.setId(rs.getInt("id"));
                det.setNombre(rs.getString("nombre"));
                det.setPrecio(rs.getDouble("precio"));
                det.setCantidad(rs.getInt("cantidad"));
                det.setComentario(rs.getString("comentario"));
                Lista.add(det);
            }
        } catch (SQLException e) {
            System.out.println(e.toString());
        }
        return Lista;
    }

    public void pdfPedido(int id_pedido) {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        String fechaPedido = null, usuario = null, total = null, sala = null, num_mesa = null;
        double pagoEfectivo = 0.0, pagoTransaccion = 0.0;
        try {
            // Configurar la carpeta oculta
            String appDataPath = System.getenv("APPDATA");
            if (appDataPath == null) {
                appDataPath = System.getProperty("java.io.tmpdir");
                System.out.println("⚠️ APPDATA no definido, usando directorio temporal: " + appDataPath);
            }
            String fechaStr = new SimpleDateFormat("MM-dd-yyyy").format(new java.util.Date());
            File pedidosDir = new File(appDataPath, ".pedidos_ocultos");
            File fechaDir = new File(pedidosDir, fechaStr);
            if (!fechaDir.exists()) {
                fechaDir.mkdirs();
            }
            System.out.println("📂 Carpeta de salida para pedido: " + fechaDir.getAbsolutePath());
            File salida = new File(fechaDir, "pedido_" + id_pedido + "_" + fechaStr + ".pdf");
            FileOutputStream archivo = new FileOutputStream(salida);
            Document doc = new Document();
            PdfWriter.getInstance(doc, archivo);
            doc.open();

            // Obtener datos del pedido
            String informacion = "SELECT p.*, s.nombre FROM pedidos p INNER JOIN salas s ON p.id_sala = s.id WHERE p.id = ?";
            try {
                con = cn.getConnection();
                ps = con.prepareStatement(informacion);
                ps.setInt(1, id_pedido);
                rs = ps.executeQuery();
                if (rs.next()) {
                    num_mesa = rs.getString("num_mesa");
                    sala = rs.getString("nombre");
                    fechaPedido = new SimpleDateFormat("dd/MM/yyyy HH:mm").format(rs.getTimestamp("fecha"));
                    usuario = rs.getString("usuario");
                    total = rs.getString("total");
                    pagoEfectivo = rs.getDouble("pago_efectivo");
                    pagoTransaccion = rs.getDouble("pago_transaccion");
                } else {
                    System.out.println("⚠️ No se encontró el pedido con ID: " + id_pedido);
                }
            } catch (SQLException e) {
                System.out.println("Error al obtener datos del pedido: " + e.toString());
            }

            // Encabezado del PDF
            PdfPTable Encabezado = new PdfPTable(2);
            Encabezado.setWidthPercentage(100);
            Encabezado.getDefaultCell().setBorder(0);
            float[] columnWidthsEncabezado = new float[]{50f, 50f};
            Encabezado.setWidths(columnWidthsEncabezado);
            Encabezado.setHorizontalAlignment(0);

            String config = "SELECT * FROM config";
            String mensaje = "";
            try {
                con = cn.getConnection();
                ps = con.prepareStatement(config);
                rs = ps.executeQuery();
                if (rs.next()) {
                    mensaje = rs.getString("mensaje");
                    Encabezado.addCell("NIT:    " + rs.getString("ruc") + "\nNombre: " + rs.getString("nombre")
                            + "\nTeléfono: " + rs.getString("telefono") + "\nDirección: " + rs.getString("direccion"));
                }
            } catch (SQLException e) {
                System.out.println("Error al obtener datos de config: " + e.toString());
            }

            Paragraph info = new Paragraph();
            Font negrita = new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.BOLD, BaseColor.BLUE);
            info.add("Atendido: " + usuario + "\nN° Pedido: " + id_pedido + "\nFecha: " + fechaPedido
                    + "\nSala: " + sala + "\nN° Mesa: " + num_mesa);
            Encabezado.addCell(info);
            doc.add(Encabezado);
            doc.add(Chunk.NEWLINE);

            // Tabla de productos
            PdfPTable tabla = new PdfPTable(4);
            tabla.setWidthPercentage(100);
            tabla.getDefaultCell().setBorder(0);
            float[] columnWidths = new float[]{10f, 50f, 15f, 15f};
            tabla.setWidths(columnWidths);
            tabla.setHorizontalAlignment(0);

            PdfPCell c1 = new PdfPCell(new Phrase("Cant.", negrita));
            PdfPCell c2 = new PdfPCell(new Phrase("Plato.", negrita));
            PdfPCell c3 = new PdfPCell(new Phrase("P. unt.", negrita));
            PdfPCell c4 = new PdfPCell(new Phrase("P. Total", negrita));
            c1.setBorder(0);
            c2.setBorder(0);
            c3.setBorder(0);
            c4.setBorder(0);
            c1.setBackgroundColor(BaseColor.LIGHT_GRAY);
            c2.setBackgroundColor(BaseColor.LIGHT_GRAY);
            c3.setBackgroundColor(BaseColor.LIGHT_GRAY);
            c4.setBackgroundColor(BaseColor.LIGHT_GRAY);
            tabla.addCell(c1);
            tabla.addCell(c2);
            tabla.addCell(c3);
            tabla.addCell(c4);

            String product = "SELECT d.* FROM pedidos p INNER JOIN detalle_pedidos d ON p.id = d.id_pedido WHERE p.id = ?";
            try {
                ps = con.prepareStatement(product);
                ps.setInt(1, id_pedido);
                rs = ps.executeQuery();
                while (rs.next()) {
                    double subTotal = rs.getInt("cantidad") * rs.getDouble("precio");
                    tabla.addCell(rs.getString("cantidad"));
                    tabla.addCell(rs.getString("nombre"));
                    tabla.addCell(String.format("%.2f COP", rs.getDouble("precio")));
                    tabla.addCell(String.format("%.2f COP", subTotal));
                }
            } catch (SQLException e) {
                System.out.println("Error al obtener detalles del pedido: " + e.toString());
            }

            doc.add(tabla);

            double cambio = (pagoEfectivo + pagoTransaccion) - Double.parseDouble(total);
            if (cambio < 0) cambio = 0.0;

            // Total
            Paragraph agra = new Paragraph();
            agra.add(Chunk.NEWLINE);
            agra.add(String.format("Total Consumo: %.2f COP\n", Double.parseDouble(total)));
            agra.add(String.format("Pago Efectivo: %.2f COP\n", pagoEfectivo));
            agra.add(String.format("Pago Transacción: %.2f COP\n", pagoTransaccion));
            agra.add(String.format("Cambio/Vueltos: %.2f COP\n", cambio));
            agra.setAlignment(2);
            doc.add(agra);

            // Firma
            Paragraph firma = new Paragraph();
            firma.add(Chunk.NEWLINE);
            firma.add("Cancelacion \n\n");
            firma.add("------------------------------------\n");
            firma.add("Firma \n");
            firma.setAlignment(1);
            doc.add(firma);

            // Mensaje
            Paragraph gr = new Paragraph();
            gr.add(Chunk.NEWLINE);
            gr.add(mensaje);
            gr.setAlignment(1);
            doc.add(gr);

            doc.close();
            archivo.close();

        } catch (DocumentException | IOException e) {
            System.out.println("❌ Error al generar PDF del pedido: " + e.toString());
            JOptionPane.showMessageDialog(null, "Error al generar el PDF del pedido: " + e.getMessage());
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
                System.out.println("❌ Error al cerrar recursos: " + e.toString());
            }
        }
    }

    public boolean actualizarEstado(int id_pedido) {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        String sql = "UPDATE pedidos SET estado = ? WHERE id = ?";
        try {
            con = cn.getConnection();
            ps = con.prepareStatement(sql);
            ps.setString(1, "FINALIZADO");
            ps.setInt(2, id_pedido);
            ps.execute();
            return true;
        } catch (SQLException e) {
            System.out.println(e.toString());
            return false;
        }
    }

    public boolean finalizarPedidoConPago(int id_pedido, double pago_efectivo, double pago_transaccion) {
        Connection con = null;
        PreparedStatement ps = null;
        String sql = "UPDATE pedidos SET estado = ?, pago_efectivo = ?, pago_transaccion = ? WHERE id = ?";
        try {
            con = cn.getConnection();
            ps = con.prepareStatement(sql);
            ps.setString(1, "FINALIZADO");
            ps.setDouble(2, pago_efectivo);
            ps.setDouble(3, pago_transaccion);
            ps.setInt(4, id_pedido);
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

    public List listarPedidos() {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List Lista = new ArrayList();
        String sql = "SELECT p.*, s.nombre FROM pedidos p INNER JOIN salas s ON p.id_sala = s.id ORDER BY p.fecha DESC";
        try {
            con = cn.getConnection();
            ps = con.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                Pedidos ped = new Pedidos();
                ped.setId(rs.getInt("id"));
                ped.setSala(rs.getString("nombre"));
                ped.setNum_mesa(rs.getInt("num_mesa"));
                ped.setFecha(rs.getString("fecha"));
                ped.setTotal(rs.getDouble("total"));
                ped.setUsuario(rs.getString("usuario"));
                ped.setEstado(rs.getString("estado"));
                Lista.add(ped);
            }
        } catch (SQLException e) {
            System.out.println(e.toString());
        }
        return Lista;
    }

    public boolean RegistrarDetalle(DetallePedido det) {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        String sql = "INSERT INTO detalle_pedidos (nombre, precio, cantidad, comentario, id_pedido) VALUES (?,?,?,?,?)";
        try {
            con = cn.getConnection();
            ps = con.prepareStatement(sql);
            ps.setString(1, det.getNombre());
            ps.setDouble(2, det.getPrecio());
            ps.setInt(3, det.getCantidad());
            ps.setString(4, det.getComentario());
            ps.setInt(5, det.getId_pedido());
            ps.executeUpdate();
            return actualizarTotalPedido(det.getId_pedido());
        } catch (SQLException e) {
            System.out.println("Error al registrar detalle: " + e.toString());
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
                System.out.println("Error al cerrar recursos: " + e.toString());
            }
        }
    }

    public boolean eliminarDetalle(int idDetalle) {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        String sql = "SELECT id_pedido FROM detalle_pedidos WHERE id = ?";
        try {
            con = cn.getConnection();
            ps = con.prepareStatement(sql);
            ps.setInt(1, idDetalle);
            rs = ps.executeQuery();
            int idPedido = rs.next() ? rs.getInt("id_pedido") : 0;
            rs.close();
            ps.close();
            if (idPedido != 0) {
                sql = "DELETE FROM detalle_pedidos WHERE id = ?";
                ps = con.prepareStatement(sql);
                ps.setInt(1, idDetalle);
                ps.executeUpdate();
                return actualizarTotalPedido(idPedido);
            }
            return false;
        } catch (SQLException e) {
            System.out.println("Error al eliminar detalle: " + e.toString());
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
                System.out.println("Error al cerrar recursos: " + e.toString());
            }
        }
    }

    public boolean actualizarTotalPedido(int idPedido) {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            con = cn.getConnection();
            String sql = "SELECT SUM(cantidad * precio) as total FROM detalle_pedidos WHERE id_pedido = ? AND nombre NOT IN ('PAGO EFECTIVO', 'PAGO TRANSACCION')";
            ps = con.prepareStatement(sql);
            ps.setInt(1, idPedido);
            rs = ps.executeQuery();
            double nuevoTotal = 0.0;
            if (rs.next()) {
                nuevoTotal = rs.getDouble("total") != 0 ? rs.getDouble("total") : 0.0;
            }
            rs.close();
            ps.close();
            sql = "UPDATE pedidos SET total = ? WHERE id = ?";
            ps = con.prepareStatement(sql);
            ps.setDouble(1, nuevoTotal);
            ps.setInt(2, idPedido);
            int rowsAffected = ps.executeUpdate();
            ps.close();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.out.println("Error al actualizar el total: " + e.getMessage());
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
                System.out.println("Error al cerrar recursos: " + e.toString());
            }
        }
    }

    public boolean eliminarPedidoPorId(int idPedido) {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            con = cn.getConnection();
            String sqlHistorial = "DELETE FROM historial_ventas WHERE id_pedido = ?";
            ps = con.prepareStatement(sqlHistorial);
            ps.setInt(1, idPedido);
            ps.executeUpdate();
            ps.close();
            String sqlDetalles = "DELETE FROM detalle_pedidos WHERE id_pedido = ?";
            ps = con.prepareStatement(sqlDetalles);
            ps.setInt(1, idPedido);
            ps.executeUpdate();
            ps.close();
            String sqlPedido = "DELETE FROM pedidos WHERE id = ?";
            ps = con.prepareStatement(sqlPedido);
            ps.setInt(1, idPedido);
            int rowsAffected = ps.executeUpdate();
            ps.close();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.out.println("Error al eliminar pedido: " + e.toString());
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
                System.out.println("Error al cerrar recursos: " + e.toString());
            }
        }
    }

    public void generarReporteDiario() {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            Calendar now = Calendar.getInstance();
            Calendar inicio = (Calendar) now.clone();
            Calendar fin = (Calendar) now.clone();
            if (now.get(Calendar.HOUR_OF_DAY) < 4) {
                inicio.add(Calendar.DAY_OF_MONTH, -1);
                inicio.set(Calendar.HOUR_OF_DAY, 16);
                inicio.set(Calendar.MINUTE, 0);
                inicio.set(Calendar.SECOND, 0);
                fin.set(Calendar.HOUR_OF_DAY, 4);
                fin.set(Calendar.MINUTE, 0);
                fin.set(Calendar.SECOND, 0);
            } else {
                inicio.set(Calendar.HOUR_OF_DAY, 16);
                inicio.set(Calendar.MINUTE, 0);
                inicio.set(Calendar.SECOND, 0);
                fin.add(Calendar.DAY_OF_MONTH, 1);
                fin.set(Calendar.HOUR_OF_DAY, 4);
                fin.set(Calendar.MINUTE, 0);
                fin.set(Calendar.SECOND, 0);
            }
            Timestamp fechaInicio = new Timestamp(inicio.getTimeInMillis());
            Timestamp fechaFin = new Timestamp(fin.getTimeInMillis());
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            System.out.println("🕓 Generando PDF entre:");
            System.out.println("   Desde: " + sdf.format(fechaInicio));
            System.out.println("   Hasta: " + sdf.format(fechaFin));

            double[] totales = calcularTotalesDia(fechaInicio, fechaFin, 0);
            double totalEfectivo = totales[0];
            double totalTransaccion = totales[1];
            double totalGeneral = 0.0;
            String sqlTotal = "SELECT SUM(total) AS total FROM pedidos WHERE estado = 'FINALIZADO' AND fecha BETWEEN ? AND ?";
            con = cn.getConnection();
            ps = con.prepareStatement(sqlTotal);
            ps.setString(1, new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(fechaInicio));
            ps.setString(2, new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(fechaFin));
            rs = ps.executeQuery();
            if (rs.next()) {
                totalGeneral = rs.getDouble("total");
                if (rs.wasNull()) {
                    totalGeneral = 0.0;
                }
            }
            rs.close();
            ps.close();

            String appDataPath = System.getenv("APPDATA");
            String fechaStr = new SimpleDateFormat("MM-dd-yyyy").format(now.getTime());
            File historialDir = new File(appDataPath, ".reporte_oculto");
            File fechaDir = new File(historialDir, fechaStr);
            if (!fechaDir.exists()) {
                fechaDir.mkdirs();
            }
            System.out.println("📂 Carpeta de salida: " + fechaDir.getAbsolutePath());

            File salida = new File(fechaDir, fechaStr + ".pdf");
            FileOutputStream archivo = new FileOutputStream(salida);
            Document doc = new Document();
            PdfWriter.getInstance(doc, archivo);
            doc.open();

            Font tituloFont = new Font(Font.FontFamily.TIMES_ROMAN, 16, Font.BOLD, BaseColor.BLACK);
            Paragraph titulo = new Paragraph("Historial de Pedidos del Día - Todas las Salas", tituloFont);
            titulo.setAlignment(1);
            doc.add(titulo);
            doc.add(Chunk.NEWLINE);

            PdfPTable tabla = new PdfPTable(6);
            tabla.setWidthPercentage(100);
            tabla.setWidths(new float[]{10f, 20f, 10f, 25f, 20f, 15f});
            Font headerFont = new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.BOLD, BaseColor.BLACK);
            String[] headers = {"ID", "Sala", "Mesa", "Fecha", "Usuario", "Total"};
            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(h, headerFont));
                cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                tabla.addCell(cell);
            }

            Font dataFont = new Font(Font.FontFamily.TIMES_ROMAN, 10, Font.NORMAL, BaseColor.BLACK);
            String sql = "SELECT p.id, s.nombre AS sala, p.num_mesa, p.fecha, p.usuario, p.total "
                    + "FROM pedidos p INNER JOIN salas s ON p.id_sala = s.id "
                    + "WHERE p.estado = 'FINALIZADO' AND p.fecha BETWEEN ? AND ? ORDER BY p.fecha";
            boolean hayDatos = false;
            ps = con.prepareStatement(sql);
            ps.setString(1, new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(fechaInicio));
            ps.setString(2, new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(fechaFin));
            rs = ps.executeQuery();
            while (rs.next()) {
                hayDatos = true;
                System.out.println("✅ Pedido ID: " + rs.getInt("id"));
                tabla.addCell(new Phrase(String.valueOf(rs.getInt("id")), dataFont));
                tabla.addCell(new Phrase(rs.getString("sala"), dataFont));
                tabla.addCell(new Phrase(String.valueOf(rs.getInt("num_mesa")), dataFont));
                tabla.addCell(new Phrase(rs.getString("fecha"), dataFont));
                tabla.addCell(new Phrase(rs.getString("usuario"), dataFont));
                tabla.addCell(new Phrase(String.format("%.2f COP", rs.getDouble("total")), dataFont));
            }

            if (!hayDatos) {
                System.out.println("⚠️ No se encontraron pedidos FINALIZADOS en ese rango.");
                Paragraph msg = new Paragraph("No se encontraron pedidos finalizados en este rango de tiempo.", dataFont);
                doc.add(msg);
            } else {
                doc.add(tabla);
            }

            doc.add(Chunk.NEWLINE);
            Paragraph totalesParrafo = new Paragraph();
            totalesParrafo.setFont(new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.BOLD, BaseColor.BLACK));
            totalesParrafo.add(String.format("Total Efectivo: COP %.2f\n", totalEfectivo));
            totalesParrafo.add(String.format("Total Transacción: COP %.2f\n", totalTransaccion));
            totalesParrafo.add(String.format("Total General: COP %.2f\n", totalGeneral));
            if (Math.abs((totalEfectivo + totalTransaccion) - totalGeneral) > 0.01) {
                totalesParrafo.add(new Phrase("Advertencia: La suma de efectivo y transacción no coincide con el total general.",
                        new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.NORMAL, BaseColor.RED)));
            }
            totalesParrafo.setAlignment(2);
            doc.add(totalesParrafo);

            doc.close();
            archivo.close();
            System.out.println("✅ PDF generado correctamente: " + salida.getAbsolutePath());
        } catch (DocumentException | IOException | SQLException e) {
            System.out.println("❌ Error al generar reporte diario: " + e.toString());
            JOptionPane.showMessageDialog(null, "Error al generar el reporte diario: " + e.getMessage());
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
                System.out.println("❌ Error al cerrar recursos: " + e.toString());
            }
        }
    }

    public List listarPedidosDelDia(Timestamp fechaInicio, Timestamp fechaFin) {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List lista = new ArrayList();
        String sql = "SELECT p.id, s.nombre AS sala, p.num_mesa, p.fecha, p.total, p.usuario, p.estado "
                + "FROM pedidos p INNER JOIN salas s ON p.id_sala = s.id "
                + "WHERE p.fecha BETWEEN ? AND ? AND p.estado IN ('FINALIZADO', 'PENDIENTE') ORDER BY p.fecha";
        try {
            con = cn.getConnection();
            ps = con.prepareStatement(sql);
            ps.setString(1, new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(fechaInicio));
            ps.setString(2, new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(fechaFin));
            rs = ps.executeQuery();
            while (rs.next()) {
                Pedidos ped = new Pedidos();
                ped.setId(rs.getInt("id"));
                ped.setSala(rs.getString("sala"));
                ped.setNum_mesa(rs.getInt("num_mesa"));
                ped.setFecha(rs.getString("fecha"));
                ped.setTotal(rs.getDouble("total"));
                ped.setUsuario(rs.getString("usuario"));
                ped.setEstado(rs.getString("estado"));
                lista.add(ped);
            }
        } catch (SQLException e) {
            System.out.println("Error al listar pedidos del día: " + e.toString());
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
                System.out.println("Error al cerrar conexión: " + e.toString());
            }
        }
        return lista;
    }

    public double[] calcularTotalesDia(Timestamp fechaInicio, Timestamp fechaFin, int idSala) {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        double totalEfectivo = 0.0;
        double totalTransaccion = 0.0;
        try {
            con = cn.getConnection();

            String sqlPedidos = "SELECT SUM(pago_efectivo) AS efectivo, SUM(pago_transaccion) AS transaccion FROM pedidos "
                    + "WHERE estado = 'FINALIZADO' AND fecha BETWEEN ? AND ?";
            if (idSala != 0) {
                sqlPedidos += " AND id_sala = ?";
            }
            ps = con.prepareStatement(sqlPedidos);
            ps.setString(1, new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(fechaInicio));
            ps.setString(2, new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(fechaFin));
            if (idSala != 0) {
                ps.setInt(3, idSala);
            }
            rs = ps.executeQuery();
            if (rs.next()) {
                totalEfectivo = rs.getDouble("efectivo");
                totalTransaccion = rs.getDouble("transaccion");
            }
            System.out.println("📊 Procesando totales de pedidos en el rango " + fechaInicio + " a " + fechaFin + (idSala == 0 ? " (todas las salas)" : " (sala " + idSala + ")"));
            System.out.println("   Total Efectivo: " + totalEfectivo);
            System.out.println("   Total Transacción: " + totalTransaccion);
        } catch (SQLException e) {
            System.out.println("❌ Error al calcular totales del día: " + e.getMessage());
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
                System.out.println("❌ Error al cerrar recursos: " + e.getMessage());
            }
        }
        return new double[]{totalEfectivo, totalTransaccion};
    }

    // En PedidosDao.java
    public boolean actualizarSalaPedido(int idPedido, int idSala) {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        String sql = "UPDATE pedidos SET id_sala = ? WHERE id = ?";
        try {
            con = cn.getConnection();
            ps = con.prepareStatement(sql);
            ps.setInt(1, idSala);
            ps.setInt(2, idPedido);
            ps.execute();
            return true;
        } catch (SQLException e) {
            System.out.println(e.toString());
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

    public boolean mesaOcupada(int idSala, int numMesa, int idPedidoActual) {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        String sql = "SELECT id FROM pedidos WHERE id_sala = ? AND num_mesa = ? AND estado = 'Pendiente' AND id != ?";
        try {
            con = cn.getConnection();
            ps = con.prepareStatement(sql);
            ps.setInt(1, idSala);
            ps.setInt(2, numMesa);
            ps.setInt(3, idPedidoActual);
            rs = ps.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            System.out.println(e.toString());
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
    }

    public java.util.Map<String, Double> obtenerVentasPorHora(String fechaInicio, String fechaFin) {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        java.util.Map<String, Double> ventas = new java.util.LinkedHashMap<>();
        String sql = "SELECT strftime('%H', fecha) AS hora, SUM(total) AS total FROM pedidos WHERE estado = 'FINALIZADO' AND fecha BETWEEN ? AND ? GROUP BY hora ORDER BY hora";
        try {
            con = cn.getConnection();
            ps = con.prepareStatement(sql);
            ps.setString(1, fechaInicio);
            ps.setString(2, fechaFin);
            rs = ps.executeQuery();
            while (rs.next()) {
                ventas.put(rs.getString("hora") + ":00", rs.getDouble("total"));
            }
        } catch (SQLException e) {
            System.out.println("Error en obtenerVentasPorHora: " + e.toString());
        } finally {
            try {
                if (rs != null) rs.close();
                if (ps != null) ps.close();
                if (con != null) con.close();
            } catch (SQLException e) {}
        }
        return ventas;
    }

    public java.util.Map<String, Double> obtenerVentasUltimos7Dias() {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        java.util.Map<String, Double> ventas = new java.util.LinkedHashMap<>();
        String sql = "SELECT DATE(fecha) AS dia, SUM(total) AS total FROM pedidos WHERE estado = 'FINALIZADO' GROUP BY dia ORDER BY dia DESC LIMIT 7";
        try {
            con = cn.getConnection();
            ps = con.prepareStatement(sql);
            rs = ps.executeQuery();
            java.util.List<String> dias = new java.util.ArrayList<>();
            java.util.List<Double> totales = new java.util.ArrayList<>();
            while (rs.next()) {
                dias.add(rs.getString("dia"));
                totales.add(rs.getDouble("total"));
            }
            for (int i = dias.size() - 1; i >= 0; i--) {
                ventas.put(dias.get(i), totales.get(i));
            }
        } catch (SQLException e) {
            System.out.println("Error en obtenerVentasUltimos7Dias: " + e.toString());
        } finally {
            try {
                if (rs != null) rs.close();
                if (ps != null) ps.close();
                if (con != null) con.close();
            } catch (SQLException e) {}
        }
        return ventas;
    }

    public java.util.List<Object[]> obtenerPlatosMasVendidos(String fechaInicio, String fechaFin) {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        java.util.List<Object[]> lista = new java.util.ArrayList<>();
        String sql = "SELECT dp.nombre, SUM(dp.cantidad) AS total_vendido FROM detalle_pedidos dp INNER JOIN pedidos p ON dp.id_pedido = p.id WHERE p.estado = 'FINALIZADO' AND p.fecha BETWEEN ? AND ? GROUP BY dp.nombre ORDER BY total_vendido DESC LIMIT 5";
        try {
            con = cn.getConnection();
            ps = con.prepareStatement(sql);
            ps.setString(1, fechaInicio);
            ps.setString(2, fechaFin);
            rs = ps.executeQuery();
            while (rs.next()) {
                lista.add(new Object[]{rs.getString("nombre"), rs.getInt("total_vendido")});
            }
            rs.close();
            ps.close();

            if (lista.isEmpty()) {
                sql = "SELECT dp.nombre, SUM(dp.cantidad) AS total_vendido FROM detalle_pedidos dp INNER JOIN pedidos p ON dp.id_pedido = p.id WHERE p.estado = 'FINALIZADO' AND p.fecha >= datetime('now', '-30 days') GROUP BY dp.nombre ORDER BY total_vendido DESC LIMIT 5";
                ps = con.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    lista.add(new Object[]{rs.getString("nombre"), rs.getInt("total_vendido")});
                }
            }
        } catch (SQLException e) {
            System.out.println("Error en obtenerPlatosMasVendidos: " + e.toString());
        } finally {
            try {
                if (rs != null) rs.close();
                if (ps != null) ps.close();
                if (con != null) con.close();
            } catch (SQLException e) {}
        }
        return lista;
    }
}
