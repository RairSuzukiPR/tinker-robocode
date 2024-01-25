package tinker;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

public enum Action {

    FORWARD, BACK, LEFT, RIGHT, FIRE;

    static public INDArray bipolarOneHotVectorOf(Action action) {
        INDArray hotVector = Nd4j.create(new double[]{-1, -1, -1, -1, -1});
        hotVector.putScalar(action.ordinal(), 1);
        return hotVector;
    }

    @Override
    public String toString() {
        return name();
    }
}
