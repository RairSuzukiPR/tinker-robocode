package tinker;

import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.api.ndarray.INDArray;

public class QTrainer {
    private LinearQNet model;
    private double lr;
    private double gamma;

    public QTrainer(LinearQNet model, double lr, double gamma) {
        this.model = model;
        this.lr = lr;
        this.gamma = gamma;
    }

    public void trainStep(INDArray state, INDArray action, double reward, INDArray nextState, boolean done) {
        // Predict Q-values for the current state
        INDArray currentQValues = model.predict(state);
    
        // Predict Q-values for the next state
        INDArray nextQValues = model.predict(nextState);

        // Calculate the target Q-value using the Bellman equation
        double targetQValue = reward;
        if (!done) {
            targetQValue += gamma * nextQValues.maxNumber().doubleValue();
        }
    
        // Update the Q-value for the chosen action
        int actionIndex = Nd4j.argMax(action).getInt(0);
        currentQValues.putScalar(actionIndex, targetQValue);
    
        // Train the model with the updated Q-values
        model.fit(state, currentQValues);
    }
}
