package com.marnistek.serverstats.Fragments;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.YAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.marnistek.serverstats.Database.DataBaseAdapter;
import com.marnistek.serverstats.R;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class ChartFragmentMonths extends Fragment implements OnChartValueSelectedListener {

    LineChart lineChart;
    BarChart barChart;
    LineDataSet lineDataSet;
    BarDataSet barDataSet;
    TableLayout tableLayout;
    String dataType;
    View v;
    Spinner spinnerBytes;
    String col;
    String selectedInterface;
    String hostFromBundle;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_chart, container, false);

        lineChart = (LineChart) v.findViewById(R.id.line_chart);
        barChart = (BarChart) v.findViewById(R.id.bar_chart);
        tableLayout = (TableLayout) v.findViewById(R.id.tableLayoutData);

        spinnerBytes = (Spinner) v.findViewById(R.id.spinner2);
        spinnerBytes.setSelection(0);

        dataType = getArguments().getString("key");
        hostFromBundle = getArguments().getString("hkey");
        selectedInterface = getArguments().getString("ikey");

        spinnerBytes.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(v.getContext());
            String chartType = SP.getString("chartType","1");
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position == 0){
                    col = "tx";
                    if(chartType.equals("1")){
                        lineChart.setVisibility(View.VISIBLE);
                        barChart.setVisibility(View.GONE);
                        BuildLineChart(col, selectedInterface, hostFromBundle);
                    }else if(chartType.equals("2")){
                        barChart.setVisibility(View.VISIBLE);
                        lineChart.setVisibility(View.GONE);
                        BuildBarChart(col, selectedInterface, hostFromBundle);
                    }
                } else if(position == 1){
                    col = "rx";
                    if(chartType.equals("1")) {
                        lineChart.setVisibility(View.VISIBLE);
                        barChart.setVisibility(View.GONE);
                        BuildLineChart(col, selectedInterface, hostFromBundle);
                    }else if(chartType.equals("2")){
                        barChart.setVisibility(View.VISIBLE);
                        lineChart.setVisibility(View.GONE);
                        BuildBarChart(col, selectedInterface, hostFromBundle);
                    }
                }else if(position == 2) {
                    col = "total";
                    if(chartType.equals("1")) {
                        lineChart.setVisibility(View.VISIBLE);
                        barChart.setVisibility(View.GONE);
                        BuildLineChart(col, selectedInterface, hostFromBundle);
                    }else if(chartType.equals("2")){
                        barChart.setVisibility(View.VISIBLE);
                        lineChart.setVisibility(View.GONE);
                        BuildBarChart(col, selectedInterface, hostFromBundle);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        BuildTable(selectedInterface, hostFromBundle);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {
        int i = (int)e.getVal();
        Log.i("VAL SELECTED",
                "Value: " + i + ", xIndex: " + e.getXIndex()
                        + ", DataSet index: " + dataSetIndex);
    }

    @Override
    public void onNothingSelected() {

    }

    private void BuildLineChart(String column, String ifce, String host){

        DataBaseAdapter mDbHelper = new DataBaseAdapter(v.getContext());
        mDbHelper.createDatabase();
        mDbHelper.open();

        Cursor getData = mDbHelper.getGraphData(dataType, ifce, host);

        getData.moveToFirst();

        ArrayList<Entry> entries = new ArrayList<>();

        lineDataSet = new LineDataSet(entries, spinnerBytes.getSelectedItem().toString());

        ArrayList<String> labels = new ArrayList<>();

        while (!getData.isAfterLast()) {
            entries.add(new Entry(getData.getLong(getData.getColumnIndexOrThrow(column)),getData.getPosition()));
            labels.add(getData.getString(getData.getColumnIndexOrThrow("xData")));
            getData.moveToNext();
        }

        mDbHelper.close();

        LineData data = new LineData(labels, lineDataSet);
        //lineDataSet.setColors(ColorTemplate.COLORFUL_COLORS); //
        //lineDataSet.setDrawCubic(true);
        lineDataSet.setDrawFilled(true);
        lineDataSet.setDrawValues(false);
        lineDataSet.setHighlightEnabled(true);
        lineDataSet.setDrawHighlightIndicators(true);

        if (lineChart != null) {
            lineChart.setData(data);
            lineChart.animateY(3000);
            lineChart.setDescription("");
            lineChart.setOnChartValueSelectedListener(this);
            lineChart.setHighlightPerTapEnabled(true);
            lineChart.setTouchEnabled(true);
            lineChart.getAxisLeft().setValueFormatter(new MyValueFormatter());
            lineChart.getAxisRight().setEnabled(false);

        }
    }

    private void BuildBarChart(String column, String ifce, String host){

        DataBaseAdapter mDbHelper = new DataBaseAdapter(v.getContext());
        mDbHelper.createDatabase();
        mDbHelper.open();

        Cursor getData = mDbHelper.getGraphData(dataType, ifce, host);

        getData.moveToFirst();

        ArrayList<BarEntry> entries = new ArrayList<>();

        barDataSet = new BarDataSet(entries, spinnerBytes.getSelectedItem().toString());

        ArrayList<String> labels = new ArrayList<>();

        while (!getData.isAfterLast()) {
            entries.add(new BarEntry(getData.getLong(getData.getColumnIndexOrThrow(column)),getData.getPosition()));
            labels.add(getData.getString(getData.getColumnIndexOrThrow("xData")));
            getData.moveToNext();
        }

        mDbHelper.close();

        BarData data = new BarData(labels, barDataSet);
        barDataSet.setDrawValues(false);
        barDataSet.setHighlightEnabled(true);

        if (barChart != null) {
            barChart.setData(data);
            barChart.animateY(3000);
            barChart.setDescription("");
            barChart.getAxisLeft().setValueFormatter(new MyValueFormatter());
            barChart.getAxisRight().setEnabled(false);

        }
    }

    private void BuildTable(String ifce, String host){

        DataBaseAdapter mDbHelper = new DataBaseAdapter(v.getContext());
        mDbHelper.createDatabase();
        mDbHelper.open();

        Cursor getData = mDbHelper.getTableData(dataType, ifce, host);

        int rows = getData.getCount();
        int cols = getData.getColumnCount();

        getData.moveToFirst();

        for (int i = 0; i < rows; i++) {

            TableRow row = new TableRow(v.getContext());
            row.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,
                    TableRow.LayoutParams.WRAP_CONTENT));

            // inner for loop
            for (int j = 0; j < cols; j++) {

                TextView tv = new TextView(v.getContext());
                tv.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,
                        TableRow.LayoutParams.WRAP_CONTENT));
                if(j == 0) {
                    tv.setGravity(Gravity.START);
                } else {
                    tv.setGravity(Gravity.CENTER);
                }
                //tv.setTextSize(18);
                tv.setPadding(0, 5, 0, 5);

                tv.setText(getData.getString(j));

                row.addView(tv);

            }

            getData.moveToNext();

            tableLayout.addView(row);
        }
        mDbHelper.close();
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshFragment();
    }

    private void refreshFragment(){
        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(v.getContext());
        String chartType = SP.getString("chartType","1");
        int position = spinnerBytes.getSelectedItemPosition();
        if(position == 0){
            col = "tx";
            if(chartType.equals("1")){
                lineChart.setVisibility(View.VISIBLE);
                barChart.setVisibility(View.GONE);
                BuildLineChart(col, selectedInterface, hostFromBundle);
            }else if(chartType.equals("2")){
                barChart.setVisibility(View.VISIBLE);
                lineChart.setVisibility(View.GONE);
                BuildBarChart(col, selectedInterface, hostFromBundle);
            }
        } else if(position == 1){
            col = "rx";
            if(chartType.equals("1")) {
                lineChart.setVisibility(View.VISIBLE);
                barChart.setVisibility(View.GONE);
                BuildLineChart(col, selectedInterface, hostFromBundle);
            }else if(chartType.equals("2")){
                barChart.setVisibility(View.VISIBLE);
                lineChart.setVisibility(View.GONE);
                BuildBarChart(col, selectedInterface, hostFromBundle);
            }
        }else if(position == 2) {
            col = "total";
            if(chartType.equals("1")) {
                lineChart.setVisibility(View.VISIBLE);
                barChart.setVisibility(View.GONE);
                BuildLineChart(col, selectedInterface, hostFromBundle);
            }else if(chartType.equals("2")){
                barChart.setVisibility(View.VISIBLE);
                lineChart.setVisibility(View.GONE);
                BuildBarChart(col, selectedInterface, hostFromBundle);
            }
        }
        //BuildTable(selectedInterface, hostFromBundle);
    }

    public class MyValueFormatter implements YAxisValueFormatter {

        private DecimalFormat mFormat;

        public MyValueFormatter() {
            mFormat = new DecimalFormat("###,###,##0.00"); // use one decimal
        }

        @Override
        public String getFormattedValue(float value, YAxis yAxis) {
            float kToM = 1024;
            float kToG = 1048576;
            float kToT = 1073741824;

            String byteTypeF = "";
            float data = 0;

            if(value < kToM) {
                data = value;
                byteTypeF = "KB";
            } else if(value > kToM && value < kToG) {
                data = value / kToM;
                byteTypeF = "MB";
            } else if(value > kToG && value < kToT) {
                data = value / kToG;
                byteTypeF = "GB";
            } else if(value > kToT) {
                data = value / kToT;
                byteTypeF = "TB";
            }

            double yData = ((double)data);
            //return Math.round(yData * 100.0) / 100.0 + " " + byteTypeF;
            return mFormat.format(yData) + " " + byteTypeF;
        }
    }
}
