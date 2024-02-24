package target_custom;

import robocode.HitRobotEvent;
import robocode.Robot;
import robocode.ScannedRobotEvent;
import robocode.WinEvent;
import static robocode.util.Utils.normalRelativeAngleDegrees;

import java.awt.*;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;


public class Tracker extends Robot {
    private static boolean hasFirstLine = false;
    private int count = 0; // Keeps track of how long we've
    private double gunTurnAmt; // How much to turn our gun when searching
    private String trackName; // Name of the robot we're currently tracking

    public void run() {
        try {
            if (!hasFirstLine) {
                hasFirstLine = true;
                PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter("./robots/target_custom/training_data_tracker2.csv", true)));
                String headers = String.format("distancia,angulo_relativo,velocidade,energia,comportamento");
                writer.println(headers);
                writer.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Set colors
        setBodyColor(new Color(128, 128, 50));
        setGunColor(new Color(50, 50, 20));
        setRadarColor(new Color(200, 200, 70));
        setScanColor(Color.white);
        setBulletColor(Color.blue);

        // Prepare gun
        trackName = null; // Initialize to not tracking anyone
        setAdjustGunForRobotTurn(true); // Keep the gun still when we turn
        gunTurnAmt = 10; // Initialize gunTurn to 10

        // Loop forever
        while (true) {
            // turn the Gun (looks for enemy)
            turnGunRight(gunTurnAmt);
            // Keep track of how long we've been looking
            count++;
            // If we've haven't seen our target for 2 turns, look left
            if (count > 2) {
                gunTurnAmt = -10;
            }
            // If we still haven't seen our target for 5 turns, look right
            if (count > 5) {
                gunTurnAmt = 10;
            }
            // If we *still* haven't seen our target after 10 turns, find another target
            if (count > 11) {
                trackName = null;
            }
        }
    }

    public void onScannedRobot(ScannedRobotEvent e) {
        double[] currentRow = new double[5];
        currentRow[0] = e.getDistance();
        currentRow[1] = e.getBearing();
        currentRow[2] = e.getVelocity();
        currentRow[3] = e.getEnergy();

        String behavior;
        if (e.getDistance() > 150) {
            behavior = "movendo_para_frente";
        } else if (e.getDistance() < 150) {
            behavior = "atacando";
        } else {
            behavior = "parado";
        }
        System.err.println(e.getDistance());
        System.err.println(e.getBearing());
        System.err.println(e.getVelocity());
        System.err.println(e.getEnergy());
        System.err.println(behavior);
        System.err.println("------------");
        currentRow[4] = getBehaviorClassIndex(behavior);
        addRowToTxt(currentRow);

        if (trackName != null && !e.getName().equals(trackName)) {
            return;
        }

        if (trackName == null) {
            trackName = e.getName();
            out.println("Tracking " + trackName);
        }
        count = 0;

        if (e.getDistance() > 150) {
            gunTurnAmt = normalRelativeAngleDegrees(e.getBearing() + (getHeading() - getRadarHeading()));
            turnGunRight(gunTurnAmt);
            turnRight(e.getBearing());
            ahead(e.getDistance() - 140);
            return;
        }

        gunTurnAmt = normalRelativeAngleDegrees(e.getBearing() + (getHeading() - getRadarHeading()));
        turnGunRight(gunTurnAmt);
        fire(3);

        if (e.getDistance() < 100) {
            if (e.getBearing() > -90 && e.getBearing() <= 90) {
                back(40);
            } else {
                ahead(40);
            }
        }
        scan();
    }

    private void addRowToTxt(double[] row) {
        try {
            PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter("./robots/target_custom/training_data_tracker2.csv", true)));
            String rowString = String.format("%.14f,%.14f,%.1f,%.1f,%.1f", row[0], row[1], row[2], row[3], row[4]);

            writer.println(rowString);

            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private double getBehaviorClassIndex(String behavior) {
        switch (behavior) {
            case "movendo_para_frente":
                return 0;
            case "atacando":
                return 1;
            case "parado":
                return 2;
            default:
                return -1;
        }
    }

    public void onHitRobot(HitRobotEvent e) {
        // Only print if he's not already our target.
        if (trackName != null && !trackName.equals(e.getName())) {
            out.println("Tracking " + e.getName() + " due to collision");
        }
        // Set the target
        trackName = e.getName();
        // Back up a bit.
        // Note:  We won't get scan events while we're doing this!
        // An AdvancedRobot might use setBack(); execute();
        gunTurnAmt = normalRelativeAngleDegrees(e.getBearing() + (getHeading() - getRadarHeading()));
        turnGunRight(gunTurnAmt);
        fire(3);
        back(50);
    }

    public void onWin(WinEvent e) {
        for (int i = 0; i < 50; i++) {
            turnRight(30);
            turnLeft(30);
        }
    }
}
