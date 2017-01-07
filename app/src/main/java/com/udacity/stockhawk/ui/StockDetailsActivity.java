package com.udacity.stockhawk.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.utils.EntryXComparator;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.utils.DateAxisValueFormatter;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Collections;

import butterknife.BindView;
import butterknife.ButterKnife;


public class StockDetailsActivity extends AppCompatActivity {

    @BindView(R.id.line_chart)
    LineChart mLineChart;
    private ArrayList<Entry> stocks;
    private Long mReferenceTimestamp;
    private String mName;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_details);

        Intent detailIntent = getIntent();
        mName = detailIntent.getStringExtra(MainActivity.STOCK_NAME);
        String history = detailIntent.getStringExtra(MainActivity.STOCK_HISTORY);
        try {
            JSONArray historyArray = new JSONArray(history);
            stocks = new ArrayList<>();
            mReferenceTimestamp = historyArray.getJSONObject(0).getLong("close");
            for (int i = 0; i < historyArray.length() - 1; i++) {
                Long date = historyArray.getJSONObject(i).getLong("date");
                Long close = historyArray.getJSONObject(i).getLong("close");
                stocks.add(i, new Entry(date - mReferenceTimestamp, close.floatValue()));
            }

        } catch (Throwable throwable) {
            Toast.makeText(this, R.string.error_no_stocks, Toast.LENGTH_SHORT).show();
        }

        ButterKnife.bind(this);

        LineDataSet dataset = new LineDataSet(stocks, mName);
        Collections.sort(stocks, new EntryXComparator());
        dataset.setDrawCircles(true);
        dataset.setDrawFilled(true);

        LineData data = new LineData(dataset);
        data.setDrawValues(true);

        IAxisValueFormatter xAxisFormatter = new DateAxisValueFormatter(mReferenceTimestamp);
        XAxis xAxis = mLineChart.getXAxis();
        xAxis.setValueFormatter(xAxisFormatter);

        mLineChart.setData(data);
        mLineChart.setDescription(null);
        mLineChart.invalidate();
        mLineChart.animateY(1000);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}