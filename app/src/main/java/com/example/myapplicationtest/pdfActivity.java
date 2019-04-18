package com.example.myapplicationtest;

import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.io.File;
import java.util.*;
import com.grapecity.documents.excel.*;
import com.grapecity.documents.excel.drawing.*;

public class pdfActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf);

        File sd = Environment.getExternalStorageDirectory();

        File directoryTypeOne = new File(sd.getAbsolutePath() + File.separator + "Download"
                + File.separator + "Excel" + File.separator + "Накладная_отчет");

        Workbook workbook = new Workbook();
        workbook.open(sd.getAbsolutePath() + File.separator + "Download"
                + File.separator + "Excel" + File.separator + "Накладная_отчет" + File.separator + "inv_typeone_1745_17.04.2019.xls");

        workbook.save(sd.getAbsolutePath() + File.separator + "Download"
                + File.separator + "Excel" + File.separator + "Накладная_отчет" + File.separator +
                "inv_typeone_1745_17.04.2019.pdf", SaveFileFormat.Pdf);
    }
}
