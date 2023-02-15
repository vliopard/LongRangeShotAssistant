package org.otdshco;

import java.lang.Math;
import java.util.ArrayList;
import java.util.List;

public class Main {

    public static double getOpositeAngle(double height, double distance) {
        return Math.atan(height / distance);
    }

    public static double zoomToAngle(double zoom, double max, double min) {
        double maxZoom = 100;
        return (((zoom - 1) * (min - max)) / (maxZoom - 1)) + max;
    }

    public static double getObjectHeight(double angle, double distance) {
        return distance * Math.tan(angle);
    }

    public static double toRadians(double grade) {
        return grade * Math.PI / 180;
    }

    public static void print(String message) {
        System.out.println(message);
    }

    public static double roundAvoid(double value, int places) {
        double scale = Math.pow(10, places);
        return Math.round(value * scale) / scale;
    }

    public static void calculate(double[][] l1, String msg) {
        print("CALCULATE [" + msg + "] ==============================");
        double[] distl = {25, 32, 40, 44, 75, 115};
        List<Double> angles = new ArrayList<Double>();
        double sum = 0;
        double average;
        for (int i = 0; i < l1.length; i++) {
            double height = l1[i][0];
            double dist = l1[i][1];
            double oa = getOpositeAngle(height, dist);
            angles.add(oa);
            print("Height[" + height + "] Distance[" + dist + "] Angle[" + oa + "]");
        }
        for (Double mark : angles) {
            sum += mark;
        }
        average = sum / angles.size();
        double average1 = roundAvoid(average, 2);
        print("AVERAGE[" + msg + "][" + average1 + "][" + average + "]");
        for (int i = 0; i < distl.length; i++) {
            double objectHeight = getObjectHeight(average1, distl[i]);
            print("Height[" + objectHeight + "] Distance[" + distl[i] + "]");
        }
    }

    public static double trends(double x) {
        x = 101 - x;
        return 6E-9 * Math.pow(x, 4) - 1E-6 * Math.pow(x, 3) + 2E-5 * Math.pow(x, 2) + 0.011 * x + 0.149;
    }

    public static void main(String[] args) {
        double[][] l1 = {
                {31.0, 25.0},
                {39.7, 32.0},
                {51.3, 40.0},
                {56.1, 44.0},
                {98.0, 75.0},
                {149.0, 114.0}
        };
        double[][] l25 = {
                {25.0, 25.0},
                {34.5, 32.0},
                {40.5, 40.0},
                {43.2, 44.0},
                {77.5, 75.0},
                {117.5, 114.0}
        };
        double[][] l50 = {
                {16.5, 25.0},
                {23.0, 32.0},
                {30.5, 40.0},
                {33.0, 44.0},
                {55.0, 75.0},
                {84.5, 114.0},
                {85.0, 114.0},
                {86.0, 114.0}
        };
        double[][] l75 = {
                {11.0, 25.0},
                {15.2, 32.0},
                {16.9, 40.0},
                {19.5, 44.0},
                {34.5, 75.0},
                {52.5, 114.0}
        };
        double[][] l100 = {
                {3.0, 25.0},
                {5.1, 32.0},
                {6.4, 40.0},
                {7.2, 44.0},
                {12.6, 75.0},
                {19.0, 114.0}
        };
        double[] distances = {1, 10, 25, 32, 40, 44, 50, 60, 75, 80, 90, 99, 100, 114};

        try {
            for (int zoom = 1; zoom <= 100; zoom++) {
                for (int distanceIndex = 0; distanceIndex < distances.length; distanceIndex++) {
                    double screenHeight = getObjectHeight(trends(zoom), distances[distanceIndex]);
                    print("Height[" + screenHeight + "] Distance[" + distances[distanceIndex] + "]@zoom[" + zoom + "]");
                }
            }
        } catch (Exception exception) {
            print("Ex: " + exception);
        }

        print("================================");
        for (int i = 0; i < distances.length; i++) {
            double zta = zoomToAngle(distances[i], 0.91, 0.16);
            print("zoom[" + distances[i] + "] to angle[" + zta + "]");

            double ztc = trends(distances[i]);
            print("zoom[" + distances[i] + "] to @ngle[" + ztc + "]");
        }

        calculate(l1, "1");
        //oppositeAngleZoom = 0.91;
        calculate(l25, "25");
        //oppositeAngleZoom = 0.79;
        calculate(l50, "50");
        //oppositeAngleZoom = 0.64;
        calculate(l75, "75");
        //oppositeAngleZoom = 0.42;
        calculate(l100, "100");
        //oppositeAngleZoom = 0.16;



    }
}