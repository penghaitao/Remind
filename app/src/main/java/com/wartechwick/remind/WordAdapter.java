package com.wartechwick.remind;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.wartechwick.remind.db.Word;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmResults;

import static android.content.Context.CLIPBOARD_SERVICE;

/**
 * Created by penghaitao on 2016-11-29.
 */

public class WordAdapter extends RecyclerView.Adapter<WordAdapter.ViewHolder> {

    private RealmResults<Word> mWords;
    private Context mContext;

    public WordAdapter(Context context, RealmResults<Word> records) {
        mContext = context;
        mWords = records;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View itemView = inflater.inflate(R.layout.item_word, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Word word = mWords.get(position);
        holder.nameView.setText(word.getText());
        holder.dateView.setText(word.getAddedDate());
    }

    @Override
    public int getItemCount() {
        return mWords.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        @BindView(R.id.tv_word_name)
        TextView nameView;
        @BindView(R.id.btn_delete)
        Button deleteView;
        @BindView(R.id.tv_date)
        TextView dateView;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            nameView.setOnClickListener(this);
            deleteView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.tv_word_name:
                    ClipboardManager clipboard = (ClipboardManager) App.getContext().getSystemService(CLIPBOARD_SERVICE);
                    ClipData clipData = ClipData.newPlainText("test", nameView.getText());
                    clipboard.setPrimaryClip(clipData);
                    break;
                case R.id.btn_delete:
                    Realm realm = Realm.getDefaultInstance();
                    realm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            Word word = mWords.get(getPosition());
                            word.deleteFromRealm();
                        }
                    });
                    break;
            }
        }
    }
}
