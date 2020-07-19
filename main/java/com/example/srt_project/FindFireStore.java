package com.example.srt_project;

import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;

public class FindFireStore {
    public static final FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();

    public boolean getCollection (String id) {

        if(id.equals("srt-1")) {
            return true;
        }
        return false;
    }

}

// 이곳에 추가하기