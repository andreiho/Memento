package android.memento;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.memento.db.MemosContract;
import android.memento.db.MemosDBHelper;
import android.os.Bundle;
import android.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

/**
 * A fragment representing a list of done memos
 */
public class DoneFragment extends ListFragment {

    private OnFragmentInteractionListener mListener;
    private MemosDBHelper helper;

    public static final String TAG = "done";

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the fragment
     */
    public DoneFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        helper = new MemosDBHelper(getActivity());
        SQLiteDatabase sqlDB = helper.getReadableDatabase();

        // Declare which items to get in this view
        String status = "done";

        String[] columns = {
                MemosContract.Columns._ID,
                MemosContract.Columns.MEMO,
                MemosContract.Columns.STATUS
        };

        String where = MemosContract.Columns.STATUS + "='" + status + "'";

        Cursor cursor = sqlDB.query(
                MemosContract.TABLE,
                columns,
                where,
                null, null, null, null
        );

        // Construct the adapter
        ListAdapter adapter = new SimpleCursorAdapter(
                getActivity(),
                R.layout.fragment_done,
                cursor,
                new String[] { MemosContract.Columns.MEMO },
                new int[] { R.id.doneView },
                0
        );

        // Set the adapter
        setListAdapter(adapter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_done, container, false);

        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(String id);
    }

}
