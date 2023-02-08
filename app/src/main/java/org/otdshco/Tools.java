package org.otdshco;

import static android.content.Context.CAMERA_SERVICE;
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

    public static int convertFromCmToPx( double centimeters, double scrHeightInCm, WindowManager windowManager )
    {
        return ( int ) ( centimeters * scrPxHeight( windowManager ) / scrHeightInCm );
    }

    public static int getNavigationBarHeight( WindowManager windowManager )
    {
        DisplayMetrics metrics = new DisplayMetrics( );
        //getWindowManager( ).getDefaultDisplay( ).getMetrics( metrics );
        windowManager.getDefaultDisplay( ).getMetrics( metrics );
        int usableHeight = metrics.heightPixels;
        //getWindowManager( ).getDefaultDisplay( ).getRealMetrics( metrics );
        windowManager.getDefaultDisplay( ).getRealMetrics( metrics );
        int realHeight = metrics.heightPixels;
        if ( realHeight > usableHeight )
        {
            return realHeight - usableHeight;
        }
        return 0;
    }

    public static double getPixels( double heightInMeters, double inclinationAngle, WindowManager windowManager )
    {
        double screenCenterInPixels = scrPxHeight( windowManager ) / 2F;
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

    public static int scrPxHeight( WindowManager windowManager )
    {
        DisplayMetrics displayMetrics = new DisplayMetrics( );
        //getWindowManager( ).getDefaultDisplay( ).getMetrics( displayMetrics );
        windowManager.getDefaultDisplay( ).getMetrics( displayMetrics );
        return displayMetrics.heightPixels + getNavigationBarHeight( windowManager );
    }

    public static int scrPxWidth( WindowManager windowManager )
    {
        DisplayMetrics displayMetrics = new DisplayMetrics( );
        //getWindowManager( ).getDefaultDisplay( ).getMetrics( displayMetrics );
        windowManager.getDefaultDisplay( ).getMetrics( displayMetrics );
        return displayMetrics.widthPixels;
    }

    // 1877
    public static int customGetIntrinsicHeight( ImageView imageView )
    {
        return imageView.getDrawable( ).getIntrinsicHeight( );
    }

    // 1792
    public static int customGetHeight( ImageView imageView )
    {
        return imageView.getHeight( );
    }

    // 1792
    public static int customGetMeasuredHeight( ImageView imageView )
    {
        return imageView.getMeasuredHeight( );
    }

    // 715
    public static int bitmapGetHeight( ImageView imageView )
    {
        return ( ( BitmapDrawable ) imageView.getDrawable( ) ).getBitmap( ).getHeight( );
    }

    // 715
    public static int drawableGetHeight( ImageView imageView, Context context )
    {
        return Math.round( imageView.getDrawable( ).getIntrinsicHeight( ) / ( context.getResources( ).getDisplayMetrics( ).xdpi / DisplayMetrics.DENSITY_DEFAULT ) );
    }


    public static double getCmOnScreen( double height, double distance )
    {
        double metersToCentimeters = 100;
        double factor = 10;
        double mm_main_camera_focal_length = 25.9;   //26
        double mm_second_camera_focal_length = 31.7; //13;
        double mm_third_camera_focal_length = 14;    //40;
        double mm_fourth_camera_focal_length = 25.6;

        if ( distance == 0 )
        {
            distance = 1;
        }
        return ( height * metersToCentimeters ) * ( mm_main_camera_focal_length * factor ) / ( distance * metersToCentimeters );
    }

    public static int getPxOnScreen( double objectHeight, double objectDistance, int pixelsAmount )
    {
        double mt2cm = 1;
        double factor = 0.021;

        double fov_c1_r = 55.1;
        double fov_c2_f = 46.1;
        double fov_c3_r = 88;
        double fov_c4_f = 55.5;
        double fov_c5_r = 55.1;

        return ( int ) ( pixelsAmount * atan( ( objectHeight * mt2cm ) / ( objectDistance * mt2cm ) ) / ( fov_c1_r * factor ) );
    }

    public static double getTriangleHeight( double distance, double angle )
    {
        return distance * tan( angle * PI / 180 );
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

    public static double getDistanceRatio( double distance, double height )
    {
        return 1 / ( 1 + Math.pow( distance / height, 2 ) );
    }

    public static double getSizeInPixels( double distance, double height, double ratio )
    {
        return ratio * ( height / distance );
    }

    public static double getPxHeightFromInclination( double incAngle )
    {
        return 170 * incAngle / 3;
    }

    public static double getMeterMax( double heightInMeters, double incAngle, WindowManager windowManager )
    {
        //ret px_screen_center * heightInMeters / 170; // 24/3
        //ret px_screen_center * heightInMeters / 340; // 44/6
        //ret px_screen_center * heightInMeters / 425; // xx/7.5
        //ret px_screen_center * heightInMeters / 510; // 62/9
        //ret px_screen_center * heightInMeters / 680; // 77/12
        return ( scrPxHeight( windowManager ) / 2F ) * heightInMeters / getPxHeightFromInclination( incAngle );
    }

    // getMaxPx ()
    // getMaxCm ()
    public static double getCmToPx( double centimeters, double maxPixels, double maxCentimeters )
    {
        return centimeters * maxPixels / maxCentimeters;
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
        catch ( CameraAccessException e )
        {
            e.printStackTrace( );
        }
    }

    public static void log( String message )
    {
        Log.e( "LOG", message );
    }

}