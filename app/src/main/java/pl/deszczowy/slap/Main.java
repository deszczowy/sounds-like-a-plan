package pl.deszczowy.slap;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import java.io.IOException;
import java.util.ArrayList;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.content.Intent;
import android.database.Cursor;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.widget.EditText;
import android.text.InputType;
import android.widget.TextView;


public class Main extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    //region [ Main activity widgets ]
    private ListView mainListWidget;
    private TextView mainLabelWidget;
    private TextView mainProgressWidget;
    //endregion

    //region [ Objects ]
    private Preferences preferences;
    private Database database;
    //endregion

    //region [ Listeners ]
    private DialogInterface.OnClickListener updateTaskListener;
    private DialogInterface.OnClickListener deleteTaskListener;
    private DialogInterface.OnClickListener deleteChallengeListener;
    //endregion

    //region [ Private fields ]

    // Database record ids of loaded tasks or challenges list.
    private ArrayList<Integer> ids;
    // Currently selected item
    private long id;
    // Currently visible page, where
    private ActivePage page;
    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // initialize
        runBuilders();
        connectActivity();
        actionLoadAllTasks();

        // items context menu
        registerForContextMenu(findViewById(R.id.TheItems));
    }

    private void connectActivity(){
        this.mainLabelWidget = (TextView) findViewById(R.id.TheLabel);
        this.mainProgressWidget = (TextView) findViewById(R.id.TheProgress);
        this.mainListWidget = (ListView) findViewById(R.id.TheItems);
    }

    //region [ Objects and Listeners build ]
    private void runBuilders() {
        buildRequiredObjects();
        buildTheItemsListener();
        buildMainButtonListener();
        buildMarkAsDoneListener();
        buildDeleteTaskListener();
        buildDeleteChallengeListener();
    }

    private void buildRequiredObjects(){
        this.database = new Database(this);
        this.database.currentChallengeRead();
        this.ids = new ArrayList<>();
        this.preferences = new Preferences(this);
        this.id = -1;
    }

    private void buildTheItemsListener(){
        this.mainListWidget = (ListView) findViewById(R.id.TheItems);
        this.mainListWidget.setOnItemClickListener(new OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,long arg3) {

                Main.this.id = ids.get(arg2);
                if (ActivePage.CHALLENGES == page) {
                    actionEnterChallenge();
                }

                if (ActivePage.TASKS == page) {

                }
            }
        });
    }

    private void buildMarkAsDoneListener(){
        this.updateTaskListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        Main.this.database.updateTask(Main.this.id, 1);
                        actionLoadAllTasks();
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        //No button clicked
                        break;
                }
            }
        };
    }

    private void buildDeleteChallengeListener(){
        this.deleteChallengeListener = new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which){
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        Main.this.database.deleteChallenge(Main.this.id);
                        actionLoadAllChallenges();
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        // No button clicked
                        break;
                }
            }
        };
    }

    private void buildDeleteTaskListener(){
        this.deleteTaskListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        Main.this.database.deleteTask(Main.this.id);
                        actionLoadAllTasks();
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        //No button clicked
                        break;
                }
            }
        };
    }

    private void buildMainButtonListener(){
        FloatingActionButton myFab = (FloatingActionButton) findViewById(R.id.TheButton);
        myFab.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View v) {
                        actionMainButtonClick();
                    }
                }
        );
    }
    //endregion

    //region [ Actions ]
    private void actionMainButtonClick(){
        switch(this.page){
            case CHALLENGES:
                actionAddChallenge();
                break;
            case TASKS:
                actionAddTask();
                break;
        }
    }

    private void actionAddSubtask(){
        AlertDialog.Builder builder = new AlertDialog.Builder(Main.this);
        builder.setTitle(R.string.input_title_new_task_element);

        final EditText input = new EditText(Main.this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton(R.string.universal_word_ok, new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
                    String taskName = input.getText().toString();
                    Main.this.database.insertSubtask(taskName, Main.this.id);
                    actionLoadAllTasks();
                }
        });
        builder.setNegativeButton(R.string.universal_word_cancel, new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
        });

        builder.show();
    }

    private void actionAddTask(){
        AlertDialog.Builder builder = new AlertDialog.Builder(Main.this);

        if (0 == this.database.currentChallenge()){
            builder.setMessage(R.string.notice_no_challenges).setNegativeButton(getResources().getString(R.string.universal_word_ok), null).show();
        } else {
            builder.setTitle(R.string.input_title_new_task);

            final EditText input = new EditText(Main.this);
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            builder.setView(input);

            builder.setPositiveButton(R.string.universal_word_ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String taskName = input.getText().toString();
                    Main.this.database.insertTask(taskName);
                    actionLoadAllTasks();
                }
            });
            builder.setNegativeButton(R.string.universal_word_cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            builder.show();
        }
    }

    private void actionAddChallenge(){
        AlertDialog.Builder builder = new AlertDialog.Builder(Main.this);
        builder.setTitle(R.string.input_title_new_challenge);

        final EditText input = new EditText(Main.this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton(R.string.universal_word_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String challengeName = input.getText().toString();
                Main.this.database.insertChallenge(challengeName);
                actionLoadAllTasks();
            }
        });
        builder.setNegativeButton(R.string.universal_word_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }
    private void actionLoadAllChallenges(){
        this.page = ActivePage.CHALLENGES;

        FloatingActionButton fb = (FloatingActionButton) findViewById(R.id.TheButton);
        fb.setVisibility(View.VISIBLE);


        Cursor cur = this.database.getAllChallenges();
        cur.moveToFirst();
        ArrayList<String> array_list = new ArrayList<>();
        this.ids.clear();


        setListLabel(R.string.page_header_challenges);

        while(!cur.isAfterLast()){
            array_list.add(cur.getString(cur.getColumnIndex(Database.CHALLENGE_COLUMN_NAME)));
            this.ids.add(cur.getInt(cur.getColumnIndex(Database.CHALLENGE_COLUMN_ID)));
            cur.moveToNext();
        }
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                array_list );

        this.mainListWidget.setAdapter(arrayAdapter);
        setProgres(0, 0);
    }


    private void actionLoadAllTasks(){
        this.page = ActivePage.TASKS;

        FloatingActionButton fb = (FloatingActionButton) findViewById(R.id.TheButton);
        fb.setVisibility(View.VISIBLE);


        Cursor cur = this.database.getAllTasks();

        cur.moveToFirst();
        ArrayList<ItemTask> array_list = new ArrayList<>();
        this.ids.clear();

        setListLabel();

        String tname, tseries;
        int tid, tstate;

        int scroll_to = 0;

        Counter counter = new Counter();
        while(!cur.isAfterLast()){
            tname = cur.getString(cur.getColumnIndex(Database.TASK_COLUMN_NAME));
            tseries = cur.getString(cur.getColumnIndex(Database.TASK_COMPUTED_SUBTASKS));
            tstate = cur.getInt(cur.getColumnIndex(Database.TASK_COLUMN_STATE));

            tid = cur.getInt(cur.getColumnIndex(Database.TASK_COLUMN_ID));
            if (tname != null) {
                if (null == tseries) tseries = "";

                counter.click(0 == tstate);

                if (1 == counter.getAdditional()) scroll_to = counter.getMain() -1;

                array_list.add(
                        new ItemTask(
                                tstate, tname, tseries, 1 == counter.getAdditional()
                        )
                );
                this.ids.add(tid);
            }
            cur.moveToNext();
        }
        ItemTaskAdapter itemTaskAdapter = new ItemTaskAdapter(this, array_list);
        this.mainListWidget.setAdapter(itemTaskAdapter);

        if (this.preferences.getPreference(
                R.string.option_start_from_current_task_name,
                R.bool.option_start_from_current_task_default
        ))
            this.mainListWidget.setSelection(scroll_to);


        setProgres(counter.getAdditional(), counter.getMain());
    }

    private void actionLoadAllStarters(){
        this.page = ActivePage.STARTERS;

        FloatingActionButton fb = (FloatingActionButton) findViewById(R.id.TheButton);
        fb.setVisibility(View.INVISIBLE);

        this.mainListWidget = (ListView) findViewById(R.id.TheItems);
        Cursor cur = this.database.getAllStarters();
        cur.moveToFirst();
        ArrayList<String> array_list = new ArrayList<>();
        this.ids.clear();


        setListLabel(R.string.page_header_starters);

        while(!cur.isAfterLast()){
            array_list.add(cur.getString(cur.getColumnIndex(Database.STARTERS_COLUMN_NAME)));
            this.ids.add(cur.getInt(cur.getColumnIndex(Database.STARTERS_COLUMN_ID)));
            cur.moveToNext();
        }
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                array_list );

        this.mainListWidget.setAdapter(arrayAdapter);
        setProgres(0, 0);
    }

    private void actionMarkAsDone() {
        Cursor rs = this.database.getData(this.id);
        rs.moveToFirst();

        AlertDialog.Builder builder = new AlertDialog.Builder(Main.this);
        builder
                .setMessage(R.string.question_mark_task_done + rs.getString(rs.getColumnIndex(Database.TASK_COLUMN_NAME)) + "?")
                .setPositiveButton(R.string.universal_word_yes, this.updateTaskListener)
                .setNegativeButton(R.string.universal_word_no, this.updateTaskListener)
                .show();
    }

    private void actionDeleteChallenge() {
        AlertDialog.Builder builder = new AlertDialog.Builder(Main.this);
        builder
                .setMessage(R.string.question_delete_challenge)
                .setPositiveButton(R.string.universal_word_yes, this.deleteChallengeListener)
                .setNegativeButton(R.string.universal_word_no, this.deleteChallengeListener)
                .show();
    }

    private void actionDeleteTask() {
        Cursor rs = this.database.getData(this.id);
        rs.moveToFirst();
        AlertDialog.Builder builder = new AlertDialog.Builder(Main.this);
        builder.setMessage(R.string.question_delete_task + rs.getString(rs.getColumnIndex(Database.TASK_COLUMN_NAME)) + "?");
        builder.setPositiveButton(R.string.universal_word_yes, this.deleteTaskListener);
        builder.setNegativeButton(R.string.universal_word_no, this.deleteTaskListener);
        builder.show();
    }

    private void actionLoadStarter() {
        Decoder decoder = new Decoder("");
        try {
            decoder.decode(this.database.getStarter(this.id));
            this.database.autoLoad(decoder.get());
            actionLoadAllChallenges();
        } catch (IOException e) {}
    }

    private void actionStartFromTask() {
        this.database.startFromTask(this.id);
        actionLoadAllTasks();
    }

    private void actionEnterChallenge() {
        this.database.currentChallengeWrite(this.id);
        actionLoadAllTasks();
    }
    //endregion

    //region [ Controls interaction ]
    private void setListLabel(){
        this.mainLabelWidget.setText(this.database.currentChallengeName());
    }

    private void setListLabel(int resourceId){
        this.mainLabelWidget.setText(resourceId);
    }

    private void setProgres(int done, int all){
        if (all > 0){
            int remaining = all - done;
            int percent = Math.round(done * 100 / all);

            this.mainProgressWidget.setText(
                    String.format("done %d of %d (%d%)", remaining, all, percent)
            );
        } else {
            this.mainProgressWidget.setText("");
        }

    }
    //endregion

    //region [ Main list context menu ]
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        int menuItemIndex = item.getItemId();
        this.id = this.ids.get(info.position);

        switch (menuItemIndex){
            case 11: break;
            case 12: actionDeleteChallenge(); break;
            case 21: actionAddSubtask(); break;
            case 22: actionMarkAsDone(); break;
            case 23: actionDeleteTask(); break;
            case 24: actionStartFromTask(); break;
            case 31: actionLoadStarter(); break;
        }
        return true;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        if (v.getId()==R.id.TheItems) {
            menu.setHeaderTitle(R.string.context_menu_title);
            if (ActivePage.CHALLENGES == this.page){
                menu.add(Menu.NONE, 11, 1, R.string.challenge_context_menu_add_challenge);
                menu.add(Menu.NONE, 12, 2, R.string.challenge_context_menu_delete_challenge);
            } else if (ActivePage.TASKS == this.page) {
                menu.add(Menu.NONE, 21, 1, R.string.task_context_menu_add_task_element);
                menu.add(Menu.NONE, 22, 2, R.string.task_context_menu_mark_as_done);
                menu.add(Menu.NONE, 23, 3, R.string.task_context_menu_delete_task);
                menu.add(Menu.NONE, 24, 4, R.string.task_context_menu_start_in_point);
            } else if (ActivePage.STARTERS == this.page) {
                menu.add(Menu.NONE, 31, 1, R.string.starters_context_menu_load);
            }
        }
    }
    //endregion

    //region [ Drawer ]
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_challenge) {
            actionLoadAllChallenges();
        } else if (id == R.id.nav_starters){
            actionLoadAllStarters();
        } else if (id == R.id.nav_manage) {
            Intent intent = new Intent(getApplicationContext(),Settings.class);
            startActivity(intent);
        } else if (id == R.id.nav_info) {
            Intent intent = new Intent(getApplicationContext(),About.class);
            startActivity(intent);
        } else if (id == R.id.nav_debug) {
            this.database.logDb();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    //endregion
}
