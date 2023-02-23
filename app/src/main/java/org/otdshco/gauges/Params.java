package org.otdshco.gauges;

public class Params
{
    // TODO: GET SCREEN VALUES FROM SETTINGS
    public static int TEXT_SIZE = 20;
    public static int STROKE_WIDTH = 2;
    public static int STROKE_BOLD_WIDTH = 3;

    public static float CENTER_GAP = 50;
    public static float HORIZON_LEN = 150;
    public static float DASH_GAP_LEN = 20;
    public static float DASH_FILL_LEN = 20;
    public static float CROSS_HAIR_LEN = 15;
    public static float FLIGHT_PATH_RAD = 10;
    public static float LARGER_MARGIN_LEN = 100;
    public static float UNITS_PER_GRADUATION = 3;

    public static double LARGER_MARGIN_VAL = 3;

    public static int screenWidth = 600;
    public static int screenHeight = 1280;

    public static float halfScreenWidth = screenWidth / 2F;
    public static float halfScreenHeight = screenHeight / 2F;

    // 250F / ( 23F * 9F / 16F )
    public static float parameter = 250F;
    public static float parameter2x = parameter * 2;
    public static float parameter1 = parameter;
    public static float parameter0 = parameter2x;
    public static final float parameter2 = 23F;
    public static final float parameter3 = 9F;
    public static final float parameter4 = 16F;

    public static final float overallValueGaugeDisplayOnFrame = 12.F;

    public static final int constantRightDegrees = 90;

    public static final int accelerometerSamplingPeriod = 30000;
    public static final int rotationVectorSamplingPeriod = 30000;

    public static final int defaultSensorMaxSamples = 5;

    public static final float defaultArrowVelocity = 48F;
    public static final float defaultLensFactor = 1.15F; // 1.15F; 1280PX // 2.13F; 2400PX
    public static final float defaultPersonHeight = 1.6F;
    public static final float defaultArrowDiameter = 0.0065F;
    public static final float defaultArrowMass = 0.018F;

    public static final boolean defaultAccelerationFormulas = true;
    public static final boolean defaultGravityFormulas = true;
    public static final boolean defaultSensorMethod = true;

    public static final String settingsEnvironmentValues = "environmentSettings";
    public static final String settingsSensorMaxSamples = "sensorMaxSamples";
    public static final String settingsArrowVelocity = "arrowVelocity";
    public static final String settingsPersonHeight = "personHeight";
    public static final String settingsArrowDiameter = "arrowDiameter";
    public static final String settingsArrowMass = "arrowMass";
    public static final String settingsAccelerationFormulas = "accelerationFormulas";
    public static final String settingsGravityFormulas = "gravityFormulas";
    public static final String settingsLensFactor = "lensFactor";
    public static final String settingsSensorMethod = "sensorMethod";

    public static final String settingsSeekZoom = "seekZoom";
    public static final String settingsSeekDistance = "seekDistance";

    public static final int defaultSeekZoom = 1;
    public static final float defaultSeekDistance = 10;
    public static final double valueTimeStep = 0.005;
    public static final double constantDensityOfAirKgM3 = 1.2;
    public static final double constantDragCoefficient = 9 * 0.3;
    public static final double constantGravitationalField = 9.81;

    public static final double RAD_TO_DEG = 57.295779513082320876798154814105;

    // Based on a measured estimate of the pitch attitude of the FRL of 5 deg and 4.32 deg of Bosch
    public static final double headPitchBias = 0; // degrees

    // public static final double headPitchBias = 2.16; // degrees
    public static final double[] coefficientArray = new double[] { 0.003, 0.01, 0.05 };

    public static int levels = 30;
    public static int indCoefficient = 1;
    public static int valueSensorMaxSample = defaultSensorMaxSamples;


    public static volatile Params hudParams = new Params( );
    public static volatile int valueScreenZoom = defaultSeekZoom;
    public static volatile float valueTargetDistance = defaultSeekDistance;
    public static volatile double valueLensFactor = defaultLensFactor;
    public static volatile double valuePersonHeight = defaultPersonHeight;
    public static volatile double inclinationAngle = 0;
    public static volatile double headPitch = 0;
    public static volatile double redAim;
    public static volatile double greenAim;
    public static volatile double blueAim;
    public static volatile double FILTER_COEFFICIENT = 0.1;

    public double roll = 0;
    public double pitch = 0;
    public double flightPath = 0;
}