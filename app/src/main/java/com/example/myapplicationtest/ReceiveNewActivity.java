package com.example.myapplicationtest;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReceiveNewActivity extends AppCompatActivity implements View.OnClickListener {

    ArrayList<String> itemsList;
    ArrayList<Integer> itemIDList;
    final String LOG_TAG = "myLogs";
    DBHelper dbHelper;
    SQLiteDatabase db;
    ArrayAdapter<String> arrayAdapter;
    ListView listViewItems;
    String output, item = "", dbName, dbUser, dbPassword, areaDefault,
            requestUrlSyncReceive = "https://caiman.ru.com/php/syncReceive.php";
    SparseBooleanArray chosen;
    Integer iTmp = 0;
    final String SAVED_DBName = "dbName";
    final String SAVED_DBUser = "dbUser";
    final String SAVED_DBPassword = "dbPassword";
    final String SAVED_AREADEFAULT = "areaDefault";
    final String SAVED_LastReceiveDate = "lastReceiveDate";
    SharedPreferences sPrefAreaDefault, sPrefDBName, sPrefDBPassword, sPrefDBUser, sPrefLastReceiveDate;
    EditText editTextSetQuantity;
    Double quantity;
    List<DataReceive> dataReceive;
    Button btnSync;
    String[] receiveList;
    SharedPreferences.Editor e;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receive_new);

        Instant instant = Instant.now();
        ZoneId zoneId = ZoneId.of( "Asia/Sakhalin" );
        ZonedDateTime zdt = ZonedDateTime.ofInstant( instant , zoneId );
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern( "yyyy-MM-dd HH:mm:ss" );
        output = zdt.format( formatter );

        itemsList = new ArrayList<>();
        itemIDList = new ArrayList<>();
        dataReceive = new ArrayList<>();

        btnSync = findViewById(R.id.buttonSync);
        btnSync.setOnClickListener(this);

        sPrefDBName = getSharedPreferences(SAVED_DBName, Context.MODE_PRIVATE);
        sPrefDBUser = getSharedPreferences(SAVED_DBUser, Context.MODE_PRIVATE);
        sPrefDBPassword = getSharedPreferences(SAVED_DBPassword, Context.MODE_PRIVATE);
        sPrefAreaDefault = getSharedPreferences(SAVED_AREADEFAULT, Context.MODE_PRIVATE);
        sPrefLastReceiveDate = getSharedPreferences(SAVED_LastReceiveDate, Context.MODE_PRIVATE);
        dbName = sPrefDBName.getString(SAVED_DBName, "");
        dbUser = sPrefDBUser.getString(SAVED_DBUser, "");
        dbPassword = sPrefDBPassword.getString(SAVED_DBPassword, "");
        areaDefault = sPrefAreaDefault.getString(SAVED_AREADEFAULT, "");

        dbHelper = new DBHelper(this);
        db = dbHelper.getWritableDatabase();

        listViewItems = findViewById(R.id.listViewItems);
        editTextSetQuantity = findViewById(R.id.editTextSetQuantity);

        if (editTextSetQuantity.getText().toString().trim().length() > 0){
            quantity = Double.parseDouble(editTextSetQuantity.getText().toString());
        } else {
            quantity = 0d;
        }

        receiveItemsListFromLocalDB();

        listViewItems.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                item = ((TextView) view).getText().toString();
                chosen = listViewItems.getCheckedItemPositions();
                for (int i = 0; i < listViewItems.getCount(); i++) {
                    if (itemsList.get(i).equals(item) && chosen.get(i) == true) {
//                        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
//                        editTextSetQuantity.setShowSoftInputOnFocus(false);
                        iTmp = i;
                        createRow();
                    }
                    if (itemsList.get(i).equals(item) && chosen.get(i) == false) {
                        iTmp = i;
                        onSelectedItem();
                    }
                }
            }
        });

        onChangeListener();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonSync:
                onNextPressed();
                break;
            default:
                break;
        }
    }

    private void receiveItemsListFromLocalDB(){
        String sql = "SELECT ????????????????????????, ?????????????? FROM items WHERE ?????????????? NOT BETWEEN 18 AND 23";
        Cursor c = db.rawQuery(sql, null);
        if (c.moveToFirst()) {
            int itemNameTmp = c.getColumnIndex("????????????????????????");
            int itemID = c.getColumnIndex("??????????????");
            do {
                itemsList.add(c.getString(itemNameTmp));
                itemIDList.add(c.getInt(itemID));
            } while (c.moveToNext());
            onLoadActivity();
        } else {
            Log.d(LOG_TAG, "0 rows");
            Toast.makeText(getApplicationContext(), "????????????: CreateInvoiceChooseSalesPartner receiveDataFromLocalDB 001",
                    Toast.LENGTH_SHORT).show();
        }
        arrayAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_multiple_choice, itemsList);
        listViewItems.setAdapter(arrayAdapter);
        c.close();
    }

    boolean tableExists(SQLiteDatabase db, String tableName){
        if (tableName == null || db == null || !db.isOpen())
        {
            return false;
        }
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM sqlite_master WHERE type = ? AND name = ?", new String[] {"table", tableName});
        if (!cursor.moveToFirst())
        {
            cursor.close();
            return false;
        }
        int count = cursor.getInt(0);
        cursor.close();
        return count > 0;
    }

    boolean valueExists(SQLiteDatabase db, String tableName, String fieldName, String fieldValue){
        if (tableName == null || db == null || !db.isOpen())
        {
            return false;
        }
        String sql = "SELECT COUNT(*) FROM " + tableName + " WHERE " + fieldName + " LIKE ?";
        Cursor cursor = db.rawQuery(sql, new String[]{fieldValue});
        if (!cursor.moveToFirst())
        {
            cursor.close();
            return false;
        }
        int count = cursor.getInt(0);
        cursor.close();
        return count > 0;
    }

    boolean resultExists(SQLiteDatabase db, String tableName, String selectField){
        if (tableName == null || db == null || !db.isOpen())
        {
            return false;
        }
        String sql = "SELECT COUNT(?) FROM " + tableName;
        Cursor cursor = db.rawQuery(sql, new String[]{selectField});
        if (!cursor.moveToFirst())
        {
            cursor.close();
            return false;
        }
        int count = cursor.getInt(0);
        cursor.close();
        return count > 0;
    }

    private void deleteRowFromLocalTable(String tableName, String fieldName, String fieldValue){
        db = dbHelper.getWritableDatabase();
        if (tableExists(db, tableName)) {
            if (valueExists(db, tableName, fieldName, fieldValue)) {
                Log.d(LOG_TAG, "--- Clear raw in: " + tableName + " ---");
                int clearCount = db.delete(tableName, fieldName + " LIKE ?", new String[]{fieldValue});
                Log.d(LOG_TAG, "deleted rows count = " + clearCount);
                Toast.makeText(getApplicationContext(), "?????????????? ?????????????? ???? ????????????", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void onSelectedItem(){
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        if (tableExists(db, "receiveLocal")){
            if (valueExists(db, "receiveLocal", "itemID", String.valueOf(itemIDList.get(iTmp)))){
                builder.setTitle("????????????????????????: " + item)
                        .setMessage("?????????????? ?????? ???????????????????????????")
                        .setCancelable(false)
                        .setNegativeButton("??????????????",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        deleteRowFromLocalTable("receiveLocal", "itemID",
                                                String.valueOf(itemIDList.get(iTmp)));
                                        dialog.cancel();
                                    }
                                })
                        .setPositiveButton("??????????????????????????",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        editTextSetQuantity.requestFocus();
                                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,0);
                                        listViewItems.setItemChecked(iTmp, true);
                                        dialog.cancel();
                                    }
                                });
                AlertDialog alert = builder.create();
                alert.show();
            }
        }
    }

    private void createRow(){
        editTextSetQuantity.setText("");
        ContentValues cv = new ContentValues();
        Log.d(LOG_TAG, "--- Insert in receiveLocal: ---");
        cv.put("itemID", itemIDList.get(iTmp));
        cv.put("DateTimeDoc", output);
        cv.put("quantity", quantity);
        cv.put("agentID", Integer.parseInt(areaDefault));
        long rowID = db.insert("receiveLocal", null, cv);
        Log.d(LOG_TAG, "row inserted, ID = " + rowID);
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,0);
//        editTextSetQuantity.setShowSoftInputOnFocus(true);
        editTextSetQuantity.requestFocus();
//        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        e = sPrefLastReceiveDate.edit();
        e.putString(SAVED_LastReceiveDate, output);
        e.apply();
    }

    private void syncReceive(){
        dataReceive.clear();
        if (tableExists(db, "receiveLocal")) {
            if (valueExists(db, "receiveLocal", "itemID", String.valueOf(itemIDList.get(iTmp)))) {
                Cursor c = db.query("receiveLocal", null, null, null, null, null, null);
                if (c.moveToFirst()) {
                    int itemIDTmp = c.getColumnIndex("itemID");
                    int agentIDTmp = c.getColumnIndex("agentID");
                    int quantityTmp = c.getColumnIndex("quantity");
                    int dateTimeDocTmp = c.getColumnIndex("dateTimeDoc");
                    do {
                        Integer itemID = Integer.parseInt(c.getString(itemIDTmp));
                        Double quantity = Double.parseDouble(c.getString(quantityTmp));
                        Integer agentID = Integer.parseInt(c.getString(agentIDTmp));
                        String dateTimeDoc = c.getString(dateTimeDocTmp);

                        DataReceive dt = new DataReceive(itemID, agentID, dateTimeDoc, quantity);
                        dataReceive.add(dt);
                    } while (c.moveToNext());
                }
                c.close();
                sendToServer();
            }
        }
    }

    private void sendToServer(){
        Gson gson = new Gson();
        final String newDataArray = gson.toJson(dataReceive);
        StringRequest request = new StringRequest(Request.Method.POST,
                requestUrlSyncReceive, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try{
                    JSONArray jsonArray = new JSONArray(response);

                    String[] requestMessage = new String[jsonArray.length()];
                    String tmpStatus = "";

                    if (jsonArray.length() > 0){
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject obj = jsonArray.getJSONObject(i);
                            requestMessage[i] = obj.getString("requestMessage");
                            if (requestMessage[i].equals("New record created successfully")){
                                tmpStatus = "Yes";
                            } else {
                                tmpStatus = "No";
                            }
                        }
                        if (tmpStatus.equals("Yes")){
                            Toast.makeText(getApplicationContext(), "<<< ???????????? ???????????????????????????????? >>>", Toast.LENGTH_SHORT).show();
                            clearTable("receiveLocal");
                            editTextSetQuantity.setText("");
                            finish();
                        } else {
                            Toast.makeText(getApplicationContext(), "<<< ???????????? ???????????????????? >>>", Toast.LENGTH_SHORT).show();
                        }
                    }else{
                        Toast.makeText(getApplicationContext(), "???????????? ????????????????. ?????????????????? ???????????????? ?????? ????????????", Toast.LENGTH_SHORT).show();
                    }
                }
                catch (JSONException e1) {
                    e1.printStackTrace();
                }
                Log.d("response", "result: " + response);
            }
        }, new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error) {
                for (int i = 0; i < itemIDList.size(); i++) {
                    if (valueExists(db, "receiveLocal", "itemID", String.valueOf(itemIDList.get(i)))) {
                        listViewItems.setItemChecked(i, true);
                    }
                }
                Toast.makeText(getApplicationContext(), "?????? ???????????? ???? ??????????????", Toast.LENGTH_SHORT).show();
                Log.e("TAG", "Error " + error.getMessage());
            }
        }){
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> parameters = new HashMap<>();
                parameters.put("dbName", dbName);
                parameters.put("dbUser", dbUser);
                parameters.put("dbPassword", dbPassword);
                parameters.put("agentID", areaDefault);
                parameters.put("array", newDataArray);
                return parameters;
            }
        };
        VolleySingleton.getInstance(this).getRequestQueue().add(request);
    }

    private void onLoadActivity() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        if (tableExists(db, "receiveLocal")) {
            if (resultExists(db, "receiveLocal", "itemID")) {
                builder.setTitle("????????????????")
                        .setMessage("?? ?????? ?????????????? ???????????????????????????????????????? ????????????")
                        .setCancelable(false)
                        .setNegativeButton("????????????????????????",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        for (int i = 0; i < itemIDList.size(); i++) {
                                            if (valueExists(db, "receiveLocal", "itemID", String.valueOf(itemIDList.get(i)))) {
                                                listViewItems.setItemChecked(i, true);
                                            }
                                        }
                                        dialog.cancel();
                                    }
                                })
                        .setPositiveButton("??????????????",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        clearTable("receiveLocal");
                                        dialog.cancel();
                                    }
                                });
                AlertDialog alert = builder.create();
                alert.show();
            }
        }
    }

    private void onNextPressed(){
        String sql = "SELECT items.????????????????????????, receiveLocal.quantity FROM receiveLocal INNER JOIN items " +
                "ON receiveLocal.itemID LIKE items.?????????????? ";
        Cursor c = db.rawQuery(sql, null);
        if (c.moveToFirst()) {
            int itemNameTmp = c.getColumnIndex("????????????????????????");
            int quantityTmp = c.getColumnIndex("quantity");
            ArrayList<String> itemNameList = new ArrayList<>();
            ArrayList<Double> quantityList = new ArrayList<>();
            do {
                itemNameList.add(c.getString(itemNameTmp));
                quantityList.add(c.getDouble(quantityTmp));
            } while (c.moveToNext());
            receiveList = new String[itemNameList.size()];
            for (int i = 0; i < itemNameList.size(); i++){
                receiveList[i] = "????????????????????????: " + itemNameList.get(i) + System.getProperty ("line.separator") +
                        "??????-????: " + String.valueOf(quantityList.get(i));
            }
        } else {
            Log.d(LOG_TAG, "0 rows");
            Toast.makeText(getApplicationContext(), "????????????: CreateInvoiceChooseSalesPartner receiveDataFromLocalDB 001",
                    Toast.LENGTH_SHORT).show();
        }
        arrayAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_multiple_choice, itemsList);
        listViewItems.setAdapter(arrayAdapter);
        c.close();

        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(this);
        builder.setTitle("????????????????")
                .setCancelable(true)
                .setNeutralButton("??????????",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int id) {
                                for (int i = 0; i < itemIDList.size(); i++) {
                                    if (valueExists(db, "receiveLocal", "itemID", String.valueOf(itemIDList.get(i)))) {
                                        listViewItems.setItemChecked(i, true);
                                    }
                                }
                                dialog.cancel();
                            }
                        })
                .setNegativeButton("????????????????",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Toast.makeText( getApplicationContext(), "?? ????????????????... ", Toast.LENGTH_SHORT).show();
                                syncReceive();
                                dialog.cancel();
                            }
                        })
                .setPositiveButton("??????????????",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                clearTable("receiveLocal");
                                finish();
                                dialog.cancel();
                            }
                        })
                .setItems(receiveList, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int item) {
                            }
                        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void onChangeListener() {
        editTextSetQuantity.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // ?????????? ???????????? ?????? ????????????????
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,0);
            }

            @Override
            public void afterTextChanged(Editable s) {
                ContentValues cv = new ContentValues();
                if (item.trim().length() > 0) {
                    if (editTextSetQuantity.getText().toString().trim().length() > 0) {
                        if (!item.equals("??????-???? ??????????????") && !item.equals("???????????? ????-???????????????????? ??????????????")) {
                            if ((Double.parseDouble(editTextSetQuantity.getText().toString()) > 0d &&
                                    Double.parseDouble(editTextSetQuantity.getText().toString()) < 1d)) {
                                Toast.makeText(getApplicationContext(), "<<< ???????? ?????????? ?? ???????????? >>>", Toast.LENGTH_SHORT).show();
                                editTextSetQuantity.setText("");
                            }
                            if (editTextSetQuantity.getText().toString().trim().length() > 0) {
                                if (Double.parseDouble(editTextSetQuantity.getText().toString()) >= 1) {
                                    Double tmpD = Double.parseDouble(editTextSetQuantity.getText().toString()) %
                                            Math.floor(Double.parseDouble(editTextSetQuantity.getText().toString()));
                                    if (tmpD > 0) {
                                        Toast.makeText(getApplicationContext(), "<<< ???????? ?????????? ?? ???????????? >>>", Toast.LENGTH_SHORT).show();
                                        editTextSetQuantity.setText("");
                                    } else {
                                        quantity = Double.parseDouble(editTextSetQuantity.getText().toString());
                                        Toast.makeText(getApplicationContext(), quantity.toString(), Toast.LENGTH_SHORT).show();
                                        if (valueExists(db, "receiveLocal", "itemID", String.valueOf(itemIDList.get(iTmp)))) {
                                            Log.d(LOG_TAG, "--- Update receiveLocal: ---");
                                            cv.put("quantity", quantity);
                                            int updCount = db.update("receiveLocal", cv, "itemID = ?",
                                                    new String[]{String.valueOf(itemIDList.get(iTmp))});
                                            Log.d(LOG_TAG, "updated rows count = " + updCount);
                                        }
                                    }
                                }
                            }
                        } else {
                            quantity = Double.parseDouble(editTextSetQuantity.getText().toString());
                            Toast.makeText(getApplicationContext(), quantity.toString(), Toast.LENGTH_SHORT).show();
                            if (valueExists(db, "receiveLocal", "itemID", String.valueOf(itemIDList.get(iTmp)))) {
                                Log.d(LOG_TAG, "--- Update receiveLocal: ---");
                                cv.put("quantity", quantity);
                                int updCount = db.update("receiveLocal", cv, "itemID = ?",
                                        new String[]{String.valueOf(itemIDList.get(iTmp))});
                                Log.d(LOG_TAG, "updated rows count = " + updCount);
                            }
                        }
                    }
                }
            }
        });
    }

    private void clearTable(String tableName){
        Log.d(LOG_TAG, "--- Clear: " + tableName + " ---");
        int clearCount = db.delete(tableName, null, null);
        Log.d(LOG_TAG, "deleted rows count = " + clearCount);
    }

    class DBHelper extends SQLiteOpenHelper {

        public DBHelper(Context context) {
            // ?????????????????????? ??????????????????????
            super(context, "myLocalDB", null, 1);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            Log.d(LOG_TAG, "--- onCreate database ---");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }
    }

    @Override
    public void onBackPressed() {
        openQuitDialog();
    }

    private void openQuitDialog() {
        AlertDialog.Builder quitDialog = new AlertDialog.Builder(this);
        quitDialog.setTitle("?????????? ?????? ?????????????????????????? ?? ?????????????????");

        quitDialog.setPositiveButton("?????????????? ?? ??????????", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                clearTable("receiveLocal");
                finish();
            }
        });

        quitDialog.setNegativeButton("????????????????", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        quitDialog.show();
    }
}
