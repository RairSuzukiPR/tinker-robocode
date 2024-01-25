package tinker;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

public class Experience {
    public INDArray previousState;
    public INDArray action;
    public double reward;
    public INDArray nextState;
    public boolean done;

    public Experience(INDArray currentState, INDArray action, double reward, INDArray nextState, boolean done) {
        this.previousState = currentState;
        this.action = action;
        this.reward = reward;
        this.nextState = nextState;
        this.done = done;
    }

    @Override
    public String toString() {
        return String.format("Experience{previousState=%s, action=%s, reward=%.2f, nextState=%s, done=%b}",
                previousState, action, reward, nextState, done);
    }
}
