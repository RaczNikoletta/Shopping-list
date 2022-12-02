package com.example.shoppinglist;

import android.app.Application;
import android.util.Log;

import io.realm.Realm;
import io.realm.RealmConfiguration;

public class realmInit
    extends Application {


        @Override
        public void onCreate() {
            Log.v("Thread name: " ,Thread.currentThread().getName());
            super.onCreate();
            Realm.init(getApplicationContext());
            //for basic realm
            //RealmConfiguration configuration = new RealmConfiguration.Builder().name("RealmData.realm").build();
            Realm.setDefaultConfiguration(initializeRealmConfig());
        }

        public RealmConfiguration initializeRealmConfig() {

            return new RealmConfiguration.Builder()
                    .name("RealmData.realm")
                    .schemaVersion(10)
                    .allowWritesOnUiThread(true)
                    .build();

        }


    }
