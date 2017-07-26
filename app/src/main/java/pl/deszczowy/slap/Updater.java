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
    private static final String INSERT_STARTER_03 = "INSERT INTO _starter(name, description, signature, status, content) VALUES ('100 day song', '', 'DS100', 0, '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')";
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
        run(INSERT_STARTER_03);
    }

    private void up02(){ // update to version 2
        run(TABLE_STARTER_DEF);
        run(INSERT_STARTER_01);
        run(INSERT_STARTER_02);
    }

    private void up03(){ // update to version 3
        run(INSERT_STARTER_03);
    }
}
