package com.example.srt_project;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

public class Recipe_list_fragment extends Fragment {

    ViewGroup viewGroup;
    private RecyclerView myFirestoreList;
    public FirebaseFirestore firebaseFirestore;
    private ArrayList<String[]> recipe_detail_list;
    private ArrayList<String> recipe_list;
    public SwipeRefreshLayout swipeRefreshLayout;
    public String recipe_url;
    private Context mContext;
    private RecyclerAdapter adapter;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        viewGroup = (ViewGroup) inflater.inflate(R.layout.recyclerview_fragment, container, false);
        mContext=this.getContext();
        recipe_detail_list = new ArrayList<>();
        recipe_list = new ArrayList<>();
        firebaseFirestore = FirebaseFirestore.getInstance();
        //식재료 api 크롤링 주소
        recipe_url = "http://211.237.50.150:7080/openapi/2e736f9835dcd6eeb1f1ce6042c471dc83d0385b1dfec20d8a062611836c2340/xml/Grid_20150827000000000226_1/1/1000";
        myFirestoreList = viewGroup.findViewById(R.id.firestore_list);
        myFirestoreList.setLayoutManager(new LinearLayoutManager(getActivity()));

        //firestore의 recipeid를 사용해 크롤링 후 리스트 띄우기
        DocumentReference docRef = firebaseFirestore.collection(SharedPref_id.getString(mContext,"myRef")).document("recipe");
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Iterator<String> recipes = document.getData().keySet().iterator();
                        while (recipes.hasNext()) {
                            String recipe = recipes.next();
                            recipe_list.add(recipe);
                        }
                        new Description().execute();


                    }
                }
            }
        });

        //새로고침
        swipeRefreshLayout = viewGroup.findViewById(R.id.swipe_layout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.detach(Recipe_list_fragment.this).attach(Recipe_list_fragment.this).commit();
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        swipeRefreshLayout.setRefreshing(false);
                    }
                }, 500);
            }
        });

        FloatingActionButton floatingActionButton=viewGroup.findViewById(R.id.floating);
        floatingActionButton.hide();
        return viewGroup;
    }


    //레시피 갱신
    private class Description extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            try {
                Document doc = Jsoup.connect(recipe_url).get();
                Elements search_recipes = doc.select("row");

                for (Element search_recipe : search_recipes) {

                    for (int i = 0; i < recipe_list.size(); i++) {
                        if (recipe_list.get(i).equals(search_recipe.select("RECIPE_ID").text())) {
                            recipe_detail_list.add(new String[]{search_recipe.select("RECIPE_ID").text(),search_recipe.select("RECIPE_NM_KO").text(), search_recipe.select("IMG_URL").text()});
                        }
                    }
                }


            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            adapter = new RecyclerAdapter(recipe_detail_list);
            myFirestoreList.setAdapter(adapter);

        }
    }


        public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {

            private ArrayList<String[]> mData = null;

            // 아이템 뷰를 저장하는 뷰홀더 클래스.
            public class ViewHolder extends RecyclerView.ViewHolder {
                TextView recipe_text;
                ImageView recipe_imageView;

                ViewHolder(final View itemView) {
                    super(itemView);

                    // 뷰 객체에 대한 참조. (hold strong reference)
                    recipe_text = itemView.findViewById(R.id.recipe_text);
                    recipe_imageView = itemView.findViewById(R.id.recipeImage);

                    itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            int pos = getAdapterPosition();
                            if (pos != RecyclerView.NO_POSITION) {

                                Intent intent = new Intent(getActivity(), Recipe_detail_Activity.class);
                                intent.putExtra("recipe_id",mData.get(pos)[0]);
                                intent.putExtra("recipe_name", mData.get(pos)[1]);
                                intent.putExtra("recipe_img", mData.get(pos)[2]);
                                startActivity(intent);

                                notifyItemChanged(pos);
                            }
                        }
                    });
                }
            }

            // 생성자에서 데이터 리스트 객체를 전달받음.
            RecyclerAdapter(ArrayList<String[]> list) {
                mData = list;
            }

            // onCreateViewHolder() - 아이템 뷰를 위한 뷰홀더 객체 생성하여 리턴.
            @Override
            public RecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                Context context = parent.getContext();
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

                View view = inflater.inflate(R.layout.recipe_list, parent, false);
                RecyclerAdapter.ViewHolder vh = new RecyclerAdapter.ViewHolder(view);

                return vh;
            }

            // onBindViewHolder() - position에 해당하는 데이터를 뷰홀더의 아이템뷰에 표시.
            @Override
            public void onBindViewHolder(RecyclerAdapter.ViewHolder holder, int position) {
                String recipe_text = mData.get(position)[1];
                String recipe_image = mData.get(position)[2];

                Glide.with(getActivity()).load(recipe_image).into(holder.recipe_imageView);
                holder.recipe_text.setText(recipe_text);
            }

            // getItemCount() - 전체 데이터 갯수 리턴.
            @Override
            public int getItemCount() {
                return mData.size();
            }
        }


}
