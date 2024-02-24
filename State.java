package tinker;

public class State {
    public double enemyBearing;
    public double enemyDistance;
    public double enemyVelocity;
    public double enemyEnergy;

    public State(double enemyEnergy, double enemyBearing, double enemyDistance, double enemyVelocity){
        this.enemyEnergy = enemyEnergy;
        this.enemyBearing = enemyBearing;
        this.enemyDistance = enemyDistance;
        this.enemyVelocity = enemyVelocity;
    }

    public double[] toArray() {
        return new double[]{this.enemyEnergy, this.enemyBearing, this.enemyDistance, this.enemyVelocity};
    }

    @Override
    public String toString() {
        return String.format("State{enemyEnergy=%.2f, enemyBearing=%.2f, " +
                "enemyDistance=%.2f, enemyVelocity=%.2f}",
                enemyEnergy, enemyBearing, enemyDistance, enemyVelocity);
    }
}
