package com.example.offlinemap;


import org.mapsforge.core.graphics.Canvas;
import org.mapsforge.core.graphics.Color;
import org.mapsforge.core.graphics.Paint;
import org.mapsforge.core.graphics.Style;
import org.mapsforge.core.model.BoundingBox;
import org.mapsforge.core.model.LatLong;
import org.mapsforge.core.model.Point;
import org.mapsforge.core.util.MercatorProjection;
import org.mapsforge.core.util.Utils;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.layer.overlay.FixedPixelCircle;
import org.mapsforge.map.layer.renderer.TileRendererLayer;
import org.mapsforge.map.rendertheme.XmlRenderTheme;
//import org.mapsforge.samples.android.DefaultTheme;

class DefaultTheme extends BaseActivity {

    @Override
    protected String getMapFileName() {
        return null;
    }

    @Override
    protected XmlRenderTheme getRenderTheme() {
        return null;
    }

}

public class Selection extends DefaultTheme {

    private static final Paint GREEN = OfflineMapUtils.createPaint(
            AndroidGraphicFactory.INSTANCE.createColor(Color.GREEN), 0,
            Style.FILL);
    private static final Paint RED = OfflineMapUtils.createPaint(
            AndroidGraphicFactory.INSTANCE.createColor(Color.RED), 0,
            Style.FILL);
    private static final Paint BLACK = OfflineMapUtils.createPaint(
            AndroidGraphicFactory.INSTANCE.createColor(Color.BLACK), 0,
            Style.FILL);

    private int i;

    @Override
    protected void createLayers() {
        TileRendererLayer tileRendererLayer = new TileRendererLayer(
                this.tileCaches.get(0), getMapFile(),
                this.mapView.getModel().mapViewPosition,
                false, true, false,
                org.mapsforge.map.android.graphics.AndroidGraphicFactory.INSTANCE) {
            @Override
            public boolean onLongPress(LatLong tapLatLong, Point thisXY,
                                       Point tapXY) {
                Selection.this.onLongPress(tapLatLong);
                return true;
            }
        };
        tileRendererLayer.setXmlRenderTheme(this.getRenderTheme());
        mapView.getLayerManager().getLayers().add(tileRendererLayer);
        BLACK.setTextSize(22 * this.mapView.getModel().displayModel.getScaleFactor());
    }

    protected void onLongPress(final LatLong position) {
        float circleSize = 20 * this.mapView.getModel().displayModel.getScaleFactor();

        i += 1;

        FixedPixelCircle tappableCircle = new FixedPixelCircle(position,
                circleSize, GREEN, null) {

            int count = i;

            @Override
            public void draw(BoundingBox boundingBox, byte zoomLevel, Canvas
                    canvas, Point topLeftPoint) {
                super.draw(boundingBox, zoomLevel, canvas, topLeftPoint);

                long mapSize = MercatorProjection.getMapSize(zoomLevel, this.displayModel.getTileSize());

                int pixelX = (int) (MercatorProjection.longitudeToPixelX(position.longitude, mapSize) - topLeftPoint.x);
                int pixelY = (int) (MercatorProjection.latitudeToPixelY(position.latitude, mapSize) - topLeftPoint.y);
                String text = Integer.toString(count);
                canvas.drawText(text, pixelX - BLACK.getTextWidth(text) / 2, pixelY + BLACK.getTextHeight(text) / 2, BLACK);
            }

            @Override
            public boolean onLongPress(LatLong geoPoint, Point viewPosition,
                                       Point tapPoint) {
                if (this.contains(viewPosition, tapPoint)) {
                    Selection.this.mapView.getLayerManager()
                            .getLayers().remove(this);
                    Selection.this.mapView.getLayerManager()
                            .redrawLayers();
                    return true;
                }
                return false;
            }

            @Override
            public boolean onTap(LatLong geoPoint, Point viewPosition,
                                 Point tapPoint) {
                if (this.contains(viewPosition, tapPoint)) {
                    toggleColor();
                    this.requestRedraw();
                    return true;
                }
                return false;
            }

            private void toggleColor() {
                if (this.getPaintFill().equals(Selection.GREEN)) {
                    this.setPaintFill(Selection.RED);
                } else {
                    this.setPaintFill(Selection.GREEN);
                }
            }
        };
        this.mapView.getLayerManager().getLayers().add(tappableCircle);
        tappableCircle.requestRedraw();

    }
}