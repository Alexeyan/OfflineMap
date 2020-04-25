package com.example.offlinemap;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;

import org.mapsforge.core.graphics.Color;
import org.mapsforge.core.graphics.GraphicFactory;
import org.mapsforge.core.graphics.Paint;
import org.mapsforge.core.graphics.Style;
import org.mapsforge.core.model.LatLong;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.layer.overlay.Circle;

public class OfflineMapUtils {
    // Replace umlauts in german OSM maps. e.g. baden-württemberg -> baden-wuerttemberg
    private static String[][] UMLAUT_REPLACEMENTS = { { "Ä", "Ae" }, { "Ü", "Ue" }, { "Ö", "Oe" }, { "ä", "ae" }, { "ü", "ue" }, { "ö", "oe" }, { "ß", "ss" } };
    public static String replaceUmlaute(String orig) {
        String result = orig;
        for (int i = 0; i < UMLAUT_REPLACEMENTS.length; i++) {
            result = result.replaceAll(UMLAUT_REPLACEMENTS[i][0], UMLAUT_REPLACEMENTS[i][1]);
        }
        return result;
    }


    public static GraphicFactory mGraphicFactory = AndroidGraphicFactory.INSTANCE;

    public static Circle createCircle(LatLong latLong) {//}, float radius, Color fillColor, Color strokeColor, float strokeWidth) {
        Paint paintFill = mGraphicFactory.createPaint();
        paintFill.setColor(Color.RED);
        paintFill.setStyle(Style.FILL);

        Paint paintStroke = mGraphicFactory.createPaint();
        paintStroke.setColor(Color.BLACK);
        paintStroke.setStrokeWidth(10.0f);
        paintStroke.setStyle(Style.STROKE);

        Circle c = new Circle(latLong, 25.0f, paintFill, paintStroke);
        return c;
    }



    static Paint createPaint(int color, int strokeWidth, Style style) {
        Paint paint = AndroidGraphicFactory.INSTANCE.createPaint();
        paint.setColor(color);
        paint.setStrokeWidth(strokeWidth);
        paint.setStyle(style);
        return paint;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static void enableHome(Activity a) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            // Show the Up button in the action bar.
            a.getActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

}
