package com.example.myapplicationtest;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class CreateInvoiceMakeCommentActivity extends AppCompatActivity implements View.OnClickListener {

    EditText editTextComment;
    Button saveComment;
    SharedPreferences.Editor e;
    SharedPreferences sPrefComment;
    final String SAVED_Comment = "comment";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_invoice_make_comment);

        editTextComment = findViewById(R.id.editTextComment);
        saveComment = findViewById(R.id.buttonSaveComment);
        saveComment.setOnClickListener(this);

        sPrefComment = getSharedPreferences(SAVED_Comment, Context.MODE_PRIVATE);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonSaveComment:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Внесено")
                        .setMessage("Название нового магазина записано в комментарий. Спасибо за работу.")
                        .setCancelable(false)
                        .setPositiveButton("Назад",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        setSaveComment();
                                        dialog.cancel();
                                    }
                                });
                AlertDialog alert = builder.create();
                alert.show();
                break;
            default:
                break;
        }
    }

    private void setSaveComment(){
        e = sPrefComment.edit();
        e.putString(SAVED_Comment, editTextComment.getText().toString());
        e.apply();
        finish();
    }
}
