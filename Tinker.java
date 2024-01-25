package tinker;

import robocode.*;

import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.api.ndarray.INDArray;
import java.awt.*;
import java.io.IOException;
import java.util.Random;
import java.util.List;

public class Tinker extends AdvancedRobot {
    // Neural config
    static int numInputs = 7;
    static int numHidden = 64;
    static int numOutput = 5;
    static double learningRate = 0.001;
    private double epsilon = 0.9; // randomness
    private double gamma = 0.9;  // discount rate
    private final int MAX_MEMORY = 100_000;
    private final int BATCH_SIZE = 1000;
    private LimitedDeque memory = new LimitedDeque(MAX_MEMORY);
    private LinearQNet model = new LinearQNet(numInputs, numHidden, numOutput);
    private QTrainer trainer = new QTrainer(model, learningRate, gamma);
    
    // Robot mode
    public enum enumOptionalMode {scan, performanceAction};
    private enumOptionalMode myOperationMode = enumOptionalMode.scan;

    // Robot states
    private State currentState = new State(100,100,120,500,0, 500, 0);
    private Action currentAction = Action.values()[0];
    private State previousState = new State(100,100,90,200,0, 800, 0);
    private Action previousAction = Action.values()[0];
    private boolean isBattleDone = false;
    
    // Statistics-data
    private int targetNumRounds = 1100;
    public static int totalNumRounds = 0;
    public static int numRoundsTo100 = 0;
    public static int numWins = 0;
    static private double curr_best_win_rate = 0.0;

    // Initialize states
    double myX = 0.0;
    double myY = 0.0;
    double myEnergy = 0.0;
    double enemyBearing = 0.0;
    double enemyDistance = 0.0;
    double distanceToCenter = 0.0;
    double enemyEnergy = 0.0;
    double enemyVelocity = 0.0;
    double gunHeat = 0.0;

    // Center of board
    int xMid = 0;
    int yMid = 0;

    // Rewards
    double totalReward = 0.0;
    private double currentReward = 0.0;
    private final double goodReward = 1.0;
    private final double badReward = -0.25;
    private final double goodTerminalReward = 2.0;
    private final double badTerminalReward = -0.5;

    // Logging
    // static String logFilename = "tinker.log";
    // static LogFile log = null;
    // private String weightsFilename =  getClass().getSimpleName() + "-weights.txt";
        
    public void run() {
        setColors(Color.white, Color.red, Color.lightGray, Color.red, Color.white);

        int xMid = (int) getBattleFieldWidth() / 2;
        int yMid = (int) getBattleFieldHeight() / 2;
        isBattleDone = false;

        // if (log == null) {
        //     log = new LogFile(getDataFile(logFilename));
        //     log.stream.printf("# Neural config\n");
        //     log.stream.printf("epsilon, %2.2f\n", epsilon);
        //     log.stream.printf("badInstantReward, %2.2f\n", badReward);
        //     log.stream.printf("badTerminalReward, %2.2f\n", badTerminalReward);
        //     log.stream.printf("goodInstantReward, %2.2f\n", goodReward);
        //     log.stream.printf("goodTerminalReward, %2.2f\n", goodTerminalReward);
        //     log.stream.printf("-------------------------\n");
        // }

        while (true) {
            if (totalNumRounds > targetNumRounds) {epsilon = 0.0;}
            if (Math.abs(getVelocity()) == 0.0) {
                // train code
                switch (myOperationMode) {
                    case scan: {
                        currentReward = 0.0;
                        turnRadarLeft(90);
                        break;
                    }
                    case performanceAction: {
                        Action currentAction = getAction();

                        switch (currentAction) {
                            case FORWARD: {
                                setAhead(100);
                                execute();
                                break;
                            }
    
                            case BACK: {
                                setBack(100);
                                execute();
                                break;
                            }
    
                            case LEFT: {
                                setTurnLeft(90);
                                setAhead(100);
                                execute();
                                break;
                            }
    
                            case RIGHT: {
                                setTurnRight(90);
                                setAhead(100);
                                execute();
                                break;
                            }

                            // possib improv: ter um aproximar, distanciar no lugar dos acima?
    
                            case FIRE: {
                                double amountToTurn = getHeading() - getGunHeading() + enemyBearing;
                                if(amountToTurn == 360.0 || amountToTurn == -360.0){amountToTurn=0.0;}
                                turnGunRight(amountToTurn);
                                fire(3);
                                break;
                            }
                        }

                        Experience experience = new Experience(
                            previousState.toINDArray(), 
                            Action.bipolarOneHotVectorOf(currentAction), 
                            currentReward, 
                            currentState.toINDArray(), 
                            isBattleDone
                        );

                        trainShortMemory(experience);

                        remember(experience);

                        // System.out.println("->previousState = " + previousState.toINDArray());
                        // System.out.println("->" + Action.bipolarOneHotVectorOf(experience.action));
                        // System.out.println("->currentReward = " + currentReward);
                        // System.out.println("->totalReward = " + totalReward);
                        myOperationMode = enumOptionalMode.scan;
                    }
                }
            }            
            execute();
        }
    }

    // Agent functions
    public void remember(Experience experience) {
        memory.add(experience);
    }

    public void trainLongMemory() {
        LimitedDeque miniSample;
    
        if (memory.size() > BATCH_SIZE) {
            List<Experience> experienceList = memory.getRandomSample(BATCH_SIZE);
            miniSample = new LimitedDeque(experienceList.size());
            miniSample.addAll(experienceList);
        } else {
            miniSample = new LimitedDeque(memory.size());
            miniSample.addAll(memory.getAllExperiences());
        }
    
        for (Experience experience : miniSample) {
            trainer.trainStep(
                experience.previousState, 
                experience.action,
                experience.reward,
                experience.nextState,
                experience.done
            );
        }
    }

    public void trainShortMemory(Experience experience) {
        trainer.trainStep(
                experience.previousState, 
                experience.action,
                experience.reward,
                experience.nextState,
                experience.done
            );
    }

    public Action getAction() {
        if (Math.random() <= epsilon){
            currentAction = selectRandomAction();
        } else {
            currentAction = selectBestAction(
                    myEnergy,
                    enemyEnergy,
                    enemyBearing,
                    enemyDistance,
                    enemyVelocity,
                    distanceToCenter,
                    gunHeat
            );
            System.err.println("best action" + currentAction);
        }

        return currentAction;
    }

    // general functions
    public Action selectRandomAction() {
        Random rand = new Random();
        int r = rand.nextInt(Action.values().length);
        return Action.values()[r];
    }

    public Action selectBestAction(
        double myEnergy, 
        double enemyEnergy, 
        double enemyBearing, 
        double enemyDistance, 
        double enemyVelocity,
        double distanceToCenter,
        double gunHeat
    ) {
        State state = new State(myEnergy, enemyEnergy, enemyBearing, enemyDistance, enemyVelocity, distanceToCenter, gunHeat);

        INDArray qValues = model.predict(state.toINDArray());

        int bestActionIndex = Nd4j.argMax(qValues, 1).getInt(0);
        Action bestAction = Action.values()[bestActionIndex];

        return bestAction;
    }

    public void onEndBattle(double reward) {
        isBattleDone= true;
        currentReward = reward;
        totalReward += currentReward;

        trainLongMemory();
        
        if (numRoundsTo100 < 100) {
            numRoundsTo100++;
            totalNumRounds++;
            numWins++;
        } else {
            try {
                logger_print();
                pocketWeights(numWins * 1.0/numRoundsTo100);
                numRoundsTo100 = 0;
                numWins = 0;
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }

        totalReward = 0;
        execute();
    }

    public double dtc(double fromX, double fromY, double toX, double toY) {
        double distance = Math.sqrt(Math.pow((fromX - toX), 2) + Math.pow((fromY - toY), 2));
        return distance;
    }

    private void pocketWeights(double winRate) throws IOException {
        if(winRate >= curr_best_win_rate*1.05 & winRate > 0.5){
            // q.save(getDataFile(weightsFilename));
            curr_best_win_rate = winRate;
        }
    }

    // logger functions
    private void logger_print() throws IOException {
        // log.stream.printf("%d - %d, %2.1f\n", totalNumRounds - 100, totalNumRounds, 100.0 * numWins / numRoundsTo100);
        // log.stream.flush();
    }

    // override robot functions
    @Override
    public void onScannedRobot(ScannedRobotEvent event) {
        myX = getX();
        myY = getY();
        myEnergy = getEnergy();
        enemyEnergy = event.getEnergy();
        enemyBearing = event.getBearing();
        enemyDistance = event.getDistance();
        enemyVelocity = event.getVelocity();
        distanceToCenter = dtc(myX, myY, xMid, yMid);
        gunHeat = getGunHeat();

        previousState.myEnergy = currentState.myEnergy;
        previousState.enemyEnergy = currentState.enemyEnergy;
        previousState.enemyBearing = currentState.enemyBearing;
        previousState.enemyDistance = currentState.enemyDistance;
        previousState.enemyVelocity = currentState.enemyVelocity;
        previousState.distanceToCenter = currentState.distanceToCenter;
        previousState.gunHeat = currentState.gunHeat;
        previousAction = currentAction;

        currentState.myEnergy = getEnergy();
        currentState.enemyEnergy = event.getEnergy();
        currentState.enemyBearing = event.getBearing();
        currentState.enemyDistance = event.getDistance();
        currentState.enemyVelocity = event.getVelocity();
        currentState.distanceToCenter = dtc(getX(), getY(), xMid, yMid);
        currentState.gunHeat = getGunHeat();
    
        myOperationMode = enumOptionalMode.performanceAction;
    }

    @Override
    public void onHitByBullet(HitByBulletEvent e) {
        currentReward = badReward;
        totalReward += currentReward;
    }

    @Override
    public void onBulletHit(BulletHitEvent e) {
        currentReward = goodReward;
        totalReward += currentReward;
    }

    @Override
    public void onHitWall(HitWallEvent event) {
        currentReward = badReward;
        totalReward += currentReward;
    }

    @Override
    public void onBulletMissed(BulletMissedEvent event) {
        currentReward = badReward;
        totalReward += currentReward;
    }

    @Override
    public void onDeath(DeathEvent e) {
        onEndBattle(badTerminalReward);
    }

    @Override
    public void onWin(WinEvent e) {
        onEndBattle(goodTerminalReward);
    }
}
