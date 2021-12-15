package com.edgesoft.resulthour;

import android.content.Intent;

import com.edgesoft.resulthour.Adapter.ViewResultAdapter;
import com.edgesoft.resulthour.Model.ResultData;
import com.github.clemp6r.futuroid.Async;
import com.github.clemp6r.futuroid.Future;
import com.github.clemp6r.futuroid.FutureCallback;
import com.google.android.gms.ads.AdView;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.Gravity;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.edgsoft.resulthour.R;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

public class ViewResultActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private List<Object> dataList = new ArrayList<>();
    private String url;
    TextView centerText;
    private ProgressBar pBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_result);


        pBar = findViewById(R.id.view_result_pbar);
        recyclerView = findViewById(R.id.view_result_recycler);
        layoutManager = new LinearLayoutManager(this);
        adapter = new ViewResultAdapter(dataList);
        centerText = findViewById(R.id.view_result_center_text);
        Intent intent = getIntent();
        url = intent.getStringExtra("url");

        GetStudentList list = new GetStudentList();
        pBar.setVisibility(View.VISIBLE);
        list.getStudentData(url).addCallback(new FutureCallback<List<Object>>() {
            @Override
            public void onSuccess(List<Object> result) {
                addBannerAds();
                pBar.setVisibility(View.GONE);
                loadRecyclerView();
            }

            @Override
            public void onFailure(Throwable t) {
                pBar.setVisibility(View.GONE);
                Toast toast = Toast.makeText(ViewResultActivity.this, "No result found.", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER,0,0);
                toast.show();
            }
        });
    }

    public class GetStudentList {

        public Future<List<Object>> getStudentData(final String url) {
            return Async.submit(new Callable<List<Object>>() {
                @Override
                public List<Object> call() {

                    Document document = null;
                    try {
                        document = Jsoup.connect(url).get();
                        for (org.jsoup.nodes.Element table : document.select("table[class=table table-striped]")) {
                            for (org.jsoup.nodes.Element row : table.select("tbody").select("tr")) {
                                Elements tds = row.select("td");
                                String name = tds.get(1).text();
                                String roll = tds.get(2).text();
                                String url  = tds.get(3).children().select("a").attr("href");
                                ResultData data = new ResultData(name,roll,url,getIntent().getStringExtra("university"));
                                dataList.add(data);
                            }
                        }
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                    return dataList;
                }
            });
        }
    }

    private void addBannerAds() {
        for (int i=4;i<dataList.size();i+=4) {
            dataList.add(i,new AdView(ViewResultActivity.this));
        }
    }

    public void loadRecyclerView() {
        if(dataList.size() > 0) {
            recyclerView.setLayoutManager(layoutManager);
            recyclerView.setAdapter(adapter);
        }
    }
}