package com.samus.freya.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.samus.freya.MonthEditActivity;
import com.samus.freya.R;
import com.samus.freya.helper.DBHelper;
import com.samus.freya.model.Month;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by samus on 09.11.2016.
 * Fragment used to display months and remove them
 */

public class FragmentMonths extends Fragment {

    private DBHelper dbHelper; // handles the database transactions
    private ListView listMonth; // view which contains all months
    private ArrayAdapter<Month> adapter; // adapter for month objects
    private List<Month> values; // data set for the adapter

    // Required empty constructor
    public FragmentMonths () { }

    // in the create cycle we get the dbhelper object
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbHelper = new DBHelper(getActivity());
    }

    // in the create-view cycle we set the adapter with data and add listener
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_months, container, false);
        listMonth = (ListView) v.findViewById(R.id.list_months);

        values = new ArrayList<Month>();
        adapter = new MonthArrayAdapter(getActivity(), 0, values);
        listMonth.setAdapter(adapter);
        updateView();
        // Delete Month
        listMonth.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                createAndShowAlertDialog(i);
                return true;
            }
        });
        // Edit Month
        listMonth.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(getContext(), MonthEditActivity.class);
                intent.putExtra(MonthEditActivity.KEY_MONTH, values.get(i).getId());
                startActivity(intent);
            }
        });
        return v;
    }

    // update the fragment
    public void updateView() {
        if (values != null) { // show always be true
            values.clear(); // clear data
            values.addAll(dbHelper.getAllMonths()); // get data
            Collections.sort(values, new Comparator<Month>() {
                @Override
                public int compare(Month m1, Month m2) { // sort data
                    return (int) (long) ((m2.getYear() - m1.getYear()) * 12 + m2.getMonth() - m1.getMonth());
                }
            });
        }
        adapter.notifyDataSetChanged(); // notify the adapter
    }

    // display remove month dialog
    private void createAndShowAlertDialog(int pos) {
        final int p = pos;

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(values.get(pos).toString() + " entfernen?");
        builder.setPositiveButton("Ja", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dbHelper.deleteMonth(values.get(p).getId());
                updateView();
                dialog.dismiss();
            }
        });

        builder.setNegativeButton("Nein", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    // Custom ArrayAdapter
    private class MonthArrayAdapter extends ArrayAdapter<Month> {

        private List<Month> months;
        private final LayoutInflater mInflater;

        // constructor, call on creation
        public MonthArrayAdapter(Context context, int resource, List<Month> objects) {
            super(context, resource, objects);
            mInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

            this.months = objects;
        }

        // called when rendering the list
        public View getView(final int position, View convertView, ViewGroup parent) {

            // get the contact we are displaying
            final Month month = months.get(position);
            if (convertView == null) // can we use the old one?
                convertView = mInflater.inflate(R.layout.month_listitem, null);

            // set month name
            TextView name = (TextView) convertView.findViewById(R.id.month_date);
            name.setText(month.toString());
            if (month.getFull() == 1)
                convertView.setBackgroundColor(Color.argb(0x80,0x90, 0xEE, 0x80));

            return convertView;
        }
    }
}
