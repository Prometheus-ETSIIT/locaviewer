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

package gui;

import control.DatosNino;
import java.awt.Color;
import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.security.Security;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.swing.JOptionPane;

/**
 * Formulario de inicio de sesión.
 */
public class InicioSesion extends javax.swing.JFrame {
    
    /**
     * Crea el formulario de inicio de sesión.
     */
    public InicioSesion() {
        initComponents();
        
        this.setBackground(Color.white);
        this.getContentPane().setBackground(Color.white);
        this.setIconImage(Toolkit.getDefaultToolkit().getImage(InicioSesion.class.getResource("icon.png")));
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        txtUser = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        btnConnect = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();
        txtPassword = new javax.swing.JPasswordField();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Inicio de sesión");
        setBackground(new java.awt.Color(255, 255, 255));
        setMaximumSize(new java.awt.Dimension(185, 325));
        setMinimumSize(new java.awt.Dimension(185, 325));
        setResizable(false);

        jLabel1.setForeground(new java.awt.Color(0, 102, 255));
        jLabel1.setText("Usuario:");

        txtUser.setToolTipText("Introduce aquí tu usuario.");

        jLabel2.setForeground(new java.awt.Color(0, 102, 255));
        jLabel2.setText("Contraseña:");

        jLabel3.setText("by Prometheus");

        btnConnect.setFont(new java.awt.Font("Dialog", 1, 14)); // NOI18N
        btnConnect.setText("¡Conectar!");
        btnConnect.setContentAreaFilled(false);
        btnConnect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnConnectActionPerformed(evt);
            }
        });

        jLabel4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gui/logo.png"))); // NOI18N

        txtPassword.setToolTipText("Introduce aquí tu contraseña.");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addComponent(btnConnect))
                    .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 153, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                            .addGap(46, 46, 46)
                            .addComponent(jLabel3))
                        .addComponent(txtUser, javax.swing.GroupLayout.PREFERRED_SIZE, 153, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel1)
                        .addComponent(jLabel2))
                    .addComponent(txtPassword, javax.swing.GroupLayout.PREFERRED_SIZE, 153, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(20, 20, 20))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtUser, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtPassword, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnConnect)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 18, Short.MAX_VALUE)
                .addComponent(jLabel3)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnConnectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnConnectActionPerformed
        // TODO: Inicio de sesión en servidor
        ArrayList<DatosNino> datos = new ArrayList<>();
        //DatosNino[] datos = new DatosNino[1];
        //datos[0] = DatosNino.FromSummary("20:14:04:11:34:37,Benito Palacios Sánchez, Beni");
        //datos[1] = DatosNino.FromSummary("42049184,Alberto Palacios Sánchez, Alber");
        
        String pw = new String(txtPassword.getPassword());
        String usuario = txtUser.getText();
        
        String [] datosNuevos;
        Socket socket = creaSocketSeguro("localhost", 6556);
        
        System.out.println("ME CONECTO");
        /*
        InputStream inStream = null;
        try {
            DataOutputStream writer = new DataOutputStream(socket.getOutputStream());
            inStream = socket.getInputStream();
            DataInputStream reader = new DataInputStream(inStream);

  
            System.out.println("ME VOY A ESCRIBIR");
            writer.writeUTF("autentificar padre "+usuario+" "+pw);
             System.out.println("VOY A LEER");
            String respuesta = reader.readUTF();
            
            if(respuesta.equals("No autentificado")){
                JOptionPane.showMessageDialog(null, "Hubo algún problema en la autentificación");
            }
            else{
                
                do{
                    datosNuevos = respuesta.split(" ");
                    datos.add(DatosNino.FromSummary(datosNuevos[0]+datosNuevos[2]+datosNuevos[2]));
                    
                    respuesta = reader.readUTF();
                }while(!respuesta.equals("fin"));
            }

           
        } catch (IOException ex) {
            Logger.getLogger(InicioSesion.class.getName()).log(Level.SEVERE, null, ex);
        }
*/
   
            InputStreamReader leer = new InputStreamReader(System.in);
            BufferedReader buff = new BufferedReader(leer);
            
            DataInputStream reader = null;
            
            DataOutputStream writer = null;
        try {
            writer = new DataOutputStream(socket.getOutputStream());
        } catch (IOException ex) {
            Logger.getLogger(InicioSesion.class.getName()).log(Level.SEVERE, null, ex);
        }
            InputStream inStream;
        try {
            inStream = socket.getInputStream();
            reader = new DataInputStream(inStream);
        } catch (IOException ex) {
            Logger.getLogger(InicioSesion.class.getName()).log(Level.SEVERE, null, ex);
        }
            
        try {
            writer.writeUTF("autentificar padre "+usuario+" "+pw);
        } catch (IOException ex) {
            Logger.getLogger(InicioSesion.class.getName()).log(Level.SEVERE, null, ex);
        }
                String respuesta = null;
              
 
        try {
            respuesta = reader.readUTF();
            
            while(!respuesta.equals("fin")){
                String [] comando = respuesta.split(" ");
                System.out.println(comando[0]+","+comando[1]+","+comando[2]);
                datos.add(DatosNino.FromSummary(comando[0]+","+comando[1]+","+comando[2]));
                respuesta = reader.readUTF();
            }
        } catch (IOException ex) {
            Logger.getLogger(InicioSesion.class.getName()).log(Level.SEVERE, null, ex);
        }
            
        try {
            socket.close();
            
        } catch (IOException ex) {
            Logger.getLogger(InicioSesion.class.getName()).log(Level.SEVERE, null, ex);
        }
      
        System.out.println("HOLA VAMOS");
        
        DatosNino[] data = new DatosNino[datos.size()];
        for(int i=0;i<datos.size();i++){
            data[i] = datos.get(i);
        }
        
        
        System.out.println(data[0].getApodo());
        
        
        
        this.onSuccessLogin(data);
        
        
    }//GEN-LAST:event_btnConnectActionPerformed
    
    private static Socket creaSocketSeguro(final String host, final int puerto) {
        SSLSocket socket = null;
        
        try {
            // Le indicamos de qué anillo obtener las claves públicas fiables
            // de autoridades de certificación:
            System.setProperty(
                    "javax.net.ssl.trustStore",
                    "cacerts.jks"
            );
            
          
            Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
            SSLSocketFactory factory = (SSLSocketFactory)SSLSocketFactory.getDefault();
            socket = (SSLSocket)factory.createSocket(host, puerto);

            socket.setEnabledCipherSuites(socket.getSupportedCipherSuites());
        } catch (IOException ex) {
            System.out.println("ERROR: " + ex.getMessage());
        }
        
        return socket;
    }
    
    
    /**
     * Se llama por la subventana de inicio de sesión cuando se realiza con
     * éxito el login.
     * 
     * @param children ID de los niños a los que conectarse.
     */
    public void onSuccessLogin(final DatosNino[] children) {
        // Turno de la ventana principal
        new MainWindow(children).setVisible(true);
        
        // Cerramos esta ventana
        this.setVisible(false);
        this.dispose();
    }
    
    /**
     * @param args Sin argumentos.
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                new InicioSesion().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnConnect;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPasswordField txtPassword;
    private javax.swing.JTextField txtUser;
    // End of variables declaration//GEN-END:variables
}
