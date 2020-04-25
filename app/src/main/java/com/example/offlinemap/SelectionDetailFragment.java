package com.example.offlinemap;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import org.mapsforge.map.android.util.AndroidUtil;
import org.mapsforge.map.android.view.MapView;
import org.mapsforge.map.layer.cache.TileCache;
import org.mapsforge.map.reader.MapFile;
import org.mapsforge.map.rendertheme.InternalRenderTheme;

import java.io.File;

/**
 * A fragment representing a single Item detail screen. This fragment is either
 * contained in a {@link SelectionListActivity} in two-pane mode (on tablets) or a
 * {@link SelectionDetailActivity} on handsets.
 */
public class SelectionDetailFragment extends Fragment {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM_ID = "item_id";

    /**
     * The dummy content this fragment is presenting.
     */
    private SelectionsValues dummyItem;
    private MapView mapView;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public SelectionDetailFragment() {
        super();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getActivity().setTitle(getClass().getSimpleName());

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            // Load the dummy content specified by the fragment
            // arguments. In a real-world scenario, use a Loader
            // to load content from a content provider.
            this.dummyItem = SelectionsValues.ITEM_MAP.get(getArguments()
                    .getString(ARG_ITEM_ID));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_item_detail,
                container, false);

        if (this.dummyItem != null) {
            this.mapView = (MapView) rootView.findViewById(R.id.mapView);
            this.mapView.getMapScaleBar().setVisible(true);

            createLayers();
        }

        return rootView;
    }

    @Override
    public void onDestroy() {
        if (this.mapView != null) {
            this.mapView.destroyAll();
        }
        super.onDestroy();
    }


    private final byte PERMISSIONS_REQUEST_READ_STORAGE = 122;

    protected void createLayers() {
        TileCache tileCache = AndroidUtil.createTileCache(this.getActivity(), "fragments",
                this.mapView.getModel().displayModel.getTileSize(), 1.0f, 1.5);
        this.mapView.getLayerManager().getLayers().add(AndroidUtil.createTileRendererLayer(
                tileCache, this.mapView.getModel().mapViewPosition, getMapFile(),
                InternalRenderTheme.DEFAULT));

        // this.mapView.setCenter(this.dummyItem.location);
        this.mapView.setZoomLevel((byte) 16);
    }

    protected MapFile getMapFile() {
        return new MapFile(new File(getMapFileDirectory(),
                this.getMapFileName()));
    }

    protected File getMapFileDirectory() {
        return getContext().getExternalFilesDir(null);
    }

    protected String getMapFileName() {
        return "berlin.map";
    }
}