package com.ocrsplitter.ocrsplitter;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

// Much taken from https://developer.android.com/training/camera/photobasics.html#TaskPhotoView
public class ChargeActivity extends AppCompatActivity {

    private static int CONTACT_PICKER_RESULT = 1;

    private ArrayList<ReceiptItem> items = new ArrayList<>();

    ArrayList<String> listItems = new ArrayList<String>();

    private ArrayAdapter<String> adapter;

    private ListView chargeList;
    private Button cancelButton;
    private Button okButton;

    // I'm sorry
    int column = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.charge_activity);

        chargeList = (ListView) findViewById(R.id.chargeListView);
        cancelButton = (Button) findViewById(R.id.cancelButton);
        okButton = (Button) findViewById(R.id.okButton);

        items = getReceiptItems("Dur dur dur");

        for (ReceiptItem item : items) {
            listItems.add(item.getName() + " - " + item.getPrice());
        }

        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listItems);
        chargeList.setAdapter(adapter);
        chargeList.setOnItemClickListener(itemClickedListener);
        chargeList.setOnItemLongClickListener(itemLongClickedListener);
    }

    protected ArrayList<ReceiptItem> getReceiptItems(String fileLoc) {

        ArrayList<ReceiptItem> items = new ArrayList<>();

        items.add(new ReceiptItem("Item 1", 12.43));
        items.add(new ReceiptItem("Item 2", 42.13));
        items.add(new ReceiptItem("Item 3", 22.22));

        return items;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(requestCode == CONTACT_PICKER_RESULT){
            if(resultCode== Activity.RESULT_OK){

                Uri uri = data.getData();
                ContentResolver contentResolver = getContentResolver();
                Cursor contentCursor = contentResolver.query(uri, null, null,null, null);

                if(contentCursor.moveToFirst()){
                    String id = contentCursor.getString(contentCursor.getColumnIndexOrThrow(ContactsContract.Contacts._ID));

                    String hasPhone =
                            contentCursor.getString(contentCursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));

                    if (hasPhone.equalsIgnoreCase("1"))
                    {
                        Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null,
                                ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = "+ id,null, null);
                        phones.moveToFirst();
                        String contactNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        String given = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                        phones.close();
                        
                        String name = given;

                        ReceiptItem item = items.get(column);

                        item.setOwnersName(name);
                        item.setPhoneNumber(contactNumber);

                        listItems.set(column, item.toString() + " - " + name);
                        adapter.notifyDataSetChanged();
                    }
                }
                super.onActivityResult(requestCode, resultCode, data);
            }
        }
    }

    private AdapterView.OnItemClickListener itemClickedListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            ReceiptItem item = items.get(position);

            Intent contactPickerIntent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
            startActivityForResult(contactPickerIntent, CONTACT_PICKER_RESULT);

            item.setPaid(false);

            column = position;
        }
    };

    private AdapterView.OnItemLongClickListener itemLongClickedListener = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            ReceiptItem item = items.get(position);
            item.setOwnersName("");
            item.setPhoneNumber("");
            item.setPaid(true);

            listItems.set(position, item.toString());
            adapter.notifyDataSetChanged();
            return true;
        }
    };
}