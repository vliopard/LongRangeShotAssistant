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

import static org.otdshco.MainActivity.headPitch;
import static org.otdshco.MainActivity.hudParams;

public class Takeoff extends SurfaceView implements Runnable
{
    Thread thread = null;
    boolean canDraw = false;

    Canvas canvas;
    SurfaceHolder surfaceHolder;
    ImageView imageView;
    private final Gauge pitch;

    public Takeoff(Context context, AttributeSet attrs)
    {
        super(context,attrs);
        pitch = new Pitch( );
    }

    public Takeoff( Context context, SurfaceView sView, ImageView iView )
    {
        super( context );
        surfaceHolder = sView.getHolder( );
        imageView = iView;
        pitch = new Pitch( );
    }

    @Override
    public void run( )
    {
        while ( canDraw )
        {
            if ( surfaceHolder.getSurface( ).isValid( ) )
            {
                //Bitmap canvasBitmap = Bitmap.createBitmap( 600, 200, Bitmap.Config.ARGB_8888 );
                Bitmap canvasBitmap = Bitmap.createBitmap( 600, 1280, Bitmap.Config.ARGB_8888 );
                canvasBitmap.eraseColor( Color.TRANSPARENT );
                canvas = surfaceHolder.lockCanvas( );
                canvas.setBitmap( canvasBitmap );
                this.drawGauges(  hudParams.roll,  headPitch + hudParams.pitch, hudParams.pitch, hudParams.flightPath );
                ( ( MainActivity ) getContext( ) ).runOnUiThread( ( ) -> imageView.setImageBitmap( canvasBitmap ) );
                surfaceHolder.unlockCanvasAndPost( canvas );
            }
        }
    }

    private void drawGauges(  float rollV, float headV, float pitchV, float flightPathV )
    {
        pitch.draw( canvas, new PointF( getWidth( ) * 0.5f, getHeight( ) * 0.425f ), rollV, headV, pitchV, flightPathV );
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
            catch ( InterruptedException e )
            {
                e.printStackTrace( );
            }
        }
    }
}