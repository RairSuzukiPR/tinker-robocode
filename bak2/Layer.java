package tinker;

import java.util.Random;

public class Layer {
    public int numNodesIn, numNodesOut;
    public double[][] costGradientW;
    public double[] costGradientB;
    public double[][] weights;
    public double[] biases;

    public Layer(int numNodesIn, int numNodesOut, Random rng) {
        // pq aq n usa this?
        costGradientW = new double[numNodesIn][numNodesOut];
        weights = new double[numNodesIn][numNodesOut];
        costGradientB = new double[numNodesOut];
        biases = new double[numNodesOut];

        this.numNodesIn = numNodesIn;
        this.numNodesOut = numNodesOut;

        initializeRandomWeights(rng);
    }

    // calcula a saida da layer -> output = input * weight + bias...
    public double[] calculateOutputs(double[] inputs) {
        double[] activations = new double[numNodesOut];

        for (int nodeOut = 0; nodeOut < numNodesOut; nodeOut++) {
            double weightedInput = biases[nodeOut];
            for (int nodeIn = 0; nodeIn < numNodesIn; nodeIn++) {
                weightedInput += inputs[nodeIn] * weights[nodeIn][nodeOut];
            }
            activations[nodeOut] = activationFunction(weightedInput);
        }

        return activations;
    }

    // permite moldarmos a curva de classificação utilizando sigmoid
    public double activationFunction(double weightedInput) {
        return 1 / (1 + Math.exp(-weightedInput));
        // return (weightedInput > 0) ? 1 : 0;
    }

    // derivada da sigmoide
    public double activationFunctionDerivative(double weightedInput) {
        double activation = activationFunction(weightedInput);
        return activation * (1 - activation);
    }

    // (como saberei a saida esperada no robocode?)
    public double nodeCost(double outputActivation, double expectedOutput) {
        double error = outputActivation - expectedOutput;
        return error * error;
    }

    // derivada parcial do custo em relacao a ativacao da saida de um node (como saberei a saida esperada no robocode?)
    public double nodeCostDerivative(double outputActivation, double expectedOutput) {
        return 2 * (outputActivation - expectedOutput);
    }

    // att os pesos e bias de acordo com o custo do grad
    public void applyGradients(double learnRate) {
		for (int nodeOut = 0 ; nodeOut < numNodesOut ; nodeOut++) {
            biases[nodeOut] -= costGradientB[nodeOut] * learnRate;
            for (int nodeIn = 0 ; nodeIn < numNodesIn ; nodeIn++) {
                weights[nodeIn][nodeOut] -= costGradientW[nodeIn][nodeOut] * learnRate;
            }
        }
	}

    // ha meios melhores de inicializar, pesquisar*
    private void initializeRandomWeights(Random rng) {
        for (int i = 0; i < weights.length; i++) {
            for (int j = 0; j < weights[i].length; j++) {
                weights[i][j] = randomInNormalDistribution(rng, 0, 1) / Math.sqrt(numNodesIn);
            }
        }
    }

    private double randomInNormalDistribution(Random rng, double mean, double standardDeviation) {
        double x1 = 1 - rng.nextDouble();
        double x2 = 1 - rng.nextDouble();

        double y1 = Math.sqrt(-2.0 * Math.log(x1)) * Math.cos(2.0 * Math.PI * x2);
        return y1 * standardDeviation + mean;
    }

    // calcula os valores dos nodes pra output layer, onde cada item do array tem a derivada parcial do custo em relacao ao input com peso calculado
	public double[] calculateOutputLayerNodeValues(double[] expectedOutputs) {
        double [] nodeValues = new double[expectedOutputs.length];
        
		for (int i = 0; i < nodeValues.length; i++) {
			// Evaluate partial derivatives for current node: cost/activation & activation/weightedInput
			double costDerivative = nodeCostDerivative(activations[i], expectedOutputs[i]);
			double activationDerivative = activationFunctionDerivative(weightedInputs[i]);
			nodeValues[i] = costDerivative * activationDerivative;
		}
	}

    // att o grad do custo em relacao aos pesos
    public void updateGradients(double[] nodeValues) {
        for (int nodeOut = 0 ; nodeOut < numNodesOut ; nodeOut++) {
            for (int nodeIn = 0 ; nodeIn < numNodesIn ; nodeIn++) {
                // avalia a derivada parcial do custo em relacao ao peso da conexao abaixo e salva em um array para cada peso p/ dps calcular a media do gradiente 
                double derivativeCostWrtWeight = inputs[nodeIn] * nodeValues[nodeOut];
                costGradientW[nodeIn][nodeOut] += derivativeCostWrtWeight;
            }

            // avalia a derivada parcial do custo em relacao ao bias do no atual
            double derivativeCostWrtBias = 1 * nodeValues[nodeOut];
            costGradientB[nodeOut] += derivativeCostWrtBias;
        }        
    }

    // calcula os valroes dos nodes pra uma hidden layer, onde cada item do array tem a derivada parcial do custo em relacao ao input com peso calculado
	public double[] calculateHiddenLayerNodeValues(Layer oldLayer, double[] oldNodeValues) {
        double[] newNodeValues = new double[numNodesOut];

		for (int newNodeIndex = 0 ; newNodeIndex < newNodeValues.length; newNodeIndex++) {
			double newNodeValue = 0;
			for (int oldNodeIndex = 0 ; oldNodeIndex < oldNodeValues.length; oldNodeIndex++) {
                // derivada parcial do input com peso calculado em relacao ao input
				double weightedInputDerivative = oldLayer.weights[newNodeIndex][oldNodeIndex];
				newNodeValue += weightedInputDerivative * oldNodeValues[oldNodeIndex];
			}
			newNodeValue *= activationFunctionDerivative(weightedInputs[newNodeIndex]);
			newNodeValues[newNodeIndex] = newNodeValue;
		}

        return newNodeValues;
	}
}

