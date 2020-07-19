package com.example.srt_project;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class My_Food_List extends Fragment{
    ViewGroup viewGroup;
    private RecyclerView myFirestoreList;
    public FirebaseFirestore firebaseFirestore;
    private ArrayList <String[]> food_list;
    public SwipeRefreshLayout swipeRefreshLayout;
    public FloatingActionButton floatingActionButton;
    public RecyclerAdapter adapter;
    private Context mContext;
    private String url;
    private String tagName;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        viewGroup = (ViewGroup) inflater.inflate(R.layout.recyclerview_fragment,container,false);
        mContext=this.getContext();
        food_list = new ArrayList<String[]>();
        firebaseFirestore = FirebaseFirestore.getInstance();
        swipeRefreshLayout=viewGroup.findViewById(R.id.swipe_layout);

        // 식재료 가져오기
        DocumentReference docRef = firebaseFirestore.collection(SharedPref_id.getString(mContext,"myRef")).document("food");
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Iterator<String> foods = document.getData().keySet().iterator();
                        Iterator<Object> user = document.getData().values().iterator();
                        while (foods.hasNext()){
                            String food = foods.next();
                            String food_user = user.next().toString();
                            if(food_user.equals(SharedPref_id.getString(mContext,"user"))){
                                food_list.add(new String[]{food,food_user});
                            }
                        }
                        //리스트 갱신
                        myFirestoreList = viewGroup.findViewById(R.id.firestore_list);
                        myFirestoreList.setLayoutManager(new LinearLayoutManager(getActivity())) ;
                        adapter = new RecyclerAdapter(food_list) ;
                        myFirestoreList.setAdapter(adapter) ;
                    }
                }
            }
        });

        //식재료 추가
        floatingActionButton=viewGroup.findViewById(R.id.floating);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                View dialogView = getLayoutInflater().inflate(R.layout.add_dialog,null);
                final EditText add_foodName = (EditText)dialogView.findViewById(R.id.add_food);
                final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setView(dialogView);
                builder.setPositiveButton("저장", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        String input_foodName = add_foodName.getText().toString();

                        firebaseFirestore.collection(SharedPref_id.getString(mContext,"myRef")).document("food")
                                .update(input_foodName,SharedPref_id.getString(mContext,"user"))
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        FragmentTransaction ft = getFragmentManager().beginTransaction();
                                        ft.detach(My_Food_List.this).attach(My_Food_List.this).commit();
                                        final Handler handler = new Handler();
                                        handler.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                myFirestoreList.setAdapter(adapter);
                                                swipeRefreshLayout.setRefreshing(false);
                                            }
                                        }, 500);
                                        final AlertDialog.Builder success_builder = new AlertDialog.Builder(getActivity());
                                        success_builder.setMessage("성공적으로 추가되었습니다.");
                                        success_builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                            }
                                        });
                                        AlertDialog sucessAlert = success_builder.create();
                                        sucessAlert.show();
                                    }
                                });
                        dialog.dismiss();
                    }
                });
                builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });


        //식재료 리스트 생성
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.detach(My_Food_List.this).attach(My_Food_List.this).commit();
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        myFirestoreList.setAdapter(adapter);
                        swipeRefreshLayout.setRefreshing(false);
                    }
                }, 500);
            }
        });





        return viewGroup;
    }


    public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {

        private ArrayList<String[]> mData = null ;

        // 아이템 뷰를 저장하는 뷰홀더 클래스.
        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView name_text ;
            TextView num_text;
            ImageView img;
            ViewHolder(final View itemView) {
                super(itemView) ;


                // 뷰 객체에 대한 참조. (hold strong reference)
                name_text = itemView.findViewById(R.id.name_text) ;
                num_text = itemView.findViewById(R.id.editional_text) ;

                img = itemView.findViewById(R.id.imageView_food);

                itemView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        final AlertDialog.Builder delete_builder = new AlertDialog.Builder(getActivity());
                        delete_builder.setMessage("삭제하시겠습니까?");
                        delete_builder.setPositiveButton("삭제", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                DocumentReference docRef = firebaseFirestore.collection(SharedPref_id.getString(mContext,"myRef")).document("food");
                                Map<String,Object> updates = new HashMap<>();
                                updates.put(mData.get(getAdapterPosition())[0], FieldValue.delete());
                                docRef.update(updates).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        FragmentTransaction ft = getFragmentManager().beginTransaction();
                                        ft.detach(My_Food_List.this).attach(My_Food_List.this).commit();
                                        final Handler handler = new Handler();
                                        handler.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                myFirestoreList.setAdapter(adapter);
                                                swipeRefreshLayout.setRefreshing(false);
                                            }
                                        }, 500);
                                        Log.d("success","good");

                                    }
                                });
                                //레시피 갱신
                                dialog.dismiss();
                            }
                        });
                        delete_builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        AlertDialog sucessAlert = delete_builder.create();
                        sucessAlert.show();
                        return true;
                    }
                });


                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int pos = getAdapterPosition();
                        if (pos != RecyclerView.NO_POSITION){
                            Intent intent = new Intent(getActivity(), Food_info_Activity.class);
                            intent.putExtra("name",mData.get(pos)[0]);
                            intent.putExtra("user",mData.get(pos)[1]);
                            startActivity(intent);
                            notifyItemChanged(pos);
                        }

                    }
                });
            }
        }

        // 생성자에서 데이터 리스트 객체를 전달받음.
        RecyclerAdapter(ArrayList<String[]> list) {
            mData = list ;
        }

        // onCreateViewHolder() - 아이템 뷰를 위한 뷰홀더 객체 생성하여 리턴.
        @Override
        public RecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            Context context = parent.getContext() ;
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) ;

            View view = inflater.inflate(R.layout.list_item_single, parent, false) ;
            RecyclerAdapter.ViewHolder vh = new RecyclerAdapter.ViewHolder(view) ;

            return vh ;
        }

        // onBindViewHolder() - position에 해당하는 데이터를 뷰홀더의 아이템뷰에 표시.
        @Override
        public void onBindViewHolder(RecyclerAdapter.ViewHolder holder, int position) {
            String name = mData.get(position)[0] ;
            String num = mData.get(position)[1] ;
            parser(mData.get(position)[0]);
            Glide.with(getActivity()).load(url).into(holder.img);
            holder.name_text.setText(name) ;
            holder.num_text.setText(num) ;
        }

        // getItemCount() - 전체 데이터 갯수 리턴.
        @Override
        public int getItemCount() {
            return mData.size() ;
        }
    }

    private void parser(String name){
        // 내부 xml파일이용시
        InputStream inputStream = getResources().openRawResource(R.raw.info_food);
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        BufferedReader reader = new BufferedReader(inputStreamReader);
        XmlPullParserFactory factory = null;
        XmlPullParser xmlParser = null;
        boolean check = false;
        boolean isName = false, isUrl = false, isUrlText=false;


        try {
            factory = XmlPullParserFactory.newInstance();
            xmlParser = factory.newPullParser();
            xmlParser.setInput(reader);
            int eventType = xmlParser.getEventType();

            while (eventType != XmlPullParser.END_DOCUMENT){
                switch (eventType){

                    case XmlPullParser.START_TAG :
                        tagName = xmlParser.getName();
                        if(tagName.equals("name")){
                            isName=true;
                        }
                        else if(tagName.equals("url")){
                            if(check==true){
                                isUrl=true;
                            }
                        }
                        break;

                    case XmlPullParser.TEXT:
                        if(isName==true) {
                            if (xmlParser.getText().equals(name)) {
                                check = true;
                            }
                        }
                        if(isUrl == true) {
                            if(check==true){
                                url = xmlParser.getText();
                            }
                        }

                    case XmlPullParser.END_TAG:
                        switch (tagName) {
                            case "name" :
                                isName = false;
                                break;
                            case "url" :
                                isUrl = false;
                                check=false;
                                break;
                            default:
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

            }catch(Exception e2){
                e2.printStackTrace();
            }
        }

    }


}
