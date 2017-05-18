package com.samus.freya;

import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.print.PageRange;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintDocumentInfo;
import android.print.PrintManager;
import android.print.pdf.PrintedPdfDocument;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.os.CancellationSignal;
import android.support.v4.print.PrintHelper;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseArray;
import android.view.ContextMenu;
import android.view.ContextThemeWrapper;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.samus.freya.helper.DBHelper;
import com.samus.freya.helper.ViewPrintAdapter;
import com.samus.freya.model.Contact;
import com.samus.freya.model.ContactService;
import com.samus.freya.model.Day;
import com.samus.freya.model.Month;
import com.samus.freya.model.Service;
import com.samus.freya.model.SparseArrayAdapter;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Created by samus on 15.11.2016.
 */

public class MonthEditActivity extends AppCompatActivity {

    public static final String KEY_MONTH = "com.samus.freya.KEY_MONTH";

    private Toolbar toolbar;
    private int month_id;
    private DBHelper dbHelper;
    private Month month;
    private SparseArray<Contact> contacts; // TODO maybe make sorted?
    private SparseArray<Day> days;
    private GridView contact_grid;
    private TableLayout day_grid;
    private GridArrayAdapter contactAdapter;
    private Map<String, TextView> day_views;
    private SparseArray<Service> services;
    private SparseArray<Float> hours, req;
    private SparseArray<Boolean> full;
    private int mode;
    private String tab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.month_edit);
        dbHelper = new DBHelper(getApplicationContext());

        if (getIntent().hasExtra(KEY_MONTH)) {
            month_id = getIntent().getIntExtra(KEY_MONTH, -1);
        } else {
            throw new IllegalArgumentException("Activity cannot find  extras " + KEY_MONTH);
        }

        month = dbHelper.getMonth(month_id);
        hours = new SparseArray<>();
        req = new SparseArray<>();
        full = new SparseArray<>();

        toolbar = (Toolbar) findViewById(R.id.toolbar2);
        contact_grid = (GridView) findViewById(R.id.contact_grid);
        day_grid = (TableLayout) findViewById(R.id.day_grid);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("Dienst Planer: " + month.toString());

        contacts = dbHelper.getAllContactsForMonth(month_id, req);
        for (int u=0; u<contacts.size(); u++) hours.append(contacts.keyAt(u), 0.0f);
        days = dbHelper.getAllDaysForMonth(month_id);
        contactAdapter = new GridArrayAdapter(this, contacts, hours, req);
        contact_grid.setAdapter(contactAdapter);

        services = dbHelper.getAllServicesForMonth(month_id);
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        if (metrics.densityDpi < 260) tab = "\t";
        else tab = "\t\t";

        initiateDayOfWeek(); // set first row with the names for the day of the week
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_bar_spinner, menu);
        MenuItem item = menu.findItem(R.id.spinner);
        Spinner spinner = (Spinner) MenuItemCompat.getActionView(item);
        ArrayAdapter mAdapter = ArrayAdapter.createFromResource(getApplicationContext(), R.array.spinner_list_item_array, android.R.layout.simple_spinner_dropdown_item);
        mAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(mAdapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {

                // switch between regular view and vacation view
                initiateDayGrid((position+1)%2);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.print:
                // User chose the "Settings" item, show the app settings UI...
                printPDF();
                Toast.makeText(this, "Print", Toast.LENGTH_SHORT).show();
                return true;

            case R.id.vaction:
                // Lets user choose vaction
                showVacationDialog();
                return true;
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    public void printPDF() {

        // displays the print dialog
        PrintManager printManager = (PrintManager) getSystemService(PRINT_SERVICE);
        printManager.print("print_any_view_job_name", new ViewPrintAdapter(this, month_id), null);
    }

    private void showVacationDialog() {
        // displays the vacation dialog
        android.app.AlertDialog.Builder alert = new android.app.AlertDialog.Builder( new ContextThemeWrapper(this, android.R.style.Theme_Material_Light_Dialog));
        LayoutInflater inflater = this.getLayoutInflater();
        View view = inflater.inflate(R.layout.vacation_dialog, null);

        final Spinner conSpinner = (Spinner) view.findViewById(R.id.vacation_dialog_con);
        List<String> tmp = new ArrayList<>();
        for (int i=0; i<contacts.size(); i++) tmp.add(contacts.valueAt(i).getName());
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, tmp);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        conSpinner.setAdapter(adapter);
        final EditText daysEditText = (EditText) view.findViewById(R.id.vacation_dialog_days);

        alert.setView(view);
        alert.setTitle("Urlaub");
        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Contact con = contacts.valueAt(conSpinner.getSelectedItemPosition());
                List<Integer> tmp = new ArrayList<>();
                int urlaub_ind = -1;
                for (int l=0; l<services.size(); l++) {
                    if (services.valueAt(l).getSpe()) urlaub_ind = l;
                }
                if (urlaub_ind == -1) return;

                try {
                    for (String el :daysEditText.getText().toString().split(",")) {
                        if (el.contains("-")) {
                            if (el.indexOf("-") == 0) continue;
                            int start = Integer.valueOf(el.split("-")[0]);
                            int end = Integer.valueOf(el.split("-")[1]);
                            if (end < start || start < 1 || end > 31) continue;
                            for (int u=start; u<=end; u++) tmp.add(u);
                        }
                        else {
                            int k = Integer.valueOf(el);
                            if (k<1 || k>31) continue;
                            tmp.add(k);
                        }
                    }
                }
                catch (Exception ex) {
                    Toast.makeText(getApplicationContext(), "Falsche Eingabe", Toast.LENGTH_SHORT).show();
                    dialogInterface.dismiss();
                    return;
                }
                for (int day: tmp) {
                    asignService(con.getId(), services.valueAt(urlaub_ind).getId(), days.get(day-1).getId());
                }

                dialogInterface.dismiss();
                Toast.makeText(getApplicationContext(), "Urlaub für " + con.getName() + " hinzugefügt", Toast.LENGTH_SHORT).show();
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

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        Day day = dbHelper.getDay((int)v.getTag());
        menu.setHeaderTitle(""+day.getDate()+". Tag bearbeiten:"); // TODO richtiges popupmenu
        List<ContactService> css = dbHelper.getAllContactsForDay((int)v.getTag());
        for (ContactService cs : css) {
            Intent tmp = new Intent();
            tmp.putExtra("day", (int)v.getTag());
            tmp.putExtra("con", cs.contact_id);
            Contact test = contacts.get(cs.contact_id);
            menu.add(0, v.getId(), 0, "Dienst von " + test.getName() + " löschent").setIntent(tmp);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        Intent tmp = item.getIntent();
        int day = tmp.getIntExtra("day", -1);
        int con = tmp.getIntExtra("con", -1);
        removeService(con, day);
        Toast.makeText(this, "Dienst gelöscht", Toast.LENGTH_SHORT).show(); // TODO make TOAST messages with undo
        contactAdapter.notifyDataSetChanged();
        return true;
    }

    private void initiateDayOfWeek() {
        LinearLayout dayOfWeek = (LinearLayout) findViewById(R.id.day_of_week);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.width = 0;
        params.weight = 1;

        String[] dayNames = new String[] { "Sonntag", "Montag", "Dienstag", "Mittwoch",
                "Donnerstag", "Freitag", "Samstag" };

        Calendar cal = new GregorianCalendar((int)month.getYear(), (int)month.getMonth()-1, 1);
        int shift = cal.get(Calendar.DAY_OF_WEEK) + 6;

        for (int i=0; i<7; i++) {
            TextView textView = new TextView(this);
            textView.setText(dayNames[(i+shift)%7]);
            textView.setLayoutParams(params);
            textView.setGravity(Gravity.CENTER);
            dayOfWeek.addView(textView);
        }
    }

    private void initiateDayGrid(int mode) {
        this.mode = mode;

        LayoutInflater inflater = getLayoutInflater();
        day_views = new HashMap<String, TextView>();
        List<Integer> holidays = getHolidays(month.getYear(), month.getMonth()-1);
        Log.d("info", "ok "+ month.getYear() + " " + month.getMonth());
        day_grid.removeAllViews();
        for (int u=0; u<contacts.size(); u++) hours.setValueAt(u, 0.0f);

        TableLayout.LayoutParams rowParams = new TableLayout.LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT,
                TableLayout.LayoutParams.MATCH_PARENT);
        rowParams.height = 0;
        rowParams.weight = 1.0f;

        for (int i=0; i<5; i++) {
            TableRow tableRow = new TableRow(this);
            TableRow.LayoutParams layoutParams = new TableRow.LayoutParams(
                    0, TableRow.LayoutParams.MATCH_PARENT, 1f);
            layoutParams.topMargin = 2;
            layoutParams.bottomMargin = 2;
            layoutParams.rightMargin = 2;
            layoutParams.leftMargin = 2;

            for (int j=0; j<7; j++) {
                RelativeLayout view = (RelativeLayout) inflater.inflate(R.layout.day_tile, null);
                if (holidays.contains(i*7+j+1)) view.setBackgroundColor(Color.argb(0xa0,0xe7,0x4c, 0x3c));
                TextView textView = (TextView) view.findViewById(R.id.day_date);
                LinearLayout innerLayout = (LinearLayout) view.findViewById(R.id.day_services);
                //view.setLayoutParams(layoutParams);

                if (i*7+j<days.size()) {
                    view.setTag(days.get(i*7+j).getId()); // TODO make better without tag

                    registerForContextMenu(view);
                    textView.setText( String.valueOf(days.get(i*7+j).getDate()) );
                    List<ContactService> css = dbHelper.getAllContactsForDay(days.get(i*7+j).getId());

                    LinearLayout.LayoutParams innerParams = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f);

                    for (int u=0; u<services.size(); u++) {
                        Service ser = services.valueAt(u);
                        if (ser.getDef() == mode) {
                            TextView tmp = new TextView(this);
                            tmp.setMaxLines(2);
                            tmp.setSingleLine(false);
                            tmp.setEllipsize(TextUtils.TruncateAt.END);
                            tmp.setTextSize(13f);
                            String tmp_des = ser.getDesc()+":\t"; // TODO make Stringbuilder, only use Vorname S.
                            int count = 0;
                            for (ContactService cs : css){
                                if (cs.service_id == ser.getId()){
                                    if (count == 0) {
                                        tmp_des = tmp_des + contacts.get(cs.contact_id).getSName() + "\n"+tab;
                                        count++;
                                    }
                                    else
                                        tmp_des = tmp_des + contacts.get(cs.contact_id).getSName();
                                }
                            }
                            tmp.setText(tmp_des);
                            ContactService cs = new ContactService();
                            cs.service_id = ser.getId();
                            cs.contact_id = days.get(i*7+j).getId();
                            tmp.setTag(cs);
                            tmp.setOnDragListener(new MyDragListener());
                            tmp.setLayoutParams(innerParams);
                            //tmp.setBackgroundColor(Color.CYAN);
                            innerLayout.addView(tmp);
                            day_views.put(String.valueOf(cs.contact_id) + " " + String.valueOf(ser.getId()), tmp);
                        }
                    }
                    for (ContactService cs: css) {
                        if (services.get(cs.service_id).getSpe())
                            hours.put(cs.contact_id, hours.get(cs.contact_id)+ contacts.get(cs.contact_id).getWh()/5);
                        else
                            hours.put(cs.contact_id, hours.get(cs.contact_id)+services.get(cs.service_id).getVal());
                    }
                }
                else {
                    textView.setText( String.valueOf((i*7+j+1)%days.size()) );
                    view.setBackgroundColor(Color.LTGRAY);
                    textView.setBackgroundColor(Color.LTGRAY);
                    //layoutParams.weight = (35.f-days.size())/(7-35-days.size());
                    TableRow.LayoutParams layoutParams2 = new TableRow.LayoutParams(
                            0, TableRow.LayoutParams.MATCH_PARENT, 1f);
                    layoutParams2.topMargin = 2;
                    layoutParams2.bottomMargin = 2;
                    layoutParams2.rightMargin = 2;
                    layoutParams2.leftMargin = 2;
                    layoutParams2.weight = (35f-days.size());
                    view.setLayoutParams(layoutParams2);
                    tableRow.addView(view);
                    tableRow.setWeightSum(7.0f);

                    break;
                    //rowParams.
                    //view.setLayoutParams(layoutParams);
                    //tableRow.addView(view);
                    //break;
                }
                view.setLayoutParams(layoutParams);
                tableRow.addView(view);
            }

            tableRow.setLayoutParams(rowParams);
            day_grid.addView(tableRow);
        }
        contactAdapter.notifyDataSetChanged();
    }

    private List<Integer> getHolidays(int year, int month) { // Calculates the holidays in a given month, important for req calc
        List<Integer> holidays = new ArrayList<>();
        Calendar easterSunday; // Ostersonntag
        switch (year) {
            case 2016: easterSunday = new GregorianCalendar(year, 2, 27); break;
            case 2017: easterSunday = new GregorianCalendar(year, 3, 16); break;
            case 2018: easterSunday = new GregorianCalendar(year, 3, 1); break;
            case 2019: easterSunday = new GregorianCalendar(year, 3, 21); break;
            case 2020: easterSunday = new GregorianCalendar(year, 3, 12); break;
            case 2021: easterSunday = new GregorianCalendar(year, 3, 4); break;
            case 2022: easterSunday = new GregorianCalendar(year, 3, 17); break;
            default: easterSunday = new GregorianCalendar();
        }

        switch (month) { // could also be done with simple array but not so clearly visible
            case 0: // January
                holidays.add(1); holidays.add(6); break; // Neujahr, Heilige Drei Könige
            case 1: // February
                break; // keine fixen Feiertage
            case 2: // March
                break; // keine fixen Feiertage
            case 3: // April
                break; // keine fixen Feiertage
            case 4: // May
                holidays.add(1); break; // National Holiday, Staatsfeiertag
            case 5: // June
                break; // keine fixen Feiertage
            case 6: // July
                break; // keine fixen Feiertage
            case 7: // August
                holidays.add(15); break; // Maria Himmelfahrt
            case 8: // September
                break; // keine fixen Feiertage
            case 9: // October
                holidays.add(26); break; // National Holiday, Nationalfeiertag
            case 10: // November
                holidays.add(1); break;// Allerheiligen, (Leopold)
            case 11: // December
                holidays.add(8); holidays.add(24); holidays.add(25); holidays.add(26); holidays.add(31); break; // Maria Empfängnis, Heiliger Abend, Christtag, Stefanitag, Silvester
        }

        //easterSunday.add(Calendar.DAY_OF_MONTH, -2); // Karfreitag?
        //if (easterSunday.get(Calendar.MONTH) == month) count++;

        easterSunday.add(Calendar.DAY_OF_MONTH, 3); // Ostermontag
        if (easterSunday.get(Calendar.MONTH) == month) holidays.add(easterSunday.get(Calendar.DAY_OF_MONTH)-2);

        easterSunday.add(Calendar.DAY_OF_MONTH, 38); // Christi Himmelfahrt
        if (easterSunday.get(Calendar.MONTH) == month) holidays.add(easterSunday.get(Calendar.DAY_OF_MONTH)-2);

        easterSunday.add(Calendar.DAY_OF_MONTH, 11); // Pfingstmontag
        if (easterSunday.get(Calendar.MONTH) == month) holidays.add(easterSunday.get(Calendar.DAY_OF_MONTH)-2);

        easterSunday.add(Calendar.DAY_OF_MONTH, 10); // Fronleichnam
        if (easterSunday.get(Calendar.MONTH) == month) holidays.add(easterSunday.get(Calendar.DAY_OF_MONTH)-2);

        return holidays;
    }

    @Override
    public void onConfigurationChanged (Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        int orientation=newConfig.orientation;
        initiateDayGrid(mode); // TODO conserve mode for diff configurations

        switch(orientation) {

            case Configuration.ORIENTATION_LANDSCAPE:
                //contact_grid.setNumColumns(2);
                //setContentView();
                break;

            case Configuration.ORIENTATION_PORTRAIT:
                //contact_grid.setNumColumns((contacts.size()+1)/2);
                break;
        }
    }

    // Custom ArrayAdapter
    private class GridArrayAdapter extends SparseArrayAdapter<Contact> {

        private SparseArray<Float> hours, req;
        private final LayoutInflater mInflater;
        int[] flat_ui;

        // constructor, call on creation
        public GridArrayAdapter(Context context, SparseArray<Contact> data, SparseArray<Float> hours, SparseArray<Float> req) {
            mInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

            setData(data);
            this.hours = hours;
            this.req = req;
            flat_ui = context.getResources().getIntArray(R.array.flat_ui);
        }

        // called when rendering the list
        public View getView(final int position, View convertView, ViewGroup parent) {

            // get the contact we are displaying
            final Contact contact = getItem(position);

            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.contact_tile, null);
                //view.setOnTouchListener(new MyTouchListener()); // TODO need to better controll touch and scroll
                convertView.setOnLongClickListener(new MyTouchListener());
            }
            RelativeLayout day_inner_view = (RelativeLayout) convertView.findViewById(R.id.contact_inner_view);

            TextView textView = (TextView) convertView.findViewById(R.id.contact_grid_name);
            //day_inner_view.setBackgroundColor(flat_ui[position%flat_ui.length]);
            //GradientDrawable bgShape = (GradientDrawable)day_inner_view.getBackground();
            //bgShape.setColor(flat_ui[position%flat_ui.length]
            TextView wh = (TextView) convertView.findViewById(R.id.contact_grid_wh);
            float hour_tmp = hours.get(contact.getId());
            float req_tmp = req.get(contact.getId());
            wh.setText(String.format(Locale.US, "%.1f", hour_tmp) + "/" + String.format(Locale.US, "%.1f", req_tmp));
            textView.setBackgroundColor(flat_ui[position%flat_ui.length]);
            textView.setAlpha(0.7f);
            if (hour_tmp >= req_tmp) {
                //day_inner_view.setBackgroundColor(Color.argb(0xA0,0x90, 0xEE, 0x80));
                wh.setTextColor(Color.argb(0xf0,0x90, 0xEE, 0x80));
                full.append(contact.getId(), true);
                if (full.size() == contacts.size())
                    if (month.getFull() == 0) {
                        month.setFull(1);
                        dbHelper.updateMonth(month);
                    }
            }
            else {
                //day_inner_view.setBackgroundColor(Color.WHITE);
                wh.setTextColor(Color.BLACK);
                full.remove(contact.getId());
                if (full.size() < contacts.size() && month.getFull() == 1) {
                    month.setFull(0);
                    dbHelper.updateMonth(month);
                }
            }

            ProgressBar progressBar = (ProgressBar) convertView.findViewById(R.id.contact_grid_val);
            progressBar.setProgress((int)(hour_tmp/req_tmp*100));
            textView.setText(contact.getSName());
            convertView.setTag(contact.getId());
            return convertView;
        }

    }

    private final class MyTouchListener implements View.OnLongClickListener {

        @Override
        public boolean onLongClick(View view) {
            ClipData data = ClipData.newPlainText("", "");
            // TODO make smaller drag icon with simple color?
            View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(
                    view);

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                view.startDragAndDrop(data,shadowBuilder, view, 0);
            } else {
                view.startDrag(data, shadowBuilder, view, 0);
            }
            return true;
        }
    }

    class MyDragListener implements View.OnDragListener {

        @Override
        public boolean onDrag(View v, DragEvent event) {
            switch (event.getAction()){
                case DragEvent.ACTION_DRAG_STARTED:
                    break;

                case DragEvent.ACTION_DRAG_ENTERED:
                    break;

                case DragEvent.ACTION_DRAG_EXITED:
                    break;

                case DragEvent.ACTION_DROP:
                    View a = (View) event.getLocalState();
                    if (a instanceof RelativeLayout && a.getId() == R.id.contact_main) {
                        ContactService tmp = (ContactService) v.getTag();
                        int cur_con = (int)a.getTag(); // TODO change the longs to ints or smth
                        int contact_id = contacts.get(cur_con).getId();

                        asignService(contact_id, tmp.service_id, tmp.contact_id); //tmp.contact_id is day id
                    }

                    break;

                case DragEvent.ACTION_DRAG_ENDED:
                    break;

                default:
                    break;
            }
            return true;
        }
    }

    /**
     * Handle the service to day assignment, prob only use for just regular services
     * @param contact_id
     * @param service_id
     * @param day_id
     */
    public void asignService(int contact_id, int service_id, int day_id) {
        List<ContactService> tmp = dbHelper.getAllContactsForDay(day_id);
        int service_count = 0;
        boolean has_already = false;

        for (ContactService cs : tmp) {
            if (contact_id == cs.contact_id) {  // Already has a service this day, just switch?
                has_already = true;
            }
            if (cs.service_id == service_id) {  // This service already has at least one
                service_count++;
                if (service_count == 2) {       // More than 2 not allowed, do nothing
                    return;
                }
            }
        }

        if (has_already) { // delete old instance
            for(ContactService cs : tmp) {
                if (contact_id == cs.contact_id) {
                    removeService(contact_id, day_id);
                }
            }
        }

        Float ho = hours.get(contact_id);
        if (services.get(service_id).getSpe())
            hours.put(contact_id, ho + contacts.get(contact_id).getWh()/5);
        else
            hours.put(contact_id, ho +services.get(service_id).getVal());
        contactAdapter.notifyDataSetChanged();
        dbHelper.insertDS(day_id, service_id, contact_id);

        TextView dis = day_views.get(String.valueOf(day_id) + " " + String.valueOf(service_id));
        if (dis == null) return;
        if (service_count == 1) {
            for(ContactService cs: tmp)
                if (cs.service_id == service_id)
                    dis.setText(services.get(service_id).getDesc() + ":\t" + contacts.get(cs.contact_id).getSName()
                            + "\n" + tab + contacts.get(contact_id).getSName());
        }
        else {
            dis.setText(services.get(service_id).getDesc() + ": " + contacts.get(contact_id).getSName());
        }

    }

    /**
     * Handle the service to day assignment, prob only use for just regular services
     * @param contact_id
     * @param day_id
     */
    public void removeService(int contact_id, int day_id) {
        List<ContactService> tmp = dbHelper.getAllContactsForDay(day_id);
        dbHelper.deleteDS(day_id, contact_id);
        int service_id = 0;

        for (ContactService cs : tmp) {
            if (cs.contact_id == contact_id) {
                service_id = (int)cs.service_id;
            }
        }
        TextView dis = day_views.get(String.valueOf(day_id) + " " + String.valueOf(service_id));

        float ho = hours.get(contact_id);
        if (services.get(service_id).getSpe())
            hours.put(contact_id, ho-contacts.get(contact_id).getWh()/5);
        else
            hours.put(contact_id, ho-services.get(service_id).getVal());

        for (ContactService cs : tmp) {
            if (cs.service_id == service_id && cs.contact_id != contact_id) {
                dis.setText(services.get(service_id).getDesc() + ":\t" + contacts.get(cs.contact_id).getSName());
                return;     // TODO maybe make cleaner with counter and one routine
            }
        }

        if (dis == null) return;
        dis.setText(services.get(service_id).getDesc() + ":\t");
    }
}
