package Modelo;

import java.awt.Color;
import java.awt.Component;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

public class Tables extends DefaultTableCellRenderer {

    @Override
    public Component getTableCellRendererComponent(JTable jtable, Object o, boolean bln, boolean bln1, int row, int col) {
        super.getTableCellRendererComponent(jtable, o, bln, bln1, row, col);
        String estado = jtable.getValueAt(row, 6).toString();
        String sala = jtable.getValueAt(row, 1).toString();
        if (estado.equals("PENDIENTE")) {
            this.setBackground(new Color(253, 235, 208)); // Naranja suave
            this.setForeground(Color.BLACK);
        } else if (estado.equals("FINALIZADO") && sala.equalsIgnoreCase("TRANSACCIONES")) {
            this.setBackground(new Color(252, 243, 207)); // Amarillo suave
            this.setForeground(Color.BLACK);
        } else if (estado.equals("FINALIZADO") && sala.equalsIgnoreCase("EFECTIVO-TRANSACCION")) {
            this.setBackground(new Color(214, 234, 248)); // Azul suave
            this.setForeground(Color.BLACK);
        } else if (estado.equals("FINALIZADO")) {
            this.setBackground(new Color(212, 239, 223)); // Verde suave
            this.setForeground(Color.BLACK);
        } else {
            this.setBackground(Color.WHITE);
            this.setForeground(Color.BLACK);
        }

        return this;
    }
}