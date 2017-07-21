package pl.deszczowy.slap;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.Switch;

public class Settings extends AppCompatActivity {

    private Switch switch_current;
    private boolean setting_start_from_current;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        this.switch_current = (Switch) findViewById(R.id.SwitchStartCurrent);
        this.switch_current.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked){
                Preferences pref = new Preferences(Settings.this);
                pref.setPreference(R.string.option_start_from_current_task_name, isChecked);
            }
        });

        readSettings();
        setValues();
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void readSettings(){
        Preferences pref = new Preferences(this);
        this.setting_start_from_current = pref.getPreference(R.string.option_start_from_current_task_name, R.bool.option_start_from_current_task_default);
    }

    private void setValues(){
        this.switch_current.setChecked(this.setting_start_from_current);
    }
}
