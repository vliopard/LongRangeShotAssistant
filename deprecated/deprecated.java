import static android.content.Context.CAMERA_SERVICE;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;

public class Deprecated {
    public static volatile double cameraFov;
    
    public static final double fov_c1_r = 55.1;
    public static final double fov_c2_f = 46.1;
    public static final double fov_c3_r = 88;
    public static final double fov_c4_f = 55.5;
    public static final double fov_c5_r = 55.1;
    public static final double mm_main_camera_focal_length = 25.9;   //26
    public static final double mm_second_camera_focal_length = 31.7; //13;
    public static final double mm_third_camera_focal_length = 14;    //40;
    public static final double mm_fourth_camera_focal_length = 25.6;

	cameraFov = Tools.getFieldOfView( this, 1 );

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

	public static double toRadians( double degrees ) 
	{
		return degrees * Math.PI / 180; 
	}
	
    public static double getPixelHeightFromInclination( double incAngle )
    {
        return 170 * incAngle / 3;
    }

    public static double getMeterMax( double heightInMeters, double incAngle )
    {
        //ret px_screen_center * heightInMeters / 170; // 24/3
        //ret px_screen_center * heightInMeters / 340; // 44/6
        //ret px_screen_center * heightInMeters / 425; // xx/7.5
        //ret px_screen_center * heightInMeters / 510; // 62/9
        //ret px_screen_center * heightInMeters / 680; // 77/12
        return ( screenHeight / 2F ) * heightInMeters / getPixelHeightFromInclination( incAngle );
    }

    // getMaxPx ()
    // getMaxCm ()
    public static double getCentimeterToPixel( double centimeters, double maxPixels, double maxCentimeters )
    {
        return centimeters * maxPixels / maxCentimeters;
    }
    
    public static int customGetHeight( ImageView imageView )
    {
        return imageView.getHeight( );
    } // 1792

    public static double getZoomHeight( double zoom )
    {
        return ( 132 * ( zoom - 1 ) / ( -99 ) + 150 ) / 100;
    }

    public static int customGetMeasuredHeight( ImageView imageView )
    {
        return imageView.getMeasuredHeight( );
    } // 1792

    public static double getOppositeAngle( double height, double distance )
    {
        return Math.atan( height / distance );
    }

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

    public static double convertHeight( double height, double maxHeight, double currentHeight )
    {
        return ( height / maxHeight ) * currentHeight;
    }

    public static int bitmapGetHeight( ImageView imageView )
    {
        return ( ( BitmapDrawable ) imageView.getDrawable( ) ).getBitmap( ).getHeight( );
    } // 715

    public static int convertFromCentimeterToPixel( double centimeters, double scrHeightInCm, WindowManager windowManager )
    {
        return ( int ) ( centimeters * screenHeight / scrHeightInCm );
    }

    public static int drawableGetHeight( ImageView imageView, Context context )
    {
        return Math.round( imageView.getDrawable( ).getIntrinsicHeight( ) / ( context.getResources( ).getDisplayMetrics( ).xdpi / DisplayMetrics.DENSITY_DEFAULT ) );
    } // 715

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
        windowManager.getDefaultDisplay( ).getMetrics( displayMetrics );
        return displayMetrics.widthPixels;
    }

    public static int screenPixelHeight( WindowManager windowManager )
    {
        DisplayMetrics displayMetrics = new DisplayMetrics( );
        windowManager.getDefaultDisplay( ).getMetrics( displayMetrics );
        return displayMetrics.heightPixels + getNavigationBarHeight( windowManager );
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

    public static double getPixels( double heightInMeters, double inclinationAngle )
    {
        double screenCenterInPixels = screenHeight / 2F;
        double maxHeightInMeters = getMeterMax( heightInMeters, inclinationAngle );
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