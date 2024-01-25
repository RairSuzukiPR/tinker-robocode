package tinker;

import org.deeplearning4j.nn.conf.*;
import org.deeplearning4j.nn.conf.layers.Layer;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import java.io.File;
import java.io.IOException;


public class LinearQNet {
    private MultiLayerNetwork model;

    public LinearQNet(int inputSize, int hiddenSize, int outputSize) {        
        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
            .seed(123)
            .updater(new Adam(0.01))
            .l2(1e-4)
            .list()
            .layer(new DenseLayer.Builder().nIn(inputSize).nOut(hiddenSize)
                .activation(Activation.RELU).build())
            .layer(new OutputLayer.Builder(LossFunctions.LossFunction.MSE)
                .nIn(hiddenSize).nOut(outputSize)
                .activation(Activation.IDENTITY).build())
            .build();      

        this.model = new MultiLayerNetwork(conf);
        this.model.init();
        this.model.setListeners(new ScoreIterationListener(100));
    }

    public INDArray predict(INDArray input) {
        return model.output(input);
    }

    public MultiLayerNetwork getModel() {
        return this.model;
    }

    public void fit(INDArray input, INDArray target) {
        model.fit(input, target);
    }

    public void save(String filePath) throws IOException {
        model.save(new File(filePath), true);
    }
}
