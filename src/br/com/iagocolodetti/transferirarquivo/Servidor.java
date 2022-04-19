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

import br.com.iagocolodetti.transferirarquivo.exception.EnviarArquivoException;
import br.com.iagocolodetti.transferirarquivo.exception.ServidorLigarException;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.regex.Pattern;

/**
 *
 * @author iagocolodetti
 */
public class Servidor {

    private final int MIN_PORTA = 0;
    private final int MAX_PORTA = 65535;
    
    private MainGUI mainGUI = null;

    private ServerSocket serverSocket = null;
    private Socket socket = null;

    private volatile boolean ligado = false;

    private boolean recebendoArquivo = false;
    Thread enviandoArquivo = null;
    
    private final int AGUARDAR_APOS_ENVIO = 2000;
    private final int AGUARDAR_APOS_ENVIO_LOOP = 10;
    
    public Servidor(MainGUI mainGUI) {
        this.mainGUI = mainGUI;
    }

    public void ligar(int porta, String diretorio) throws ServidorLigarException {
        if (porta < MIN_PORTA || porta > MAX_PORTA) {
            throw new ServidorLigarException("A porta deve ser no mínimo " + MIN_PORTA + " e no máximo " + MAX_PORTA + ".");
        }
        if (diretorio.isEmpty()) {
            throw new ServidorLigarException("Selecione um diretório para recebimento de arquivos.", 1);
        } else {
            try {
                new File(diretorio).mkdirs();
            } catch (SecurityException ex) {
                throw new ServidorLigarException("O diretório selecionado não existe e não foi possível criá-lo.\nSelecione outro diretório ou tente iniciar a aplicação como administrador.");
            }
        }
        mainGUI.svLigando();
        Thread t = new Thread(new ServidorLigar(porta, diretorio));
        t.start();
    }

    public void desligar() {
        try {
            ligado = false;
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            serverSocket = null;
            socket = null;
            mainGUI.svDesligado();
        }
    }

    public void enviarArquivos(List<File> arquivos) throws EnviarArquivoException {
        if (!isSocketConnected()) {
            throw new EnviarArquivoException("É necessário que um cliente esteja conectado para enviar o arquivo.");
        }
        if (arquivos == null || arquivos.isEmpty()) {
            throw new EnviarArquivoException("Selecione pelo menos um arquivo para enviar.");
        }
        if (recebendoArquivo || enviandoArquivos()) {
            throw new EnviarArquivoException("Uma transferência já está em andamento.");
        }
        enviandoArquivo = new Thread(new EnviarArquivos(arquivos));
        enviandoArquivo.start();
    }
    
    public boolean enviandoArquivos() {
        return (enviandoArquivo != null && enviandoArquivo.isAlive());
    }
    
    private boolean isSocketConnected() {
        return (socket != null && !socket.isClosed() && socket.isConnected());
    }

    private void esperar(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }

    private class ServidorLigar implements Runnable {

        private int porta = 0;
        private String diretorio = "";

        public ServidorLigar(int porta, String diretorio) {
            ligado = true;
            this.porta = porta;
            this.diretorio = diretorio;
        }

        @Override
        public void run() {
            try {
                serverSocket = new ServerSocket(porta, 1, InetAddress.getLocalHost());
                mainGUI.svLigado(serverSocket.getInetAddress().getHostAddress());
            } catch (IllegalArgumentException | IOException ex) {
                Utils.msgBoxErro(mainGUI, "Não foi possível iniciar o servidor nessa porta.");
                desligar();
                ex.printStackTrace();
            }
            while (ligado) {
                try {
                    recebendoArquivo = false;
                    socket = serverSocket.accept();
                    mainGUI.svSetCliente(socket.getRemoteSocketAddress().toString().replace("/", "").replace(socket.getRemoteSocketAddress().toString().substring(socket.getRemoteSocketAddress().toString().indexOf(":")), ""));
                } catch (IOException ex) {
                    if (ligado) {
                        Utils.msgBoxErro(mainGUI, "Não foi possível aceitar a conexão com o cliente.");
                        ex.printStackTrace();
                    }
                }
                if (ligado) {
                    InputStream is = null;
                    DataInputStream dis = null;
                    OutputStream out = null;
                    try {
                        is = socket.getInputStream();
                        dis = new DataInputStream(is);
                        String[] data = dis.readUTF().split(Pattern.quote("|"));
                        recebendoArquivo = true;
                        esperar(1000);
                        String novoNomeArquivo = data[0];
                        for (int i = 1; new File(diretorio + novoNomeArquivo).exists(); i++) {
                            novoNomeArquivo = data[0].substring(0, data[0].lastIndexOf(".")) + "(" + i + ")" + data[0].substring(data[0].lastIndexOf("."));
                        }
                        long tamanho = Long.parseLong(data[1]);
                        long tamanhokb = (data[1].equals("0") ? 1 : (tamanho / 1024));
                        tamanhokb = (tamanhokb == 0 ? 1 : tamanhokb);
                        is = socket.getInputStream();
                        out = new FileOutputStream(diretorio + novoNomeArquivo);
                        mainGUI.svSetStatus("Recebendo Arquivo");
                        mainGUI.svAddAreaTextLog("Recebendo Arquivo: \"" + novoNomeArquivo + "\".");
                        mainGUI.svSetProgressPainted(true);
                        byte[] buffer = new byte[1024];
                        long bytesRecebidos = 0;
                        int count;
                        long kb = 0;
                        int progresso;
                        while ((count = is.read(buffer)) > 0) {
                            out.write(buffer, 0, count);
                            bytesRecebidos += count;
                            progresso = (int)(++kb * 100 / tamanhokb);
                            mainGUI.svSetProgress(progresso < 100 ? progresso : 100);
                        }
                        if (bytesRecebidos == tamanho) {
                            mainGUI.svAddAreaTextLog("Arquivo \"" + novoNomeArquivo + "\" recebido com sucesso.");
                        } else {
                            mainGUI.svAddAreaTextLog("Arquivo \"" + novoNomeArquivo + "\" corrompido durante o recebimento.");
                            Utils.msgBoxErro(mainGUI, "Conexão com o cliente perdida.");
                        }
                    } catch (FileNotFoundException ex) {
                        if (ligado && !enviandoArquivos()) {
                            desligar();
                            Utils.msgBoxErro(mainGUI, "O diretório selecionado não foi encontrado.");
                            ex.printStackTrace();
                        }
                    } catch (IOException ex) {
                        if (ligado && !enviandoArquivos()) {
                            Utils.msgBoxErro(mainGUI, "Conexão com o cliente perdida.");
                            ex.printStackTrace();
                        }
                    } finally {
                        try {
                            if (out != null) {
                                out.close();
                            }
                            if (is != null) {
                                is.close();
                            }
                            if (dis != null) {
                                dis.close();
                            }
                            if (socket != null) {
                                socket.close();
                            }
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        } finally {
                            mainGUI.svSetCliente("");
                            mainGUI.svSetStatus("");
                            mainGUI.svSetProgress(0);
                            mainGUI.svSetProgressPainted(false);
                        }
                    }
                }
            }
        }
    }

    private class EnviarArquivos implements Runnable {

        private List<File> arquivos = null;

        public EnviarArquivos(List<File> arquivos) {
            this.arquivos = arquivos;
        }

        @Override
        public void run() {
            for (File arquivo : arquivos) {
                int loop = 0;
                while (!isSocketConnected() && loop < AGUARDAR_APOS_ENVIO_LOOP) {
                    loop++;
                    esperar(AGUARDAR_APOS_ENVIO);
                }
                if (loop == AGUARDAR_APOS_ENVIO_LOOP) {
                    Utils.msgBoxErro(mainGUI, "Não foi possível enviar os arquivos restantes.");
                    break;
                }
                if (arquivo.exists()) {
                    DataOutputStream dos = null;
                    FileInputStream fis = null;
                    OutputStream out = null;
                    try {
                        dos = new DataOutputStream(socket.getOutputStream());
                        dos.writeUTF(arquivo.getName() + "|" + arquivo.length());
                        esperar(1000);
                        fis = new FileInputStream(arquivo);
                        out = socket.getOutputStream();
                        mainGUI.svSetStatus("Enviando Arquivo");
                        mainGUI.svAddAreaTextLog("Enviando Arquivo: \"" + arquivo.getName() + "\".");
                        mainGUI.svSetProgressPainted(true);
                        byte[] buffer = new byte[1024];
                        long bytesEnviados = 0;
                        int count;
                        long tamanhokb = (arquivo.length() / 1024);
                        tamanhokb = (tamanhokb == 0 ? 1 : tamanhokb);
                        long kb = 0;
                        int progresso;
                        while ((count = fis.read(buffer)) > 0) {
                            out.write(buffer, 0, count);
                            bytesEnviados += count;
                            progresso = (int)(++kb * 100 / tamanhokb);
                            mainGUI.svSetProgress(progresso < 100 ? progresso : 100);
                        }
                        if (bytesEnviados == arquivo.length()) {
                            mainGUI.svAddAreaTextLog("Arquivo \"" + arquivo.getName() + "\" enviado com sucesso.");
                        } else {
                            mainGUI.svAddAreaTextLog("Arquivo \"" + arquivo.getName() + "\" corrompido durante o envio.");
                        }
                    } catch (IOException ex) {
                        if (ligado) {
                            Utils.msgBoxErro(mainGUI, "Conexão com o cliente perdida.");
                            ex.printStackTrace();
                        }
                    } finally {
                        try {
                            if (out != null) {
                                out.close();
                            }
                            if (fis != null) {
                                fis.close();
                            }
                            if (dos != null) {
                                dos.close();
                            }
                            if (socket != null) {
                                socket.close();
                            }
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        } finally {
                            mainGUI.svSetCliente("");
                            mainGUI.svSetStatus("");
                            mainGUI.svSetProgress(0);
                            mainGUI.svSetProgressPainted(false);
                        }
                    }
                } else {
                    mainGUI.svAddAreaTextLog("Arquivo: \"" + arquivo.getName() + "\" não foi encontrado.");
                }
                esperar(AGUARDAR_APOS_ENVIO);
            }
        }
    }
}
