/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Vista;

import Modelo.Config;
import Modelo.DetallePedido;
import Modelo.Eventos;
import Modelo.LoginDao;
import Modelo.Pedidos;
import Modelo.PedidosDao;
import Modelo.Platos;
import Modelo.PlatosDao;
import Modelo.Salas;
import Modelo.SalasDao;
import Modelo.Tables;
import Modelo.login;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import Modelo.ImpresionTicket;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

public final class Sistema extends javax.swing.JFrame {

    Salas sl = new Salas();
    SalasDao slDao = new SalasDao();
    Config conf = new Config();
    Eventos event = new Eventos();
    Platos pla = new Platos();
    PlatosDao plaDao = new PlatosDao();
    Pedidos ped = new Pedidos();
    PedidosDao pedDao = new PedidosDao();
    DetallePedido detPedido = new DetallePedido();
    DefaultTableModel modelo = new DefaultTableModel();
    DefaultTableModel tmp = new DefaultTableModel();
    LoginDao lgDao = new LoginDao();
    int item;
    double Totalpagar = 0.0;
    Date fechaActual = new Date();
    String fechaFormato;

    public Sistema(login priv) {
        initComponents();
        cargarPedidosDelDia(); // Cargar pedidos al iniciar la aplicación
        cargarSalasCombo(); // Añade esta línea
        ImageIcon img = new ImageIcon(getClass().getResource("/Img/pizzeria.png"));

        jTabbedPane1.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jTabbedPane1StateChanged(evt);
            }
        });
        Image igmEscalada = img.getImage().getScaledInstance(labelLogo.getWidth(), labelLogo.getHeight(), Image.SCALE_SMOOTH);
        Icon icono = new ImageIcon(igmEscalada);
        labelLogo.setIcon(icono);
        this.setIconImage(img.getImage());
        this.setLocationRelativeTo(null);
        txtIdHistorialPedido.setVisible(false);
        txtIdConfig.setVisible(false);

        // UX: Reloj en tiempo real
        javax.swing.Timer timerReloj = new javax.swing.Timer(1000, e -> {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            jLabel38.setText("COMUNEROS - " + sdf.format(new java.util.Date()));
        });
        timerReloj.start();

        // UX: Hover en Sidebar
        javax.swing.JButton[] btns = {btnSala, btnVentas, btnConfig, btnUsuarios, btnPlatos};
        for (javax.swing.JButton btn : btns) {
            btn.setBackground(new java.awt.Color(60, 63, 65)); // Color base
            btn.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseEntered(java.awt.event.MouseEvent evt) {
                    btn.setBackground(new java.awt.Color(90, 93, 95)); // Hover
                }

                @Override
                public void mouseExited(java.awt.event.MouseEvent evt) {
                    btn.setBackground(new java.awt.Color(60, 63, 65)); // Normal
                }
            });
        }

        // UX: Estilo del badge de Rol
        LabelVendedor.setOpaque(true);
        LabelVendedor.setForeground(java.awt.Color.WHITE);
        LabelVendedor.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14));
        LabelVendedor.setText(" " + priv.getNombre() + " "); // Padding

        if (priv.getRol() != null && priv.getRol().equals("Asistente")) {
            btnSala.setEnabled(false);
            btnConfig.setEnabled(false);
            btnEliminarPedido.setEnabled(false);
            btnUsuarios.setEnabled(false);
            btnEliminarPlato.setEnabled(false); // Solo admin
            btnEliminarSala.setEnabled(false); // Solo admin
            LabelVendedor.setBackground(new java.awt.Color(230, 126, 34)); // Naranja para Asistente
        } else {
            LabelVendedor.setBackground(new java.awt.Color(39, 174, 96)); // Verde para Admin
            btnEliminarPlato.setEnabled(true);
            btnEliminarSala.setEnabled(true);
        }

        txtIdConfig.setVisible(false);
        txtIdHistorialPedido.setVisible(false);
        txtIdPedido.setVisible(false);
        txtIdPlato.setVisible(false);
        txtIdSala.setVisible(false);
        txtTempIdSala.setVisible(false);
        txtTempNumMesa.setVisible(false);
        jTabbedPane1.setEnabled(false);

        // UX: Estilos Finalizar Pedido
        btnFinalizar.setBackground(new java.awt.Color(46, 204, 113)); // Verde esmeralda
        btnFinalizar.setForeground(java.awt.Color.WHITE);
        btnFinalizar.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 16));
        totalFinalizar.setForeground(new java.awt.Color(46, 204, 113));
        totalFinalizar.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 24));

        // UX: Mejoras en Tablas (Hover + Zebra)
        javax.swing.JTable[] tablas = {tableSala, TableUsuarios, TablePlatos, TablePedidos, tableMenu, tableFinalizar, tblTemPlatos};
        for (javax.swing.JTable t : tablas) {
            t.setRowHeight(30);
            t.setSelectionBackground(new java.awt.Color(41, 128, 185)); // Azul brillante al seleccionar
            t.setSelectionForeground(java.awt.Color.WHITE);
            t.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 14));
            // Hover (MouseMotionListener)
            t.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
                int lastRow = -1;

                @Override
                public void mouseMoved(java.awt.event.MouseEvent e) {
                    int row = t.rowAtPoint(e.getPoint());
                    if (row != lastRow) {
                        lastRow = row;
                        t.repaint();
                    }
                }
            });
            // Restaurar repintado al salir de la tabla
            t.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseExited(java.awt.event.MouseEvent e) {
                    t.repaint();
                }
            });
        }

        // Asignar el renderizador de cebra/hover (excepto en TablePedidos que usa Tables.java)
        javax.swing.table.DefaultTableCellRenderer zebraRenderer = new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public java.awt.Component getTableCellRendererComponent(javax.swing.JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                java.awt.Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    java.awt.Point p = table.getMousePosition();
                    int hoverRow = p != null ? table.rowAtPoint(p) : -1;
                    if (row == hoverRow) {
                        c.setBackground(new java.awt.Color(51, 65, 85)); // Hover (Slate 700)
                    } else if (row % 2 == 0) {
                        c.setBackground(new java.awt.Color(30, 41, 59)); // Cebra par (Slate 800)
                    } else {
                        c.setBackground(new java.awt.Color(15, 23, 42)); // Cebra impar (Slate 900)
                    }
                    c.setForeground(new java.awt.Color(241, 245, 249)); // Texto claro
                } else {
                    c.setBackground(new java.awt.Color(59, 130, 246)); // Fila seleccionada (Azul)
                    c.setForeground(java.awt.Color.WHITE);
                }
                return c;
            }
        };
        tableSala.setDefaultRenderer(Object.class, zebraRenderer);
        TableUsuarios.setDefaultRenderer(Object.class, zebraRenderer);
        TablePlatos.setDefaultRenderer(Object.class, zebraRenderer);
        tableMenu.setDefaultRenderer(Object.class, zebraRenderer);
        tableFinalizar.setDefaultRenderer(Object.class, zebraRenderer);
        tblTemPlatos.setDefaultRenderer(Object.class, zebraRenderer);

        // Validaciones numéricas para prevenir crashes
        aplicarFiltroNumerico(txtMesas, false); // Solo enteros
        aplicarFiltroNumerico(txtPrecioPlato, true); // Permite decimales

        // UX: Letras blancas sobre fondo oscuro en inputs
        javax.swing.JTextField[] camposInput = {txtNombreSala, txtMesas, txtTelefonoConfig, txtDireccionConfig, txtMensaje, txtRucConfig, txtNombreConfig, txtCorreo, txtPass, txtNombre, txtNombrePlato, txtPrecioPlato, txtBuscarPlato};
        for (javax.swing.JTextField campo : camposInput) {
            campo.setForeground(java.awt.Color.WHITE);
        }

        decorarSistemaUI();
        panelSalas();
    }

    // Método auxiliar para evitar que los usuarios escriban letras en campos numéricos
    private void aplicarFiltroNumerico(javax.swing.JTextField textField, boolean permiteDecimal) {
        textField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent e) {
                char c = e.getKeyChar();
                if (permiteDecimal) {
                    if (((c < '0') || (c > '9')) && (c != java.awt.event.KeyEvent.VK_BACK_SPACE) && (c != '.')) {
                        e.consume(); // Ignorar tecla
                    }
                    if (c == '.' && textField.getText().contains(".")) {
                        e.consume(); // Evitar multiples puntos
                    }
                } else {
                    if (((c < '0') || (c > '9')) && (c != java.awt.event.KeyEvent.VK_BACK_SPACE)) {
                        e.consume();
                    }
                }
            }
        });
    }

    // Método auxiliar para feedback visual de éxito en botones
    private void mostrarExitoEnBoton(javax.swing.JButton boton, String textoOriginal, String textoExito) {
        java.awt.Color colorOriginal = boton.getBackground();
        boton.setBackground(new java.awt.Color(39, 174, 96));
        boton.setText(textoExito);
        boton.setEnabled(false);
        // También mostrar un Toast para refuerzo visual
        ToastNotification.exito(boton, textoExito.replace("✅ ", ""));
        new javax.swing.Timer(2000, evt -> {
            boton.setBackground(colorOriginal);
            boton.setText(textoOriginal);
            boton.setEnabled(true);
        }).start();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel() {
            @Override
            protected void paintComponent(java.awt.Graphics g) {
                super.paintComponent(g);
                java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
                g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                java.awt.GradientPaint gp = new java.awt.GradientPaint(0, 0, new java.awt.Color(15, 23, 42), getWidth(), getHeight(), new java.awt.Color(30, 64, 175));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                // Círculo decorativo
                g2.setColor(new java.awt.Color(255, 255, 255, 10));
                g2.fillOval(-50, 500, 200, 200);
                g2.dispose();
            }
        };
        labelLogo = new javax.swing.JLabel();
        btnSala = new javax.swing.JButton();
        btnVentas = new javax.swing.JButton();
        btnConfig = new javax.swing.JButton();
        LabelVendedor = new javax.swing.JLabel() {
            @Override
            protected void paintComponent(java.awt.Graphics g) {
                java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
                g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        tipo = new javax.swing.JLabel();
        btnUsuarios = new javax.swing.JButton();
        btnPlatos = new javax.swing.JButton();
        jLabel38 = new javax.swing.JLabel();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel9 = new javax.swing.JPanel();
        jScrollPane8 = new javax.swing.JScrollPane();
        PanelSalas = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        tableSala = new javax.swing.JTable();
        jPanel10 = new javax.swing.JPanel();
        jLabel18 = new javax.swing.JLabel();
        txtNombreSala = new javax.swing.JTextField();
        btnRegistrarSala = new javax.swing.JButton();
        btnActualizarSala = new javax.swing.JButton();
        btnNuevoSala = new javax.swing.JButton();
        btnEliminarSala = new javax.swing.JButton();
        txtIdSala = new javax.swing.JTextField();
        jPanel35 = new javax.swing.JPanel();
        jPanel38 = new javax.swing.JPanel();
        jLabel33 = new javax.swing.JLabel();
        jPanel36 = new javax.swing.JPanel();
        txtMesas = new javax.swing.JTextField();
        jLabel19 = new javax.swing.JLabel();
        jPanel22 = new javax.swing.JPanel();
        jScrollPane9 = new javax.swing.JScrollPane();
        PanelMesas = new javax.swing.JPanel();
        jPanel23 = new javax.swing.JPanel();
        jPanel24 = new javax.swing.JPanel();
        txtBuscarPlato = new javax.swing.JTextField();
        jScrollPane10 = new javax.swing.JScrollPane();
        tblTemPlatos = new javax.swing.JTable();
        btnAddPlato = new javax.swing.JButton();
        jScrollPane11 = new javax.swing.JScrollPane();
        tableMenu = new javax.swing.JTable();
        jLabel6 = new javax.swing.JLabel();
        jScrollPane12 = new javax.swing.JScrollPane();
        txtComentario = new javax.swing.JTextPane();
        jLabel11 = new javax.swing.JLabel();
        totalMenu = new javax.swing.JLabel();
        btnGenerarPedido = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        btnEliminarTempPlato = new javax.swing.JButton();
        txtTempIdSala = new javax.swing.JTextField();
        txtTempNumMesa = new javax.swing.JTextField();
        jPanel25 = new javax.swing.JPanel();
        btnFinalizar = new javax.swing.JButton();
        totalFinalizar = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        jScrollPane13 = new javax.swing.JScrollPane();
        tableFinalizar = new javax.swing.JTable();
        txtIdPedido = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        txtFechaHora = new javax.swing.JTextField();
        txtSalaFinalizar = new javax.swing.JTextField();
        txtNumMesaFinalizar = new javax.swing.JTextField();
        btnPdfPedido = new javax.swing.JButton();
        txtIdHistorialPedido = new javax.swing.JTextField();
        btnEliminarPlatoFinalizar = new javax.swing.JButton();
        btnAddPlatoFinalizar = new javax.swing.JButton();
        btnImprimir = new javax.swing.JButton();
        jComboSalas = new javax.swing.JComboBox<>();
        btnEfectivo = new javax.swing.JButton();
        btnTransaccion = new javax.swing.JButton();
        jPanel6 = new javax.swing.JPanel();
        jScrollPane5 = new javax.swing.JScrollPane();
        TablePedidos = new javax.swing.JTable();
        jLabel16 = new javax.swing.JLabel();
        txtTotalDia = new javax.swing.JTextField();
        txtTotalDiaTrans = new javax.swing.JTextField();
        jLabel20 = new javax.swing.JLabel();
        jLabel21 = new javax.swing.JLabel();
        btnEliminarPedido = new javax.swing.JButton();
        BtnImprimirDia = new javax.swing.JButton();
        txtPedidosDia = new javax.swing.JTextField();
        jLabel22 = new javax.swing.JLabel();
        jPanel7 = new javax.swing.JPanel();
        jLabel32 = new javax.swing.JLabel();
        jPanel8 = new javax.swing.JPanel();
        txtIdConfig = new javax.swing.JTextField();
        jLabel30 = new javax.swing.JLabel();
        jLabel29 = new javax.swing.JLabel();
        txtTelefonoConfig = new javax.swing.JTextField();
        txtDireccionConfig = new javax.swing.JTextField();
        jLabel31 = new javax.swing.JLabel();
        txtMensaje = new javax.swing.JTextField();
        btnActualizarConfig = new javax.swing.JButton();
        jLabel27 = new javax.swing.JLabel();
        txtRucConfig = new javax.swing.JTextField();
        jLabel28 = new javax.swing.JLabel();
        txtNombreConfig = new javax.swing.JTextField();
        jPanel41 = new javax.swing.JPanel();
        jPanel42 = new javax.swing.JPanel();
        jPanel43 = new javax.swing.JPanel();
        jPanel44 = new javax.swing.JPanel();
        jPanel45 = new javax.swing.JPanel();
        jPanel40 = new javax.swing.JPanel();
        jPanel12 = new javax.swing.JPanel();
        jScrollPane6 = new javax.swing.JScrollPane();
        TableUsuarios = new javax.swing.JTable();
        jPanel15 = new javax.swing.JPanel();
        jLabel34 = new javax.swing.JLabel();
        jLabel35 = new javax.swing.JLabel();
        txtCorreo = new javax.swing.JTextField();
        txtPass = new javax.swing.JPasswordField();
        btnIniciar = new javax.swing.JButton();
        jLabel36 = new javax.swing.JLabel();
        txtNombre = new javax.swing.JTextField();
        jLabel37 = new javax.swing.JLabel();
        cbxRol = new javax.swing.JComboBox<>();
        jPanel16 = new javax.swing.JPanel();
        jPanel17 = new javax.swing.JPanel();
        jPanel18 = new javax.swing.JPanel();
        jPanel21 = new javax.swing.JPanel();
        jLabel39 = new javax.swing.JLabel();
        btnModificarUsua = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jPanel11 = new javax.swing.JPanel();
        jLabel23 = new javax.swing.JLabel();
        txtNombrePlato = new javax.swing.JTextField();
        jLabel25 = new javax.swing.JLabel();
        txtPrecioPlato = new javax.swing.JTextField();
        btnGuardarPlato = new javax.swing.JButton();
        btnEditarPlato = new javax.swing.JButton();
        btnEliminarPlato = new javax.swing.JButton();
        btnNuevoPlato = new javax.swing.JButton();
        jPanel31 = new javax.swing.JPanel();
        jPanel33 = new javax.swing.JPanel();
        jPanel39 = new javax.swing.JPanel();
        jLabel40 = new javax.swing.JLabel();
        txtIdPlato = new javax.swing.JTextField();
        jScrollPane4 = new javax.swing.JScrollPane();
        TablePlatos = new javax.swing.JTable();
        jLabel_wallpaper = new javax.swing.JLabel();
        BtnCerrarSesion = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Panel de Adminstración");
        setResizable(false);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        labelLogo.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        labelLogo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/pizzeria.png"))); // NOI18N
        labelLogo.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        labelLogo.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                labelLogoMouseClicked(evt);
            }
        });

        btnSala.setBackground(new java.awt.Color(60, 63, 65));
        btnSala.setForeground(new java.awt.Color(255, 255, 255));
        btnSala.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/sala.png"))); // NOI18N
        btnSala.setText("Salas");
        btnSala.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        btnSala.setEnabled(false);
        btnSala.setFocusable(false);
        btnSala.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSalaActionPerformed(evt);
            }
        });

        btnVentas.setBackground(new java.awt.Color(60, 63, 65));
        btnVentas.setForeground(new java.awt.Color(255, 255, 255));
        btnVentas.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/pedidos.png"))); // NOI18N
        btnVentas.setText("Pedidos");
        btnVentas.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        btnVentas.setFocusable(false);
        btnVentas.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnVentasActionPerformed(evt);
            }
        });

        btnConfig.setBackground(new java.awt.Color(60, 63, 65));
        btnConfig.setForeground(new java.awt.Color(255, 255, 255));
        btnConfig.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/config.png"))); // NOI18N
        btnConfig.setText("Config");
        btnConfig.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        btnConfig.setFocusable(false);
        btnConfig.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnConfigActionPerformed(evt);
            }
        });

        LabelVendedor.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        LabelVendedor.setText("Administrador");

        tipo.setForeground(new java.awt.Color(255, 255, 255));

        btnUsuarios.setBackground(new java.awt.Color(60, 63, 65));
        btnUsuarios.setForeground(new java.awt.Color(255, 255, 255));
        btnUsuarios.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/usuarios.png"))); // NOI18N
        btnUsuarios.setText("Usuarios");
        btnUsuarios.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        btnUsuarios.setFocusable(false);
        btnUsuarios.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUsuariosActionPerformed(evt);
            }
        });

        btnPlatos.setBackground(new java.awt.Color(60, 63, 65));
        btnPlatos.setForeground(new java.awt.Color(255, 255, 255));
        btnPlatos.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/platos.png"))); // NOI18N
        btnPlatos.setText("Carta");
        btnPlatos.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        btnPlatos.setFocusable(false);
        btnPlatos.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPlatosActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
                jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(btnSala, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnVentas, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnConfig, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(LabelVendedor, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnUsuarios, javax.swing.GroupLayout.DEFAULT_SIZE, 200, Short.MAX_VALUE)
                        .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(74, 74, 74)
                                .addComponent(tipo)
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addComponent(btnPlatos, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(labelLogo, javax.swing.GroupLayout.PREFERRED_SIZE, 199, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
                jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(labelLogo, javax.swing.GroupLayout.PREFERRED_SIZE, 175, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(tipo)
                                .addGap(18, 18, 18)
                                .addComponent(LabelVendedor)
                                .addGap(28, 28, 28)
                                .addComponent(btnPlatos, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(37, 37, 37)
                                .addComponent(btnSala, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(37, 37, 37)
                                .addComponent(btnVentas, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(39, 39, 39)
                                .addComponent(btnConfig, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(38, 38, 38)
                                .addComponent(btnUsuarios, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap(89, Short.MAX_VALUE))
        );

        getContentPane().add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 200, 720));

        jLabel38.setFont(new java.awt.Font("Zilla Slab", 1, 48)); // NOI18N
        jLabel38.setText("COMUNEROS- PUENTE NACIONAL");
        jLabel38.setFocusable(false);
        jLabel38.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        getContentPane().add(jLabel38, new org.netbeans.lib.awtextra.AbsoluteConstraints(340, 0, 820, 90));

        jScrollPane8.setBackground(new java.awt.Color(60, 63, 65));

        PanelSalas.setForeground(new java.awt.Color(0, 0, 0));
        PanelSalas.setLayout(new java.awt.GridLayout(0, 5));
        jScrollPane8.setViewportView(PanelSalas);

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
                jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel9Layout.createSequentialGroup()
                                .addGap(30, 30, 30)
                                .addComponent(jScrollPane8, javax.swing.GroupLayout.PREFERRED_SIZE, 1030, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        jPanel9Layout.setVerticalGroup(
                jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel9Layout.createSequentialGroup()
                                .addGap(30, 30, 30)
                                .addComponent(jScrollPane8, javax.swing.GroupLayout.PREFERRED_SIZE, 540, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jTabbedPane1.addTab("Panel", jPanel9);

        jPanel4.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jScrollPane3.setBackground(new java.awt.Color(60, 63, 65));

        tableSala.setModel(new javax.swing.table.DefaultTableModel(
                new Object[][]{},
                new String[]{
                    "ID", "NOMBRE", "Mesas"
                }
        ));
        tableSala.setRowHeight(23);
        tableSala.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tableSalaMouseClicked(evt);
            }
        });
        jScrollPane3.setViewportView(tableSala);
        if (tableSala.getColumnModel().getColumnCount() > 0) {
            tableSala.getColumnModel().getColumn(0).setMinWidth(80);
            tableSala.getColumnModel().getColumn(0).setPreferredWidth(80);
            tableSala.getColumnModel().getColumn(0).setMaxWidth(130);
            tableSala.getColumnModel().getColumn(1).setPreferredWidth(100);
            tableSala.getColumnModel().getColumn(2).setMinWidth(80);
            tableSala.getColumnModel().getColumn(2).setPreferredWidth(80);
            tableSala.getColumnModel().getColumn(2).setMaxWidth(150);
        }

        jPanel4.add(jScrollPane3, new org.netbeans.lib.awtextra.AbsoluteConstraints(510, 80, 490, 470));

        jPanel10.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel10.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel18.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel18.setText("Nombre:");
        jPanel10.add(jLabel18, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 80, -1, -1));

        txtNombreSala.setBackground(new java.awt.Color(204, 204, 204));
        txtNombreSala.setForeground(new java.awt.Color(0, 0, 0));
        txtNombreSala.setBorder(null);
        jPanel10.add(txtNombreSala, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 70, 190, 30));

        btnRegistrarSala.setBackground(new java.awt.Color(46, 204, 113));
        btnRegistrarSala.setForeground(new java.awt.Color(255, 255, 255));
        btnRegistrarSala.setText("REGISTRAR");
        btnRegistrarSala.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRegistrarSalaActionPerformed(evt);
            }
        });
        jPanel10.add(btnRegistrarSala, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 230, 100, 40));

        btnActualizarSala.setText("ACTUALIZAR");
        btnActualizarSala.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnActualizarSalaActionPerformed(evt);
            }
        });
        jPanel10.add(btnActualizarSala, new org.netbeans.lib.awtextra.AbsoluteConstraints(190, 230, 100, 40));

        btnNuevoSala.setText("NUEVA SALA");
        btnNuevoSala.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNuevoSalaActionPerformed(evt);
            }
        });
        jPanel10.add(btnNuevoSala, new org.netbeans.lib.awtextra.AbsoluteConstraints(190, 300, 100, 40));

        btnEliminarSala.setBackground(new java.awt.Color(220, 20, 60));
        btnEliminarSala.setForeground(new java.awt.Color(255, 255, 255));
        btnEliminarSala.setText("ELIMINAR");
        btnEliminarSala.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEliminarSalaActionPerformed(evt);
            }
        });
        jPanel10.add(btnEliminarSala, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 300, 100, 40));
        jPanel10.add(txtIdSala, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 50, 24, -1));

        jPanel35.setBackground(new java.awt.Color(0, 0, 0));

        javax.swing.GroupLayout jPanel35Layout = new javax.swing.GroupLayout(jPanel35);
        jPanel35.setLayout(jPanel35Layout);
        jPanel35Layout.setHorizontalGroup(
                jPanel35Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 190, Short.MAX_VALUE)
        );
        jPanel35Layout.setVerticalGroup(
                jPanel35Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 2, Short.MAX_VALUE)
        );

        jPanel10.add(jPanel35, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 100, 190, 2));

        jPanel38.setBackground(new java.awt.Color(0, 0, 0));
        jPanel38.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel33.setFont(new java.awt.Font("Times New Roman", 1, 18)); // NOI18N
        jLabel33.setForeground(new java.awt.Color(255, 255, 255));
        jLabel33.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel33.setText("Nuevo Sala");
        jPanel38.add(jLabel33, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 310, 30));

        jPanel10.add(jPanel38, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 310, 35));

        jPanel36.setBackground(new java.awt.Color(0, 0, 0));

        javax.swing.GroupLayout jPanel36Layout = new javax.swing.GroupLayout(jPanel36);
        jPanel36.setLayout(jPanel36Layout);
        jPanel36Layout.setHorizontalGroup(
                jPanel36Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 190, Short.MAX_VALUE)
        );
        jPanel36Layout.setVerticalGroup(
                jPanel36Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 2, Short.MAX_VALUE)
        );

        jPanel10.add(jPanel36, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 170, 190, 2));

        txtMesas.setBackground(new java.awt.Color(204, 204, 204));
        txtMesas.setForeground(new java.awt.Color(0, 0, 0));
        txtMesas.setBorder(null);
        jPanel10.add(txtMesas, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 140, 190, 30));

        jLabel19.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel19.setText("Mesas:");
        jPanel10.add(jLabel19, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 150, -1, -1));

        jPanel4.add(jPanel10, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 140, 310, 370));

        jTabbedPane1.addTab("Salas", jPanel4);

        PanelMesas.setLayout(new java.awt.GridLayout(0, 5));
        jScrollPane9.setViewportView(PanelMesas);

        javax.swing.GroupLayout jPanel22Layout = new javax.swing.GroupLayout(jPanel22);
        jPanel22.setLayout(jPanel22Layout);
        jPanel22Layout.setHorizontalGroup(
                jPanel22Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel22Layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(jScrollPane9, javax.swing.GroupLayout.DEFAULT_SIZE, 1068, Short.MAX_VALUE)
                                .addContainerGap())
        );
        jPanel22Layout.setVerticalGroup(
                jPanel22Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel22Layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(jScrollPane9, javax.swing.GroupLayout.DEFAULT_SIZE, 573, Short.MAX_VALUE)
                                .addContainerGap())
        );

        jTabbedPane1.addTab("Mesas", jPanel22);

        jPanel24.setBorder(javax.swing.BorderFactory.createTitledBorder("Platos del Dia"));

        txtBuscarPlato.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtBuscarPlatoKeyReleased(evt);
            }
        });

        tblTemPlatos.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        tblTemPlatos.setModel(new javax.swing.table.DefaultTableModel(
                new Object[][]{},
                new String[]{
                    "", "Nombre", "Precio"
                }
        ) {
            boolean[] canEdit = new boolean[]{
                false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit[columnIndex];
            }
        });
        tblTemPlatos.setRowHeight(23);
        jScrollPane10.setViewportView(tblTemPlatos);
        if (tblTemPlatos.getColumnModel().getColumnCount() > 0) {
            tblTemPlatos.getColumnModel().getColumn(0).setMinWidth(30);
            tblTemPlatos.getColumnModel().getColumn(0).setPreferredWidth(30);
            tblTemPlatos.getColumnModel().getColumn(0).setMaxWidth(50);
            tblTemPlatos.getColumnModel().getColumn(2).setMinWidth(150);
            tblTemPlatos.getColumnModel().getColumn(2).setPreferredWidth(150);
            tblTemPlatos.getColumnModel().getColumn(2).setMaxWidth(200);
        }

        btnAddPlato.setBackground(new java.awt.Color(0, 0, 0));
        btnAddPlato.setFont(new java.awt.Font("Arial Black", 1, 24)); // NOI18N
        btnAddPlato.setForeground(new java.awt.Color(255, 255, 255));
        btnAddPlato.setText("+");
        btnAddPlato.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnAddPlato.setFocusable(false);
        btnAddPlato.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnAddPlato.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddPlatoActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel24Layout = new javax.swing.GroupLayout(jPanel24);
        jPanel24.setLayout(jPanel24Layout);
        jPanel24Layout.setHorizontalGroup(
                jPanel24Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel24Layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(jPanel24Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jScrollPane10, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                                        .addGroup(jPanel24Layout.createSequentialGroup()
                                                .addComponent(txtBuscarPlato, javax.swing.GroupLayout.PREFERRED_SIZE, 349, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 36, Short.MAX_VALUE)
                                                .addComponent(btnAddPlato)))
                                .addContainerGap())
        );
        jPanel24Layout.setVerticalGroup(
                jPanel24Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel24Layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(jPanel24Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addComponent(txtBuscarPlato, javax.swing.GroupLayout.DEFAULT_SIZE, 32, Short.MAX_VALUE)
                                        .addComponent(btnAddPlato, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                                .addGap(18, 18, 18)
                                .addComponent(jScrollPane10, javax.swing.GroupLayout.DEFAULT_SIZE, 450, Short.MAX_VALUE)
                                .addContainerGap())
        );

        tableMenu.setModel(new javax.swing.table.DefaultTableModel(
                new Object[][]{},
                new String[]{
                    "", "Plato", "Cant", "Precio", "SubTotal", "Comentario"
                }
        ) {
            boolean[] canEdit = new boolean[]{
                false, false, false, false, false, true
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit[columnIndex];
            }
        });
        tableMenu.setRowHeight(23);
        jScrollPane11.setViewportView(tableMenu);
        if (tableMenu.getColumnModel().getColumnCount() > 0) {
            tableMenu.getColumnModel().getColumn(0).setMinWidth(30);
            tableMenu.getColumnModel().getColumn(0).setPreferredWidth(30);
            tableMenu.getColumnModel().getColumn(0).setMaxWidth(50);
            tableMenu.getColumnModel().getColumn(1).setPreferredWidth(100);
            tableMenu.getColumnModel().getColumn(2).setMinWidth(40);
            tableMenu.getColumnModel().getColumn(2).setPreferredWidth(40);
            tableMenu.getColumnModel().getColumn(2).setMaxWidth(50);
            tableMenu.getColumnModel().getColumn(3).setPreferredWidth(50);
            tableMenu.getColumnModel().getColumn(4).setPreferredWidth(60);
        }

        jLabel6.setText("Comentario:");

        jScrollPane12.setViewportView(txtComentario);

        jLabel11.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel11.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/money.png"))); // NOI18N
        jLabel11.setText("Total a Pagar");

        totalMenu.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        totalMenu.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        totalMenu.setText("00.00");

        btnGenerarPedido.setText("Realizar Pedido");
        btnGenerarPedido.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGenerarPedidoActionPerformed(evt);
            }
        });

        jButton2.setText("Agregar");
        jButton2.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        btnEliminarTempPlato.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/eliminar.png"))); // NOI18N
        btnEliminarTempPlato.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnEliminarTempPlato.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEliminarTempPlatoActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel23Layout = new javax.swing.GroupLayout(jPanel23);
        jPanel23.setLayout(jPanel23Layout);
        jPanel23Layout.setHorizontalGroup(
                jPanel23Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel23Layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(jPanel23Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(jPanel23Layout.createSequentialGroup()
                                                .addComponent(jLabel6)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(jScrollPane12, javax.swing.GroupLayout.PREFERRED_SIZE, 380, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addGroup(jPanel23Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                        .addComponent(btnEliminarTempPlato, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                        .addComponent(jButton2, javax.swing.GroupLayout.DEFAULT_SIZE, 80, Short.MAX_VALUE)))
                                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel23Layout.createSequentialGroup()
                                                .addGap(0, 23, Short.MAX_VALUE)
                                                .addGroup(jPanel23Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                                        .addComponent(jScrollPane11, javax.swing.GroupLayout.PREFERRED_SIZE, 570, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addGroup(jPanel23Layout.createSequentialGroup()
                                                                .addGroup(jPanel23Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                                        .addComponent(txtTempIdSala)
                                                                        .addComponent(txtTempNumMesa, javax.swing.GroupLayout.DEFAULT_SIZE, 117, Short.MAX_VALUE))
                                                                .addGap(79, 342, Short.MAX_VALUE)
                                                                .addGroup(jPanel23Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                                                        .addComponent(btnGenerarPedido)
                                                                        .addGroup(jPanel23Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                                                .addComponent(jLabel11)
                                                                                .addGroup(jPanel23Layout.createSequentialGroup()
                                                                                        .addGap(10, 10, 10)
                                                                                        .addComponent(totalMenu, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE))))))))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jPanel24, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap())
        );
        jPanel23Layout.setVerticalGroup(
                jPanel23Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel23Layout.createSequentialGroup()
                                .addContainerGap(43, Short.MAX_VALUE)
                                .addGroup(jPanel23Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addGroup(jPanel23Layout.createSequentialGroup()
                                                .addGroup(jPanel23Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addComponent(jLabel6)
                                                        .addGroup(jPanel23Layout.createSequentialGroup()
                                                                .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addGap(18, 18, 18)
                                                                .addComponent(btnEliminarTempPlato, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                        .addComponent(jScrollPane12, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                .addGap(18, 18, 18)
                                                .addComponent(jScrollPane11, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                                                .addGap(18, 18, 18)
                                                .addGroup(jPanel23Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addGroup(jPanel23Layout.createSequentialGroup()
                                                                .addComponent(txtTempIdSala, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addGap(14, 14, 14)
                                                                .addComponent(txtTempNumMesa, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                        .addGroup(jPanel23Layout.createSequentialGroup()
                                                                .addComponent(jLabel11)
                                                                .addGap(14, 14, 14)
                                                                .addComponent(totalMenu)
                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                                .addComponent(btnGenerarPedido, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                                .addGap(39, 39, 39))
                                        .addComponent(jPanel24, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addContainerGap(7, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Platos", jPanel23);

        jPanel25.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        btnFinalizar.setText("Finalizar");
        btnFinalizar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnFinalizarActionPerformed(evt);
            }
        });
        jPanel25.add(btnFinalizar, new org.netbeans.lib.awtextra.AbsoluteConstraints(920, 440, 110, 40));

        totalFinalizar.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        totalFinalizar.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        totalFinalizar.setText("00.00");
        jPanel25.add(totalFinalizar, new org.netbeans.lib.awtextra.AbsoluteConstraints(910, 390, 120, -1));

        jLabel17.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel17.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/money.png"))); // NOI18N
        jLabel17.setText("Total a Pagar");
        jPanel25.add(jLabel17, new org.netbeans.lib.awtextra.AbsoluteConstraints(900, 340, -1, -1));

        tableFinalizar.setModel(new javax.swing.table.DefaultTableModel(
                new Object[][]{},
                new String[]{
                    "", "Plato", "Cant", "Precio", "SubTotal", "Comentario"
                }
        ) {
            boolean[] canEdit = new boolean[]{
                false, false, false, false, false, true
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit[columnIndex];
            }
        });
        tableFinalizar.setRowHeight(23);
        jScrollPane13.setViewportView(tableFinalizar);
        if (tableFinalizar.getColumnModel().getColumnCount() > 0) {
            tableFinalizar.getColumnModel().getColumn(0).setMinWidth(30);
            tableFinalizar.getColumnModel().getColumn(0).setPreferredWidth(30);
            tableFinalizar.getColumnModel().getColumn(0).setMaxWidth(50);
            tableFinalizar.getColumnModel().getColumn(1).setPreferredWidth(100);
            tableFinalizar.getColumnModel().getColumn(2).setMinWidth(40);
            tableFinalizar.getColumnModel().getColumn(2).setPreferredWidth(40);
            tableFinalizar.getColumnModel().getColumn(2).setMaxWidth(50);
            tableFinalizar.getColumnModel().getColumn(3).setPreferredWidth(50);
            tableFinalizar.getColumnModel().getColumn(4).setPreferredWidth(60);
        }

        jPanel25.add(jScrollPane13, new org.netbeans.lib.awtextra.AbsoluteConstraints(39, 13, 1030, 316));
        jPanel25.add(txtIdPedido, new org.netbeans.lib.awtextra.AbsoluteConstraints(510, 390, 50, -1));

        jLabel7.setFont(new java.awt.Font("Times New Roman", 1, 18)); // NOI18N
        jLabel7.setText("Fecha y Hora:");
        jPanel25.add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 350, -1, -1));

        jLabel8.setFont(new java.awt.Font("Times New Roman", 1, 18)); // NOI18N
        jLabel8.setText("Sala:");
        jPanel25.add(jLabel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 400, -1, -1));

        jLabel9.setFont(new java.awt.Font("Times New Roman", 1, 18)); // NOI18N
        jLabel9.setText("N° Mesa:");
        jPanel25.add(jLabel9, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 450, -1, -1));

        txtFechaHora.setEditable(false);
        jPanel25.add(txtFechaHora, new org.netbeans.lib.awtextra.AbsoluteConstraints(180, 350, 240, 30));

        txtSalaFinalizar.setEditable(false);
        jPanel25.add(txtSalaFinalizar, new org.netbeans.lib.awtextra.AbsoluteConstraints(180, 400, 150, 30));

        txtNumMesaFinalizar.setEditable(false);
        jPanel25.add(txtNumMesaFinalizar, new org.netbeans.lib.awtextra.AbsoluteConstraints(180, 450, 240, 30));

        btnPdfPedido.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/pdf.png"))); // NOI18N
        btnPdfPedido.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPdfPedidoActionPerformed(evt);
            }
        });
        jPanel25.add(btnPdfPedido, new org.netbeans.lib.awtextra.AbsoluteConstraints(780, 440, 110, 40));

        txtIdHistorialPedido.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtIdHistorialPedidoActionPerformed(evt);
            }
        });
        jPanel25.add(txtIdHistorialPedido, new org.netbeans.lib.awtextra.AbsoluteConstraints(530, 450, 50, -1));

        btnEliminarPlatoFinalizar.setBackground(new java.awt.Color(220, 20, 60));
        btnEliminarPlatoFinalizar.setForeground(new java.awt.Color(255, 255, 255));
        btnEliminarPlatoFinalizar.setText("Eliminar");
        btnEliminarPlatoFinalizar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEliminarPlatoFinalizarActionPerformed(evt);
            }
        });
        jPanel25.add(btnEliminarPlatoFinalizar, new org.netbeans.lib.awtextra.AbsoluteConstraints(920, 510, 110, 40));

        btnAddPlatoFinalizar.setBackground(new java.awt.Color(46, 204, 113));
        btnAddPlatoFinalizar.setForeground(new java.awt.Color(255, 255, 255));
        btnAddPlatoFinalizar.setText("Agregar");
        btnAddPlatoFinalizar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddPlatoFinalizarActionPerformed(evt);
            }
        });
        jPanel25.add(btnAddPlatoFinalizar, new org.netbeans.lib.awtextra.AbsoluteConstraints(780, 510, 110, 40));

        btnImprimir.setText("Imprimir ");
        btnImprimir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnImprimirActionPerformed(evt);
            }
        });
        jPanel25.add(btnImprimir, new org.netbeans.lib.awtextra.AbsoluteConstraints(670, 440, 90, 40));

        jComboSalas.setModel(new javax.swing.DefaultComboBoxModel<>(new String[]{"Seleccionar", "Efectivo", "Transaccion", "Transaccion-efectivo", " "}));
        jComboSalas.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboSalasActionPerformed(evt);
            }
        });
        jPanel25.add(jComboSalas, new org.netbeans.lib.awtextra.AbsoluteConstraints(330, 400, 90, 30));

        btnEfectivo.setText("Efectivo");
        btnEfectivo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEfectivoActionPerformed(evt);
            }
        });
        jPanel25.add(btnEfectivo, new org.netbeans.lib.awtextra.AbsoluteConstraints(530, 510, 110, 40));

        btnTransaccion.setText("Transaccion");
        btnTransaccion.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnTransaccionActionPerformed(evt);
            }
        });
        jPanel25.add(btnTransaccion, new org.netbeans.lib.awtextra.AbsoluteConstraints(650, 510, 110, 40));

        jTabbedPane1.addTab("Finalizar Pedido", jPanel25);

        jPanel6.setBackground(new java.awt.Color(70, 73, 75));
        jPanel6.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        TablePedidos.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        TablePedidos.setModel(new javax.swing.table.DefaultTableModel(
                new Object[][]{},
                new String[]{
                    "Id", "Sala", "Atendido", "N° Mesa", "Fecha", "Total", "Estado"
                }
        ) {
            boolean[] canEdit = new boolean[]{
                false, false, false, false, false, false, true
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit[columnIndex];
            }
        });
        TablePedidos.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        TablePedidos.setRowHeight(23);
        TablePedidos.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                TablePedidosMouseClicked(evt);
            }
        });
        jScrollPane5.setViewportView(TablePedidos);
        if (TablePedidos.getColumnModel().getColumnCount() > 0) {
            TablePedidos.getColumnModel().getColumn(0).setMinWidth(80);
            TablePedidos.getColumnModel().getColumn(0).setPreferredWidth(80);
            TablePedidos.getColumnModel().getColumn(0).setMaxWidth(120);
            TablePedidos.getColumnModel().getColumn(2).setPreferredWidth(60);
            TablePedidos.getColumnModel().getColumn(3).setMinWidth(100);
            TablePedidos.getColumnModel().getColumn(3).setPreferredWidth(100);
            TablePedidos.getColumnModel().getColumn(3).setMaxWidth(150);
            TablePedidos.getColumnModel().getColumn(4).setPreferredWidth(60);
        }

        jPanel6.add(jScrollPane5, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 50, 1020, 440));

        jLabel16.setFont(new java.awt.Font("Times New Roman", 1, 18)); // NOI18N
        jLabel16.setForeground(new java.awt.Color(255, 255, 255));
        jLabel16.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel16.setText("Historial Pedidos");
        jPanel6.add(jLabel16, new org.netbeans.lib.awtextra.AbsoluteConstraints(390, 20, 280, -1));

        txtTotalDia.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        txtTotalDia.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtTotalDiaActionPerformed(evt);
            }
        });
        jPanel6.add(txtTotalDia, new org.netbeans.lib.awtextra.AbsoluteConstraints(270, 530, 240, 40));

        txtTotalDiaTrans.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        txtTotalDiaTrans.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtTotalDiaTransActionPerformed(evt);
            }
        });
        jPanel6.add(txtTotalDiaTrans, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 530, 230, 40));

        jLabel20.setFont(new java.awt.Font("Times New Roman", 1, 18)); // NOI18N
        jLabel20.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel20.setText("NUM PED:");
        jPanel6.add(jLabel20, new org.netbeans.lib.awtextra.AbsoluteConstraints(480, 500, 280, -1));

        jLabel21.setFont(new java.awt.Font("Times New Roman", 1, 18)); // NOI18N
        jLabel21.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel21.setText("TRANSACCIONES:");
        jPanel6.add(jLabel21, new org.netbeans.lib.awtextra.AbsoluteConstraints(-10, 500, 280, -1));

        btnEliminarPedido.setBackground(new java.awt.Color(220, 20, 60));
        btnEliminarPedido.setForeground(new java.awt.Color(255, 255, 255));
        btnEliminarPedido.setText("Eliminar pedido");
        btnEliminarPedido.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEliminarPedidoActionPerformed(evt);
            }
        });
        jPanel6.add(btnEliminarPedido, new org.netbeans.lib.awtextra.AbsoluteConstraints(770, 520, 110, 40));

        BtnImprimirDia.setBackground(new java.awt.Color(46, 204, 113));
        BtnImprimirDia.setForeground(new java.awt.Color(255, 255, 255));
        BtnImprimirDia.setText("Imprimir total del dia");
        BtnImprimirDia.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnImprimirDiaActionPerformed(evt);
            }
        });
        jPanel6.add(BtnImprimirDia, new org.netbeans.lib.awtextra.AbsoluteConstraints(900, 520, 110, 40));

        txtPedidosDia.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        txtPedidosDia.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtPedidosDiaActionPerformed(evt);
            }
        });
        jPanel6.add(txtPedidosDia, new org.netbeans.lib.awtextra.AbsoluteConstraints(540, 530, 160, 40));

        jLabel22.setFont(new java.awt.Font("Times New Roman", 1, 18)); // NOI18N
        jLabel22.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel22.setText("EFECTIVO:");
        jPanel6.add(jLabel22, new org.netbeans.lib.awtextra.AbsoluteConstraints(260, 500, 280, -1));

        jTabbedPane1.addTab("Historial Pedidos", jPanel6);

        jPanel7.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel32.setFont(new java.awt.Font("Times New Roman", 1, 18)); // NOI18N
        jLabel32.setText("DATOS DE LA EMPRESA");
        jPanel7.add(jLabel32, new org.netbeans.lib.awtextra.AbsoluteConstraints(390, 40, -1, -1));

        jPanel8.setBackground(new java.awt.Color(70, 73, 75));
        jPanel8.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        txtIdConfig.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtIdConfigActionPerformed(evt);
            }
        });
        jPanel8.add(txtIdConfig, new org.netbeans.lib.awtextra.AbsoluteConstraints(350, 410, 24, -1));

        jLabel30.setFont(new java.awt.Font("Times New Roman", 3, 14)); // NOI18N
        jLabel30.setText("Dirección");
        jPanel8.add(jLabel30, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 160, -1, -1));

        jLabel29.setFont(new java.awt.Font("Times New Roman", 3, 14)); // NOI18N
        jLabel29.setText("Teléfono");
        jPanel8.add(jLabel29, new org.netbeans.lib.awtextra.AbsoluteConstraints(190, 160, -1, -1));

        txtTelefonoConfig.setBackground(new java.awt.Color(204, 204, 204));
        txtTelefonoConfig.setForeground(new java.awt.Color(0, 0, 0));
        txtTelefonoConfig.setBorder(null);
        jPanel8.add(txtTelefonoConfig, new org.netbeans.lib.awtextra.AbsoluteConstraints(190, 190, 218, 30));

        txtDireccionConfig.setBackground(new java.awt.Color(204, 204, 204));
        txtDireccionConfig.setForeground(new java.awt.Color(0, 0, 0));
        txtDireccionConfig.setBorder(null);
        jPanel8.add(txtDireccionConfig, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 190, 147, 30));

        jLabel31.setFont(new java.awt.Font("Times New Roman", 3, 14)); // NOI18N
        jLabel31.setText("Mensaje");
        jPanel8.add(jLabel31, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 290, -1, -1));

        txtMensaje.setBackground(new java.awt.Color(204, 204, 204));
        txtMensaje.setForeground(new java.awt.Color(0, 0, 0));
        txtMensaje.setBorder(null);
        jPanel8.add(txtMensaje, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 320, 400, 30));

        btnActualizarConfig.setBackground(new java.awt.Color(255, 255, 255));
        btnActualizarConfig.setFont(new java.awt.Font("Times New Roman", 1, 13)); // NOI18N
        btnActualizarConfig.setForeground(new java.awt.Color(0, 0, 0));
        btnActualizarConfig.setText("Modificar");
        btnActualizarConfig.setBorder(null);
        btnActualizarConfig.setFocusable(false);
        btnActualizarConfig.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnActualizarConfig.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnActualizarConfig.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnActualizarConfigActionPerformed(evt);
            }
        });
        jPanel8.add(btnActualizarConfig, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 405, 220, 50));

        jLabel27.setFont(new java.awt.Font("Times New Roman", 3, 14)); // NOI18N
        jLabel27.setText("Ruc");
        jPanel8.add(jLabel27, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 30, -1, -1));

        txtRucConfig.setBackground(new java.awt.Color(204, 204, 204));
        txtRucConfig.setForeground(new java.awt.Color(0, 0, 0));
        txtRucConfig.setBorder(null);
        jPanel8.add(txtRucConfig, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 60, 147, 30));

        jLabel28.setFont(new java.awt.Font("Times New Roman", 3, 14)); // NOI18N
        jLabel28.setText("Nombre");
        jPanel8.add(jLabel28, new org.netbeans.lib.awtextra.AbsoluteConstraints(190, 30, -1, -1));

        txtNombreConfig.setBackground(new java.awt.Color(204, 204, 204));
        txtNombreConfig.setForeground(new java.awt.Color(0, 0, 0));
        txtNombreConfig.setBorder(null);
        jPanel8.add(txtNombreConfig, new org.netbeans.lib.awtextra.AbsoluteConstraints(190, 60, 220, 30));

        jPanel41.setBackground(new java.awt.Color(0, 0, 0));

        javax.swing.GroupLayout jPanel41Layout = new javax.swing.GroupLayout(jPanel41);
        jPanel41.setLayout(jPanel41Layout);
        jPanel41Layout.setHorizontalGroup(
                jPanel41Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 0, Short.MAX_VALUE)
        );
        jPanel41Layout.setVerticalGroup(
                jPanel41Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 0, Short.MAX_VALUE)
        );

        jPanel8.add(jPanel41, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 90, 147, 2));

        jPanel42.setBackground(new java.awt.Color(0, 0, 0));

        javax.swing.GroupLayout jPanel42Layout = new javax.swing.GroupLayout(jPanel42);
        jPanel42.setLayout(jPanel42Layout);
        jPanel42Layout.setHorizontalGroup(
                jPanel42Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 0, Short.MAX_VALUE)
        );
        jPanel42Layout.setVerticalGroup(
                jPanel42Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 0, Short.MAX_VALUE)
        );

        jPanel8.add(jPanel42, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 220, 147, 2));

        jPanel43.setBackground(new java.awt.Color(0, 0, 0));

        javax.swing.GroupLayout jPanel43Layout = new javax.swing.GroupLayout(jPanel43);
        jPanel43.setLayout(jPanel43Layout);
        jPanel43Layout.setHorizontalGroup(
                jPanel43Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 0, Short.MAX_VALUE)
        );
        jPanel43Layout.setVerticalGroup(
                jPanel43Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 0, Short.MAX_VALUE)
        );

        jPanel8.add(jPanel43, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 350, 400, 2));

        jPanel44.setBackground(new java.awt.Color(0, 0, 0));

        javax.swing.GroupLayout jPanel44Layout = new javax.swing.GroupLayout(jPanel44);
        jPanel44.setLayout(jPanel44Layout);
        jPanel44Layout.setHorizontalGroup(
                jPanel44Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 0, Short.MAX_VALUE)
        );
        jPanel44Layout.setVerticalGroup(
                jPanel44Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 0, Short.MAX_VALUE)
        );

        jPanel8.add(jPanel44, new org.netbeans.lib.awtextra.AbsoluteConstraints(190, 90, 220, 2));

        jPanel45.setBackground(new java.awt.Color(0, 0, 0));

        javax.swing.GroupLayout jPanel45Layout = new javax.swing.GroupLayout(jPanel45);
        jPanel45.setLayout(jPanel45Layout);
        jPanel45Layout.setHorizontalGroup(
                jPanel45Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 0, Short.MAX_VALUE)
        );
        jPanel45Layout.setVerticalGroup(
                jPanel45Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 0, Short.MAX_VALUE)
        );

        jPanel8.add(jPanel45, new org.netbeans.lib.awtextra.AbsoluteConstraints(190, 220, 220, 2));

        javax.swing.GroupLayout jPanel40Layout = new javax.swing.GroupLayout(jPanel40);
        jPanel40.setLayout(jPanel40Layout);
        jPanel40Layout.setHorizontalGroup(
                jPanel40Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 100, Short.MAX_VALUE)
        );
        jPanel40Layout.setVerticalGroup(
                jPanel40Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 10, Short.MAX_VALUE)
        );

        jPanel8.add(jPanel40, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 70, -1, -1));

        jPanel7.add(jPanel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(250, 100, 420, 470));

        jTabbedPane1.addTab("Datos de la Empresa", jPanel7);

        jPanel12.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        TableUsuarios.setModel(new javax.swing.table.DefaultTableModel(
                new Object[][]{},
                new String[]{
                    "Id", "Nombre", "Correo", "Rol"
                }
        ));
        TableUsuarios.setRowHeight(23);
        jScrollPane6.setViewportView(TableUsuarios);
        if (TableUsuarios.getColumnModel().getColumnCount() > 0) {
            TableUsuarios.getColumnModel().getColumn(0).setMinWidth(50);
            TableUsuarios.getColumnModel().getColumn(0).setPreferredWidth(50);
            TableUsuarios.getColumnModel().getColumn(0).setMaxWidth(80);
            TableUsuarios.getColumnModel().getColumn(3).setMinWidth(150);
            TableUsuarios.getColumnModel().getColumn(3).setPreferredWidth(150);
            TableUsuarios.getColumnModel().getColumn(3).setMaxWidth(200);
        }

        jPanel12.add(jScrollPane6, new org.netbeans.lib.awtextra.AbsoluteConstraints(410, 40, 660, 520));

        jPanel15.setBackground(new java.awt.Color(70, 73, 75));
        jPanel15.setForeground(new java.awt.Color(70, 73, 75));
        jPanel15.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel34.setFont(new java.awt.Font("Times New Roman", 3, 14)); // NOI18N
        jLabel34.setText("Correo Electrónico");
        jPanel15.add(jLabel34, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 102, -1, -1));

        jLabel35.setFont(new java.awt.Font("Times New Roman", 3, 14)); // NOI18N
        jLabel35.setText("Password");
        jPanel15.add(jLabel35, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 180, 130, -1));

        txtCorreo.setBackground(new java.awt.Color(204, 204, 204));
        txtCorreo.setForeground(new java.awt.Color(0, 0, 0));
        txtCorreo.setBorder(null);
        txtCorreo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtCorreoActionPerformed(evt);
            }
        });
        jPanel15.add(txtCorreo, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 126, 300, 30));

        txtPass.setBackground(new java.awt.Color(204, 204, 204));
        txtPass.setForeground(new java.awt.Color(0, 0, 0));
        txtPass.setBorder(null);
        jPanel15.add(txtPass, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 200, 300, 30));

        btnIniciar.setBackground(new java.awt.Color(0, 0, 0));
        btnIniciar.setFont(new java.awt.Font("Times New Roman", 1, 13)); // NOI18N
        btnIniciar.setForeground(new java.awt.Color(255, 255, 255));
        btnIniciar.setText("Registrar");
        btnIniciar.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        btnIniciar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnIniciarActionPerformed(evt);
            }
        });
        jPanel15.add(btnIniciar, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 450, 130, 40));

        jLabel36.setFont(new java.awt.Font("Times New Roman", 3, 14)); // NOI18N
        jLabel36.setText("Nombre:");
        jPanel15.add(jLabel36, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 280, -1, -1));

        txtNombre.setBackground(new java.awt.Color(204, 204, 204));
        txtNombre.setForeground(new java.awt.Color(0, 0, 0));
        txtNombre.setBorder(null);
        jPanel15.add(txtNombre, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 300, 300, 30));

        jLabel37.setFont(new java.awt.Font("Times New Roman", 3, 14)); // NOI18N
        jLabel37.setText("Rol:");
        jPanel15.add(jLabel37, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 360, 90, -1));

        cbxRol.setModel(new javax.swing.DefaultComboBoxModel<>(new String[]{"Administrador", "Asistente"}));
        jPanel15.add(cbxRol, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 380, 300, 30));

        jPanel16.setBackground(new java.awt.Color(0, 0, 0));

        javax.swing.GroupLayout jPanel16Layout = new javax.swing.GroupLayout(jPanel16);
        jPanel16.setLayout(jPanel16Layout);
        jPanel16Layout.setHorizontalGroup(
                jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 300, Short.MAX_VALUE)
        );
        jPanel16Layout.setVerticalGroup(
                jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 2, Short.MAX_VALUE)
        );

        jPanel15.add(jPanel16, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 156, 300, 2));

        jPanel17.setBackground(new java.awt.Color(0, 0, 0));

        javax.swing.GroupLayout jPanel17Layout = new javax.swing.GroupLayout(jPanel17);
        jPanel17.setLayout(jPanel17Layout);
        jPanel17Layout.setHorizontalGroup(
                jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 300, Short.MAX_VALUE)
        );
        jPanel17Layout.setVerticalGroup(
                jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 2, Short.MAX_VALUE)
        );

        jPanel15.add(jPanel17, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 230, 300, 2));

        jPanel18.setBackground(new java.awt.Color(0, 0, 0));

        javax.swing.GroupLayout jPanel18Layout = new javax.swing.GroupLayout(jPanel18);
        jPanel18.setLayout(jPanel18Layout);
        jPanel18Layout.setHorizontalGroup(
                jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 300, Short.MAX_VALUE)
        );
        jPanel18Layout.setVerticalGroup(
                jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 2, Short.MAX_VALUE)
        );

        jPanel15.add(jPanel18, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 330, 300, 2));

        jPanel21.setBackground(new java.awt.Color(0, 0, 0));
        jPanel21.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel39.setFont(new java.awt.Font("Times New Roman", 1, 18)); // NOI18N
        jLabel39.setForeground(new java.awt.Color(255, 255, 255));
        jLabel39.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel39.setText("Nuevo Usuario");
        jPanel21.add(jLabel39, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 310, 35));

        jPanel15.add(jPanel21, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 360, 35));

        btnModificarUsua.setBackground(new java.awt.Color(0, 0, 0));
        btnModificarUsua.setFont(new java.awt.Font("Times New Roman", 1, 13)); // NOI18N
        btnModificarUsua.setForeground(new java.awt.Color(255, 255, 255));
        btnModificarUsua.setText(" Modificar");
        btnModificarUsua.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        btnModificarUsua.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnModificarUsuaActionPerformed(evt);
            }
        });
        jPanel15.add(btnModificarUsua, new org.netbeans.lib.awtextra.AbsoluteConstraints(180, 450, 140, 40));

        jPanel12.add(jPanel15, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 40, 360, 520));

        jTabbedPane1.addTab("Usuarios", jPanel12);

        jPanel11.setBackground(new java.awt.Color(70, 73, 75));
        jPanel11.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel23.setFont(new java.awt.Font("Tahoma", 3, 12)); // NOI18N
        jLabel23.setText("Nombre:");
        jPanel11.add(jLabel23, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 110, -1, -1));

        txtNombrePlato.setBackground(new java.awt.Color(204, 204, 204));
        txtNombrePlato.setBorder(null);
        jPanel11.add(txtNombrePlato, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 100, 170, 30));

        jLabel25.setFont(new java.awt.Font("Tahoma", 3, 12)); // NOI18N
        jLabel25.setText("Precio:");
        jPanel11.add(jLabel25, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 180, -1, -1));

        txtPrecioPlato.setBackground(new java.awt.Color(204, 204, 204));
        txtPrecioPlato.setBorder(null);
        txtPrecioPlato.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txtPrecioPlatoKeyTyped(evt);
            }
        });
        jPanel11.add(txtPrecioPlato, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 170, 170, 30));

        btnGuardarPlato.setBackground(new java.awt.Color(46, 204, 113));
        btnGuardarPlato.setForeground(new java.awt.Color(255, 255, 255));
        btnGuardarPlato.setText("GUARDAR");
        btnGuardarPlato.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGuardarPlatoActionPerformed(evt);
            }
        });
        jPanel11.add(btnGuardarPlato, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 270, 110, 50));

        btnEditarPlato.setText("EDITAR");
        btnEditarPlato.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEditarPlatoActionPerformed(evt);
            }
        });
        jPanel11.add(btnEditarPlato, new org.netbeans.lib.awtextra.AbsoluteConstraints(167, 270, 100, 50));

        btnEliminarPlato.setBackground(new java.awt.Color(220, 20, 60));
        btnEliminarPlato.setForeground(new java.awt.Color(255, 255, 255));
        btnEliminarPlato.setText("ELIMINAR");
        btnEliminarPlato.setEnabled(false);
        btnEliminarPlato.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEliminarPlatoActionPerformed(evt);
            }
        });
        jPanel11.add(btnEliminarPlato, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 370, 110, 50));

        btnNuevoPlato.setText("NUEVO");
        btnNuevoPlato.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNuevoPlatoActionPerformed(evt);
            }
        });
        jPanel11.add(btnNuevoPlato, new org.netbeans.lib.awtextra.AbsoluteConstraints(170, 370, 100, 50));

        jPanel31.setBackground(new java.awt.Color(0, 0, 0));

        javax.swing.GroupLayout jPanel31Layout = new javax.swing.GroupLayout(jPanel31);
        jPanel31.setLayout(jPanel31Layout);
        jPanel31Layout.setHorizontalGroup(
                jPanel31Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 0, Short.MAX_VALUE)
        );
        jPanel31Layout.setVerticalGroup(
                jPanel31Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 0, Short.MAX_VALUE)
        );

        jPanel11.add(jPanel31, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 130, 170, 2));

        jPanel33.setBackground(new java.awt.Color(0, 0, 0));

        javax.swing.GroupLayout jPanel33Layout = new javax.swing.GroupLayout(jPanel33);
        jPanel33.setLayout(jPanel33Layout);
        jPanel33Layout.setHorizontalGroup(
                jPanel33Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 0, Short.MAX_VALUE)
        );
        jPanel33Layout.setVerticalGroup(
                jPanel33Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 0, Short.MAX_VALUE)
        );

        jPanel11.add(jPanel33, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 200, 170, 2));

        jPanel39.setBackground(new java.awt.Color(0, 0, 0));

        jLabel40.setFont(new java.awt.Font("Times New Roman", 1, 18)); // NOI18N
        jLabel40.setForeground(new java.awt.Color(255, 255, 255));
        jLabel40.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel40.setText("Platos del Día");

        javax.swing.GroupLayout jPanel39Layout = new javax.swing.GroupLayout(jPanel39);
        jPanel39.setLayout(jPanel39Layout);
        jPanel39Layout.setHorizontalGroup(
                jPanel39Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jLabel40, javax.swing.GroupLayout.DEFAULT_SIZE, 290, Short.MAX_VALUE)
        );
        jPanel39Layout.setVerticalGroup(
                jPanel39Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel39Layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(jLabel40, javax.swing.GroupLayout.DEFAULT_SIZE, 24, Short.MAX_VALUE)
                                .addContainerGap())
        );

        jPanel11.add(jPanel39, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 290, 50));
        jPanel11.add(txtIdPlato, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 470, 80, -1));

        TablePlatos.setForeground(new java.awt.Color(255, 255, 255));
        TablePlatos.setModel(new javax.swing.table.DefaultTableModel(
                new Object[][]{},
                new String[]{
                    "ID", "DESCRIPCIÓN", "PRECIO"
                }
        ));
        TablePlatos.setRowHeight(23);
        TablePlatos.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                TablePlatosMouseClicked(evt);
            }
        });
        jScrollPane4.setViewportView(TablePlatos);
        if (TablePlatos.getColumnModel().getColumnCount() > 0) {
            TablePlatos.getColumnModel().getColumn(0).setMinWidth(100);
            TablePlatos.getColumnModel().getColumn(0).setPreferredWidth(100);
            TablePlatos.getColumnModel().getColumn(0).setMaxWidth(150);
            TablePlatos.getColumnModel().getColumn(2).setMinWidth(200);
            TablePlatos.getColumnModel().getColumn(2).setPreferredWidth(200);
            TablePlatos.getColumnModel().getColumn(2).setMaxWidth(300);
        }

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
                jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGap(26, 26, 26)
                                .addComponent(jPanel11, javax.swing.GroupLayout.PREFERRED_SIZE, 289, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(30, 30, 30)
                                .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 729, Short.MAX_VALUE)
                                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
                jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGap(25, 25, 25)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addComponent(jScrollPane4)
                                        .addComponent(jPanel11, javax.swing.GroupLayout.DEFAULT_SIZE, 543, Short.MAX_VALUE))
                                .addContainerGap(22, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Platos", jPanel2);
        jTabbedPane1.addTab("", jLabel_wallpaper);

        getContentPane().add(jTabbedPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 95, 1080, 620));

        BtnCerrarSesion.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/cerrar-sesion.png"))); // NOI18N
        BtnCerrarSesion.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnCerrarSesionActionPerformed(evt);
            }
        });
        getContentPane().add(BtnCerrarSesion, new org.netbeans.lib.awtextra.AbsoluteConstraints(1180, 10, 90, 90));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnSalaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSalaActionPerformed
        // TODO add your handling code here:
        LimpiarTable();
        ListarSalas();
        LimpiarTableMenu();
        LimpiarPlatos();
        jTabbedPane1.setSelectedIndex(1);
    }//GEN-LAST:event_btnSalaActionPerformed

    private void btnConfigActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnConfigActionPerformed
        // TODO add your handling code here:
        jTabbedPane1.setSelectedIndex(6);
        ListarConfig();
        LimpiarTableMenu();
        LimpiarPlatos();

    }//GEN-LAST:event_btnConfigActionPerformed

    private void btnVentasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnVentasActionPerformed
        // TODO add your handling code here:
        LimpiarTable();
        ListarPedidos();
        actualizarTotalDia(); // Llamar aquí para actualizar el total del día
        LimpiarTableMenu();
        LimpiarPlatos();

        jTabbedPane1.setSelectedIndex(5);

    }//GEN-LAST:event_btnVentasActionPerformed

    private void btnUsuariosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUsuariosActionPerformed
        // TODO add your handling code here:
        LimpiarTable();
        ListarUsuarios();
        LimpiarTableMenu();
        LimpiarPlatos();

        jTabbedPane1.setSelectedIndex(7);
    }//GEN-LAST:event_btnUsuariosActionPerformed

    private void labelLogoMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_labelLogoMouseClicked
        jTabbedPane1.setSelectedIndex(0);
        PanelSalas.removeAll();
        LimpiarTableMenu();
        LimpiarPlatos();

        panelSalas();
    }//GEN-LAST:event_labelLogoMouseClicked

    private void btnPlatosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPlatosActionPerformed
        jTabbedPane1.setSelectedIndex(8);
        LimpiarTable();
        LimpiarTableMenu();
        LimpiarPlatos();

        ListarPlatos(TablePlatos);
    }//GEN-LAST:event_btnPlatosActionPerformed

    private void TablePlatosMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_TablePlatosMouseClicked
        int fila = TablePlatos.rowAtPoint(evt.getPoint());
        if (fila >= 0) {
            txtIdPlato.setText(TablePlatos.getValueAt(fila, 0).toString());
            txtNombrePlato.setText(TablePlatos.getValueAt(fila, 1).toString());
            txtPrecioPlato.setText(TablePlatos.getValueAt(fila, 2).toString());
            // Indicar visualmente que está en modo edición
            btnGuardarPlato.setText("✏️ Modificar");
            btnGuardarPlato.setBackground(new java.awt.Color(41, 128, 185)); // Azul = editar
        }
    }//GEN-LAST:event_TablePlatosMouseClicked

    private void btnNuevoPlatoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNuevoPlatoActionPerformed
        // TODO add your handling code here:
        LimpiarPlatos();
    }//GEN-LAST:event_btnNuevoPlatoActionPerformed

    private void btnEliminarPlatoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEliminarPlatoActionPerformed
        System.out.println("Botón Eliminar Plato presionado"); // Depuración

        // Forzar la actualización de la selección
        int fila = tableMenu.getSelectedRow();
        if (fila != -1) {
            tableMenu.setRowSelectionInterval(fila, fila); // Forzar la selección de la fila actual
        }
        System.out.println("Fila seleccionada: " + fila); // Depuración

        if (fila == -1) {
            JOptionPane.showMessageDialog(this,
                    "Seleccione un plato de la tabla",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Obtener el ID del plato seleccionado
        int id;
        try {
            id = Integer.parseInt(tableMenu.getValueAt(fila, 0).toString());
            System.out.println("ID del plato seleccionado: " + id); // Depuración
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                    "ID de plato inválido: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Obtener el nombre del plato para el mensaje de confirmación
        String nombrePlato = tableMenu.getValueAt(fila, 1).toString();
        System.out.println("Nombre del plato: " + nombrePlato); // Depuración

        // Confirmar la eliminación
        int confirmacion = JOptionPane.showConfirmDialog(this,
                "¿Desea eliminar el plato '" + nombrePlato + "'?",
                "Eliminar Plato",
                JOptionPane.YES_NO_OPTION);

        if (confirmacion == JOptionPane.YES_OPTION) {
            // Eliminar el plato usando PlatosDao
            PlatosDao platosDao = new PlatosDao();
            boolean eliminado = platosDao.Eliminar(id);
            System.out.println("Resultado de eliminación: " + eliminado); // Depuración

            if (eliminado) {
                JOptionPane.showMessageDialog(this,
                        "Plato eliminado correctamente",
                        "Éxito",
                        JOptionPane.INFORMATION_MESSAGE);
                // Actualizar la tabla
                cargarTablaPlatos();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Error al eliminar el plato",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_btnEliminarPlatoActionPerformed

    private void btnEditarPlatoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEditarPlatoActionPerformed
        if ("".equals(txtIdPlato.getText())) {
            JOptionPane.showMessageDialog(null, "Primero haga clic en un plato de la tabla", "Aviso", JOptionPane.WARNING_MESSAGE);
        } else {
            // El botón Guardar ya maneja edición automáticamente cuando hay un ID
            // Sólo enfocar el campo nombre para facilitar la edición
            txtNombrePlato.requestFocus();
            txtNombrePlato.selectAll();
        }
    }//GEN-LAST:event_btnEditarPlatoActionPerformed

    private void btnGuardarPlatoActionPerformed(java.awt.event.ActionEvent evt) {
        if (txtNombrePlato.getText().isEmpty() || txtPrecioPlato.getText().isEmpty()) {
            JOptionPane.showMessageDialog(null, "Los campos están vacíos");
            return;
        }
        try {
            pla.setNombre(txtNombrePlato.getText());
            pla.setPrecio(Double.parseDouble(txtPrecioPlato.getText()));
            pla.setFecha(fechaFormato);

            if (!"".equals(txtIdPlato.getText())) {
                // Modo Edición: El usuario hizo clic en la tabla y modificó algo
                pla.setId(Integer.parseInt(txtIdPlato.getText()));
                if (plaDao.Modificar(pla)) {
                    mostrarExitoEnBoton(btnGuardarPlato, "GUARDAR", "✅ Modificado");
                    LimpiarTable();
                    ListarPlatos(TablePlatos);
                    LimpiarPlatos();
                } else {
                    JOptionPane.showMessageDialog(null, "Error al modificar el plato");
                }
            } else {
                // Modo Registro: Plato nuevo
                if (plaDao.Registrar(pla)) {
                    mostrarExitoEnBoton(btnGuardarPlato, "GUARDAR", "✅ Guardado");
                    LimpiarTable();
                    ListarPlatos(TablePlatos);
                    LimpiarPlatos();
                } else {
                    JOptionPane.showMessageDialog(null, "Error al registrar el plato");
                }
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "El precio debe ser un número válido");
        }
    }

    private void txtPrecioPlatoKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtPrecioPlatoKeyTyped
        // TODO add your handling code here:
        event.numberDecimalKeyPress(evt, txtPrecioPlato);
    }//GEN-LAST:event_txtPrecioPlatoKeyTyped

    private void btnIniciarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnIniciarActionPerformed
        if (txtNombre.getText().equals("") || txtCorreo.getText().equals("") || txtPass.getPassword().equals("")) {
            JOptionPane.showMessageDialog(null, "Todo los campos son requeridos");
        } else {
            login lg = new login();
            String correo = txtCorreo.getText();
            String pass = String.valueOf(txtPass.getPassword());
            String nom = txtNombre.getText();
            String rol = cbxRol.getSelectedItem().toString();
            lg.setNombre(nom);
            lg.setCorreo(correo);
            lg.setPass(pass);
            lg.setRol(rol);
            lgDao.Registrar(lg);
            JOptionPane.showMessageDialog(null, "Usuario Registrado");
        }
    }//GEN-LAST:event_btnIniciarActionPerformed

    private void txtCorreoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtCorreoActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtCorreoActionPerformed

    private void btnActualizarConfigActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnActualizarConfigActionPerformed
        // TODO add your handling code here:
        if (!"".equals(txtRucConfig.getText()) || !"".equals(txtNombreConfig.getText()) || !"".equals(txtTelefonoConfig.getText()) || !"".equals(txtDireccionConfig.getText())) {
            conf.setRuc(txtRucConfig.getText());
            conf.setNombre(txtNombreConfig.getText());
            conf.setTelefono(txtTelefonoConfig.getText());
            conf.setDireccion(txtDireccionConfig.getText());
            conf.setMensaje(txtMensaje.getText());
            conf.setId(Integer.parseInt(txtIdConfig.getText()));
            lgDao.ModificarDatos(conf);
            JOptionPane.showMessageDialog(null, "Datos de la empresa modificado");
            //ListarConfig();
        } else {
            JOptionPane.showMessageDialog(null, "Los campos estan vacios");
        }
    }//GEN-LAST:event_btnActualizarConfigActionPerformed

    private void txtIdConfigActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtIdConfigActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtIdConfigActionPerformed

    private void TablePedidosMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_TablePedidosMouseClicked
        // TODO add your handling code here:
        int fila = TablePedidos.rowAtPoint(evt.getPoint());
        int id_pedido = Integer.parseInt(TablePedidos.getValueAt(fila, 0).toString());
        LimpiarTable();
        verPedido(id_pedido);
        verPedidoDetalle(id_pedido);
        jTabbedPane1.setSelectedIndex(4);
//        btnFinalizar.setEnabled(false);
        txtIdHistorialPedido.setText("" + id_pedido);
    }//GEN-LAST:event_TablePedidosMouseClicked

    private void btnAddPlatoFinalizarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddPlatoFinalizarActionPerformed
        if (txtIdPedido.getText().isEmpty()) {
            JOptionPane.showMessageDialog(null, "No hay pedido seleccionado");
            return;
        }
        String idPlatoStr = JOptionPane.showInputDialog(null, "Ingresa el ID del plato:");
        if (idPlatoStr == null || idPlatoStr.trim().isEmpty()) {
            JOptionPane.showMessageDialog(null, "Ingresa un ID válido");
            return;
        }
        try {
            int idPedido = Integer.parseInt(txtIdPedido.getText());
            int idPlato = Integer.parseInt(idPlatoStr);
            PlatosDao platosDao = new PlatosDao();
            Platos plato = platosDao.buscarPorId(idPlato);
            if (plato != null) {
                double precio = plato.getPrecio();
                String nombre = plato.getNombre();
                double total = 1.0 * precio;
                item++;
                modelo = (DefaultTableModel) tableFinalizar.getModel();
                DetallePedido det = new DetallePedido();
                det.setNombre(nombre);
                det.setPrecio(precio);
                det.setCantidad(1);
                det.setComentario("");
                det.setId_pedido(idPedido);
                if (pedDao.RegistrarDetalle(det)) {
                    verPedidoDetalle(idPedido);
                    TotalPagar(tableFinalizar, totalFinalizar);
                    JOptionPane.showMessageDialog(null, "Plato agregado al pedido");
                } else {
                    JOptionPane.showMessageDialog(null, "Error al agregar el plato");
                }
            } else {
                JOptionPane.showMessageDialog(null, "Plato no encontrado con ID: " + idPlato);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "El ID debe ser un número válido");
        }
    }//GEN-LAST:event_btnAddPlatoFinalizarActionPerformed

    private void btnEliminarPlatoFinalizarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEliminarPlatoFinalizarActionPerformed
        if (txtIdPedido.getText().isEmpty()) {
            JOptionPane.showMessageDialog(null, "No hay pedido seleccionado");
            return;
        }
        if (tableFinalizar.getSelectedRow() >= 0) {
            try {
                int idPedido = Integer.parseInt(txtIdPedido.getText());
                int idDetalle = Integer.parseInt(tableFinalizar.getValueAt(tableFinalizar.getSelectedRow(), 0).toString());
                if (pedDao.eliminarDetalle(idDetalle)) {
                    modelo = (DefaultTableModel) tableFinalizar.getModel();
                    modelo.removeRow(tableFinalizar.getSelectedRow());
                    TotalPagar(tableFinalizar, totalFinalizar);
                    JOptionPane.showMessageDialog(null, "Plato eliminado del pedido");
                } else {
                    JOptionPane.showMessageDialog(null, "Error al eliminar el plato");
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "Error: ID inválido");
            }
        } else {
            JOptionPane.showMessageDialog(null, "Seleccione una fila");
        }
    }//GEN-LAST:event_btnEliminarPlatoFinalizarActionPerformed

    private void btnPdfPedidoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPdfPedidoActionPerformed
        if (txtIdHistorialPedido.getText().equals("")) {
            JOptionPane.showMessageDialog(null, "Selecciona una fila");
        } else {
            final int idPedido = Integer.parseInt(txtIdHistorialPedido.getText());
            btnPdfPedido.setEnabled(false);
            btnPdfPedido.setText("Generando...");
            new javax.swing.SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() {
                    pedDao.pdfPedido(idPedido);
                    return null;
                }

                @Override
                protected void done() {
                    btnPdfPedido.setEnabled(true);
                    btnPdfPedido.setText("PDF Pedido");
                    txtIdHistorialPedido.setText("");
                    JOptionPane.showMessageDialog(null, "✅ PDF generado correctamente.");
                }
            }.execute();
        }
    }//GEN-LAST:event_btnPdfPedidoActionPerformed

    private void btnFinalizarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnFinalizarActionPerformed
        if (txtIdPedido.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "No hay pedido seleccionado.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int pregunta = JOptionPane.showConfirmDialog(null, "Esta seguro de finalizar");
        if (pregunta == 0) {
            final int idPedido = Integer.parseInt(txtIdPedido.getText());
            btnFinalizar.setEnabled(false);
            btnFinalizar.setText("⏳ Procesando...");
            new javax.swing.SwingWorker<Boolean, Void>() {
                @Override
                protected Boolean doInBackground() {
                    boolean ok = pedDao.actualizarEstado(idPedido);
                    if (ok) {
                        pedDao.pdfPedido(idPedido);
                        PedidosDao pedidosDao = new PedidosDao();
                        pedidosDao.generarReporteDiario();
                    }
                    return ok;
                }

                @Override
                protected void done() {
                    try {
                        boolean ok = get();
                        btnFinalizar.setEnabled(true);
                        btnFinalizar.setText("Finalizar");
                        if (ok) {
                            LimpiarTable();
                            ListarPedidos();
                            actualizarTotalDia();
                            JOptionPane.showMessageDialog(Sistema.this, "✅ Pedido finalizado.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                        } else {
                            JOptionPane.showMessageDialog(Sistema.this, "Error al finalizar el pedido.", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (Exception e) {
                        btnFinalizar.setEnabled(true);
                        btnFinalizar.setText("Finalizar");
                        JOptionPane.showMessageDialog(Sistema.this, "Error inesperado: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }.execute();
        }
    }//GEN-LAST:event_btnFinalizarActionPerformed

    private void btnEliminarTempPlatoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEliminarTempPlatoActionPerformed
        modelo = (DefaultTableModel) tableMenu.getModel();
        modelo.removeRow(tableMenu.getSelectedRow());
        TotalPagar(tableMenu, totalMenu);
    }//GEN-LAST:event_btnEliminarTempPlatoActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        if (txtComentario.getText().equals("")) {
            JOptionPane.showMessageDialog(null, "SELECCIONE UNA FILA");
        } else {
            int id = Integer.parseInt(tableMenu.getValueAt(tableMenu.getSelectedRow(), 0).toString());
            for (int i = 0; i < tableMenu.getRowCount(); i++) {
                if (tableMenu.getValueAt(i, 0).equals(id)) {
                    tmp.setValueAt(txtComentario.getText(), i, 5);
                    txtComentario.setText("");
                    tableMenu.clearSelection();
                    return;
                }
            }
        }
    }//GEN-LAST:event_jButton2ActionPerformed

    private void btnGenerarPedidoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGenerarPedidoActionPerformed
        if (tableMenu.getRowCount() > 0) {
            RegistrarPedido();
            detallePedido();
            LimpiarTableMenu();
            JOptionPane.showMessageDialog(null, "PEDIDO REGISTRADO");
            jTabbedPane1.setSelectedIndex(0);
            LimpiarTable();
            ListarPedidos();
            actualizarTotalDia(); // Llamar aquí
        } else {
            JOptionPane.showMessageDialog(null, "NO HAY PRODUCTO EN LA PEDIDO");
        }
    }//GEN-LAST:event_btnGenerarPedidoActionPerformed

    private void btnAddPlatoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddPlatoActionPerformed
        if (tblTemPlatos.getSelectedRow() >= 0) {
            int id = Integer.parseInt(tblTemPlatos.getValueAt(tblTemPlatos.getSelectedRow(), 0).toString());
            String descripcion = tblTemPlatos.getValueAt(tblTemPlatos.getSelectedRow(), 1).toString();
            double precio = Double.parseDouble(tblTemPlatos.getValueAt(tblTemPlatos.getSelectedRow(), 2).toString());
            double total = 1 * precio;
            item = item + 1;
            tmp = (DefaultTableModel) tableMenu.getModel();
            for (int i = 0; i < tableMenu.getRowCount(); i++) {
                if (tableMenu.getValueAt(i, 0).equals(id)) {
                    int cantActual = Integer.parseInt(tableMenu.getValueAt(i, 2).toString());
                    int nuevoCantidad = cantActual + 1;
                    double nuevoSub = precio * nuevoCantidad;
                    tmp.setValueAt(nuevoCantidad, i, 2);
                    tmp.setValueAt(nuevoSub, i, 4);
                    TotalPagar(tableMenu, totalMenu);
                    return;
                }
            }
            ArrayList lista = new ArrayList();
            lista.add(item);
            lista.add(id);
            lista.add(descripcion);
            lista.add(1);
            lista.add(precio);
            lista.add(total);
            Object[] O = new Object[6];
            O[0] = lista.get(1);
            O[1] = lista.get(2);
            O[2] = lista.get(3);
            O[3] = lista.get(4);
            O[4] = lista.get(5);
            O[5] = "";
            tmp.addRow(O);
            tableMenu.setModel(tmp);
            TotalPagar(tableMenu, totalMenu);
        } else {
            JOptionPane.showMessageDialog(null, "SELECCIONA UNA FILA");
        }
    }//GEN-LAST:event_btnAddPlatoActionPerformed

    private void txtBuscarPlatoKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtBuscarPlatoKeyReleased
        LimpiarTable();
        ListarPlatos(tblTemPlatos);
    }//GEN-LAST:event_txtBuscarPlatoKeyReleased

    private void btnEliminarSalaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEliminarSalaActionPerformed
        // TODO add your handling code here:
        if (!"".equals(txtIdSala.getText())) {
            int pregunta = JOptionPane.showConfirmDialog(null, "Esta seguro de eliminar");
            if (pregunta == 0) {
                int id = Integer.parseInt(txtIdSala.getText());
                slDao.Eliminar(id);
                LimpiarSala();
                LimpiarTable();
                ListarSalas();
            }
        } else {
            JOptionPane.showMessageDialog(null, "Seleccione una fila");
        }
    }//GEN-LAST:event_btnEliminarSalaActionPerformed

    private void btnNuevoSalaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNuevoSalaActionPerformed
        // TODO add your handling code here:
        LimpiarSala();
    }//GEN-LAST:event_btnNuevoSalaActionPerformed

    private void btnActualizarSalaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnActualizarSalaActionPerformed
        // TODO add your handling code here:
        if ("".equals(txtIdSala.getText())) {
            JOptionPane.showMessageDialog(null, "Seleecione una fila");
        } else {
            if (!"".equals(txtNombreSala.getText())) {
                sl.setNombre(txtNombreSala.getText());
                sl.setId(Integer.parseInt(txtIdSala.getText()));
                slDao.Modificar(sl);
                JOptionPane.showMessageDialog(null, "Sala Modificado");
                LimpiarSala();
                LimpiarTable();
                ListarSalas();
            }
        }
    }//GEN-LAST:event_btnActualizarSalaActionPerformed

    private void btnRegistrarSalaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRegistrarSalaActionPerformed
        if (txtNombreSala.getText().isEmpty() || txtMesas.getText().isEmpty()) {
            JOptionPane.showMessageDialog(null, "Los campos están vacíos");
            return;
        }
        try {
            sl.setNombre(txtNombreSala.getText());
            sl.setMesas(Integer.parseInt(txtMesas.getText()));
            slDao.RegistrarSala(sl);

            // Éxito: Feedback visual en el botón en lugar de popup bloqueante
            mostrarExitoEnBoton(btnRegistrarSala, "REGISTRAR", "✅ Registrado");

            LimpiarSala();
            LimpiarTable();
            ListarSalas();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "El número de mesas debe ser un entero válido");
        }
    }//GEN-LAST:event_btnRegistrarSalaActionPerformed

    private void tableSalaMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tableSalaMouseClicked
        // TODO add your handling code here:
        int fila = tableSala.rowAtPoint(evt.getPoint());
        txtIdSala.setText(tableSala.getValueAt(fila, 0).toString());
        txtNombreSala.setText(tableSala.getValueAt(fila, 1).toString());
        txtMesas.setText(tableSala.getValueAt(fila, 2).toString());
    }//GEN-LAST:event_tableSalaMouseClicked

    private void txtTotalDiaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtTotalDiaActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtTotalDiaActionPerformed

    private void txtTotalDiaTransActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtTotalDiaTransActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtTotalDiaTransActionPerformed

    private void txtIdHistorialPedidoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtIdHistorialPedidoActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtIdHistorialPedidoActionPerformed

    private void btnImprimirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnImprimirActionPerformed
        if (txtIdPedido.getText().isEmpty()) {
            JOptionPane.showMessageDialog(null, "Seleccione un pedido para imprimir");
            return;
        }
        try {
            int idPedido = Integer.parseInt(txtIdPedido.getText());
            if (tableFinalizar.getRowCount() == 0) {
                JOptionPane.showMessageDialog(null, "El pedido no tiene detalles para imprimir");
                return;
            }
            ImpresionTicket impresion = new ImpresionTicket();
            impresion.imprimirTicket(idPedido, tableFinalizar);
            JOptionPane.showMessageDialog(null, "Ticket impreso correctamente");
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "ID de pedido inválido");
        }
    }//GEN-LAST:event_btnImprimirActionPerformed

    private void btnEliminarPedidoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEliminarPedidoActionPerformed
        String idInput = JOptionPane.showInputDialog(this, "Ingrese el ID del pedido a eliminar:", "Eliminar Pedido", JOptionPane.PLAIN_MESSAGE);
        if (idInput == null || idInput.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "No se ingresó un ID válido.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int idPedido;
        try {
            idPedido = Integer.parseInt(idInput.trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "El ID debe ser un número entero válido.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Verificar si el pedido existe
        PedidosDao pedidosDao = new PedidosDao();
        Pedidos pedido = pedidosDao.verPedido(idPedido);
        if (pedido.getId() == 0) {
            JOptionPane.showMessageDialog(this, "No se encontró un pedido con ID " + idPedido + ".", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int confirmacion = JOptionPane.showConfirmDialog(this,
                "¿Está seguro de que desea eliminar el pedido con ID " + idPedido + "?",
                "Confirmar Eliminación",
                JOptionPane.YES_NO_OPTION);

        if (confirmacion == JOptionPane.YES_OPTION) {
            boolean eliminado = pedidosDao.eliminarPedidoPorId(idPedido);
            if (eliminado) {
                cargarPedidosDelDia(); // Actualizar la tabla con el rango del día operativo
                JOptionPane.showMessageDialog(this, "Pedido eliminado correctamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Error al eliminar el pedido.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_btnEliminarPedidoActionPerformed

    private void btnTransaccionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTransaccionActionPerformed
        if (txtIdPedido.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "No hay pedido seleccionado", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String montoInput = JOptionPane.showInputDialog(this, "Ingrese el monto de la transacción:", "Monto Transacción", JOptionPane.PLAIN_MESSAGE);
        if (montoInput == null || montoInput.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "No se ingresó un monto.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            double monto = Double.parseDouble(montoInput.trim());
            if (monto <= 0) {
                JOptionPane.showMessageDialog(this, "El monto debe ser mayor a 0.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int idPedido = Integer.parseInt(txtIdPedido.getText());
            String comentario = "TRANSACCION - $" + String.format("%.2f", monto);
            DetallePedido det = new DetallePedido();
            det.setNombre("PAGO TRANSACCION");
            det.setPrecio(monto);
            det.setCantidad(1);
            det.setComentario(comentario);
            det.setId_pedido(idPedido);

            if (pedDao.RegistrarDetalle(det)) {
                modelo = (DefaultTableModel) tableFinalizar.getModel();
                Object[] fila = new Object[6];
                fila[0] = det.getId(); // ID del detalle (puede ser 0 si no se usa)
                fila[1] = "PAGO TRANSACCION";
                fila[2] = 1;
                fila[3] = monto;
                fila[4] = monto;
                fila[5] = comentario;
                modelo.addRow(fila);
                TotalPagar(tableFinalizar, totalFinalizar);
                JOptionPane.showMessageDialog(this, "Pago por transacción añadido.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Error al registrar el pago por transacción.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "El monto debe ser un número válido.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnTransaccionActionPerformed

    private void btnEfectivoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEfectivoActionPerformed

        if (txtIdPedido.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "No hay pedido seleccionado", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String montoInput = JOptionPane.showInputDialog(this, "Ingrese el monto en efectivo:", "Monto Efectivo", JOptionPane.PLAIN_MESSAGE);
        if (montoInput == null || montoInput.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "No se ingresó un monto.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            double monto = Double.parseDouble(montoInput.trim());
            if (monto <= 0) {
                JOptionPane.showMessageDialog(this, "El monto debe ser mayor a 0.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int idPedido = Integer.parseInt(txtIdPedido.getText());
            String comentario = "EFECTIVO - $" + String.format("%.2f", monto);
            DetallePedido det = new DetallePedido();
            det.setNombre("PAGO EFECTIVO");
            det.setPrecio(monto);
            det.setCantidad(1);
            det.setComentario(comentario);
            det.setId_pedido(idPedido);

            if (pedDao.RegistrarDetalle(det)) {
                modelo = (DefaultTableModel) tableFinalizar.getModel();
                Object[] fila = new Object[6];
                fila[0] = det.getId(); // ID del detalle (puede ser 0 si no se usa)
                fila[1] = "PAGO EFECTIVO";
                fila[2] = 1;
                fila[3] = monto;
                fila[4] = monto;
                fila[5] = comentario;
                modelo.addRow(fila);
                TotalPagar(tableFinalizar, totalFinalizar);
                JOptionPane.showMessageDialog(this, "Pago en efectivo añadido.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Error al registrar el pago en efectivo.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "El monto debe ser un número válido.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnEfectivoActionPerformed

    private void jComboSalasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboSalasActionPerformed
        if (txtIdPedido.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "No hay pedido seleccionado", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String salaSeleccionada = (String) jComboSalas.getSelectedItem();
        if (salaSeleccionada == null || salaSeleccionada.equals("Seleccionar")) {
            return;
        }
        // Validar si la sala seleccionada es diferente de la actual
        String salaActual = txtSalaFinalizar.getText();
        if (salaSeleccionada.equals(salaActual)) {
            return; // No hacer nada si la sala no cambió
        }
        try {
            int idPedido = Integer.parseInt(txtIdPedido.getText());
            int numMesa = Integer.parseInt(txtNumMesaFinalizar.getText());
            SalasDao salaDao = new SalasDao();
            int idSala = salaDao.buscarIdSalaPorNombre(salaSeleccionada);
            if (idSala == 0) {
                JOptionPane.showMessageDialog(this, "Sala no encontrada", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            // Verificar si la mesa está ocupada en la sala seleccionada
            PedidosDao pedDao = new PedidosDao();
            if (pedDao.mesaOcupada(idSala, numMesa, idPedido)) {
                JOptionPane.showMessageDialog(this, "La mesa " + numMesa + " en la sala " + salaSeleccionada + " ya está ocupada por otro pedido pendiente", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            // Actualizar la sala si la mesa está libre
            if (pedDao.actualizarSalaPedido(idPedido, idSala)) {
                txtSalaFinalizar.setText(salaSeleccionada);
                JOptionPane.showMessageDialog(this, "Sala actualizada a " + salaSeleccionada, "Éxito", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Error al actualizar la sala", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "ID de pedido o número de mesa inválido", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_jComboSalasActionPerformed

    private void BtnImprimirDiaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnImprimirDiaActionPerformed
        try {
            ImpresionTicket impresion = new ImpresionTicket();
            impresion.imprimirTotalesDiariosMinimal();
            JOptionPane.showMessageDialog(null, "Ticket de totales diarios impreso correctamente");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error al imprimir totales diarios: " + e.getMessage());
        }
    }//GEN-LAST:event_BtnImprimirDiaActionPerformed

    private void BtnCerrarSesionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnCerrarSesionActionPerformed
        /// Confirmar si el usuario desea cerrar sesión
        Object[] options = {"Volver al Login", "Salir de la Aplicación", "Cancelar"};
        int confirmacion = JOptionPane.showOptionDialog(this,
                "¿Qué desea hacer?",
                "Cerrar Sesión",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]);

        if (confirmacion == 0) { // Volver al Login
            // Limpiar datos de la sesión
            LabelVendedor.setText("");
            // Opcional: Totalpagar = 0.0; LimpiarTableMenu(); LimpiarTable();

            // Cerrar la ventana actual
            dispose();

            // Abrir la ventana de login
            try {
                FrmLogin login = new FrmLogin();
                login.setVisible(true);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                        "Error al abrir la ventana de login: " + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        } else if (confirmacion == 1) { // Salir de la Aplicación
            System.exit(0);
        }

    }//GEN-LAST:event_BtnCerrarSesionActionPerformed

    private void btnModificarUsuaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnModificarUsuaActionPerformed
// Asumiendo que la tabla se llama tablaUsuarios
        int fila = TableUsuarios.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this,
                    "Seleccione un usuario de la tabla",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Obtener el ID del usuario seleccionado
        int id = Integer.parseInt(TableUsuarios.getValueAt(fila, 0).toString()); // Ajusta según la columna del ID
        LoginDao loginDao = new LoginDao();
        login usuario = loginDao.buscarUsuarioPorId(id); // Método en LoginDao

        if (usuario == null) {
            JOptionPane.showMessageDialog(this,
                    "No se encontró el usuario",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Confirmar si el usuario desea modificar o eliminar
        String[] options = {"Modificar", "Eliminar"};
        int confirmacion = JOptionPane.showOptionDialog(this,
                "¿Qué desea hacer con " + usuario.getNombre() + "?",
                "Gestión de Usuario",
                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
                null, options, options[0]);

        if (confirmacion == 0) { // Modificar
            // Crear un formulario para modificar datos
            JDialog dialog = new JDialog(this, "Modificar Usuario", true);
            dialog.setLayout(new GridLayout(7, 2, 10, 10)); // Aumentar filas para botón extra
            dialog.setSize(400, 380);
            dialog.setLocationRelativeTo(this);

            // Campos para editar datos
            JLabel lblNombre = new JLabel("Nombre:");
            JTextField txtNombre = new JTextField(usuario.getNombre());
            JLabel lblCorreo = new JLabel("Usuario:");
            JTextField txtCorreo = new JTextField(usuario.getCorreo());
            JLabel lblPass = new JLabel("Contraseña:");
            JPasswordField txtPass = new JPasswordField(usuario.getPass());
            JLabel lblRol = new JLabel("Rol:");
            JComboBox<String> cmbRol = new JComboBox<>(new String[]{"Administrador", "Asistente"});
            cmbRol.setSelectedItem(usuario.getRol()); // Seleccionar el rol actual

            JButton btnGuardar = new JButton("Guardar");
            JButton btnEliminar = new JButton("Eliminar"); // Botón para eliminar
            JButton btnCancelar = new JButton("Cancelar");

            // Agregar componentes al diálogo
            dialog.add(lblNombre);
            dialog.add(txtNombre);
            dialog.add(lblCorreo);
            dialog.add(txtCorreo);
            dialog.add(lblPass);
            dialog.add(txtPass);
            dialog.add(lblRol);
            dialog.add(cmbRol);
            dialog.add(new JLabel()); // Espacio vacío
            dialog.add(btnGuardar);
            dialog.add(new JLabel()); // Espacio vacío
            dialog.add(btnEliminar);
            dialog.add(new JLabel()); // Espacio vacío
            dialog.add(btnCancelar);

            // Acción del botón Guardar
            btnGuardar.addActionListener(e -> {
                String nombre = txtNombre.getText().trim();
                String correo = txtCorreo.getText().trim();
                String pass = new String(txtPass.getPassword()).trim();
                String rol = (String) cmbRol.getSelectedItem();

                // Validar datos
                if (nombre.isEmpty() || correo.isEmpty() || pass.isEmpty() || rol == null) {
                    JOptionPane.showMessageDialog(dialog,
                            "Todos los campos son obligatorios",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Actualizar objeto login
                usuario.setNombre(nombre);
                usuario.setCorreo(correo);
                usuario.setPass(pass);
                usuario.setRol(rol);

                // Actualizar en la base de datos
                boolean actualizado = loginDao.actualizarUsuario(usuario);

                if (actualizado) {
                    JOptionPane.showMessageDialog(dialog,
                            "Usuario actualizado correctamente",
                            "Éxito",
                            JOptionPane.INFORMATION_MESSAGE);
                    cargarTablaUsuarios(); // Método para recargar la tabla
                    dialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(dialog,
                            "Error al actualizar el usuario",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            });

            // Acción del botón Eliminar
            btnEliminar.addActionListener(e -> {
                int deleteConfirm = JOptionPane.showConfirmDialog(dialog,
                        "¿Está seguro de que desea eliminar a " + usuario.getNombre() + "?",
                        "Confirmar Eliminación",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE);
                if (deleteConfirm == JOptionPane.YES_OPTION) {
                    boolean eliminado = loginDao.eliminarUsuario(id); // Método en LoginDao
                    if (eliminado) {
                        JOptionPane.showMessageDialog(dialog,
                                "Usuario eliminado correctamente",
                                "Éxito",
                                JOptionPane.INFORMATION_MESSAGE);
                        cargarTablaUsuarios(); // Recargar la tabla
                        dialog.dispose();
                    } else {
                        JOptionPane.showMessageDialog(dialog,
                                "Error al eliminar el usuario",
                                "Error",
                                JOptionPane.ERROR_MESSAGE);
                    }
                }
            });

            // Acción del botón Cancelar
            btnCancelar.addActionListener(e -> dialog.dispose());

            // Mostrar el diálogo
            dialog.setVisible(true);
        } else if (confirmacion == 1) { // Eliminar
            int deleteConfirm = JOptionPane.showConfirmDialog(this,
                    "¿Está seguro de que desea eliminar a " + usuario.getNombre() + "?",
                    "Confirmar Eliminación",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
            if (deleteConfirm == JOptionPane.YES_OPTION) {
                boolean eliminado = loginDao.eliminarUsuario(id); // Método en LoginDao
                if (eliminado) {
                    JOptionPane.showMessageDialog(this,
                            "Usuario eliminado correctamente",
                            "Éxito",
                            JOptionPane.INFORMATION_MESSAGE);
                    cargarTablaUsuarios(); // Recargar la tabla
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Error al eliminar el usuario",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        }

    }//GEN-LAST:event_btnModificarUsuaActionPerformed

    private void btnModificarUsuarioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnIniciar1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnIniciar1ActionPerformed

    private void txtPedidosDiaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtPedidosDiaActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtPedidosDiaActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton BtnCerrarSesion;
    private javax.swing.JButton BtnImprimirDia;
    private javax.swing.JLabel LabelVendedor;
    private javax.swing.JPanel PanelMesas;
    private javax.swing.JPanel PanelSalas;
    private javax.swing.JTable TablePedidos;
    private javax.swing.JTable TablePlatos;
    public javax.swing.JTable TableUsuarios;
    private javax.swing.JButton btnActualizarConfig;
    private javax.swing.JButton btnActualizarSala;
    private javax.swing.JButton btnAddPlato;
    private javax.swing.JButton btnAddPlatoFinalizar;
    private javax.swing.JButton btnConfig;
    private javax.swing.JButton btnEditarPlato;
    private javax.swing.JButton btnEfectivo;
    private javax.swing.JButton btnEliminarPedido;
    private javax.swing.JButton btnEliminarPlato;
    private javax.swing.JButton btnEliminarPlatoFinalizar;
    private javax.swing.JButton btnEliminarSala;
    private javax.swing.JButton btnEliminarTempPlato;
    private javax.swing.JButton btnFinalizar;
    private javax.swing.JButton btnGenerarPedido;
    private javax.swing.JButton btnGuardarPlato;
    private javax.swing.JButton btnImprimir;
    private javax.swing.JButton btnIniciar;
    private javax.swing.JButton btnModificarUsua;
    private javax.swing.JButton btnNuevoPlato;
    private javax.swing.JButton btnNuevoSala;
    private javax.swing.JButton btnPdfPedido;
    private javax.swing.JButton btnPlatos;
    private javax.swing.JButton btnRegistrarSala;
    private javax.swing.JButton btnSala;
    private javax.swing.JButton btnTransaccion;
    private javax.swing.JButton btnUsuarios;
    private javax.swing.JButton btnVentas;
    private javax.swing.JComboBox<String> cbxRol;
    private javax.swing.JButton jButton2;
    private javax.swing.JComboBox<String> jComboSalas;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel29;
    private javax.swing.JLabel jLabel30;
    private javax.swing.JLabel jLabel31;
    private javax.swing.JLabel jLabel32;
    private javax.swing.JLabel jLabel33;
    private javax.swing.JLabel jLabel34;
    private javax.swing.JLabel jLabel35;
    private javax.swing.JLabel jLabel36;
    private javax.swing.JLabel jLabel37;
    private javax.swing.JLabel jLabel38;
    private javax.swing.JLabel jLabel39;
    private javax.swing.JLabel jLabel40;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JLabel jLabel_wallpaper;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel12;
    private javax.swing.JPanel jPanel15;
    private javax.swing.JPanel jPanel16;
    private javax.swing.JPanel jPanel17;
    private javax.swing.JPanel jPanel18;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel21;
    private javax.swing.JPanel jPanel22;
    private javax.swing.JPanel jPanel23;
    private javax.swing.JPanel jPanel24;
    private javax.swing.JPanel jPanel25;
    private javax.swing.JPanel jPanel31;
    private javax.swing.JPanel jPanel33;
    private javax.swing.JPanel jPanel35;
    private javax.swing.JPanel jPanel36;
    private javax.swing.JPanel jPanel38;
    private javax.swing.JPanel jPanel39;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel40;
    private javax.swing.JPanel jPanel41;
    private javax.swing.JPanel jPanel42;
    private javax.swing.JPanel jPanel43;
    private javax.swing.JPanel jPanel44;
    private javax.swing.JPanel jPanel45;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane10;
    private javax.swing.JScrollPane jScrollPane11;
    private javax.swing.JScrollPane jScrollPane12;
    private javax.swing.JScrollPane jScrollPane13;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JScrollPane jScrollPane8;
    private javax.swing.JScrollPane jScrollPane9;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JLabel labelLogo;
    private javax.swing.JTable tableFinalizar;
    private javax.swing.JTable tableMenu;
    private javax.swing.JTable tableSala;
    private javax.swing.JTable tblTemPlatos;
    private javax.swing.JLabel tipo;
    private javax.swing.JLabel totalFinalizar;
    private javax.swing.JLabel totalMenu;
    private javax.swing.JTextField txtBuscarPlato;
    private javax.swing.JTextPane txtComentario;
    private javax.swing.JTextField txtCorreo;
    private javax.swing.JTextField txtDireccionConfig;
    private javax.swing.JTextField txtFechaHora;
    private javax.swing.JTextField txtIdConfig;
    private javax.swing.JTextField txtIdHistorialPedido;
    private javax.swing.JTextField txtIdPedido;
    private javax.swing.JTextField txtIdPlato;
    private javax.swing.JTextField txtIdSala;
    private javax.swing.JTextField txtMensaje;
    private javax.swing.JTextField txtMesas;
    private javax.swing.JTextField txtNombre;
    private javax.swing.JTextField txtNombreConfig;
    private javax.swing.JTextField txtNombrePlato;
    private javax.swing.JTextField txtNombreSala;
    private javax.swing.JTextField txtNumMesaFinalizar;
    private javax.swing.JPasswordField txtPass;
    private javax.swing.JTextField txtPedidosDia;
    private javax.swing.JTextField txtPrecioPlato;
    private javax.swing.JTextField txtRucConfig;
    private javax.swing.JTextField txtSalaFinalizar;
    private javax.swing.JTextField txtTelefonoConfig;
    private javax.swing.JTextField txtTempIdSala;
    private javax.swing.JTextField txtTempNumMesa;
    private javax.swing.JTextField txtTotalDia;
    private javax.swing.JTextField txtTotalDiaTrans;
    // End of variables declaration//GEN-END:variables

    private void TotalPagar(JTable tabla, JLabel label) {
        Totalpagar = 0.00;
        int numFila = tabla.getRowCount();
        for (int i = 0; i < numFila; i++) {
            double cal = Double.parseDouble(String.valueOf(tabla.getModel().getValueAt(i, 4)));
            Totalpagar += cal;
        }
        label.setText(String.format("%.2f", Totalpagar));
    }

    private void LimpiarTableMenu() {
        tmp = (DefaultTableModel) tableMenu.getModel();
        int fila = tableMenu.getRowCount();
        for (int i = 0; i < fila; i++) {
            tmp.removeRow(0);
        }
    }

    public void ListarConfig() {
        conf = lgDao.datosEmpresa();
        txtIdConfig.setText("" + conf.getId());
        txtRucConfig.setText("" + conf.getRuc());
        txtNombreConfig.setText("" + conf.getNombre());
        txtTelefonoConfig.setText("" + conf.getTelefono());
        txtDireccionConfig.setText("" + conf.getDireccion());
        txtMensaje.setText("" + conf.getMensaje());
    }

    private void ListarPedidos() {
        new javax.swing.SwingWorker<java.util.List<Pedidos>, Void>() {
            Timestamp fechaInicio, fechaFin;

            @Override
            protected java.util.List<Pedidos> doInBackground() {
                LocalDateTime ahora = LocalDateTime.now(ZoneId.of("America/Lima"));
                LocalDateTime inicio, fin;
                if (ahora.getHour() < 4) {
                    inicio = ahora.minusDays(1).withHour(16).withMinute(0).withSecond(0).withNano(0);
                    fin = ahora;
                } else if (ahora.getHour() < 16) {
                    inicio = ahora.minusDays(1).withHour(16).withMinute(0).withSecond(0).withNano(0);
                    fin = ahora;
                } else {
                    inicio = ahora.withHour(16).withMinute(0).withSecond(0).withNano(0);
                    fin = ahora;
                }
                fechaInicio = Timestamp.valueOf(inicio);
                fechaFin = Timestamp.valueOf(fin);
                System.out.println("⏱️ ListarPedidos usando rango: " + fechaInicio + " → " + fechaFin);
                return pedDao.listarPedidosDelDia(fechaInicio, fechaFin);
            }

            @Override
            protected void done() {
                try {
                    java.util.List<Pedidos> Listar = get();
                    Tables color = new Tables();
                    modelo = (DefaultTableModel) TablePedidos.getModel();
                    modelo.setRowCount(0);
                    txtPedidosDia.setText(String.valueOf(Listar.size()));
                    Object[] ob = new Object[7];
                    for (int i = 0; i < Listar.size(); i++) {
                        ob[0] = Listar.get(i).getId();
                        ob[1] = Listar.get(i).getSala();
                        ob[2] = Listar.get(i).getUsuario();
                        ob[3] = Listar.get(i).getNum_mesa();
                        ob[4] = Listar.get(i).getFecha();
                        ob[5] = String.format("%.2f", Listar.get(i).getTotal());
                        ob[6] = Listar.get(i).getEstado();
                        modelo.addRow(ob);
                        System.out.println("Pedido " + i + ": Fecha = " + ob[4] + ", Total = " + ob[5]);
                    }
                    colorHeader(TablePedidos);
                    TablePedidos.setDefaultRenderer(Object.class, color);
                    // Actualizar totales DESPUÉS de que la conexión anterior ya fue liberada
                    actualizarTotalDia();
                } catch (Exception e) {
                    System.out.println("Error al listar pedidos: " + e.getMessage());
                }
            }
        }.execute();
    }

    public void LimpiarTable() {
        for (int i = 0; i < modelo.getRowCount(); i++) {
            modelo.removeRow(i);
            i = i - 1;
        }
    }

    private void ListarUsuarios() {
        List<login> Listar = lgDao.ListarUsuarios();
        modelo = (DefaultTableModel) TableUsuarios.getModel();
        Object[] ob = new Object[4];
        for (int i = 0; i < Listar.size(); i++) {
            ob[0] = Listar.get(i).getId();
            ob[1] = Listar.get(i).getNombre();
            ob[2] = Listar.get(i).getCorreo();
            ob[3] = Listar.get(i).getRol();
            modelo.addRow(ob);
        }
        colorHeader(TableUsuarios);
    }

    private void ListarSalas() {
        List<Salas> Listar = slDao.Listar();
        modelo = (DefaultTableModel) tableSala.getModel();
        Object[] ob = new Object[3];
        for (int i = 0; i < Listar.size(); i++) {
            ob[0] = Listar.get(i).getId();
            ob[1] = Listar.get(i).getNombre();
            ob[2] = Listar.get(i).getMesas();
            modelo.addRow(ob);
        }
        colorHeader(tableSala);

    }

    private void colorHeader(JTable tabla) {
        tabla.setModel(modelo);
        JTableHeader header = tabla.getTableHeader();
        header.setOpaque(false);
        header.setBackground(new Color(0, 110, 255));
        header.setForeground(Color.white);
    }

    private void LimpiarSala() {
        txtIdSala.setText("");
        txtNombreSala.setText("");
        txtMesas.setText("");
    }

    private void LimpiarPlatos() {
        txtIdPlato.setText("");
        txtNombrePlato.setText("");
        txtPrecioPlato.setText("");
        // Restaurar botón GUARDAR a modo Registro (verde)
        btnGuardarPlato.setText("GUARDAR");
        btnGuardarPlato.setBackground(new java.awt.Color(46, 204, 113));
    }

    private void decorarSistemaUI() {
        // --- 1. Cabecera (Header) y Reloj ---
        jLabel38.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 26));
        jLabel38.setForeground(new java.awt.Color(241, 245, 249)); // Slate claro
        jLabel38.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);

        // --- 2. Barra Lateral (Sidebar) Botones ---
        javax.swing.JButton[] btns = {btnSala, btnVentas, btnConfig, btnUsuarios, btnPlatos};
        for (javax.swing.JButton btn : btns) {
            btn.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14));
            btn.setForeground(java.awt.Color.WHITE);
            btn.setBorderPainted(false);
            btn.setContentAreaFilled(false);
            btn.setFocusPainted(false);
            btn.setOpaque(false);

            btn.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseEntered(java.awt.event.MouseEvent evt) {
                    if (btn.isEnabled()) {
                        btn.setOpaque(true);
                        btn.setBackground(new java.awt.Color(255, 255, 255, 30)); // Blanco traslúcido
                        btn.getParent().repaint();
                    }
                }

                @Override
                public void mouseExited(java.awt.event.MouseEvent evt) {
                    btn.setOpaque(false);
                    btn.getParent().repaint();
                }
            });
        }

        // Cerrar sesión
        BtnCerrarSesion.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
        BtnCerrarSesion.setForeground(new java.awt.Color(254, 226, 226)); // Rojo claro
        BtnCerrarSesion.setBorderPainted(false);
        BtnCerrarSesion.setContentAreaFilled(false);
        BtnCerrarSesion.setFocusPainted(false);
        BtnCerrarSesion.setOpaque(false);
        BtnCerrarSesion.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                BtnCerrarSesion.setOpaque(true);
                BtnCerrarSesion.setBackground(new java.awt.Color(239, 68, 68, 60)); // Hover rojo traslúcido
                BtnCerrarSesion.getParent().repaint();
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                BtnCerrarSesion.setOpaque(false);
                BtnCerrarSesion.getParent().repaint();
            }
        });

        // --- 3. Cabeceras de Tablas Personalizadas (JTableHeader) ---
        javax.swing.JTable[] tablas = {tableSala, TableUsuarios, TablePlatos, TablePedidos, tableMenu, tableFinalizar, tblTemPlatos};
        for (javax.swing.JTable t : tablas) {
            javax.swing.table.JTableHeader header = t.getTableHeader();
            header.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 13));
            header.setBackground(new java.awt.Color(15, 23, 42)); // Azul oscuro pizarra
            header.setForeground(java.awt.Color.WHITE);
            header.setReorderingAllowed(false);

            header.setDefaultRenderer(new javax.swing.table.DefaultTableCellRenderer() {
                @Override
                public java.awt.Component getTableCellRendererComponent(javax.swing.JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                    java.awt.Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                    c.setBackground(new java.awt.Color(15, 23, 42));
                    c.setForeground(java.awt.Color.WHITE);
                    setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 13));
                    setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
                    setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 1, new java.awt.Color(51, 65, 85)));
                    return c;
                }
            });
        }

        // --- 4. Inputs (JTextFields) Modernos en Modo Oscuro ---
        javax.swing.JTextField[] camposInput = {txtNombreSala, txtMesas, txtTelefonoConfig, txtDireccionConfig, txtMensaje, txtRucConfig, txtNombreConfig, txtCorreo, txtPass, txtNombre, txtNombrePlato, txtPrecioPlato, txtBuscarPlato};
        for (javax.swing.JTextField tf : camposInput) {
            tf.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 14));
            tf.setBackground(new java.awt.Color(51, 65, 85)); // Slate 700
            tf.setForeground(java.awt.Color.WHITE);
            tf.setCaretColor(new java.awt.Color(96, 165, 250)); // Azul brillante
            tf.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                    new RoundedBorder(8, new java.awt.Color(71, 85, 105), new java.awt.Insets(6, 10, 6, 10)),
                    javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0)
            ));

            tf.addFocusListener(new java.awt.event.FocusAdapter() {
                @Override
                public void focusGained(java.awt.event.FocusEvent e) {
                    tf.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                            new RoundedBorder(8, new java.awt.Color(96, 165, 250), new java.awt.Insets(6, 10, 6, 10)), // Azul brillante al enfocar
                            javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0)
                    ));
                }

                @Override
                public void focusLost(java.awt.event.FocusEvent e) {
                    tf.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                            new RoundedBorder(8, new java.awt.Color(71, 85, 105), new java.awt.Insets(6, 10, 6, 10)),
                            javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0)
                    ));
                }
            });
        }

        // --- 5. JComboBox Moderno ---
        cbxRol.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 14));
        cbxRol.setBackground(new java.awt.Color(51, 65, 85)); // Slate 700
        cbxRol.setForeground(java.awt.Color.WHITE);

        // --- 6. Paneles estilo "Card" (Tarjetas) ---
        javax.swing.JPanel[] cards = {jPanel10, jPanel15, jPanel2, jPanel8};
        for (javax.swing.JPanel p : cards) {
            p.setBackground(new java.awt.Color(30, 41, 59)); // Slate 800
            p.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                    new RoundedBorder(12, new java.awt.Color(51, 65, 85), new java.awt.Insets(12, 12, 12, 12)), // Borde Slate 600
                    javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0)
            ));
        }

        // --- 7. Botones Semánticos en Formularios CRUD ---
        // Registrar/Guardar (Verde)
        javax.swing.JButton[] successBtns = {btnRegistrarSala, btnIniciar, btnGuardarPlato, btnActualizarConfig};
        for (javax.swing.JButton btn : successBtns) {
            btn.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 13));
            btn.setForeground(java.awt.Color.WHITE);
            btn.setBackground(new java.awt.Color(16, 185, 129)); // Emerald 500
            btn.setBorder(new RoundedBorder(8, new java.awt.Color(16, 185, 129), new java.awt.Insets(6, 12, 6, 12)));
            btn.setFocusPainted(false);
            btn.setContentAreaFilled(false);
            btn.setOpaque(true);
            btn.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseEntered(java.awt.event.MouseEvent evt) {
                    btn.setBackground(new java.awt.Color(5, 150, 105)); // Emerald 600
                }

                @Override
                public void mouseExited(java.awt.event.MouseEvent evt) {
                    btn.setBackground(new java.awt.Color(16, 185, 129));
                }
            });
        }

        // Modificar/Editar (Azul)
        javax.swing.JButton[] infoBtns = {btnActualizarSala, btnEditarPlato, btnNuevoPlato, btnNuevoSala, btnModificarUsua};
        for (javax.swing.JButton btn : infoBtns) {
            btn.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 13));
            btn.setForeground(java.awt.Color.WHITE);
            btn.setBackground(new java.awt.Color(59, 130, 246)); // Blue 500
            btn.setBorder(new RoundedBorder(8, new java.awt.Color(59, 130, 246), new java.awt.Insets(6, 12, 6, 12)));
            btn.setFocusPainted(false);
            btn.setContentAreaFilled(false);
            btn.setOpaque(true);
            btn.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseEntered(java.awt.event.MouseEvent evt) {
                    btn.setBackground(new java.awt.Color(37, 99, 235)); // Blue 600
                }

                @Override
                public void mouseExited(java.awt.event.MouseEvent evt) {
                    btn.setBackground(new java.awt.Color(59, 130, 246));
                }
            });
        }

        // Eliminar (Rojo)
        javax.swing.JButton[] dangerBtns = {btnEliminarSala, btnEliminarPlato, btnEliminarPlatoFinalizar, btnEliminarPedido};
        for (javax.swing.JButton btn : dangerBtns) {
            btn.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 13));
            btn.setForeground(java.awt.Color.WHITE);
            btn.setBackground(new java.awt.Color(239, 68, 68)); // Red 500
            btn.setBorder(new RoundedBorder(8, new java.awt.Color(239, 68, 68), new java.awt.Insets(6, 12, 6, 12)));
            btn.setFocusPainted(false);
            btn.setContentAreaFilled(false);
            btn.setOpaque(true);
            btn.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseEntered(java.awt.event.MouseEvent evt) {
                    btn.setBackground(new java.awt.Color(220, 38, 38)); // Red 600
                }

                @Override
                public void mouseExited(java.awt.event.MouseEvent evt) {
                    btn.setBackground(new java.awt.Color(239, 68, 68));
                }
            });
        }

        // Botones Especiales de Venta (Efectivo / Transacción)
        btnEfectivo.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14));
        btnEfectivo.setForeground(java.awt.Color.WHITE);
        btnEfectivo.setBackground(new java.awt.Color(16, 185, 129));
        btnEfectivo.setBorder(new RoundedBorder(10, new java.awt.Color(16, 185, 129), new java.awt.Insets(8, 16, 8, 16)));
        btnEfectivo.setOpaque(true);
        btnEfectivo.setContentAreaFilled(false);

        btnTransaccion.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14));
        btnTransaccion.setForeground(java.awt.Color.WHITE);
        btnTransaccion.setBackground(new java.awt.Color(59, 130, 246));
        btnTransaccion.setBorder(new RoundedBorder(10, new java.awt.Color(59, 130, 246), new java.awt.Insets(8, 16, 8, 16)));
        btnTransaccion.setOpaque(true);
        btnTransaccion.setContentAreaFilled(false);

        // --- 8. Ocultar Pestañas del TabbedPane ---
        jTabbedPane1.putClientProperty("JTabbedPane.showTabArea", false);
        jTabbedPane1.setUI(new javax.swing.plaf.basic.BasicTabbedPaneUI() {
            @Override
            protected int calculateTabHeight(int tabPlacement, int tabIndex, int verticalPadding) {
                return 0; // Oculta las pestañas superiores
            }

            @Override
            protected void paintTabArea(java.awt.Graphics g, int tabPlacement, int selectedIndex) {
                // No pintar nada
            }
        });

        // --- 9. Estilo General de Fondos y Transparencias (Modo Oscuro Slate) ---
        java.awt.Color darkSlate = new java.awt.Color(15, 23, 42); // Slate 900
        this.getContentPane().setBackground(darkSlate);
        jTabbedPane1.setBackground(darkSlate);
        jTabbedPane1.setOpaque(true);
        labelLogo.setOpaque(false);
        LabelVendedor.setOpaque(false);

        // Iterar de forma segura sobre todas las pestañas registradas para cambiar su fondo y estilizar subcomponentes
        for (int i = 0; i < jTabbedPane1.getTabCount(); i++) {
            java.awt.Component tabComponent = jTabbedPane1.getComponentAt(i);
            if (tabComponent != null) {
                tabComponent.setBackground(darkSlate);
                if (tabComponent instanceof javax.swing.JComponent) {
                    ((javax.swing.JComponent) tabComponent).setOpaque(true);
                }
                if (tabComponent instanceof java.awt.Container) {
                    styleComponentsRecursively((java.awt.Container) tabComponent);
                }
            }
        }
    }

    private void styleComponentsRecursively(java.awt.Container container) {
        java.awt.Color bgDark = new java.awt.Color(15, 23, 42); // Slate 900
        java.awt.Color bgCard = new java.awt.Color(30, 41, 59); // Slate 800
        java.awt.Color textLight = new java.awt.Color(241, 245, 249); // Slate 100

        for (java.awt.Component child : container.getComponents()) {
            if (child instanceof javax.swing.JPanel) {
                if (child != jPanel10 && child != jPanel15 && child != jPanel2 && child != jPanel8 && child != PanelSalas && child != PanelMesas) {
                    child.setBackground(bgDark);
                } else if (child == PanelSalas || child == PanelMesas) {
                    child.setBackground(bgDark);
                    ((javax.swing.JPanel) child).setOpaque(true);
                } else {
                    child.setBackground(bgCard);
                }
            } else if (child instanceof javax.swing.JLabel) {
                if (child.getParent() != jPanel1 && child != jLabel38) {
                    child.setForeground(textLight);
                    child.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 13));
                }
            } else if (child instanceof javax.swing.JScrollPane) {
                child.setBackground(bgDark);
                javax.swing.JScrollPane sp = (javax.swing.JScrollPane) child;
                sp.getViewport().setBackground(bgDark);
                sp.setBorder(javax.swing.BorderFactory.createEmptyBorder());
            } else if (child instanceof javax.swing.JViewport) {
                child.setBackground(bgDark);
            }
            if (child instanceof java.awt.Container) {
                styleComponentsRecursively((java.awt.Container) child);
            }
        }
    }


    private static class RoundedBorder implements javax.swing.border.Border {
        private int radius;
        private java.awt.Color color;
        private java.awt.Insets insets;

        RoundedBorder(int radius, java.awt.Color color) {
            this.radius = radius;
            this.color = color;
            this.insets = new java.awt.Insets(6, 10, 6, 10);
        }

        RoundedBorder(int radius, java.awt.Color color, java.awt.Insets insets) {
            this.radius = radius;
            this.color = color;
            this.insets = insets;
        }

        public java.awt.Insets getBorderInsets(java.awt.Component c) {
            return insets;
        }

        public boolean isBorderOpaque() {
            return true;
        }

        public void paintBorder(java.awt.Component c, java.awt.Graphics g, int x, int y, int width, int height) {
            java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
            g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.draw(new java.awt.geom.RoundRectangle2D.Double(x, y, width - 1, height - 1, radius, radius));
            g2.dispose();
        }
    }

    private void panelSalas() {
        List<Salas> Listar = slDao.Listar();
        for (int i = 0; i < Listar.size(); i++) {
            int id = Listar.get(i).getId();
            int cantidad = Listar.get(i).getMesas();
            JButton boton = new JButton(Listar.get(i).getNombre(), new ImageIcon(getClass().getResource("/Img/salas.png"))) {
                @Override
                protected void paintComponent(java.awt.Graphics g) {
                    java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
                    g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                    if (getModel().isPressed()) {
                        g2.setColor(new java.awt.Color(15, 23, 42)); // Slate 900
                    } else if (getModel().isRollover()) {
                        g2.setColor(new java.awt.Color(51, 65, 85)); // Slate 700
                    } else {
                        g2.setColor(new java.awt.Color(30, 41, 59)); // Slate 800
                    }
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            boton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
            boton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
            boton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
            boton.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14));
            boton.setForeground(new java.awt.Color(241, 245, 249)); // Slate 100
            boton.setBorder(new RoundedBorder(16, new java.awt.Color(51, 65, 85), new java.awt.Insets(12, 12, 12, 12)));
            boton.setFocusPainted(false);
            boton.setOpaque(false);
            boton.setContentAreaFilled(false);

            // Hover effect for border
            boton.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseEntered(java.awt.event.MouseEvent evt) {
                    boton.setBorder(new RoundedBorder(16, new java.awt.Color(59, 130, 246), new java.awt.Insets(12, 12, 12, 12)));
                }
                @Override
                public void mouseExited(java.awt.event.MouseEvent evt) {
                    boton.setBorder(new RoundedBorder(16, new java.awt.Color(51, 65, 85), new java.awt.Insets(12, 12, 12, 12)));
                }
            });

            PanelSalas.add(boton);
            boton.addActionListener((ActionEvent e) -> {
                LimpiarTable();
                PanelMesas.removeAll();
                panelMesas(id, cantidad);
                jTabbedPane1.setSelectedIndex(2);
            });
        }
    }

    //crear mesas
    private void panelMesas(int id_sala, int cant) {
        final int cantFinal = (cant < 4) ? 4 : cant;

        // 1. Crear todos los botones primero (sin consultas a BD)
        JButton[] botones = new JButton[cantFinal];
        for (int i = 0; i < cantFinal; i++) {
            int num_mesa = i + 1;
            String etiqueta = (num_mesa > cantFinal - 4) ? "DOMICILIO N\u00b0: " + num_mesa : "MESA N\u00b0: " + num_mesa;
            JButton boton = new JButton(etiqueta, new ImageIcon(getClass().getResource(""))) {
                @Override
                protected void paintComponent(java.awt.Graphics g) {
                    java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
                    g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(getBackground());
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            boton.setOpaque(false);
            boton.setContentAreaFilled(false);
            boton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
            boton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
            boton.setBackground(new java.awt.Color(30, 41, 59)); // Slate 800
            boton.setForeground(new java.awt.Color(148, 163, 184)); // Slate 400
            boton.setBorder(new RoundedBorder(16, new java.awt.Color(51, 65, 85), new java.awt.Insets(10, 10, 10, 10)));
            boton.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14));
            boton.setFocusable(false);
            boton.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
            boton.setToolTipText("Cargando estado...");
            botones[i] = boton;
            PanelMesas.add(boton);
        }
        PanelMesas.revalidate();
        PanelMesas.repaint();

        // 2. Verificar estado de mesas en hilo de fondo (1 sola conexion por mesa, secuencialmente)
        new javax.swing.SwingWorker<int[], Void>() {
            @Override
            protected int[] doInBackground() {
                int[] estados = new int[cantFinal];
                for (int i = 0; i < cantFinal; i++) {
                    estados[i] = pedDao.verificarStado(i + 1, id_sala);
                }
                return estados;
            }
            @Override
            protected void done() {
                try {
                    int[] estados = get();
                    for (int i = 0; i < cantFinal; i++) {
                        final int verificar = estados[i];
                        final int num_mesa = i + 1;
                        final String etiqueta = (num_mesa > cantFinal - 4) ? "DOMICILIO N\u00b0: " + num_mesa : "MESA N\u00b0: " + num_mesa;
                        JButton boton = botones[i];
                        if (verificar > 0) {
                            boton.setBackground(new java.awt.Color(127, 29, 29)); // Dark Red #7F1D1D
                            boton.setForeground(new java.awt.Color(252, 165, 165)); // Soft Red #FCA5A5
                            boton.setBorder(new RoundedBorder(16, new java.awt.Color(220, 38, 38), new java.awt.Insets(10, 10, 10, 10)));
                            boton.setToolTipText("Ocupada - Click para ver pedido");
                        } else {
                            boton.setBackground(new java.awt.Color(6, 78, 59)); // Dark Green #064E3B
                            boton.setForeground(new java.awt.Color(52, 211, 153)); // Soft Green #34D399
                            boton.setBorder(new RoundedBorder(16, new java.awt.Color(5, 150, 105), new java.awt.Insets(10, 10, 10, 10)));
                            boton.setToolTipText("Libre - Click para tomar pedido");
                        }
                        boton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

                        for (java.awt.event.ActionListener al : boton.getActionListeners()) {
                            boton.removeActionListener(al);
                        }
                        boton.addActionListener((ActionEvent e) -> {
                            if (verificar > 0) {
                                LimpiarTable();
                                verPedido(verificar);
                                verPedidoDetalle(verificar);
                                btnFinalizar.setEnabled(true);
                                btnPdfPedido.setEnabled(false);
                                jTabbedPane1.setSelectedIndex(4);
                            } else {
                                if (id_sala == 3) {
                                    txtTempIdSala.setText("" + id_sala);
                                    txtTempNumMesa.setText("" + num_mesa);
                                    ped.setId_sala(id_sala);
                                    ped.setNum_mesa(num_mesa);
                                    ped.setTotal(0.0);
                                    ped.setUsuario(LabelVendedor.getText());
                                    int id_pedido = pedDao.RegistrarPedido(ped);
                                    if (id_pedido != -1) {
                                        LimpiarTable();
                                        verPedido(id_pedido);
                                        verPedidoDetalle(id_pedido);
                                        btnFinalizar.setEnabled(true);
                                        btnPdfPedido.setEnabled(false);
                                        jTabbedPane1.setSelectedIndex(4);
                                        JOptionPane.showMessageDialog(null, "Pedido creado para " + etiqueta + " en Sala 3");
                                    } else {
                                        JOptionPane.showMessageDialog(null, "Error al crear el pedido");
                                    }
                                } else {
                                    LimpiarTable();
                                    ListarPlatos(tblTemPlatos);
                                    jTabbedPane1.setSelectedIndex(3);
                                    txtTempIdSala.setText("" + id_sala);
                                    txtTempNumMesa.setText("" + num_mesa);
                                }
                            }
                        });
                    }
                    PanelMesas.revalidate();
                    PanelMesas.repaint();
                } catch (Exception e) {
                    System.out.println("Error al cargar estados de mesas: " + e.getMessage());
                }
            }
        }.execute();
    }


    // platos
    private void ListarPlatos(JTable tabla) {
        List<Platos> Listar = plaDao.Listar(txtBuscarPlato.getText());
        modelo = (DefaultTableModel) tabla.getModel();
        modelo.setRowCount(0); // Limpiar la tabla antes de cargar
        Object[] ob = new Object[3];
        for (int i = 0; i < Listar.size(); i++) {
            ob[0] = Listar.get(i).getId();
            ob[1] = Listar.get(i).getNombre();
            ob[2] = Listar.get(i).getPrecio();
            modelo.addRow(ob);
        }
        colorHeader(tabla);
    }

    //registrar pedido
    private void RegistrarPedido() {
        int id_sala = Integer.parseInt(txtTempIdSala.getText());
        int num_mesa = Integer.parseInt(txtTempNumMesa.getText());
        double monto = Totalpagar;
        ped.setId_sala(id_sala);
        ped.setNum_mesa(num_mesa);
        ped.setTotal(monto);
        ped.setUsuario(LabelVendedor.getText());
        pedDao.RegistrarPedido(ped);
    }

    private void detallePedido() {
        int id = pedDao.IdPedido();
        for (int i = 0; i < tableMenu.getRowCount(); i++) {
            String nombre = tableMenu.getValueAt(i, 1).toString();
            int cant = Integer.parseInt(tableMenu.getValueAt(i, 2).toString());
            double precio = Double.parseDouble(tableMenu.getValueAt(i, 3).toString());
            String comentario = tableMenu.getValueAt(i, 5).toString(); // Obtener el comentario de tableMenu
            detPedido.setNombre(nombre);
            detPedido.setCantidad(cant);
            detPedido.setPrecio(precio);
            detPedido.setComentario(comentario);
            detPedido.setId_pedido(id);
            pedDao.RegistrarDetalle(detPedido);
        }
        LimpiarTable();
        LimpiarPlatos();

    }

    private void actualizarTotalMenu() {
        double total = 0.0;
        for (int i = 0; i < tableMenu.getRowCount(); i++) {
            double totalFila = Double.parseDouble(tableMenu.getValueAt(i, 4).toString());
            total += totalFila;
        }
        totalMenu.setText(String.format("%.2f", total));
    }

    public void verPedidoDetalle(int id_pedido) {
        List<DetallePedido> Listar = pedDao.verPedidoDetalle(id_pedido);
        modelo = (DefaultTableModel) tableFinalizar.getModel();
        modelo.setRowCount(0); // Limpiar tabla antes de recargar
        Object[] ob = new Object[6];
        for (int i = 0; i < Listar.size(); i++) {
            ob[0] = Listar.get(i).getId();
            ob[1] = Listar.get(i).getNombre();
            ob[2] = Listar.get(i).getCantidad();
            ob[3] = Listar.get(i).getPrecio();
            ob[4] = Listar.get(i).getCantidad() * Listar.get(i).getPrecio();
            ob[5] = Listar.get(i).getComentario();
            modelo.addRow(ob);
        }
        colorHeader(tableFinalizar);
        TotalPagar(tableFinalizar, totalFinalizar);
    }

    public void verPedido(int id_pedido) {
        txtSalaFinalizar.setText(ped.getSala());
        // Deshabilitar el listener temporalmente
        ActionListener[] listeners = jComboSalas.getActionListeners();
        for (ActionListener listener : listeners) {
            jComboSalas.removeActionListener(listener);
        }
        // Configurar la sala actual
        jComboSalas.setSelectedItem(ped.getSala());
        // Restaurar los listeners
        for (ActionListener listener : listeners) {
            jComboSalas.addActionListener(listener);
        }
        ped = pedDao.verPedido(id_pedido);
        totalFinalizar.setText("" + ped.getTotal());
        txtFechaHora.setText("" + ped.getFecha());
        txtSalaFinalizar.setText("" + ped.getSala());
        txtSalaFinalizar.setText(ped.getSala());
        jComboSalas.setSelectedItem(ped.getSala());
        txtNumMesaFinalizar.setText("" + ped.getNum_mesa());
        txtIdPedido.setText("" + ped.getId());
    }

    private void actualizarTotalDia() {
        new javax.swing.SwingWorker<double[], Void>() {
            String rangoTexto;
            @Override
            protected double[] doInBackground() {
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                java.util.Calendar cal = java.util.Calendar.getInstance();
                java.util.Calendar ci = (java.util.Calendar) cal.clone();
                java.util.Calendar cf = (java.util.Calendar) cal.clone();
                if (cal.get(java.util.Calendar.HOUR_OF_DAY) < 16) {
                    ci.add(java.util.Calendar.DAY_OF_MONTH, -1);
                    ci.set(java.util.Calendar.HOUR_OF_DAY, 16);
                    ci.set(java.util.Calendar.MINUTE, 0);
                    ci.set(java.util.Calendar.SECOND, 0);
                    cf.set(java.util.Calendar.HOUR_OF_DAY, 4);
                    cf.set(java.util.Calendar.MINUTE, 0);
                    cf.set(java.util.Calendar.SECOND, 0);
                } else {
                    ci.set(java.util.Calendar.HOUR_OF_DAY, 16);
                    ci.set(java.util.Calendar.MINUTE, 0);
                    ci.set(java.util.Calendar.SECOND, 0);
                    cf.add(java.util.Calendar.DAY_OF_MONTH, 1);
                    cf.set(java.util.Calendar.HOUR_OF_DAY, 4);
                    cf.set(java.util.Calendar.MINUTE, 0);
                    cf.set(java.util.Calendar.SECOND, 0);
                }
                Timestamp inicio = new Timestamp(ci.getTimeInMillis());
                Timestamp fin = new Timestamp(cf.getTimeInMillis());
                rangoTexto = sdf.format(inicio) + " a " + sdf.format(fin);
                PedidosDao pedidosDao = new PedidosDao();
                return pedidosDao.calcularTotalesDia(inicio, fin, 0);
            }
            @Override
            protected void done() {
                try {
                    double[] totales = get();
                    txtTotalDia.setText(String.format("%.2f", totales[0]));
                    txtTotalDiaTrans.setText(String.format("%.2f", totales[1]));
                    System.out.println("Total EFECTIVO (" + rangoTexto + "): S/ " + String.format("%.2f", totales[0]));
                    System.out.println("Total TRANSACCION (" + rangoTexto + "): S/ " + String.format("%.2f", totales[1]));
                } catch (Exception e) {
                    System.out.println("Error al actualizar totales: " + e.getMessage());
                }
            }
        }.execute();
    }

    private void cargarPedidosDelDia() {
        // Obtener la fecha y hora actual
        LocalDateTime ahora = LocalDateTime.now(ZoneId.of("America/Lima"));
        LocalDateTime inicio, fin;

        // Determinar el rango del día operativo
        if (ahora.getHour() < 16) {
            // Rango: 16:00 del día anterior a la hora actual
            inicio = ahora.minusDays(1).withHour(16).withMinute(0).withSecond(0).withNano(0);
            fin = ahora; // Usar la hora actual en lugar de 04:00
        } else {
            // Rango: 16:00 del día actual a la hora actual (o hasta 04:00 del día siguiente si prefieres)
            inicio = ahora.withHour(16).withMinute(0).withSecond(0).withNano(0);
            fin = ahora; // Usar la hora actual para incluir pedidos recientes
        }

        // Convertir a Timestamp
        Timestamp fechaInicio = Timestamp.valueOf(inicio);
        Timestamp fechaFin = Timestamp.valueOf(fin);

        // Depuración
        System.out.println("Rango de búsqueda de pedidos: " + fechaInicio + " a " + fechaFin);

        // Resto del código (consultar pedidos y llenar la tabla)
        PedidosDao pedidosDao = new PedidosDao();
        List<Pedidos> listaPedidos = pedidosDao.listarPedidosDelDia(fechaInicio, fechaFin);
        System.out.println("Pedidos encontrados: " + listaPedidos.size());

        DefaultTableModel modelo = (DefaultTableModel) TablePedidos.getModel();
        modelo.setRowCount(0);
        for (Pedidos ped : listaPedidos) {
            modelo.addRow(new Object[]{
                ped.getId(),
                ped.getSala(),
                ped.getUsuario(),
                ped.getNum_mesa(),
                ped.getFecha(),
                String.format("%.2f", ped.getTotal()),
                ped.getEstado()
            });
            System.out.println("Pedido ID: " + ped.getId() + ", Fecha: " + ped.getFecha());
        }
    }

    // En Sistema.java
    private void cargarSalasCombo() {
        ActionListener[] listeners = jComboSalas.getActionListeners();
        for (ActionListener listener : listeners) {
            jComboSalas.removeActionListener(listener);
        }
        jComboSalas.removeAllItems();
        SalasDao salaDao = new SalasDao();
        List<Salas> lista = salaDao.Listar();
        jComboSalas.addItem("Seleccionar");
        for (Salas sala : lista) {
            jComboSalas.addItem(sala.getNombre());
        }
        for (ActionListener listener : listeners) {
            jComboSalas.addActionListener(listener);
        }
    }

    private void jTabbedPane1StateChanged(javax.swing.event.ChangeEvent evt) {
        if (jTabbedPane1.getSelectedIndex() == 4) { // Índice 4 es jPanel25 (Finalizar Pedido)
            cargarSalasCombo();
        }
    }

    private void cargarTablaUsuarios() {
        DefaultTableModel modelo = (DefaultTableModel) TableUsuarios.getModel();
        modelo.setRowCount(0); // Limpiar tabla
        LoginDao loginDao = new LoginDao();
        List<login> usuarios = loginDao.ListarUsuarios();
        for (login lg : usuarios) {
            Object[] fila = {lg.getId(), lg.getNombre(), lg.getCorreo(), lg.getRol()};
            modelo.addRow(fila);
        }
    }

    private void cargarTablaPlatos() {
        DefaultTableModel modelo = (DefaultTableModel) tableMenu.getModel();
        modelo.setRowCount(0); // Limpiar tabla
        PlatosDao platosDao = new PlatosDao();
        List<Platos> platos = platosDao.Listar(""); // Lista todos los platos
        System.out.println("Número de platos cargados: " + platos.size()); // Depuración
        for (Platos pl : platos) {
            Object[] fila = {pl.getId(), pl.getNombre(), pl.getPrecio()};
            modelo.addRow(fila);
        }
    }
}
