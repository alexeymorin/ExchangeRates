package com.github.alexeymorin.exchangerates;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

class ValuteArrayAdapter extends ArrayAdapter<Valute> {

    private Context context;

    public ValuteArrayAdapter(Context context, List<Valute> valuteList) {
        super(context, -1, valuteList);
        this.context = context;
    }

    private static class ViewHolder {
        TextView currencyTextView;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, final ViewGroup parent) {
        final Valute valute = getItem(position);
        ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.list_item, parent, false);
            viewHolder.currencyTextView = (TextView) convertView.findViewById(R.id.currencyTextView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        };

        viewHolder.currencyTextView.setText(valute.nominal + " " + valute.name + " = " + valute.value + " " + getContext().getString(R.string.rub));
        viewHolder.currencyTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent grafIntent = new Intent(context, GraphActivity.class);
                grafIntent.putExtra("ID", valute.id);
                grafIntent.putExtra("CharCode", valute.charCode);
                context.startActivity(grafIntent);
            }
        });
        return convertView;
    }
}
