package com.samus.freya;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.SparseArray;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.List;

import com.samus.freya.R;
import com.samus.freya.fragments.FragmentMonths;
import com.samus.freya.fragments.FragmentContact;
import com.samus.freya.fragments.FragmentServices;
import com.samus.freya.helper.DBHelper;
import com.samus.freya.model.Contact;
import com.samus.freya.model.Day;
import com.samus.freya.model.Month;
import com.samus.freya.model.Service;

public class MainActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private FragmentContact fragmentContact;
    private FragmentMonths fragmentMonths;
    private FragmentServices fragmentServices;
    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.floatingActionButton);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch(viewPager.getCurrentItem()) {
                    case 0:
                        MonthYearPickerDialog pd = new MonthYearPickerDialog();
                        pd.setListener(new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                                addMonth(i, i1);
                            }
                        });
                        pd.show(getSupportFragmentManager(), "Test");
                        break;

                    case 1:
                        addContact();
                        break;

                    case 2:
                        addService();
                        break;
                }

            }
        });

        dbHelper = new DBHelper(this);
        setUrlaub(0);
    }

    private void setUrlaub(int mode) {
        SharedPreferences sharedPreferences = getPreferences(Context.MODE_PRIVATE);
        int i = sharedPreferences.getInt("U", -1); // Check if the special case for urlaub has been added
        if(i == -1 || mode == 1) {
            Service urlaub = new Service();
            urlaub.setDesc("U");
            urlaub.setSpe(1);
            urlaub.setDef(0);
            urlaub.setVal(0.0f);
            dbHelper.insertService(urlaub);

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("U", 1);
            editor.commit();
        }
    }

    public void addContact() {
        android.app.AlertDialog.Builder alert = new android.app.AlertDialog.Builder( new ContextThemeWrapper(this, android.R.style.Theme_Material_Light_Dialog));
        LayoutInflater inflater = this.getLayoutInflater();
        View view = inflater.inflate(R.layout.contact_dialog, null);

        final EditText contactDesc = (EditText) view.findViewById(R.id.contact_dialog_name);
        final EditText contactWh = (EditText) view.findViewById(R.id.contact_dialog_wh);

        alert.setView(view);
        alert.setTitle("Kollegen hinzufügen");
        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Contact con = new Contact();
                con.setName(contactDesc.getText().toString());

                try {
                    con.setWh(Float.valueOf(contactWh.getText().toString()));
                }
                catch (Exception ex) {
                    con.setWh(0.0f);
                }

                dbHelper.insertContact(con);
                fragmentContact.updateView();
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

    public void addService() {
        android.app.AlertDialog.Builder alert = new android.app.AlertDialog.Builder( new ContextThemeWrapper(this, android.R.style.Theme_Material_Light_Dialog));
        LayoutInflater inflater = this.getLayoutInflater();
        View view = inflater.inflate(R.layout.service_dialog, null);

        final EditText serviceDesc = (EditText) view.findViewById(R.id.service_dialog_desc);
        final EditText serviceVal = (EditText) view.findViewById(R.id.service_dialog_val);
        final CheckBox serviceMode = (CheckBox) view.findViewById(R.id.service_dialog_mode);

        alert.setView(view);
        alert.setTitle("Dienst hinzufügen");
        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Service ser = new Service();
                ser.setDesc(serviceDesc.getText().toString());
                ser.setDef(serviceMode.isChecked() ? 1 : 0);

                try {
                    ser.setVal(Float.valueOf(serviceVal.getText().toString()));
                }
                catch (Exception ex) {
                    ser.setVal(0.0f);
                }

                dbHelper.insertService(ser);
                fragmentServices.updateView();
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

    public void addMonth(int year, int month) {
        Month mo = new Month();
        mo.setYear(year);
        month = month-1;
        mo.setMonth(month+1);
        int month_id = dbHelper.insertMonth(mo);

        SparseArray<Contact> contacts = dbHelper.getAllContacts();
        Calendar c1 = GregorianCalendar.getInstance();
        c1.set(year, month, 1);
        int days = c1.getActualMaximum(Calendar.DAY_OF_MONTH);

        int workdays = 0;
        for (int i=0; i<days; i++) {
            c1.set(year, month, i+1);
            int tmp = c1.get(Calendar.DAY_OF_WEEK);
            if ((tmp >= Calendar.MONDAY) && (tmp <= Calendar.FRIDAY)) workdays++;
        }

        int holidays = getHolidays(year, month);
        for (int u=0; u<contacts.size(); u++) {         // holiday reduce workhours needed per month
            Contact contact = contacts.valueAt(u);
            float tmp = contact.getWh()/5*(workdays-holidays);
            dbHelper.insertMC(month_id, contact.getId(), tmp);
        }

        for (int i=0; i<days; i++) {
            Day day = new Day();
            day.setMonth(month_id);
            day.setDate(i+1);
            dbHelper.insertDay(day);
        }

        SparseArray<Service> services = dbHelper.getAllServices();
        for (int i=0; i<services.size(); i++) {
            dbHelper.insertMS(month_id, services.valueAt(i).getId());
        }

        Intent intent = new Intent(this, MonthEditActivity.class);
        intent.putExtra(MonthEditActivity.KEY_MONTH, month_id);
        startActivity(intent);
    }

    private int getHolidays(int year, int month) { // Calculates the holidays in a given month, important for req calc
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

        return holidays.size();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();

        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final android.app.AlertDialog.Builder alert = new android.app.AlertDialog.Builder( new ContextThemeWrapper(this, android.R.style.Theme_Material_Light_Dialog));
        switch (item.getItemId()) {
            case R.id.action_settings:
                // User chose the "Settings" item, show the app settings UI...
                alert.setTitle("Programm komplett zurücksetzen?");
                alert.setPositiveButton("Ja", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dbHelper.resetDB();
                        setUrlaub(1);
                        fragmentMonths.updateView();
                        fragmentContact.updateView();
                        fragmentServices.updateView();
                        dialogInterface.dismiss();
                    }
                });
                alert.setNegativeButton("Nein", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                alert.show();
                return true;

            case R.id.action_help:
                // Display help message
                alert.setTitle("Hilfe");
                TextView textView = new TextView(this);
                textView.setText("Simple Dienstverwaltungsanwendung:\n" +
                        "Folgende Feiertage werden gezählt:\n" +
                        "\tNeujahr, Heiligen Drei Könige, Staatsfeiertag\n" +
                        "\tMaria Himmelfahrt, Nationalfeiertag, Allerheiligen\n" +
                        "\tMaria Empfängnis, Heiliger Abend, Christtag\n" +
                        "\tStefanitag, Silvester, Ostermontag\n" +
                        "\tChristi Himmelfahr, Pfingsmontag, Fronleichnam");
                alert.setView(textView);
                alert.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                alert.show();
                return true;

            case R.id.action_contacts:
                // Display backup insert dialog
                alert.setTitle("Aktuelles Pflege Team hinzufügen?");
                alert.setPositiveButton("Ja", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Contact contact = new Contact(-1, "Abel Petra", 38f, 1);
                        dbHelper.insertContact(contact);
                        contact = new Contact(-1, "Amesreiter Sabrina", 35f, 1);
                        dbHelper.insertContact(contact);
                        contact = new Contact(-1, "Ascher Patricia", 35f, 1);
                        dbHelper.insertContact(contact);
                        contact = new Contact(-1, "Dariz Sieglinde", 22f, 1);
                        dbHelper.insertContact(contact);
                        contact = new Contact(-1, "Lechner Josefa", 25f, 1);
                        dbHelper.insertContact(contact);
                        contact = new Contact(-1, "Meierhofer Paul", 30f, 1);
                        dbHelper.insertContact(contact);
                        contact = new Contact(-1, "Neugschwentner Nicole", 30f, 1);
                        dbHelper.insertContact(contact);
                        contact = new Contact(-1, "Schod Ingrid", 33f, 1);
                        dbHelper.insertContact(contact);
                        contact = new Contact(-1, "Semler Kristina", 25f, 1);
                        dbHelper.insertContact(contact);
                        contact = new Contact(-1, "Tomitsch Doris", 38f, 1);
                        dbHelper.insertContact(contact);
                        contact = new Contact(-1, "Edelmayer Sylvia", 30f, 1);
                        dbHelper.insertContact(contact);
                        contact = new Contact(-1, "Hofegger Michaela", 30f, 1);
                        dbHelper.insertContact(contact);
                        contact = new Contact(-1, "Maurer Renate", 30f, 1);
                        dbHelper.insertContact(contact);
                        contact = new Contact(-1, "Punzengruber Sandra", 30f, 1);
                        dbHelper.insertContact(contact);
                        fragmentContact.updateView();
                        dialogInterface.dismiss();
                    }
                });
                alert.setNegativeButton("Nein", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                alert.show();
                return true;

            case R.id.action_services:
                // Display backup insert dialog
                alert.setTitle("Aktuelle Dienste eintragen?");
                alert.setPositiveButton("Ja", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Service service = new Service(-1, "f8", 10.25f, 1, 0, 1);
                        dbHelper.insertService(service);
                        service = new Service(-1, "f1", 9f, 1, 0, 1);
                        dbHelper.insertService(service);
                        service = new Service(-1, "f4", 6f, 1, 0, 1);
                        dbHelper.insertService(service);
                        service = new Service(-1, "N", 12f, 1, 0, 1);
                        dbHelper.insertService(service);
                        service = new Service(-1, "fw", 0f, 0, 0, 1);
                        dbHelper.insertService(service);
                        fragmentServices.updateView();
                        dialogInterface.dismiss();
                    }
                });
                alert.setNegativeButton("Nein", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                alert.show();
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        fragmentContact = new FragmentContact();
        fragmentMonths = new FragmentMonths();
        fragmentServices = new FragmentServices();
        adapter.addFragment(fragmentMonths, "Monate");
        adapter.addFragment(fragmentContact, "Kollegen");
        adapter.addFragment(fragmentServices, "Dienste");
        viewPager.setAdapter(adapter);
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

    public static class MonthYearPickerDialog extends DialogFragment {

        private static final int MAX_YEAR = 2099;
        private DatePickerDialog.OnDateSetListener listener;
        private DBHelper dbHelper;

        public void setListener(DatePickerDialog.OnDateSetListener listener) {
            this.listener = listener;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            // Get the layout inflater
            LayoutInflater inflater = getActivity().getLayoutInflater();
            Calendar cal = Calendar.getInstance();
            dbHelper = new DBHelper(getActivity());

            View dialog = inflater.inflate(R.layout.date_picker_dialog, null);
            final NumberPicker monthPicker = (NumberPicker) dialog.findViewById(R.id.picker_month);
            final NumberPicker yearPicker = (NumberPicker) dialog.findViewById(R.id.picker_year);

            List<Month> months = dbHelper.getAllMonths();
            if (months.size() > 0) {
                Collections.sort(months, new Comparator<Month>() {
                    @Override
                    public int compare(Month m1, Month m2) {
                        return (int) (long) ((m2.getYear() - m1.getYear()) * 12 + m2.getMonth() - m1.getMonth());
                    }
                });
            } else {
                Month tmp = new Month();
                tmp.setMonth(cal.get(Calendar.MONTH)+1);
                tmp.setYear(cal.get(Calendar.YEAR));
                months.add(tmp);
            }

            monthPicker.setMinValue(1);
            monthPicker.setMaxValue(12);
            monthPicker.setValue((int)months.get(0).getMonth()+1);
            monthPicker.setDisplayedValues(Month.name);
            monthPicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);

            int year = cal.get(Calendar.YEAR);
            yearPicker.setMinValue(year);
            yearPicker.setMaxValue(year+5);
            yearPicker.setValue((int)months.get(0).getYear() + (months.get(0).getMonth() == 12 ? 1 : 0));
            yearPicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);

            builder.setView(dialog)
                    // Add action buttons
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            listener.onDateSet(null, yearPicker.getValue(), monthPicker.getValue(), 0);
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            MonthYearPickerDialog.this.getDialog().cancel();
                        }
                    });
            return builder.create();
        }
    }
}
