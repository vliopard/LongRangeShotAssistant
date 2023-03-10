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
                Bitmap canvasBitmap = Bitmap.createBitmap( Params.screenWidth, Params.screenHeight, Bitmap.Config.ARGB_8888 );
                canvasBitmap.eraseColor( Color.TRANSPARENT );
                canvas = surfaceHolder.lockCanvas( );
                canvas.setBitmap( canvasBitmap );
                this.drawGauges( Params.hudParams.roll, Params.headPitch + Params.hudParams.pitch, Params.hudParams.pitch, Params.hudParams.flightPath );
                ( ( MainActivity ) getContext( ) ).runOnUiThread( ( ) -> imageView.setImageBitmap( canvasBitmap ) );
                surfaceHolder.unlockCanvasAndPost( canvas );
            }
        }
    }

    private void drawGauges( double rollV, double headV, double pitchV, double flightPathV )
    {
        float width = getWidth( ) * 0.5F; // zero
        float height = getHeight( ) * 0.425F; // zero
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