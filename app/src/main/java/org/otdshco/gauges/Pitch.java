package org.otdshco.gauges;

public class Pitch extends SymmetricGauge
{
    public Pitch( )
    {
        super( );
    }

    void defineGaugeChars( )
    {
        float PIXELS_PER_DEGREE = 250F / ( 23F * 9F / 16F );
        OVERALL_VALUE = 12.f;
        GAUGE_HEIGHT = OVERALL_VALUE * PIXELS_PER_DEGREE;
    }
}