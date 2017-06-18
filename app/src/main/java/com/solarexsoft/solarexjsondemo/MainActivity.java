package com.solarexsoft.solarexjsondemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.solarexsoft.solarexjson.SolarexJSON;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "Solarex";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void fromJSON(View v) {
        News news = new News();
        news.setId(1);
        news.setTitle("新年放假通知");
        news.setContent("从今天开始放假啦。");
        news.setAuthor(createAuthor());
        news.setReader(createReaders());
        String json = SolarexJSON.toJSON(news);
        News generated = (News) SolarexJSON.fromJSON(json, News.class);
        Log.d(TAG, "fromJSON: " + generated);
    }

    public void toJSON(View view) {
        News news = new News();
        news.setId(1);
        news.setTitle("新年放假通知");
        news.setContent("从今天开始放假啦。");
        news.setAuthor(createAuthor());
        news.setReader(createReaders());
        Log.d(TAG, "toJSON: " + SolarexJSON.toJSON(news));
    }

    private static List<User> createReaders() {
        List<User> readers = new ArrayList<User>();
        User readerA = new User();
        readerA.setId(2);
        readerA.setName("Jack");

        readers.add(readerA);

        User readerB = new User();
        readerB.setId(1);
        readerB.setName("Lucy");
        readerB.setPwd("123456789");
        readers.add(readerB);

        return readers;
    }

    private static User createAuthor() {
        User author = new User();
        author.setId(1);
        author.setName("Fancyy");
        author.setPwd("123456");
        return author;
    }
}
