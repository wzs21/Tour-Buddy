package com.gooftroop.tourbuddy;


import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.gooftroop.tourbuddy.R;
import com.google.android.gms.internal.no;
import com.google.android.gms.internal.ok;

import java.util.List;

public class NoteListActivity extends ListActivity {

    private Activity curActivity = this;

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        DataSource db = new DataSource(curActivity);
        db.open();
        List<TourNote> notes = db.getAllNotes();
        db.close();

        String[] noteStrArr = new String[notes.size()];

        for (int i = 0; i<notes.size(); i++)
        {
            noteStrArr[i] = notes.get(i).getNote();
        }

        // use your custom layout
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(curActivity,
                R.layout.note_list_item, R.id.textViewNote, noteStrArr);
        setListAdapter(adapter);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        String item = (String) getListAdapter().getItem(position);
}
} 