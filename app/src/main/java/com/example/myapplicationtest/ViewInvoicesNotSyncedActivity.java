package com.example.myapplicationtest;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.media.MediaScannerConnection;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import jxl.Cell;
import jxl.CellType;
import jxl.Sheet;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.format.CellFormat;
import jxl.read.biff.BiffException;
import jxl.write.Label;
import jxl.write.WritableCell;
import jxl.write.WritableCellFormat;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

public class ViewInvoicesNotSyncedActivity extends AppCompatActivity implements View.OnClickListener {

    List<DataInvoiceLocal> listTmp = new ArrayList<>();
    final String LOG_TAG = "myLogs";
    DBHelper dbHelper;
    SQLiteDatabase db;
    Button btnSaveInvoiceToLocalDB;
    SharedPreferences sPrefDBName, sPrefDBPassword, sPrefDBUser, sPrefLogin, sPrefAccountingTypeDefault,
            sPrefArea, sPrefAreaDefault, sPrefInvoiceNumberLast;
    String paymentStatus, invoiceNumbers = "", dbName, dbUser, dbPassword, csvFileTypeOneReport,
            csvFileTypeOneFormCopy, csvFileTypeOneForm, csvFileTypeTwoForm, salesPartnerNameChosen,
            inputFileTypeOne, inputFileTypeTwo, csvFileTypeTwoReport, csvFileTypeTwoFormCopy,
            requestUrlSaveRecord = "https://caiman.ru.com/php/saveNewInvoice_new.php",
            loginSecurity, statusSave = "", areaDefault, invoiceNumberLast, invoiceNumberChosen, accTypeSPChosen;
    final String SAVED_DBName = "dbName";
    final String SAVED_DBUser = "dbUser";
    final String SAVED_DBPassword = "dbPassword";
    final String SAVED_LOGIN = "Login";
    final String SAVED_ACCOUNTINGTYPEDEFAULT = "AccountingTypeDefault";
    final String SAVED_AREA = "Area";
    final String SAVED_AREADEFAULT = "areaDefault";
    final String SAVED_InvoiceNumberLast = "invoiceNumberLast";
    ArrayList<String> arrItems, invoiceNumberServerTmp, dateTimeDocServer, summaryListTmp, accTypeListTmp,
            accTypeSPListTmp, invoiceNumberListTmp, summaryListTmpSecond, accTypeListTmpSecond,
            accTypeSPListTmpSecond, invoiceNumberListTmpSecond, salesPartnerNameListTmp, infoItemNameList,
            infoDateTimeDocLocalList, infoItemDescriptionList;
    ArrayList<Double> arrQuantity, arrExchange, arrReturn, arrSum, infoExchangeList, infoQuantityList,
            infoTotalList, infoReturnList, infoInvoiceSumList;
    ArrayList<Integer> arrPriceChanged, invoiceNumbersList, infoPriceList, salesPartnerDB_IDList;
    List<DataInvoice> dataArray;
    String[] requestMessage, summaryList, summaryListSecond;
    Boolean saveMenuTrigger = false;
    File fileTypeOne, fileTypeTwo;
    Integer salesPartnerDB_IDChosen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_invoices_not_synced);

        dbHelper = new DBHelper(this);
        db = dbHelper.getWritableDatabase();

        dataArray = new ArrayList<>();
        arrItems = new ArrayList<>();
        arrSum = new ArrayList<>();
        arrQuantity = new ArrayList<>();
        arrExchange = new ArrayList<>();
        arrReturn = new ArrayList<>();
        arrPriceChanged = new ArrayList<>();
        invoiceNumbersList = new ArrayList<>();
        invoiceNumberServerTmp = new ArrayList<>();
        dateTimeDocServer = new ArrayList<>();
        summaryListTmp = new ArrayList<>();
        accTypeSPListTmp = new ArrayList<>();
        accTypeListTmp = new ArrayList<>();
        invoiceNumberListTmp = new ArrayList<>();
        summaryListTmpSecond = new ArrayList<>();
        accTypeSPListTmpSecond = new ArrayList<>();
        accTypeListTmpSecond = new ArrayList<>();
        invoiceNumberListTmpSecond = new ArrayList<>();
        salesPartnerNameListTmp = new ArrayList<>();
        salesPartnerDB_IDList = new ArrayList<>();

        btnSaveInvoiceToLocalDB = findViewById(R.id.buttonSyncInvoicesWithServer);
        btnSaveInvoiceToLocalDB.setOnClickListener(this);

        sPrefDBName = getSharedPreferences(SAVED_DBName, Context.MODE_PRIVATE);
        sPrefDBUser = getSharedPreferences(SAVED_DBUser, Context.MODE_PRIVATE);
        sPrefDBPassword = getSharedPreferences(SAVED_DBPassword, Context.MODE_PRIVATE);
        sPrefLogin = getSharedPreferences(SAVED_LOGIN, Context.MODE_PRIVATE);
        sPrefAccountingTypeDefault = getSharedPreferences(SAVED_ACCOUNTINGTYPEDEFAULT, Context.MODE_PRIVATE);
        sPrefArea= getSharedPreferences(SAVED_AREA, Context.MODE_PRIVATE);
        sPrefAreaDefault  = getSharedPreferences(SAVED_AREADEFAULT, Context.MODE_PRIVATE);
        sPrefInvoiceNumberLast = getSharedPreferences(SAVED_InvoiceNumberLast, Context.MODE_PRIVATE);

        areaDefault = sPrefAreaDefault.getString(SAVED_AREADEFAULT, "");
        dbName = sPrefDBName.getString(SAVED_DBName, "");
        dbUser = sPrefDBUser.getString(SAVED_DBUser, "");
        dbPassword = sPrefDBPassword.getString(SAVED_DBPassword, "");
        loginSecurity = sPrefLogin.getString(SAVED_LOGIN, "");
        invoiceNumberLast = sPrefInvoiceNumberLast.getString(SAVED_InvoiceNumberLast, "");

        setInitialData();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonSyncInvoicesWithServer:
                if (statusSave.equals("Saved")){
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Внимание")
                            .setMessage("У вас нет несинхронизированных накладных")
                            .setCancelable(false)
                            .setPositiveButton("Назад",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            finish();
                                            dialog.cancel();
                                        }
                                    });
                    AlertDialog alert = builder.create();
                    alert.show();
                } else {
                    saveMenu();
                }
                break;
            default:
                break;
        }
    }

    private void setInitialData() {
//        Integer count;
//        String sql = "SELECT COUNT(*) FROM itemsToInvoiceTmp ";
//        Cursor cursor = db.rawQuery(sql, null);
//        if (!cursor.moveToFirst()) {
//            cursor.close();
//            count = 0;
//        } else {
//            count = cursor.getInt(0);
//        }
//        cursor.close();
//        tmpCount = count;
        if (statusSave.equals("Saved")){
            Toast.makeText(getApplicationContext(), "StatusSave", Toast.LENGTH_SHORT).show();
        } else {
            invoiceNumberServerTmp.add(String.valueOf(0));
            dateTimeDocServer.add("");
        }

//        if (resultExists(db, "syncedInvoice","invoiceNumber")){
//            String sql = "SELECT DISTINCT invoiceLocalDB.invoiceNumber FROM invoiceLocalDB " +
//                    "WHERE NOT EXISTS (SELECT syncedInvoice.invoiceNumber FROM syncedInvoice " +
//                    "WHERE invoiceLocalDB.invoiceNumber LIKE  syncedInvoice.invoiceNumber) ";
//            Cursor c = db.rawQuery(sql, null);
//            if (c.moveToFirst()) {
//                int iNumber = c.getColumnIndex("invoiceNumber");
//                do {
//                    invoiceNumbers = invoiceNumbers + "----" + c.getString(iNumber) ;
//                    invoiceNumbersList.add(Integer.parseInt(c.getString(iNumber)));
//                } while (c.moveToNext());
//            } else {
//                alreadySyncedPrompt();
//            }
//            c.close();
//            Toast.makeText(getApplicationContext(), "Несинхронизированные: " + invoiceNumbers, Toast.LENGTH_SHORT).show();
//        } else {
        if (resultExists(db, "invoiceLocalDB", "invoiceNumber")){
            String sql = "SELECT DISTINCT invoiceNumber FROM invoiceLocalDB ";
            Cursor c = db.rawQuery(sql, null);
            if (c.moveToFirst()) {
                int iNumber = c.getColumnIndex("invoiceNumber");
                do {
                    invoiceNumbers = invoiceNumbers + "----" + c.getString(iNumber);
                    invoiceNumbersList.add(Integer.parseInt(c.getString(iNumber)));
                } while (c.moveToNext());
            }
            c.close();
            for (int i = 0; i < invoiceNumbersList.size(); i++){
                Toast.makeText(getApplicationContext(), "Ничего не синхронизировано: " + invoiceNumbers, Toast.LENGTH_SHORT).show();
            }
        } else {
            alreadySyncedPrompt();
        }

        for (int i = 0; i < invoiceNumbersList.size(); i++){
            String sql = "SELECT salesPartnerName, accountingTypeDoc, dateTimeDocLocal, invoiceSum" +
                    " FROM invoiceLocalDB WHERE invoiceNumber LIKE ?";
            Cursor c = db.rawQuery(sql, new String[]{invoiceNumbersList.get(i).toString()});
            if (c.moveToFirst()) {
                int salesPartnerNameTmp = c.getColumnIndex("salesPartnerName");
                int accountingTypeDocTmp = c.getColumnIndex("accountingTypeDoc");
                int dateTimeDocLocalTmp = c.getColumnIndex("dateTimeDocLocal");
                int invoiceSumTmp = c.getColumnIndex("invoiceSum");
                String salesPartnerName = c.getString(salesPartnerNameTmp);
                String accountingTypeDoc = c.getString(accountingTypeDocTmp);
                String dateTimeDocLocal = c.getString(dateTimeDocLocalTmp);
                Double invoiceSum = Double.parseDouble(c.getString(invoiceSumTmp));
                paymentStatus = "";
                listTmp.add(new DataInvoiceLocal(salesPartnerName, accountingTypeDoc,
                        Integer.parseInt(invoiceNumberServerTmp.get(0)), dateTimeDocServer.get(0), dateTimeDocLocal,
                        invoiceSum, paymentStatus));
                c.moveToNext();
            }
            c.close();
        }
        RecyclerView recyclerView = findViewById(R.id.recyclerViewInvoicesLocal);
        DataAdapterViewInvoicesFromLocalDB adapter = new DataAdapterViewInvoicesFromLocalDB(this, listTmp);
        recyclerView.setAdapter(adapter);

        saveMenuTrigger = true;
        saveInvoicesToServerDB();
    }

    private void alreadySyncedPrompt(){
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Поздравляю")
                .setMessage("У вас нет несинхронизированных документов")
                .setCancelable(false)
                .setPositiveButton("Я  Рад(а)",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                finish();
                                dialog.cancel();
                            }
                        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void saveMenu(){
        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(this);
        builder.setTitle("Синхронизация");
        builder.setPositiveButton("Синхронизировать",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        saveInvoicesToServerDB();
//                        finish();
                        dialog.cancel();
                    }
                });
        builder.setNegativeButton("Контроль",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        listWithoutIDFilter();
                        manageMenu();
                        dialog.cancel();
                    }
                });
        builder.setItems(summaryList, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                invoiceNumberChosen = invoiceNumberListTmp.get(item);
                salesPartnerNameChosen = salesPartnerNameListTmp.get(item);
                salesPartnerDB_IDChosen = salesPartnerDB_IDList.get(item);
                receiveInvoiceInfo();
                Toast.makeText(getApplicationContext(), "Накладная №: " + invoiceNumberChosen, Toast.LENGTH_SHORT).show();
                makeExcel();
                try {
                    read();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        builder.setCancelable(true);
        AlertDialog alert = builder.create();
        alert.show();
//        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
//        lp.copyFrom(alert.getWindow().getAttributes());
//        lp.width = 500;
//        lp.height = 1100;
//        lp.x = -300;
//        alert.getWindow().setAttributes(lp);
    }

    private void printMenu() throws IOException {

    }

//    public void setInputFile(String inputFile) {
//        this.inputFile = inputFile;
//    }

    public void read() throws IOException {
        Instant instant = Instant.now();
        ZoneId zoneId = ZoneId.of( "Asia/Sakhalin" );
        ZonedDateTime zdt = ZonedDateTime.ofInstant( instant , zoneId );
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern( "dd.MM.yyyy" );
        String output = zdt.format( formatter );

        File sd = Environment.getExternalStorageDirectory();
        csvFileTypeOneFormCopy = "inv_typeone_" + invoiceNumberChosen + "_" + output + ".xls";
        csvFileTypeTwoFormCopy = "inv_typetwo_" + invoiceNumberChosen + "_" + output + ".xls";

        File directoryTypeOne = new File(sd.getAbsolutePath() + File.separator + "Download"
                + File.separator + "Excel" + File.separator + "Накладная_отчет");
        File directoryTypeTwo = new File(sd.getAbsolutePath() + File.separator + "Download"
                + File.separator + "Excel" + File.separator + "Фактура_отчет");
        if (!directoryTypeOne.isDirectory()) {
            directoryTypeOne.mkdirs();
        }
        if (!directoryTypeTwo.isDirectory()) {
            directoryTypeTwo.mkdirs();
        }
        File fileTypeOneSave = new File(directoryTypeOne, csvFileTypeOneFormCopy);
        File fileTypeTwoSave = new File(directoryTypeTwo, csvFileTypeTwoFormCopy);
        File inputWorkbookTypeOne = fileTypeOne;
        File inputWorkbookTypeTwo = fileTypeTwo;
        Workbook wTypeOne;
        Workbook wTypeTwo;
        WorkbookSettings wbSettings = new WorkbookSettings();
        wbSettings.setLocale(new Locale("ru", "Ru"));
        try {
            wTypeOne = Workbook.getWorkbook(inputWorkbookTypeOne);
            wTypeTwo = Workbook.getWorkbook(inputWorkbookTypeTwo);
            WritableWorkbook copyTypeOne = Workbook.createWorkbook(fileTypeOneSave, wTypeOne);
            WritableSheet sheetTypeOne = copyTypeOne.getSheet(0);
            WritableWorkbook copyTypeTwo = Workbook.createWorkbook(fileTypeTwoSave, wTypeTwo);
            WritableSheet sheetTypeTwo = copyTypeTwo.getSheet(0);

//            WritableCell cellTypeOne = sheetTypeOne.getWritableCell(3, 29);
//            CellFormat cfmTypeOne = cellTypeOne.getCellFormat();

            String dateTimeDoc = infoDateTimeDocLocalList.get(0);
            DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern( "yyyy/MM/dd HH:mm:ss" );
            LocalDateTime d2 = LocalDateTime.parse(dateTimeDoc, formatter2);
            final String output2 = d2.format( formatter2 );

//            Date outputDate = stringToDate(dateTimeDoc, "yyyy/MM/dd HH:mm:ss");
//            String output3 = outputDate.toString();
//            DateTimeFormatter formatter3 = DateTimeFormatter.ofPattern( "dd.MM.yyyy HH:mm:ss" );
//            LocalDateTime d3 = LocalDateTime.parse(dateTimeDoc, formatter3);
//            final String output4 = d3.with(LocalTime.MIN).format( formatter3 );
//            Toast.makeText(getApplicationContext(), output4, Toast.LENGTH_SHORT).show();

            String ceo;
            String license;
            String address;
            String ceoFull;
            String ceoTaxNumber;
            String ceoFullTaxNumber;
            String code = "--", codeValue = "--";
            String phoneNumber;
            String ceoCompanyTaxNumber, eaes;
            if (salesPartnerDB_IDChosen == 66 || salesPartnerDB_IDChosen == 1059 ||
                    (salesPartnerDB_IDChosen >= 100 && salesPartnerDB_IDChosen <=105) ||
                    salesPartnerDB_IDChosen == 1057 || salesPartnerDB_IDChosen == 1080){
                ceo = "Ли Г.С.";
                ceoFull = "ИП Ли Ген Сун";
                license = "";
                ceoTaxNumber = "651600222647";
                ceoFullTaxNumber = "ИП Ли Ген Сун, ИНН 651600222647";
                address = "693005, Сахалинская обл, Южно-Сахалинск г, Колодезная ул, дом №8";
                phoneNumber = "25-02-62";
                ceoCompanyTaxNumber = "318650100014220";
                eaes = "№ RU Д RU.АЯ23.В.00129/18";
            } else {
                ceo = "Че В.Е.";
                ceoFull = "Индивидуальный предприниматель Че Владимир Енгунович";
                ceoFullTaxNumber = "Индивидуальный предприниматель Че Владимир Енгунович, ИНН 651600635813";
                license = "свидетельство 65 №000878852 от 10.12.2010";
                ceoTaxNumber = "651600635813";
                ceoCompanyTaxNumber = "310650134400052";
                address = "693005, Сахалинская обл, Южно-Сахалинск г, Колодезная ул, дом №8";
                phoneNumber = "25-02-62";
                eaes = "№ RU Д RU.АЯ23.В.00129/18";
            }

            for (int j = 0; j < sheetTypeOne.getColumns(); j++){
                for (int i = 0; i < sheetTypeOne.getRows(); i++){
                    WritableCell cellTypeOne = sheetTypeOne.getWritableCell(j, i);
                    CellFormat cfmTypeOne = cellTypeOne.getCellFormat();
                    if (j == 5 && i == 0) {
                        if (cellTypeOne.getType() == CellType.LABEL) {
                            Label lTypeOne = (Label) cellTypeOne;
                            lTypeOne.setString("Дата: " + output2); //Дата
                        }
                    }
                    if (j == 0 && i == 1) {
                        if (cellTypeOne.getType() == CellType.LABEL) {
                            Label lTypeOne = (Label) cellTypeOne;
                            lTypeOne.setString("Продавец: " + ceoFull); //Продавец
                        }
                    }
                    if (j == 0 && i == 2) {
                        if (cellTypeOne.getType() == CellType.LABEL) {
                            Label lTypeOne = (Label) cellTypeOne;
                            lTypeOne.setString("Адрес: " + address); //Адрес
                        }
                    }
                    if (j == 0 && i == 3) {
                        if (cellTypeOne.getType() == CellType.LABEL) {
                            Label lTypeOne = (Label) cellTypeOne;
                            lTypeOne.setString("ИНН: " + ceoTaxNumber + ", тел. " + phoneNumber); //ИНН, тел.
                        }
                    }
                    if (j == 0 && i == 4) {
                        if (cellTypeOne.getType() == CellType.LABEL) {
                            Label lTypeOne = (Label) cellTypeOne;
                            lTypeOne.setString("ОГРНИП: " + ceoCompanyTaxNumber); //ОГРНИП
                        }
                    }
                    if (j == 0 && i == 5) {
                        if (cellTypeOne.getType() == CellType.LABEL) {
                            Label lTypeOne = (Label) cellTypeOne;
                            lTypeOne.setString("ЕАЭС: " + eaes); //ЕАЭС
                        }
                    }
                    if (j == 0 && i == 6) {
                        if (cellTypeOne.getType() == CellType.LABEL) {
                            Label lTypeOne = (Label) cellTypeOne;
                            lTypeOne.setString("НАКЛАДНАЯ № " + areaDefault + " - " + invoiceNumberChosen
                                    + " - " + salesPartnerNameChosen); //Номер накладной
                        }
                    }
                    if (j == 3 && i == 36) {
                        if (cellTypeOne.getType() == CellType.LABEL) {
                            Label lTypeOne = (Label) cellTypeOne;
                            lTypeOne.setString(String.valueOf(infoInvoiceSumList.get(0))); //Итого
                        }
//                        sheetTypeOne.addCell(new Label(j, i, String.valueOf(infoInvoiceSumList.get(0))));
                    }
                    if (i > 8 && i < 30 && i < (infoItemNameList.size() + 9)) {
                        if (j == 0) {
                            if (cellTypeOne.getType() == CellType.LABEL) {
                                Label lTypeOne = (Label) cellTypeOne;
                                lTypeOne.setString(String.valueOf(i - 8)); //Порядковый номер
                            }
//                            sheetTypeOne.addCell(new Label(j, i, String.valueOf(i - 8)));
                        }
                        if (j == 1) {
                            if (cellTypeOne.getType() == CellType.LABEL) {
                                Label lTypeOne = (Label) cellTypeOne;
                                lTypeOne.setString(String.valueOf(infoExchangeList.get(i - 9))); //Обмен
                            }
//                            sheetTypeOne.addCell(new Label(j, i, String.valueOf(infoExchangeList.get(i - 9))));
                        }
                        if (j == 2) {
                            if (cellTypeOne.getType() == CellType.LABEL) {
                                Label lTypeOne = (Label) cellTypeOne;
                                lTypeOne.setString(infoItemNameList.get(i - 9)); //Наименование
                            }
//                            sheetTypeOne.addCell(new Label(j, i, infoItemNameList.get(i - 9)));
                        }
                        if (j == 5) {
                            if (cellTypeOne.getType() == CellType.LABEL) {
                                Label lTypeOne = (Label) cellTypeOne;
                                lTypeOne.setString(infoItemDescriptionList.get(i - 9)); //Описание товара (масса)
                            }
                        }
                        if (j == 6) {
                            if (cellTypeOne.getType() == CellType.LABEL) {
                                Label lTypeOne = (Label) cellTypeOne;
                                lTypeOne.setString(String.valueOf(infoQuantityList.get(i - 9))); //Кол-во
                            }
//                            sheetTypeOne.addCell(new Label(j, i, String.valueOf(infoQuantityList.get(i - 9))));
                        }
                        if (j == 7) {
                            if (cellTypeOne.getType() == CellType.LABEL) {
                                Label lTypeOne = (Label) cellTypeOne;
                                lTypeOne.setString(String.valueOf(infoPriceList.get(i - 9))); //Цена
                            }
//                            sheetTypeOne.addCell(new Label(j, i, String.valueOf(infoPriceList.get(i - 9))));
                        }
                        if (j == 8) {
                            if (cellTypeOne.getType() == CellType.LABEL) {
                                Label lTypeOne = (Label) cellTypeOne;
                                lTypeOne.setString(String.valueOf(infoTotalList.get(i - 9))); //Сумма
                            }
//                            sheetTypeOne.addCell(new Label(j, i, String.valueOf(infoTotalList.get(i - 9))));
                        }
                    }
                    cellTypeOne.setCellFormat(cfmTypeOne);
                }
            }

            Integer b = 0, c = 0, d = 0, f = 0, g = 0, h = 0, k = 0, l = 0, m = 0, n = 0, o = 0;
            for (int j = 0; j < sheetTypeTwo.getColumns(); j++) {
                for (int i = 0; i < sheetTypeTwo.getRows(); i++) {
                    WritableCell cellTypeTwo = sheetTypeTwo.getWritableCell(j, i);
                    CellFormat cfmTypeTwo = cellTypeTwo.getCellFormat();
                    if (j == 16 && i == 0) {
                        if (cellTypeTwo.getType() == CellType.LABEL) {
                            Label lTypeTwo = (Label) cellTypeTwo;
                            lTypeTwo.setString("Номер документа"); //Номер документа согласовать с Мариной
                            //Тут могут быть проблемы с согласованием, объемом ручной работы и налоговой
                        }
                    }
                    if (j == 25 && i == 0) {
                        if (cellTypeTwo.getType() == CellType.LABEL) {
                            Label lTypeTwo = (Label) cellTypeTwo;
                            lTypeTwo.setString(output);
                        }
                    }
                    if (j == 4 && i == 4) {
                        if (cellTypeTwo.getType() == CellType.LABEL) {
                            Label lTypeTwo = (Label) cellTypeTwo;
                            lTypeTwo.setString("1"); //Статус документа: 2 или 1. Не понятно что должно быть!!!
                        }
                    }

                    if (!infoItemNameList.equals("Ким-ча весовая") && !infoItemNameList.equals("Редька по-восточному весовая")){
                        code = "796";
                        codeValue = "шт";
                    }

                    if (j == 21 && i == 3) {
                        if (cellTypeTwo.getType() == CellType.LABEL) {
                            Label lTypeTwo = (Label) cellTypeTwo;
                            lTypeTwo.setString(ceoFull); //ИП Ли Ген Сун или Индивидуальный предприниматель Че Владимир Енгунович
                        }
                    }
                    if (j == 21 && i == 4) {
                        if (cellTypeTwo.getType() == CellType.LABEL) {
                            Label lTypeTwo = (Label) cellTypeTwo;
                            lTypeTwo.setString(address); //Для Ли Ген Сун - 694820, Сахалинская обл, Томаринский р-н, Томари г, Подгорная ул, дом №21
                            //Для Че В.Е. - Сахалинская обл, Южно-Сахалинск г п/р Ново-Александровск, 2-я Комсомольская, дом № 10а, квартира 15
                        }
                    }
                    if (j == 21 && i == 5) {
                        if (cellTypeTwo.getType() == CellType.LABEL) {
                            Label lTypeTwo = (Label) cellTypeTwo;
                            lTypeTwo.setString(ceoTaxNumber); //Для Ли Ген Сун - 651600222647 или Для Че В.Е. - 651600635813
                        }
                    }
                    if (j == 21 && i == 7) {
                        if (cellTypeTwo.getType() == CellType.LABEL) {
                            Label lTypeTwo = (Label) cellTypeTwo;
                            lTypeTwo.setString("Грузополучатель: Юр.наим/ИП и его адрес"); //Юр.название орг, адрес с индексом
                        }
                    }
                    if (j == 21 && i == 9) {
                        if (cellTypeTwo.getType() == CellType.LABEL) {
                            Label lTypeTwo = (Label) cellTypeTwo;
                            lTypeTwo.setString("Покупатель: Юр.наименование или ИП"); //Юр.название организации
                        }
                    }
                    if (j == 21 && i == 10) {
                        if (cellTypeTwo.getType() == CellType.LABEL) {
                            Label lTypeTwo = (Label) cellTypeTwo;
                            lTypeTwo.setString("Адрес отгрузки"); //Фактический адрес доставки. Может совпадать с адресом в грузополучателе
                        }
                    }
                    if (j == 21 && i == 11) {
                        if (cellTypeTwo.getType() == CellType.LABEL) {
                            Label lTypeTwo = (Label) cellTypeTwo;
                            lTypeTwo.setString("ИНН/КПП покупателя"); //ИНН и КПП (если для Юр.лиц) покупателя
                        }
                    }
                    if (j == 1 && i == 33) {
                        if (cellTypeTwo.getType() == CellType.LABEL) {
                            Label lTypeTwo = (Label) cellTypeTwo;
                            lTypeTwo.setString("Универсальный передаточный документ № ???" + " от " + output + " г.");
                            //Номер документа нужно согласовать с Мариной
                        }
                    }
                    if (j == 1 && i == 76) {
                        if (cellTypeTwo.getType() == CellType.LABEL) {
                            Label lTypeTwo = (Label) cellTypeTwo;
                            lTypeTwo.setString("Универсальный передаточный документ № ???" + " от " + output + " г.");
                        }
                    }

                    int a = 0;
                    if (infoItemNameList.size() > 16 && infoItemNameList.size() < 25){
                        a = 43;
                    }
                    if (infoItemNameList.size() < 17){
                        a = 51;
                    }

                    if (j == 29 && i == 94 - a) {
                        if (cellTypeTwo.getType() == CellType.LABEL) {
                            Label lTypeTwo = (Label) cellTypeTwo;
                            lTypeTwo.setString(ceo); //Че В.Е. или Ли Г.С.
                        }
                    }
                    if (j == 49 && i == 94 - a) {
                        if (cellTypeTwo.getType() == CellType.LABEL) {
                            Label lTypeTwo = (Label) cellTypeTwo;
                            lTypeTwo.setString(license); //Свидетельство (ДЛЯ ИП Че: свидетельство 65 №000878852 от 10.12.2010)

                        }
                    }

                    if (j == 18 && i == 96 - a) {
                        if (cellTypeTwo.getType() == CellType.LABEL) {
                            Label lTypeTwo = (Label) cellTypeTwo;
                            lTypeTwo.setString("Документ № " + invoiceNumberChosen + ", "
                                    + salesPartnerNameChosen + " от " + output2); //Основание
                        }
                    }
                    if (j == 15 && i == 105 - a) {
                        if (cellTypeTwo.getType() == CellType.LABEL) {
                            Label lTypeTwo = (Label) cellTypeTwo;
                            lTypeTwo.setString(output); //Дата отгрузки товара покупателю
                        }
                    }
                    if (j == 24 && i == 111 - a) {
                        if (cellTypeTwo.getType() == CellType.LABEL) {
                            Label lTypeTwo = (Label) cellTypeTwo;
                            lTypeTwo.setString(ceo); //ФИО продавца: Ли Г.С. ли Че В.Е.
                        }
                    }
                    if (j == 2 && i == 114 - a) {
                        if (cellTypeTwo.getType() == CellType.LABEL) {
                            Label lTypeTwo = (Label) cellTypeTwo;
                            lTypeTwo.setString(ceoFullTaxNumber); //Для ИП Че: Индивидуальный предприниматель Че Владимир Енгунович, ИНН 651600635813
                            //Для Ли: ИП Ли Ген Сун, ИНН 651600222647
                        }
                    }
                    if (j == 43 && i == 114 - a) {
                        if (cellTypeTwo.getType() == CellType.LABEL) {
                            Label lTypeTwo = (Label) cellTypeTwo;
                            lTypeTwo.setString("Получатель - ЮР.Лицо/ИП, ИНН/КПП"); //Получатель. Юр.наименование или ИП, а также ИНН и КПП, если есть
                        }
                    }
                    if (j == 39 && i == 90 - a) {
                        if (cellTypeTwo.getType() == CellType.LABEL) {
                            Label lTypeTwo = (Label) cellTypeTwo;
                            lTypeTwo.setString(String.valueOf(infoInvoiceSumList.get(0))); //Итого без налога
                        }
                    }
                    if (j == 56 && i == 90 - a) {
                        if (cellTypeTwo.getType() == CellType.LABEL) {
                            Label lTypeTwo = (Label) cellTypeTwo;
                            lTypeTwo.setString("Итого с налогом"); //Итого c налогом
                        }
                    }

                    if (i >= 18 && infoItemNameList.size() < 17 && i != 35 && i != 36 && i != 37 && i!= 39
                            && i != 76 && i != 78 && i!= 79 && i!= 80) {
                        if (infoItemNameList.size() != 0) {
                            if (j == 3) {
                                if (cellTypeTwo.getType() == CellType.LABEL) {
                                    Label lTypeTwo = (Label) cellTypeTwo;
                                    lTypeTwo.setString("Артикул товара"); //Артикул товара
                                    b++;
                                }
                            }
                            if (j == 6) {
                                if (cellTypeTwo.getType() == CellType.LABEL) {
                                    Label lTypeTwo = (Label) cellTypeTwo;
                                    lTypeTwo.setString(infoItemNameList.get(c)); //Наименование товара
                                    c++;
                                }
                            }
                            if (j == 20) {
                                if (cellTypeTwo.getType() == CellType.LABEL) {
                                    Label lTypeTwo = (Label) cellTypeTwo;
                                    lTypeTwo.setString(code); //Единица измерения. Код
                                    d++;
                                }
                            }
                            if (j == 22) {
                                if (cellTypeTwo.getType() == CellType.LABEL) {
                                    Label lTypeTwo = (Label) cellTypeTwo;
                                    lTypeTwo.setString(codeValue); //Единица измерения. Условное обозначение
                                    f++;
                                }
                            }
                            if (j == 26) {
                                if (cellTypeTwo.getType() == CellType.LABEL) {
                                    Label lTypeTwo = (Label) cellTypeTwo;
                                    lTypeTwo.setString(String.valueOf(infoQuantityList.get(g))); //Количество
                                    g++;
                                }
                            }
                            if (j == 30) {
                                if (cellTypeTwo.getType() == CellType.LABEL) {
                                    Label lTypeTwo = (Label) cellTypeTwo;
                                    lTypeTwo.setString(String.valueOf(infoPriceList.get(h))); //Цена
                                    h++;
                                }
                            }
                            if (j == 39) {
                                if (cellTypeTwo.getType() == CellType.LABEL) {
                                    Label lTypeTwo = (Label) cellTypeTwo;
                                    lTypeTwo.setString(String.valueOf(infoTotalList.get(k))); //Стоимость без налога
                                    k++;
                                }
                            }
                            if (j == 48) {
                                if (cellTypeTwo.getType() == CellType.LABEL) {
                                    Label lTypeTwo = (Label) cellTypeTwo;
                                    lTypeTwo.setString("Акциз"); //Акциз
                                    l++;
                                }
                            }
                            if (j == 50) {
                                if (cellTypeTwo.getType() == CellType.LABEL) {
                                    Label lTypeTwo = (Label) cellTypeTwo;
                                    lTypeTwo.setString("Налоговая ставка"); //Налоговая ставка
                                    m++;
                                }
                            }
                            if (j == 54) {
                                if (cellTypeTwo.getType() == CellType.LABEL) {
                                    Label lTypeTwo = (Label) cellTypeTwo;
                                    lTypeTwo.setString("Сумма налога"); //Сумма налога
                                    n++;
                                }
                            }
                            if (j == 56) {
                                if (cellTypeTwo.getType() == CellType.LABEL) {
                                    Label lTypeTwo = (Label) cellTypeTwo;
                                    lTypeTwo.setString("Стоимость с налогом"); //Стоимость с налогом
                                    o++;
                                }
                            }
                        }
                    }
                    cellTypeTwo.setCellFormat(cfmTypeTwo);
                }
            }

//            if (cellTypeOne.getType() == CellType.LABEL)
//            {
//                Label lTypeOne = (Label) cellTypeOne;
//                lTypeOne.setString("Шо за бред");
//            }

            copyTypeOne.write();
            copyTypeOne.close();
            copyTypeTwo.write();
            copyTypeTwo.close();
//            w.close();
            // Get the first sheet
//            Sheet sheet = w.getSheet(0);
            // Loop over first 10 column and lines
//            Map<CellFormat, WritableCellFormat> definedFormats = new HashMap<>();
//            for (int j = 0; j < sheet.getColumns(); j++) {
//                newSheet.setColumnView(j, sheet.getColumnView(j));
//                for (int i = 0; i < sheet.getRows(); i++) {
//
////                    Cell cell = sheet.getCell(j, i);
//                    if (j == 0) {
//                        newSheet.setRowView(i, sheet.getRowView(i));
//                    }
//                    Cell readCell = sheet.getCell(j, i);
//                    Label label = new Label(j, i, readCell.getContents());
////                    if (i == 18){
////                        label = new Label(1, i, String.valueOf(j));
////                        if (j == 3){
////                            label = new Label(j, i, "test");
////                        }
////                    }
//                    CellFormat readFormat = readCell.getCellFormat();
//                    if (readFormat != null) {
//                        if (!definedFormats.containsKey(readFormat)) {
//                            definedFormats.put(readFormat, new WritableCellFormat(
//                                    readFormat));
//                        }
//                        label.setCellFormat(definedFormats.get(readFormat));
//                    }
//                    newSheet.addCell(label);
//                    WritableCell cell = sheet.getWritableCell(j, i);
//                    CellFormat cfm = cell.getCellFormat();
//                    CellType type = cell.getType();
//                    if (type == CellType.LABEL) {
//                        if (j == 21 && i == 3) {
//                            System.out.println("I got a label "
//                                    + cell.getContents());
//                            Label l = (Label) cell;
//                            l.setString("modified cell");
//                            cell.setCellFormat(cfm);
//                        }
//                    }

//                    if (type == CellType.NUMBER) {
//                        System.out.println("I got a number "
//                                + cell.getContents());
//                        Number l = (Number) cell;
//                        l.setValue();
//                        cell.setCellFormat(cfm);
//                    }
//                }
//            }
//            workbook.write();
//            workbook.close();
//            wTypeOne.close();
            wTypeOne.close();
            wTypeTwo.close();
        } catch (BiffException e) {
            e.printStackTrace();
        } catch (WriteException e) {
            e.printStackTrace();
        }
    }

    private void makeExcel(){
        File sd = Environment.getExternalStorageDirectory();

        csvFileTypeOneForm = "invoice_form.xls";
        csvFileTypeTwoForm = "УПД_" + infoItemNameList.size() + ".xls";

        File directoryTypeOne = new File(sd.getAbsolutePath() + File.separator + "Download"
                + File.separator + "Excel" + File.separator + "Накладная_форма");
        File directoryTypeTwo = new File(sd.getAbsolutePath() + File.separator + "Download"
                + File.separator + "Excel" + File.separator + "Фактура_форма");
//        if (!directoryTypeOne.isDirectory()) {
//            directoryTypeOne.mkdirs();
//        }
//        if (!directoryTypeTwo.isDirectory()) {
//            directoryTypeTwo.mkdirs();
//        }
        fileTypeOne = new File(directoryTypeOne, csvFileTypeOneForm);
        fileTypeTwo = new File(directoryTypeTwo, csvFileTypeTwoForm);

//        setInputFile(sd.getAbsolutePath() + File.separator + "Download" + File.separator + "Excel" + File.separator + "ЧЕВЕ.xls");
//        MediaScannerConnection.scanFile(this, new String[]{fileTypeOne.getAbsolutePath()}, null, null);

    }

    private void manageMenu(){
        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(this);
        builder.setTitle("Сырой список. Несовпадений: " + (summaryListSecond.length - summaryList.length));
        builder.setNegativeButton("Назад",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        saveMenu();
                        dialog.cancel();
                    }
                });
        builder.setItems(summaryListSecond, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                invoiceNumberChosen = invoiceNumberListTmpSecond.get(item);
                accTypeSPChosen = accTypeSPListTmpSecond.get(item);
                Toast.makeText(getApplicationContext(), "Накладная №: " + invoiceNumberChosen, Toast.LENGTH_SHORT).show();
                changeInvoice();
                finish();
            }
        });
        builder.setCancelable(true);
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void changeInvoice(){
        String accTypeTmp;
        if (accTypeSPChosen.equals("непровод")){
            accTypeTmp = "провод";
        } else {
            accTypeTmp = "непровод";
        }
        ContentValues cv = new ContentValues();
        Log.d(LOG_TAG, "--- update invoiceLocalDB: ---");
        cv.put("accountingTypeSP", accTypeTmp);
        long rowID = db.update("invoiceLocalDB", cv, "invoiceNumber = ?",
                new String[]{invoiceNumberChosen});
        Log.d(LOG_TAG, "row updated, ID = " + rowID);
    }

    private void saveInvoicesToServerDB(){
        for (int i = 0; i < invoiceNumbersList.size(); i++){
//            dataArray.clear();
            String sql = "SELECT invoiceLocalDB.*, salesPartners.serverDB_ID FROM invoiceLocalDB " +
                    "INNER JOIN salesPartners ON invoiceLocalDB.salesPartnerName LIKE salesPartners.Наименование " +
                    "AND invoiceLocalDB.areaSP LIKE salesPartners.Район AND invoiceLocalDB.accountingTypeSP " +
                    "LIKE salesPartners.Учет WHERE invoiceLocalDB.invoiceNumber LIKE ? ";
//            String sql = "SELECT * FROM invoiceLocalDB WHERE invoiceLocalDB.invoiceNumber LIKE ? ";
            Cursor c = db.rawQuery(sql, new String[]{invoiceNumbersList.get(i).toString()});
            if (c.moveToFirst()) {
                int invoiceNumberLocalTmp = c.getColumnIndex("invoiceNumber");
                int agentIDTmp = c.getColumnIndex("agentID");
                int areaSPTmp = c.getColumnIndex("areaSP");
                int salesPartnerIDTmp = c.getColumnIndex("serverDB_ID");
                int accountingTypeDocTmp = c.getColumnIndex("accountingTypeDoc");
                int accountingTypeSPTmp = c.getColumnIndex("accountingTypeSP");
                int itemNameTmp = c.getColumnIndex("itemName");
                int quantityTmp = c.getColumnIndex("quantity");
                int priceTmp = c.getColumnIndex("price");
                int totalCostTmp = c.getColumnIndex("totalCost");
                int exchangeTmp = c.getColumnIndex("exchangeQuantity");
                int returnTmp = c.getColumnIndex("returnQuantity");
                int dateTimeDocLocalTmp = c.getColumnIndex("dateTimeDocLocal");
                int invoiceSumTmp = c.getColumnIndex("invoiceSum");
                int commentTmp = c.getColumnIndex("comment");
                int salesPartnerNameTmp = c.getColumnIndex("salesPartnerName");
                summaryListTmp.add(c.getString(salesPartnerNameTmp));
                accTypeListTmp.add(c.getString(accountingTypeDocTmp));
                accTypeSPListTmp.add(c.getString(accountingTypeSPTmp));
                invoiceNumberListTmp.add(c.getString(invoiceNumberLocalTmp));
                salesPartnerNameListTmp.add(c.getString(salesPartnerNameTmp));
                salesPartnerDB_IDList.add(c.getInt(salesPartnerIDTmp));
                do {
                    Integer invoiceNumberLocal = Integer.parseInt(c.getString(invoiceNumberLocalTmp));
                    Integer agentID = Integer.parseInt(c.getString(agentIDTmp));
                    Integer areaSP = c.getInt(areaSPTmp);
                    Integer salesPartnerID = c.getInt(salesPartnerIDTmp);
                    String accountingTypeDoc = c.getString(accountingTypeDocTmp);
                    String accountingTypeSP = c.getString(accountingTypeSPTmp);
                    String itemName = c.getString(itemNameTmp);
                    Double quantity = Double.parseDouble(c.getString(quantityTmp));
                    Double price = Double.parseDouble(c.getString(priceTmp));
                    Double totalCost = Double.parseDouble(c.getString(totalCostTmp));
                    Double exchangeQuantity = Double.parseDouble(c.getString(exchangeTmp));
                    Double returnQuantity = Double.parseDouble(c.getString(returnTmp));
                    String dateTimeDocLocal = c.getString(dateTimeDocLocalTmp);
                    Double invoiceSum = Double.parseDouble(c.getString(invoiceSumTmp));
                    String comment = c.getString(commentTmp);

                    Log.d(LOG_TAG, "invoiceNumber: " + invoiceNumberLocal.toString());

                    DataInvoice dt = new DataInvoice(accountingTypeDoc, accountingTypeSP,
                            itemName, dateTimeDocLocal, comment, salesPartnerID, invoiceNumberLocal, agentID, areaSP, price,
                            quantity, totalCost, exchangeQuantity, returnQuantity, invoiceSum);
                    dataArray.add(dt);

                } while (c.moveToNext());
            }
            c.close();
        }
        summaryList = new String[summaryListTmp.size()];
        for (int i = 0; i < summaryListTmp.size(); i++) {
            summaryList[i] = "№." + invoiceNumberListTmp.get(i) + " " + summaryListTmp.get(i);
//                    + " accSP: " + accTypeSPListTmp.get(i) + " accDoc: " + accTypeListTmp.get(i);
        }

        if (saveMenuTrigger == false) {
            sendToServer();
        }
        saveMenuTrigger = false;
    }

    private void listWithoutIDFilter(){
        for (int i = 0; i < invoiceNumbersList.size(); i++){
            String sql = "SELECT * FROM invoiceLocalDB WHERE invoiceLocalDB.invoiceNumber LIKE ? ";
            Cursor c = db.rawQuery(sql, new String[]{invoiceNumbersList.get(i).toString()});
            if (c.moveToFirst()) {
                int invoiceNumberLocalTmp = c.getColumnIndex("invoiceNumber");
                int accountingTypeDocTmp = c.getColumnIndex("accountingTypeDoc");
                int accountingTypeSPTmp = c.getColumnIndex("accountingTypeSP");
                int salesPartnerNameTmp = c.getColumnIndex("salesPartnerName");
                summaryListTmpSecond.add(c.getString(salesPartnerNameTmp));
                accTypeListTmpSecond.add(c.getString(accountingTypeDocTmp));
                accTypeSPListTmpSecond.add(c.getString(accountingTypeSPTmp));
                invoiceNumberListTmpSecond.add(c.getString(invoiceNumberLocalTmp));
                do {

                } while (c.moveToNext());
            }
            c.close();
        }
        summaryListSecond = new String[summaryListTmpSecond.size()];
        for (int i = 0; i < summaryListTmpSecond.size(); i++) {
            summaryListSecond[i] = "№." + invoiceNumberListTmpSecond.get(i) + " " + summaryListTmpSecond.get(i) +
                    System.lineSeparator() + "тип Точки: " + accTypeSPListTmpSecond.get(i) + System.lineSeparator() +
                    "тип документа: " + accTypeListTmpSecond.get(i);
        }
    }

    private void sendToServer(){
        Gson gson = new Gson();
        final String newDataArray = gson.toJson(dataArray);
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        StringRequest request = new StringRequest(Request.Method.POST,
                requestUrlSaveRecord, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try{
                    JSONArray jsonArray = new JSONArray(response);
                    requestMessage = new String[jsonArray.length()];

                    if (jsonArray.length() > 0){
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject obj = jsonArray.getJSONObject(i);
                            requestMessage[i] = obj.getString("requestMessage");
                        }
                        if (requestMessage[0].equals("New record created successfully")){
                            builder.setTitle("Поздравляю")
                                    .setMessage("Синхронизировано успешно")
                                    .setCancelable(false)
                                    .setNegativeButton("Круто",
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
//                                                    invoiceNumbersList.clear();
                                                    finish();
                                                    dialog.cancel();
                                                }
                                            });
                            AlertDialog alert = builder.create();
                            alert.show();
                        }
                    } else {
                        builder.setTitle("Внимание")
                                .setMessage("Возможно все синхронизировано, но сервер выкаблучивается. Обратитесь к Создателю и больше не жмите до талого!")
                                .setCancelable(false)
                                .setNegativeButton("Ясно",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                finish();
                                                dialog.cancel();
                                            }
                                        });
                        AlertDialog alert = builder.create();
                        alert.show();
                        Toast.makeText(getApplicationContext(), "Ошибка загрузки. Проверьте Интернет или Учётку", Toast.LENGTH_SHORT).show();
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
                onConnectionFailed();
                Toast.makeText(getApplicationContext(), "Сообщите об этой ошибке. Код 001", Toast.LENGTH_SHORT).show();
                Log.e("TAG", "Error " + error.getMessage());
            }
        }){
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> parameters = new HashMap<>();
                parameters.put("dbName", dbName);
                parameters.put("dbUser", dbUser);
                parameters.put("dbPassword", dbPassword);
                parameters.put("array", newDataArray);
                return parameters;
            }
        };
        VolleySingleton.getInstance(this).getRequestQueue().add(request);
    }

    class DBHelper extends SQLiteOpenHelper {

        public DBHelper(Context context) {
            // конструктор суперкласса
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

    private void onConnectionFailed(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Нет ответа от Сервера")
                .setMessage("Попробуйте позже")
                .setCancelable(false)
                .setNegativeButton("Ok",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                finish();
                                dialog.cancel();
                            }
                        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void clearTable(String tableName){
        Log.d(LOG_TAG, "--- Clear " + tableName + " : ---");
        // удаляем все записи
        int clearCount = db.delete(tableName, null, null);
        Log.d(LOG_TAG, "deleted rows count = " + clearCount);
        Toast.makeText(getApplicationContext(), "<<< Таблицы очищены >>>", Toast.LENGTH_SHORT).show();
    }

    private void receiveInvoiceInfo(){
        infoExchangeList = new ArrayList<>();
        infoItemNameList = new ArrayList<>();
        infoPriceList = new ArrayList<>();
        infoQuantityList = new ArrayList<>();
        infoTotalList = new ArrayList<>();
        infoReturnList = new ArrayList<>();
        infoInvoiceSumList = new ArrayList<>();
        infoDateTimeDocLocalList = new ArrayList<>();
        infoItemDescriptionList = new ArrayList<>();
//        String sql = "SELECT salesPartners.serverDB_ID FROM salesPartners " +
//                "INNER JOIN invoiceLocalDB On salesPartners.Наименование LIKE invoiceLocalDB.salesPartnerName " +
//                "AND invoiceLocalDB.areaSP = salesPartners.Район AND invoiceLocalDB.accountingTypeSP LIKE salesPartners.Учет " +
//                "WHERE invoiceNumber LIKE ?";
//        Cursor c = db.rawQuery(sql, new String[]{invoiceNumberChosen});
        String sql = "SELECT invoiceLocalDB.*, items.Описание FROM invoiceLocalDB INNER JOIN items " +
                "ON invoiceLocalDB.itemName LIKE items.Наименование WHERE invoiceNumber LIKE ?";
        Cursor c = db.rawQuery(sql, new String[]{invoiceNumberChosen});
        if (c.moveToFirst()) {
            int exchange = c.getColumnIndex("exchangeQuantity");
            int itemName = c.getColumnIndex("itemName");
            int price = c.getColumnIndex("price");
            int quantity = c.getColumnIndex("quantity");
            int total = c.getColumnIndex("totalCost");
            int returnQuantity = c.getColumnIndex("returnQuantity");
            int itemDescription = c.getColumnIndex("Описание");
            int accountingTypeDocTmp = c.getColumnIndex("accountingTypeDoc");
            int invoiceSumTmp = c.getColumnIndex("invoiceSum");
            int dateTimeDoc = c.getColumnIndex("dateTimeDocLocal");

            do {
                infoExchangeList.add(c.getDouble(exchange));
                infoItemNameList.add(c.getString(itemName));
                infoPriceList.add(c.getInt(price));
                infoQuantityList.add(c.getDouble(quantity));
                infoTotalList.add(c.getDouble(total));
                infoReturnList.add(c.getDouble(returnQuantity));
                infoInvoiceSumList.add(c.getDouble(invoiceSumTmp));
                infoDateTimeDocLocalList.add(c.getString(dateTimeDoc));
                infoItemDescriptionList.add(c.getString(itemDescription));
            } while (c.moveToNext());
        }
//        Toast.makeText(getApplicationContext(), "Позиций продано: " + String.valueOf(infoItemNameList.size()), Toast.LENGTH_SHORT).show();
        Toast.makeText(getApplicationContext(), infoItemDescriptionList.get(4), Toast.LENGTH_SHORT).show();
        c.close();
    }

    private Date stringToDate(String aDate, String aFormat) {

        if(aDate==null) return null;
        ParsePosition pos = new ParsePosition(0);
        SimpleDateFormat simpledateformat = new SimpleDateFormat(aFormat);
        Date stringDate = simpledateformat.parse(aDate, pos);
        return stringDate;

    }
}
