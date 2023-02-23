package org.otdshco;

// TODO: BUGFIX - CHECK WHY TRAJECTORY IS ABOVE RETICLE CENTER AT NEGATIVE INCLINATION ANGLE
// TODO: BUGFIX - FIX HORIZON LINE WHILE ZOOMING

// TODO: USE GRAVITY FORMULAS AND ACCELERATION FORMULAS AT THE SAME TIME WITH DIFFERENT CROSSHAIR COLORS (MAGENTA, CYAN, LIME)
// TODO: PARAMETERS OF HARDWARE AND SOFTWARE - CHANGE HARDCODED CONSTANTS TO HAVE EITHER AUTOMATIC DETECTION OR EVEN MANUAL INPUT VIA SETTINGS (LIKE timeStep)

// TODO: DOUBLE CHECK UNITS (SOMETIMES CM, SOMETIMES METERS) AND PUT STANDARDS TO CM INSTEAD OF HANDLING METERS

// TODO: OPTIMIZATION - TRY TO REDUCE THE AMOUNT OF LOOPS AND AVOID PHONE HEATING
// TODO: OPTIMIZATION - GO STRAIGHT TO THE LAST CALCULATION RESULT WITHOUT LOOPING

import org.otdshco.gauges.Params;

import static java.lang.StrictMath.sin;
import static java.lang.StrictMath.cos;
import static java.lang.StrictMath.pow;
import static java.lang.StrictMath.atan;
import static java.lang.StrictMath.asin;
import static java.lang.StrictMath.sqrt;
import static java.lang.StrictMath.atan2;
import static java.lang.StrictMath.round;
import static java.lang.StrictMath.toDegrees;
import static java.lang.StrictMath.toRadians;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

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
    private final List<Double>[] rollingAverage = new List[3];

    private Camera camera;
    private CameraSelector cameraSelector;

    private Takeoff pitchTakeOff = null;

    private TextView textViewForInclinationAngle;

    private ImageView imageViewHorizonLine;

    private PrintWriter printWriter;
    private PreviewView cameraPreview;
    private RelativeLayout relativeLayoutForRotation;

    // verticalSeekBar setThumbPlaceholderDrawable ( getResources( ).getDrawable( R.drawable.seekbar_distance, this.getTheme( ) ) )

    private VerticalSeekBar verticalSeekBarZoom;
    private VerticalSeekBar verticalSeekBarTargetDistance;

    private int backButtonPress = 0;
    private int imageViewHeight = 0;

    private double valueArrowVelocity = Params.defaultArrowVelocity;
    private double valueArrowDiameter = Params.defaultArrowDiameter;
    private double valueArrowMass = Params.defaultArrowMass;
    private boolean valueAccelerationFormulas = Params.defaultAccelerationFormulas;
    private boolean valueGravityFormulas = Params.defaultGravityFormulas;
    private boolean valueSensorMethod = Params.defaultSensorMethod;

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
        SharedPreferences sharedPreferences = getSharedPreferences( Params.settingsEnvironmentValues, Context.MODE_PRIVATE );
        SharedPreferences.Editor editor = sharedPreferences.edit( );
        editor.putInt( Params.settingsSeekZoom, Params.valueScreenZoom );
        editor.putFloat( Params.settingsSeekDistance, Params.valueTargetDistance );
        editor.apply( );
    }

    private void loadSettings( )
    {
        SharedPreferences sharedPreferences = getSharedPreferences( Params.settingsEnvironmentValues, Context.MODE_PRIVATE );

        valueArrowMass = sharedPreferences.getFloat( Params.settingsArrowMass, Params.defaultArrowMass );
        valueArrowVelocity = sharedPreferences.getFloat( Params.settingsArrowVelocity, Params.defaultArrowVelocity );
        valueArrowDiameter = sharedPreferences.getFloat( Params.settingsArrowDiameter, Params.defaultArrowDiameter );

        Params.valueTargetDistance = sharedPreferences.getFloat( Params.settingsSeekDistance, Params.defaultSeekDistance );
        Params.valuePersonHeight = sharedPreferences.getFloat( Params.settingsPersonHeight, Params.defaultPersonHeight );
        Params.valueLensFactor = sharedPreferences.getFloat( Params.settingsLensFactor, Params.defaultLensFactor );

        Params.valueScreenZoom = sharedPreferences.getInt( Params.settingsSeekZoom, Params.defaultSeekZoom );
        Params.valueSensorMaxSample = sharedPreferences.getInt( Params.settingsSensorMaxSamples, Params.defaultSensorMaxSamples );

        valueAccelerationFormulas = sharedPreferences.getBoolean( Params.settingsAccelerationFormulas, Params.defaultAccelerationFormulas );
        valueGravityFormulas = sharedPreferences.getBoolean( Params.settingsGravityFormulas, Params.defaultGravityFormulas );
        valueSensorMethod = sharedPreferences.getBoolean( Params.settingsSensorMethod, Params.defaultSensorMethod );
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
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN;
        View view = getWindow( ).getDecorView( );
        view.setSystemUiVisibility( uiOptions );

        setContentView( R.layout.activity_main );

        Button button = findViewById( R.id.settings );
        button.setOnClickListener( v -> startActivity( new Intent( MainActivity.this, Settings.class ) ) );

        ImageView pitchImageView = findViewById( R.id.im_pitch );
        SurfaceView pitchSurfaceView = findViewById( R.id.sv_pitch );
        cameraPreview = findViewById( R.id.camera_preview );
        imageViewHorizonLine = findViewById( R.id.aim_level_image );
        relativeLayoutForRotation = findViewById( R.id.rotate_layout );
        textViewForInclinationAngle = findViewById( R.id.inclination_text );

        rollingAverage[0] = new ArrayList<>( );
        rollingAverage[1] = new ArrayList<>( );
        rollingAverage[2] = new ArrayList<>( );

        loadSettings( );

        if ( Tools.allPermissionsGranted( this ) )
        {
            startCamera( );
        }
        else
        {
            ActivityCompat.requestPermissions( this, Tools.REQUIRED_PERMISSIONS, Tools.REQUEST_CODE_PERMISSIONS );
            ActivityCompat.requestPermissions( this, Tools.REQUIRED_PERMISSIONS, Tools.REQUEST_CODE_PERMISSIONS );
        }

        RelativeLayout relative_main_layout = findViewById( R.id.main_layout );
        relative_main_layout.setOnTouchListener( ( v, event ) -> {
            if ( event.getAction( ) != MotionEvent.ACTION_UP )
            {
                return true;
            }
            MeteringPointFactory meteringPointFactory = cameraPreview.createMeteringPointFactory( cameraSelector );
            FocusMeteringAction action = new FocusMeteringAction.Builder( meteringPointFactory.createPoint( event.getX( ), event.getY( ) ) ).build( );
            AtomicReference<CameraControl> cameraControl = new AtomicReference<>( camera.getCameraControl( ) );
            cameraControl.get( ).cancelFocusAndMetering( );
            cameraControl.get( ).startFocusAndMetering( action );
            return true;
        } );

        verticalSeekBarZoom = findViewById( R.id.zoomSeekBar );
        verticalSeekBarZoom.setOnTouchListener( ( v, event ) -> {
            if ( event.getAction( ) != MotionEvent.ACTION_UP )
            {
                return true;
            }
            return true;
        } );
        verticalSeekBarZoom.setOnReleaseListener( ( progress ) -> null );
        verticalSeekBarZoom.setOnPressListener( ( progress ) -> null );
        verticalSeekBarZoom.setOnProgressChangeListener( ( progress ) -> {
            Params.valueScreenZoom = progress;
            setIcon( progress, R.id.chargingIconLeftZoom, R.id.chargingIconRightZoom, verticalSeekBarZoom, R.drawable.seekbar_zoom );
            AtomicReference<CameraControl> cameraControl = new AtomicReference<>( camera.getCameraControl( ) );
            cameraControl.get( ).setLinearZoom( progress.floatValue( ) / 100F );
            saveSettings( );
            return null;
        } );
        Button buttonZoomUp = findViewById( R.id.bt_zoom_up );
        Button buttonZoomDown = findViewById( R.id.bt_zoom_down );
        buttonZoomUp.setOnClickListener( v -> changeZoom( false ) );
        buttonZoomDown.setOnClickListener( v -> changeZoom( true ) );

        verticalSeekBarTargetDistance = findViewById( R.id.distanceSeekBar );
        verticalSeekBarTargetDistance.setOnTouchListener( ( v, event ) -> {
            if ( event.getAction( ) != MotionEvent.ACTION_UP )
            {
                return true;
            }
            return true;
        } );
        verticalSeekBarTargetDistance.setOnReleaseListener( ( progress ) -> null );
        verticalSeekBarTargetDistance.setOnPressListener( ( progress ) -> null );
        verticalSeekBarTargetDistance.setOnProgressChangeListener( ( progress ) -> {
            Params.valueTargetDistance = progress;
            setIcon( progress, R.id.chargingIconLeftDistance, R.id.chargingIconRightDistance, verticalSeekBarTargetDistance, R.drawable.seekbar_distance );
            saveSettings( );
            shot( );
            return null;
        } );
        Button buttonDistanceUp = findViewById( R.id.bt_dist_up );
        Button buttonDistanceDown = findViewById( R.id.bt_dist_down );
        buttonDistanceUp.setOnClickListener( v -> changeDistance( false ) );
        buttonDistanceDown.setOnClickListener( v -> changeDistance( true ) );

        SensorManager sensorManager = ( SensorManager ) getSystemService( Context.SENSOR_SERVICE );

        Sensor accelerometerSensor = sensorManager.getDefaultSensor( Sensor.TYPE_ACCELEROMETER );
        sensorManager.registerListener( this, accelerometerSensor, Params.accelerometerSamplingPeriod );

        Sensor rotationVectorSensor = sensorManager.getDefaultSensor( Sensor.TYPE_ROTATION_VECTOR );
        sensorManager.registerListener( this, rotationVectorSensor, Params.rotationVectorSamplingPeriod );

        if ( Environment.MEDIA_MOUNTED.equals( Environment.getExternalStorageState( ) ) )
        {
            File fileOutputStream = new File( getExternalFilesDir( Environment.DIRECTORY_PICTURES ), Calendar.getInstance( ).getTime( ) + ".txt" );
            try
            {
                printWriter = new PrintWriter( new FileOutputStream( fileOutputStream, true ) );
            }
            catch ( FileNotFoundException fileNotFoundException )
            {
                fileNotFoundException.printStackTrace( );
            }
        }

        // Params_screenHeight = Tools_screenPixelHeight( this_getWindowManager( ) )
        // Params_halfScreenHeight = Params_screenHeight / 2F

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

    private void changeZoom( boolean down )
    {
        CameraInfo cameraInfo = camera.getCameraInfo( );
        float linearScale = Objects.requireNonNull( cameraInfo.getZoomState( ).getValue( ) ).getLinearZoom( ) * 100;
        if ( down )
        {
            if ( linearScale > 0 )
            {
                linearScale = linearScale - 1;
            }
        }
        else
        {
            if ( linearScale < 100 )
            {
                linearScale = linearScale + 1;
            }
        }
        Params.valueScreenZoom = ( int ) linearScale;
        verticalSeekBarZoom.setProgress( ( int ) linearScale );
        setIcon( ( int ) linearScale, R.id.chargingIconLeftZoom, R.id.chargingIconRightZoom, verticalSeekBarZoom, R.drawable.seekbar_zoom );
        AtomicReference<CameraControl> cameraControl = new AtomicReference<>( camera.getCameraControl( ) );
        cameraControl.get( ).setLinearZoom( linearScale / 100F );
        saveSettings( );
    }

    private void changeDistance( boolean down )
    {
        int progress = verticalSeekBarTargetDistance.getProgress( );
        if ( down )
        {
            if ( progress > 0 )
            {
                progress = progress - 1;
            }
        }
        else
        {
            if ( progress < 100 )
            {
                progress = progress + 1;
            }
        }
        Params.valueTargetDistance = progress;
        verticalSeekBarTargetDistance.setProgress( progress );
        setIcon( progress, R.id.chargingIconLeftDistance, R.id.chargingIconRightDistance, verticalSeekBarTargetDistance, R.drawable.seekbar_distance );
        saveSettings( );
        shot( );
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
        cameraControl.get( ).setLinearZoom( Params.valueScreenZoom );
        verticalSeekBarZoom.setProgress( Params.valueScreenZoom );

        setIcon( Params.valueScreenZoom, R.id.chargingIconLeftZoom, R.id.chargingIconRightZoom, verticalSeekBarZoom, R.drawable.seekbar_zoom );

        verticalSeekBarTargetDistance.setProgress( round( Params.valueTargetDistance ) );

        shot( );
    }

    @Override
    public void onRequestPermissionsResult( int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults )
    {
        if ( requestCode == Tools.REQUEST_CODE_PERMISSIONS )
        {
            if ( Tools.allPermissionsGranted( this ) )
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
        if ( list.size( ) == Params.valueSensorMaxSample )
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
        if ( event.sensor.getType( ) == Sensor.TYPE_ACCELEROMETER )
        {
            double xCoordinate;
            double yCoordinate;
            double zCoordinate;
            if ( valueSensorMethod )
            {
                double xAxis = event.values[0];
                double yAxis = event.values[1];
                double zAxis = event.values[2];
                xCoordinate = ( Params.RAD_TO_DEG * atan( xAxis / sqrt( yAxis * yAxis + zAxis * zAxis ) ) );
                yCoordinate = ( Params.RAD_TO_DEG * atan( yAxis / sqrt( xAxis * xAxis + zAxis * zAxis ) ) );
                zCoordinate = ( Params.RAD_TO_DEG * atan( zAxis / sqrt( yAxis * yAxis + xAxis * xAxis ) ) );
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
                double gravityNormalized = sqrt( acceleration[0] * acceleration[0] + acceleration[1] * acceleration[1] + acceleration[2] * acceleration[2] );
                double[] accelerationNormalized = new double[3];
                accelerationNormalized[0] = acceleration[0] / gravityNormalized;
                accelerationNormalized[1] = acceleration[1] / gravityNormalized;
                accelerationNormalized[2] = acceleration[2] / gravityNormalized;
                double[] tiltDegrees = new double[3];
                tiltDegrees[0] = toDegrees( asin( accelerationNormalized[0] ) );
                tiltDegrees[1] = toDegrees( asin( accelerationNormalized[1] ) );
                tiltDegrees[2] = toDegrees( asin( accelerationNormalized[2] ) );
                xCoordinate = tiltDegrees[0];
                yCoordinate = tiltDegrees[1];
                zCoordinate = tiltDegrees[2];
            }
            relativeLayoutForRotation.setRotation( round( xCoordinate ) );

            RelativeLayout.LayoutParams layoutParams = ( RelativeLayout.LayoutParams ) imageViewHorizonLine.getLayoutParams( );
            //inclination_angle = ( int ) round( -zCoordinate );
            Params.inclinationAngle = -zCoordinate;
            textViewForInclinationAngle.setText( String.format( getResources( ).getString( R.string.inclination ), -zCoordinate ) );

            if ( imageViewHeight == 0 )
            {
                imageViewHeight = Tools.customGetIntrinsicHeight( imageViewHorizonLine );
            }

            layoutParams.topMargin = ( int ) Tools.getHorizonLine( );
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
                double q0 = sqrt( 1 - q1 * q1 - q2 * q2 - q3 * q3 ); // Its a unit quaternion

                // Formulas are obtained from https://en.wikipedia.org/wiki/Conversion_between_quaternions_and_Euler_angles
                double pitch = toDegrees( atan2( 2F * ( q0 * q1 + q2 * q3 ), 1F - 2F * ( q1 * q1 + q2 * q2 ) ) ) - Params.constantRightDegrees;
                if ( !Double.isNaN( pitch ) )
                {
                    Params.headPitch = Params.headPitch + ( pitch - Params.headPitch ) * Params.FILTER_COEFFICIENT;
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

        double friction = Params.constantDensityOfAirKgM3 * pow( ( valueArrowDiameter / 2 ), 2 );

        double startingArrowYRed = Params.valuePersonHeight;
        double startingArrowYGreen = Params.valuePersonHeight;
        double startingArrowYGray = Params.valuePersonHeight;

        double startingVelocityX0 = valueArrowVelocity * cos( toRadians( Params.inclinationAngle ) );

        double startingVelocitySin = valueArrowVelocity * sin( toRadians( Params.inclinationAngle ) );
        double startingVelocityYRed = startingVelocitySin;
        double startingVelocityYGreen = startingVelocitySin;
        double startingVelocityYGray = startingVelocitySin;

        double crossSectionalArea = Math.PI * pow( ( valueArrowDiameter / 2 ), 2 );

        double pyRed = 0;
        double pyGreen = 0;
        double pyGray = 0;

        double totalForce;
        double acceleration;
        double accelerationX;
        double accelerationY;

        while ( startingArrowX0 <= Params.valueTargetDistance )
        {
            pyRed = startingArrowYRed;
            pyGreen = startingArrowYGreen;
            pyGray = startingArrowYGray;

            totalForce = -valueArrowMass * Params.constantGravitationalField + 0.5 * Params.constantDensityOfAirKgM3 * crossSectionalArea * Params.constantDragCoefficient * pow( startingVelocityYGreen, 2 );
            acceleration = totalForce / valueArrowMass;

            if ( valueAccelerationFormulas )
            {
                accelerationX = acceleration;
                accelerationY = acceleration - Params.constantGravitationalField;
            }
            else
            {
                accelerationX = -friction * startingVelocityX0 / valueArrowMass;
                accelerationY = -friction * startingVelocityYRed / valueArrowMass - Params.constantGravitationalField;
            }

            startingArrowX0 = startingArrowX0 + startingVelocityX0 * Params.valueTimeStep;
            startingArrowYRed = startingArrowYRed + startingVelocityYRed * Params.valueTimeStep;
            startingArrowYGreen = startingArrowYGreen + startingVelocityYGreen * Params.valueTimeStep;
            startingArrowYGray = startingArrowYGray + startingVelocityYGray * Params.valueTimeStep;

            if ( valueGravityFormulas )
            {
                startingVelocityX0 = startingVelocityX0 + accelerationX * Params.valueTimeStep;
            }
            else
            {
                startingVelocityX0 = startingVelocityX0 - Params.constantGravitationalField * Params.valueTimeStep;
            }

            startingVelocityYRed = startingVelocityYRed + accelerationY * Params.valueTimeStep;
            startingVelocityYGray = startingVelocityYGray - Params.constantGravitationalField * Params.valueTimeStep;
            startingVelocityYGreen = startingVelocityYGreen + acceleration * Params.valueTimeStep;

            startingTime = startingTime + Params.valueTimeStep;
        }

        Params.redAim = pyRed;
        Params.greenAim = pyGreen;
        Params.blueAim = pyGray;
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
                    if ( Params.indCoefficient < Params.coefficientArray.length - 1 )
                    {
                        Params.indCoefficient = Params.indCoefficient + 1;
                        Params.FILTER_COEFFICIENT = Params.coefficientArray[Params.indCoefficient];
                    }
                }
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                if ( action == KeyEvent.ACTION_DOWN )
                {
                    // Ensure the index is bound within the lower limit
                    if ( Params.indCoefficient > 0 )
                    {
                        Params.indCoefficient = Params.indCoefficient - 1;
                        Params.FILTER_COEFFICIENT = Params.coefficientArray[Params.indCoefficient];
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
