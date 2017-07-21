package pl.deszczowy.slap;

import java.util.ArrayList;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

class Database extends SQLiteOpenHelper {

    // database settings
    private static final String DATABASE_NAME = "TrackMeOne.db";
    private static final int DATABASE_VERSION = 2;

    // columns
    private static final String META_TABLE_NAME = "_meta";
    private static final String META_CHALLENGE_ID = "idChallenge";

    static final String STARTERS_COLUMN_ID = "idStarter";
    static final String STARTERS_COLUMN_NAME = "name";
    static final String STARTERS_COLUMN_CONTENT = "content";

    private static final String CHALLENGE_TABLE_NAME = "_challenge";
    static final String CHALLENGE_COLUMN_ID = "idChallenge";
    static final String CHALLENGE_COLUMN_NAME = "name";

    private static final String TASK_TABLE_NAME = "_task";
    static final String TASK_COLUMN_ID = "idTask";
    static final String TASK_COLUMN_NAME = "name";
    static final String TASK_COLUMN_STATE = "state";
    private static final String TASK_COLUMN_CHALLEGNE = "idChallenge";
    static final String TASK_COMPUTED_SUBTASKS = "subs";

    private static final String SUBTASK_TABLE_NAME = "_subtask";
    private static final String SUBTASK_COLUMN_ID = "idSubtask";
    private static final String SUBTASK_COLUMN_NAME = "name";
    private static final String SUBTASK_COLUMN_TASK = "idTask";

    // queries
    private static final String QUERY_ALL_CHALLENGES = "SELECT idChallenge, name FROM _challenge";
    //public static final String QUERY_ALL_TASKS = "SELECT (CASE WHEN state IS NOT NULL AND state = 1 THEN name || ' (done)' ELSE name  END) AS name, idTask FROM _task WHERE idChallenge = ?;";

    private static final String QUERY_ALL_TASKS =
            "SELECT a.state, a.name, GROUP_CONCAT(t.Name, ', ') AS subs, a.idTask FROM _task AS a LEFT JOIN _subtask AS t ON t.idTask = a.idTask " +
            "WHERE a.idChallenge = ?" +
            "GROUP BY a.idTask, a.name ;";

    private static final String QUERY_ALL_STARTERS = "SELECT idStarter, name FROM _starter ORDER BY idStarter";

    private static final String QUERY_CURRENT_CHALLENGE = "SELECT idChallenge, name FROM _challenge WHERE idChallenge = (SELECT idChallenge FROM _meta WHERE idMeta = 1)";
    private static final String QUERY_START_FROM_TASK = "UPDATE _task SET state = (case when idTask < ? then 1 else 0 end) WHERE idChallenge = (SELECT idChallenge FROM _meta WHERE idMeta = 1)";


    // automated queries
    private ArrayList<QueryCommand> autoQueries;
    private String autoMainPattern;
    private String autoSeriesPattern;

    // current challenge
    private long currentChallenge;
    private String currentChallengeName;

    Database(Context context) {
        super(context, DATABASE_NAME , null, DATABASE_VERSION);
        this.autoQueries = new ArrayList<>();
    }

    void currentChallengeRead(){
        this.currentChallenge = 0;
        this.currentChallengeName = "No challenge selected";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c =  db.rawQuery(QUERY_CURRENT_CHALLENGE, null );
        c.moveToFirst();
        if (c.getCount() > 0) {
            this.currentChallenge = c.getInt(c.getColumnIndex(Database.CHALLENGE_COLUMN_ID));
            if (this.currentChallenge > 0) {
                this.currentChallengeName = c.getString(c.getColumnIndex(Database.CHALLENGE_COLUMN_NAME));
            }
        }
        c.close();
    }

    void currentChallengeWrite(int challenge){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(META_CHALLENGE_ID, challenge);
        db.update(META_TABLE_NAME, contentValues, "idMeta = 1 ", null);
        this.currentChallenge = challenge;
    }

    long currentChallenge(){
        return this.currentChallenge;
    }

    String currentChallengeName(){
        return this.currentChallengeName;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Updater updater = new Updater(db);
        updater.create();
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Updater updater = new Updater(db, oldVersion, newVersion);
        updater.update();
    }

    long insertTask (String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(TASK_COLUMN_NAME, name);
        contentValues.put(TASK_COLUMN_CHALLEGNE, currentChallenge);
        contentValues.put(TASK_COLUMN_STATE, 0);
        return db.insert(TASK_TABLE_NAME, null, contentValues);
    }

    long insertChallenge (String name){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(CHALLENGE_COLUMN_NAME, name);
        long i = db.insert(CHALLENGE_TABLE_NAME, null, contentValues);

        String sql = "UPDATE _meta SET idChallenge = (SELECT idChallenge FROM _challenge ORDER BY idChallenge DESC LIMIT 1) WHERE idMeta = 1";
        db.execSQL(sql);
        currentChallengeRead();
        return i;
    }

    long insertSubtask (String name, Long task){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(SUBTASK_COLUMN_NAME, name);
        contentValues.put(SUBTASK_COLUMN_TASK, task);
        return db.insert(SUBTASK_TABLE_NAME, null, contentValues);
    }

    Cursor getData(long id) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery( "select a.name from _task as a where a.idTask = " + id + "", null );
    }

    /*
    public int numberOfRows(){
        SQLiteDatabase db = this.getReadableDatabase();
        int numRows = (int) DatabaseUtils.queryNumEntries(db, TASKS_TABLE_NAME);
        return numRows;
    }
    */

    boolean updateTask (Long id, Integer state) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(TASK_COLUMN_STATE, state);
        db.update(TASK_TABLE_NAME, contentValues, "idTask = ? ", new String[] { Long.toString(id) } );
        return true;
    }

    void startFromTask(long id){
        SQLiteDatabase db = this.getWritableDatabase();
        SQLiteStatement stmt = db.compileStatement(QUERY_START_FROM_TASK);
        stmt.bindLong(1, id);
        stmt.execute();
    }

    Integer deleteTask (Long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TASK_TABLE_NAME, "idTask = ? ", new String[] { Long.toString(id) });
    }

    Cursor getAllTasks() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(
                QUERY_ALL_TASKS,
                new String[] { Long.toString(this.currentChallenge) }
        );
    }

    Cursor getAllChallenges() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(QUERY_ALL_CHALLENGES, null);
    }

    Cursor getAllStarters() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(QUERY_ALL_STARTERS, null);
    }

    // automated queries
    private void autoInitialize(){
        this.autoMainPattern = "([\\w ]*)\\{([\\w\\d\", ]*)\\}";
        this.autoSeriesPattern = "\"([\\w\\d ]*)\"";
    }

    private void autoBuildQuery(String item){
        String task = item.replaceAll(this.autoMainPattern, "$1");
        String series = item.replaceAll(this.autoMainPattern, "$2");
        autoQueryAdd(new QueryCommand(QueryTag.TASK, task));

        Matcher m = Pattern.compile(this.autoSeriesPattern).matcher(series);
        while (m.find()) {
            autoQueryAdd(
                    new QueryCommand(
                            QueryTag.SERIES,
                            m.group().replaceAll(this.autoSeriesPattern, "$1")
                    )
            );
        }
    }

    private void autoQueryAdd(QueryCommand query){
        if (!query.getParam().equals("")) {
            this.autoQueries.add(query);
        }
    }

    private boolean autoGenerateQueries(String input){
        input = input.trim();
        if (input.equals("")) return false;

        int i = input.indexOf("\n");

        if (i >= 0){
            autoQueryAdd(new QueryCommand(QueryTag.CHALLENGE, input.substring(0, i)));
            input = input.substring(i);

            Matcher m = Pattern.compile(this.autoMainPattern).matcher(input);
            while (m.find()) {
                autoBuildQuery(m.group());
            }
            return true;
        }
        return false;
    }

    private boolean autoCommit(){
        SQLiteDatabase db = this.getWritableDatabase();
        long challenge = this.currentChallenge;
        long task = -1;
        boolean go = false; // this one is set when a challenge record is added. this record should be first.

        QueryCommand qc;

        if (0 == this.autoQueries.size()) return false;

        db.beginTransaction();

        try {
            for (int i = 0; i < this.autoQueries.size(); i++) {
                qc = this.autoQueries.get(i);

                switch (qc.getTag()) {
                    case CHALLENGE:
                        this.currentChallenge = insertChallenge(qc.getParam());
                        go = this.currentChallenge > 0; // new challange on empty database will have 1.
                        break;
                    case TASK:
                        if (go) {
                            task = insertTask(qc.getParam());
                            go = task > 0;
                        }
                        break;
                    case SERIES:
                        if (go && task > 0) { // go i set by challenge, series are for items, so it should be added now/
                            go = insertSubtask(qc.getParam(), task) > 0;
                        }
                        break;
                }

                // some entry did not pass
                if (!go) {
                    db.endTransaction();
                    break;
                }
            }
        }
        finally {
            // everything went ok
            if (go){
                db.setTransactionSuccessful();
                this.currentChallenge = challenge;
            }
        }

        //
        return go;
    }

    public boolean autoLoad(String input) {
        autoInitialize();
        return autoGenerateQueries(input) && autoCommit();
    }

    public String getStarter(long id){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cr = db.rawQuery( "select a.content from _starter as a where a.idStarter = " + id + "", null );
        cr.moveToFirst();
        return cr.getString(cr.getColumnIndex(STARTERS_COLUMN_CONTENT));
    }
}