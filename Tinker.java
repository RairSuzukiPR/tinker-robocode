package tinker;

import robocode.*;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Random;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import com.google.gson.Gson;

public class Tinker extends AdvancedRobot {
    // Neural config
    static int numInputs = 7;
    static int numHidden = 256;
    static int numOutput = 5;
    static double learningRate = 0.001;
    private double epsilon = 0.9; // randomness
    private double gamma = 0.9;  // discount rate
    // private final int MAX_MEMORY = 100_000;
    // private final int BATCH_SIZE = 1000;
    // private LimitedDeque memory = new LimitedDeque(MAX_MEMORY);
    // private LinearQNet model = new LinearQNet(numInputs, numHidden, numOutput);
    // private QTrainer trainer = new QTrainer(model, learningRate, gamma);
    
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

    // Utils
    Gson gson = new Gson();
        
    public void run() {
        setColors(Color.white, Color.red, Color.lightGray, Color.red, Color.white);

        int xMid = (int) getBattleFieldWidth() / 2;
        int yMid = (int) getBattleFieldHeight() / 2;
        isBattleDone = false;

        if (totalNumRounds == 0) {
            try {
                initModel();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        while (true) {
            // procurar melhor trade-off
            if (totalNumRounds > 1000) {epsilon = 0.0;}
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
    
                            case FIRE: {
                                double amountToTurn = getHeading() - getGunHeading() + enemyBearing;
                                if(amountToTurn == 360.0 || amountToTurn == -360.0){amountToTurn=0.0;}
                                turnGunRight(amountToTurn);
                                fire(3);
                                break;
                            }
                        }

                        try {
                            train();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        myOperationMode = enumOptionalMode.scan;
                    }
                }
            }            
            execute();
        }
    }

    // Request functions
    private void initModel() throws IOException {
        // Criar um cliente HTTP para a requisição
        CloseableHttpClient httpClient = HttpClients.createDefault();
    
        // Criar uma requisição POST para enviar os dados de inicialização do modelo
        HttpPost request = new HttpPost("http://localhost:8000/init-neural-net");
    
        // Configurar o JSON para a requisição
        String json = "{"
                      + "\"input_size\":" + numInputs + ","
                      + "\"hidden_size\":" + numHidden + ","
                      + "\"output_size\":" + numOutput + ","
                      + "\"learning_rate\":" + learningRate + ","
                      + "\"gamma\":" + gamma
                      + "}";
        StringEntity entity = new StringEntity(json);
        request.setEntity(entity);
        request.setHeader("Content-Type", "application/json");
    
        // Executar a requisição
        CloseableHttpResponse response = null;
        try {
            response = httpClient.execute(request);
    
            // Verificar o status da resposta
            if (response.getStatusLine().getStatusCode() != 200) {
                throw new RuntimeException("Failed : HTTP error code : " + response.getStatusLine().getStatusCode());
            }
    
            // Consumir a resposta
            HttpEntity httpEntity = response.getEntity();
            String result = EntityUtils.toString(httpEntity);
            System.out.println("1Response from server: " + result);
        } finally {
            // Fechar a resposta e o cliente HTTP
            if (response != null) {
                response.close();
            }
            httpClient.close();
        }
    }

    private void train() throws IOException {
        // Criar um cliente HTTP para a requisição
        CloseableHttpClient httpClient = HttpClients.createDefault();
        
        // Criar uma requisição PATCH para enviar os dados de treinamento
        HttpPatch request = new HttpPatch("http://localhost:8000/train");

        // Configurar o JSON para a requisição
        String json = "{"
            + "\"state\":" + gson.toJson(previousState.toArray()) + ","
            + "\"action\":" + gson.toJson(Action.bipolarOneHotVectorOf(previousAction)) + ","
            + "\"reward\":" + currentReward + ","
            + "\"new_state\":" + gson.toJson(currentState.toArray()) + ","
            + "\"done\":" + isBattleDone
            + "}";

        StringEntity entity = new StringEntity(json);
        request.setEntity(entity);
        request.setHeader("Content-Type", "application/json");

        // Executar a requisição
        CloseableHttpResponse response = null;
        try {
            response = httpClient.execute(request);

            // Verificar o status da resposta
            if (response.getStatusLine().getStatusCode() != 200) {
                throw new RuntimeException("Failed : HTTP error code : " + response.getStatusLine().getStatusCode());
            }

            // Consumir a resposta
            HttpEntity httpEntity = response.getEntity();
            String result = EntityUtils.toString(httpEntity);
            // System.out.println("Response from server: " + result);
        } finally {
            // Fechar a resposta e o cliente HTTP
            if (response != null) {
                response.close();
            }
            httpClient.close();
        }
    }

    // Agent functions
    // public void remember(Experience experience) {
        // memory.add(experience);
    // }

    // public void trainLongMemory() {
        // LimitedDeque miniSample;
    
        // if (memory.size() > BATCH_SIZE) {
        //     List<Experience> experienceList = memory.getRandomSample(BATCH_SIZE);
        //     miniSample = new LimitedDeque(experienceList.size());
        //     miniSample.addAll(experienceList);
        // } else {
        //     miniSample = new LimitedDeque(memory.size());
        //     miniSample.addAll(memory.getAllExperiences());
        // }
    
        // for (Experience experience : miniSample) {
        //     trainer.trainStep(
        //         experience.previousState, 
        //         experience.action,
        //         experience.reward,
        //         experience.nextState,
        //         experience.done
        //     );
        // }
    // }

    // public void trainShortMemory(Experience experience) {
    //     trainer.trainStep(
    //             experience.previousState, 
    //             experience.action,
    //             experience.reward,
    //             experience.nextState,
    //             experience.done
    //         );
    // }

    public Action getAction() {
        if (Math.random() <= epsilon){
            // System.err.println("random action");
            currentAction = selectRandomAction();
        } else {
            currentAction = selectBestAction(
                    previousState.myEnergy,
                    previousState.enemyEnergy,
                    previousState.enemyBearing,
                    previousState.enemyDistance,
                    previousState.enemyVelocity,
                    previousState.distanceToCenter,
                    previousState.gunHeat
            );
            System.err.println("best action = " + currentAction);
        }

        return currentAction;
    }

    // general functions
    public Action selectRandomAction() {
        Random rand = new Random();
        int r = rand.nextInt(Action.values().length);
        return Action.values()[r];
    }

    // Função para selecionar a melhor ação
    public Action selectBestAction(
            double myEnergy,
            double enemyEnergy,
            double enemyBearing,
            double enemyDistance,
            double enemyVelocity,
            double distanceToCenter,
            double gunHeat
    ) {
        Action bestAction = null;
        State state = new State(myEnergy, enemyEnergy, enemyBearing, enemyDistance, enemyVelocity, distanceToCenter, gunHeat);

        try {
            String url = String.format("http://localhost:8000/predict?state=%.2f&state=%.2f&state=%.2f&state=%.2f&state=%.2f&state=%.2f&state=%.2f",
                    state.myEnergy, state.enemyEnergy, state.enemyBearing, state.enemyDistance, state.enemyVelocity, state.distanceToCenter, state.gunHeat);

            CloseableHttpClient httpClient = HttpClients.createDefault();
            HttpGet request = new HttpGet(url);

            CloseableHttpResponse response = httpClient.execute(request);

            if (response.getStatusLine().getStatusCode() == 200) {
                HttpEntity httpEntity = response.getEntity();
                String result = EntityUtils.toString(httpEntity);
                bestAction = Action.values()[Integer.parseInt(result.trim())];
                // System.err.println("Melhor acao: " + bestAction);
            } else {
                System.err.println("Failed to get prediction: " + response.getStatusLine().getReasonPhrase());
            }

            response.close();
            httpClient.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bestAction;
    }

    public void onEndBattle(double reward) {
        isBattleDone= true;
        currentReward = reward;
        totalReward += currentReward;

        // trainLongMemory();
        
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
