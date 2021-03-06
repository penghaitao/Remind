package com.wartechwick.remind.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.wartechwick.remind.Utils;
import com.wartechwick.remind.clipboard.ClipboardManagerCompat;
import com.wartechwick.remind.db.Word;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import io.realm.Realm;
import io.realm.RealmQuery;

public final class ListenClipboardService extends Service {

    private static final String KEY_FOR_WEAK_LOCK = "weak-lock";
    private static final String KEY_FOR_CMD = "cmd";
    private static final String KEY_FOR_CONTENT = "content";
    private static final String CMD_TEST = "test";

    private static Realm realm;

    private static CharSequence sLastContent = null;
    private ClipboardManagerCompat mClipboardWatcher;
    private ClipboardManagerCompat.OnPrimaryClipChangedListener mOnPrimaryClipChangedListener = new ClipboardManagerCompat.OnPrimaryClipChangedListener() {
        public void onPrimaryClipChanged() {
            performClipboardCheck();
        }
    };

    public static void start(Context context) {
        Intent serviceIntent = new Intent(context, ListenClipboardService.class);
        context.startService(serviceIntent);
    }

    /**
     * for dev
     */
    public static void startForTest(Context context, String content) {

        Intent serviceIntent = new Intent(context, ListenClipboardService.class);
        serviceIntent.putExtra(KEY_FOR_CMD, CMD_TEST);
        serviceIntent.putExtra(KEY_FOR_CONTENT, content);
        context.startService(serviceIntent);
    }

    public static void startForWeakLock(Context context, Intent intent) {

        Intent serviceIntent = new Intent(context, ListenClipboardService.class);
        context.startService(serviceIntent);

        intent.putExtra(ListenClipboardService.KEY_FOR_WEAK_LOCK, true);
        Intent myIntent = new Intent(context, ListenClipboardService.class);

        // using wake lock to start service
        WakefulBroadcastReceiver.startWakefulService(context, myIntent);
    }

    @Override
    public void onCreate() {
        Log.i("pp", "dlsfjds");
        realm = Realm.getDefaultInstance();
        mClipboardWatcher = ClipboardManagerCompat.create(this);
        mClipboardWatcher.addPrimaryClipChangedListener(mOnPrimaryClipChangedListener);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mClipboardWatcher.removePrimaryClipChangedListener(mOnPrimaryClipChangedListener);

        sLastContent = null;
        realm.close();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Utils.printIntent("onStartCommand", intent);

        if (intent != null) {
            // remove wake lock
            if (intent.getBooleanExtra(KEY_FOR_WEAK_LOCK, false)) {
                BootCompletedReceiver.completeWakefulIntent(intent);
            }
            String cmd = intent.getStringExtra(KEY_FOR_CMD);
            if (!TextUtils.isEmpty(cmd)) {
                if (cmd.equals(CMD_TEST)) {
                    String content = intent.getStringExtra(KEY_FOR_CONTENT);
                    showContent(content);
                }
            }
        }
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void performClipboardCheck() {
        Log.i("pp", "dlsfjds");
        CharSequence content = mClipboardWatcher.getText();
        if (TextUtils.isEmpty(content)) {
            return;
        }
        showContent(content);
    }

    private void showContent(CharSequence content) {
        if (sLastContent != null && sLastContent.equals(content) || content == null) {
            return;
        }
        String text = String.valueOf(content);
        boolean isWord=text.matches("[a-zA-Z\\s]+");
        sLastContent = content;
        if (isWord && !checkIfExists(text) && !text.startsWith("http")) {
            realm.beginTransaction();
            Word word = realm.createObject(Word.class);
            Toast.makeText(getApplicationContext(), text.toLowerCase(), Toast.LENGTH_LONG).show();
            word.setText(text.toLowerCase());
            Calendar c = Calendar.getInstance();
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            String formattedDate = df.format(c.getTime());
            word.setAddedDate(formattedDate);
            word.setDeleted(false);
            word.setSelected(false);
            realm.commitTransaction();
        }
    }

    private boolean checkIfExists(String text){

        RealmQuery<Word> query = realm.where(Word.class)
                .equalTo("text", text);

        return query.count() != 0;
    }

}