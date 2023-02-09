package org.otdshco.gauges;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;

import static org.otdshco.MainActivity.redAim;
import static org.otdshco.MainActivity.greenAim;
import static org.otdshco.MainActivity.blueAim;

import static org.otdshco.MainActivity.sWidth;
import static org.otdshco.MainActivity.sHeight;

import static org.otdshco.MainActivity.mtSettingsEyeHeight;
import static org.otdshco.MainActivity.targetDistanceValue;
import static org.otdshco.MainActivity.inclinationAngle;

import org.otdshco.Tools;

abstract class SymmetricGauge implements Gauge
{
    float MX = sWidth / 2F;
    float MY = sHeight / 2F;

    float UNITS_PER_GRADUATION = 3;
    float OVERALL_VALUE; // Overall value the gauge can display in one frame
    double LARGER_MARGIN_VAL = 3; // Larger graduation value
    float GAUGE_HEIGHT; // Overall gauge height (px)

    float LARGER_MARGIN_LEN = 75; // Larger graduation length, (px)

    float HORIZON_LEN = 200; // Horizon length (px)
    float CENTER_GAP = 20;
    float DASH_FILL_LEN = 10; // Dash length in dashed line, for negative value (px)
    float DASH_GAP_LEN = 20;  // Gap in dashed line, for negative value (px)
    float CROSS_HAIR_LEN = 10;
    float FLIGHT_PATH_RAD = 5;
    String MAX_TEXT = "XXX"; // Maximum number of digits the gauge can represent
    int STROKE_WIDTH = 2;
    int TEXT_SIZE = 20;
    private PointF GRAD_DIR = new PointF( );
    private PointF LADDER_DIR = new PointF( );
    private final PointF HORIZON_DIR;
    private final float UNITS_PER_PIXEL;
    private final float MARGIN_SPACING; // Spacing between any two adjacent graduations on the gauge (px)
    private final Paint textPaint;
    private final Paint targetPaintR;
    private final Paint targetPaintG;
    private final Paint targetPaintB;
    private final Paint positivePaint;
    private final Paint negativePaint;
    private final float textHeight;
    private final float textWidth;

    SymmetricGauge( )
    {
        // Define all the characteristics of the derived classes
        // NOTE: Ensure this is done right at the beginning of the constructor method, to
        // get a correct customization of the gauge
        defineGaugeChars( );

        /* Initialize paint brushes */
        int customColor = Color.RED;

        textPaint = new Paint( );
        textPaint.setColor( customColor );
        textPaint.setStyle( Paint.Style.FILL );
        textPaint.setTextSize( TEXT_SIZE );
        textPaint.setTextAlign( Paint.Align.CENTER );

        targetPaintG = new Paint( );
        targetPaintG.setColor( Color.GREEN );
        targetPaintG.setStrokeWidth( 3 );
        targetPaintG.setStyle( Paint.Style.STROKE );
        targetPaintB = new Paint( );

        targetPaintB.setColor( Color.BLUE );
        targetPaintB.setStrokeWidth( 3 );
        targetPaintB.setStyle( Paint.Style.STROKE );
        targetPaintR = new Paint( );

        targetPaintR.setColor( Color.RED );
        targetPaintR.setStrokeWidth( 3 );
        targetPaintR.setStyle( Paint.Style.STROKE );

        positivePaint = new Paint( );
        positivePaint.setColor( customColor );
        positivePaint.setStrokeWidth( STROKE_WIDTH );
        positivePaint.setStyle( Paint.Style.STROKE );

        negativePaint = new Paint( );
        negativePaint.setColor( customColor );
        negativePaint.setStrokeWidth( STROKE_WIDTH );
        negativePaint.setStyle( Paint.Style.STROKE );

        float[] intervals = new float[] { DASH_FILL_LEN, DASH_GAP_LEN };
        float phase = 0;
        DashPathEffect dashPathEffect = new DashPathEffect( intervals, phase );
        negativePaint.setPathEffect( dashPathEffect );

        // Get height of a generic text
        Rect rect = new Rect( );
        textPaint.getTextBounds( "0123456789", 0, 10, rect );
        textHeight = rect.height( );

        // Assume the value never goes higher than MAX_TEXT
        textWidth = textPaint.measureText( MAX_TEXT );

        // Initialize all the dependent variables
        UNITS_PER_PIXEL = OVERALL_VALUE / GAUGE_HEIGHT;

        // MARGIN_SPACING must be an integer, else, there will be round-off error in the final calculations
        MARGIN_SPACING = GAUGE_HEIGHT / ( OVERALL_VALUE / UNITS_PER_GRADUATION );

        // The horizon direction is always leveled
        HORIZON_DIR = new PointF( ( float ) Math.cos( 0 ), ( float ) Math.sin( 0 ) );
    }

    // Must be implemented by the derived class to customize the gauge
    abstract void defineGaugeChars( );

    private void fix( PointF i )
    {
        i.x = i.x + MX;
        i.y = i.y + MY;
    }

    public void draw( Canvas canvas, PointF drawLocation, float... currVals )
    {
        double theta = currVals[0];

        // Estimate GRAD_DIR and LADDER_DIR
        GRAD_DIR = new PointF( ( float ) Math.cos( Math.toRadians( theta ) ), ( float ) Math.sin( Math.toRadians( theta ) ) );
        LADDER_DIR = new PointF( ( float ) Math.cos( Math.toRadians( theta + 90 ) ), ( float ) Math.sin( Math.toRadians( theta + 90 ) ) );

        float centerVal = currVals[1];

        // Estimate the number of units to the nearest valid value (HIGHER than or equal to the current value)
        // NOTE: A valid value is one that can be represented on a graduation
        float unitsAway = centerVal % UNITS_PER_GRADUATION == 0 ? 0 : UNITS_PER_GRADUATION - centerVal % UNITS_PER_GRADUATION;

        // Estimate the nearest valid value (HIGHER than or equal to the current value)
        // tempVal = centerVal + unitsAway;

        // Estimate the number of pixels to the nearest valid value, e.g. 3 units is 3 units / UNITS_PER_PIXEL away
        float pixelsAway = unitsAway / UNITS_PER_PIXEL;

        // Estimate location of the nearest valid value
        PointF location = new PointF( );

        location.x = drawLocation.x - LADDER_DIR.x * pixelsAway;
        location.y = drawLocation.y - LADDER_DIR.y * pixelsAway;

        Path positivePath = new Path( );
        Path negativePath = new Path( );

        Path targetPathR = new Path( );
        Path targetPathG = new Path( );
        Path targetPathB = new Path( );

        // Estimate the nearest valid value (LOWER than or equal to the current value)
        unitsAway = centerVal % UNITS_PER_GRADUATION == 0 ? 0 : centerVal % UNITS_PER_GRADUATION;
        pixelsAway = unitsAway / UNITS_PER_PIXEL;
        double tempVal = centerVal - unitsAway;

        // Reset the value of location
        location.x = drawLocation.x + LADDER_DIR.x * pixelsAway;
        location.y = drawLocation.y + LADDER_DIR.y * pixelsAway;

        if ( pixelsAway == 0 ) // Ensure the center graduation is NOT drawn twice
        {
            updateCounter( location, tempVal, +1 );
        }

        float pitch = currVals[2];
        pixelsAway = ( pitch - centerVal ) / UNITS_PER_PIXEL;

        float x = drawLocation.x - LADDER_DIR.x * pixelsAway;
        float y = drawLocation.y - LADDER_DIR.y * pixelsAway;

        drawCrossHairs( positivePath, new PointF( x, y ) );

        drawAim( targetPathR, new PointF( x, y ), redAim );
        drawAim( targetPathG, new PointF( x, y ), greenAim );
        drawAim( targetPathB, new PointF( x, y ), blueAim );

        double temporaryValue;
        PointF p = new PointF( );

        temporaryValue = 0;
        p.x = x;
        p.y = y;
        for ( int i = 0; i < 40; i++ )
        {
            temporaryValue = updateCounter( p, temporaryValue, -1 );
            drawMargins( canvas, positivePath, p, temporaryValue );
            p.x = p.x - MX;
            p.y = p.y - MY;
        }

        temporaryValue = 0;
        p.x = x;
        p.y = y;
        for ( int i = 0; i < 40; i++ )
        {
            temporaryValue = updateCounter( p, temporaryValue, +1 );
            drawMargins( canvas, negativePath, p, -temporaryValue );
            p.x = p.x - MX;
            p.y = p.y - MY;
        }

        float flightPath = currVals[3];
        pixelsAway = ( flightPath - centerVal ) / UNITS_PER_PIXEL;

        x = drawLocation.x - LADDER_DIR.x * pixelsAway;
        y = drawLocation.y - LADDER_DIR.y * pixelsAway;
        drawFlightPath( positivePath, new PointF( x, y ) );

        canvas.drawPath( positivePath, positivePaint );
        canvas.drawPath( negativePath, negativePaint );

        canvas.drawPath( targetPathR, targetPaintR );
        canvas.drawPath( targetPathB, targetPaintB );
        canvas.drawPath( targetPathG, targetPaintG );
    }

    private void drawAim( Path path, PointF i, double height )
    {
        fix( i );

        float x = i.x;
        float y = sHeight / 2F;

        double triHeight = Tools.getTriangleHeight( targetDistanceValue, inclinationAngle );

        double td = triHeight + ( mtSettingsEyeHeight - height );

        int pxOnScreen = Tools.getPxOnScreen( td, targetDistanceValue, sHeight );

        y = y + pxOnScreen;
        // y = y + 58*3;
        // 1.29m == 58px   @ 3°
        // 4.64m == 58*2px @ 6°
        // 9.83m == 58*3px @ 9°

        path.moveTo( x - 16, y );
        path.lineTo( x - 4, y );
        path.moveTo( x + 4, y );
        path.lineTo( x + 16, y );
        Tools.log( "y[" + y + "] px[" + pxOnScreen + "] height[" + height + "] tri[" + triHeight + "] td[" + td + "]" );
    }

    private void drawCrossHairs( Path path, PointF i )
    {
        fix( i );

        // In positive GRAD direction
        path.moveTo( i.x, i.y );
        path.lineTo( i.x + GRAD_DIR.x * CROSS_HAIR_LEN, i.y + GRAD_DIR.y * CROSS_HAIR_LEN );

        // In negative GRAD direction
        path.moveTo( i.x, i.y );
        path.lineTo( i.x - GRAD_DIR.x * CROSS_HAIR_LEN, i.y - GRAD_DIR.y * CROSS_HAIR_LEN );

        // In positive LADDER direction
        path.moveTo( i.x, i.y );
        path.lineTo( i.x + LADDER_DIR.x * CROSS_HAIR_LEN, i.y + LADDER_DIR.y * CROSS_HAIR_LEN );

        // In negative LADDER direction
        path.moveTo( i.x, i.y );
        path.lineTo( i.x - LADDER_DIR.x * CROSS_HAIR_LEN, i.y - LADDER_DIR.y * CROSS_HAIR_LEN );

        float xMovePositive = i.x + HORIZON_DIR.x * CENTER_GAP;
        float yMovePositive = i.y + HORIZON_DIR.y * CENTER_GAP;

        float xLinePositive = i.x + HORIZON_DIR.x * ( CENTER_GAP + HORIZON_LEN );
        float yLinePositive = i.y + HORIZON_DIR.y * ( CENTER_GAP + HORIZON_LEN );

        // Draw on positive side
        path.moveTo( xMovePositive, yMovePositive );
        path.lineTo( xLinePositive, yLinePositive );

        float xMoveNegative = i.x - HORIZON_DIR.x * CENTER_GAP;
        float yMoveNegative = i.y - HORIZON_DIR.y * CENTER_GAP;

        float xLineNegative = i.x - HORIZON_DIR.x * ( CENTER_GAP + HORIZON_LEN );
        float yLineNegative = i.y - HORIZON_DIR.y * ( CENTER_GAP + HORIZON_LEN );

        // Draw on negative side
        path.moveTo( xMoveNegative, yMoveNegative );
        path.lineTo( xLineNegative, yLineNegative );
    }

    private void drawFlightPath( Path path, PointF i )
    {
        fix( i );

        // In positive GRAD direction
        path.moveTo( i.x + GRAD_DIR.x * FLIGHT_PATH_RAD, i.y + GRAD_DIR.y * FLIGHT_PATH_RAD );
        path.lineTo( i.x + GRAD_DIR.x * ( FLIGHT_PATH_RAD + CROSS_HAIR_LEN ), i.y + GRAD_DIR.y * ( FLIGHT_PATH_RAD + CROSS_HAIR_LEN ) );

        // In negative GRAD direction
        path.moveTo( i.x - GRAD_DIR.x * FLIGHT_PATH_RAD, i.y - GRAD_DIR.y * FLIGHT_PATH_RAD );
        path.lineTo( i.x - GRAD_DIR.x * ( FLIGHT_PATH_RAD + CROSS_HAIR_LEN ), i.y - GRAD_DIR.y * ( FLIGHT_PATH_RAD + CROSS_HAIR_LEN ) );

        // In negative LADDER direction
        path.moveTo( i.x - LADDER_DIR.x * FLIGHT_PATH_RAD, i.y - LADDER_DIR.y * FLIGHT_PATH_RAD );
        path.lineTo( i.x - LADDER_DIR.x * ( FLIGHT_PATH_RAD + CROSS_HAIR_LEN ), i.y - LADDER_DIR.y * ( FLIGHT_PATH_RAD + CROSS_HAIR_LEN ) );

        // Center circle
        path.addCircle( i.x, i.y, FLIGHT_PATH_RAD, Path.Direction.CCW );
    }

    private void drawMargins( Canvas canvas, Path path, PointF i, double tempVal )
    {
        fix( i );

        // Prevent any loss in precision up to 3 decimal places
        tempVal = Math.round( tempVal * 1000F ) / 1000F;

        if ( tempVal == 0 )
        {
            // Draw horizon line, if the current value is 0
            float xMovePositive = i.x + HORIZON_DIR.x * CENTER_GAP;
            float yMovePositive = i.y + HORIZON_DIR.y * CENTER_GAP;

            float xLInePositive = i.x + HORIZON_DIR.x * ( CENTER_GAP + HORIZON_LEN );
            float yLinePositive = i.y + HORIZON_DIR.y * ( CENTER_GAP + HORIZON_LEN );

            // Draw on positive side
            path.moveTo( xMovePositive, yMovePositive );
            path.lineTo( xLInePositive, yLinePositive );

            float xMoveNegative = i.x - HORIZON_DIR.x * CENTER_GAP;
            float yMoveNegative = i.y - HORIZON_DIR.y * CENTER_GAP;

            float xLineNegative = i.x - HORIZON_DIR.x * ( CENTER_GAP + HORIZON_LEN );
            float yLineNegative = i.y - HORIZON_DIR.y * ( CENTER_GAP + HORIZON_LEN );

            // Draw on negative side
            path.moveTo( xMoveNegative, yMoveNegative );
            path.lineTo( xLineNegative, yLineNegative );
        }
        else
        {
            // Draw larger margin, if the current value is a multiple of LARGER_MARGIN_VAL
            if ( tempVal % LARGER_MARGIN_VAL == 0 )
            {
                float xMovePositive = i.x + GRAD_DIR.x * CENTER_GAP;
                float yMovePositive = i.y + GRAD_DIR.y * CENTER_GAP;
                float xLinePositive = i.x + GRAD_DIR.x * ( CENTER_GAP + LARGER_MARGIN_LEN );
                float yLinePositive = i.y + GRAD_DIR.y * ( CENTER_GAP + LARGER_MARGIN_LEN );

                // Draw on positive side
                path.moveTo( xMovePositive, yMovePositive );
                path.lineTo( xLinePositive, yLinePositive );

                float xMoveNegative = i.x - GRAD_DIR.x * CENTER_GAP;
                float yMoveNegative = i.y - GRAD_DIR.y * CENTER_GAP;
                float xLineNegative = i.x - GRAD_DIR.x * ( CENTER_GAP + LARGER_MARGIN_LEN );
                float yLineNegative = i.y - GRAD_DIR.y * ( CENTER_GAP + LARGER_MARGIN_LEN );

                // Draw on negative side
                path.moveTo( xMoveNegative, yMoveNegative );
                path.lineTo( xLineNegative, yLineNegative );

                // Draw a new line to properly place the text
                placeText( canvas, i, Integer.toString( ( int ) tempVal ) );
            }
        }
    }

    private void placeText( Canvas canvas, PointF i, String str )
    {
        Path textPath = new Path( );

        float xMovePositive = i.x + GRAD_DIR.x * ( CENTER_GAP + LARGER_MARGIN_LEN ) + LADDER_DIR.x * textHeight / 2;
        float yMovePositive = i.y + GRAD_DIR.y * ( CENTER_GAP + LARGER_MARGIN_LEN ) + LADDER_DIR.y * textHeight / 2;
        float xLinePositive = i.x + GRAD_DIR.x * ( CENTER_GAP + LARGER_MARGIN_LEN + textWidth ) + LADDER_DIR.x * textHeight / 2;
        float yLinePositive = i.y + GRAD_DIR.y * ( CENTER_GAP + LARGER_MARGIN_LEN + textWidth ) + LADDER_DIR.y * textHeight / 2;

        textPath.moveTo( xMovePositive, yMovePositive );
        textPath.lineTo( xLinePositive, yLinePositive );

        canvas.drawTextOnPath( str, textPath, 0, 0, textPaint );

        textPath.rewind( );

        float xMoveNegative = i.x - GRAD_DIR.x * ( CENTER_GAP + LARGER_MARGIN_LEN + textWidth ) + LADDER_DIR.x * textHeight / 2;
        float yMoveNegative = i.y - GRAD_DIR.y * ( CENTER_GAP + LARGER_MARGIN_LEN + textWidth ) + LADDER_DIR.y * textHeight / 2;
        float xLineNegative = i.x - GRAD_DIR.x * ( CENTER_GAP + LARGER_MARGIN_LEN ) + LADDER_DIR.x * textHeight / 2;
        float yLineNegative = i.y - GRAD_DIR.y * ( CENTER_GAP + LARGER_MARGIN_LEN ) + LADDER_DIR.y * textHeight / 2;

        textPath.moveTo( xMoveNegative, yMoveNegative );
        textPath.lineTo( xLineNegative, yLineNegative );

        canvas.drawTextOnPath( str, textPath, 0, 0, textPaint );
    }

    // Update the counter location variable and return the current value
    private double updateCounter( PointF location, double tempVal, int sign )
    {
        location.x = location.x + sign * LADDER_DIR.x * MARGIN_SPACING;
        location.y = location.y + sign * LADDER_DIR.y * MARGIN_SPACING;
        return ( tempVal - sign * ( MARGIN_SPACING * UNITS_PER_PIXEL ) );
    }
}