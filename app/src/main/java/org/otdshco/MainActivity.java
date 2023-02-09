package org.otdshco;

// TODO: BUGFIX - CHECK WHY TRAJECTORY IS ABOVE RETICLE CENTER AT NEGATIVE INCLINATION ANGLE
// TODO: FEATURE - ZOOM CHANGES THE SCREEN DIMENSIONS, RETICLE MUST UPDATE WIDTH/HEIGHT FROM THE REAL TO THE PROPORTIONAL FOR EACH ZOOM LEVEL

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
import android.util.Log;
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
import org.otdshco.gauges.Params;

//  d = (1/2) * (9.8 m/s²) * (30 m / 107 m/s)² = 0.38 m

public class MainActivity extends AppCompatActivity implements SensorEventListener
{
    private final List<Double>[] rollingAverage = new List[3];

    private final int REQUEST_CODE_PERMISSIONS = 1001;
    private final String[] REQUIRED_PERMISSIONS = new String[] { "android.permission.CAMERA", "android.permission.WRITE_EXTERNAL_STORAGE" };

    PreviewView cameraPreview;

    private Camera camera;
    private CameraInfo cameraInfo;
    private CameraSelector cameraSelector;

    private int screenZoomValue = 0;
    private VerticalSeekBar verticalSeekBarZoom;

    private double generalProgress = 0;
    public static volatile float targetDistanceValue = 10;
    private VerticalSeekBar verticalSeekBarTargetDistance;

    private RelativeLayout relativeLayoutForRotation;

    private ImageView imageViewHorizonLine;

    private TextView textViewForInclinationAngle;

    public static volatile double inclinationAngle = 0;

    private int backButtonPress = 0;

    private PrintWriter printWriter;

    private int MAX_SAMPLE_SIZE = 5;
    private double arrowVelocitySettings = 48;

    public static volatile double mtSettingsEyeHeight = 1.6;

    private int imageViewHeight = 0;

    private double settingsArrowDiameter = 0.0065;
    private double settingsArrowMass = 0.009;
    private boolean settingsAccelerationFormulas = true;
    private boolean settingsGravityFormulas = true;
    private int lensFactorSettings = 100;

    public static volatile Params hudParams = new Params( );
    public static volatile double headPitch = 0;
    public static volatile double redAim;
    public static volatile double greenAim;
    public static volatile double blueAim;
    public static volatile int sWidth = 600;
    public static volatile int sHeight = 1280;

    // Based on a measured estimate of the pitch attitude of the FRL of 5 deg and 4.32 deg of Bosch
    public static final double headPitchBias = 0; // degrees

    // public static final double headPitchBias = 2.16; // degrees
    private final double[] coeffArray = new double[] { 0.003, 0.01, 0.05 };

    private int indCoeff = 1;
    public static volatile double FILTER_COEFFICIENT = 0.1;

    private Takeoff pitchTakeOff = null;
    private SurfaceView pitchSurfaceView;
    private ImageView pitchImageView;

    private int getPos( int value )
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
        editor.putInt( "seek_zoom", screenZoomValue );
        editor.putFloat( "seek_distance", targetDistanceValue );
        editor.apply( );
    }

    private void loadSettings( )
    {
        SharedPreferences sharedPreferences = getSharedPreferences( "environment_settings", MODE_PRIVATE );
        MAX_SAMPLE_SIZE = sharedPreferences.getInt( "sensor_max_samples", 5 );
        arrowVelocitySettings = sharedPreferences.getFloat( "arrow_velocity", 48 );
        mtSettingsEyeHeight = sharedPreferences.getFloat( "person_height", 1.6F );
        settingsArrowDiameter = sharedPreferences.getFloat( "arrow_diameter", 0.0065F );
        settingsArrowMass = sharedPreferences.getFloat( "arrow_mass", 0.009F );
        settingsAccelerationFormulas = sharedPreferences.getBoolean( "acceleration_formulas", true );
        settingsGravityFormulas = sharedPreferences.getBoolean( "gravity_formulas", true );
        lensFactorSettings = sharedPreferences.getInt( "lens_factor", 100 );
        screenZoomValue = sharedPreferences.getInt( "seek_zoom", 100 );
        targetDistanceValue = sharedPreferences.getFloat( "seek_distance", 0 );
    }

    private void setIco( int progress, int tens, int ones, VerticalSeekBar verticalSeekBar, int drawablePlaceHolder )
    {
        int cen = ( progress % 1000 ) / 100;
        int dez = ( progress % 100 ) / 10;
        int uni = ( progress % 100 ) % 10;

        @SuppressLint ( "UseCompatLoadingForDrawables" ) LayerDrawable layerDrawable = ( LayerDrawable ) getResources( ).getDrawable( drawablePlaceHolder, this.getTheme( ) );

        int pos0;
        if ( cen == 1 )
        {
            pos0 = getPos( 1 );
        }
        else
        {
            pos0 = getPos( dez );
        }

        @SuppressLint ( "UseCompatLoadingForDrawables" ) Drawable drawable = getResources( ).getDrawable( pos0, this.getTheme( ) );
        layerDrawable.setDrawableByLayerId( tens, drawable );

        int pos1;
        if ( cen == 1 )
        {
            pos1 = getPos( 99 );
        }
        else
        {
            pos1 = getPos( uni );
        }

        @SuppressLint ( "UseCompatLoadingForDrawables" ) Drawable drawable1 = getResources( ).getDrawable( pos1, this.getTheme( ) );
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
            screenZoomValue = progress;
            setIco( progress, R.id.chargingIconLeftZoom, R.id.chargingIconRightZoom, verticalSeekBarZoom, R.drawable.seekbar_zoom );
            float lz = progress.floatValue( ) / 100F;
            AtomicReference<CameraControl> cameraControl = new AtomicReference<>( camera.getCameraControl( ) );
            cameraControl.get( ).setLinearZoom( lz );
            saveSettings( );
            return null;
        } );

        verticalSeekBarTargetDistance.setOnProgressChangeListener( ( progress ) -> {
            targetDistanceValue = progress;
            setIco( progress, R.id.chargingIconLeftDistance, R.id.chargingIconRightDistance, verticalSeekBarTargetDistance, R.drawable.seekbar_distance );
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
            screenZoomValue = ( int ) linearScale;
            setIco( ( int ) linearScale, R.id.chargingIconLeftZoom, R.id.chargingIconRightZoom, verticalSeekBarZoom, R.drawable.seekbar_zoom );

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
            screenZoomValue = ( int ) linearScale;
            setIco( ( int ) linearScale, R.id.chargingIconLeftZoom, R.id.chargingIconRightZoom, verticalSeekBarZoom, R.drawable.seekbar_zoom );

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
            targetDistanceValue = progress;
            verticalSeekBarTargetDistance.setProgress( progress );
            setIco( progress, R.id.chargingIconLeftDistance, R.id.chargingIconRightDistance, verticalSeekBarTargetDistance, R.drawable.seekbar_distance );
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

            targetDistanceValue = progress;
            verticalSeekBarTargetDistance.setProgress( progress );
            setIco( progress, R.id.chargingIconLeftDistance, R.id.chargingIconRightDistance, verticalSeekBarTargetDistance, R.drawable.seekbar_distance );
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

        sensorManager.registerListener( this, accelerometerSensor, 600000 ); // 500000
        sensorManager.registerListener( this, rotationVectorSensor, 30000 ); // 500000

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
                Log.e( "START_CAMERA", "EXCEPTION: " + e );
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
        cameraControl.get( ).setLinearZoom( screenZoomValue );
        verticalSeekBarZoom.setProgress( screenZoomValue );

        setIco( screenZoomValue, R.id.chargingIconLeftZoom, R.id.chargingIconRightZoom, verticalSeekBarZoom, R.drawable.seekbar_zoom );

        verticalSeekBarTargetDistance.setProgress( Math.round( targetDistanceValue ) );
        generalProgress = targetDistanceValue;
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
        if ( list.size( ) == MAX_SAMPLE_SIZE )
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
    public void onAccuracyChanged( Sensor arg0, int arg1 )
    {
        Log.e( "onAccuracyChanged", "CHANGED: " + arg0 );
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
                double RAD_TO_DEG = 57.295779513082320876798154814105;
                double ax = event.values[0];
                double ay = event.values[1];
                double az = event.values[2];
                xCoordinate = ( RAD_TO_DEG * atan( ax / sqrt( ay * ay + az * az ) ) );
                yCoordinate = ( RAD_TO_DEG * atan( ay / sqrt( ax * ax + az * az ) ) );
                zCoordinate = ( RAD_TO_DEG * atan( az / sqrt( ay * ay + ax * ax ) ) );
                rollingAverage[0] = roll( rollingAverage[0], xCoordinate );
                rollingAverage[1] = roll( rollingAverage[1], yCoordinate );
                rollingAverage[2] = roll( rollingAverage[2], zCoordinate );
                xCoordinate = averageList( rollingAverage[0] );
                yCoordinate = averageList( rollingAverage[1] );
                zCoordinate = averageList( rollingAverage[2] );
            }
            else
            {
                float[] acc = event.values.clone( );
                double gravityNormalized = Math.sqrt( acc[0] * acc[0] + acc[1] * acc[1] + acc[2] * acc[2] );
                double[] accNormalized = new double[3];
                accNormalized[0] = acc[0] / gravityNormalized;
                accNormalized[1] = acc[1] / gravityNormalized;
                accNormalized[2] = acc[2] / gravityNormalized;
                double[] tiltDegrees = new double[3];
                tiltDegrees[0] = Math.toDegrees( Math.asin( accNormalized[0] ) );
                tiltDegrees[1] = Math.toDegrees( Math.asin( accNormalized[1] ) );
                tiltDegrees[2] = Math.toDegrees( Math.asin( accNormalized[2] ) );
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

            estimatePitch( android );
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
                double pitch = Math.toDegrees( Math.atan2( 2f * ( q0 * q1 + q2 * q3 ), 1f - 2f * ( q1 * q1 + q2 * q2 ) ) ) - 90;
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
        double localProgress = generalProgress;
        double inclinationAngleLevel = inclinationAngle;
        double startingVelocity = arrowVelocitySettings;
        double personHeight = mtSettingsEyeHeight;
        double diameterOfArrowMeters = settingsArrowDiameter;
        double massOfArrowKilos = settingsArrowMass;

        double startingArrowX0 = 0;
        double startingArrowYRed = personHeight;
        double radiusOfArrow = diameterOfArrowMeters / 2;

        double startingTime = 0;
        double gravitationalConstant = 9.81;
        double timeStep = 0.005;
        double densityOfAirKgM3 = 1.2;

        double friction = densityOfAirKgM3 * pow( radiusOfArrow, 2 );

        double startingArrowYGreen = startingArrowYRed;
        double startingArrowYGray = startingArrowYRed;

        double startingVelocityX0 = startingVelocity * cos( toRadians( inclinationAngleLevel ) );
        double startingVelocitySin = startingVelocity * sin( toRadians( inclinationAngleLevel ) );
        double startingVelocityYRed = startingVelocitySin;
        double startingVelocityYGreen = startingVelocitySin;
        double startingVelocityYGray = startingVelocitySin;

        double dragCoefficient = 9 * 0.3;
        double crossSectionalArea = PI * pow( ( diameterOfArrowMeters / 2 ), 2 );

        double pyRed = 0;
        double pyGreen = 0;
        double pyGray = 0;

        while ( startingArrowX0 <= localProgress )
        {
            pyRed = startingArrowYRed;
            pyGreen = startingArrowYGreen;
            pyGray = startingArrowYGray;

            double totalForce = -massOfArrowKilos * gravitationalConstant + 0.5 * densityOfAirKgM3 * crossSectionalArea * dragCoefficient * pow( startingVelocityYGreen, 2 );

            double accelerationX;
            double accelerationY;

            double acceleration = totalForce / massOfArrowKilos;

            if ( settingsAccelerationFormulas )
            {
                accelerationX = acceleration;
                accelerationY = acceleration - gravitationalConstant;
            }
            else
            {
                accelerationX = -friction * startingVelocityX0 / massOfArrowKilos;
                accelerationY = -friction * startingVelocityYRed / massOfArrowKilos - gravitationalConstant;
            }

            startingArrowX0 = startingArrowX0 + startingVelocityX0 * timeStep;
            startingArrowYRed = startingArrowYRed + startingVelocityYRed * timeStep;
            startingArrowYGreen = startingArrowYGreen + startingVelocityYGreen * timeStep;
            startingArrowYGray = startingArrowYGray + startingVelocityYGray * timeStep;

            if ( settingsGravityFormulas )
            {
                startingVelocityX0 = startingVelocityX0 + accelerationX * timeStep;
            }
            else
            {
                startingVelocityX0 = startingVelocityX0 - gravitationalConstant * timeStep;
            }

            startingVelocityYRed = startingVelocityYRed + accelerationY * timeStep;
            startingVelocityYGray = startingVelocityYGray - gravitationalConstant * timeStep;
            startingVelocityYGreen = startingVelocityYGreen + acceleration * timeStep;

            startingTime = startingTime + timeStep;
        }

        redAim = pyRed;
        greenAim = pyGreen;
        blueAim = pyGray;
    }

    private double getHorizonLine( )
    {
        // h = tan( ( inclination_angle ) * PI / 180 ) * 0.395
        // px = convert_from_cm_to_px( h ) * screenHeightInCm / ( tan( 60 * PI / 180 ) * 0.395 )
        double OVERALL_VALUE = 12.f;
        double PIXELS_PER_DEGREE = 715F / ( 23F * 9F / 16F );
        double GAUGE_HEIGHT = OVERALL_VALUE * PIXELS_PER_DEGREE;
        double UNITS_PER_PIXEL = OVERALL_VALUE / GAUGE_HEIGHT;
        double latherY = Math.sin( Math.toRadians( 90 ) );
        double pixelsAway = ( -headPitch ) / UNITS_PER_PIXEL;
        return -latherY * pixelsAway;
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
                    if ( indCoeff < coeffArray.length - 1 )
                    {
                        indCoeff = indCoeff + 1;
                        FILTER_COEFFICIENT = coeffArray[indCoeff];
                    }
                }
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                if ( action == KeyEvent.ACTION_DOWN )
                {
                    // Ensure the index is bound within the lower limit
                    if ( indCoeff > 0 )
                    {
                        indCoeff = indCoeff - 1;
                        FILTER_COEFFICIENT = coeffArray[indCoeff];
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

    private void estimatePitch( double[] android )
    {
        double androidMag = Math.sqrt( android[0] * android[0] + android[1] * android[1] + android[2] * android[2] );
        for ( int i = 0; i <= 2; i++ )
        {
            android[i] = android[i] / androidMag;
        }
        double c0 = android[0] + android[1] + android[2];
        double v1 = android[2] - android[1];
        double v2 = android[0] - android[2];
        double v3 = android[1] - android[0];
        double R32 = v1 + v2 * v3 / ( 1 + c0 );
        double R31 = -v2 + v1 * v3 / ( 1 + c0 );
        double theta = Math.toDegrees( Math.asin( R31 / Math.cos( Math.asin( -R32 ) ) ) ) + headPitchBias;
        if ( !Double.isNaN( theta ) )
        {
            headPitch = headPitch + ( theta - headPitch ) * FILTER_COEFFICIENT;
        }
    }
}
