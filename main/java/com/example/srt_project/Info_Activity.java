package com.example.srt_project;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

public class Info_Activity extends AppCompatActivity {

    public FirebaseFirestore firebaseFirestore;
    private Context mContext;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_);
        mContext = this;

        firebaseFirestore = FirebaseFirestore.getInstance();
        EditText username = (EditText)findViewById(R.id.add_username);
        Button setButton = (Button)findViewById(R.id.set_button);
        setButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!username.getText().toString().equals("")){
                    SharedPref_id.setString(mContext,"user",username.getText().toString());
                    update_user();
                    Intent intent = new Intent(Info_Activity.this,HomeActivity.class);
                    startActivity(intent);
                    finish();
                }
                else{
                    username.setError("사용자 이름을 입력해주세요.");
                }
            }
        });
    }

    void update_user(){
        firebaseFirestore.collection(SharedPref_id.getString(mContext,"myRef")).document("user")
                .update(SharedPref_id.getString(mContext,"user"),"");

    }
}
