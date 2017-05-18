package com.samus.freya.helper;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.print.PageRange;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintDocumentInfo;
import android.print.pdf.PrintedPdfDocument;
import android.text.TextPaint;
import android.util.SparseArray;
import android.view.View;

import com.samus.freya.model.Contact;
import com.samus.freya.model.ContactService;
import com.samus.freya.model.Day;
import com.samus.freya.model.Month;
import com.samus.freya.model.Service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

/**
 * Created by samus on 09.01.2017.
 */

public class ViewPrintAdapter extends PrintDocumentAdapter {

    private PrintedPdfDocument mDocument;
    private Context mContext;
    private Month month;
    private DBHelper dbHelper;
    private SparseArray<Contact> contacts;
    private List<Contact> contactsOrder;
    private SparseArray<Float> req, hours;
    private SparseArray<Day> days;
    private SparseArray<Service> services;
    private int maxContacts = 15; // max allowed contacts per page
    private int numPages;

    public ViewPrintAdapter(Context context, int month_id) {
        mContext = context;
        dbHelper = new DBHelper(context);
        month = dbHelper.getMonth(month_id);
        req = new SparseArray<>();
        contacts = dbHelper.getAllContactsForMonth(month_id, req);
        days = dbHelper.getAllDaysForMonth(month_id);
        services = dbHelper.getAllServicesForMonth(month_id);
        contactsOrder = asList(contacts);
        Collections.sort(contactsOrder, new Comparator<Contact>() {
            @Override
            public int compare(Contact contact, Contact t1) {
                if (contact.getName().split(" ").length==2 && t1.getName().split(" ").length==2)
                    return contact.getName().split(" ")[1].compareTo(t1.getName().split(" ")[1]);
                else
                    return contact.getName().compareTo(t1.getName());
            }
        });
        numPages = (contactsOrder.size()-1)/maxContacts+1;
        if (contacts.size() == 0) numPages=0; // no contacts
    }

    private <C> List<C> asList(SparseArray<C> sparseArray) {
        if (sparseArray == null) return null;
        List<C> arrayList = new ArrayList<>(sparseArray.size());
        for (int i = 0; i < sparseArray.size(); i++)
            arrayList.add(sparseArray.valueAt(i));
        return arrayList;
    }

    @Override
    public void onLayout(PrintAttributes oldAttributes, PrintAttributes newAttributes,
                         android.os.CancellationSignal cancellationSignal,
                         LayoutResultCallback callback, Bundle extras) {

        PrintAttributes pdfPrintAttrs = new PrintAttributes.Builder().
                setColorMode(PrintAttributes.COLOR_MODE_MONOCHROME).
                setMediaSize(PrintAttributes.MediaSize.ISO_A4.asLandscape()).
                setResolution(new PrintAttributes.Resolution("zooey", Activity.PRINT_SERVICE, 300, 300)).
                setMinMargins(PrintAttributes.Margins.NO_MARGINS).
                build();

        mDocument = new PrintedPdfDocument(mContext, pdfPrintAttrs);

        if (cancellationSignal.isCanceled()) {
            callback.onLayoutCancelled();
            return;
        }

        PrintDocumentInfo.Builder builder = new PrintDocumentInfo
                .Builder("print_output.pdf")
                .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                .setPageCount(numPages);

        PrintDocumentInfo info = builder.build();
        callback.onLayoutFinished(info, true);
    }

    @Override
    public void onWrite(PageRange[] pages, ParcelFileDescriptor destination,
                        android.os.CancellationSignal cancellationSignal,
                        WriteResultCallback callback) {

        int leftgap = 30;
        int rightgap = leftgap;
        int topgap = 120;
        int bottomgap = 20;
        int padding = 6;

        TextPaint textPaint = new TextPaint();
        textPaint.setColor(Color.BLACK);
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(1);
        Rect rect = new Rect();
        textPaint.getTextBounds("SDADSAS", 0, 5, rect);
        int textheight = rect.height();

        hours = new SparseArray<>();
        for (int i=0; i<contacts.size(); i++) hours.append(contacts.keyAt(i), 0f);

        for (int i = 0; i < numPages; i++) {
            // Start the page
            PdfDocument.Page page = mDocument.startPage(i);
            textPaint.setTextSize(12f);
            // get the page canvas and measure it.
            Canvas pageCanvas = page.getCanvas();
            int width = pageCanvas.getWidth();
            int height = pageCanvas.getHeight();
            int gap = (height-topgap-bottomgap)/maxContacts;
            int maxNameWidth = 0; // TODO set a max and use sname if too long or smaller font or two lines
            int contactsLeft = 0;

            // Name section
            pageCanvas.drawLine(leftgap, topgap-gap/2-textheight/2, width-rightgap, topgap-gap/2-textheight/2, paint);
            for (int l=0; l<maxContacts && i*maxContacts+l<contacts.size(); l++) {
                Contact contact = contactsOrder.get(i*maxContacts+l);
                String field = contact.getName() + ", " + contact.getWh();
                paint.getTextBounds(field, 0, field.length(), rect);
                if (rect.width() > maxNameWidth) maxNameWidth = rect.width();
                pageCanvas.drawText(contact.getName() + ", " + contact.getWh(),
                        leftgap+padding, topgap+l*gap, textPaint);
                pageCanvas.drawLine(leftgap, topgap+l*gap+gap/2-textheight/2, width-rightgap, topgap+l*gap+gap/2-textheight/2, paint);
                contactsLeft = l;
            }

            pageCanvas.drawLine(leftgap, topgap-gap/2-textheight/2, leftgap, topgap+contactsLeft*gap+gap/2-textheight/2, paint); // left line
            pageCanvas.drawLine(width-rightgap, topgap-gap/2-textheight/2, width-rightgap, topgap+contactsLeft*gap+gap/2-textheight/2, paint); // right line
            int curX = leftgap + maxNameWidth + 2*padding+2; // +2 is from width of line
            int curY = topgap-gap/2-textheight/2-2*textheight-2*padding;
            pageCanvas.drawLine(curX, curY, curX, topgap+contactsLeft*gap+gap/2-textheight/2, paint); // after name line

            // Req section
            paint.getTextBounds("100.0/100.0",0,11,rect);
            maxNameWidth = rect.width()+3;
            //pageCanvas.drawLine(width-rightgap-maxNameWidth-2*padding, curY, width-rightgap-maxNameWidth-2*padding, topgap+contactsLeft*gap+gap/2-textheight/2, paint); // left of req line

            // Day section
            int numDays = days.size();
            String[] dayNames = new String[] { "So", "Mo", "Di", "Mi",
                    "Do", "Fr", "Sa" };

            Calendar cal = new GregorianCalendar(month.getYear(), month.getMonth()-1, 1);
            int shift = cal.get(Calendar.DAY_OF_WEEK) + 6;

            int horizontal_gap = Math.round((1.0f*width-rightgap-maxNameWidth-2*padding-curX)/numDays);
            int dif = (horizontal_gap*numDays-(width-rightgap-maxNameWidth-2*padding-curX));
            pageCanvas.drawLine(curX, curY, curX+numDays*horizontal_gap, curY, paint); // over date line
            for (int l=0; l<numDays; l++) {
                Day day = days.get(l);
                if (day.getDate() > 9) pageCanvas.drawText(String.valueOf(day.getDate()), curX+l*horizontal_gap+padding/2, curY+padding/2+textheight, textPaint); // day number
                else pageCanvas.drawText(String.valueOf(day.getDate()), curX+l*horizontal_gap+padding, curY+padding/2+textheight, textPaint); // day number
                textPaint.getTextBounds(dayNames[(l+shift)%7], 0, dayNames[(l+shift)%7].length(), rect);
                int dateWidth = rect.width();
                pageCanvas.drawText(dayNames[(l+shift)%7], curX+l*horizontal_gap+(horizontal_gap-dateWidth)/2, curY+padding+2*textheight, textPaint); // day name
                pageCanvas.drawLine(curX+(l+1)*horizontal_gap, curY, curX+(l+1)*horizontal_gap, topgap+contactsLeft*gap+gap/2-textheight/2, paint); // inner lines
            }

            // Service Section
            int x0 = curX; // with horizonral_gap
            int y0 = topgap; // with gap
            for (int l=0; l<numDays; l++) {
                Day day = days.get(l);
                List<ContactService> css = dbHelper.getAllContactsForDay(day.getId());

                for (ContactService cs: css) {
                    int conInd = contactsOrder.indexOf(contacts.get(cs.contact_id));
                    if (conInd >= (i+1)*maxContacts || conInd < i*maxContacts) continue;
                    String servText = services.get(cs.service_id).getDesc();
                    textPaint.getTextBounds(servText, 0, servText.length(), rect);
                    int servWidth = rect.width();
                    pageCanvas.drawText(servText, x0+l*horizontal_gap+(horizontal_gap-servWidth)/2, y0+(conInd%maxContacts)*gap, textPaint);
                    if (services.get(cs.service_id).getSpe()) hours.append(cs.contact_id, hours.get(cs.contact_id) + contacts.get(cs.contact_id).getWh()/5);
                    else hours.append(cs.contact_id, hours.get(cs.contact_id) + services.get(cs.service_id).getVal());
                }
            }

            // additional req section
            for (int l=0; l<maxContacts && i*maxContacts+l<contacts.size(); l++) {
                float have = hours.get(contactsOrder.get(l).getId());
                float need = req.get(contactsOrder.get(l).getId());
                String reqText = String.format(Locale.US, "%.1f", have) + "/" + String.format(Locale.US, "%.1f", need);
                pageCanvas.drawText(reqText, width-rightgap-maxNameWidth-padding+dif, topgap+l*gap, textPaint);
            }

            textPaint.setTextSize(21f); // Title
            String monText = month.toString();
            textPaint.getTextBounds(monText, 0, monText.length(), rect);
            pageCanvas.drawText(monText, width/2-rect.width()/2, topgap/2, textPaint);

            mDocument.finishPage(page);
        }

        try {
            mDocument.writeTo(new FileOutputStream(
                    destination.getFileDescriptor()));
        } catch (IOException e) {
            callback.onWriteFailed(e.toString());
            return;
        } finally {
            mDocument.close();
            mDocument = null;
        }
        callback.onWriteFinished(new PageRange[]{new PageRange(0, numPages)});
    }
}