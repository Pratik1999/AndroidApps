package com.example.sunny.memoriableplaces;

import android.app.AlertDialog;
import android.app.LauncherActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    static ArrayList<String> list = new ArrayList<>();
    static ArrayList<LatLng> addressArray =new ArrayList<>();
    static ArrayAdapter adapter;
    ListView listView;
    SQLiteDatabase db;

    public void displayDialog(final int index)
    {
        Toast.makeText(getApplicationContext(),"LONG_PRESSED",Toast.LENGTH_LONG).show();
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_delete)
                .setMessage("DO YOU REALLY WANT TO DELETE")
                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        deleteRow(String.valueOf(addressArray.get(index).latitude),String.valueOf(addressArray.get(index).longitude));
                        Toast.makeText(getApplicationContext(),"DELETED",Toast.LENGTH_LONG).show();

                    }
                })
                .setNegativeButton("NO",null)
                .show();
    }

    protected void deleteRow(String lat,String lang) {
        Log.i("inside","deleteRow()");
        try {
            db.execSQL("DELETE FROM placesTable WHERE latitude=" + lat + " AND longitude=" + lang);
            Toast.makeText(getApplicationContext(),"DELETED",Toast.LENGTH_LONG).show();
            list.clear();
            getData();
            adapter.notifyDataSetChanged();
        }catch (Exception e)
        {
            Log.i("sql2 MainActivity ",e.toString());
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView =(ListView)findViewById(R.id.listView);
        list.clear();
        addressArray.clear();
        getData();
       adapter = new ArrayAdapter(getApplicationContext(),android.R.layout.simple_list_item_1,list);
       listView.setAdapter(adapter);



     listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
         @Override
         public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {

                if(i!=0)
                {
                    displayDialog(i);
                }
             return true;
         }
     });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(getApplicationContext(),MapsActivity.class);
                intent.putExtra("PlaceNumber",i);
                Log.i("index",String.valueOf(i));
                startActivity(intent);

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

    Log.i("inside","on resume");

    }

    void getData()
    {
        list.add("ADD A NEW PLACE....");
        addressArray.add(new LatLng(0,0));
        try
        {
            db= this.openOrCreateDatabase("places",MODE_PRIVATE,null);
            db.execSQL("CREATE TABLE IF NOT EXISTS placesTable (place VARCHAR,latitude VARCHAR,longitude VARCHAR)");
            Cursor c= db.rawQuery("SELECT * FROM placesTable",null);
            int placeIndex=c.getColumnIndex("place");
            int latIndex=c.getColumnIndex("latitude");
            int langIndex=c.getColumnIndex("longitude");
            c.moveToFirst();
            while (c !=null)
            {
                Log.i("place",c.getString(placeIndex));
                list.add(c.getString(placeIndex));
                Log.i("latitude",c.getString(latIndex));
                addressArray.add(new LatLng(Double.parseDouble(c.getString(latIndex)),Double.parseDouble(c.getString(langIndex))));
                Log.i("longitude",c.getString(langIndex));
                c.moveToNext();
            }

        }catch(Exception e)
        {
            Log.i("MainActivity Sql Error",e.toString());
        }
    }
}
