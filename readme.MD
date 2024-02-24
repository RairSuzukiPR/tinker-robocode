# Tinker-robocode

### PASSOS PARA RODAR:
	usar jars dependentes em /home/username/robocode/libs
	javac -cp /home/rairs/robocode/libs/robocode.jar:/home/rairs/robocode/libs/deeplearning4j.jar:/home/rairs/robocode/libs/apachehttp.jar -d .. *.java
	compilar: javac -cp /home/username/robocode/libs/robocode.jar:/home/username/robocode/libs/deeplearning4j.jar -d .. *.java
	add -DNOSECURITY=true em robocode.sh (permissao para usar libs externas de ML)