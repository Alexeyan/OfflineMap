package com.example.offlinemap;

import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;

import androidx.core.app.NavUtils;
import androidx.fragment.app.FragmentActivity;

import java.io.File;

/**
 * An activity representing a list of Items. This activity has different
 * presentations for handset and tablet-size devices. On handsets, the activity
 * presents a list of items, which when touched, lead to a
 * {@link SelectionDetailActivity} representing item details. On tablets, the
 * activity presents the list of items and item details side-by-side using two
 * vertical panes.
 * <p/>
 * The activity makes heavy use of fragments. The list of items is a
 * {@link SelectionListFragment} and the item details (if present) is a
 * {@link SelectionDetailFragment}.
 * <p/>
 * This activity also implements the required {@link SelectionListFragment.Callbacks}
 * interface to listen for item selections.
 */
public class SelectionListActivity extends FragmentActivity implements
        SelectionListFragment.Callbacks {

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.map_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case R.id.delete:
                // Delete map file.
                File f = new File(getExternalFilesDir(null) + "/" + item.getItemId());
                f.delete();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    /**
     * Callback method from {@link SelectionListFragment.Callbacks} indicating that
     * the item with the given ID was selected.
     */
    @Override
    public void onItemSelected(String id) {
        /*Intent detailIntent = new Intent(this, SelectionDetailActivity.class);
        detailIntent.putExtra(SelectionDetailFragment.ARG_ITEM_ID, id);
        startActivity(detailIntent);*/
        Log.d("MAP FILES", "Deleting "+id);
        SelectionsValues.delete(this, id);
        SelectionsValues.loadFiles(this);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // This ID represents the Home or Up button. In the case of this
                // activity, the Up button is shown. Use NavUtils to allow users
                // to navigate up one level in the application structure. For
                // more details, see the Navigation pattern on Android Design:
                //
                // http://developer.android.com/design/patterns/navigation.html#up-vs-back
                //
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_list);

        setTitle("Manage Maps");


        //OfflineMapUtils.enableHome(this);

    }
}