package Modelo;

import java.awt.Color;
import java.awt.Component;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

public class Tables extends DefaultTableCellRenderer {

    @Override
    public Component getTableCellRendererComponent(JTable jtable, Object o, boolean bln, boolean bln1, int row, int col) {
        super.getTableCellRendererComponent(jtable, o, bln, bln1, row, col);
        Color textColor = new Color(241, 245, 249); // Texto claro (Slate 100)

        // Hover
        java.awt.Point p = jtable.getMousePosition();
        int hoverRow = p != null ? jtable.rowAtPoint(p) : -1;

        if (bln) {
            // Fila seleccionada
            this.setBackground(new Color(59, 130, 246)); // Azul
            this.setForeground(Color.WHITE);
            return this;
        }

        String estado = jtable.getValueAt(row, 6).toString();
        String sala = jtable.getValueAt(row, 1).toString();

        if (row == hoverRow) {
            this.setBackground(new Color(51, 65, 85)); // Hover (Slate 700)
            this.setForeground(textColor);
        } else if (estado.equals("PENDIENTE")) {
            this.setBackground(new Color(120, 80, 20)); // Naranja oscuro
            this.setForeground(new Color(253, 224, 171)); // Texto naranja claro
        } else if (estado.equals("FINALIZADO") && sala.equalsIgnoreCase("TRANSACCIONES")) {
            this.setBackground(new Color(100, 90, 30)); // Amarillo oscuro
            this.setForeground(new Color(253, 243, 180)); // Texto amarillo claro
        } else if (estado.equals("FINALIZADO") && sala.equalsIgnoreCase("EFECTIVO-TRANSACCION")) {
            this.setBackground(new Color(30, 58, 95)); // Azul oscuro
            this.setForeground(new Color(170, 210, 245)); // Texto azul claro
        } else if (estado.equals("FINALIZADO")) {
            if (row % 2 == 0) {
                this.setBackground(new Color(30, 41, 59)); // Cebra par (Slate 800)
            } else {
                this.setBackground(new Color(15, 23, 42)); // Cebra impar (Slate 900)
            }
            this.setForeground(textColor);
        } else {
            if (row % 2 == 0) {
                this.setBackground(new Color(30, 41, 59)); // Cebra par (Slate 800)
            } else {
                this.setBackground(new Color(15, 23, 42)); // Cebra impar (Slate 900)
            }
            this.setForeground(textColor);
        }

        return this;
    }
}