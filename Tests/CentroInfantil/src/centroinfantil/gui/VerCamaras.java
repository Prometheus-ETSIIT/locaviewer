/*
 * Copyright (C) 2014 Prometheus
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package centroinfantil.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

/**
 * Diálogo para ver todas las cámaras del sistema clasificadas por habitación.
 */
public class VerCamaras extends javax.swing.JFrame {

    /**
     * Creates new form VerCamaras
     */
    public VerCamaras() {
        initComponents();
        
        this.setTitle("Videovigilancia de Centro Infantil");
        // TODO: Poner icono
        this.setBackground(Color.white);
        this.getContentPane().setBackground(Color.white);
        this.addTab();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        roomTabs = new javax.swing.JTabbedPane();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setPreferredSize(new java.awt.Dimension(900, 620));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(roomTabs, javax.swing.GroupLayout.DEFAULT_SIZE, 570, Short.MAX_VALUE)
                .addGap(0, 0, 0))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(roomTabs, javax.swing.GroupLayout.DEFAULT_SIZE, 439, Short.MAX_VALUE)
                .addGap(0, 0, 0))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void addTab() {
        JPanel panel = new JPanel();
        GridBagLayout layout = new GridBagLayout();
        layout.columnWidths = new int[] { 170, 170, 170, 170, 210 };
        layout.rowHeights   = new int[] {  30, 130, 130, 130, 130 };
        panel.setLayout(layout); 
        GridBagConstraints con = new GridBagConstraints();
        
        // Componentes de selección de habitación.
        JLabel label = new JLabel("Seleccione habitación:");
        con.gridx = 0;
        con.gridy = 0;
        con.gridwidth  = 1;
        con.gridheight = 1;
        con.fill   = GridBagConstraints.NONE;
        con.ipadx  = 0;
        con.ipady  = 0;
        con.insets = new Insets(0, 5, 0, 0);
        con.anchor = GridBagConstraints.LINE_START;
        con.weightx = 0;
        con.weighty = 0;
        panel.add(label, con);
        
        JComboBox combo = new JComboBox(new String[] { "Desactivar" });
        con.gridx = 1;
        con.gridy = 0;
        con.gridwidth  = 1;
        con.gridheight = 1;
        con.fill   = GridBagConstraints.HORIZONTAL;
        con.ipadx  = 0;
        con.ipady  = 0;
        con.insets = new Insets(0, 0, 0, 5);
        con.anchor = GridBagConstraints.LINE_START;
        con.weightx = 1;
        con.weighty = 0;
        panel.add(combo, con);
        
        // Botones para añadir o quitar pestañas
        JButton btnAnadir = new JButton("Añadir habitación");
        btnAnadir.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent me) {
                addTab();
            }
        });
        con.gridx = 2;
        con.gridy = 0;
        con.gridwidth  = 1;
        con.gridheight = 1;
        con.fill   = GridBagConstraints.NONE;
        con.ipadx  = 0;
        con.ipady  = 0;
        con.insets = new Insets(0, 5, 0, 5);
        con.anchor = GridBagConstraints.LINE_END;
        con.weightx = 0;
        con.weighty = 0;
        panel.add(btnAnadir, con);
        
        JButton btnEliminar = new JButton("Eliminar habitación");
        btnEliminar.setEnabled(roomTabs.getTabCount() >= 1);
        btnEliminar.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent me) {
                removeTab();
            }
        });
        con.gridx = 3;
        con.gridy = 0;
        con.gridwidth  = 1;
        con.gridheight = 1;
        con.fill   = GridBagConstraints.NONE;
        con.ipadx  = 0;
        con.ipady  = 0;
        con.insets = new Insets(0, 5, 0, 5);
        con.anchor = GridBagConstraints.LINE_START;
        con.weightx = 0;
        con.weighty = 0;
        panel.add(btnEliminar, con);
        
        // Cámaras
        for (int x = 0; x < 2; x++) {
            for (int y = 0; y < 2; y++) {
                int idCam = x + y * 2;
                
                // Crea el panel de vídeo
                JPanel panelVideo = new JPanel();
                panelVideo.setBackground(Color.black);
                panelVideo.setSize(320, 240);
                panelVideo.setPreferredSize(new Dimension(320, 240));
                con.gridx = x * 2;
                con.gridy = (y * 2) + 1;
                con.gridwidth  = 2;
                con.gridheight = 2;
                con.fill   = GridBagConstraints.BOTH;
                con.ipadx  = 0;
                con.ipady  = 0;
                con.insets = new Insets(5, 5, 5, 5);
                con.anchor = GridBagConstraints.CENTER;
                con.weightx = 0.5;
                con.weighty = 0.5;
                panel.add(panelVideo, con);
                
                // Crea el controlador de la cámara
                JPanel panelControl = new JPanel();
                panelControl.setBorder(new TitledBorder("Cámara " + (idCam + 1)));
                panelControl.setSize(200, 120);
                panelControl.setPreferredSize(new Dimension(200, 120));
                con.gridx = 4;
                con.gridy = (y * 2) + x + 1;
                con.gridwidth  = 1;
                con.gridheight = 1;
                con.fill   = GridBagConstraints.HORIZONTAL;
                con.ipadx  = 0;
                con.ipady  = 0;
                con.insets = new Insets(5, 5, 5, 5);
                con.anchor = GridBagConstraints.CENTER;
                con.weightx = 0;
                con.weighty = 0.5;
                
                // Añade los controles
                panelControl.setLayout(new BoxLayout(panelControl, BoxLayout.Y_AXIS));
                
                JCheckBox checkControl = new JCheckBox("Activar", false);
                checkControl.setName(String.valueOf(idCam));
                checkControl.setAlignmentX(Component.LEFT_ALIGNMENT);
                checkControl.setAlignmentY(Component.TOP_ALIGNMENT);
                panelControl.add(checkControl);
                
                Dimension minFill = new Dimension(0, 20);
                Dimension maxFill = new Dimension(0, 60);
                panelControl.add(new Box.Filler(minFill, minFill, maxFill));
                
                JLabel lblControl = new JLabel("Cámara ID:");
                lblControl.setAlignmentX(Component.LEFT_ALIGNMENT);
                lblControl.setAlignmentY(Component.BOTTOM_ALIGNMENT);
                panelControl.add(lblControl);
                
                JComboBox comboControl = new JComboBox();
                comboControl.setName(String.valueOf(idCam));
                comboControl.setAlignmentX(Component.LEFT_ALIGNMENT);
                comboControl.setAlignmentY(Component.BOTTOM_ALIGNMENT);
                panelControl.add(comboControl);
                
                panel.add(panelControl, con);
            }
        }
        
        roomTabs.addTab("Desactivar", panel);
    }
    
    private void removeTab() {
        int currTab = roomTabs.getSelectedIndex();
        roomTabs.removeTabAt(currTab);
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTabbedPane roomTabs;
    // End of variables declaration//GEN-END:variables
}
