# Tinker-robocode

## Passos para Executar Robocode

1. Baixe os JARs dependentes em `/home/username/robocode/libs` nos seguintes links:
   - [Apache](https://drive.google.com/file/d/1Pw2QtTsiF_uL5ypCgO_cSPI3bK8OyNCf/view?usp=drive_link)
   - [DeepLearning4j](https://drive.google.com/file/d/1E6In77HlSslJ1rC5_7duJ2PwFHG0LAAH/view?usp=drive_link)

2. Adicione o parâmetro `-DNOSECURITY=true` no arquivo `robocode.sh` para permitir o uso de bibliotecas externas de aprendizado de máquina (ML) e conexão com a API.

3. Compile os arquivos Java usando o seguinte comando:
4. Para compilar os arquivos Java, utilize o seguinte comando:
    ### Comando para compilar 
    ```
    javac -cp /home/rairs/robocode/libs/robocode.jar:/home/rairs/robocode/libs/deeplearning4j.jar:/home/rairs/robocode/libs/apachehttp.jar -d .. *.java
    ```
