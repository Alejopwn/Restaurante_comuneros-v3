package Vista;

import Modelo.LoginDao;
import Modelo.login;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.RoundRectangle2D;
import javax.swing.*;
import javax.swing.Timer;

public class FrmLogin extends javax.swing.JFrame {

    login lg = new login();
    LoginDao loginDao = new LoginDao();
    private javax.swing.Timer shakeTimer;
    int shakeCount = 0;

    // ─── Componentes declarados manualmente (sin NetBeans) ───────────────────────
    private JPanel panelLeft;
    private JPanel panelRight;
    private JLabel lblLogo;
    private JLabel lblSubtitle;
    private JLabel lblUser;
    private JLabel lblPass;
    private JTextField txtCorreo;
    private JPasswordField txtPass;
    private JButton btnIniciar;
    private JButton jButton1;
    public JProgressBar barra;

    public FrmLogin() {
        setUndecorated(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(700, 460);
        setLocationRelativeTo(null);
        setResizable(false);
        getContentPane().setBackground(Color.WHITE);
        getContentPane().setLayout(new BorderLayout());

        // ─── PANEL IZQUIERDO (degradado azul oscuro) ─────────────────────────────
        panelLeft = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Degradado diagonal
                GradientPaint gp = new GradientPaint(0, 0, new Color(15, 23, 42), getWidth(), getHeight(), new Color(30, 64, 175));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        panelLeft.setPreferredSize(new Dimension(290, 460));
        panelLeft.setOpaque(false);

        // Círculos decorativos de fondo
        JLabel decorCircle1 = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 255, 255, 18));
                g2.fillOval(0, 0, 200, 200);
                g2.dispose();
            }
        };
        decorCircle1.setBounds(-50, -50, 200, 200);
        panelLeft.add(decorCircle1);

        JLabel decorCircle2 = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 255, 255, 12));
                g2.fillOval(0, 0, 180, 180);
                g2.dispose();
            }
        };
        decorCircle2.setBounds(120, 300, 180, 180);
        panelLeft.add(decorCircle2);

        // ─── Imagen del logo ────────────────────────────────────────────────────────
        JLabel lblLogoImg = new JLabel();
        try {
            java.awt.Image imgLogo = new javax.swing.ImageIcon(
                getClass().getResource("/Img/pizzeria.png")
            ).getImage().getScaledInstance(110, 110, java.awt.Image.SCALE_SMOOTH);
            lblLogoImg.setIcon(new javax.swing.ImageIcon(imgLogo));
        } catch (Exception ex) {
            lblLogoImg.setText("🍕"); // Fallback si no carga
            lblLogoImg.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 60));
        }
        lblLogoImg.setHorizontalAlignment(SwingConstants.CENTER);
        lblLogoImg.setBounds(0, 60, 290, 120);
        panelLeft.add(lblLogoImg);

        lblLogo = new JLabel("COMUNEROS");
        lblLogo.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblLogo.setForeground(Color.WHITE);
        lblLogo.setHorizontalAlignment(SwingConstants.CENTER);
        lblLogo.setBounds(0, 185, 290, 40);
        panelLeft.add(lblLogo);

        lblSubtitle = new JLabel("Puente Nacional");
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        lblSubtitle.setForeground(new Color(147, 197, 253));
        lblSubtitle.setHorizontalAlignment(SwingConstants.CENTER);
        lblSubtitle.setBounds(0, 225, 290, 25);
        panelLeft.add(lblSubtitle);

        JLabel lblCopyright = new JLabel("\u00a9 2025 alejopwn. Todos los derechos reservados.");
        lblCopyright.setFont(new Font("Segoe UI", Font.PLAIN, 9));
        lblCopyright.setForeground(new Color(80, 120, 180));
        lblCopyright.setHorizontalAlignment(SwingConstants.CENTER);
        lblCopyright.setBounds(0, 400, 290, 20);
        panelLeft.add(lblCopyright);

        // ─── PANEL DERECHO (formulario blanco) ───────────────────────────────────
        panelRight = new JPanel(null);
        panelRight.setBackground(Color.WHITE);

        JLabel lblBienvenido = new JLabel("Bienvenido de vuelta");
        lblBienvenido.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblBienvenido.setForeground(new Color(15, 23, 42));
        lblBienvenido.setHorizontalAlignment(SwingConstants.CENTER);
        lblBienvenido.setBounds(0, 50, 410, 30);
        panelRight.add(lblBienvenido);

        JLabel lblHint = new JLabel("Ingrese sus credenciales para continuar");
        lblHint.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblHint.setForeground(new Color(107, 114, 128));
        lblHint.setHorizontalAlignment(SwingConstants.CENTER);
        lblHint.setBounds(0, 82, 410, 20);
        panelRight.add(lblHint);

        // Label Correo
        JLabel lblEmailLbl = new JLabel("Correo electrónico");
        lblEmailLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblEmailLbl.setForeground(new Color(55, 65, 81));
        lblEmailLbl.setBounds(40, 130, 200, 20);
        panelRight.add(lblEmailLbl);

        // Campo correo con borde redondeado
        txtCorreo = new JTextField() {
            @Override
            protected void paintBorder(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(isFocusOwner() ? new Color(59, 130, 246) : new Color(209, 213, 219));
                g2.setStroke(new BasicStroke(isFocusOwner() ? 2f : 1.5f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2.dispose();
            }
        };
        txtCorreo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtCorreo.setForeground(new Color(55, 65, 81));
        txtCorreo.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
        txtCorreo.setOpaque(true);
        txtCorreo.setBackground(new Color(249, 250, 251));
        txtCorreo.setBounds(40, 155, 330, 45);
        panelRight.add(txtCorreo);

        // Placeholder
        txtCorreo.setText("usuario@restaurante.com");
        txtCorreo.setForeground(new Color(156, 163, 175));
        txtCorreo.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent e) {
                if (txtCorreo.getText().equals("usuario@restaurante.com")) {
                    txtCorreo.setText("");
                    txtCorreo.setForeground(new Color(55, 65, 81));
                }
                txtCorreo.repaint();
            }
            public void focusLost(java.awt.event.FocusEvent e) {
                if (txtCorreo.getText().isEmpty()) {
                    txtCorreo.setText("usuario@restaurante.com");
                    txtCorreo.setForeground(new Color(156, 163, 175));
                }
                txtCorreo.repaint();
            }
        });

        // Label Pass
        JLabel lblPassLbl = new JLabel("Contraseña");
        lblPassLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblPassLbl.setForeground(new Color(55, 65, 81));
        lblPassLbl.setBounds(40, 215, 200, 20);
        panelRight.add(lblPassLbl);

        // Campo contraseña
        txtPass = new JPasswordField() {
            @Override
            protected void paintBorder(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(isFocusOwner() ? new Color(59, 130, 246) : new Color(209, 213, 219));
                g2.setStroke(new BasicStroke(isFocusOwner() ? 2f : 1.5f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2.dispose();
            }
        };
        txtPass.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtPass.setForeground(new Color(156, 163, 175));
        txtPass.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
        txtPass.setBackground(new Color(249, 250, 251));
        txtPass.setEchoChar((char) 0);
        txtPass.setText("••••••••");
        txtPass.setBounds(40, 240, 330, 45);
        panelRight.add(txtPass);
        txtPass.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent e) {
                if (String.valueOf(txtPass.getPassword()).equals("••••••••")) {
                    txtPass.setText("");
                    txtPass.setForeground(new Color(55, 65, 81));
                    txtPass.setEchoChar('●');
                }
                txtPass.repaint();
            }
            public void focusLost(java.awt.event.FocusEvent e) {
                if (String.valueOf(txtPass.getPassword()).isEmpty()) {
                    txtPass.setEchoChar((char) 0);
                    txtPass.setText("••••••••");
                    txtPass.setForeground(new Color(156, 163, 175));
                }
                txtPass.repaint();
            }
        });

        // Barra de progreso (quitada por solicitud - acceso directo)
        barra = new JProgressBar(); // Se mantiene como variable pero NO se agrega al panel


        // Botón Ingresar
        btnIniciar = new JButton("Ingresar") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color base = getModel().isPressed() ? new Color(29, 78, 216) :
                             getModel().isRollover() ? new Color(96, 165, 250) : new Color(59, 130, 246);
                g2.setColor(base);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnIniciar.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnIniciar.setForeground(Color.WHITE);
        btnIniciar.setContentAreaFilled(false);
        btnIniciar.setBorderPainted(false);
        btnIniciar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnIniciar.setBounds(40, 315, 330, 50);
        btnIniciar.addActionListener(e -> validar());
        panelRight.add(btnIniciar);

        // Botón Salir (link-style)
        jButton1 = new JButton("Salir del sistema");
        jButton1.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        jButton1.setForeground(new Color(107, 114, 128));
        jButton1.setBorderPainted(false);
        jButton1.setContentAreaFilled(false);
        jButton1.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        jButton1.setBounds(130, 375, 150, 30);
        jButton1.addActionListener(e -> System.exit(0));
        panelRight.add(jButton1);

        // ─── Enter en campo contraseña ────────────────────────────────────────────
        txtPass.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent e) {
                if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) validar();
            }
        });
        txtCorreo.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent e) {
                if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) txtPass.requestFocus();
            }
        });

        // ─── Ensamblar ventana ────────────────────────────────────────────────────
        getContentPane().add(panelLeft, BorderLayout.WEST);
        getContentPane().add(panelRight, BorderLayout.CENTER);

        // Borde redondeado de la ventana
        setShape(new RoundRectangle2D.Double(0, 0, 700, 460, 20, 20));
        ImageIcon img = new ImageIcon(getClass().getResource("/Img/Login.png"));
        this.setIconImage(img.getImage());
    }



    private void shakeWindow() {
        int originalX = getX();
        shakeCount = 0;
        shakeTimer = new Timer(25, e -> {
            shakeCount++;
            int offset = (shakeCount % 2 == 0) ? 8 : -8;
            setLocation(originalX + offset, getY());
            if (shakeCount >= 8) {
                ((Timer) e.getSource()).stop();
                setLocation(originalX, getY());
            }
        });
        shakeTimer.start();
    }

    public void validar() {
        String correo = txtCorreo.getText();
        String pass = String.valueOf(txtPass.getPassword());

        if (correo.equals("usuario@restaurante.com") || pass.equals("••••••••") || correo.isEmpty() || pass.isEmpty()) {
            shakeWindow();
            ToastNotification.advertencia(btnIniciar, "Por favor ingrese sus credenciales.");
            return;
        }

        // Feedback de carga en el botón
        btnIniciar.setText("⏳ Verificando...");
        btnIniciar.setEnabled(false);

        new SwingWorker<Modelo.login, Void>() {
            @Override
            protected Modelo.login doInBackground() {
                return loginDao.log(correo, pass);
            }
            @Override
            protected void done() {
                try {
                    lg = get();
                    if (lg.getCorreo() != null && lg.getPass() != null) {
                        // Acceso directo sin barra de carga
                        btnIniciar.setText("✅ Bienvenido!");
                        javax.swing.Timer t = new javax.swing.Timer(400, null);
                        t.setRepeats(false); // Solo una vez
                        t.addActionListener(evt -> {
                            Sistema sis = new Sistema(lg);
                            sis.setVisible(true);
                            dispose();
                        });
                        t.start();
                    } else {
                        shakeWindow();
                        ToastNotification.error(btnIniciar, "Correo o contraseña incorrectos");
                        btnIniciar.setText("Ingresar");
                        btnIniciar.setEnabled(true);
                    }
                } catch (Exception ex) {
                    ToastNotification.error(btnIniciar, "Error de conexión con la base de datos");
                    btnIniciar.setText("Ingresar");
                    btnIniciar.setEnabled(true);
                }
            }
        }.execute();
    }

    // ─── Variables declaration ─────────────────────────────────────────────────
    // (sin GEN-BEGIN para permitir edición completa)
}
