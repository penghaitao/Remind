package com.wartechwick.remind.service;

import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.wartechwick.remind.App;
import com.wartechwick.remind.db.Word;

import java.util.Random;

import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;

import static android.content.Context.CLIPBOARD_SERVICE;

/**
 * Created by drakeet on 7/1/15.
 */
public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                RealmQuery<Word> query = realm.where(Word.class);
                query.equalTo("selected", false);
                RealmResults<Word> words = query.findAll();
                if (words.size()>0) {
                    Word word = words.get(new Random().nextInt(words.size()));
                    word.setSelected(true);
                    ClipboardManager clipboard = (ClipboardManager) App.getContext().getSystemService(CLIPBOARD_SERVICE);
                    ClipData clipData = ClipData.newPlainText("test", word.getText());
                    clipboard.setPrimaryClip(clipData);
                    Toast.makeText(App.getContext(), word.getText() + words.size(), Toast.LENGTH_LONG).show();
                } else {
                    RealmResults<Word> results = realm.where(Word.class).findAll();
                    for (int i=0; i<results.size(); i++) {
                        results.get(i).setSelected(false);
                    }
                }
            }
        });

    }
}