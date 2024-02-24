package tinker;

public enum Action {

    FORWARD, FIRE, STILL;

    static public double[] bipolarOneHotVectorOf(Action action){
        double [] hotVector = new double[]{-1,-1,-1};
        hotVector[action.ordinal()]=+1;
        return hotVector;
    }

    @Override
    public String toString() {
        return name();
    }
}
