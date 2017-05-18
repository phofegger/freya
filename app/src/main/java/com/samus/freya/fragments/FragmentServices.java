package com.samus.freya.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.SparseArray;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.samus.freya.R;
import com.samus.freya.helper.DBHelper;
import com.samus.freya.model.Service;
import com.samus.freya.model.SparseArrayAdapter;

/**
 * Created by samus on 20.11.2016.
 * Fragment used to display services and modifying them
 */

public class FragmentServices extends Fragment {

    private DBHelper dbHelper; // handles the database transactions
    private ListView listServices; // view which contains all services
    private ServiceArrayAdapter serviceArrayAdapter; // adapter for service objects
    private SparseArray<Service> services; // data set for the adapter

    // Required empty constructor
    public FragmentServices() { }

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
        View v = inflater.inflate(R.layout.fragment_services, container, false);
        listServices = (ListView) v.findViewById(R.id.listview_services);

        services = dbHelper.getAllServices();
        serviceArrayAdapter = new ServiceArrayAdapter(getActivity(), services);
        listServices.setAdapter(serviceArrayAdapter);

        return v;
    }

    // update the fragment
    public void updateView() {
        serviceArrayAdapter.setData(dbHelper.getAllServices());
        serviceArrayAdapter.notifyDataSetChanged();
    }

    // display the update service dialog
    private void updateService(final Service service) {
        AlertDialog.Builder alert = new AlertDialog.Builder( new ContextThemeWrapper(getActivity(), android.R.style.Theme_Material_Light_Dialog));
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.service_dialog, null);

        // get dialog elements
        final EditText serviceDesc = (EditText) view.findViewById(R.id.service_dialog_desc);
        final EditText serviceVal = (EditText) view.findViewById(R.id.service_dialog_val);
        final CheckBox serviceMode = (CheckBox) view.findViewById(R.id.service_dialog_mode);
        serviceDesc.setText(service.getDesc());
        serviceVal.setText(String.valueOf(service.getVal()));
        serviceMode.setChecked(service.getDef() == 1);

        // set dialog controls
        alert.setView(view);
        alert.setTitle("Dienst ändern");
        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                service.setDesc(serviceDesc.getText().toString());
                service.setDef(serviceMode.isChecked() ? 1 : 0);
                try {
                    service.setVal(Float.valueOf(serviceVal.getText().toString()));
                }
                catch (Exception ex) {
                    service.setVal(0.0f);
                }

                dbHelper.updateService(service);
                services.put(service.getId(), service);
                updateView();
            }
        });
        alert.setNegativeButton("Abbrechen", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        alert.show(); // show dialog
    }

    // display the delete dialog
    private void deleteService(final Service ser) {

        AlertDialog.Builder alert = new AlertDialog.Builder( new ContextThemeWrapper(getActivity(), android.R.style.Theme_Material_Light_Dialog));
        alert.setTitle(ser.getDesc() + " entfernen?");
        alert.setPositiveButton("Ja", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                ser.setEna(0);
                dbHelper.updateService(ser);
                services.delete(ser.getId());
                updateView();
                dialog.dismiss();
            }
        });

        alert.setNegativeButton("Nein", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = alert.create();
        dialog.show();
    }

    // Custom ArrayAdapter
    private class ServiceArrayAdapter extends SparseArrayAdapter<Service> {
        private final LayoutInflater mInflater;

        // constructor, call on creation
        public ServiceArrayAdapter(Context context, SparseArray<Service> data) {
            mInflater = LayoutInflater.from(context);

            setData(data);
        }

        // called when rendering the list
        public View getView(final int position, View convertView, ViewGroup parent) {

            // get the contact we are displaying
            final Service service = getItem(position);

            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.service_listitem, null);
                if (!service.getSpe()) {
                    convertView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            updateService(service);
                        }
                    });
                    convertView.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View view) {
                            deleteService(service);
                            return true;
                        }
                    });
                }
            }

            TextView name = (TextView) convertView.findViewById(R.id.service_name);
            TextView val = (TextView) convertView.findViewById(R.id.service_val);
            CheckBox check = (CheckBox) convertView.findViewById(R.id.service_mode);

            name.setText(service.getDesc());
            if (service.getSpe()) val.setText("*");
            else val.setText(String.valueOf(service.getVal()));
            if (service.getDef() == 1) check.setChecked(true);
            else check.setChecked(false);
            check.setClickable(false);
            return convertView;
        }
    }
}
