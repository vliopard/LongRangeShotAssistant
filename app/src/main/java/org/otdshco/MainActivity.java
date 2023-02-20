package org.otdshco;

// TODO: BUGFIX - CHECK WHY TRAJECTORY IS ABOVE RETICLE CENTER AT NEGATIVE INCLINATION ANGLE
// TODO: BUGFIX - FIX HORIZON LINE WHILE ZOOMING

// TODO: USE GRAVITY FORMULAS AND ACCELERATION FORMULAS AT THE SAME TIME WITH DIFFERENT CROSSHAIR COLORS (MAGENTA, CYAN, LIME)
// TODO: PARAMETERS OF HARDWARE AND SOFTWARE - CHANGE HARDCODED CONSTANTS TO HAVE EITHER AUTOMATIC DETECTION OR EVEN MANUAL INPUT VIA SETTINGS (LIKE timeStep)

// TODO: DOUBLE CHECK UNITS (SOMETIMES CM, SOMETIMES METERS) AND PUT STANDARDS TO CM INSTEAD OF HANDLING METERS

// TODO: OPTIMIZATION - TRY TO REDUCE THE AMOUNT OF LOOPS AND AVOID PHONE HEATING
// TODO: OPTIMIZATION - CHANGE THE timeStep TO GREATER VALUE 
// TODO: OPTIMIZATION - GO STRAIGHT TO THE LAST CALCULATION RESULT WITHOUT LOOPING
// TODO: OPTIMIZATION - CHANGE SENSOR LISTENER RATE FOR SHORT VALUES

import static org.otdshco.gauges.Params.FILTER_COEFFICIENT;
import static org.otdshco.gauges.Params.RAD_TO_DEG;
import static org.otdshco.gauges.Params.accelerometerSamplingPeriod;
import static org.otdshco.gauges.Params.cameraFov;
import static org.otdshco.gauges.Params.coefficientArray;

import static org.otdshco.gauges.Params.constantDensityOfAirKgM3;
import static org.otdshco.gauges.Params.constantDragCoefficient;
import static org.otdshco.gauges.Params.constantGravitationalField;
import static org.otdshco.gauges.Params.constantRightDegrees;

import static org.otdshco.gauges.Params.defaultAccelerationFormulas;
import static org.otdshco.gauges.Params.defaultArrowDiameter;
import static org.otdshco.gauges.Params.defaultArrowMass;
import static org.otdshco.gauges.Params.defaultArrowVelocity;
import static org.otdshco.gauges.Params.defaultGravityFormulas;
import static org.otdshco.gauges.Params.defaultLensFactor;
import static org.otdshco.gauges.Params.defaultPersonHeight;
import static org.otdshco.gauges.Params.defaultSeekDistance;
import static org.otdshco.gauges.Params.defaultSeekZoom;
import static org.otdshco.gauges.Params.defaultSensorMaxSamples;

import static org.otdshco.gauges.Params.headPitch;
import static org.otdshco.gauges.Params.inclinationAngle;
import static org.otdshco.gauges.Params.indCoefficient;
import static org.otdshco.gauges.Params.overallValueGaugeDisplayOnFrame;
import static org.otdshco.gauges.Params.parameter0;
import static org.otdshco.gauges.Params.parameter2;
import static org.otdshco.gauges.Params.parameter3;
import static org.otdshco.gauges.Params.parameter4;

import static org.otdshco.gauges.Params.redAim;
import static org.otdshco.gauges.Params.greenAim;
import static org.otdshco.gauges.Params.blueAim;

import static org.otdshco.gauges.Params.rotationVectorSamplingPeriod;
import static org.otdshco.gauges.Params.screenHeight;

import static org.otdshco.gauges.Params.settingsAccelerationFormulas;
import static org.otdshco.gauges.Params.settingsArrowDiameter;
import static org.otdshco.gauges.Params.settingsArrowMass;
import static org.otdshco.gauges.Params.settingsArrowVelocity;
import static org.otdshco.gauges.Params.settingsEnvironmentValues;
import static org.otdshco.gauges.Params.settingsGravityFormulas;
import static org.otdshco.gauges.Params.settingsLensFactor;
import static org.otdshco.gauges.Params.settingsPersonHeight;
import static org.otdshco.gauges.Params.settingsSeekDistance;
import static org.otdshco.gauges.Params.settingsSeekZoom;
import static org.otdshco.gauges.Params.settingsSensorMaxSamples;

import static org.otdshco.gauges.Params.valuePersonHeight;
import static org.otdshco.gauges.Params.valueScreenZoom;
import static org.otdshco.gauges.Params.valueSensorMaxSample;
import static org.otdshco.gauges.Params.valueTargetDistance;
import static org.otdshco.gauges.Params.valueTimeStep;

import static java.lang.Math.PI;
import static java.lang.Math.cos;
import static java.lang.Math.pow;
import static java.lang.Math.sin;
import static java.lang.Math.toRadians;
import static java.lang.StrictMath.atan;
import static java.lang.StrictMath.sqrt;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.view.KeyEvent;
import android.view.MotionEvent;

import android.view.SurfaceView;
import android.view.View;

import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import androidx.camera.core.Camera;
import androidx.camera.core.CameraControl;
import androidx.camera.core.CameraInfo;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.FocusMeteringAction;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.MeteringPointFactory;
import androidx.camera.core.Preview;
import androidx.camera.extensions.HdrImageCaptureExtender;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;
import com.lukelorusso.verticalseekbar.VerticalSeekBar;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;

import org.otdshco.gauges.Takeoff;

public class MainActivity extends AppCompatActivity implements SensorEventListener
{
    private final int REQUEST_CODE_PERMISSIONS = 1001;
    private final String[] REQUIRED_PERMISSIONS = new String[] { "android.permission.CAMERA", "android.permission.WRITE_EXTERNAL_STORAGE" };

    private final List<Double>[] rollingAverage = new List[3];

    private double generalProgress = 0;

    private Camera camera;
    private CameraInfo cameraInfo;
    private CameraSelector cameraSelector;

    private Takeoff pitchTakeOff = null;

    private TextView textViewForInclinationAngle;

    private int valueLensFactor = defaultLensFactor;
    private ImageView pitchImageView;
    private ImageView imageViewHorizonLine;
    private SurfaceView pitchSurfaceView;

    private PrintWriter printWriter;
    private PreviewView cameraPreview;
    private RelativeLayout relativeLayoutForRotation;
    private VerticalSeekBar verticalSeekBarZoom;
    private VerticalSeekBar verticalSeekBarTargetDistance;

    private int backButtonPress = 0;
    private int imageViewHeight = 0;

    private double valueArrowVelocity = defaultArrowVelocity;
    private double valueArrowDiameter = defaultArrowDiameter;
    private double valueArrowMass = defaultArrowMass;
    private boolean valueAccelerationFormulas = defaultAccelerationFormulas;
    private boolean valueGravityFormulas = defaultGravityFormulas;

    private int getPosition( int value )
    {
        switch ( value )
        {
            case 0:
                return R.drawable.number00;
            case 1:
                return R.drawable.number01;
            case 2:
                return R.drawable.number02;
            case 3:
                return R.drawable.number03;
            case 4:
                return R.drawable.number04;
            case 5:
                return R.drawable.number05;
            case 6:
                return R.drawable.number06;
            case 7:
                return R.drawable.number07;
            case 8:
                return R.drawable.number08;
            case 9:
                return R.drawable.number09;
            default:
                return R.drawable.number99;
        }
    }

    private void saveSettings( )
    {
        SharedPreferences sharedPreferences = getSharedPreferences( "environment_settings", Context.MODE_PRIVATE );
        SharedPreferences.Editor editor = sharedPreferences.edit( );
        editor.putInt( settingsSeekZoom, valueScreenZoom );
        editor.putFloat( settingsSeekDistance, valueTargetDistance );
        editor.apply( );
    }

    private void loadSettings( )
    {
        SharedPreferences sharedPreferences = getSharedPreferences( settingsEnvironmentValues, MODE_PRIVATE );
        valueSensorMaxSample = sharedPreferences.getInt( settingsSensorMaxSamples, defaultSensorMaxSamples );
        valueArrowVelocity = sharedPreferences.getFloat( settingsArrowVelocity, defaultArrowVelocity );
        valuePersonHeight = sharedPreferences.getFloat( settingsPersonHeight, defaultPersonHeight );
        valueArrowDiameter = sharedPreferences.getFloat( settingsArrowDiameter, defaultArrowDiameter );
        valueArrowMass = sharedPreferences.getFloat( settingsArrowMass, defaultArrowMass );
        valueAccelerationFormulas = sharedPreferences.getBoolean( settingsAccelerationFormulas, defaultAccelerationFormulas );
        valueGravityFormulas = sharedPreferences.getBoolean( settingsGravityFormulas, defaultGravityFormulas );
        valueScreenZoom = sharedPreferences.getInt( settingsSeekZoom, defaultSeekZoom );
        valueTargetDistance = sharedPreferences.getFloat( settingsSeekDistance, defaultSeekDistance );
        valueLensFactor = sharedPreferences.getInt( settingsLensFactor, defaultLensFactor );
    }

    private void setIcon( int progress, int tens, int ones, VerticalSeekBar verticalSeekBar, int drawablePlaceHolder )
    {
        int cen = ( progress % 1000 ) / 100;
        int dez = ( progress % 100 ) / 10;
        int uni = ( progress % 100 ) % 10;

        @SuppressLint ( "UseCompatLoadingForDrawables" ) LayerDrawable layerDrawable = ( LayerDrawable ) getResources( ).getDrawable( drawablePlaceHolder, this.getTheme( ) );

        int position0;
        if ( cen == 1 )
        {
            position0 = getPosition( 1 );
        }
        else
        {
            position0 = getPosition( dez );
        }

        @SuppressLint ( "UseCompatLoadingForDrawables" ) Drawable drawable0 = getResources( ).getDrawable( position0, this.getTheme( ) );
        layerDrawable.setDrawableByLayerId( tens, drawable0 );

        int position1;
        if ( cen == 1 )
        {
            position1 = getPosition( 99 );
        }
        else
        {
            position1 = getPosition( uni );
        }

        @SuppressLint ( "UseCompatLoadingForDrawables" ) Drawable drawable1 = getResources( ).getDrawable( position1, this.getTheme( ) );
        layerDrawable.setDrawableByLayerId( ones, drawable1 );
        verticalSeekBar.setThumbPlaceholderDrawable( layerDrawable );
    }

    @SuppressLint ( { "WrongViewCast", "UseCompatLoadingForDrawables", "ClickableViewAccessibility" } )
    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        getWindow( ).setFlags( WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN );
        View view = getWindow( ).getDecorView( );
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN;
        view.setSystemUiVisibility( uiOptions );
        setContentView( R.layout.activity_main );

        Button button = findViewById( R.id.settings );
        button.setOnClickListener( v -> {
            Intent intent = new Intent( MainActivity.this, Settings.class );
            startActivity( intent );
        } );

        pitchSurfaceView = findViewById( R.id.sv_pitch );
        pitchImageView = findViewById( R.id.im_pitch );

        cameraPreview = findViewById( R.id.camera_preview );
        relativeLayoutForRotation = findViewById( R.id.rotate_layout );

        imageViewHorizonLine = findViewById( R.id.aim_level_image );

        textViewForInclinationAngle = findViewById( R.id.inclination_text );

        Button buttonZoomUp = findViewById( R.id.bt_zoom_up );
        Button buttonZoomDown = findViewById( R.id.bt_zoom_down );
        Button buttonDistanceUp = findViewById( R.id.bt_dist_up );
        Button buttonDistanceDown = findViewById( R.id.bt_dist_down );

        verticalSeekBarZoom = findViewById( R.id.zoomSeekBar );
        verticalSeekBarTargetDistance = findViewById( R.id.distanceSeekBar );

        verticalSeekBarTargetDistance.setThumbPlaceholderDrawable( getResources( ).getDrawable( R.drawable.seekbar_distance, this.getTheme( ) ) );

        rollingAverage[0] = new ArrayList<>( );
        rollingAverage[1] = new ArrayList<>( );
        rollingAverage[2] = new ArrayList<>( );

        loadSettings( );

        if ( allPermissionsGranted( ) )
        {
            startCamera( );
        }
        else
        {
            ActivityCompat.requestPermissions( this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS );
        }

        RelativeLayout relative_main_layout = findViewById( R.id.main_layout );
        relative_main_layout.setOnTouchListener( ( v, event ) -> {
            if ( event.getAction( ) != MotionEvent.ACTION_UP )
            {
                return true;
            }
            MeteringPointFactory meteringPointFactory = cameraPreview.createMeteringPointFactory( cameraSelector );
            float eventX = event.getX( );
            float eventY = event.getY( );
            FocusMeteringAction action = new FocusMeteringAction.Builder( meteringPointFactory.createPoint( eventX, eventY ) ).build( );
            AtomicReference<CameraControl> cameraControl = new AtomicReference<>( camera.getCameraControl( ) );
            cameraControl.get( ).cancelFocusAndMetering( );
            cameraControl.get( ).startFocusAndMetering( action );
            return true;
        } );

        verticalSeekBarZoom.setOnTouchListener( ( v, event ) -> {
            if ( event.getAction( ) != MotionEvent.ACTION_UP )
            {
                return true;
            }
            return true;
        } );

        verticalSeekBarTargetDistance.setOnTouchListener( ( v, event ) -> {
            if ( event.getAction( ) != MotionEvent.ACTION_UP )
            {
                return true;
            }
            return true;
        } );

        verticalSeekBarZoom.setOnReleaseListener( ( progress ) -> null );
        verticalSeekBarTargetDistance.setOnReleaseListener( ( progress ) -> null );
        verticalSeekBarZoom.setOnPressListener( ( progress ) -> null );
        verticalSeekBarTargetDistance.setOnPressListener( ( progress ) -> null );
        verticalSeekBarZoom.setOnProgressChangeListener( ( progress ) -> {
            valueScreenZoom = progress;
            setIcon( progress, R.id.chargingIconLeftZoom, R.id.chargingIconRightZoom, verticalSeekBarZoom, R.drawable.seekbar_zoom );
            float linearZoom = progress.floatValue( ) / 100F;
            AtomicReference<CameraControl> cameraControl = new AtomicReference<>( camera.getCameraControl( ) );
            cameraControl.get( ).setLinearZoom( linearZoom );
            saveSettings( );
            return null;
        } );

        verticalSeekBarTargetDistance.setOnProgressChangeListener( ( progress ) -> {
            valueTargetDistance = progress;
            setIcon( progress, R.id.chargingIconLeftDistance, R.id.chargingIconRightDistance, verticalSeekBarTargetDistance, R.drawable.seekbar_distance );
            generalProgress = progress;
            saveSettings( );
            shot( );
            return null;
        } );

        buttonZoomUp.setOnClickListener( v -> {
            cameraInfo = camera.getCameraInfo( );
            float scale = Objects.requireNonNull( cameraInfo.getZoomState( ).getValue( ) ).getLinearZoom( );

            float linearScale = scale * 100;
            if ( linearScale < 100 )
            {
                linearScale = linearScale + 1;
            }

            verticalSeekBarZoom.setProgress( ( int ) linearScale );
            valueScreenZoom = ( int ) linearScale;
            setIcon( ( int ) linearScale, R.id.chargingIconLeftZoom, R.id.chargingIconRightZoom, verticalSeekBarZoom, R.drawable.seekbar_zoom );

            AtomicReference<CameraControl> cameraControl = new AtomicReference<>( camera.getCameraControl( ) );
            cameraControl.get( ).setLinearZoom( linearScale / 100F );
            saveSettings( );
        } );

        buttonZoomDown.setOnClickListener( v -> {
            cameraInfo = camera.getCameraInfo( );
            float scale = Objects.requireNonNull( cameraInfo.getZoomState( ).getValue( ) ).getLinearZoom( );

            float linearScale = scale * 100;
            if ( linearScale > 0 )
            {
                linearScale = linearScale - 1;
            }

            verticalSeekBarZoom.setProgress( ( int ) linearScale );
            valueScreenZoom = ( int ) linearScale;
            setIcon( ( int ) linearScale, R.id.chargingIconLeftZoom, R.id.chargingIconRightZoom, verticalSeekBarZoom, R.drawable.seekbar_zoom );

            AtomicReference<CameraControl> cameraControl = new AtomicReference<>( camera.getCameraControl( ) );
            cameraControl.get( ).setLinearZoom( linearScale / 100F );
            saveSettings( );
        } );

        buttonDistanceUp.setOnClickListener( v -> {
            int progress = verticalSeekBarTargetDistance.getProgress( );

            if ( progress < 100 )
            {
                progress = progress + 1;
            }
            valueTargetDistance = progress;
            verticalSeekBarTargetDistance.setProgress( progress );
            setIcon( progress, R.id.chargingIconLeftDistance, R.id.chargingIconRightDistance, verticalSeekBarTargetDistance, R.drawable.seekbar_distance );
            generalProgress = progress;
            saveSettings( );
            shot( );
        } );

        buttonDistanceDown.setOnClickListener( v -> {
            int progress = verticalSeekBarTargetDistance.getProgress( );

            if ( progress > 0 )
            {
                progress = progress - 1;
            }

            valueTargetDistance = progress;
            verticalSeekBarTargetDistance.setProgress( progress );
            setIcon( progress, R.id.chargingIconLeftDistance, R.id.chargingIconRightDistance, verticalSeekBarTargetDistance, R.drawable.seekbar_distance );
            generalProgress = progress;
            saveSettings( );
            shot( );
        } );

        String fileName;
        SensorManager sensorManager;
        Sensor accelerometerSensor;
        Sensor rotationVectorSensor;
        sensorManager = ( SensorManager ) getSystemService( Context.SENSOR_SERVICE );
        accelerometerSensor = sensorManager.getDefaultSensor( Sensor.TYPE_ACCELEROMETER );
        rotationVectorSensor = sensorManager.getDefaultSensor( Sensor.TYPE_ROTATION_VECTOR );

        sensorManager.registerListener( this, accelerometerSensor, accelerometerSamplingPeriod );
        sensorManager.registerListener( this, rotationVectorSensor, rotationVectorSamplingPeriod );

        fileName = Calendar.getInstance( ).getTime( ) + ".txt";

        if ( Environment.MEDIA_MOUNTED.equals( Environment.getExternalStorageState( ) ) )
        {
            File fileOutputStream = new File( getExternalFilesDir( Environment.DIRECTORY_PICTURES ), fileName );
            try
            {
                printWriter = new PrintWriter( new FileOutputStream( fileOutputStream, true ) );
            }
            catch ( FileNotFoundException fileNotFoundException )
            {
                fileNotFoundException.printStackTrace( );
            }
        }

        //screenWidth = Tools.screenPixelWidth( this.getWindowManager( ) );
        screenHeight = Tools.screenPixelHeight( this.getWindowManager( ) );

        StrictMode.ThreadPolicy threadPolicy = new StrictMode.ThreadPolicy.Builder( ).permitAll( ).build( );
        StrictMode.setThreadPolicy( threadPolicy );

        // Set full-screen
        Window window = getWindow( );
        WindowManager.LayoutParams layoutParams = window.getAttributes( );
        layoutParams.flags |= WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS;

        pitchTakeOff = new Takeoff( this, pitchSurfaceView, pitchImageView );
        Thread thread = new Thread( pitchTakeOff );
        thread.start( );

    }

    public boolean onTouchEvent( MotionEvent motionEvent )
    {
        return true;
    }

    private void startCamera( )
    {
        final ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance( this );
        cameraProviderFuture.addListener( ( ) -> {
            try
            {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get( );
                bindPreview( cameraProvider );
            }
            catch ( ExecutionException | InterruptedException e )
            {
                Tools.log( "START_CAMERA EXCEPTION: [" + e + "]" );
            }
        }, ContextCompat.getMainExecutor( this ) );

        cameraFov = Tools.getFieldOfView( this, 1 );
    }

    @SuppressLint ( "UnsafeExperimentalUsageError" )
    void bindPreview( @NonNull ProcessCameraProvider cameraProvider )
    {
        Preview preview = new Preview.Builder( ).build( );
        cameraSelector = new CameraSelector.Builder( ).requireLensFacing( CameraSelector.LENS_FACING_BACK ).build( );
        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder( ).build( );
        ImageCapture.Builder builder = new ImageCapture.Builder( );

        HdrImageCaptureExtender hdrImageCaptureExtender = HdrImageCaptureExtender.create( builder );

        if ( hdrImageCaptureExtender.isExtensionAvailable( cameraSelector ) )
        {
            hdrImageCaptureExtender.enableExtension( cameraSelector );
        }

        final ImageCapture imageCapture = builder.setTargetRotation( this.getWindowManager( ).getDefaultDisplay( ).getRotation( ) ).build( );
        preview.setSurfaceProvider( cameraPreview.createSurfaceProvider( ) );
        camera = cameraProvider.bindToLifecycle( this, cameraSelector, preview, imageAnalysis, imageCapture );

        AtomicReference<CameraControl> cameraControl = new AtomicReference<>( camera.getCameraControl( ) );
        cameraControl.get( ).setLinearZoom( valueScreenZoom );
        verticalSeekBarZoom.setProgress( valueScreenZoom );

        setIcon( valueScreenZoom, R.id.chargingIconLeftZoom, R.id.chargingIconRightZoom, verticalSeekBarZoom, R.drawable.seekbar_zoom );

        verticalSeekBarTargetDistance.setProgress( Math.round( valueTargetDistance ) );
        generalProgress = valueTargetDistance;

        shot( );
    }

    private boolean allPermissionsGranted( )
    {
        for ( String permission : REQUIRED_PERMISSIONS )
        {
            if ( ContextCompat.checkSelfPermission( this, permission ) != PackageManager.PERMISSION_GRANTED )
            {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult( int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults )
    {
        if ( requestCode == REQUEST_CODE_PERMISSIONS )
        {
            if ( allPermissionsGranted( ) )
            {
                startCamera( );
            }
            else
            {
                Toast.makeText( this, "Permissions not granted by the user.", Toast.LENGTH_SHORT ).show( );
                this.finish( );
            }
        }
    }

    public List<Double> roll( List<Double> list, double newMember )
    {
        if ( list.size( ) == valueSensorMaxSample )
        {
            list.remove( 0 );
        }
        list.add( newMember );
        return list;
    }

    public double averageList( List<Double> tallyUp )
    {
        double total = 0;
        for ( Double item : tallyUp )
        {
            total += item;
        }
        total = total / tallyUp.size( );

        return total;
    }

    @Override
    public void onAccuracyChanged( Sensor sensor, int value )
    {
        Tools.log( "CHANGED: SENSOR[" + sensor + "] VALUE[" + value + "]" );
    }

    @Override
    public void onSensorChanged( SensorEvent event )
    {
        boolean method = true;
        if ( event.sensor.getType( ) == Sensor.TYPE_ACCELEROMETER )
        {
            double xCoordinate;
            double yCoordinate;
            double zCoordinate;
            if ( method )
            {
                double xAxis = event.values[0];
                double yAxis = event.values[1];
                double zAxis = event.values[2];
                xCoordinate = ( RAD_TO_DEG * atan( xAxis / sqrt( yAxis * yAxis + zAxis * zAxis ) ) );
                yCoordinate = ( RAD_TO_DEG * atan( yAxis / sqrt( xAxis * xAxis + zAxis * zAxis ) ) );
                zCoordinate = ( RAD_TO_DEG * atan( zAxis / sqrt( yAxis * yAxis + xAxis * xAxis ) ) );
                rollingAverage[0] = roll( rollingAverage[0], xCoordinate );
                rollingAverage[1] = roll( rollingAverage[1], yCoordinate );
                rollingAverage[2] = roll( rollingAverage[2], zCoordinate );
                xCoordinate = averageList( rollingAverage[0] );
                yCoordinate = averageList( rollingAverage[1] );
                zCoordinate = averageList( rollingAverage[2] );
            }
            else
            {
                float[] acceleration = event.values.clone( );
                double gravityNormalized = Math.sqrt( acceleration[0] * acceleration[0] + acceleration[1] * acceleration[1] + acceleration[2] * acceleration[2] );
                double[] accelerationNormalized = new double[3];
                accelerationNormalized[0] = acceleration[0] / gravityNormalized;
                accelerationNormalized[1] = acceleration[1] / gravityNormalized;
                accelerationNormalized[2] = acceleration[2] / gravityNormalized;
                double[] tiltDegrees = new double[3];
                tiltDegrees[0] = Math.toDegrees( Math.asin( accelerationNormalized[0] ) );
                tiltDegrees[1] = Math.toDegrees( Math.asin( accelerationNormalized[1] ) );
                tiltDegrees[2] = Math.toDegrees( Math.asin( accelerationNormalized[2] ) );
                xCoordinate = tiltDegrees[0];
                yCoordinate = tiltDegrees[1];
                zCoordinate = tiltDegrees[2];
            }
            relativeLayoutForRotation.setRotation( Math.round( xCoordinate ) );

            RelativeLayout.LayoutParams layoutParams = ( RelativeLayout.LayoutParams ) imageViewHorizonLine.getLayoutParams( );
            //inclination_angle = ( int ) Math.round( -zCoordinate );
            inclinationAngle = -zCoordinate;
            textViewForInclinationAngle.setText( String.format( getResources( ).getString( R.string.inclination ), -zCoordinate ) );

            if ( imageViewHeight == 0 )
            {
                imageViewHeight = Tools.customGetIntrinsicHeight( imageViewHorizonLine );
            }

            layoutParams.topMargin = ( int ) getHorizonLine( );
            imageViewHorizonLine.setLayoutParams( layoutParams );

            shot( );

            if ( xCoordinate > 10 || xCoordinate < -10 || yCoordinate > 7 || yCoordinate < -7 )
            {
                try
                {
                    writeFile( printWriter, xCoordinate + "", yCoordinate + "", zCoordinate + "" );
                }
                catch ( IOException ioException )
                {
                    ioException.printStackTrace( );
                }
            }

            double[] android = new double[3];
            android[0] = -event.values[2];
            android[1] = event.values[0];
            android[2] = -event.values[1];

            Tools.estimatePitch( android );
        }
        else
        {
            if ( event.sensor.getType( ) == Sensor.TYPE_ROTATION_VECTOR )
            {
                double q1 = event.values[0];
                double q2 = event.values[1];
                double q3 = event.values[2];
                double q0 = Math.sqrt( 1 - q1 * q1 - q2 * q2 - q3 * q3 ); // Its a unit quaternion

                // Formulas are obtained from https://en.wikipedia.org/wiki/Conversion_between_quaternions_and_Euler_angles
                double pitch = Math.toDegrees( Math.atan2( 2F * ( q0 * q1 + q2 * q3 ), 1F - 2F * ( q1 * q1 + q2 * q2 ) ) ) - constantRightDegrees;
                if ( !Double.isNaN( pitch ) )
                {
                    headPitch = headPitch + ( pitch - headPitch ) * FILTER_COEFFICIENT;
                }
            }
        }

    }

    private void writeFile( PrintWriter pw, String x, String y, String z ) throws IOException
    {
        pw.println( "X = " + x + " Y = " + y + " Z = " + z );
    }

    public void onBackPressed( )
    {
        backButtonPress = backButtonPress + 1;
        printWriter.close( );
        Toast.makeText( MainActivity.this, "Press again to exit", Toast.LENGTH_SHORT ).show( );
        if ( backButtonPress > 1 )
        {
            super.onBackPressed( );
        }
    }

    public void shot( )
    {
        double startingTime = 0;
        double startingArrowX0 = 0;

        double friction = constantDensityOfAirKgM3 * pow( ( valueArrowDiameter / 2 ), 2 );

        double startingArrowYRed = valuePersonHeight;
        double startingArrowYGreen = valuePersonHeight;
        double startingArrowYGray = valuePersonHeight;

        double startingVelocityX0 = valueArrowVelocity * cos( toRadians( inclinationAngle ) );

        double startingVelocitySin = valueArrowVelocity * sin( toRadians( inclinationAngle ) );
        double startingVelocityYRed = startingVelocitySin;
        double startingVelocityYGreen = startingVelocitySin;
        double startingVelocityYGray = startingVelocitySin;

        double crossSectionalArea = PI * pow( ( valueArrowDiameter / 2 ), 2 );

        double pyRed = 0;
        double pyGreen = 0;
        double pyGray = 0;

        double totalForce;
        double acceleration;
        double accelerationX;
        double accelerationY;

        while ( startingArrowX0 <= generalProgress )
        {
            pyRed = startingArrowYRed;
            pyGreen = startingArrowYGreen;
            pyGray = startingArrowYGray;

            totalForce = -valueArrowMass * constantGravitationalField + 0.5 * constantDensityOfAirKgM3 * crossSectionalArea * constantDragCoefficient * pow( startingVelocityYGreen, 2 );
            acceleration = totalForce / valueArrowMass;

            if ( valueAccelerationFormulas )
            {
                accelerationX = acceleration;
                accelerationY = acceleration - constantGravitationalField;
            }
            else
            {
                accelerationX = -friction * startingVelocityX0 / valueArrowMass;
                accelerationY = -friction * startingVelocityYRed / valueArrowMass - constantGravitationalField;
            }

            startingArrowX0 = startingArrowX0 + startingVelocityX0 * valueTimeStep;
            startingArrowYRed = startingArrowYRed + startingVelocityYRed * valueTimeStep;
            startingArrowYGreen = startingArrowYGreen + startingVelocityYGreen * valueTimeStep;
            startingArrowYGray = startingArrowYGray + startingVelocityYGray * valueTimeStep;

            if ( valueGravityFormulas )
            {
                startingVelocityX0 = startingVelocityX0 + accelerationX * valueTimeStep;
            }
            else
            {
                startingVelocityX0 = startingVelocityX0 - constantGravitationalField * valueTimeStep;
            }

            startingVelocityYRed = startingVelocityYRed + accelerationY * valueTimeStep;
            startingVelocityYGray = startingVelocityYGray - constantGravitationalField * valueTimeStep;
            startingVelocityYGreen = startingVelocityYGreen + acceleration * valueTimeStep;

            startingTime = startingTime + valueTimeStep;
        }

        redAim = pyRed;
        greenAim = pyGreen;
        blueAim = pyGray;

        // singleFormula = (1/2) * (9.8 m/s²) * (30 m / 107 m/s)² = 0.38 m
        // singleFormula = ( 1 / 2 ) *  gravitationalConstant  * Math.pow( targetDistanceValue / arrowVelocitySettings, 2 );
    }

    private double getHorizonLine( )
    {
        // h = tan( ( inclination_angle ) * PI / 180 ) * 0.395
        // px = convert_from_cm_to_px( h ) * screenHeightInCm / ( tan( 60 * PI / 180 ) * 0.395 )
        return -Math.sin( Math.toRadians( constantRightDegrees ) ) * ( ( -headPitch ) / ( overallValueGaugeDisplayOnFrame / ( overallValueGaugeDisplayOnFrame * ( parameter0 / ( parameter2 * parameter3 / parameter4 ) ) ) ) );
    }

    @Override
    public boolean dispatchKeyEvent( KeyEvent event )
    {
        int action = event.getAction( );
        int keyCode = event.getKeyCode( );
        switch ( keyCode )
        {
            case KeyEvent.KEYCODE_VOLUME_UP:
                if ( action == KeyEvent.ACTION_DOWN )
                {
                    // Ensure the index is bound within the upper limit
                    if ( indCoefficient < coefficientArray.length - 1 )
                    {
                        indCoefficient = indCoefficient + 1;
                        FILTER_COEFFICIENT = coefficientArray[indCoefficient];
                    }
                }
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                if ( action == KeyEvent.ACTION_DOWN )
                {
                    // Ensure the index is bound within the lower limit
                    if ( indCoefficient > 0 )
                    {
                        indCoefficient = indCoefficient - 1;
                        FILTER_COEFFICIENT = coefficientArray[indCoefficient];
                    }
                }
                return true;
            default:
                return super.dispatchKeyEvent( event );
        }
    }

    @Override
    public void onPause( )
    {
        super.onPause( );
        pitchTakeOff.pause( );
    }

    @Override
    public void onResume( )
    {
        super.onResume( );
        pitchTakeOff.resume( );
    }
}
