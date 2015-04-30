package android.memento;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ListFragment;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
import android.memento.db.MemosContract;
import android.memento.db.MemosDBHelper;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends ActionBarActivity
        implements  MemosFragment.OnFragmentInteractionListener,
                    SnoozedFragment.OnFragmentInteractionListener,
                    DoneFragment.OnFragmentInteractionListener,
                    TrashFragment.OnFragmentInteractionListener,
                    NavigationDrawerCallbacks {

    private MemosDBHelper helper;
    private static final int RESULT_SETTINGS = 1;
    private static final int REQUEST_CODE = 2;

    Vibrator vibrator;

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;
    private Toolbar mToolbar;
    private final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);

        setSupportActionBar(mToolbar);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.fragment_drawer);

        // Set up the drawer.
        mNavigationDrawerFragment.setup(R.id.fragment_drawer, (DrawerLayout) findViewById(R.id.drawer), mToolbar);

        // Create memos using the FAB
        findViewById(R.id.fab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Initialize the dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

                builder.setTitle("Remind me to...");

                // Create the text input
                final EditText input = new EditText(MainActivity.this);
                input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
                input.setSingleLine();

                // Create a custom container for the input
                FrameLayout container = new FrameLayout(MainActivity.this);
                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

                // Set padding on the input in dp values
                int dp = 20;
                final float scale = getResources().getDisplayMetrics().density;
                int px = (int) (dp * scale + 0.5f);
                container.setPadding(px, px, px, px);

                // Set layout params and add the input to the dialog
                input.setLayoutParams(params);
                container.addView(input);
                builder.setView(container);

                // Create the buttons
                builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        String memo = input.getText().toString();
                        String status = "active";

                        MemosDBHelper helper = new MemosDBHelper(MainActivity.this);
                        SQLiteDatabase db = helper.getWritableDatabase();
                        ContentValues values = new ContentValues();

                        values.clear();
                        values.put(MemosContract.Columns.MEMO, memo);
                        values.put(MemosContract.Columns.STATUS, status);

                        db.insertWithOnConflict(MemosContract.TABLE, null, values, SQLiteDatabase.CONFLICT_IGNORE);

                        Toast.makeText(MainActivity.this, "Memo created.", Toast.LENGTH_SHORT).show();

                        // Hide the keyboard
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(input.getWindowToken(), 0);

                        // Setup AlarmManager used for notifications
                        // Put the current memo text in shared prefs to use in the alarm
                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString("memo_alarm", memo);
                        editor.commit();

                        // Setup the new intent
                        Intent intent = new Intent(MainActivity.this, AlarmReceiver.class);
                        PendingIntent pendingIntent = PendingIntent.getBroadcast(MainActivity.this, REQUEST_CODE, intent, 0);

                        // Setup the AlarmManager to send a notification in 10 seconds after creation, every day
                        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
                        am.setRepeating(am.RTC_WAKEUP, System.currentTimeMillis() + 10000, am.INTERVAL_DAY, pendingIntent);

                        // Vibrate
                        vibrator.vibrate(500);


                    }
                }).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
                    }

                });

                // Start the dialog finally
                builder.create().show();

                // Force keyboard to show on focus
                input.requestFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
            }
        });

        PreferenceManager.setDefaultValues(this, R.xml.user_settings, false);

        showUserSettings();

        // Vibrator
        vibrator = (Vibrator)getSystemService(VIBRATOR_SERVICE);
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {

        // update the main content by replacing fragments
        Fragment fragment;

        switch (position) {
            case 0: // memos
                fragment = getFragmentManager().findFragmentByTag(MemosFragment.TAG);

                if (fragment == null) {
                    fragment = new MemosFragment();
                }

                setTitle("Memos");

                getFragmentManager().beginTransaction().replace(R.id.container, fragment, MemosFragment.TAG).commit();
                break;

            case 1: // snoozed
                fragment = getFragmentManager().findFragmentByTag(SnoozedFragment.TAG);

                if (fragment == null) {
                    fragment = new SnoozedFragment();
                }

                setTitle("Snoozed");

                getFragmentManager().beginTransaction().replace(R.id.container, fragment, SnoozedFragment.TAG).commit();
                break;

            case 2: // done
                fragment = getFragmentManager().findFragmentByTag(DoneFragment.TAG);

                if (fragment == null) {
                    fragment = new DoneFragment();
                }

                setTitle("Done");

                getFragmentManager().beginTransaction().replace(R.id.container, fragment, DoneFragment.TAG).commit();
                break;

            case 3: // trash //todo
                fragment = getFragmentManager().findFragmentByTag(TrashFragment.TAG);

                if (fragment == null) {
                    fragment = new TrashFragment();
                }

                setTitle("Trash");

                getFragmentManager().beginTransaction().replace(R.id.container, fragment, TrashFragment.TAG).commit();
                break;

            case 4: // map
                Intent intent = new Intent(MainActivity.this, UserSettingsActivity.class);
                startActivityForResult(intent, RESULT_SETTINGS);

                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (resultCode) {
            case RESULT_SETTINGS:
                showUserSettings();
                break;
        }
    }

    // Method returning the user information from shared prefs
    private void showUserSettings() {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        String name = new String();
        name = sharedPrefs.getString("userName", "John Doe");

        String email = new String();
        email = sharedPrefs.getString("userEmail", "john.doe@gmail.com");

        mNavigationDrawerFragment.setUserData(
                name,
                email,
                BitmapFactory.decodeResource(getResources(), R.drawable.avatar)
        );
    }

    @Override
    public void onBackPressed() {
        if (mNavigationDrawerFragment.isDrawerOpen())
            mNavigationDrawerFragment.closeDrawer();
        else
            super.onBackPressed();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            return true;
        }

        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

    public void onFragmentInteraction(String id) {
        // this can be empty
    }

    /**
     * This method marks an item as done
     */
    public void markMemoAsDone(View view) {
        View v = (View) view.getParent();
        TextView memoTextView = (TextView) v.findViewById(R.id.memosView);
        String memo = memoTextView.getText().toString();

        String sql = String.format(
                "UPDATE %s SET status = 'done' WHERE %s = '%s'",
                MemosContract.TABLE,
                MemosContract.Columns.MEMO,
                memo);

        helper = new MemosDBHelper(MainActivity.this);
        SQLiteDatabase sqlDB = helper.getWritableDatabase();
        sqlDB.execSQL(sql);

        ListFragment fragment = (ListFragment) getFragmentManager().findFragmentByTag(MemosFragment.TAG);
        BaseAdapter adapter = (BaseAdapter) fragment.getListAdapter();
        adapter.notifyDataSetChanged();

        Toast.makeText(MainActivity.this, "Memo done.", Toast.LENGTH_SHORT).show();

        // Vibrate
        vibrator.vibrate(500);
    }

    /**
     * This method marks an item as done
     */
    public void markSnoozedAsDone(View view) {
        View v = (View) view.getParent();
        TextView snoozeTextView = (TextView) v.findViewById(R.id.snoozedView);
        String snooze = snoozeTextView.getText().toString();

        String sql = String.format(
                "UPDATE %s SET status = 'done' WHERE %s = '%s'",
                MemosContract.TABLE,
                MemosContract.Columns.MEMO,
                snooze);

        helper = new MemosDBHelper(MainActivity.this);
        SQLiteDatabase sqlDB = helper.getWritableDatabase();
        sqlDB.execSQL(sql);

        ListFragment fragment = (ListFragment) getFragmentManager().findFragmentByTag(SnoozedFragment.TAG);
        BaseAdapter adapter = (BaseAdapter) fragment.getListAdapter();
        adapter.notifyDataSetChanged();

        Toast.makeText(MainActivity.this, "Memo done.", Toast.LENGTH_SHORT).show();

        // Vibrate
        vibrator.vibrate(500);
    }

    /**
     * This method marks an item as snoozed
     */
    public void markAsSnoozed(View view) {
        View v = (View) view.getParent();
        TextView memoTextView = (TextView) v.findViewById(R.id.memosView);
        String memo = memoTextView.getText().toString();

        String sql = String.format(
                "UPDATE %s SET status = 'snoozed' WHERE %s = '%s'",
                MemosContract.TABLE,
                MemosContract.Columns.MEMO,
                memo);

        helper = new MemosDBHelper(MainActivity.this);
        SQLiteDatabase sqlDB = helper.getWritableDatabase();
        sqlDB.execSQL(sql);

        ListFragment fragment = (ListFragment) getFragmentManager().findFragmentByTag(MemosFragment.TAG);
        BaseAdapter adapter = (BaseAdapter) fragment.getListAdapter();
        adapter.notifyDataSetChanged();

        Toast.makeText(MainActivity.this, "Memo snoozed.", Toast.LENGTH_SHORT).show();

        // Vibrate
        vibrator.vibrate(500);
    }

    /**
     * This method marks an item as trash
     */
    public void markAsTrash(View view) {
        View v = (View) view.getParent();
        TextView doneTextView = (TextView) v.findViewById(R.id.doneView);
        String done = doneTextView.getText().toString();

        String sql = String.format(
                "UPDATE %s SET status = 'trash' WHERE %s = '%s'",
                MemosContract.TABLE,
                MemosContract.Columns.MEMO,
                done);

        helper = new MemosDBHelper(MainActivity.this);
        SQLiteDatabase sqlDB = helper.getWritableDatabase();
        sqlDB.execSQL(sql);

        ListFragment fragment = (ListFragment) getFragmentManager().findFragmentByTag(DoneFragment.TAG);
        BaseAdapter adapter = (BaseAdapter) fragment.getListAdapter();
        adapter.notifyDataSetChanged();

        Toast.makeText(MainActivity.this, "Memo trashed.", Toast.LENGTH_SHORT).show();

        // Vibrate
        vibrator.vibrate(500);
    }
}
