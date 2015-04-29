package com.gooftroop.tourbuddy;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;


public class SearchBuildingActivity extends Activity implements TextWatcher {
    ListView resultListView;
    EditText searchTextVeiw;
    SearchAdapter adapter;
    ArrayList<String> buildingList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_building);
        buildingList = new ArrayList<>();
        getActionBar().setDisplayHomeAsUpEnabled(true);
        searchTextVeiw = (EditText) findViewById(R.id.search_text);
        searchTextVeiw.addTextChangedListener(this);
        resultListView = (ListView) findViewById(R.id.results);
        adapter = new SearchAdapter(this,R.layout.search_list_item,buildingList);
        resultListView.setAdapter(adapter);
        resultListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String buildingname = (String) view.getTag();
                Intent i = getIntent();
                i.putExtra("buildingName", buildingname);
                setResult(RESULT_OK, i);
                finish();
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_search_building, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if(id == android.R.id.home){
            finish();
        }


        return super.onOptionsItemSelected(item);
    }

    private void onResultReceived(List<String> buildingNames){
        buildingList.clear();
        buildingList.addAll(buildingNames);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        //DO Nothing
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        String searchText = s.toString();
        //TODO query for building names, add names to a list then give them to onResultReceived
        ArrayList<String> results = new ArrayList<>();
        for(int i = 0; i < 10; i++){
            results.add(searchText + i);
        }
        onResultReceived(results);
    }

    @Override
    public void afterTextChanged(Editable s) {
        //DO Nothing
    }

    class SearchAdapter extends ArrayAdapter<String>{

        List items;
        Context context;
        public SearchAdapter(Context context, int resource, List objects) {
            super(context, resource, objects);
            items = objects;
            this.context = context;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(R.layout.search_list_item, parent, false);
            TextView textView = (TextView) rowView.findViewById(R.id.building_name);

            textView.setText((String) items.get(position));
            rowView.setTag(items.get(position));

            return rowView;
        }
    }

}
