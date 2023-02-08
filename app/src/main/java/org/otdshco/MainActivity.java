package org.otdshco;

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

    PreviewView mPreviewView;

    private Camera camera;
    private CameraInfo cameraInfo;
    private CameraSelector cameraSelector;

    private int screenZoomValue = 0;
    private VerticalSeekBar verticalSeekBarZoom;

    private float general_progress = 0;
    public static volatile float targetDistanceValue = 10;
    private VerticalSeekBar verticalSeekBarTargetDistance;

    private RelativeLayout RelativeLayoutForRotation;

    private ImageView imageViewHorizonLine;

    private TextView textViewForInclinationAngle;

    public static volatile double inclination_angle = 0;

    private int back_button_press = 0;

    private PrintWriter printWriter;

    private int MAX_SAMPLE_SIZE = 5;
    private float arrow_velocity_settings = 48;

    public static volatile float mt_settings_eye_height = 1.6F;

    private int image_view_height = 0;

    private float settingsArrowDiameter = 0.0065F;
    private float settingsArrowMass = 0.009F;
    private boolean settingsAccelerationFormulas = true;
    private boolean settingsGravityFormulas = true;
    private int lens_factor_settings = 100;

    public static volatile Params hudParams = new Params( );
    public static volatile float headPitch = 0.0f;
    public static volatile double red_aim;
    public static volatile double green_aim;
    public static volatile double blue_aim;
    public static volatile int sWidth = 600;
    public static volatile int sHeight = 1280;

    // Based on a measured estimate of the pitch attitude of the FRL of 5 deg and 4.32 deg of Bosch
    public static final float headPitchBias = 0f; // degrees

    // public static final float headPitchBias = 2.16f; // degrees
    private final float[] CoeffArray = new float[] { 0.003f, 0.01f, 0.05f };

    private int indCoeff = 1;
    public static volatile float FILTER_COEFFICIENT = 0.1f;

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
        SharedPreferences sharedPref = getSharedPreferences( "environment_settings", Context.MODE_PRIVATE );
        SharedPreferences.Editor editor = sharedPref.edit( );
        editor.putInt( "seek_zoom", screenZoomValue );
        editor.putFloat( "seek_distance", targetDistanceValue );
        editor.apply( );
    }

    private void loadSettings( )
    {
        SharedPreferences settings = getSharedPreferences( "environment_settings", MODE_PRIVATE );
        MAX_SAMPLE_SIZE = settings.getInt( "sensor_max_samples", 5 );
        arrow_velocity_settings = settings.getFloat( "arrow_velocity", 48 );
        mt_settings_eye_height = settings.getFloat( "person_height", 1.6F );
        settingsArrowDiameter = settings.getFloat( "arrow_diameter", 0.0065F );
        settingsArrowMass = settings.getFloat( "arrow_mass", 0.009F );
        settingsAccelerationFormulas = settings.getBoolean( "acceleration_formulas", true );
        settingsGravityFormulas = settings.getBoolean( "gravity_formulas", true );
        lens_factor_settings = settings.getInt( "lens_factor", 100 );
        screenZoomValue = settings.getInt( "seek_zoom", 100 );
        targetDistanceValue = settings.getFloat( "seek_distance", 0 );
    }

    private void setIco( int progress, int tens, int ones, VerticalSeekBar seekBar, int drawablePlaceHolder )
    {
        int cen = ( progress % 1000 ) / 100;
        int dez = ( progress % 100 ) / 10;
        int uni = ( progress % 100 ) % 10;

        @SuppressLint ( "UseCompatLoadingForDrawables" ) LayerDrawable bgDrawable = ( LayerDrawable ) getResources( ).getDrawable( drawablePlaceHolder, this.getTheme( ) );

        int pos0;
        if ( cen == 1 )
        {
            pos0 = getPos( 1 );
        }
        else
        {
            pos0 = getPos( dez );
        }

        @SuppressLint ( "UseCompatLoadingForDrawables" ) Drawable replace0 = getResources( ).getDrawable( pos0, this.getTheme( ) );
        bgDrawable.setDrawableByLayerId( tens, replace0 );

        int pos1;
        if ( cen == 1 )
        {
            pos1 = getPos( 99 );
        }
        else
        {
            pos1 = getPos( uni );
        }

        @SuppressLint ( "UseCompatLoadingForDrawables" ) Drawable replace1 = getResources( ).getDrawable( pos1, this.getTheme( ) );
        bgDrawable.setDrawableByLayerId( ones, replace1 );
        seekBar.setThumbPlaceholderDrawable( bgDrawable );
    }

    @SuppressLint ( { "WrongViewCast", "UseCompatLoadingForDrawables", "ClickableViewAccessibility" } )
    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        getWindow( ).setFlags( WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN );
        View decorView = getWindow( ).getDecorView( );
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility( uiOptions );
        setContentView( R.layout.activity_main );

        Button settingsBtn = findViewById( R.id.settings );
        settingsBtn.setOnClickListener( v -> {
            Intent i = new Intent( MainActivity.this, Settings.class );
            startActivity( i );
        } );

        pitchSurfaceView = findViewById( R.id.sv_pitch );
        pitchImageView = findViewById( R.id.im_pitch );

        mPreviewView = findViewById( R.id.camera_preview );
        RelativeLayoutForRotation = findViewById( R.id.rotate_layout );

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
            MeteringPointFactory meteringPointFactory = mPreviewView.createMeteringPointFactory( cameraSelector );
            float gx = event.getX( );
            float gy = event.getY( );
            FocusMeteringAction action = new FocusMeteringAction.Builder( meteringPointFactory.createPoint( gx, gy ) ).build( );
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
            general_progress = progress;
            saveSettings( );
            shot( );
            return null;
        } );

        buttonZoomUp.setOnClickListener( v -> {
            cameraInfo = camera.getCameraInfo( );
            float scale = Objects.requireNonNull( cameraInfo.getZoomState( ).getValue( ) ).getLinearZoom( );

            float linear_scale = scale * 100;
            if ( linear_scale < 100 )
            {
                linear_scale = linear_scale + 1;
            }

            verticalSeekBarZoom.setProgress( ( int ) linear_scale );
            screenZoomValue = ( int ) linear_scale;
            setIco( ( int ) linear_scale, R.id.chargingIconLeftZoom, R.id.chargingIconRightZoom, verticalSeekBarZoom, R.drawable.seekbar_zoom );

            AtomicReference<CameraControl> cameraControl = new AtomicReference<>( camera.getCameraControl( ) );
            cameraControl.get( ).setLinearZoom( linear_scale / 100F );
            saveSettings( );
        } );

        buttonZoomDown.setOnClickListener( v -> {
            cameraInfo = camera.getCameraInfo( );
            float scale = Objects.requireNonNull( cameraInfo.getZoomState( ).getValue( ) ).getLinearZoom( );

            float linear_scale = scale * 100;
            if ( linear_scale > 0 )
            {
                linear_scale = linear_scale - 1;
            }

            verticalSeekBarZoom.setProgress( ( int ) linear_scale );
            screenZoomValue = ( int ) linear_scale;
            setIco( ( int ) linear_scale, R.id.chargingIconLeftZoom, R.id.chargingIconRightZoom, verticalSeekBarZoom, R.drawable.seekbar_zoom );

            AtomicReference<CameraControl> cameraControl = new AtomicReference<>( camera.getCameraControl( ) );
            cameraControl.get( ).setLinearZoom( linear_scale / 100F );
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
            general_progress = progress;
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
            general_progress = progress;
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
            File outFile = new File( getExternalFilesDir( Environment.DIRECTORY_PICTURES ), fileName );
            try
            {
                printWriter = new PrintWriter( new FileOutputStream( outFile, true ) );
            }
            catch ( FileNotFoundException e )
            {
                e.printStackTrace( );
            }
        }

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder( ).permitAll( ).build( );
        StrictMode.setThreadPolicy( policy );

        // Set full-screen
        Window win = getWindow( );
        WindowManager.LayoutParams winParams = win.getAttributes( );
        winParams.flags |= WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS;

        pitchTakeOff = new Takeoff( this, pitchSurfaceView, pitchImageView );
        Thread th = new Thread( pitchTakeOff );
        th.start( );
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
        preview.setSurfaceProvider( mPreviewView.createSurfaceProvider( ) );
        camera = cameraProvider.bindToLifecycle( this, cameraSelector, preview, imageAnalysis, imageCapture );

        AtomicReference<CameraControl> cameraControl = new AtomicReference<>( camera.getCameraControl( ) );
        cameraControl.get( ).setLinearZoom( screenZoomValue );
        verticalSeekBarZoom.setProgress( screenZoomValue );

        setIco( screenZoomValue, R.id.chargingIconLeftZoom, R.id.chargingIconRightZoom, verticalSeekBarZoom, R.drawable.seekbar_zoom );

        verticalSeekBarTargetDistance.setProgress( Math.round( targetDistanceValue ) );
        general_progress = targetDistanceValue;
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

    public float averageList( List<Double> tallyUp )
    {
        float total = 0;
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
            double x, y, z;
            if ( method )
            {
                double ax = event.values[0];
                double ay = event.values[1];
                double az = event.values[2];
                double RAD_TO_DEG = 57.295779513082320876798154814105f;
                x = ( RAD_TO_DEG * atan( ax / sqrt( ay * ay + az * az ) ) );
                y = ( RAD_TO_DEG * atan( ay / sqrt( ax * ax + az * az ) ) );
                z = ( RAD_TO_DEG * atan( az / sqrt( ay * ay + ax * ax ) ) );
                rollingAverage[0] = roll( rollingAverage[0], x );
                rollingAverage[1] = roll( rollingAverage[1], y );
                rollingAverage[2] = roll( rollingAverage[2], z );

                x = averageList( rollingAverage[0] );
                y = averageList( rollingAverage[1] );
                z = averageList( rollingAverage[2] );
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
                x = tiltDegrees[0];
                y = tiltDegrees[1];
                z = tiltDegrees[2];
            }
            RelativeLayoutForRotation.setRotation( Math.round( x ) );

            RelativeLayout.LayoutParams params = ( RelativeLayout.LayoutParams ) imageViewHorizonLine.getLayoutParams( );
            //inclination_angle = ( int ) Math.round( -z );
            inclination_angle = -z;
            textViewForInclinationAngle.setText( String.format( getResources( ).getString( R.string.inclination ), -z ) );

            if ( image_view_height == 0 )
            {
                image_view_height = Tools.customGetIntrinsicHeight( imageViewHorizonLine );
            }

            float iy = getHorizonLine( );

            params.topMargin = ( int ) ( iy );
            imageViewHorizonLine.setLayoutParams( params );

            shot( );

            if ( x > 10 || x < -10 || y > 7 || y < -7 )
            {
                try
                {
                    writeFile( printWriter, x + "", y + "", z + "" );
                }
                catch ( IOException ioException )
                {
                    ioException.printStackTrace( );
                }
            }

            float[] android = new float[3];
            android[0] = -event.values[2];
            android[1] = event.values[0];
            android[2] = -event.values[1];

            estimatePitch( android );
        }
        else
        {
            if ( event.sensor.getType( ) == Sensor.TYPE_ROTATION_VECTOR )
            {
                float q1 = event.values[0];
                float q2 = event.values[1];
                float q3 = event.values[2];
                float q0 = ( float ) Math.sqrt( 1 - q1 * q1 - q2 * q2 - q3 * q3 ); // Its a unit quaternion

                // Formulas are obtained from https://en.wikipedia.org/wiki/Conversion_between_quaternions_and_Euler_angles
                float pitch = ( float ) ( Math.toDegrees( Math.atan2( 2f * ( q0 * q1 + q2 * q3 ), 1f - 2f * ( q1 * q1 + q2 * q2 ) ) ) - 90 );
                if ( !Float.isNaN( pitch ) )
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
        back_button_press = back_button_press + 1;
        printWriter.close( );
        Toast.makeText( MainActivity.this, "Press again to exit", Toast.LENGTH_SHORT ).show( );
        if ( back_button_press > 1 )
        {
            super.onBackPressed( );
        }
    }

    public void shot( )
    {
        float local_progress = general_progress;
        double inclination_angle_level = inclination_angle;
        double starting_velocity = arrow_velocity_settings;
        double person_height = mt_settings_eye_height;
        double diameter_of_arrow_meters = settingsArrowDiameter;
        double mass_of_arrow_kilos = settingsArrowMass;

        double starting_arrow_x0 = 0;
        double starting_arrow_y_red = person_height;
        double radius_of_arrow = diameter_of_arrow_meters / 2;

        double starting_time = 0;
        double gravitational_constant = 9.81;
        double time_step = 0.005;
        double density_of_air_kg_m3 = 1.2;

        double friction = density_of_air_kg_m3 * pow( radius_of_arrow, 2 );

        double starting_arrow_y_green = starting_arrow_y_red;
        double starting_arrow_y_gray = starting_arrow_y_red;

        double starting_velocity_x0 = starting_velocity * cos( toRadians( inclination_angle_level ) );
        double starting_velocity_sin = starting_velocity * sin( toRadians( inclination_angle_level ) );
        double starting_velocity_y_red = starting_velocity_sin;
        double starting_velocity_y_green = starting_velocity_sin;
        double starting_velocity_y_gray = starting_velocity_sin;

        double drag_coefficient = 9 * 0.3;
        double cross_sectional_area = PI * pow( ( diameter_of_arrow_meters / 2 ), 2 );

        double py_red = 0;
        double py_green = 0;
        double py_gray = 0;

        while ( starting_arrow_x0 <= local_progress )
        {
            py_red = starting_arrow_y_red;
            py_green = starting_arrow_y_green;
            py_gray = starting_arrow_y_gray;

            double total_force = -mass_of_arrow_kilos * gravitational_constant + 0.5 * density_of_air_kg_m3 * cross_sectional_area * drag_coefficient * pow( starting_velocity_y_green, 2 );

            double acceleration_x;
            double acceleration_y;

            double acceleration = total_force / mass_of_arrow_kilos;

            if ( settingsAccelerationFormulas )
            {
                acceleration_x = acceleration;
                acceleration_y = acceleration - gravitational_constant;
            }
            else
            {
                acceleration_x = -friction * starting_velocity_x0 / mass_of_arrow_kilos;
                acceleration_y = -friction * starting_velocity_y_red / mass_of_arrow_kilos - gravitational_constant;
            }

            starting_arrow_x0 = starting_arrow_x0 + starting_velocity_x0 * time_step;
            starting_arrow_y_red = starting_arrow_y_red + starting_velocity_y_red * time_step;
            starting_arrow_y_green = starting_arrow_y_green + starting_velocity_y_green * time_step;
            starting_arrow_y_gray = starting_arrow_y_gray + starting_velocity_y_gray * time_step;

            if ( settingsGravityFormulas )
            {
                starting_velocity_x0 = starting_velocity_x0 + acceleration_x * time_step;
            }
            else
            {
                starting_velocity_x0 = starting_velocity_x0 - gravitational_constant * time_step;
            }

            starting_velocity_y_red = starting_velocity_y_red + acceleration_y * time_step;
            starting_velocity_y_gray = starting_velocity_y_gray - gravitational_constant * time_step;
            starting_velocity_y_green = starting_velocity_y_green + acceleration * time_step;

            starting_time = starting_time + time_step;
        }

        red_aim = py_red;
        green_aim = py_green;
        blue_aim = py_gray;
    }

    private float getHorizonLine( )
    {
        // h = tan( ( inclination_angle ) * PI / 180 ) * 0.395
        // px = convert_from_cm_to_px( h ) * screenHeightInCm / ( tan( 60 * PI / 180 ) * 0.395 )
        float OVERALL_VALUE = 12.f;
        float PIXELS_PER_DEGREE = 715F / ( 23F * 9F / 16F );
        float GAUGE_HEIGHT = OVERALL_VALUE * PIXELS_PER_DEGREE;
        float UNITS_PER_PIXEL = OVERALL_VALUE / GAUGE_HEIGHT;
        float latherY = ( float ) Math.sin( Math.toRadians( 90 ) );
        float pixelsAway = ( -headPitch ) / UNITS_PER_PIXEL;
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
                    if ( indCoeff < CoeffArray.length - 1 )
                    {
                        indCoeff = indCoeff + 1;
                        FILTER_COEFFICIENT = CoeffArray[indCoeff];
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
                        FILTER_COEFFICIENT = CoeffArray[indCoeff];
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

    private void estimatePitch( float[] android )
    {
        float androidMag = ( float ) Math.sqrt( android[0] * android[0] + android[1] * android[1] + android[2] * android[2] );
        for ( int i = 0; i <= 2; i++ )
        {
            android[i] = android[i] / androidMag;
        }
        float c0 = android[0] + android[1] + android[2];
        float v1 = android[2] - android[1];
        float v2 = android[0] - android[2];
        float v3 = android[1] - android[0];
        float R32 = v1 + v2 * v3 / ( 1 + c0 );
        float R31 = -v2 + v1 * v3 / ( 1 + c0 );
        float theta = ( float ) Math.toDegrees( Math.asin( R31 / Math.cos( Math.asin( -R32 ) ) ) ) + headPitchBias;
        if ( !Float.isNaN( theta ) )
        {
            headPitch = headPitch + ( theta - headPitch ) * FILTER_COEFFICIENT;
        }
    }
}
