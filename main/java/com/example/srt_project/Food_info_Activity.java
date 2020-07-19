package com.example.srt_project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import static android.os.Environment.DIRECTORY_DOWNLOADS;

public class Food_info_Activity extends AppCompatActivity {


    private String getname;
    private TextView food_expiration;
    private TextView food_kcal;
    private TextView food_detail;
    private ImageView imageView;
    private String name, url, expiration, tagName, kcal, detail;
    private Button user_button;
    Context mContext;
    public FirebaseFirestore firebaseFirestore;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_info);
        mContext = this;
        firebaseFirestore = FirebaseFirestore.getInstance();

        TextView food_name = (TextView)findViewById(R.id.food_name);
        TextView food_user = (TextView)findViewById(R.id.user);
        food_expiration = (TextView)findViewById(R.id.expiration);
        food_kcal = (TextView)findViewById(R.id.kcal);
        food_detail = (TextView)findViewById(R.id.detail);

        imageView = (ImageView)findViewById(R.id.food_img);
        user_button = (Button)findViewById(R.id.user_button);


        Bundle bundle = getIntent().getExtras();
        getname = bundle.getString("name");
        food_name.setText(bundle.getString("name"));
        food_user.setText(bundle.getString("user"));
        if(bundle.getString("user").isEmpty()){
            user_button.setVisibility(View.VISIBLE);
        }

        user_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firebaseFirestore.collection(SharedPref_id.getString(mContext,"myRef")).document("food")
                        .update(bundle.getString("name"),SharedPref_id.getString(mContext,"user"));
                user_button.setVisibility(View.GONE);
                food_user.setText(SharedPref_id.getString(mContext,"user"));
            }
        });

        parser();


    }

    private void parser(){

        // 내부 xml파일이용시
        InputStream inputStream = getResources().openRawResource(R.raw.info_food);
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        BufferedReader reader = new BufferedReader(inputStreamReader);
        XmlPullParserFactory factory = null;
        XmlPullParser xmlParser = null;
        boolean check = false;

        boolean isName = false, isUrl = false, isExpiration = false, isKcal = false, isDetail = false;
        try {
            factory = XmlPullParserFactory.newInstance();
            xmlParser = factory.newPullParser();
            xmlParser.setInput(reader);
            int eventType = xmlParser.getEventType();

            while (eventType != XmlPullParser.END_DOCUMENT){
                switch (eventType){
                    case XmlPullParser.START_TAG :
                        tagName = xmlParser.getName();
                        switch (tagName) {
                            case "name":
                                isName = true;
                                break;
                            case "url" :
                                isUrl = true;
                                break;
                            case "expiration" :
                                isExpiration = true;
                                break;
                            case "kcal" :
                                isKcal = true;
                                break;
                            case "detail" :
                                isDetail = true;
                                break;
                        }
                        break;

                    case XmlPullParser.TEXT:
                        if(isName == true) {
                            if(xmlParser.getText().equals(getname)){
                                check=true;
                                name = xmlParser.getText();
                                Log.d("TAG!!",xmlParser.getText());
                            }
                        }
                        if(isUrl == true) {
                            if(check==true){
                                url = xmlParser.getText();
                                Log.d("TAG!!",xmlParser.getText());
                            }
                        }
                        if(isExpiration == true) {
                            if(check==true){
                                expiration = xmlParser.getText();
                                Log.d("TAG!!",xmlParser.getText());
                            }
                        }
                        if(isKcal == true) {
                            if(check==true){
                                kcal = xmlParser.getText();
                                Log.d("TAG!!",xmlParser.getText());
                            }
                        }
                        if(isDetail == true) {
                            if(check==true){
                                detail = xmlParser.getText();
                                Log.d("TAG!!",xmlParser.getText());
                            }
                        }
                        break;

                    case XmlPullParser.END_TAG:
                        tagName = xmlParser.getName();
                        switch (tagName) {
                            case "name":
                                isName = false;
                                break;
                            case "url" :
                                isUrl = false;
                                break;
                            case "expiration" :
                                isExpiration = false;
                                break;
                            case "kcal" :
                                isKcal = false;
                                break;
                            case "detail" :
                                isDetail = false;
                                check=false;
                                break;
                        }

                        break;
                }
                try {
                    eventType = xmlParser.next();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } finally{
            try{
                if(reader !=null) reader.close();
                if(inputStreamReader !=null) inputStreamReader.close();
                if(inputStream !=null) inputStream.close();
                Glide.with(Food_info_Activity.this).load(url).into(imageView);
                food_expiration.setText(expiration+"일");
                food_kcal.setText(kcal);
                food_detail.setText(detail);

            }catch(Exception e2){
                e2.printStackTrace();
            }
        }

    }

}
