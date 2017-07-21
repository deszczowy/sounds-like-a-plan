package pl.deszczowy.slap;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.res.ResourcesCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import pl.deszczowy.slap.ItemTask;
import pl.deszczowy.slap.R;

class ItemTaskAdapter extends BaseAdapter {

    private Context context;
    private List<ItemTask> list;

    ItemTaskAdapter (Context context, List<ItemTask> list) {
        this.list = list;
        this.context = context;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public ItemTask getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final View view = View.inflate(this.context, R.layout.item_task, null);
        final TextView labelTask= (TextView)view.findViewById(R.id.task_name);
        final TextView labelSerie= (TextView)view.findViewById(R.id.task_series);

        ItemTask item = getItem(position);
        labelTask.setText(item.getName());
        labelSerie.setText(item.getSerie());

        int color;
        if (0 == item.getStatus()){
            if (item.getIsCurrent()){
                color = ResourcesCompat.getColor(this.context.getResources(), R.color.PrimaryLight, null);
            }else{
                color = ResourcesCompat.getColor(this.context.getResources(), R.color.TertiaryLight, null);
            }
        }else{
            color = ResourcesCompat.getColor(this.context.getResources(), R.color.ComplementLight, null);
        }
        labelTask.setTextColor(color);
        labelSerie.setTextColor(color);
        return view;
    }
}
