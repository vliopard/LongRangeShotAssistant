package org.otdshco.gauges;

import android.graphics.Canvas;
import android.graphics.PointF;

public interface Gauge
{
    void draw( Canvas canvas, PointF currLocation, float... currVals );
}

