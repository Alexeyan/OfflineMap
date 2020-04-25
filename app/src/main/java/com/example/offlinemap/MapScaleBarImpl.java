package com.example.offlinemap;

import org.mapsforge.core.graphics.Bitmap;
import org.mapsforge.core.graphics.GraphicContext;
import org.mapsforge.core.graphics.GraphicFactory;
import org.mapsforge.map.model.DisplayModel;
import org.mapsforge.map.model.IMapViewPosition;
import org.mapsforge.map.model.MapViewDimension;
import org.mapsforge.map.scalebar.DefaultMapScaleBar;

public class MapScaleBarImpl extends DefaultMapScaleBar {

    private final MapViewDimension mapViewDimension;

    public MapScaleBarImpl(IMapViewPosition mapViewPosition,
                           MapViewDimension mapViewDimension, GraphicFactory graphicFactory,
                           DisplayModel displayModel) {
        super(mapViewPosition, mapViewDimension, graphicFactory, displayModel);
        this.mapViewDimension = mapViewDimension;
    }

    @Override
    public void draw(GraphicContext graphicContext) {
        if (!this.isVisible()) {
            return;
        }

        if (this.mapViewDimension.getDimension() == null) {
            return;
        }

        if (this.isRedrawNecessary()) {
            redraw(this.mapScaleCanvas);
            this.redrawNeeded = false;
        }

        graphicContext.drawBitmap(this.mapScaleBitmap, 0, 0);
    }

    public Bitmap getMapScaleBitmap() {
        return this.mapScaleBitmap;
    }
}
