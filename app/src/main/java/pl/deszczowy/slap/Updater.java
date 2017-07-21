package pl.deszczowy.slap;

import android.database.sqlite.SQLiteDatabase;

class Updater{

    // starting scripts
    private static final String TABLE_META_DEF = "CREATE TABLE _meta (idMeta INTEGER, idChallenge INTEGER);";
    private static final String TABLE_CHALLENGE_DEF = "CREATE TABLE _challenge(idChallenge INTEGER PRIMARY KEY NOT NULL, name TEXT NOT NULL);";
    private static final String TABLE_SUBTASK_DEF = "CREATE TABLE _subtask(idSubtask INTEGER PRIMARY KEY NOT NULL, name TEXT NOT NULL, idTask INTEGER NOT NULL);";
    private static final String TABLE_TASK_DEF = "CREATE TABLE _task(idTask INTEGER PRIMARY KEY NOT NULL, name TEXT NOT NULL, idChallenge INTEGER NOT NULL, state INTEGER NOT NULL);";
    private static final String TABLE_STARTER_DEF = "CREATE TABLE _starter(idStarter INTEGER PRIMARY KEY NOT NULL, name TEXT NOT NULL, description TEXT NOT NULL, content TEXT NOT NULL, signature TEXT NOT NULL, status INTEGER NOT NULL)";

    private static final String INSERT_META = "INSERT INTO _meta (idMeta, idChallenge) VALUES (1, 0);";
    private static final String INSERT_STARTER_01 = "INSERT INTO _starter(name, description, signature, status, content) VALUES ('30 days plank', '', 'P30D', 0, 'H4sIAAAAAAAEAIWQTQrCMBBG94J3GHKCzOSnKW49gTcItouiRiF1UYJ3N2CpoRnIMi9vZr4ZA48pvOcRXncfbjAFUBIGv8Tj4ewXwCRIQhyvzzBE8Tn9KLFUJaFqqllqkriMcd7eNgltKqtjqUsC19Qb6xmG8g+ByYDY+KddSMwb0lpQaJqlpqBsd9sS8vaq7utY2u+ikuQ0woJyM4lagmoJ+Rq6HmxYavepO1ZzBWVn9g1B5WuYou8Xz7H+GPUCAAA=')";
    private static final String INSERT_STARTER_02 = "INSERT INTO _starter(name, description, signature, status, content) VALUES ('100 push-ups', '', 'PU100', 0, 'H4sIAAAAAAAEAL1Xy27bMBC8F+g/EDo3AN8UgSCHop/QHzBctQlqO4FdHwKj/16RFJc0tV7bBZSDCEvmkLszs0tJcM7ejofnh+Pbga2fV5vNsPs1fP70bfXOBPv5umePzJw62X3p1HjJ6dq+7F62xy1T3d80V566r/th9Tvfq1Ocr6f5qsLoPEc3mHGfMN9MOF1hTJ5jG4xr7vtTxFtkDZvn+AYj+GWQg0miRcmEclPIBkWpFtUmLcasvw+HP3DfZijaFJMslj2QgRtCGTuFbadrFjaiTD/OE3wCugo0PrxdGx9WEePQTxesIih14sYiGMlPF+AUKVDECJVjjwNAzfIqCTHKJHmizyPs9YRMPkc8I4sTQkV6Y8qXubpBqcScvsIcKlak22Sl4wBIup7SfhY0q9uG8MvLJaNcJmfv8EAcoVnKwAN3Gs0Aky1kLQVQV7cTKe4RLjhFSiDSogthwsmgsQwbp1+15PJKnUWWYGdZnxHyA+pMhnaoeOa/R/mXnBCuJI/wrwjhEtDBtj2a+g3CRfET/SIPsJAjhYsgD/A6dsVJ4SJKKcDX/UKp5YVToeJU8L+DwpnzTwlX2lMp1fLMo2pg9VfAxQQSBo5SeoOqNSmYdhgnwcw66BprSeMFSR3wVQrgyeqZRpPBSAF0CSAJ5VqN1D1W/w9SdDCKDvXmCLOTpPjssNKjqgQlus7Fgucgzbk+Z+vohZ2ig1PM+J8KW6sQSfrF0SBoUlQeGqKAFOr1sCLAwSA6rJdpvSwpJjjFmExKbHAmDxCEvHoW8DNSqgKYtXlDng9gtiJNcS6y2MKFZIJnbPaMjt8NPA/zIDB6IKNKaw3umWfUU/TE9x6bT6F0FMGz+duEXbiknhI38SM0WEebPNzJTaU2tAmEG+qdUMbG1J8ZuKIcvPkR3AjO2Wr3g21f90M4dKmm8mQ5W7+vNwORW/mWuSHof4JgacumEAAA')";
    // update scripts
    // private static final String UPDATE_version_counter = "...
    // private static final String UPDATE_03_01 = "...

    // Notices:
    // Starter pack should be updated with use of signatures.

    //
    private int oldVersion;
    private int newVersion;
    private SQLiteDatabase db;

    // api
    Updater(SQLiteDatabase db, int oldVersion, int newVersion){
        this.oldVersion = oldVersion;
        this.newVersion = newVersion;
        this.db = db;
    }

    Updater(SQLiteDatabase db){
        this.oldVersion = 0;
        this.newVersion = 0;
        this.db = db;
    }

    void update(){
        if (this.oldVersion < this.newVersion) {
            if (this.oldVersion < 2) {
                up02();
            }
            if (this.oldVersion < 3) {
                up03();
            }
        }
    }

    void create(){
        starter();
    }

    private void run(String query){
        this.db.execSQL(query);
    }

    // updaters
    private void starter(){
        // global app data
        run(TABLE_META_DEF);
        run(INSERT_META);
        // current challenges
        run(TABLE_CHALLENGE_DEF);
        run(TABLE_TASK_DEF);
        run(TABLE_SUBTASK_DEF);
        // starter pack
        run(TABLE_STARTER_DEF);
        run(INSERT_STARTER_01);
        run(INSERT_STARTER_02);
    }

    private void up02(){ // update to version 2
        run(TABLE_STARTER_DEF);
        run(INSERT_STARTER_01);
        run(INSERT_STARTER_02);
    }

    private void up03(){
        //run(UPDATE_03_01);
    }
}
