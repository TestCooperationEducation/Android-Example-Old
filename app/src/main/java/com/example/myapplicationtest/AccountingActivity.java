package com.example.myapplicationtest;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class AccountingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accounting);


    }

//    private void testOne(){
//        String[] columns = { "First Name", "Last Name", "Email",
//                "Date Of Birth" };
//        List<Contact> contacts = new ArrayList<>();
//
//
//        contacts.add(new Contact("Sylvain", "Saurel",
//                "sylvain.saurel@gmail.com", "17/01/1980"));
//        contacts.add(new Contact("Albert", "Dupond",
//                "sylvain.saurel@gmail.com", "17/08/1989"));
//        contacts.add(new Contact("Pierre", "Dupont",
//                "sylvain.saurel@gmail.com", "17/07/1956"));
//        contacts.add(new Contact("Mariano", "Diaz", "sylvain.saurel@gmail.com",
//                "17/05/1988"));
//
//        Workbook workbook = new XSSFWorkbook();
//        Sheet sheet = workbook.createSheet("Contacts");
//
//        Font headerFont = workbook.createFont();
//        headerFont.setBold(true);
//        headerFont.setFontHeightInPoints((short) 14);
//        headerFont.setColor(IndexedColors.RED.getIndex());
//
//        CellStyle headerCellStyle = workbook.createCellStyle();
//        headerCellStyle.setFont(headerFont);
//
//        // Create a Row
//        Row headerRow = sheet.createRow(0);
//
//        for (int i = 0; i < columns.length; i++) {
//            Cell cell = headerRow.createCell(i);
//            cell.setCellValue(columns[i]);
//            cell.setCellStyle(headerCellStyle);
//        }
//
//        // Create Other rows and cells with contacts data
//        int rowNum = 1;
//
//        for (Contact contact : contacts) {
//            Row row = sheet.createRow(rowNum++);
//            row.createCell(0).setCellValue(contact.firstName);
//            row.createCell(1).setCellValue(contact.lastName);
//            row.createCell(2).setCellValue(contact.email);
//            row.createCell(3).setCellValue(contact.dateOfBirth);
//        }
//
//        // Resize all columns to fit the content size
//        for (int i = 0; i < columns.length; i++) {
//            sheet.autoSizeColumn(i);
//        }
//
//        // Write the output to a file
//        FileOutputStream fos = null;
//        try {
//            String str_path = Environment.getExternalStorageDirectory().toString();
//            File file ;
//            file = new File(str_path, getString(R.string.app_name) + ".xls");
//            fos = new FileOutputStream(file);
//            workbook.write(fos);
//        } catch (IOException e) {
//            e.printStackTrace();
//        } finally {
//            if (fos != null) {
//                try {
//                    fos.flush();
//                    fos.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//            Toast.makeText(this, "Excel Sheet Generated", Toast.LENGTH_SHORT).show();
//        }
//    }

    private void testTwo(){

    }
}
