/*
 * Copyright (C) 2019 Iago Colodetti
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
package br.com.iagocolodetti.transferirarquivo;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author iagocolodetti
 */
public class AddArquivos extends javax.swing.JFrame {

    private MainGUI mainGUI = null;
    private String lado = null;
    private List<File> arquivosSelecionados = null;
    private List<File> _arquivos = null;
    private long tamanhoTotal = 0;
    private String ultDir = "";

    private final DefaultTableModel model;

    private void adicionarArquivos() {
        if (arquivosSelecionados != null && !arquivosSelecionados.isEmpty()) {
            for (File arquivo : arquivosSelecionados) {
                if (!_arquivos.contains(arquivo)) {
                    _arquivos.add(arquivo);
                    String tipo = cbTamanhoTotal.getSelectedItem().toString();
                    model.addRow(new Object[]{arquivo.getName(), Utils.calcularTamanho(tipo, arquivo.length()) + " " + tipo});
                    tbArquivos.changeSelection(tbArquivos.getRowCount() - 1, 0, false, false);
                    tamanhoTotal += arquivo.length();
                    tfTamanhoTotal.setText(Utils.calcularTamanho(tipo, tamanhoTotal));
                } else {
                    Utils.msgBoxErro(rootPane, "O arquivo \"" + arquivo.getName() + "\" já foi adicionado à lista.");
                }
            }
            tfArquivo.setText("");
            tfTamanho.setText("");
            arquivosSelecionados.clear();
        }
    }

    private void removerArquivos() {
        int[] rows = tbArquivos.getSelectedRows();
        for (int i = 0; i < rows.length; i++) {
            int index = rows[i] - i;
            if (index > -1) {
                tamanhoTotal -= _arquivos.get(index).length();
                _arquivos.remove(index);
                model.removeRow(index);
                if (index == 0 && tbArquivos.getRowCount() > 0) {
                    tbArquivos.changeSelection(index, 0, false, false);
                } else if (index > 0) {
                    tbArquivos.changeSelection(index - 1, 0, false, false);
                }
                tfTamanhoTotal.setText(Utils.calcularTamanho(cbTamanhoTotal.getSelectedItem().toString(), tamanhoTotal));
            }
        }
    }

    private void removerTodosArquivos() {
        _arquivos.clear();
        model.setRowCount(0);
        tamanhoTotal = 0;
        tfTamanhoTotal.setText("");
    }

    /**
     * Creates new form AddArquivos
     */
    private AddArquivos() {
        initComponents();
        model = (DefaultTableModel) tbArquivos.getModel();
    }

    public AddArquivos(MainGUI mainGUI, String lado, List<File> arquivos, String ultDir) {
        initComponents();
        model = (DefaultTableModel) tbArquivos.getModel();
        this.mainGUI = mainGUI;
        this.lado = lado;
        _arquivos = new ArrayList<>();
        arquivosSelecionados = new ArrayList<>();
        this.ultDir = ultDir;
        if (arquivos != null && !arquivos.isEmpty()) {
            arquivosSelecionados = arquivos;
            adicionarArquivos();
        }
        tbArquivos.setDropTarget(new DropTarget() {
            @Override
            public synchronized void drop(DropTargetDropEvent dtde) {
                try {
                    dtde.acceptDrop(DnDConstants.ACTION_COPY);
                    List droppedFiles = (List) dtde.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                    if (droppedFiles.size() > 0) {
                        for (Object object : droppedFiles) {
                            if (object instanceof File) {
                                File arquivo = (File) object;
                                if (!_arquivos.contains(arquivo)) {
                                    _arquivos.add(arquivo);
                                    String tipo = cbTamanhoTotal.getSelectedItem().toString();
                                    model.addRow(new Object[]{arquivo.getName(), Utils.calcularTamanho(tipo, arquivo.length()) + " " + tipo});
                                    tbArquivos.changeSelection(tbArquivos.getRowCount() - 1, 0, false, false);
                                    tamanhoTotal += arquivo.length();
                                    tfTamanhoTotal.setText(Utils.calcularTamanho(tipo, tamanhoTotal));
                                } else {
                                    Utils.msgBoxErro(rootPane, "O arquivo \"" + arquivo.getName() + "\" já foi adicionado à lista.");
                                }
                            }
                        }
                    }
                } catch (UnsupportedFlavorException | IOException ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lbArquivo = new javax.swing.JLabel();
        tfArquivo = new javax.swing.JTextField();
        lbTamanho = new javax.swing.JLabel();
        tfTamanho = new javax.swing.JTextField();
        cbTipo = new javax.swing.JComboBox<>();
        btSelecionar = new javax.swing.JButton();
        btAdicionar = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        tbArquivos = new javax.swing.JTable();
        btRemover = new javax.swing.JButton();
        lbTamanhoTotal = new javax.swing.JLabel();
        tfTamanhoTotal = new javax.swing.JTextField();
        cbTamanhoTotal = new javax.swing.JComboBox<>();
        btConfirmar = new javax.swing.JButton();
        btCancelar = new javax.swing.JButton();
        btRemoverTodos = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("TransferirArquivo - Adicionar Arquivos");
        setFocusable(false);
        setIconImage(new ImageIcon(getClass().getResource("/br/com/iagocolodetti/transferirarquivo/resource/icone.png")).getImage());
        setMaximumSize(new java.awt.Dimension(600, 767));
        setMinimumSize(new java.awt.Dimension(600, 767));
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        lbArquivo.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        lbArquivo.setText("Arquivo:");

        tfArquivo.setEditable(false);
        tfArquivo.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        tfArquivo.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        tfArquivo.setFocusable(false);

        lbTamanho.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        lbTamanho.setText("Tamanho:");

        tfTamanho.setEditable(false);
        tfTamanho.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        tfTamanho.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        tfTamanho.setFocusable(false);

        cbTipo.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        cbTipo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "B", "KB", "MB", "GB", "TB" }));
        cbTipo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbTipoActionPerformed(evt);
            }
        });

        btSelecionar.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        btSelecionar.setText("Selecionar Arquivo(s)");
        btSelecionar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btSelecionarActionPerformed(evt);
            }
        });

        btAdicionar.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        btAdicionar.setText("Adicionar Arquivo(s)");
        btAdicionar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btAdicionarActionPerformed(evt);
            }
        });

        tbArquivos.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        tbArquivos.setFont(new java.awt.Font("Tahoma", 0, 13)); // NOI18N
        tbArquivos.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Arquivo", "Tamanho"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tbArquivos.setFillsViewportHeight(true);
        tbArquivos.setRowHeight(26);
        tbArquivos.setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        tbArquivos.getTableHeader().setReorderingAllowed(false);
        jScrollPane1.setViewportView(tbArquivos);
        if (tbArquivos.getColumnModel().getColumnCount() > 0) {
            tbArquivos.getColumnModel().getColumn(0).setMinWidth(100);
            tbArquivos.getColumnModel().getColumn(0).setPreferredWidth(420);
            tbArquivos.getColumnModel().getColumn(1).setMinWidth(100);
            tbArquivos.getColumnModel().getColumn(1).setPreferredWidth(160);
        }
        tbArquivos.getTableHeader().setFont(new java.awt.Font("Tahoma", 0, 14));
        tbArquivos.setShowGrid(true);
        jScrollPane1.getViewport().setBackground(java.awt.Color.WHITE);

        btRemover.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        btRemover.setText("Remover Arquivo(s) Selecionado(s)");
        btRemover.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btRemoverActionPerformed(evt);
            }
        });

        lbTamanhoTotal.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        lbTamanhoTotal.setText("Tamanho Total:");

        tfTamanhoTotal.setEditable(false);
        tfTamanhoTotal.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        tfTamanhoTotal.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        tfTamanhoTotal.setFocusable(false);

        cbTamanhoTotal.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        cbTamanhoTotal.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "B", "KB", "MB", "GB", "TB" }));
        cbTamanhoTotal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbTamanhoTotalActionPerformed(evt);
            }
        });

        btConfirmar.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        btConfirmar.setText("Confirmar");
        btConfirmar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btConfirmarActionPerformed(evt);
            }
        });

        btCancelar.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        btCancelar.setText("Cancelar");
        btCancelar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btCancelarActionPerformed(evt);
            }
        });

        btRemoverTodos.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        btRemoverTodos.setText("Remover Todos Arquivos");
        btRemoverTodos.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btRemoverTodosActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 580, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lbTamanho)
                            .addComponent(lbArquivo))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(tfTamanho)
                                .addGap(18, 18, 18)
                                .addComponent(cbTipo, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(tfArquivo)))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(btSelecionar, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btAdicionar, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lbTamanhoTotal)
                        .addGap(18, 18, 18)
                        .addComponent(tfTamanhoTotal)
                        .addGap(18, 18, 18)
                        .addComponent(cbTamanhoTotal, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(btCancelar, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(btConfirmar, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(btRemover, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btRemoverTodos, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lbArquivo)
                    .addComponent(tfArquivo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lbTamanho)
                    .addComponent(tfTamanho, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cbTipo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btSelecionar)
                    .addComponent(btAdicionar))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 493, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btRemoverTodos)
                    .addComponent(btRemover))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lbTamanhoTotal)
                    .addComponent(tfTamanhoTotal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cbTamanhoTotal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btConfirmar)
                    .addComponent(btCancelar))
                .addContainerGap())
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void cbTipoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbTipoActionPerformed
        if (arquivosSelecionados != null && !arquivosSelecionados.isEmpty()) {
            long tamanho = 0;
            for (File arquivo : arquivosSelecionados) {
                tamanho += arquivo.length();
            }
            tfTamanho.setText(Utils.calcularTamanho(cbTipo.getSelectedItem().toString(), tamanho));
        }
    }//GEN-LAST:event_cbTipoActionPerformed

    private void btSelecionarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btSelecionarActionPerformed
        try {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            chooser.setMultiSelectionEnabled(true);
            if (ultDir != null && !ultDir.isEmpty()) {
                chooser.setCurrentDirectory(new File(ultDir));
            }
            chooser.setDialogTitle("Selecione o(s) arquivo(s)");

            if (chooser.showOpenDialog(this) == JFileChooser.OPEN_DIALOG) {
                arquivosSelecionados.clear();
                long tamanho = 0;
                for (File arquivo : chooser.getSelectedFiles()) {
                    arquivosSelecionados.add(arquivo);
                    tamanho += arquivo.length();
                }
                ultDir = chooser.getSelectedFile().getParent();
                tfArquivo.setText((arquivosSelecionados.size() == 1 ? arquivosSelecionados.get(0).getName() : "Vários"));
                tfTamanho.setText(Utils.calcularTamanho(cbTipo.getSelectedItem().toString(), tamanho));
            }
        } catch (IndexOutOfBoundsException ex) {
            ex.printStackTrace();
        }
    }//GEN-LAST:event_btSelecionarActionPerformed

    private void btAdicionarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btAdicionarActionPerformed
        adicionarArquivos();
    }//GEN-LAST:event_btAdicionarActionPerformed

    private void btRemoverActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btRemoverActionPerformed
        removerArquivos();
    }//GEN-LAST:event_btRemoverActionPerformed

    private void cbTamanhoTotalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbTamanhoTotalActionPerformed
        if (!_arquivos.isEmpty()) {
            String tipo = cbTamanhoTotal.getSelectedItem().toString();
            for (int i = 0; i < model.getRowCount(); i++) {
                model.setValueAt(Utils.calcularTamanho(tipo, _arquivos.get(i).length()) + " " + tipo, i, 1);
            }
            tfTamanhoTotal.setText(Utils.calcularTamanho(tipo, tamanhoTotal));
        }
    }//GEN-LAST:event_cbTamanhoTotalActionPerformed

    private void btConfirmarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btConfirmarActionPerformed
        if (_arquivos.size() > 1) {
            if (lado.equals("sv")) {
                mainGUI.svAddArquivos(_arquivos, tamanhoTotal);
                mainGUI.svSetUltDir(ultDir);
            } else if (lado.equals("cl")) {
                mainGUI.clAddArquivos(_arquivos, tamanhoTotal);
                mainGUI.clSetUltDir(ultDir);
            }
            this.dispose();
            mainGUI.setVisible(true);
        } else {
            Utils.msgBoxErro(rootPane, "Adicione pelo menos 2 (dois) arquivos.");
        }
    }//GEN-LAST:event_btConfirmarActionPerformed

    private void btCancelarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btCancelarActionPerformed
        if (lado.equals("sv")) {
            mainGUI.svSetUltDir(ultDir);
        } else if (lado.equals("cl")) {
            mainGUI.clSetUltDir(ultDir);
        }
        this.dispose();
        mainGUI.setVisible(true);
    }//GEN-LAST:event_btCancelarActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        mainGUI.setVisible(true);
    }//GEN-LAST:event_formWindowClosing

    private void btRemoverTodosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btRemoverTodosActionPerformed
        if (JOptionPane.showConfirmDialog(rootPane, "Deseja realmente remover todos os arquivos adicionados?", "Remover Todos Arquivos", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
            removerTodosArquivos();
        }
    }//GEN-LAST:event_btRemoverTodosActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(AddArquivos.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(AddArquivos.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(AddArquivos.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(AddArquivos.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
                    ex.printStackTrace();
                }
                new AddArquivos().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btAdicionar;
    private javax.swing.JButton btCancelar;
    private javax.swing.JButton btConfirmar;
    private javax.swing.JButton btRemover;
    private javax.swing.JButton btRemoverTodos;
    private javax.swing.JButton btSelecionar;
    private javax.swing.JComboBox<String> cbTamanhoTotal;
    private javax.swing.JComboBox<String> cbTipo;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lbArquivo;
    private javax.swing.JLabel lbTamanho;
    private javax.swing.JLabel lbTamanhoTotal;
    private javax.swing.JTable tbArquivos;
    private javax.swing.JTextField tfArquivo;
    private javax.swing.JTextField tfTamanho;
    private javax.swing.JTextField tfTamanhoTotal;
    // End of variables declaration//GEN-END:variables
}
