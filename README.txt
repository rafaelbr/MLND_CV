O projeto consiste em construir uma aplicação Android capaz de permitir que usuários 
gravassem imagens e descrições para essas imagens. O sistema seria capaz de descrever 
automaticamente as imagens de tempos em tempos.

O sistema consiste em uma aplicação Android, no diretório Android, e uma aplicação 
Python, no diretório Python, além de diversos notebooks iPython.

Para executar o treinamento do modelo e alguns notebooks, é necessário baixar o dataset
Flickr8k, disponível em https://illinois.edu/fb/sec/1713398. O acesso requer cadastro. Basta
realizar o cadastro que o link é enviado por e-mail. Favor colocar os diretórios 
Flicker8k_dataset e Flickr8k_text no caminho Python/Flickr8k.

O arquivo train_model.py é uma forma mais controlada de treinar o modelo, pois evita 
a dependência do Jupyter aberto, e também pode ser executado em segundo plano.