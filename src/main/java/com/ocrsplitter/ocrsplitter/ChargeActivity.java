package com.ocrsplitter.ocrsplitter;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionRequestInitializer;
import com.google.api.services.vision.v1.model.AnnotateImageRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.EntityAnnotation;
import com.google.api.services.vision.v1.model.Feature;
import com.google.api.services.vision.v1.model.Image;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;

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
        String data = uploadImage(Uri.fromFile(new File(HomeActivity.pictureSaveLoc)));

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

    protected ArrayList<JsonValue> extractItems(String s) {

        JsonReader reader = Json.createReader(new StringReader(s));

        JsonObject data = reader.readObject();

        reader.close();

        JsonArray text_data = data.getJsonArray("textAnnotation");

        ArrayList<JsonValue> words = new ArrayList();

        for (int i=0; i < text_data.size(); ++i) {
            words.add(text_data.get(i));
        }

        return words;
    }

    protected ArrayList<ArrayList<JsonValue>> selectLines(ArrayList<JsonValue> words) {
        ArrayList<ArrayList<JsonValue>> lines = new ArrayList();
        ArrayList<JsonValue> current_line = new ArrayList();

        ArrayList<JsonValue> words_done = new ArrayList();

        int current_line_y = 0;

        for (JsonValue primary_word : words) {

            boolean exclude = false;
            for (JsonValue check_word : words_done) {
                if (primary_word == check_word) {
                    exclude = true;
                    break;
                }
            }
            if (!exclude) {
                exclude = false;

                current_line.add(primary_word);
                words_done.add(primary_word);

                JsonArray vertexes = ((JsonObject)primary_word).getJsonObject("boudingPoly").getJsonArray("vertices");

                current_line_y = vertexes.getJsonObject(0).getInt("y");
                int current_line_height = vertexes.getJsonObject(0).getInt("y") - vertexes.getJsonObject(2).getInt("y");

                for (JsonValue secondary_word : words) {
                    for (JsonValue check_word : words_done) {
                        if (secondary_word == check_word) {
                            exclude = true;
                        }
                    }

                    if (!exclude) {
                        exclude = false;
                        int current_word_y = ((JsonObject)secondary_word).getJsonObject("boundingPoly").getJsonArray("vertices").getJsonObject(0).getInt("y");
                        if (Math.abs(current_word_y - current_line_y) <= current_line_height) {
                            current_line.add(secondary_word);
                            words_done.add(secondary_word);
                        }
                    }
                }
                lines.add(current_line);
                current_line.clear();
            }
        }
        return lines;
    }


    public ArrayList<ArrayList<String>> getTextData(String s) {
        ArrayList<ArrayList<JsonValue>> jtext = selectLines(extractItems(s));
        String [] money_vals = {".0", ".1", ".2", ".3", ".4", ".5", ".6", ".7", ".8", ".9"};
        ArrayList<ArrayList<String>> stext = new ArrayList();


        ArrayList<ArrayList<String>> items = new ArrayList();

        for(ArrayList<JsonValue> ar_list1 : jtext){
            ArrayList<String> tempitems = new ArrayList();
            for(JsonValue text_object : ar_list1) {
                String text = ((JsonObject)text_object).getString("description");
                tempitems.add(text);
            }
            stext.add(tempitems);
        }

        //Now check to see if they have dollar values
        for(ArrayList<String> slist: stext){
            for(String component: slist){
                for(String m_vals: money_vals){
                    if (component.contains(m_vals)){
                        items.add(slist);
                    }
                }
            }

        }

        return items;
    }



    protected ArrayList<ReceiptItem> getReceiptItems(String s) {

        ArrayList<ArrayList<String>> lines = getTextData(s);
        ArrayList<ReceiptItem> items = new ArrayList<>();

        for (ArrayList<String> line : lines) {
            String name = new String("");
            for (int i=0; i < line.size() - 1; ++i) {
                name += line.get(i) + " ";
            }
            ReceiptItem item = new ReceiptItem(name,Double.parseDouble(line.get(line.size()-1)));
            items.add(item);
        }

        /*
        items.add(new ReceiptItem("Item 1", 12.43));
        items.add(new ReceiptItem("Item 2", 42.13));
        items.add(new ReceiptItem("Item 3", 22.22));
        */
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

    //////////////////////////////////////////////////////////////////////////

    private static final String CLOUD_VISION_API_KEY = KeyClass.key;
    public String FILE_NAME = "temp.jpg";

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int GALLERY_IMAGE_REQUEST = 1;
    public static final int CAMERA_PERMISSIONS_REQUEST = 2;
    public static final int CAMERA_IMAGE_REQUEST = 3;

    public String uploadImage(Uri uri) {
        FILE_NAME = HomeActivity.pictureName;
        if (uri != null) {
            try {
                // scale the image to save on bandwidth
                Bitmap bitmap =
                        scaleBitmapDown(
                                MediaStore.Images.Media.getBitmap(getContentResolver(), uri),
                                1200);

                return callCloudVision(bitmap);

            } catch (IOException e) {
                System.out.println("Fail: " + e.getMessage());
            }
        } else {
            System.out.println( "Image picker gave us a null image.");
        }
        return "fail";
    }

    private String callCloudVision(final Bitmap bitmap) throws IOException {
        // Do the real work in an async task, because we need to use the network anyway
        new AsyncTask<Object, Void, String>() {
            @Override
            protected String doInBackground(Object... params) {
                try {
                    HttpTransport httpTransport = AndroidHttp.newCompatibleTransport();
                    JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

                    Vision.Builder builder = new Vision.Builder(httpTransport, jsonFactory, null);
                    builder.setVisionRequestInitializer(new
                            VisionRequestInitializer(CLOUD_VISION_API_KEY));
                    Vision vision = builder.build();

                    BatchAnnotateImagesRequest batchAnnotateImagesRequest =
                            new BatchAnnotateImagesRequest();
                    batchAnnotateImagesRequest.setRequests(new ArrayList<AnnotateImageRequest>() {{
                        AnnotateImageRequest annotateImageRequest = new AnnotateImageRequest();

                        // Add the image
                        Image base64EncodedImage = new Image();
                        // Convert the bitmap to a JPEG
                        // Just in case it's a format that Android understands but Cloud Vision
                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream);
                        byte[] imageBytes = byteArrayOutputStream.toByteArray();

                        // Base64 encode the JPEG
                        base64EncodedImage.encodeContent(imageBytes);
                        annotateImageRequest.setImage(base64EncodedImage);

                        // add the features we want
                        annotateImageRequest.setFeatures(new ArrayList<Feature>() {{
                            Feature labelDetection = new Feature();
                            labelDetection.setType("TEXT_DETECTION");
                            labelDetection.setMaxResults(100);
                            add(labelDetection);
                        }});

                        // Add the list of one thing to the request
                        add(annotateImageRequest);
                    }});

                    Vision.Images.Annotate annotateRequest =
                            vision.images().annotate(batchAnnotateImagesRequest);
                    // Due to a bug: requests to Vision API containing large images fail when GZipped.
                    annotateRequest.setDisableGZipContent(true);
                    Log.d(TAG, "created Cloud Vision request object, sending request");

                    BatchAnnotateImagesResponse response = annotateRequest.execute();

                    String converted = response.toPrettyString();
                    return converted;

                } catch (GoogleJsonResponseException e) {
                    Log.d(TAG, "failed to make API request because " + e.getContent());
                } catch (IOException e) {
                    Log.d(TAG, "failed to make API request because of other IOException " +
                            e.getMessage());
                }
                return "Cloud Vision API request failed. Check logs for details.";
            }
        }.execute();
        return "failed";
    }

    public Bitmap scaleBitmapDown(Bitmap bitmap, int maxDimension) {

        int originalWidth = bitmap.getWidth();
        int originalHeight = bitmap.getHeight();
        int resizedWidth = maxDimension;
        int resizedHeight = maxDimension;

        if (originalHeight > originalWidth) {
            resizedHeight = maxDimension;
            resizedWidth = (int) (resizedHeight * (float) originalWidth / (float) originalHeight);
        } else if (originalWidth > originalHeight) {
            resizedWidth = maxDimension;
            resizedHeight = (int) (resizedWidth * (float) originalHeight / (float) originalWidth);
        } else if (originalHeight == originalWidth) {
            resizedHeight = maxDimension;
            resizedWidth = maxDimension;
        }
        return Bitmap.createScaledBitmap(bitmap, resizedWidth, resizedHeight, false);
    }

    private String convertResponseToString(BatchAnnotateImagesResponse response) {
        String message = "I found these things:\n\n";

        List<EntityAnnotation> labels = response.getResponses().get(0).getLabelAnnotations();
        if (labels != null) {
            for (EntityAnnotation label : labels) {
                message += String.format("%.3f: %s", label.getScore(), label.getDescription());
                message += "\n";
            }
        } else {
            message += "nothing";
        }

        return message;
    }
}