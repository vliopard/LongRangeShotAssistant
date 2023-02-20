package org.otdshco.gauges;

import android.content.Context;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PointF;

import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.ImageView;

import org.otdshco.MainActivity;

import static org.otdshco.gauges.Params.headPitch;
import static org.otdshco.gauges.Params.hudParams;
import static org.otdshco.gauges.Params.screenWidth;
import static org.otdshco.gauges.Params.screenHeight;

public class Takeoff extends SurfaceView implements Runnable
{
    Thread thread = null;
    boolean canDraw = false;

    Canvas canvas;
    SurfaceHolder surfaceHolder;
    ImageView imageView;
    private final Gauge pitch;

    public Takeoff( Context context, AttributeSet attributeSet )
    {
        super( context, attributeSet );
        pitch = new Pitch( );
    }

    public Takeoff( Context context, SurfaceView surfaceView, ImageView imageView )
    {
        super( context );
        surfaceHolder = surfaceView.getHolder( );
        this.imageView = imageView;
        pitch = new Pitch( );
    }

    @Override
    public void run( )
    {
        while ( canDraw )
        {
            if ( surfaceHolder.getSurface( ).isValid( ) )
            {
                Bitmap canvasBitmap = Bitmap.createBitmap( screenWidth, screenHeight, Bitmap.Config.ARGB_8888 );
                canvasBitmap.eraseColor( Color.TRANSPARENT );
                canvas = surfaceHolder.lockCanvas( );
                canvas.setBitmap( canvasBitmap );
                this.drawGauges( hudParams.roll, headPitch + hudParams.pitch, hudParams.pitch, hudParams.flightPath );
                ( ( MainActivity ) getContext( ) ).runOnUiThread( ( ) -> imageView.setImageBitmap( canvasBitmap ) );
                surfaceHolder.unlockCanvasAndPost( canvas );
            }
        }
    }

    private void drawGauges( double rollV, double headV, double pitchV, double flightPathV )
    {
        //flt width = screenWidth
        //flt height = screenHeight
        //flt width = screenWidth * 0.5F // zero
        //flt height = screenHeight * 0.425F // zero
        float width = getWidth( ) * 0.5F; // zero
        float height = getHeight( )* 0.425F; // zero
        pitch.draw( canvas, new PointF( width, height ), ( float ) rollV, ( float ) headV, ( float ) pitchV, ( float ) flightPathV );
    }

    public void resume( )
    {
        canDraw = true;
        thread = new Thread( this );
        thread.start( );
    }

    public void pause( )
    {
        canDraw = false;
        while ( thread != null )
        {
            try
            {
                thread.join( );
                thread = null;
            }
            catch ( InterruptedException interruptedException )
            {
                interruptedException.printStackTrace( );
            }
        }
    }
}