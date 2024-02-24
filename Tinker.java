package tinker;

import robocode.*;
import static robocode.util.Utils.normalRelativeAngleDegrees;

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
    static int numInputs = 4;
    static int numHidden1Size = 8;
    static int numHidden2Size = 8;
    static int numOutput = 3;
    static double learningRate = 0.01;
    static int epochs = 10000;
    
    // Robot mode
    public enum enumOptionalMode {scan, performanceAction};
    private enumOptionalMode myOperationMode = enumOptionalMode.scan;
    private int direction = 1;

    // Robot states
    private State currentState = new State(0,0,0,0);
    private Action currentAction = Action.values()[0];
    private boolean isBattleDone = false;
    private double gunTurnAmt;
    private int count = 0;
    
    // Statistics-data
    private int targetNumRounds = 0;
    public static int totalNumRounds = 0;
    public static int numRoundsTo100 = 0;
    public static int numWins = 0;
    static private double curr_best_win_rate = 0.0;

    // Initialize states
    double enemyBearing = 0.0;
    double enemyDistance = 0.0;
    double enemyEnergy = 0.0;
    double enemyVelocity = 0.0;

    // Utils
    Gson gson = new Gson();
        
    public void run() {
        setColors(Color.white, Color.red, Color.lightGray, Color.red, Color.white);
        setAdjustGunForRobotTurn(true);
        isBattleDone = false;

        while (true) {
            if (Math.abs(getVelocity()) == 0.0) {
                switch (myOperationMode) {
                    case scan: {                        
                        turnGunRight(90);
                        break;
                    }
                    case performanceAction: {
                        Action currentAction = getAction();

                        switch (currentAction) {
                            case FORWARD: {
                                gunTurnAmt = normalRelativeAngleDegrees(currentState.enemyBearing + (getHeading() - getRadarHeading()));
                                turnGunRight(gunTurnAmt);
                                turnRight(currentState.enemyBearing);
                                ahead(currentState.enemyDistance - 140);
                                execute();
                                break;
                            }
    
                            case FIRE: {
                                gunTurnAmt = normalRelativeAngleDegrees(currentState.enemyBearing + (getHeading() - getRadarHeading()));
                                turnGunRight(gunTurnAmt);
                                fire(3);
                                break;
                            }

                            case STILL: {
                                if (currentState.enemyDistance < 100) {
                                    if (currentState.enemyBearing > -90 && currentState.enemyBearing <= 90) {
                                        back(40);
                                    } else {
                                        ahead(40);
                                    }
                                }
                                break;
                            }
                        }
                        myOperationMode = enumOptionalMode.scan;
                    }
                }
            }            
            execute();
        }
    }

    // Request functions
    // private void initModel() throws IOException {
    //     CloseableHttpClient httpClient = HttpClients.createDefault();
    
    //     HttpPost request = new HttpPost("http://localhost:8000/init-neural-net");
    
    //     String json = "{"
    //                   + "\"input_size\":" + numInputs + ","
    //                   + "\"hidden1_size\":" + numHidden1Size + ","
    //                   + "\"hidden2_size\":" + numHidden2Size + ","
    //                   + "\"output_size\":" + numOutput + ","
    //                   + "\"learning_rate\":" + learningRate + ","
    //                   + "\"epochs\":" + epochs
    //                   + "}";
    //     StringEntity entity = new StringEntity(json);
    //     request.setEntity(entity);
    //     request.setHeader("Content-Type", "application/json");
    
    //     CloseableHttpResponse response = null;
    //     try {
    //         response = httpClient.execute(request);
    
    //         if (response.getStatusLine().getStatusCode() != 200) {
    //             throw new RuntimeException("Failed : HTTP error code : " + response.getStatusLine().getStatusCode());
    //         }
    
    //         HttpEntity httpEntity = response.getEntity();
    //         String result = EntityUtils.toString(httpEntity);
    //         System.out.println("1Response from server: " + result);
    //     } finally {
    //         if (response != null) {
    //             response.close();
    //         }
    //         httpClient.close();
    //     }
    // }

    // private void train() throws IOException {
    //     CloseableHttpClient httpClient = HttpClients.createDefault();
        
    //     HttpPatch request = new HttpPatch("http://localhost:8000/train");

    //     // String json = "{"
    //     //     + "\"state\":" + gson.toJson(previousState.toArray()) + ","
    //     //     + "\"action\":" + gson.toJson(Action.bipolarOneHotVectorOf(previousAction)) + ","
    //     //     + "\"reward\":" + currentReward + ","
    //     //     + "\"new_state\":" + gson.toJson(currentState.toArray()) + ","
    //     //     + "\"done\":" + isBattleDone
    //     //     + "}";

    //     // StringEntity entity = new StringEntity(json);
    //     // request.setEntity(entity);
    //     request.setHeader("Content-Type", "application/json");

    //     CloseableHttpResponse response = null;
    //     try {
    //         response = httpClient.execute(request);

    //         if (response.getStatusLine().getStatusCode() != 200) {
    //             throw new RuntimeException("Failed : HTTP error code : " + response.getStatusLine().getStatusCode());
    //         }

    //         HttpEntity httpEntity = response.getEntity();
    //         String result = EntityUtils.toString(httpEntity);
    //         // System.out.println("Response from server: " + result);
    //     } finally {
    //         if (response != null) {
    //             response.close();
    //         }
    //         httpClient.close();
    //     }
    // }

    public Action getAction() {
        currentAction = predictAction(
                currentState.enemyEnergy,
                currentState.enemyBearing,
                currentState.enemyDistance,
                currentState.enemyVelocity
        );
        return currentAction;
    }

    public Action predictAction(
            double enemyEnergy,
            double enemyBearing,
            double enemyDistance,
            double enemyVelocity
    ) {
        Action bestAction = null;
        State state = new State(enemyEnergy, enemyBearing, enemyDistance, enemyVelocity);

        try {
            String url = String.format("http://localhost:8000/predict?state=%.2f&state=%.2f&state=%.2f&state=%.2f",
                    state.enemyDistance, state.enemyBearing, state.enemyVelocity, state.enemyEnergy);

            CloseableHttpClient httpClient = HttpClients.createDefault();
            HttpGet request = new HttpGet(url);

            CloseableHttpResponse response = httpClient.execute(request);

            if (response.getStatusLine().getStatusCode() == 200) {
                HttpEntity httpEntity = response.getEntity();
                String result = EntityUtils.toString(httpEntity);
                bestAction = Action.values()[Integer.parseInt(result.trim())];
                System.err.println("predictAction: " + bestAction);
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

    public void onEndBattle() {
        isBattleDone= true;
        
        if (numRoundsTo100 < 100) {
            numRoundsTo100++;
            totalNumRounds++;
            numWins++;
        } else {
            try {
                winrate(numWins * 1.0/numRoundsTo100);
                numRoundsTo100 = 0;
                numWins = 0;
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
        execute();
    }

    private void winrate(double winRate) throws IOException {
        if(winRate >= curr_best_win_rate*1.05 & winRate > 0.5){
            curr_best_win_rate = winRate;
        }
    }

    // override robot functions
    @Override
    public void onScannedRobot(ScannedRobotEvent event) {
        enemyEnergy = event.getEnergy();
        enemyBearing = event.getBearing();
        enemyDistance = event.getDistance();
        enemyVelocity = event.getVelocity();

        currentState.enemyEnergy = event.getEnergy();
        currentState.enemyBearing = event.getBearing();
        currentState.enemyDistance = event.getDistance();
        currentState.enemyVelocity = event.getVelocity();
    
        myOperationMode = enumOptionalMode.performanceAction;
    }


    @Override
    public void onHitWall(HitWallEvent event) {
        direction *= -1;
    }

    @Override
    public void onDeath(DeathEvent e) {
        onEndBattle();
    }

    @Override
    public void onWin(WinEvent e) {
        onEndBattle();
    }
}
