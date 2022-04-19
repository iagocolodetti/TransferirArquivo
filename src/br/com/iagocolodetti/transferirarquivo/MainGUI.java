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

import br.com.iagocolodetti.transferirarquivo.exception.ClienteConectarException;
import br.com.iagocolodetti.transferirarquivo.exception.EnviarArquivoException;
import br.com.iagocolodetti.transferirarquivo.exception.ServidorLigarException;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.text.DefaultCaret;

/**
 *
 * @author iagocolodetti
 */
public class MainGUI extends javax.swing.JFrame {

    private final String ARQUIVO_PROPERTIES = "TransferirArquivo.properties";

    private Servidor servidor = null;
    private List<File> svArquivos = null;
    private long svTamanhoTotal = 0;
    private String svUltDir = "";

    private Cliente cliente = null;
    private List<File> clArquivos = null;
    private long clTamanhoTotal = 0;
    private String clUltDir = "";

    private void resetarPropriedades() {
        try {
            Properties p = new Properties();
            p.setProperty("svPorta", "");
            p.setProperty("svDir", "");
            p.setProperty("svUltDir", "");
            p.setProperty("svCbLote", "");
            p.setProperty("clIP", "");
            p.setProperty("clPorta", "");
            p.setProperty("clDir", "");
            p.setProperty("clUltDir", "");
            p.setProperty("clCbLote", "");
            p.store(new FileWriter(ARQUIVO_PROPERTIES), "Arquivo de propriedades");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void carregarPropriedades() {
        try {
            if (new File(ARQUIVO_PROPERTIES).exists()) {
                Properties p = new Properties();
                p.load(new FileReader(ARQUIVO_PROPERTIES));
                svTfPorta.setText(p.getProperty("svPorta"));
                svTfDir.setText(p.getProperty("svDir"));
                svUltDir = p.getProperty("svUltDir");
                svCbLote.setSelected(Boolean.valueOf(p.getProperty("svCbLote")));
                clTfIP.setText(p.getProperty("clIP"));
                clTfPorta.setText(p.getProperty("clPorta"));
                clTfDir.setText(p.getProperty("clDir"));
                clUltDir = p.getProperty("clUltDir");
                clCbLote.setSelected(Boolean.valueOf(p.getProperty("clCbLote")));
            } else {
                resetarPropriedades();
            }
        } catch (IOException ex) {
            resetarPropriedades();
            ex.printStackTrace();
        }
    }

    private void salvarPropriedades() {
        try {
            Properties p = new Properties();
            p.setProperty("svPorta", svTfPorta.getText());
            p.setProperty("svDir", svTfDir.getText());
            p.setProperty("svUltDir", svUltDir);
            p.setProperty("svCbLote", Boolean.toString(svCbLote.isSelected()));
            p.setProperty("clIP", clTfIP.getText());
            p.setProperty("clPorta", clTfPorta.getText());
            p.setProperty("clDir", clTfDir.getText());
            p.setProperty("clUltDir", clUltDir);
            p.setProperty("clCbLote", Boolean.toString(clCbLote.isSelected()));
            p.store(new FileWriter(ARQUIVO_PROPERTIES), "Arquivo de propriedades");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    // <editor-fold defaultstate="collapsed" desc="Métodos para o servidor">
    public void svAddArquivos(List<File> arquivos, long tamanhoTotal) {
        if (arquivos != null && !arquivos.isEmpty()) {
            this.svArquivos = arquivos;
            svTfArquivo.setText("Vários");
            svTamanhoTotal = tamanhoTotal;
            svTfTamanho.setText(Utils.calcularTamanho(svCbTipo.getSelectedItem().toString(), svTamanhoTotal));
        }
    }

    public void svSetUltDir(String ultDir) {
        this.svUltDir = ultDir;
    }

    public void svAddAreaTextLog(String mensagem) {
        if (!svTALog.getText().isEmpty()) {
            svTALog.append("\n");
        }
        svTALog.append(String.format("[%s] %s", new SimpleDateFormat("HH:mm:ss").format(new Date()), mensagem));
    }

    public void svSetCliente(String IP) {
        svTfCliente.setText(IP);
    }

    public void svSetStatus(String status) {
        svTfStatus.setText(status);
    }

    public void svSetProgressPainted(boolean painted) {
        svPb.setStringPainted(painted);
    }

    public void svSetProgress(int valor) {
        svPb.setValue(valor);
    }

    private void svLigar() {
        if (!svTfPorta.getText().isEmpty()) {
            int porta;
            try {
                porta = Integer.parseInt(svTfPorta.getText());
                servidor.ligar(porta, svTfDir.getText());
            } catch (NumberFormatException ex) {
                Utils.msgBoxErro(rootPane, "A porta deve ser definida com número inteiro.");
            } catch (ServidorLigarException ex) {
                Utils.msgBoxErro(rootPane, ex.getMessage());
                if (ex.getErrorCode() == 1) {
                    svTfDir.setText(Utils.selecionarDir(this));
                }
            }
        } else {
            Utils.msgBoxErro(rootPane, "Defina o número da porta.");
        }
    }

    public void svLigando() {
        svBtLigar.setText("LIGANDO");
        svTfPorta.setEditable(false);
    }

    public void svLigado(String IP) {
        svTfIP.setText(IP);
        svBtLigar.setText("DESLIGAR");
    }

    private void svDesligar() {
        servidor.desligar();
    }

    public void svDesligado() {
        svTfIP.setText("");
        svTfPorta.setEditable(true);
        svTfCliente.setText("");
        svTfStatus.setText("");
        svPb.setValue(0);
        svBtLigar.setText("LIGAR");
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Métodos para o cliente">
    public void clAddArquivos(List<File> arquivos, long tamanhoTotal) {
        if (arquivos != null && !arquivos.isEmpty()) {
            this.clArquivos = arquivos;
            clTfArquivo.setText("Vários");
            clTamanhoTotal = tamanhoTotal;
            clTfTamanho.setText(Utils.calcularTamanho(clCbTipo.getSelectedItem().toString(), clTamanhoTotal));
        }
    }

    public void clSetUltDir(String ultDir) {
        this.clUltDir = ultDir;
    }

    public void clAddAreaTextLog(String mensagem) {
        if (!clTALog.getText().isEmpty()) {
            clTALog.append("\n");
        }
        clTALog.append(String.format("[%s] %s", new SimpleDateFormat("HH:mm:ss").format(new Date()), mensagem));
    }

    public void clSetStatus(String status) {
        clTfStatus.setText(status);
    }

    public void clSetProgressPainted(boolean painted) {
        clPb.setStringPainted(painted);
    }

    public void clSetProgress(int valor) {
        clPb.setValue(valor);
    }

    private void clConectar() {
        if (!clTfPorta.getText().isEmpty()) {
            int porta;
            try {
                porta = Integer.parseInt(clTfPorta.getText());
                cliente.conectar(clTfIP.getText(), porta, clTfDir.getText());
            } catch (NumberFormatException ex) {
                Utils.msgBoxErro(rootPane, "A porta deve ser definida com número inteiro.");
            } catch (ClienteConectarException ex) {
                Utils.msgBoxErro(rootPane, ex.getMessage());
                if (ex.getErrorCode() == 1) {
                    clTfDir.setText(Utils.selecionarDir(this));
                }
            }
        } else {
            Utils.msgBoxErro(rootPane, "Defina o número da porta.");
        }
    }

    public void clConectando() {
        clTfIP.setEditable(false);
        clTfPorta.setEditable(false);
        clBtConectar.setText("CONECTANDO");
    }

    public void clConectado() {
        clBtConectar.setText("DESCONECTAR");
    }

    private void clDesconectar() {
        cliente.desconectar();
    }

    public void clDesconectado() {
        clTfIP.setEditable(true);
        clTfPorta.setEditable(true);
        clBtConectar.setText("CONECTAR");
    }
    // </editor-fold>

    /**
     * Creates new form MainGUI
     */
    public MainGUI() {
        initComponents();
        servidor = new Servidor(this);
        svArquivos = new ArrayList<>();
        cliente = new Cliente(this);
        clArquivos = new ArrayList<>();
        DefaultCaret svCaret = (DefaultCaret) svTALog.getCaret();
        svCaret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        DefaultCaret clCaret = (DefaultCaret) clTALog.getCaret();
        clCaret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        carregarPropriedades();
        svTfArquivo.setDropTarget(new DropTarget() {
            @Override
            public synchronized void drop(DropTargetDropEvent dtde) {
                try {
                    dtde.acceptDrop(DnDConstants.ACTION_COPY);
                    List droppedFiles = (List) dtde.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                    svArquivos.clear();
                    if (droppedFiles.size() == 1) {
                        Object object = droppedFiles.get(0);
                        if (object instanceof File) {
                            File arquivo = (File) object;
                            svArquivos.add(arquivo);
                            svUltDir = arquivo.getParent();
                            svTfArquivo.setText(svArquivos.get(0).getName());
                            svTfTamanho.setText(Utils.calcularTamanho(svCbTipo.getSelectedItem().toString(), svArquivos.get(0).length()));
                        }
                    } else if (droppedFiles.size() > 1) {
                        for (Object object : droppedFiles) {
                            if (object instanceof File) {
                                File arquivo = (File) object;
                                svArquivos.add(arquivo);
                                svTamanhoTotal += arquivo.length();
                                svTfArquivo.setText("Vários");
                                svTfTamanho.setText(Utils.calcularTamanho(svCbTipo.getSelectedItem().toString(), svTamanhoTotal));
                            }
                        }
                    }
                } catch (UnsupportedFlavorException | IOException ex) {
                    ex.printStackTrace();
                }
            }
        });
        clTfArquivo.setDropTarget(new DropTarget() {
            @Override
            public synchronized void drop(DropTargetDropEvent dtde) {
                try {
                    dtde.acceptDrop(DnDConstants.ACTION_COPY);
                    List droppedFiles = (List) dtde.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                    clArquivos.clear();
                    if (droppedFiles.size() == 1) {
                        Object object = droppedFiles.get(0);
                        if (object instanceof File) {
                            File arquivo = (File) object;
                            clArquivos.add(arquivo);
                            clUltDir = arquivo.getParent();
                            clTfArquivo.setText(clArquivos.get(0).getName());
                            clTfTamanho.setText(Utils.calcularTamanho(clCbTipo.getSelectedItem().toString(), clArquivos.get(0).length()));
                        }
                    } else if (droppedFiles.size() > 1) {
                        for (Object object : droppedFiles) {
                            if (object instanceof File) {
                                File arquivo = (File) object;
                                clArquivos.add(arquivo);
                                clTamanhoTotal += arquivo.length();
                                clTfArquivo.setText("Vários");
                                clTfTamanho.setText(Utils.calcularTamanho(clCbTipo.getSelectedItem().toString(), clTamanhoTotal));
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

        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        svLbIP = new javax.swing.JLabel();
        svLbPorta = new javax.swing.JLabel();
        svTfIP = new javax.swing.JTextField();
        svTfPorta = new javax.swing.JTextField();
        svTfDir = new javax.swing.JTextField();
        svLbDir = new javax.swing.JLabel();
        svBtLigar = new javax.swing.JButton();
        svLbArquivo = new javax.swing.JLabel();
        svTfArquivo = new javax.swing.JTextField();
        svLbTamanho = new javax.swing.JLabel();
        svTfTamanho = new javax.swing.JTextField();
        svCbTipo = new javax.swing.JComboBox<>();
        svBtSelecionar = new javax.swing.JButton();
        svBtEnviar = new javax.swing.JButton();
        svLbCliente = new javax.swing.JLabel();
        svTfCliente = new javax.swing.JTextField();
        svLbStatus = new javax.swing.JLabel();
        svTfStatus = new javax.swing.JTextField();
        svPb = new javax.swing.JProgressBar();
        jScrollPane1 = new javax.swing.JScrollPane();
        svTALog = new javax.swing.JTextArea();
        svCbLote = new javax.swing.JCheckBox();
        jPanel2 = new javax.swing.JPanel();
        clLbIP = new javax.swing.JLabel();
        clTfIP = new javax.swing.JTextField();
        clLbPorta = new javax.swing.JLabel();
        clTfPorta = new javax.swing.JTextField();
        clTfDir = new javax.swing.JTextField();
        clLbDir = new javax.swing.JLabel();
        clBtConectar = new javax.swing.JButton();
        clLbArquivo = new javax.swing.JLabel();
        clTfArquivo = new javax.swing.JTextField();
        clLbTamanho = new javax.swing.JLabel();
        clTfTamanho = new javax.swing.JTextField();
        clCbTipo = new javax.swing.JComboBox<>();
        clBtSelecionar = new javax.swing.JButton();
        clBtEnviar = new javax.swing.JButton();
        clTfStatus = new javax.swing.JTextField();
        clLbStatus = new javax.swing.JLabel();
        clPb = new javax.swing.JProgressBar();
        jScrollPane2 = new javax.swing.JScrollPane();
        clTALog = new javax.swing.JTextArea();
        clCbLote = new javax.swing.JCheckBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("TransferirArquivo");
        setIconImage(new ImageIcon(getClass().getResource("/br/com/iagocolodetti/transferirarquivo/resource/icone.png")).getImage());
        setMaximumSize(new java.awt.Dimension(600, 767));
        setMinimumSize(new java.awt.Dimension(600, 767));
        setName("FramePrincipal"); // NOI18N
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        jTabbedPane1.setBackground(new java.awt.Color(230, 230, 230));
        jTabbedPane1.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jTabbedPane1.setMaximumSize(new java.awt.Dimension(1920, 1080));
        jTabbedPane1.setMinimumSize(new java.awt.Dimension(1, 1));
        jTabbedPane1.setPreferredSize(new java.awt.Dimension(470, 800));

        jPanel1.setBackground(new java.awt.Color(235, 235, 235));

        svLbIP.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        svLbIP.setText("IP:");

        svLbPorta.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        svLbPorta.setText("Porta:");

        svTfIP.setEditable(false);
        svTfIP.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        svTfIP.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        svTfIP.setFocusable(false);

        svTfPorta.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        svTfPorta.setHorizontalAlignment(javax.swing.JTextField.CENTER);

        svTfDir.setEditable(false);
        svTfDir.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        svTfDir.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        svTfDir.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                svTfDirMouseClicked(evt);
            }
        });
        svTfDir.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                svTfDirKeyPressed(evt);
            }
        });

        svLbDir.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        svLbDir.setText("Diretório:");

        svBtLigar.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        svBtLigar.setText("LIGAR");
        svBtLigar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                svBtLigarActionPerformed(evt);
            }
        });

        svLbArquivo.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        svLbArquivo.setText("Arquivo:");

        svTfArquivo.setEditable(false);
        svTfArquivo.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        svTfArquivo.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        svTfArquivo.setFocusable(false);

        svLbTamanho.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        svLbTamanho.setText("Tamanho:");

        svTfTamanho.setEditable(false);
        svTfTamanho.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        svTfTamanho.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        svTfTamanho.setFocusable(false);

        svCbTipo.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        svCbTipo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "B", "KB", "MB", "GB", "TB" }));
        svCbTipo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                svCbTipoActionPerformed(evt);
            }
        });

        svBtSelecionar.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        svBtSelecionar.setText("Selecionar Arquivo");
        svBtSelecionar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                svBtSelecionarActionPerformed(evt);
            }
        });

        svBtEnviar.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        svBtEnviar.setText("Enviar");
        svBtEnviar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                svBtEnviarActionPerformed(evt);
            }
        });

        svLbCliente.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        svLbCliente.setText("Cliente Conectado:");

        svTfCliente.setEditable(false);
        svTfCliente.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        svTfCliente.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        svTfCliente.setFocusable(false);

        svLbStatus.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        svLbStatus.setText("Status:");

        svTfStatus.setEditable(false);
        svTfStatus.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        svTfStatus.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        svTfStatus.setFocusable(false);

        svPb.setForeground(new java.awt.Color(0, 204, 51));
        svPb.setFocusable(false);

        svTALog.setEditable(false);
        svTALog.setBackground(new java.awt.Color(240, 240, 240));
        svTALog.setColumns(20);
        svTALog.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        svTALog.setLineWrap(true);
        svTALog.setRows(5);
        svTALog.setWrapStyleWord(true);
        svTALog.setFocusable(false);
        jScrollPane1.setViewportView(svTALog);

        svCbLote.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        svCbLote.setText("Lote");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(svLbIP)
                        .addGap(50, 50, 50)
                        .addComponent(svTfIP)
                        .addGap(18, 18, 18)
                        .addComponent(svLbPorta)
                        .addGap(18, 18, 18)
                        .addComponent(svTfPorta, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(svPb, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(svLbDir)
                        .addGap(10, 10, 10)
                        .addComponent(svTfDir))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(svLbTamanho)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(svTfArquivo, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                .addComponent(svTfTamanho, javax.swing.GroupLayout.PREFERRED_SIZE, 414, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(svCbTipo, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(svBtSelecionar, javax.swing.GroupLayout.PREFERRED_SIZE, 210, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(svCbLote)
                        .addGap(50, 50, 50)
                        .addComponent(svBtEnviar, javax.swing.GroupLayout.PREFERRED_SIZE, 210, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(svBtLigar, javax.swing.GroupLayout.PREFERRED_SIZE, 242, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(166, 166, 166))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(svLbStatus)
                        .addGap(18, 18, 18)
                        .addComponent(svTfStatus))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(svLbArquivo)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(svLbCliente)
                                .addGap(18, 18, 18)
                                .addComponent(svTfCliente, javax.swing.GroupLayout.PREFERRED_SIZE, 441, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(svLbIP)
                    .addComponent(svTfIP, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(svLbPorta)
                    .addComponent(svTfPorta, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(svLbDir)
                    .addComponent(svTfDir, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(svBtLigar)
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(svLbCliente)
                    .addComponent(svTfCliente, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(svLbArquivo)
                    .addComponent(svTfArquivo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(svLbTamanho)
                    .addComponent(svTfTamanho, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(svCbTipo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(svBtSelecionar)
                    .addComponent(svBtEnviar)
                    .addComponent(svCbLote))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(svLbStatus)
                    .addComponent(svTfStatus, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(13, 13, 13)
                .addComponent(svPb, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 349, Short.MAX_VALUE)
                .addContainerGap())
        );

        svTfPorta.getAccessibleContext().setAccessibleName("");

        jTabbedPane1.addTab("      Servidor      ", jPanel1);

        jPanel2.setBackground(new java.awt.Color(235, 235, 235));

        clLbIP.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        clLbIP.setText("IP:");

        clTfIP.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        clTfIP.setHorizontalAlignment(javax.swing.JTextField.CENTER);

        clLbPorta.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        clLbPorta.setText("Porta:");

        clTfPorta.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        clTfPorta.setHorizontalAlignment(javax.swing.JTextField.CENTER);

        clTfDir.setEditable(false);
        clTfDir.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        clTfDir.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        clTfDir.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                clTfDirMouseClicked(evt);
            }
        });
        clTfDir.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                clTfDirKeyPressed(evt);
            }
        });

        clLbDir.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        clLbDir.setText("Diretório:");

        clBtConectar.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        clBtConectar.setText("CONECTAR");
        clBtConectar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clBtConectarActionPerformed(evt);
            }
        });

        clLbArquivo.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        clLbArquivo.setText("Arquivo:");

        clTfArquivo.setEditable(false);
        clTfArquivo.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        clTfArquivo.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        clTfArquivo.setFocusable(false);

        clLbTamanho.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        clLbTamanho.setText("Tamanho:");

        clTfTamanho.setEditable(false);
        clTfTamanho.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        clTfTamanho.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        clTfTamanho.setFocusable(false);

        clCbTipo.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        clCbTipo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "B", "KB", "MB", "GB", "TB" }));
        clCbTipo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clCbTipoActionPerformed(evt);
            }
        });

        clBtSelecionar.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        clBtSelecionar.setText("Selecionar Arquivo");
        clBtSelecionar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clBtSelecionarActionPerformed(evt);
            }
        });

        clBtEnviar.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        clBtEnviar.setText("Enviar");
        clBtEnviar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clBtEnviarActionPerformed(evt);
            }
        });

        clTfStatus.setEditable(false);
        clTfStatus.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        clTfStatus.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        clTfStatus.setFocusable(false);

        clLbStatus.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        clLbStatus.setText("Status:");

        clPb.setForeground(new java.awt.Color(0, 204, 51));
        clPb.setFocusable(false);

        clTALog.setEditable(false);
        clTALog.setBackground(new java.awt.Color(240, 240, 240));
        clTALog.setColumns(20);
        clTALog.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        clTALog.setLineWrap(true);
        clTALog.setRows(5);
        clTALog.setWrapStyleWord(true);
        clTALog.setFocusable(false);
        jScrollPane2.setViewportView(clTALog);

        clCbLote.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        clCbLote.setText("Lote");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(clLbIP)
                        .addGap(50, 50, 50)
                        .addComponent(clTfIP)
                        .addGap(18, 18, 18)
                        .addComponent(clLbPorta)
                        .addGap(18, 18, 18)
                        .addComponent(clTfPorta, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(clBtConectar, javax.swing.GroupLayout.PREFERRED_SIZE, 242, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(166, 166, 166))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(clLbDir)
                        .addGap(10, 10, 10)
                        .addComponent(clTfDir))
                    .addComponent(clPb, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(clLbTamanho)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(clTfArquivo, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                                .addComponent(clTfTamanho, javax.swing.GroupLayout.PREFERRED_SIZE, 414, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(clCbTipo, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(clBtSelecionar, javax.swing.GroupLayout.PREFERRED_SIZE, 210, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(51, 51, 51)
                        .addComponent(clCbLote)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(clBtEnviar, javax.swing.GroupLayout.PREFERRED_SIZE, 210, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(clLbArquivo)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(clLbStatus)
                                .addGap(18, 18, 18)
                                .addComponent(clTfStatus, javax.swing.GroupLayout.PREFERRED_SIZE, 513, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(clLbIP)
                    .addComponent(clTfIP, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(clLbPorta)
                    .addComponent(clTfPorta, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(clLbDir)
                    .addComponent(clTfDir, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(clBtConectar)
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(clLbArquivo)
                    .addComponent(clTfArquivo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(clLbTamanho)
                    .addComponent(clTfTamanho, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(clCbTipo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(clBtSelecionar)
                    .addComponent(clBtEnviar)
                    .addComponent(clCbLote))
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(clLbStatus)
                    .addComponent(clTfStatus, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(13, 13, 13)
                .addComponent(clPb, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 390, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPane1.addTab("      Cliente      ", jPanel2);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 600, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 767, Short.MAX_VALUE))
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    // <editor-fold defaultstate="collapsed" desc="Ações para o servidor">
    private void svBtLigarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_svBtLigarActionPerformed
        if (svBtLigar.getText().equals("LIGAR")) {
            svLigar();
        } else if (svBtLigar.getText().equals("DESLIGAR")) {
            svDesligar();
        }
    }//GEN-LAST:event_svBtLigarActionPerformed

    private void svCbTipoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_svCbTipoActionPerformed
        if (!svArquivos.isEmpty()) {
            if (svArquivos.size() == 1) {
                svTfTamanho.setText(Utils.calcularTamanho(svCbTipo.getSelectedItem().toString(), svArquivos.get(0).length()));
            } else {
                svTfTamanho.setText(Utils.calcularTamanho(svCbTipo.getSelectedItem().toString(), svTamanhoTotal));
            }
        }
    }//GEN-LAST:event_svCbTipoActionPerformed

    private void svBtSelecionarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_svBtSelecionarActionPerformed
        if (!servidor.enviandoArquivos()) {
            if (!svCbLote.isSelected()) {
                try {
                    JFileChooser chooser = new JFileChooser();
                    chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                    if (!svUltDir.isEmpty()) {
                        chooser.setCurrentDirectory(new File(svUltDir));
                    }
                    chooser.setDialogTitle("Selecione o arquivo");

                    if (chooser.showOpenDialog(this) == JFileChooser.OPEN_DIALOG) {
                        svArquivos.clear();
                        svArquivos.add(chooser.getSelectedFile());
                        svUltDir = chooser.getSelectedFile().getParent();
                        svTfArquivo.setText(svArquivos.get(0).getName());
                        svTfTamanho.setText(Utils.calcularTamanho(svCbTipo.getSelectedItem().toString(), svArquivos.get(0).length()));
                    }
                } catch (IndexOutOfBoundsException ex) {
                    ex.printStackTrace();
                }
            } else {
                this.setVisible(false);
                new AddArquivos(this, "sv", svArquivos, svUltDir).setVisible(true);
            }
        } else {
            Utils.msgBoxErro(rootPane, "O servidor ainda está enviando arquivos, aguarde o termino da operação atual.");
        }
    }//GEN-LAST:event_svBtSelecionarActionPerformed

    private void svBtEnviarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_svBtEnviarActionPerformed
        if (svBtLigar.getText().equals("DESLIGAR") && !svTfCliente.getText().isEmpty()) {
            try {
                servidor.enviarArquivos(svArquivos);
            } catch (EnviarArquivoException ex) {
                Utils.msgBoxErro(rootPane, ex.getMessage());
            }
        } else {
            Utils.msgBoxErro(rootPane, "O servidor deve estar ligado e um cliente conectado.");
        }
    }//GEN-LAST:event_svBtEnviarActionPerformed

    private void svTfDirMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_svTfDirMouseClicked
        if (svBtLigar.getText().equals("LIGAR")) {
            svTfDir.setText(Utils.selecionarDir(this));
        }
    }//GEN-LAST:event_svTfDirMouseClicked

    private void svTfDirKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_svTfDirKeyPressed
        if (svBtLigar.getText().equals("LIGAR") && evt.getKeyCode() == KeyEvent.VK_ENTER) {
            svTfDir.setText(Utils.selecionarDir(this));
        }
    }//GEN-LAST:event_svTfDirKeyPressed
    // </editor-fold>

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        salvarPropriedades();
    }//GEN-LAST:event_formWindowClosing

    // <editor-fold defaultstate="collapsed" desc="Ações para o cliente">
    private void clTfDirMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_clTfDirMouseClicked
        if (clBtConectar.getText().equals("CONECTAR")) {
            clTfDir.setText(Utils.selecionarDir(this));
        }
    }//GEN-LAST:event_clTfDirMouseClicked

    private void clTfDirKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_clTfDirKeyPressed
        if (clBtConectar.getText().equals("CONECTAR") && evt.getKeyCode() == KeyEvent.VK_ENTER) {
            clTfDir.setText(Utils.selecionarDir(this));
        }
    }//GEN-LAST:event_clTfDirKeyPressed

    private void clBtConectarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clBtConectarActionPerformed
        if (clBtConectar.getText().equals("CONECTAR")) {
            clConectar();
        } else if (clBtConectar.getText().equals("DESCONECTAR") || clBtConectar.getText().equals("CONECTANDO")) {
            clDesconectar();
        }
    }//GEN-LAST:event_clBtConectarActionPerformed

    private void clCbTipoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clCbTipoActionPerformed
        if (!clArquivos.isEmpty()) {
            if (clArquivos.size() == 1) {
                clTfTamanho.setText(Utils.calcularTamanho(clCbTipo.getSelectedItem().toString(), clArquivos.get(0).length()));
            } else {
                clTfTamanho.setText(Utils.calcularTamanho(clCbTipo.getSelectedItem().toString(), clTamanhoTotal));
            }
        }
    }//GEN-LAST:event_clCbTipoActionPerformed

    private void clBtSelecionarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clBtSelecionarActionPerformed
        if (!cliente.enviandoArquivos()) {
            if (!clCbLote.isSelected()) {
                try {
                    JFileChooser chooser = new JFileChooser();
                    chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                    if (!clUltDir.isEmpty()) {
                        chooser.setCurrentDirectory(new File(clUltDir));
                    }
                    chooser.setDialogTitle("Selecione o arquivo");

                    if (chooser.showOpenDialog(this) == JFileChooser.OPEN_DIALOG) {
                        clArquivos.clear();
                        clArquivos.add(chooser.getSelectedFile());
                        clUltDir = chooser.getSelectedFile().getParent();
                        clTfArquivo.setText(clArquivos.get(0).getName());
                        clTfTamanho.setText(Utils.calcularTamanho(clCbTipo.getSelectedItem().toString(), clArquivos.get(0).length()));
                    }
                } catch (IndexOutOfBoundsException ex) {
                    ex.printStackTrace();
                }
            } else {
                this.setVisible(false);
                new AddArquivos(this, "cl", clArquivos, clUltDir).setVisible(true);
            }
        } else {
            Utils.msgBoxErro(rootPane, "O cliente ainda está enviando arquivos, aguarde o termino da operação atual.");
        }
    }//GEN-LAST:event_clBtSelecionarActionPerformed

    private void clBtEnviarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clBtEnviarActionPerformed
        if (clBtConectar.getText().equals("DESCONECTAR")) {
            try {
                cliente.enviarArquivos(clArquivos);
            } catch (EnviarArquivoException e) {
                Utils.msgBoxErro(rootPane, e.getMessage());
            }
        } else {
            Utils.msgBoxErro(rootPane, "Conecte-se a um servidor.");
        }
    }//GEN-LAST:event_clBtEnviarActionPerformed
    // </editor-fold>

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
            java.util.logging.Logger.getLogger(MainGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(MainGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(MainGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MainGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
                    ex.printStackTrace();
                }
                new MainGUI().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton clBtConectar;
    private javax.swing.JButton clBtEnviar;
    private javax.swing.JButton clBtSelecionar;
    private javax.swing.JCheckBox clCbLote;
    private javax.swing.JComboBox<String> clCbTipo;
    private javax.swing.JLabel clLbArquivo;
    private javax.swing.JLabel clLbDir;
    private javax.swing.JLabel clLbIP;
    private javax.swing.JLabel clLbPorta;
    private javax.swing.JLabel clLbStatus;
    private javax.swing.JLabel clLbTamanho;
    private javax.swing.JProgressBar clPb;
    private javax.swing.JTextArea clTALog;
    private javax.swing.JTextField clTfArquivo;
    private javax.swing.JTextField clTfDir;
    private javax.swing.JTextField clTfIP;
    private javax.swing.JTextField clTfPorta;
    private javax.swing.JTextField clTfStatus;
    private javax.swing.JTextField clTfTamanho;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JButton svBtEnviar;
    private javax.swing.JButton svBtLigar;
    private javax.swing.JButton svBtSelecionar;
    private javax.swing.JCheckBox svCbLote;
    private javax.swing.JComboBox<String> svCbTipo;
    private javax.swing.JLabel svLbArquivo;
    private javax.swing.JLabel svLbCliente;
    private javax.swing.JLabel svLbDir;
    private javax.swing.JLabel svLbIP;
    private javax.swing.JLabel svLbPorta;
    private javax.swing.JLabel svLbStatus;
    private javax.swing.JLabel svLbTamanho;
    private javax.swing.JProgressBar svPb;
    private javax.swing.JTextArea svTALog;
    private javax.swing.JTextField svTfArquivo;
    private javax.swing.JTextField svTfCliente;
    private javax.swing.JTextField svTfDir;
    private javax.swing.JTextField svTfIP;
    private javax.swing.JTextField svTfPorta;
    private javax.swing.JTextField svTfStatus;
    private javax.swing.JTextField svTfTamanho;
    // End of variables declaration//GEN-END:variables
}
