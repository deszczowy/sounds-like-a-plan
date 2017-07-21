package pl.deszczowy.slap;

import android.content.Context;
import android.content.SharedPreferences;

public class Preferences {

    private Context context;

    Preferences(Context context){
        if(context != null){
            this.context = context;
        }
    }

    void setPreference(int optionId, boolean value){
        SharedPreferences sharedPref = this.context.getSharedPreferences("SLAP", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(
                this.context.getText(optionId).toString(),
                value
        );
        editor.commit();
    }

    boolean getPreference(int optionId, int defaultValueId){
        SharedPreferences sharedPref = this.context.getSharedPreferences("SLAP", Context.MODE_PRIVATE);
        return sharedPref.getBoolean(
                this.context.getText(optionId).toString(),
                this.context.getResources().getBoolean(defaultValueId)
        );
    }
}
