package tinker;

import java.util.Random;

// utiliza aprendizado supervisionado, alterar template para reforco*?
public class NeuralNetwork {
    public Layer[] layers;

    public NeuralNetwork(int... layerSizes) {
        layers = new Layer[layerSizes.length - 1];
        for (int i = 0; i < layers.length; i++) {
            layers[i] = new Layer(layerSizes[i], layerSizes[i + 1], new Random());
        }
    }

    // calcula as saidas finais da rede 
    public double[] calculateOutputs(double[] inputs) {
        for (Layer layer : layers) {
            inputs = layer.calculateOutputs(inputs);
        }

        return inputs;
    }

    // retorna o indice do valor maximo entre as saidas da rede
    public int classify(double[] inputs) {
        double[] outputs = calculateOutputs(inputs);
        return indexOfMaxValue(outputs);
    }

    private int indexOfMaxValue(double[] array) {
        int maxIndex = 0;
        double maxValue = array[0];

        for (int i = 1; i < array.length; i++) {
            if (array[i] > maxValue) {
                maxIndex = i;
                maxValue = array[i];
            }
        }

        return maxIndex;
    }

    // funcao de perda para um input
    public double cost(State input) {
        double[] outputs = calculateOutputs(input.toArray());
        Layer outputLayer = layers[layers.length - 1];
        double cost = 0;

        for (int nodeOut = 0 ; nodeOut < outputs.length ; nodeOut++) {
            cost += outputLayer.nodeCost(outputs[nodeOut], expectedOutputs);
        }

        return cost;
    }

    // funcao de perda para varios inputs q retorna a media, precisamos buscar a menor media de custo
    public double cost(State[] inputs) {
        double totalCost = 0;

        for (State input : inputs) {
            totalCost += cost(input);
        }

        return totalCost / inputs.length;
    }

    // ---
    public double inputValue;

    public void Init (double randomValue) {
        inputValue = randomValue;
    }

    // uma iteracao do algoritmo gradient descent, learnRate = intervalo de aprendizado de busca de valor ideal (trainingData deve ser input ou output?)
    public void Learn(double[] trainingData, double learnRate) {
        for (double data : trainingData) {
            updateAllGradients(data);
        }

        // aplica o gradiente em tds as layers, dessa forma se o learnRate nao for mt grande, deve diminuir o custo a cada iteracao
        for (int i = 0; i < layers.length; i++) {
            layers[i].applyGradients(learnRate / trainingData.length);
        }

        // clearAllGradients()
    }

    // 2° itr
    // // uma iteracao do algoritmo gradient descent, learnRate = intervalo de aprendizado de busca de valor ideal (trainingData deve ser input ou output?)
    // public void Learn(State[] trainingData, double learnRate) {
    //     final double h = 0.0001;
    //     double originalCost = cost(trainingData);

    //     // calcula o custo do gradiente para os pesos atuais
    //     for (Layer layer : layers) {
    //         for (int nodeIn = 0; nodeIn < layer.numNodesIn ; nodeIn++) {
    //             for (int nodeOut = 0; nodeOut < layer.numNodesOut ; nodeOut++) {
    //                 // faz um pequeno ajuste no peso e verifica o custo, verificando quao sensivel eh
    //                 layer.weights[nodeIn][nodeOut] += h;
    //                 double deltaCost = cost(trainingData) - originalCost;
    //                 layer.weights[nodeIn][nodeOut] -= h;
    //                 layer.costGradientW[nodeIn][nodeOut] = deltaCost / h;
    //             }
    //         }

    //         // calcula o custo do gradiente para os bias atuais
    //         for (int biasIndex = 0 ; biasIndex < layer.biases.length ; biasIndex++) {
    //             layer.biases[biasIndex] += h;
    //             double deltaCost = cost(trainingData) - originalCost;
    //             layer.biases[biasIndex] -= h;
    //             layer.costGradientB[biasIndex] = deltaCost / h;
    //         }
    //     }
        
    //     // aplica o gradiente em tds as layers, dessa forma se o learnRate nao for mt grande, deve diminuir o custo a cada iteracao
    //     for (int i = 0; i < layers.length; i++) {
	// 		layers[i].applyGradients(learnRate / trainingData.length);
	// 	}
    // }

    // 1° itr
    // public void Learn(double learnRate) {
    //     final double h = 0.00001;
    //     double deltaOutput = Function(inputValue + h) - Function(inputValue);
    //     double slope = deltaOutput / h;

    //     inputValue -= slope * learnRate;
    // }

    // funcao exemplo
    public double Function(double x) {
        return 0.2 * Math.pow(x, 4) + 0.1 * Math.pow(x, 3) - Math.pow(x, 2) + 2;
    }

    // att os grads e faz o backpropagation. (ter um state que tenha inputs e expetedOutputs? como ter a porra do expected?)
    public void updateAllGradients(double[] inputs) {
        calculateOutputs(inputs); // validar*

        // att os grad da output layer
        Layer outputLayer = layers[layers.length - 1];
        double[] nodeValues = outputLayer.calculateOutputs(expectedOutputs);
        outputLayer.updateGradients(nodeValues);

        // att os grad da hidden layer
        for (int hiddenLayerIndex = layers.length - 2 ; hiddenLayerIndex >= 0 ; hiddenLayerIndex-- ) { // entender indices
            Layer hiddenLayer = layers[hiddenLayerIndex]; 
            nodeValues = hiddenLayer.calculateHiddenLayerNodeValues(layers[hiddenLayerIndex + 1], nodeValues);
            hiddenLayer.updateGradients(nodeValues);
        }
    }
}

