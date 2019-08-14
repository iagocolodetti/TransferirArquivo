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
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.List;
import java.util.regex.Pattern;

/**
 *
 * @author iagocolodetti
 */
public class Cliente {

    private final int MIN_PORTA = 0;
    private final int MAX_PORTA = 65535;
    
    private MainGUI mainGUI = null;

    private Socket socket = null;

    private volatile boolean conectado = false;

    private boolean recebendoArquivo = false;
    private Thread enviandoArquivo = null;

    private final int CONNECT_TIMEOUT = 5000;
    private final int TENTATIVAS_RECONEXAO = 5;
    private final int AGUARDAR_APOS_ENVIO = 2000;
    private final int AGUARDAR_APOS_ENVIO_LOOP = 10;

    public Cliente(MainGUI mainGUI) {
        this.mainGUI = mainGUI;
    }

    public void conectar(String ip, int porta, String diretorio) throws ClienteConectarException {
        if (ip.isEmpty()) {
            throw new ClienteConectarException("Entre com o IP do servidor.");
        }
        if (!Pattern.compile("^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$").matcher(ip).matches()) {
            throw new ClienteConectarException("Digite um IP válido.");
        }
        if (porta < MIN_PORTA || porta > MAX_PORTA) {
            throw new ClienteConectarException("A porta deve ser no mínimo " + MIN_PORTA + " e no máximo " + MAX_PORTA + ".");
        }
        if (diretorio.isEmpty()) {
            throw new ClienteConectarException("Selecione um diretório para recebimento de arquivos.", 1);
        }
        mainGUI.clConectando();
        Thread t = new Thread(new ClienteConectar(ip, porta, diretorio));
        t.start();
    }

    public void desconectar() {
        try {
            conectado = false;
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            socket = null;
            mainGUI.clDesconectado();
        }
    }

    public void enviarArquivos(List<File> arquivos) throws EnviarArquivoException {
        if (!isSocketConnected()) {
            throw new EnviarArquivoException("É necessário estar conectado a um servidor para enviar arquivos.");
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
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private class ClienteConectar implements Runnable {

        private String ip = "";
        private int porta = 0;
        private String diretorio = "";

        public ClienteConectar(String ip, int porta, String diretorio) {
            conectado = true;
            this.ip = ip;
            this.porta = porta;
            this.diretorio = diretorio;
        }

        @Override
        public void run() {
            while (conectado) {
                recebendoArquivo = false;
                if (!isSocketConnected()) {
                    int tentativas = 0;
                    while (conectado && tentativas < TENTATIVAS_RECONEXAO) {
                        try {
                            socket = null;
                            socket = new Socket(ip, porta);
                            mainGUI.clConectado();
                            break;
                        } catch (IllegalArgumentException e) {
                            Util.msgBoxErro(mainGUI, "IP e/ou porta incorreto(s).");
                            desconectar();
                            e.printStackTrace();
                            break;
                        } catch (IOException e) {
                            tentativas++;
                            e.printStackTrace();
                            esperar(CONNECT_TIMEOUT);
                        }
                    }
                    if (tentativas == TENTATIVAS_RECONEXAO) {
                        Util.msgBoxErro(mainGUI, "Não foi possível conectar-se a esse servidor.");
                        desconectar();
                    }
                }
                if (conectado) {
                    InputStream in = null;
                    DataInputStream dis = null;
                    OutputStream out = null;
                    try {
                        in = socket.getInputStream();
                        dis = new DataInputStream(in);
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
                        in = socket.getInputStream();
                        out = new FileOutputStream(diretorio + novoNomeArquivo);
                        mainGUI.clSetStatus("Recebendo Arquivo");
                        mainGUI.clAddAreaTextLog("Recebendo Arquivo: \"" + novoNomeArquivo + "\".");
                        mainGUI.clSetProgressPainted(true);
                        byte[] buffer = new byte[1024];
                        long bytesRecebidos = 0;
                        int count;
                        long kb = 0;
                        int progresso;
                        while ((count = in.read(buffer)) > 0) {
                            out.write(buffer, 0, count);
                            bytesRecebidos += count;
                            progresso = (int)(++kb * 100 / tamanhokb);
                            mainGUI.clSetProgress(progresso < 100 ? progresso : 100);
                        }
                        if (bytesRecebidos == tamanho) {
                            mainGUI.clAddAreaTextLog("Arquivo \"" + novoNomeArquivo + "\" recebido com sucesso.");
                        } else {
                            mainGUI.clAddAreaTextLog("Arquivo \"" + novoNomeArquivo + "\" corrompido durante o recebimento.");
                            Util.msgBoxErro(mainGUI, "Conexão com o servidor perdida.");
                            desconectar();
                        }
                    } catch (IOException e) {
                        if (conectado && !enviandoArquivos()) {
                            Util.msgBoxErro(mainGUI, "Conexão com o servidor perdida.");
                            desconectar();
                            e.printStackTrace();
                        }
                    } finally {
                        try {
                            if (out != null) {
                                out.close();
                            }
                            if (in != null) {
                                in.close();
                            }
                            if (dis != null) {
                                dis.close();
                            }
                            if (socket != null) {
                                socket.close();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        } finally {
                            mainGUI.clSetStatus("");
                            mainGUI.clSetProgress(0);
                            mainGUI.clSetProgressPainted(false);
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
                    Util.msgBoxErro(mainGUI, "Não foi possível enviar os arquivos restantes.");
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
                        mainGUI.clSetStatus("Enviando Arquivo");
                        mainGUI.clAddAreaTextLog("Enviando Arquivo: \"" + arquivo.getName() + "\".");
                        mainGUI.clSetProgressPainted(true);
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
                            mainGUI.clSetProgress(progresso < 100 ? progresso : 100);
                        }
                        if (bytesEnviados == arquivo.length()) {
                            mainGUI.clAddAreaTextLog("Arquivo \"" + arquivo.getName() + "\" enviado com sucesso.");
                        } else {
                            mainGUI.clAddAreaTextLog("Arquivo \"" + arquivo.getName() + "\" corrompido durante o envio.");
                        }
                    } catch (IOException e) {
                        if (conectado) {
                            Util.msgBoxErro(mainGUI, "Conexão com o servidor perdida.");
                            desconectar();
                            e.printStackTrace();
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
                        } catch (IOException e) {
                            e.printStackTrace();
                        } finally {
                            mainGUI.clSetStatus("");
                            mainGUI.clSetProgress(0);
                            mainGUI.clSetProgressPainted(false);
                        }
                    }
                } else {
                    mainGUI.clAddAreaTextLog("Arquivo: \"" + arquivo.getName() + "\" não foi encontrado.");
                }
                esperar(AGUARDAR_APOS_ENVIO);
            }
        }
    }
}
