package org.otdshco;

import static android.content.Context.CAMERA_SERVICE;
import static org.otdshco.gauges.Params.FILTER_COEFFICIENT;
import static org.otdshco.gauges.Params.UNITS_PER_GRADUATION;
import static org.otdshco.gauges.Params.cameraFov;
import static org.otdshco.gauges.Params.headPitch;
import static org.otdshco.gauges.Params.headPitchBias;
import static org.otdshco.gauges.Params.overallValueGaugeDisplayOnFrame;
import static org.otdshco.gauges.Params.parameter1;
import static org.otdshco.gauges.Params.parameter2;
import static org.otdshco.gauges.Params.parameter3;
import static org.otdshco.gauges.Params.parameter4;

import static java.lang.Math.PI;
import static java.lang.StrictMath.tan;
import static java.lang.StrictMath.atan;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;
import android.widget.ImageView;

public class Tools
{
    public static double getPixelHeightFromInclination( double incAngle )
    {
        return 170 * incAngle / 3;
    }

    public static double getObjectHeight( double angle, double distance )
    {
        return distance * Math.tan( angle );
    }

    public static double getTriangleHeight( double distance, double angle ) { return distance * tan( angle * PI / 180 ); }

    public static int customGetIntrinsicHeight( ImageView imageView )
    {
        return imageView.getDrawable( ).getIntrinsicHeight( );
    } // 1877

    public static double trends( double xValue )
    {
        xValue = 101 - xValue;
        return 6E-9 * Math.pow( xValue, 4 ) - 1E-6 * Math.pow( xValue, 3 ) + 2E-5 * Math.pow( xValue, 2 ) + 0.011 * xValue + 0.149;
    }

    public static int screenPixelHeight( WindowManager windowManager )
    {
        DisplayMetrics displayMetrics = new DisplayMetrics( );
        windowManager.getDefaultDisplay( ).getMetrics( displayMetrics );
        return displayMetrics.heightPixels + getNavigationBarHeight( windowManager );
    }

    public static double getMeterMax( double heightInMeters, double incAngle, WindowManager windowManager )
    {
        //ret px_screen_center * heightInMeters / 170; // 24/3
        //ret px_screen_center * heightInMeters / 340; // 44/6
        //ret px_screen_center * heightInMeters / 425; // xx/7.5
        //ret px_screen_center * heightInMeters / 510; // 62/9
        //ret px_screen_center * heightInMeters / 680; // 77/12
        return ( screenPixelHeight( windowManager ) / 2F ) * heightInMeters / getPixelHeightFromInclination( incAngle );
    }

    public static int getNavigationBarHeight( WindowManager windowManager )
    {
        DisplayMetrics metrics = new DisplayMetrics( );
        windowManager.getDefaultDisplay( ).getMetrics( metrics );
        int usableHeight = metrics.heightPixels;
        windowManager.getDefaultDisplay( ).getRealMetrics( metrics );
        int realHeight = metrics.heightPixels;
        if ( realHeight > usableHeight )
        {
            return realHeight - usableHeight;
        }
        return 0;
    }

    public static int getPixelsOnScreen( double objectHeight, double objectDistance, int pixelsAmount )
    {
        // TODO: REVIEW THESE VALUES (FACTOR AND FOV). ARE THEM CORRECT?
        double metersToCentimeters = 1;
        double factor = 0.47; //0.021;
        double fov_c1_r = cameraFov;
        //pub stat vol double cameraFov
        //db currentFov = cameraFov
        //db fov_c1_r = 55.1
        //db fov_c2_f = 46.1
        //db fov_c3_r = 88
        //db fov_c4_f = 55.5
        //db fov_c5_r = 55.1

        return ( int ) ( pixelsAmount * atan( ( objectHeight * metersToCentimeters ) / ( objectDistance * metersToCentimeters ) ) / ( fov_c1_r * factor ) );
    }

    public static float getFieldOfView( Context context, int cameraId )
    {
        try
        {
            CameraManager cameraManager = ( CameraManager ) context.getSystemService( CAMERA_SERVICE );
            CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics( cameraManager.getCameraIdList( )[cameraId] );
            return cameraCharacteristics.get( CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS )[0];
        }
        catch ( CameraAccessException cameraAccessException )
        {
            cameraAccessException.printStackTrace( );
        }
        return -1;
    }

    public static void estimatePitch( double[] android )
    {
        double androidMagnitude = Math.sqrt( android[0] * android[0] + android[1] * android[1] + android[2] * android[2] );
        for ( int i = 0; i <= 2; i++ )
        {
            android[i] = android[i] / androidMagnitude;
        }
        double c0 = android[0] + android[1] + android[2];
        double vector1 = android[2] - android[1];
        double vector2 = android[0] - android[2];
        double vector3 = android[1] - android[0];
        double R32 = vector1 + vector2 * vector3 / ( 1 + c0 );
        double R31 = -vector2 + vector1 * vector3 / ( 1 + c0 );
        double theta = Math.toDegrees( Math.asin( R31 / Math.cos( Math.asin( -R32 ) ) ) ) + headPitchBias;
        if ( !Double.isNaN( theta ) )
        {
            headPitch = headPitch + ( theta - headPitch ) * FILTER_COEFFICIENT;
        }
    }
    public static float getGaugeHeight() { return overallValueGaugeDisplayOnFrame * (parameter1 / ( parameter2 * parameter3 / parameter4 )); }
    public static float getMarginSpacing() { return getGaugeHeight() / ( overallValueGaugeDisplayOnFrame / UNITS_PER_GRADUATION ); }
    public static float getUnitsPerPixel() { return overallValueGaugeDisplayOnFrame / getGaugeHeight(); }

    public static void log( String message )
    {
        Log.e( "LOG", message );
    }

    // getMaxPx ()
    // getMaxCm ()
    public static double getCentimeterToPixel( double centimeters, double maxPixels, double maxCentimeters ) { return centimeters * maxPixels / maxCentimeters; }
    public static double toRadians( double grade )
    {
        return grade * Math.PI / 180;
    }
    public static int customGetHeight( ImageView imageView )
    {
        return imageView.getHeight( );
    } // 1792
    public static double getZoomHeight( double zoom ) { return ( 132 * ( zoom - 1 ) / ( -99 ) + 150 ) / 100; }
    public static int customGetMeasuredHeight( ImageView imageView )
    {
        return imageView.getMeasuredHeight( );
    } // 1792
    public static double getOppositeAngle( double height, double distance ) { return Math.atan( height / distance ); }
    public static double getSizeInPixels( double distance, double height, double ratio )
    {
        return ratio * ( height / distance );
    }
    public static double getDistanceRatio( double distance, double height )
    {
        return 1 / ( 1 + Math.pow( distance / height, 2 ) );
    }
    public static double getMagnificationFromHeights( double imageHeight, double objectHeight )
    {
        return imageHeight / objectHeight;
    }
    public static double getMagnificationFromFocal( double focalLen, double distanceFromObject )
    {
        return focalLen / distanceFromObject;
    }
    public static double getNearObjectHeightFromMagnification( double magnification, double realSize )
    {
        return magnification * realSize;
    }
    public static double convertHeight( double height, double maxHeight, double currentHeight ) { return ( height / maxHeight ) * currentHeight; }
    public static int bitmapGetHeight( ImageView imageView )
    {
        return ( ( BitmapDrawable ) imageView.getDrawable( ) ).getBitmap( ).getHeight( );
    } // 715
    public static int convertFromCentimeterToPixel( double centimeters, double scrHeightInCm, WindowManager windowManager )
    {
        return ( int ) ( centimeters * screenPixelHeight( windowManager ) / scrHeightInCm );
    }
    public static int drawableGetHeight( ImageView imageView, Context context ) { return Math.round( imageView.getDrawable( ).getIntrinsicHeight( ) / ( context.getResources( ).getDisplayMetrics( ).xdpi / DisplayMetrics.DENSITY_DEFAULT ) ); } // 715
    public static double roundAvoid( double value, int places )
    {
        double scale = Math.pow( 10, places );
        return Math.round( value * scale ) / scale;
    }
    public static double zoomToAngle( double zoom, double max, double min )
    {
        double maxZoom = 100;
        return ( ( ( zoom - 1 ) * ( min - max ) ) / ( maxZoom - 1 ) ) + max;
    }
    public static int screenPixelWidth( WindowManager windowManager )
    {
        DisplayMetrics displayMetrics = new DisplayMetrics( );
        //getWindowManager( ).getDefaultDisplay( ).getMetrics( displayMetrics );
        windowManager.getDefaultDisplay( ).getMetrics( displayMetrics );
        return displayMetrics.widthPixels;
    }
    public static double getPixels( double heightInMeters, double inclinationAngle, WindowManager windowManager )
    {
        double screenCenterInPixels = screenPixelHeight( windowManager ) / 2F;
        double maxHeightInMeters = getMeterMax( heightInMeters, inclinationAngle, windowManager );
        //ret heightInMeters * screenCenterInPixels / 8.878494372656393;  // 24/3
        //ret heightInMeters * screenCenterInPixels / 16.32206947655211;  // 44/6
        //ret heightInMeters * screenCenterInPixels / 23.1054948238147;   // 62/9
        //ret heightInMeters * screenCenterInPixels / 28.882685732808884; // 77/12
        if ( maxHeightInMeters == 0 )
        {
            maxHeightInMeters = 1;
        }
        return heightInMeters * screenCenterInPixels / maxHeightInMeters;
    }
    public static double getCentimetersOnScreen( double height, double distance )
    {
        double metersToCentimeters = 100;
        double factor = 10;
        double mm_main_camera_focal_length = 25.9;   //26
        //db mm_second_camera_focal_length = 31.7 //13;
        //db mm_third_camera_focal_length = 14    //40;
        //db mm_fourth_camera_focal_length = 25.6

        if ( distance == 0 )
        {
            distance = 1;
        }
        return ( height * metersToCentimeters ) * ( mm_main_camera_focal_length * factor ) / ( distance * metersToCentimeters );
    }
    public static void fixImageViewInHorizon( ImageView imageView, Context context )
    {
        CameraManager cameraManager = ( CameraManager ) context.getSystemService( CAMERA_SERVICE );
        try
        {
            String cameraId = cameraManager.getCameraIdList( )[0];
            CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics( cameraId );
            int sensorOrientation = cameraCharacteristics.get( CameraCharacteristics.SENSOR_ORIENTATION );
            Matrix matrix = new Matrix( );
            matrix.setRotate( sensorOrientation );
            imageView.setImageMatrix( matrix );
        }
        catch ( CameraAccessException cameraAccessException )
        {
            cameraAccessException.printStackTrace( );
        }
    }
}