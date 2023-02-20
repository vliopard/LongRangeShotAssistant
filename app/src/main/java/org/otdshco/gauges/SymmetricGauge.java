package org.otdshco.gauges;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;

import static org.otdshco.gauges.Params.CENTER_GAP;
import static org.otdshco.gauges.Params.CROSS_HAIR_LEN;
import static org.otdshco.gauges.Params.DASH_FILL_LEN;
import static org.otdshco.gauges.Params.DASH_GAP_LEN;
import static org.otdshco.gauges.Params.FLIGHT_PATH_RAD;
import static org.otdshco.gauges.Params.HORIZON_LEN;
import static org.otdshco.gauges.Params.LARGER_MARGIN_LEN;
import static org.otdshco.gauges.Params.LARGER_MARGIN_VAL;
import static org.otdshco.gauges.Params.STROKE_BOLD_WIDTH;
import static org.otdshco.gauges.Params.STROKE_WIDTH;
import static org.otdshco.gauges.Params.TEXT_SIZE;
import static org.otdshco.gauges.Params.UNITS_PER_GRADUATION;

import static org.otdshco.gauges.Params.halfScreenHeight;
import static org.otdshco.gauges.Params.halfScreenWidth;
import static org.otdshco.gauges.Params.levels;

import static org.otdshco.gauges.Params.redAim;
import static org.otdshco.gauges.Params.greenAim;
import static org.otdshco.gauges.Params.blueAim;

import static org.otdshco.gauges.Params.constantRightDegrees;
import static org.otdshco.gauges.Params.screenHeight;

import static org.otdshco.gauges.Params.valuePersonHeight;
import static org.otdshco.gauges.Params.valueTargetDistance;
import static org.otdshco.gauges.Params.valueScreenZoom;
import static org.otdshco.gauges.Params.inclinationAngle;

import org.otdshco.Tools;

abstract class SymmetricGauge implements Gauge
{
    String MAX_TEXT = "XXX"; // Maximum number of digits the gauge can represent

    float GAUGE_HEIGHT; // Overall gauge height (px)
    float OVERALL_VALUE; // Overall value the gauge can display in one frame

    private final float UNITS_PER_PIXEL;
    private final float MARGIN_SPACING; // Spacing between any two adjacent graduations on the gauge (px)

    private final float textWidth;
    private final float textHeight;

    private PointF GRAD_DIR = new PointF( );
    private PointF LADDER_DIR = new PointF( );

    private final PointF HORIZON_DIR;

    private final Paint textPaint;

    private final Paint targetPaintR;
    private final Paint targetPaintG;
    private final Paint targetPaintB;

    private final Paint positivePaint;
    private final Paint negativePaint;

    SymmetricGauge( )
    {
        // Define all the characteristics of the derived classes
        // NOTE: Ensure this is done right at the beginning of the constructor method, to
        // get a correct customization of the gauge
        defineGaugeChars( );

        /* Initialize paint brushes */
        textPaint = new Paint( );
        textPaint.setColor( Color.RED );
        textPaint.setStyle( Paint.Style.FILL );
        textPaint.setTextSize( TEXT_SIZE );
        textPaint.setTextAlign( Paint.Align.CENTER );

        targetPaintG = new Paint( );
        targetPaintG.setColor( Color.GREEN );
        targetPaintG.setStrokeWidth( STROKE_BOLD_WIDTH );
        targetPaintG.setStyle( Paint.Style.STROKE );
        targetPaintB = new Paint( );

        targetPaintB.setColor( Color.BLUE );
        targetPaintB.setStrokeWidth( STROKE_BOLD_WIDTH );
        targetPaintB.setStyle( Paint.Style.STROKE );
        targetPaintR = new Paint( );

        targetPaintR.setColor( Color.RED );
        targetPaintR.setStrokeWidth( STROKE_BOLD_WIDTH );
        targetPaintR.setStyle( Paint.Style.STROKE );

        positivePaint = new Paint( );
        positivePaint.setColor( Color.RED );
        positivePaint.setStrokeWidth( STROKE_WIDTH );
        positivePaint.setStyle( Paint.Style.STROKE );

        negativePaint = new Paint( );
        negativePaint.setColor( Color.RED );
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

    private void fix( PointF location )
    {
        location.x = location.x + halfScreenWidth;
        location.y = location.y + halfScreenHeight;
    }

    public void draw( Canvas canvas, PointF drawLocation, float... currentValues )
    {
        double theta = currentValues[0];

        // Estimate GRAD_DIR and LADDER_DIR
        GRAD_DIR = new PointF( ( float ) Math.cos( Math.toRadians( theta ) ), ( float ) Math.sin( Math.toRadians( theta ) ) );
        LADDER_DIR = new PointF( ( float ) Math.cos( Math.toRadians( theta + constantRightDegrees ) ), ( float ) Math.sin( Math.toRadians( theta + constantRightDegrees ) ) );

        float centerValue = currentValues[1];

        // Estimate the number of units to the nearest valid value (HIGHER than or equal to the current value)
        // NOTE: A valid value is one that can be represented on a graduation
        float unitsAway = centerValue % UNITS_PER_GRADUATION == 0 ? 0 : UNITS_PER_GRADUATION - centerValue % UNITS_PER_GRADUATION;

        // Estimate the nearest valid value (HIGHER than or equal to the current value)
        // tempVal = centerValue + unitsAway;

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
        unitsAway = centerValue % UNITS_PER_GRADUATION == 0 ? 0 : centerValue % UNITS_PER_GRADUATION;
        pixelsAway = unitsAway / UNITS_PER_PIXEL;
        double tempVal = centerValue - unitsAway;

        // Reset the value of location
        location.x = drawLocation.x + LADDER_DIR.x * pixelsAway;
        location.y = drawLocation.y + LADDER_DIR.y * pixelsAway;

        // Ensure the center graduation is NOT drawn twice
        if ( pixelsAway == 0 )
        {
            updateCounter( location, tempVal, +1 );
        }

        float pitch = currentValues[2];
        pixelsAway = ( pitch - centerValue ) / UNITS_PER_PIXEL;

        float xCoordinate = drawLocation.x - LADDER_DIR.x * pixelsAway;
        float yCoordinate = drawLocation.y - LADDER_DIR.y * pixelsAway;

        drawCrossHairs( positivePath, new PointF( xCoordinate, yCoordinate ) );

        drawAim( targetPathR, new PointF( xCoordinate, yCoordinate ), redAim );
        drawAim( targetPathG, new PointF( xCoordinate, yCoordinate ), greenAim );
        drawAim( targetPathB, new PointF( xCoordinate, yCoordinate ), blueAim );

        double temporaryValue;
        PointF point = new PointF( );

        temporaryValue = 0;
        point.x = xCoordinate;
        point.y = yCoordinate;
        for ( int i = 0; i < levels; i++ )
        {
            temporaryValue = updateCounter( point, temporaryValue, -1 );
            drawMargins( canvas, positivePath, point, temporaryValue );
            point.x = point.x - halfScreenWidth;
            point.y = point.y - halfScreenHeight;
        }

        temporaryValue = 0;
        point.x = xCoordinate;
        point.y = yCoordinate;
        for ( int i = 0; i < levels; i++ )
        {
            temporaryValue = updateCounter( point, temporaryValue, +1 );
            drawMargins( canvas, negativePath, point, -temporaryValue );
            point.x = point.x - halfScreenWidth;
            point.y = point.y - halfScreenHeight;
        }

        float flightPath = currentValues[3];
        pixelsAway = ( flightPath - centerValue ) / UNITS_PER_PIXEL;

        xCoordinate = drawLocation.x - LADDER_DIR.x * pixelsAway;
        yCoordinate = drawLocation.y - LADDER_DIR.y * pixelsAway;
        drawFlightPath( positivePath, new PointF( xCoordinate, yCoordinate ) );

        canvas.drawPath( positivePath, positivePaint );
        canvas.drawPath( negativePath, negativePaint );

        canvas.drawPath( targetPathR, targetPaintR );
        canvas.drawPath( targetPathB, targetPaintB );
        canvas.drawPath( targetPathG, targetPaintG );
    }

    private void drawAim( Path path, PointF image, double height )
    {
        fix( image );

        float xValue = image.x;
        float yValue = halfScreenHeight;

        double triHeight = Tools.getTriangleHeight( valueTargetDistance, inclinationAngle );

        double objectHeight = triHeight + ( valuePersonHeight - height );

        if ( valueScreenZoom > 1 )
        {
            double maxHeight = Tools.getObjectHeight( Tools.trends( 1 ), valueTargetDistance );
            double currentHeight = Tools.getObjectHeight( Tools.trends( valueScreenZoom ), valueTargetDistance );
            double zoomFactor = currentHeight / maxHeight;

            //objectHeight = convertHeight( objectHeight, maxHeight, currentHeight );

            Tools.log( "Height[" + objectHeight + "] ZoomFactor[" + zoomFactor + "]" );
            objectHeight = objectHeight / zoomFactor;
            Tools.log( "NewHeight[" + objectHeight + "] ZoomFactor[" + zoomFactor + "]" );
        }

        int pxOnScreen = Tools.getPixelsOnScreen( objectHeight, valueTargetDistance, screenHeight );

        yValue = yValue + pxOnScreen;
        // yValue = yValue + 58*3;
        // 1.29m == 58px   @ 3°
        // 4.64m == 58*2px @ 6°
        // 9.83m == 58*3px @ 9°

        path.moveTo( xValue - 16, yValue );
        path.lineTo( xValue - 4, yValue );
        path.moveTo( xValue + 4, yValue );
        path.lineTo( xValue + 16, yValue );
        //Tools.log( "yValue[" + yValue + "] px[" + pxOnScreen + "] height[" + height + "] tri[" + triHeight + "] objectHeight[" + objectHeight + "]" );
    }

    private void drawCrossHairs( Path path, PointF image )
    {
        fix( image );

        // In positive GRAD direction
        path.moveTo( image.x, image.y );

        float xValue = image.x + GRAD_DIR.x * CROSS_HAIR_LEN;
        float yValue = image.y + GRAD_DIR.y * CROSS_HAIR_LEN;
        path.lineTo( xValue, yValue );

        // In negative GRAD direction
        path.moveTo( image.x, image.y );
        xValue = image.x - GRAD_DIR.x * CROSS_HAIR_LEN;
        yValue = image.y - GRAD_DIR.y * CROSS_HAIR_LEN;
        path.lineTo( xValue, yValue );

        // In positive LADDER direction
        path.moveTo( image.x, image.y );
        xValue = image.x + LADDER_DIR.x * CROSS_HAIR_LEN;
        yValue = image.y + LADDER_DIR.y * CROSS_HAIR_LEN;
        path.lineTo( xValue, yValue );

        // In negative LADDER direction
        path.moveTo( image.x, image.y );
        xValue = image.x - LADDER_DIR.x * CROSS_HAIR_LEN;
        yValue = image.y - LADDER_DIR.y * CROSS_HAIR_LEN;
        path.lineTo( xValue, yValue );

        // Draw on positive side
        xValue = image.x + HORIZON_DIR.x * CENTER_GAP;
        yValue = image.y + HORIZON_DIR.y * CENTER_GAP;
        path.moveTo( xValue, yValue );

        xValue = image.x + HORIZON_DIR.x * ( CENTER_GAP + HORIZON_LEN );
        yValue = image.y + HORIZON_DIR.y * ( CENTER_GAP + HORIZON_LEN );
        path.lineTo( xValue, yValue );

        // Draw on negative side
        xValue = image.x - HORIZON_DIR.x * CENTER_GAP;
        yValue = image.y - HORIZON_DIR.y * CENTER_GAP;
        path.moveTo( xValue, yValue );

        xValue = image.x - HORIZON_DIR.x * ( CENTER_GAP + HORIZON_LEN );
        yValue = image.y - HORIZON_DIR.y * ( CENTER_GAP + HORIZON_LEN );
        path.lineTo( xValue, yValue );
    }

    private void drawFlightPath( Path path, PointF image )
    {
        fix( image );

        // In positive GRAD direction
        float xValue = image.x + GRAD_DIR.x * FLIGHT_PATH_RAD;
        float yValue = image.y + GRAD_DIR.y * FLIGHT_PATH_RAD;
        path.moveTo( xValue, yValue );

        xValue = image.x + GRAD_DIR.x * ( FLIGHT_PATH_RAD + CROSS_HAIR_LEN );
        yValue = image.y + GRAD_DIR.y * ( FLIGHT_PATH_RAD + CROSS_HAIR_LEN );
        path.lineTo( xValue, yValue );

        // In negative GRAD direction
        xValue = image.x - GRAD_DIR.x * FLIGHT_PATH_RAD;
        yValue = image.y - GRAD_DIR.y * FLIGHT_PATH_RAD;
        path.moveTo( xValue, yValue );

        xValue = image.x - GRAD_DIR.x * ( FLIGHT_PATH_RAD + CROSS_HAIR_LEN );
        yValue = image.y - GRAD_DIR.y * ( FLIGHT_PATH_RAD + CROSS_HAIR_LEN );
        path.lineTo( xValue, yValue );

        // In negative LADDER direction
        xValue = image.x - LADDER_DIR.x * FLIGHT_PATH_RAD;
        yValue = image.y - LADDER_DIR.y * FLIGHT_PATH_RAD;
        path.moveTo( xValue, yValue );

        xValue = image.x - LADDER_DIR.x * ( FLIGHT_PATH_RAD + CROSS_HAIR_LEN );
        yValue = image.y - LADDER_DIR.y * ( FLIGHT_PATH_RAD + CROSS_HAIR_LEN );
        path.lineTo( xValue, yValue );

        // Center circle
        path.addCircle( image.x, image.y, FLIGHT_PATH_RAD, Path.Direction.CCW );
    }

    private void drawMargins( Canvas canvas, Path path, PointF image, double temporaryValue )
    {
        fix( image );

        // Prevent any loss in precision up to 3 decimal places
        temporaryValue = Math.round( temporaryValue * 1000F ) / 1000F;

        if ( temporaryValue == 0 )
        {
            // Draw horizon line, if the current value is 0
            // Draw on positive side
            float xValue = image.x + HORIZON_DIR.x * CENTER_GAP;
            float yValue = image.y + HORIZON_DIR.y * CENTER_GAP;
            path.moveTo( xValue, yValue );

            xValue = image.x + HORIZON_DIR.x * ( CENTER_GAP + HORIZON_LEN );
            yValue = image.y + HORIZON_DIR.y * ( CENTER_GAP + HORIZON_LEN );
            path.lineTo( xValue, yValue );

            // Draw on negative side
            xValue = image.x - HORIZON_DIR.x * CENTER_GAP;
            yValue = image.y - HORIZON_DIR.y * CENTER_GAP;
            path.moveTo( xValue, yValue );

            xValue = image.x - HORIZON_DIR.x * ( CENTER_GAP + HORIZON_LEN );
            yValue = image.y - HORIZON_DIR.y * ( CENTER_GAP + HORIZON_LEN );
            path.lineTo( xValue, yValue );
        }
        else
        {
            // Draw larger margin, if the current value is a multiple of LARGER_MARGIN_VAL
            if ( temporaryValue % LARGER_MARGIN_VAL == 0 )
            {
                // Draw on positive side
                float xValue = image.x + GRAD_DIR.x * CENTER_GAP;
                float yValue = image.y + GRAD_DIR.y * CENTER_GAP;
                path.moveTo( xValue, yValue );

                xValue = image.x + GRAD_DIR.x * ( CENTER_GAP + LARGER_MARGIN_LEN );
                yValue = image.y + GRAD_DIR.y * ( CENTER_GAP + LARGER_MARGIN_LEN );
                path.lineTo( xValue, yValue );

                // Draw on negative side
                xValue = image.x - GRAD_DIR.x * CENTER_GAP;
                yValue = image.y - GRAD_DIR.y * CENTER_GAP;
                path.moveTo( xValue, yValue );

                xValue = image.x - GRAD_DIR.x * ( CENTER_GAP + LARGER_MARGIN_LEN );
                yValue = image.y - GRAD_DIR.y * ( CENTER_GAP + LARGER_MARGIN_LEN );
                path.lineTo( xValue, yValue );

                // Draw a new line to properly place the text
                placeText( canvas, image, Integer.toString( ( int ) temporaryValue ) );
            }
        }
    }

    private void placeText( Canvas canvas, PointF image, String string )
    {
        Path textPath = new Path( );

        float xValue = image.x + GRAD_DIR.x * ( CENTER_GAP + LARGER_MARGIN_LEN ) + LADDER_DIR.x * textHeight / 2;
        float yValue = image.y + GRAD_DIR.y * ( CENTER_GAP + LARGER_MARGIN_LEN ) + LADDER_DIR.y * textHeight / 2;
        textPath.moveTo( xValue, yValue );

        xValue = image.x + GRAD_DIR.x * ( CENTER_GAP + LARGER_MARGIN_LEN + textWidth ) + LADDER_DIR.x * textHeight / 2;
        yValue = image.y + GRAD_DIR.y * ( CENTER_GAP + LARGER_MARGIN_LEN + textWidth ) + LADDER_DIR.y * textHeight / 2;
        textPath.lineTo( xValue, yValue );

        canvas.drawTextOnPath( string, textPath, 0, 0, textPaint );

        textPath.rewind( );

        xValue = image.x - GRAD_DIR.x * ( CENTER_GAP + LARGER_MARGIN_LEN + textWidth ) + LADDER_DIR.x * textHeight / 2;
        yValue = image.y - GRAD_DIR.y * ( CENTER_GAP + LARGER_MARGIN_LEN + textWidth ) + LADDER_DIR.y * textHeight / 2;
        textPath.moveTo( xValue, yValue );

        xValue = image.x - GRAD_DIR.x * ( CENTER_GAP + LARGER_MARGIN_LEN ) + LADDER_DIR.x * textHeight / 2;
        yValue = image.y - GRAD_DIR.y * ( CENTER_GAP + LARGER_MARGIN_LEN ) + LADDER_DIR.y * textHeight / 2;
        textPath.lineTo( xValue, yValue );

        canvas.drawTextOnPath( string, textPath, 0, 0, textPaint );
    }

    private double updateCounter( PointF location, double temporaryValue, int sign )
    {
        location.x = location.x + sign * LADDER_DIR.x * MARGIN_SPACING;
        location.y = location.y + sign * LADDER_DIR.y * MARGIN_SPACING;
        return ( temporaryValue - sign * ( MARGIN_SPACING * UNITS_PER_PIXEL ) );
    }
}