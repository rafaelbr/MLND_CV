O projeto consiste em construir uma aplica��o Android capaz de permitir que usu�rios 
gravassem imagens e descri��es para essas imagens. O sistema seria capaz de descrever 
automaticamente as imagens de tempos em tempos.

O sistema consiste em uma aplica��o Android, no diret�rio Android, e uma aplica��o 
Python, no diret�rio Python, al�m de diversos notebooks iPython.

Para executar o treinamento do modelo e alguns notebooks, � necess�rio baixar o dataset
Flickr8k, dispon�vel em https://illinois.edu/fb/sec/1713398. O acesso requer cadastro. Basta
realizar o cadastro que o link � enviado por e-mail. Favor colocar os diret�rios 
Flicker8k_dataset e Flickr8k_text no caminho Python/Flickr8k.

O arquivo train_model.py � uma forma mais controlada de treinar o modelo, pois evita 
a depend�ncia do Jupyter aberto, e tamb�m pode ser executado em segundo plano.