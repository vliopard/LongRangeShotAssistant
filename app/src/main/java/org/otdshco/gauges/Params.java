package org.otdshco.gauges;

public class Params
{
    // TODO: GET SCREEN VALUES FROM SETTINGS
    public static int TEXT_SIZE = 30;
    public static int STROKE_WIDTH = 1;
    public static int STROKE_BOLD_WIDTH = 3;

    public static float CENTER_GAP = 50;
    public static float HORIZON_LEN = 300; // Horizon length (px)
    public static float DASH_GAP_LEN = 20;  // Gap in dashed line, for negative value (px)
    public static float DASH_FILL_LEN = 20; // Dash length in dashed line, for negative value (px)
    public static float CROSS_HAIR_LEN = 15;
    public static float FLIGHT_PATH_RAD = 10;
    public static float LARGER_MARGIN_LEN = 200; // Larger graduation length, (px)
    public static float UNITS_PER_GRADUATION = 3;

    public static double LARGER_MARGIN_VAL = 3; // Larger graduation value

    public static int screenWidth = 600;
    public static int screenHeight = 2400;

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
    public static final float defaultPersonHeight = 1.6F;
    public static final float defaultArrowDiameter = 0.0065F;
    public static final float defaultArrowMass = 0.009F;

    public static final boolean defaultAccelerationFormulas = true;
    public static final boolean defaultGravityFormulas = true;

    public static final float defaultLensFactor = 2.13F;

    public static final String settingsEnvironmentValues = "environment_settings";
    public static final String settingsSensorMaxSamples = "sensor_max_samples";
    public static final String settingsArrowVelocity = "arrow_velocity";
    public static final String settingsPersonHeight = "person_height";
    public static final String settingsArrowDiameter = "arrow_diameter";
    public static final String settingsArrowMass = "arrow_mass";
    public static final String settingsAccelerationFormulas = "acceleration_formulas";
    public static final String settingsGravityFormulas = "gravity_formulas";
    public static final String settingsLensFactor = "lens_factor";

    public static final String settingsSeekZoom = "seek_zoom";
    public static final String settingsSeekDistance = "seek_distance";

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

    public static int defaultSeekZoom = 1;
    public static int defaultSeekDistance = 10;

    public static volatile Params hudParams = new Params( );
    public static volatile int valueScreenZoom = defaultSeekZoom;
    public static volatile float valueTargetDistance = defaultSeekDistance;
    public static volatile double valuePersonHeight = defaultPersonHeight;
    public static volatile double valueLensFactor = defaultLensFactor;
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