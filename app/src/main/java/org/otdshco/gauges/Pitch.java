package org.otdshco.gauges;

import static org.otdshco.gauges.Params.parameter1;
import static org.otdshco.gauges.Params.parameter2;
import static org.otdshco.gauges.Params.parameter3;
import static org.otdshco.gauges.Params.parameter4;
import static org.otdshco.gauges.Params.overallValueGaugeDisplayOnFrame;

public class Pitch extends SymmetricGauge
{
    public Pitch( )
    {
        super( );
    }

    void defineGaugeChars( )
    {
        float PIXELS_PER_DEGREE = parameter1 / ( parameter2 * parameter3 / parameter4 );
        OVERALL_VALUE = overallValueGaugeDisplayOnFrame;
        GAUGE_HEIGHT = OVERALL_VALUE * PIXELS_PER_DEGREE;
    }
}