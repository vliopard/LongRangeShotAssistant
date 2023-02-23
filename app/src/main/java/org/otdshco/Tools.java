package org.otdshco;

import org.otdshco.gauges.Params;

import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;
import android.widget.ImageView;

import static java.lang.StrictMath.tan;
import static java.lang.StrictMath.sin;
import static java.lang.StrictMath.cos;
import static java.lang.StrictMath.pow;
import static java.lang.StrictMath.atan;
import static java.lang.StrictMath.asin;
import static java.lang.StrictMath.sqrt;
import static java.lang.StrictMath.toDegrees;
import static java.lang.StrictMath.toRadians;

import androidx.core.content.ContextCompat;

public class Tools
{
    public static final int REQUEST_CODE_PERMISSIONS = 1001;

    public static final String[] REQUIRED_PERMISSIONS = new String[] { "android.permission.CAMERA" };

    public static double getObjectHeight( double distance, double angle, int radians )
    {
        double tanValue;
        switch ( radians )
        {
            case 0:
                tanValue = tan( angle );
                break;
            case 1:
                tanValue = tan( toRadians( angle ) );
                break;
            default:
                tanValue = tan( angle * Math.PI / 180 );
        }
        return distance * tanValue;
    }

    public static int customGetIntrinsicHeight( ImageView imageView ) {return imageView.getDrawable( ).getIntrinsicHeight( );} // 1877

    public static double ZoomFactor( double object )
    {
        if ( Params.valueScreenZoom > 0 )
        {
            object = object / Tools.getZoomFactor( );
        }
        return object;
    }

    public static double getHorizonLine( )
    {
        Params.parameter0 = ( float ) Tools.ZoomFactor( Params.parameter2x );
        return -sin( toRadians( Params.constantRightDegrees ) ) * ( ( -Params.headPitch ) / ( Params.overallValueGaugeDisplayOnFrame / ( Params.overallValueGaugeDisplayOnFrame * ( Params.parameter0 / ( Params.parameter2 * Params.parameter3 / Params.parameter4 ) ) ) ) );
    }

    public static double trends( double xValue )
    {
        xValue = 101 - xValue;
        return 6E-9 * pow( xValue, 4 ) - 1E-6 * pow( xValue, 3 ) + 2E-5 * pow( xValue, 2 ) + 0.011 * xValue + 0.149;
    }

    public static double getZoomFactor( )
    {
        return Tools.getObjectHeight( Tools.trends( Params.valueScreenZoom ), Params.valueTargetDistance, 0 ) / Tools.getObjectHeight( Tools.trends( 1 ), Params.valueTargetDistance, 0 );
    }

    public static int getPixelsOnScreen( double objectHeight, double objectDistance, int pixelsAmount )
    {
        return ( int ) ( pixelsAmount * atan( objectHeight / objectDistance ) / Params.valueLensFactor );
    }

    public static void estimatePitch( double[] android )
    {
        double androidMagnitude = sqrt( android[0] * android[0] + android[1] * android[1] + android[2] * android[2] );
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
        double theta = toDegrees( asin( R31 / cos( asin( -R32 ) ) ) ) + Params.headPitchBias;
        if ( !Double.isNaN( theta ) )
        {
            Params.headPitch = Params.headPitch + ( theta - Params.headPitch ) * Params.FILTER_COEFFICIENT;
        }
    }

    public static boolean allPermissionsGranted( Context context )
    {
        for ( String permission : REQUIRED_PERMISSIONS )
        {
            if ( ContextCompat.checkSelfPermission( context, permission ) != PackageManager.PERMISSION_GRANTED )
            {
                return false;
            }
        }
        return true;
    }

    public static float getGaugeHeight( ) {return Params.overallValueGaugeDisplayOnFrame * ( Params.parameter1 / ( Params.parameter2 * Params.parameter3 / Params.parameter4 ) );}

    public static float getMarginSpacing( ) {return getGaugeHeight( ) / ( Params.overallValueGaugeDisplayOnFrame / Params.UNITS_PER_GRADUATION );}

    public static float getUnitsPerPixel( ) {return Params.overallValueGaugeDisplayOnFrame / getGaugeHeight( );}

    public static void log( String message ) {Log.e( "LOG", message );}
}