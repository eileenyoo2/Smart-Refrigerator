package com.example.srt_project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;


public class HomeActivity extends AppCompatActivity {
    BottomNavigationView bottomNavigationView;
    Food_list_fragment fragment1;
    Recipe_list_fragment fragment2;
    My_Food_List fragment3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        fragment1 = new Food_list_fragment();
        fragment2 = new Recipe_list_fragment();
        fragment3 = new My_Food_List();
        getSupportFragmentManager().beginTransaction().replace(R.id.main_layout, fragment1).commitAllowingStateLoss();

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.tab1: {
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.main_layout, fragment1).commitAllowingStateLoss();
                        return true;
                    }
                    case R.id.tab2: {
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.main_layout, fragment2).commitAllowingStateLoss();
                        return true;
                    }
                    case R.id.tab3: {
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.main_layout, fragment3).commitAllowingStateLoss();
                        return true;
                    }

                    default:
                        return false;
                }
            }
        });

    }
}
