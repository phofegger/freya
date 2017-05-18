package com.samus.freya.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.SparseArray;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.samus.freya.R;
import com.samus.freya.helper.DBHelper;
import com.samus.freya.model.Contact;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;


/**
 * Created by samus on 09.11.2016.
 * Fragment used to display contacts and modifying them
 */

public class FragmentContact extends Fragment {

    private DBHelper dbHelper; // handles the database transactions
    private ListView listContact; // view which contains all contacts
    private ContactArrayAdapter adapter; // adapter for contact objects
    private List<Contact> values; // data set for the adapter

    // Required empty constructor
    public FragmentContact () { }

    // in the create cycle we get the dbhelper object
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbHelper = new DBHelper(getActivity());
    }

    // in the create-view cycle we get the data and set the adapter
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_contact, container, false);
        listContact = (ListView) v.findViewById(R.id.list_contact);

        SparseArray<Contact> tmp = dbHelper.getAllContacts();
        values = asList(tmp);
        adapter = new ContactArrayAdapter(getActivity(), 0, values);
        listContact.setAdapter(adapter);
        listContact.setItemsCanFocus(true);

        return v;
    }

    // display remove dialog
    private void deleteContact(int pos) {
        final int p = pos;

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(values.get(pos).getName() + " entfernen?");
        builder.setPositiveButton("Ja", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Contact tmp = values.get(p);
                tmp.setEnabled(0);
                dbHelper.updateContact(tmp);
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

    // display update dialog
    private void updateContact(int contact_id) {
        android.app.AlertDialog.Builder alert = new android.app.AlertDialog.Builder( new ContextThemeWrapper(getActivity(), android.R.style.Theme_Material_Light_Dialog));
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.contact_dialog, null);

        // get the contact and dialog elements
        final Contact con = dbHelper.getContact(contact_id);
        final EditText contactDesc = (EditText) view.findViewById(R.id.contact_dialog_name);
        final EditText contactWh = (EditText) view.findViewById(R.id.contact_dialog_wh);
        contactDesc.setText(con.getName());
        contactWh.setText(String.valueOf(con.getWh()));

        alert.setView(view); // set the view
        alert.setTitle(con.getName() + " bearbeiten"); // set dialog title
        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                con.setName(contactDesc.getText().toString());

                try { con.setWh(Float.valueOf(contactWh.getText().toString())); }
                catch (Exception ex) { con.setWh(0.0f); }

                dbHelper.updateContact(con);
                updateView();
                dialogInterface.dismiss();
            }
        });
        alert.setNegativeButton("Abbrechen", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        alert.show();
    }

    // updates the fragment
    public void updateView() {
        if (values != null) { // should be always true
            SparseArray<Contact> tmp = dbHelper.getAllContacts(); // get update data
            values.clear(); // clear current data set
            values.addAll(asList(tmp)); // set the new data
            Collections.sort(values, new Comparator<Contact>() { // and order it
                @Override
                public int compare(Contact contact, Contact t1) {
                    if (contact.getName().split(" ").length==2 && t1.getName().split(" ").length==2)
                        return contact.getName().split(" ")[1].compareTo(t1.getName().split(" ")[1]);
                    else
                        return contact.getName().compareTo(t1.getName());
                }
            });
            adapter.notifyDataSetChanged(); // finally notify the adapter
        }
    }

    // converts a sparsearray to list which we can order easier
    private <C> List<C> asList(SparseArray<C> sparseArray) {
        if (sparseArray == null) return null;
        List<C> arrayList = new ArrayList<>(sparseArray.size());
        for (int i = 0; i < sparseArray.size(); i++)
            arrayList.add(sparseArray.valueAt(i));
        return arrayList;
    }

    // Custom ArrayAdapter
    private class ContactArrayAdapter extends ArrayAdapter<Contact> {

        private final LayoutInflater mInflater;
        int[] flat_ui; // flat_ui color array used for better disction of names

        // constructor, call on creation
        private ContactArrayAdapter(Context context, int resource, List<Contact> data) {
            super(context, resource, data);
            mInflater = LayoutInflater.from(context);
            flat_ui = context.getResources().getIntArray(R.array.flat_ui);
        }

        // called when rendering the list
        public View getView(final int position, View convertView, ViewGroup parent) {

            // get the contact we are displaying
            final Contact contact = getItem(position);

            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.contact_listitem, null);
            }
            convertView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    deleteContact(position);
                    return true;
                }
            });
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    updateContact(contact.getId());
                }
            });

            // set color and name of the contact item
            View box = convertView.findViewById(R.id.contact_color);
            box.setBackgroundColor(flat_ui[position % flat_ui.length]);
            TextView name = (TextView) convertView.findViewById(R.id.contact_name);
            name.setText(contact.getName());

            // set wh of the contact item
            TextView wh = (TextView) convertView.findViewById(R.id.contact_wh);
            wh.setText(String.format(Locale.ENGLISH, "%.1f", contact.getWh()));

            return convertView;
        }
    }
}