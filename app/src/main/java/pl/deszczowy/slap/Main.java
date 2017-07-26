package pl.deszczowy.slap;

import android.content.Context;
import android.content.SharedPreferences;
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

    private ListView fMainList;
    private TextView fMainLabel;
    private TextView fMainProgress;
    private Preferences fPreferences;

    private Database database;

    private DialogInterface.OnClickListener dialogClickListener;
    private DialogInterface.OnClickListener deleteItemListener;
    private ArrayList<Integer> array_ids;
    private long idToSearch;
    private int page; // 1 - challenges , 2 - tasks, 3 - starters

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

        this.fPreferences = new Preferences(this);

        // builders
        buildRequiredObjects();
        buildTheItemsListener();
        buildMainButtonListener();
        buildMarkAsDoneListener();
        buildDeleteTaskListener();

        // loading tasks
        loadAllTasks();

        // items context menu
        registerForContextMenu(findViewById(R.id.TheItems));
    }

    private void buildRequiredObjects(){
        this.database = new Database(this);
        this.database.currentChallengeRead();
        this.array_ids = new ArrayList<>();
    }

    private void buildTheItemsListener(){
        this.fMainList = (ListView) findViewById(R.id.TheItems);
        fMainList.setOnItemClickListener(new OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,long arg3) {

                int id = array_ids.get(arg2);
                if (1 == page) {
                    Main.this.database.currentChallengeWrite(id);
                    loadAllTasks();
                }
            }
        });
    }

    private void buildMarkAsDoneListener(){
        this.dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        Main.this.database.updateTask(idToSearch, 1);
                        loadAllTasks();
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        //No button clicked
                        break;
                }
            }
        };
    }

    private void buildDeleteTaskListener(){
        deleteItemListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        Main.this.database.deleteTask(idToSearch);
                        loadAllTasks();
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
                        mainButtonClick();
                    }
                }
        );
    }

    private void mainButtonClick(){
        switch(this.page){
            case 1:
                actionAddChallenge();
                break;
            case 2:
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
                    Main.this.database.insertSubtask(taskName, idToSearch);
                    loadAllTasks();
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
                    loadAllTasks();
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
                loadAllTasks();
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
    private void loadAllChallenges(){
        this.page = 1;

        FloatingActionButton fb = (FloatingActionButton) findViewById(R.id.TheButton);
        fb.setVisibility(View.VISIBLE);

        fMainList = (ListView) findViewById(R.id.TheItems);
        Cursor cur = this.database.getAllChallenges();
        cur.moveToFirst();
        ArrayList<String> array_list = new ArrayList<>();
        array_ids.clear();

        fMainLabel = (TextView) findViewById(R.id.TheLabel);
        fMainLabel.setText(R.string.page_header_challenges);

        while(!cur.isAfterLast()){
            array_list.add(cur.getString(cur.getColumnIndex(Database.CHALLENGE_COLUMN_NAME)));
            array_ids.add(cur.getInt(cur.getColumnIndex(Database.CHALLENGE_COLUMN_ID)));
            cur.moveToNext();
        }
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                array_list );

        fMainList.setAdapter(arrayAdapter);
    }


    private void loadAllTasks(){
        this.page = 2;

        FloatingActionButton fb = (FloatingActionButton) findViewById(R.id.TheButton);
        fb.setVisibility(View.VISIBLE);

        fMainList = (ListView) findViewById(R.id.TheItems);
        Cursor cur = this.database.getAllTasks();

        cur.moveToFirst();
        ArrayList<ItemTask> array_list = new ArrayList<>();
        array_ids.clear();

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
                array_ids.add(tid);
            }
            cur.moveToNext();
        }
        ItemTaskAdapter itemTaskAdapter = new ItemTaskAdapter(this, array_list);
        fMainList.setAdapter(itemTaskAdapter);

        if (this.fPreferences.getPreference(
                R.string.option_start_from_current_task_name,
                R.bool.option_start_from_current_task_default
        ))
            fMainList.setSelection(scroll_to);

        this.fMainProgress = (TextView) findViewById(R.id.TheProgress);
        this.fMainProgress.setText(
                String.format("%d of %d to go", counter.getAdditional(), counter.getMain())
        );
    }

    private void loadAllStarters(){
        this.page = 3;

        FloatingActionButton fb = (FloatingActionButton) findViewById(R.id.TheButton);
        fb.setVisibility(View.INVISIBLE);

        fMainList = (ListView) findViewById(R.id.TheItems);
        Cursor cur = this.database.getAllStarters();
        cur.moveToFirst();
        ArrayList<String> array_list = new ArrayList<>();
        array_ids.clear();

        fMainLabel = (TextView) findViewById(R.id.TheLabel);
        fMainLabel.setText(R.string.page_header_starters);

        while(!cur.isAfterLast()){
            array_list.add(cur.getString(cur.getColumnIndex(Database.STARTERS_COLUMN_NAME)));
            array_ids.add(cur.getInt(cur.getColumnIndex(Database.STARTERS_COLUMN_ID)));
            cur.moveToNext();
        }
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                array_list );

        fMainList.setAdapter(arrayAdapter);
    }

    private void setListLabel(){
        fMainLabel = (TextView) findViewById(R.id.TheLabel);
        fMainLabel.setText(this.database.currentChallengeName());
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        int menuItemIndex = item.getItemId();
        this.idToSearch = array_ids.get(info.position);

        Cursor rs = this.database.getData(this.idToSearch);
        rs.moveToFirst();

        AlertDialog.Builder builder = new AlertDialog.Builder(Main.this);

        switch (menuItemIndex){
            case 11:
                break;
            case 12:
                break;
            case 21: // new subtask
                actionAddSubtask();
                break;
            case 22: // done
                builder
                        .setMessage(R.string.question_mark_task_done + rs.getString(rs.getColumnIndex(Database.TASK_COLUMN_NAME)) + "?")
                        .setPositiveButton(R.string.universal_word_yes, dialogClickListener)
                        .setNegativeButton(R.string.universal_word_no, dialogClickListener)
                        .show();
                break;
            case 23: // delete
                builder.setMessage(R.string.question_delete_task + rs.getString(rs.getColumnIndex(Database.TASK_COLUMN_NAME)) + "?");
                builder.setPositiveButton(R.string.universal_word_yes, deleteItemListener);
                builder.setNegativeButton(R.string.universal_word_no, deleteItemListener);
                        builder.show();
                break;
            case 24: // start from task
                this.database.startFromTask(this.idToSearch);
                loadAllTasks();
                break;
            case 31:
                Decoder decoder = new Decoder("");
                try {
                    decoder.decode(this.database.getStarter(this.idToSearch));
                    this.database.autoLoad(decoder.get());
                    loadAllChallenges();
                } catch (IOException e) {}
                break;
        }
        return true;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        if (v.getId()==R.id.TheItems) {
            menu.setHeaderTitle(R.string.context_menu_title);
            if (1 == this.page){
                menu.add(Menu.NONE, 11, 1, R.string.challenge_context_menu_add_challenge);
                menu.add(Menu.NONE, 12, 2, R.string.challenge_context_menu_delete_challenge);
            } else if (2 == this.page) {
                menu.add(Menu.NONE, 21, 1, R.string.task_context_menu_add_task_element);
                menu.add(Menu.NONE, 22, 2, R.string.task_context_menu_mark_as_done);
                menu.add(Menu.NONE, 23, 3, R.string.task_context_menu_delete_task);
                menu.add(Menu.NONE, 24, 4, R.string.task_context_menu_start_in_point);
            } else if (3 == this.page) {
                menu.add(Menu.NONE, 31, 1, R.string.starters_context_menu_load);
            }
        }
    }
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
            loadAllChallenges();
        } else if (id == R.id.nav_starters){
            loadAllStarters();
        } else if (id == R.id.nav_manage) {
            Intent intent = new Intent(getApplicationContext(),Settings.class);
            startActivity(intent);
        } else if (id == R.id.nav_info) {
            Intent intent = new Intent(getApplicationContext(),About.class);
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
