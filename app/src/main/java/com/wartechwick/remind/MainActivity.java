package com.wartechwick.remind;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.wartechwick.remind.db.Word;
import com.wartechwick.remind.service.ListenClipboardService;
import com.wartechwick.remind.util.PreferencesLoader;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.main_recycler_view)
    RecyclerView recyclerView;

    private Realm realm;
    private WordAdapter wordAdapter;
    private RealmResults<Word> results;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        Intent intent = getIntent();
        Utils.printIntent("MainActivity::onCreate()", intent);

        ListenClipboardService.start(this);
        realm = Realm.getDefaultInstance();
        setupRecyclerView();
        results.addChangeListener(new RealmChangeListener<RealmResults<Word>>() {
            @Override
            public void onChange(RealmResults<Word> element) {
                if (wordAdapter != null) {
                    wordAdapter.notifyDataSetChanged();
                } else {
                    setAdapter();
                }
            }
        });
        AlarmManagerUtils.register(this);
    }



    private void setupRecyclerView() {
        results = realm.where(Word.class).findAll();
        if (results.size() != 0) {
            setAdapter();
        }
    }

    private void setAdapter() {
        wordAdapter = new WordAdapter(this, results);
        recyclerView.setAdapter(wordAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close(); // Remember to close Realm when done.
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem item = menu.findItem(R.id.action_cancel);
        initHighResItemState(item);
        return true;
    }

    private void initHighResItemState(MenuItem item) {
        PreferencesLoader loader = new PreferencesLoader(this);
        item.setChecked(loader.getBoolean(R.string.action_cancel, false));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean isChecked = !item.isChecked();
        item.setChecked(isChecked);
        PreferencesLoader loader = new PreferencesLoader(this);
        loader.saveBoolean(R.string.action_cancel, isChecked);
        Toast.makeText(this, getResources().getString(isChecked ? R.string.cancel:R.string.not_cancel), Toast.LENGTH_SHORT).show();
        if (isChecked) {
            AlarmManagerUtils.unRegister(this);
        } else {
            AlarmManagerUtils.register(this);
        }
        return true;
    }
}
