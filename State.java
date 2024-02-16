package tinker;

public class State {
    public double myEnergy;
    public double enemyEnergy;
    public double enemyBearing;
    public double enemyDistance;
    public double enemyVelocity;
    public double distanceToCenter;
    public double gunHeat;

    private static final double MIN_MY_ENERGY = 0.0;
    private static final double MAX_MY_ENERGY = 150.0;
    private static final double MIN_ENEMY_ENERGY = 0.0;
    private static final double MAX_ENEMY_ENERGY = 150.0;
    private static final double MIN_ENEMY_BEARING = -180.0;
    private static final double MAX_ENEMY_BEARING = 180.0;
    private static final double MIN_ENEMY_DISTANCE = 0.0;
    private static final double MAX_ENEMY_DISTANCE = 1000.0;
    private static final double MIN_ENEMY_VELOCITY = -8.0;
    private static final double MAX_ENEMY_VELOCITY = 8.0;
    private static final double MIN_DISTANCE_TO_CENTER = 0.0;
    private static final double MAX_DISTANCE_TO_CENTER = 500.0;
    private static final double MIN_GUN_HEAT = 0.0;
    private static final double MAX_GUN_HEAT = 1.5;

    public State(double myE, double eE, double eB, double eD, double eV, double dtc, double gH){
        this.myEnergy = myE;
        this.enemyEnergy = eE;
        this.enemyBearing = eB;
        this.enemyDistance = eD;
        this.enemyVelocity = eV;
        this.distanceToCenter = dtc;
        this.gunHeat = gH;
    }

    public double[] toArray() {
        double normalizedMyEnergy = (myEnergy - MIN_MY_ENERGY) / (MAX_MY_ENERGY - MIN_MY_ENERGY);
        double normalizedEnemyEnergy = (enemyEnergy - MIN_ENEMY_ENERGY) / (MAX_ENEMY_ENERGY - MIN_ENEMY_ENERGY);
        double normalizedEnemyBearing = (enemyBearing - MIN_ENEMY_BEARING) / (MAX_ENEMY_BEARING - MIN_ENEMY_BEARING);
        double normalizedEnemyDistance = (enemyDistance - MIN_ENEMY_DISTANCE) / (MAX_ENEMY_DISTANCE - MIN_ENEMY_DISTANCE);
        double normalizedEnemyVelocity = (enemyVelocity - MIN_ENEMY_VELOCITY) / (MAX_ENEMY_VELOCITY - MIN_ENEMY_VELOCITY);
        double normalizedDistanceToCenter = (distanceToCenter - MIN_DISTANCE_TO_CENTER) / (MAX_DISTANCE_TO_CENTER - MIN_DISTANCE_TO_CENTER);
        double normalizedGunHeat = (gunHeat - MIN_GUN_HEAT) / (MAX_GUN_HEAT - MIN_GUN_HEAT);

        return new double[]{normalizedMyEnergy, normalizedEnemyEnergy, normalizedEnemyBearing, normalizedEnemyDistance, normalizedEnemyVelocity, normalizedDistanceToCenter, normalizedGunHeat};
    }

    // public double[] toArray() {
    //     double normalizedMyEnergy = myEnergy / MAX_MY_ENERGY ;
    //     double normalizedEnemyEnergy = enemyEnergy / MAX_ENEMY_ENERGY;
    //     double normalizedEnemyBearing = enemyBearing / MAX_ENEMY_BEARING;
    //     double normalizedEnemyDistance = enemyDistance / MAX_ENEMY_DISTANCE;
    //     double normalizedEnemyVelocity = enemyVelocity / MAX_ENEMY_VELOCITY;
    //     double normalizedDistanceToCenter = distanceToCenter / MAX_DISTANCE_TO_CENTER;
    //     double normalizedGunHeat = gunHeat / MAX_GUN_HEAT;

    //     return new double[]{normalizedMyEnergy, normalizedEnemyEnergy, normalizedEnemyBearing, normalizedEnemyDistance, normalizedEnemyVelocity, normalizedDistanceToCenter, normalizedGunHeat};
    // }

    @Override
    public String toString() {
        return String.format("State{myEnergy=%.2f, enemyEnergy=%.2f, enemyBearing=%.2f, " +
                "enemyDistance=%.2f, enemyVelocity=%.2f, distanceToCenter=%.2f, gunHeat=%.2f}",
                myEnergy, enemyEnergy, enemyBearing, enemyDistance, enemyVelocity, distanceToCenter, gunHeat);
    }
}