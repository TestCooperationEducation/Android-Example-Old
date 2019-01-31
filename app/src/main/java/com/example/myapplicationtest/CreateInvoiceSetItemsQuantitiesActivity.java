package com.example.myapplicationtest;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.myapplicationtest.DataPrice;
import com.example.myapplicationtest.R;
import com.example.myapplicationtest.VolleySingleton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CreateInvoiceSetItemsQuantitiesActivity extends AppCompatActivity implements View.OnClickListener {

    SharedPreferences sPrefDBName, sPrefDBPassword, sPrefDBUser, sPrefItemsList, sPrefSalesPartner,
            sPrefConnectionStatus, sPrefAccountingType, sPrefItemName;
    String requestUrlFinalPrice = "https://caiman.ru.com/php/price.php", dbName, dbUser, dbPassword,
            salesPartner, connStatus, item;
    String[] itemPrice, discountType, discountValue;
    final String SAVED_DBName = "dbName";
    final String SAVED_DBUser = "dbUser";
    final String SAVED_DBPassword = "dbPassword";
    final String SAVED_ItemsListToInvoice = "itemsToInvoice";
    final String SAVED_SALESPARTNER = "SalesPartner";
    final String SAVED_CONNSTATUS = "connectionStatus";
    final String SAVED_ACCOUNTINGTYPE = "AccountingType";
    final String SAVED_ITEMNAME = "itemName";
    Double finalPrice, priceChanged, tmpQuantityOnStart, tmpExchangeOnStart, tmpReturnOnStart;
//    ArrayList<String> myList;
    ArrayList<DataPrice> dataArray;
    TextView textViewSalesPartner, textViewItemName, textViewAccountingType, textViewTotal;
    EditText editTextQuantity, editTextExchange, editTextReturn, editTextPrice;
    Button btnChangePrice,btnSaveTmp;
    final String LOG_TAG = "myLogs";
    DBHelper dbHelper;
    SQLiteDatabase db;
    Boolean quantityType, priceFromTmpLoaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_invoice_set_items_quantities);

        dbHelper = new DBHelper(this);
        db = dbHelper.getWritableDatabase();

        quantityType = false;

        dataArray = new ArrayList<>();
//        btnChangePrice = findViewById(R.id.buttonChangePrice);
//        btnChangePrice.setOnClickListener(this);
        btnSaveTmp = findViewById(R.id.buttonSaveTmp);
        btnSaveTmp.setOnClickListener(this);

        sPrefDBName = getSharedPreferences(SAVED_DBName, Context.MODE_PRIVATE);
        sPrefDBUser = getSharedPreferences(SAVED_DBUser, Context.MODE_PRIVATE);
        sPrefDBPassword = getSharedPreferences(SAVED_DBPassword, Context.MODE_PRIVATE);
        sPrefItemsList = getSharedPreferences(SAVED_ItemsListToInvoice, Context.MODE_PRIVATE);
        sPrefSalesPartner = getSharedPreferences(SAVED_SALESPARTNER, Context.MODE_PRIVATE);
        sPrefConnectionStatus = getSharedPreferences(SAVED_CONNSTATUS, Context.MODE_PRIVATE);
        sPrefAccountingType = getSharedPreferences(SAVED_ACCOUNTINGTYPE, Context.MODE_PRIVATE);
        sPrefItemName = getSharedPreferences(SAVED_ITEMNAME, Context.MODE_PRIVATE);

        textViewSalesPartner = findViewById(R.id.textViewSalesPartner);
        textViewItemName = findViewById(R.id.textViewItemName);
        editTextPrice = findViewById(R.id.editTextViewPrice);
        textViewAccountingType = findViewById(R.id.textViewAccountingType);
        editTextQuantity = findViewById(R.id.editTextQuantity);
        editTextExchange = findViewById(R.id.editTextExchange);
        editTextReturn = findViewById(R.id.editTextReturn);
        textViewTotal = findViewById(R.id.textViewTotal);

        salesPartner = sPrefSalesPartner.getString(SAVED_SALESPARTNER, "");
        textViewSalesPartner.setText(salesPartner);
        textViewItemName.setText(sPrefItemName.getString(SAVED_ITEMNAME, ""));
        item = textViewItemName.getText().toString();
        textViewAccountingType.setText(sPrefAccountingType.getString(SAVED_ACCOUNTINGTYPE, ""));

        if (sPrefDBName.contains(SAVED_DBName) && sPrefDBUser.contains(SAVED_DBUser) && sPrefDBPassword.contains(SAVED_DBPassword)) {
            dbName = sPrefDBName.getString(SAVED_DBName, "");
            dbUser = sPrefDBUser.getString(SAVED_DBUser, "");
            dbPassword = sPrefDBPassword.getString(SAVED_DBPassword, "");
        }

//        myList = getStringArrayPref(getApplicationContext(), SAVED_ItemsListToInvoice);

        if (sPrefConnectionStatus.contains(SAVED_CONNSTATUS)) {
            connStatus = sPrefConnectionStatus.getString(SAVED_CONNSTATUS, "");
            if (connStatus.equals("success")) {
                getPriceFromServerDB();
            } else {
                finalPrice = 0d;
                priceChanged = finalPrice;
                getPriceFromLocalDB();
            }
            if (resultExists(db, "itemsToInvoiceTmp", "Наименование", item)) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Внимание")
                        .setMessage("Вы хотите загрузить данные из последней сессии?")
                        .setCancelable(false)
                        .setNegativeButton("Да",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        getDataFromTmp();
                                        priceFromTmpLoaded = true;
                                        dialog.cancel();
                                    }
                                })
                        .setPositiveButton("Нет",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {

                                        dialog.cancel();
                                    }
                                });
                AlertDialog alert = builder.create();
                alert.show();
            }
        }
        if (editTextQuantity.getText().toString().trim().length() == 0) {
            tmpQuantityOnStart = 0d;
        } else {
            tmpQuantityOnStart = Double.parseDouble(editTextQuantity.getText().toString());
        }
        if (editTextExchange.getText().toString().trim().length() == 0) {
            tmpExchangeOnStart = 0d;
        } else {
            tmpExchangeOnStart = Double.parseDouble(editTextExchange.getText().toString());
        }
        if (editTextReturn.getText().toString().trim().length() == 0) {
            tmpReturnOnStart = 0d;
        } else {
            tmpReturnOnStart = Double.parseDouble(editTextReturn.getText().toString());
        }



        onChangeListener();
    }

    public void onClick(View v) {
        switch (v.getId()) {
//            case R.id.buttonChangePrice:
//                changePrice();
//                break;
            case R.id.buttonSaveTmp:
                saveTmp();
                break;
            default:
                break;
        }
    }

    public static ArrayList<String> getStringArrayPref(Context context, String key) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String json = prefs.getString(key, null);
        ArrayList<String> urls = new ArrayList<>();
        if (json != null) {
            try {
                JSONArray a = new JSONArray(json);
                for (int i = 0; i < a.length(); i++) {
                    String url = a.optString(i);
                    urls.add(url);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return urls;
    }

    private void getPriceFromServerDB() {
        StringRequest request = new StringRequest(Request.Method.POST,
                requestUrlFinalPrice, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try{
                    JSONArray jsonArray = new JSONArray(response);
                    Toast.makeText(getApplicationContext(), "Query successful", Toast.LENGTH_SHORT).show();
                    itemPrice = new String[jsonArray.length()];
                    discountType = new String[jsonArray.length()];
                    discountValue = new String[jsonArray.length()];
                    if (jsonArray.length() > 0){
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject obj = jsonArray.getJSONObject(i);
                            itemPrice[i] = obj.getString("Цена");
                            if (obj.isNull("Скидка") && obj.isNull("Тип_скидки")) {
                                discountValue[i] = String.valueOf(0);
                                discountType[i] = String.valueOf(0);
                                Toast.makeText(getApplicationContext(), "Без скидки", Toast.LENGTH_SHORT).show();
                            }
                            else {
                                discountValue[i] = obj.getString("Скидка");
                                discountType[i] = obj.getString("Тип_скидки");
                                Toast.makeText(getApplicationContext(), "Со скидкой", Toast.LENGTH_SHORT).show();
                            }
                        }
                        if (Double.parseDouble(discountType[0]) == 0){
                            editTextPrice.setText(itemPrice[0]);
                            finalPrice = Double.parseDouble(editTextPrice.getText().toString());
                        }
                        if (Double.parseDouble(discountType[0]) == 1){
                            finalPrice = Double.parseDouble(itemPrice[0]) - Double.parseDouble(discountValue[0]);
                            editTextPrice.setText(finalPrice.toString());
                        }
                        if (Double.parseDouble(discountType[0]) == 2){
                            finalPrice = Double.parseDouble(itemPrice[0]) - (Double.parseDouble(itemPrice[0]) / 10);
                            editTextPrice.setText(finalPrice.toString());
                        }
                        priceChanged = finalPrice;
                    }else{
                        Toast.makeText(getApplicationContext(), "Something went wrong with DB query", Toast.LENGTH_SHORT).show();
                    }
                }
                catch (JSONException e1) {
                    e1.printStackTrace();
                }
            }
        }, new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error){
                finalPrice = 0d;
                priceChanged = finalPrice;
                onConnectionFailed();
                Toast.makeText(getApplicationContext(), "<<< Нет соединения >>>", Toast.LENGTH_SHORT).show();
                Log.e("TAG", "Error " + error.getMessage());
            }
        }){
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> parameters = new HashMap<>();
                parameters.put("dbName", dbName);
                parameters.put("dbUser", dbUser);
                parameters.put("dbPassword", dbPassword);
                parameters.put("ItemName", item);
                parameters.put("SalesPartner", salesPartner);
                return parameters;
            }
        };
        VolleySingleton.getInstance(this).getRequestQueue().add(request);
    }

    private void getPriceFromLocalDB(){
//        db = dbHelper.getReadableDatabase();
//        String sql;
//        sql = "SELECT  FROM items";
//        Cursor c = db.rawQuery(sql, null);
//        if (c.moveToFirst()) {
//            int idColIndex = c.getColumnIndex("Наименование");
//            do {
//                Log.d(LOG_TAG,"ID = " + c.getString(idColIndex));
//                itemsList.add(c.getString(idColIndex));
//            } while (c.moveToNext());
//            onLoadActivity();
//        } else {
//            Log.d(LOG_TAG, "0 rows");
//            Toast.makeText(getApplicationContext(), "Ошибка: CreateInvoiceChooseSalesPartner receiveDataFromLocalDB 001",
//                    Toast.LENGTH_SHORT).show();
//        }
//        arrayAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_multiple_choice, itemsList);
//        listViewItems.setAdapter(arrayAdapter);
//        c.close();
    }

    private void getDataFromTmp(){
        if (resultExists(db, "itemsToInvoiceTmp", "Наименование", item)){
            Log.d(LOG_TAG, "--- Rows in itemsToInvoiceTmp: ---");
            // делаем запрос всех данных из таблицы itemsToInvoiceTmp, получаем Cursor
            Cursor c = db.query("itemsToInvoiceTmp", null, null, null, null, null, null);
            // ставим позицию курсора на первую строку выборки
            // если в выборке нет строк, вернется false
            if (c.moveToFirst()) {
                // определяем номера столбцов по имени в выборке
                int itemName = c.getColumnIndex("Наименование");
                int price = c.getColumnIndex("Цена");
                int priceChangedTmp = c.getColumnIndex("ЦенаИзмененная");
                int quantity = c.getColumnIndex("Количество");
                int exchangeQuantity = c.getColumnIndex("Обмен");
                int returnQuantity = c.getColumnIndex("Возврат");
                int total = c.getColumnIndex("Итого");
                do {
                    if (c.getString(itemName).equals(item)){
                        editTextQuantity.setText(c.getString(quantity));
                        editTextExchange.setText(c.getString(exchangeQuantity));
                        editTextReturn.setText(c.getString(returnQuantity));
                        priceChanged = Double.parseDouble(c.getString(priceChangedTmp));
                        if (!c.getString(price).equals(c.getString(priceChangedTmp))){
//                            editTextPrice.setText(c.getString(price));
//                            priceChanged = Double.parseDouble(editTextPrice.getText().toString());
//                        } else {
                            editTextPrice.setText(c.getString(priceChangedTmp));
//                            priceChanged = Double.parseDouble(editTextPrice.getText().toString());
                        }
                        textViewTotal.setText(c.getString(total));
                    }
                    // получаем значения по номерам столбцов и пишем все в лог
                    Log.d(LOG_TAG,
                            "ID = " + c.getInt(itemName) +
                                    ", name = " + c.getString(price) +
                                    ", email = " + c.getString(priceChangedTmp));
                    // переход на следующую строку
                    // а если следующей нет (текущая - последняя), то false - выходим из цикла
                } while (c.moveToNext());
                tmpQuantityOnStart = Double.parseDouble(editTextQuantity.getText().toString());
                tmpExchangeOnStart = Double.parseDouble(editTextExchange.getText().toString());
                tmpReturnOnStart = Double.parseDouble(editTextReturn.getText().toString());
            }
        }
    }

    private void saveTmp(){
        Double tmpSum, tmpQuantity, tmpExchange, tmpReturn;
        if (editTextPrice.getText().toString().trim().length() == 0){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Ошибка")
                    .setMessage("Цена не может быть равна нулю")
                    .setCancelable(false)
                    .setNegativeButton("Назад",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });
            AlertDialog alert = builder.create();
            alert.show();
        } else {
            if (editTextQuantity.getText().toString().trim().length() == 0){
                tmpQuantity = 0d;
            } else {
                tmpQuantity = Double.parseDouble(editTextQuantity.getText().toString());
            }

            if (!finalPrice.equals(priceChanged)){
                if (tmpQuantity == 0d) {
                    tmpSum  = 0d;
                } else {
                    tmpSum = priceChanged * Double.parseDouble(editTextQuantity.getText().toString());
                }
            } else {
                if (tmpQuantity == 0d) {
                    tmpSum  = 0d;
                } else {
                    tmpSum = finalPrice * Double.parseDouble(editTextQuantity.getText().toString());
                }
            }

            if (editTextExchange.getText().toString().trim().length() == 0){
                tmpExchange = 0d;
            } else {
                tmpExchange = Double.parseDouble(editTextExchange.getText().toString());
            }
            if (editTextReturn.getText().toString().trim().length() == 0){
                tmpReturn = 0d;
            } else {
                tmpReturn = Double.parseDouble(editTextReturn.getText().toString());
            }

            if (tmpQuantity == 0d && tmpExchange == 0d && tmpReturn == 0d){
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Ошибка")
                        .setMessage("Введите кол-во товара или обмена и возврата")
                        .setCancelable(true)
                        .setNegativeButton("Ок",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });
                AlertDialog alert = builder.create();
                alert.show();
            }

            if (tmpQuantity != 0d || tmpExchange != 0d || tmpReturn != 0d){
                ContentValues cv = new ContentValues();
                Log.d(LOG_TAG, "--- Insert in itemsToInvoiceTmp: ---");
                cv.put("Контрагент", salesPartner);
                cv.put("Наименование", item);
                cv.put("Цена", finalPrice);
                cv.put("ЦенаИзмененная", priceChanged);
                cv.put("Количество", tmpQuantity);
                cv.put("Обмен", tmpExchange);
                cv.put("Возврат", tmpReturn);
                cv.put("Итого", tmpSum);
                long rowID = db.insert("itemsToInvoiceTmp", null, cv);
                Log.d(LOG_TAG, "row inserted, ID = " + rowID);
                Toast.makeText(getApplicationContext(), "<<< Товар добавлен в список >>>", Toast.LENGTH_SHORT).show();
                tmpQuantityOnStart = tmpQuantity;
                tmpExchangeOnStart = tmpExchange;
                tmpReturnOnStart = tmpReturn;
            }
        }
    }

    private void changePrice(){
        if (editTextPrice.getText().toString().trim().length() == 0 || Double.parseDouble(editTextPrice.getText().toString()) == 0){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Ошибка")
                    .setMessage("Цена товара не может быть равна нулю")
                    .setCancelable(true)
                    .setNegativeButton("Назад",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });
            AlertDialog alert = builder.create();
            alert.show();
            editTextPrice.setText(String.valueOf(finalPrice));
        } else {
            priceChanged = Double.parseDouble(editTextPrice.getText().toString());
        }
    }

    private void onChangeListener(){
        editTextQuantity.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // текст только что изменили
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // текст будет изменен
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (editTextQuantity.getText().toString().trim().length() > 0){
                    if (!item.equals("Ким-ча весовая") && !item.equals("Редька по-восточному весовая")){
                        if ((Double.parseDouble(editTextQuantity.getText().toString()) > 0d &&
                                Double.parseDouble(editTextQuantity.getText().toString()) < 1d)){
                            Toast.makeText(getApplicationContext(), "<<< Этот товар в пачках >>>", Toast.LENGTH_SHORT).show();
                            editTextQuantity.setText("");
                        }
                        if (editTextQuantity.getText().toString().trim().length() > 0) {
                            if (Double.parseDouble(editTextQuantity.getText().toString()) >= 1) {
                                Double tmpD = Double.parseDouble(editTextQuantity.getText().toString()) %
                                        Math.floor(Double.parseDouble(editTextQuantity.getText().toString()));
                                if (tmpD > 0) {
                                    Toast.makeText(getApplicationContext(), "<<< Этот товар в пачках >>>", Toast.LENGTH_SHORT).show();
                                    editTextQuantity.setText("");
                                } else {
                                    Double tmpSum;
                                    if (!priceFromTmpLoaded == true) {
                                        if (!finalPrice.equals(priceChanged)) {
                                            tmpSum = priceChanged * Double.parseDouble(editTextQuantity.getText().toString());
                                            textViewTotal.setText(String.valueOf(tmpSum));
                                        } else {
                                            tmpSum = finalPrice * Double.parseDouble(editTextQuantity.getText().toString());
                                            textViewTotal.setText(String.valueOf(tmpSum));
                                        }
                                    } else {
                                        tmpSum = priceChanged * Double.parseDouble(editTextQuantity.getText().toString());
                                        textViewTotal.setText(String.valueOf(tmpSum));
                                    }
                                }
                            }
                        }
                    } else {
                        Double tmpSum;
                        if (priceFromTmpLoaded == true) {
                            tmpSum = priceChanged * Double.parseDouble(editTextQuantity.getText().toString());
                            textViewTotal.setText(String.valueOf(tmpSum));
                            Toast.makeText(getApplicationContext(), "finalPrice1: " + finalPrice + " priceChanged1: " + priceChanged, Toast.LENGTH_SHORT).show();
                        } else {
                            if (priceChanged.equals(finalPrice)){
                                tmpSum = finalPrice * Double.parseDouble(editTextQuantity.getText().toString());
                                textViewTotal.setText(String.valueOf(tmpSum));
                                Toast.makeText(getApplicationContext(), "finalPrice2: " + finalPrice + " priceChanged2: " + priceChanged, Toast.LENGTH_SHORT).show();
                            } else {
                                tmpSum = priceChanged * Double.parseDouble(editTextQuantity.getText().toString());
                                textViewTotal.setText(String.valueOf(tmpSum));
                            }

                        }
                    }
                } else {
                    textViewTotal.setText(String.valueOf(0));
                }
            }
        });

        editTextExchange.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // текст только что изменили
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // текст будет изменен
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (editTextExchange.getText().toString().trim().length() > 0d){
                    if (!item.equals("Ким-ча весовая") && !item.equals("Редька по-восточному весовая")){
                        if ((Double.parseDouble(editTextExchange.getText().toString()) > 0d &&
                                Double.parseDouble(editTextExchange.getText().toString()) < 1d)){
                            Toast.makeText(getApplicationContext(), "<<< Этот товар в пачках >>>", Toast.LENGTH_SHORT).show();
                            editTextExchange.setText("");
                        }
                        if (editTextExchange.getText().toString().trim().length() > 0d) {
                            if (Double.parseDouble(editTextExchange.getText().toString()) >= 1) {
                                Double tmpD = Double.parseDouble(editTextExchange.getText().toString()) %
                                        Math.floor(Double.parseDouble(editTextExchange.getText().toString()));
                                if (tmpD > 0) {
                                    Toast.makeText(getApplicationContext(), "<<< Этот товар в пачках >>>", Toast.LENGTH_SHORT).show();
                                    editTextExchange.setText("");
                                }
                            }
                        }
                    }
                }
            }
        });

        editTextReturn.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // текст только что изменили
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // текст будет изменен
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (editTextReturn.getText().toString().trim().length() > 0d){
                    if (!item.equals("Ким-ча весовая") && !item.equals("Редька по-восточному весовая")){
                        if ((Double.parseDouble(editTextReturn.getText().toString()) > 0d &&
                                Double.parseDouble(editTextReturn.getText().toString()) < 1d)){
                            Toast.makeText(getApplicationContext(), "<<< Этот товар в пачках >>>", Toast.LENGTH_SHORT).show();
                            editTextReturn.setText("");
                        }
                        if (editTextReturn.getText().toString().trim().length() > 0d) {
                            if (Double.parseDouble(editTextReturn.getText().toString()) >= 1) {
                                Double tmpD = Double.parseDouble(editTextReturn.getText().toString()) %
                                        Math.floor(Double.parseDouble(editTextReturn.getText().toString()));
                                if (tmpD > 0) {
                                    Toast.makeText(getApplicationContext(), "<<< Этот товар в пачках >>>", Toast.LENGTH_SHORT).show();
                                    editTextReturn.setText("");
                                }
                            }
                        }
                    }
                }
            }
        });

        editTextPrice.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (editTextQuantity.getText().toString().trim().length() > 0){
                    if (editTextPrice.getText().toString().trim().length() > 0){
                        Double tmpSum = Double.parseDouble(editTextPrice.getText().toString())
                                * Double.parseDouble(editTextQuantity.getText().toString());
                        textViewTotal.setText(String.valueOf(tmpSum));
                        Toast.makeText(getApplicationContext(), "finalPrice3: " + finalPrice + " priceChanged3: " + priceChanged, Toast.LENGTH_SHORT).show();
                        if (Double.parseDouble(editTextPrice.getText().toString()) != (finalPrice)){
                            priceChanged = Double.parseDouble(editTextPrice.getText().toString());
                            Toast.makeText(getApplicationContext(), "finalPrice4: " + finalPrice + " priceChanged4: " + priceChanged, Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        textViewTotal.setText(String.valueOf(0));
                    }
                } else {
                    textViewTotal.setText(String.valueOf(0));
                    if (editTextPrice.getText().toString().trim().length() > 0){
                        priceChanged = Double.parseDouble(editTextPrice.getText().toString());
                    }
                }
            }
        });
    }

    boolean resultExists(SQLiteDatabase db, String tableName, String fieldName, String fieldValue){
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

    class DBHelper extends SQLiteOpenHelper {

        public DBHelper(Context context) {
            // конструктор суперкласса
            super(context, "myLocalDB", null, 1);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {

        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }
    }

    @Override
    public void onBackPressed() {
        if (editTextQuantity.getText().toString().trim().length() > 0 &&
                editTextExchange.getText().toString().trim().length() > 0 &&
                editTextReturn.getText().toString().trim().length() > 0){
            if (tmpQuantityOnStart != Double.parseDouble(editTextQuantity.getText().toString()) ||
                    tmpExchangeOnStart != Double.parseDouble(editTextExchange.getText().toString()) ||
                    tmpReturnOnStart != Double.parseDouble(editTextReturn.getText().toString())){
                openQuitDialog();
            }
            if (tmpQuantityOnStart == Double.parseDouble(editTextQuantity.getText().toString()) &&
                    tmpExchangeOnStart == Double.parseDouble(editTextExchange.getText().toString()) &&
                    tmpReturnOnStart == Double.parseDouble(editTextReturn.getText().toString())) {
                finish();
            }
        } else {
            finish();
        }

    }

    private void openQuitDialog() {
        AlertDialog.Builder quitDialog = new AlertDialog.Builder(this);
        quitDialog.setTitle("Выйти без сохранения этой записи?");

        quitDialog.setPositiveButton("Выйти", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub

                finish();
            }
        });

        quitDialog.setNegativeButton("Остаться", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
            }
        });

        quitDialog.show();
    }

    private void onConnectionFailed(){
//        e = sPrefConnectionStatus.edit();
//        e.putString(SAVED_CONNSTATUS, "failed");
//        e.apply();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Нет ответа от Сервера")
                .setMessage("Придется вписать цену вручную")
                .setCancelable(false)
                .setNegativeButton("Ok",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
        AlertDialog alert = builder.create();
        alert.show();
    }
}
