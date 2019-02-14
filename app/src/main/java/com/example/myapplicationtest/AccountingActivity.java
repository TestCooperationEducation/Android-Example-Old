package com.example.myapplicationtest;

import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

public class AccountingActivity extends AppCompatActivity {

    String csvFile, folder_name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accounting);

        folder_name = File.separator + "Download" + File.separator + "Excel";


    }

    private void makeExcel(){
        File sd = Environment.getExternalStorageDirectory();
        csvFile = "accountant.xls";

        File directory = new File(sd.getAbsolutePath() + File.separator + "Download" + File.separator + "Excel");
        //create directory if not exist
        if (!directory.isDirectory()) {
            directory.mkdirs();
        }
        //file path
        File file = new File(directory, csvFile);
        MediaScannerConnection.scanFile(this, new String[]{file.getAbsolutePath()}, null, null);
        WorkbookSettings wbSettings = new WorkbookSettings();
        wbSettings.setLocale(new Locale("en", "EN"));
        WritableWorkbook workbook;
        try {
            workbook = Workbook.createWorkbook(file, wbSettings);
            //Excel sheet name. 0 represents first sheet
            WritableSheet sheet = workbook.createSheet("userList", 0);
            // column and row
            sheet.addCell(new Label(0, 0, "UserName"));
            sheet.addCell(new Label(1, 0, "PhoneNumber"));
            String name = "Vova";
            String phoneNumber = "Che";
            sheet.addCell(new Label(0, 1, name));
            sheet.addCell(new Label(1, 1, phoneNumber));
            workbook.write();
            workbook.close();
            Toast.makeText(getApplication(),
                    "Data Exported in a Excel Sheet", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (RowsExceededException e) {
            e.printStackTrace();
        } catch (WriteException e) {
            e.printStackTrace();
        }
    }

    private void sendViaEmail(String folder_name, String file_name, String emailAddress) {
        try {
            File Root= Environment.getExternalStorageDirectory();
            String fileLocation = Root.getAbsolutePath() + folder_name + File.separator + file_name;
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setType("text/plain");
            String message="File to be shared is " + file_name + ".";
            intent.putExtra(Intent.EXTRA_SUBJECT, "Subject");
            intent.putExtra(Intent.EXTRA_STREAM, Uri.parse( "file://" + fileLocation));
            intent.putExtra(Intent.EXTRA_TEXT, message);
            intent.setData(Uri.parse("mailto:" + emailAddress));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            startActivity(intent);
        } catch(Exception e)  {
            System.out.println("is exception raises during sending mail" + e);
        }
    }
}
