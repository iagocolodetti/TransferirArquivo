[Downloads](https://github.com/iagocolodetti/TransferirArquivo/blob/master/README.md#downloads "Downloads")
<br>
[Descrição](https://github.com/iagocolodetti/TransferirArquivo/blob/master/README.md#descri%C3%A7%C3%A3o "Descrição")
<br>
[Funcionamento](https://github.com/iagocolodetti/TransferirArquivo/blob/master/README.md#funcionamento "Funcionamento")
<br>
[Requisitos](https://github.com/iagocolodetti/TransferirArquivo/blob/master/README.md#requisitos "Requisitos")
<br>
[Projeto](https://github.com/iagocolodetti/TransferirArquivo/blob/master/README.md#projeto "Projeto")
<br>
<br>
# Downloads
https://github.com/iagocolodetti/TransferirArquivo/releases
* [TransferirArquivo.jar](https://github.com/iagocolodetti/TransferirArquivo/releases/download/v1.0/TransferirArquivo.jar "TransferirArquivo.jar")
* [Código-fonte](https://github.com/iagocolodetti/TransferirArquivo/archive/v1.0.zip "v1.0.zip")
* [Cliente Android](https://github.com/iagocolodetti/TransferirArquivoAndroid/blob/master/README.md#downloads "TransferirArquivoAndroid#Downloads")
# Descrição
Projeto desenvolvido com o objetivo de fazer a conexão entre Servidor e Cliente para a transferência de arquivos de maneira bidirecional, ou seja, Servidor e Cliente pode tanto receber quanto enviar arquivos.
<br>
Servidor e Cliente estão na mesma aplicação, na mesma "view" em janelas/abas diferentes, como pode ser visto nas imagens demonstrativas abaixo.
<br>
<h3>Servidor</h3>
<br>
<img src="https://github.com/iagocolodetti/imagens/blob/master/transferirarquivoservidor1.png" alt="Servidor desligado">
<img src="https://github.com/iagocolodetti/imagens/blob/master/transferirarquivoservidor2.png" alt="Servidor ligado">
<img src="https://github.com/iagocolodetti/imagens/blob/master/transferirarquivoservidor3.png" alt="Servidor ligado e Cliente conectado">
<br>
<br>
<h3>Cliente</h3>
<br>
<img src="https://github.com/iagocolodetti/imagens/blob/master/transferirarquivocliente1.png" alt="Cliente desconectado">
<img src="https://github.com/iagocolodetti/imagens/blob/master/transferirarquivocliente2.png" alt="Cliente conectado">
<br>
<br>
<h3>Tela selecionar arquivos em lote</h3>
<br>
<img src="https://github.com/iagocolodetti/imagens/blob/master/transferirarquivolote.png" alt="Tela selecionar arquivos">
<br>
<br>

# Funcionamento
Ao ligar o Servidor fornecendo a porta desejada e o diretório para o recebimento de arquivos, o IP do Servidor é exibido no campo "IP".
<br>
Com o IP e porta do Servidor o Cliente será capaz de conectar-se ao Servidor usando o IP e porta do mesmo e claro, selecionando o diretório em que deseja receber arquivos.
<br>
Após a conexão entre Servidor e Cliente for estabelicida, ambos poderão transferir arquivos de maneira bidirecional, porém singular (um arquivo por vez e enquanto estiver recebendo um arquivo, não poderá enviar um arquivo).

# Requisitos
Uso/instalação do [Java Runtime Environment (JRE)](https://www.java.com/pt_BR/download "Java Runtime Environment (JRE)").
<br>
O Servidor (PC) e o Cliente (PC/Android) devem estar conectados à mesma rede.
<br>
A porta selecionada no Servidor deverá estar aberta/liberada localmente.
<br>
<br>
# Projeto
Desenvolvido no NetBeans IDE 8.2 usando a linguagem Java e usando as tecnologias Socket e Thread.
