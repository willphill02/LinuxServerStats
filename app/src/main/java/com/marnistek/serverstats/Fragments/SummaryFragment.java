package com.marnistek.serverstats.Fragments;

import android.database.Cursor;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.marnistek.serverstats.Database.DataBaseAdapter;
import com.marnistek.serverstats.R;

import java.text.DecimalFormat;
import java.util.ArrayList;


public class SummaryFragment extends Fragment {

    PieChart pieChart;

    public TextView txtInOverall;
    public TextView txtOutOverall;
    public TextView txtTotalOverall;

    public TextView txtInHour;
    public TextView txtOutHour;
    public TextView txtTotalHour;

    public TextView txtInDay;
    public TextView txtOutDay;
    public TextView txtTotalDay;

    public TextView txtInMonth;
    public TextView txtOutMonth;
    public TextView txtTotalMonth;

    double totalRX;
    double totalTX;
    double total;

    double hourlyRX;
    double hourlyTX;
    double hourlyTotal;

    double dailyRX;
    double dailyTX;
    double dailyTotal;

    double monthlyRX;
    double monthlyTX;
    double monthlyTotal;

    String byteTypeRX;
    String byteTypeTX;
    String byteType;

    String byteTypeRXh;
    String byteTypeTXh;
    String byteTypeTh;

    String byteTypeRXd;
    String byteTypeTXd;
    String byteTypeTd;

    String byteTypeRXm;
    String byteTypeTXm;
    String byteTypeTm;

    String overallIn;
    String overallOut;
    String overall;

    String hourlyIn;
    String hourlyOut;
    String hourlyOverall;

    String dailyIn;
    String dailyOut;
    String dailyOverall;

    String monthlyIn;
    String monthlyOut;
    String monthlyOverall;

    TableLayout tableLayoutSummary;
    TableLayout tableLayout;

    Spinner spinner;

    String selectedInterface;

    View v;

    PieData pieData;
    PieDataSet pieDataSet;
    String centerText;

    Cursor tableData;

    ProgressBar progressBar;
    LinearLayout linearLayout;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        v = inflater.inflate(R.layout.fragment_summary, container, false);

        txtInOverall = (TextView) v.findViewById(R.id.txtInData);
        txtOutOverall = (TextView) v.findViewById(R.id.txtOutData);
        txtTotalOverall = (TextView) v.findViewById(R.id.txtTotalData);

        txtInHour = (TextView) v.findViewById(R.id.txtInDataHour);
        txtOutHour = (TextView) v.findViewById(R.id.txtOutDataHour);
        txtTotalHour = (TextView) v.findViewById(R.id.txtTotalDataHour);

        txtInDay = (TextView) v.findViewById(R.id.txtInDataDay);
        txtOutDay = (TextView) v.findViewById(R.id.txtOutDataDay);
        txtTotalDay = (TextView) v.findViewById(R.id.txtTotalDataDay);

        txtInMonth = (TextView) v.findViewById(R.id.txtInDataMonth);
        txtOutMonth = (TextView) v.findViewById(R.id.txtOutDataMonth);
        txtTotalMonth = (TextView) v.findViewById(R.id.txtTotalDataMonth);

        tableLayoutSummary = (TableLayout) v.findViewById(R.id.tableLayoutSummary);
        tableLayout = (TableLayout) v.findViewById(R.id.tableLayout1);

        spinner = (Spinner) v.findViewById(R.id.spinner);

        pieChart = (PieChart) v.findViewById(R.id.pieChart);

        progressBar = (ProgressBar) v.findViewById(R.id.loadingCircle);

        //String serverName = getArguments().getString("nkey");
        final String host = getArguments().getString("hkey");
        selectedInterface = getArguments().getString("ikey");
        Log.d("HOSTCHECK",host + " TEST");
        Toast.makeText(getActivity(), "Network Interface: " + selectedInterface,Toast.LENGTH_LONG).show();

        linearLayout = (LinearLayout) v.findViewById(R.id.linearLayout2);
        linearLayout.setVisibility(View.GONE);

        new processData().execute(selectedInterface,host);

        spinner.setSelection(0);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position == 0){
                    ReBuildPieChart("s", selectedInterface, host);
                } else if(position == 1){
                    ReBuildPieChart("m", selectedInterface, host);
                }else if(position == 2){
                    ReBuildPieChart("d", selectedInterface, host);
                }else if(position == 3){
                    ReBuildPieChart("h", selectedInterface, host);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setRetainInstance(true);
    }

    public void refreshData(String ifce, String host){

        DataBaseAdapter mDbHelper = new DataBaseAdapter(v.getContext());
        mDbHelper.createDatabase();
        mDbHelper.open();

        double kToM = 1024;
        double kToG = 1048576;
        double kToT = 1073741824;

        /****Totals****/
        Cursor testdata = mDbHelper.getTestData(ifce, host);
        String totalrx_miB = testdata.getString(testdata.getColumnIndexOrThrow("totalrx_MiB"));
        String totalrx_kiB = testdata.getString(testdata.getColumnIndexOrThrow("totalrx_KiB"));
        String totaltx_miB = testdata.getString(testdata.getColumnIndexOrThrow("totaltx_MiB"));
        String totaltx_kiB = testdata.getString(testdata.getColumnIndexOrThrow("totaltx_KiB"));

        /****Hourly****/
        Cursor hourlyData = mDbHelper.getSummaryData("h", ifce, host);
        String hourlyrx_kiB = hourlyData.getString(hourlyData.getColumnIndexOrThrow("rx_KiB"));
        String hourlytx_kiB = hourlyData.getString(hourlyData.getColumnIndexOrThrow("tx_KiB"));

        /****Daily****/
        Cursor dailyData = mDbHelper.getSummaryData("d", ifce, host);
        String dailyrx_kiB = dailyData.getString(dailyData.getColumnIndexOrThrow("rx_KiB"));
        String dailytx_kiB = dailyData.getString(dailyData.getColumnIndexOrThrow("tx_KiB"));

        /****Monthly****/
        Cursor monthlyData = mDbHelper.getSummaryData("m", ifce, host);
        String monthlyrx_kiB = monthlyData.getString(monthlyData.getColumnIndexOrThrow("rx_KiB"));
        String monthlytx_kiB = monthlyData.getString(monthlyData.getColumnIndexOrThrow("tx_KiB"));

        /****Totals****/
        double totalrxKB = ((Double.parseDouble(totalrx_miB) * 1024) + Double.parseDouble(totalrx_kiB));
        double totaltxKB = ((Double.parseDouble(totaltx_miB) * 1024) + Double.parseDouble(totaltx_kiB));
        double totalKB = totalrxKB + totaltxKB;

        double totalrxMB = ((Double.parseDouble(totalrx_miB) * 1024) + Double.parseDouble(totalrx_kiB)) / kToM;
        double totaltxMB = ((Double.parseDouble(totaltx_miB) * 1024) + Double.parseDouble(totaltx_kiB)) / kToM;
        double totalMB = (totalrxKB + totaltxKB) / kToM;

        double totalrxGB = ((Double.parseDouble(totalrx_miB) * 1024) + Double.parseDouble(totalrx_kiB)) / kToG;
        double totaltxGB = ((Double.parseDouble(totaltx_miB) * 1024) + Double.parseDouble(totaltx_kiB)) / kToG;
        double totalGB = (totalrxKB + totaltxKB) / kToG;

        double totalrxTB = ((Double.parseDouble(totalrx_miB) * 1024) + Double.parseDouble(totalrx_kiB)) / kToT;
        double totaltxTB = ((Double.parseDouble(totaltx_miB) * 1024) + Double.parseDouble(totaltx_kiB)) / kToT;
        double totalTB = (totalrxKB + totaltxKB) / kToT;
        /****Totals End****/

        /****Hourly****/
        double hourlyrxKB = Double.parseDouble(hourlyrx_kiB);
        double hourlytxKB = Double.parseDouble(hourlytx_kiB);
        double hourlyTotalKB = hourlyrxKB + hourlytxKB;

        double hourlyrxMB = hourlyrxKB / kToM;
        double hourlytxMB = hourlytxKB / kToM;
        double hourlyTotalMB = hourlyTotalKB / kToM;

        double hourlyrxGB = hourlyrxKB / kToG;
        double hourlytxGB = hourlytxKB / kToG;
        double hourlyTotalGB = hourlyTotalKB / kToG;

        double hourlyrxTB = hourlyrxKB / kToT;
        double hourlytxTB = hourlytxKB / kToT;
        double hourlyTotalTB = hourlyTotalKB / kToT;
        /****Hourly End****/

        /****Daily****/
        double dailyrxKB = Double.parseDouble(dailyrx_kiB);
        double dailytxKB = Double.parseDouble(dailytx_kiB);
        double dailyTotalKB = dailyrxKB + dailytxKB;

        double dailyrxMB = dailyrxKB / kToM;
        double dailytxMB = dailytxKB / kToM;
        double dailyTotalMB = dailyTotalKB / kToM;

        double dailyrxGB = dailyrxKB / kToG;
        double dailytxGB = dailytxKB / kToG;
        double dailyTotalGB = dailyTotalKB / kToG;

        double dailyrxTB = dailyrxKB / kToT;
        double dailytxTB = dailytxKB / kToT;
        double dailyTotalTB = dailyTotalKB / kToT;
        /****Daily End****/

        /****Monthly****/
        double monthlyrxKB = Double.parseDouble(monthlyrx_kiB);
        double monthlytxKB = Double.parseDouble(monthlytx_kiB);
        double monthlyTotalKB = monthlyrxKB + monthlytxKB;

        double monthlyrxMB = monthlyrxKB / kToM;
        double monthlytxMB = monthlytxKB / kToM;
        double monthlyTotalMB = monthlyTotalKB / kToM;

        double monthlyrxGB = monthlyrxKB / kToG;
        double monthlytxGB = monthlytxKB / kToG;
        double monthlyTotalGB = monthlyTotalKB / kToG;

        double monthlyrxTB = monthlyrxKB / kToT;
        double monthlytxTB = monthlytxKB / kToT;
        double monthlyTotalTB = monthlyTotalKB / kToT;
        /****Monthly End****/

        /****Totals****/
        if(totalrxKB < kToM) {
            totalRX = totalrxKB;
            byteTypeRX = "KB";
        } else if(totalrxKB > kToM && totalrxKB < kToG) {
            totalRX = totalrxMB;
            byteTypeRX = "MB";
        } else if(totalrxKB > kToG && totalrxKB < kToT) {
            totalRX = totalrxGB;
            byteTypeRX = "GB";
        } else if(totalrxKB > kToT) {
            totalRX = totalrxTB;
            byteTypeRX = "TB";
        }

        if(totaltxKB < kToM) {
            totalTX = totaltxKB;
            byteTypeTX = "KB";
        } else if(totaltxKB > kToM && totaltxKB < kToG) {
            totalTX = totaltxMB;
            byteTypeTX = "MB";
        } else if(totaltxKB > kToG && totaltxKB < kToT) {
            totalTX = totaltxGB;
            byteTypeTX = "GB";
        } else if(totaltxKB > kToT) {
            totalTX = totaltxTB;
            byteTypeTX = "TB";
        }

        if(totalKB < kToM) {
            total = totalKB;
            byteType = "KB";
        } else if(totalKB > kToM && totalKB < kToG) {
            total = totalMB;
            byteType = "MB";
        } else if(totalKB > kToG && totalKB < kToT) {
            total = totalGB;
            byteType= "GB";
        } else if(totalKB > kToT) {
            total = totalTB;
            byteType = "TB";
        }

        /****Hourly****/
        if(hourlyrxKB < kToM) {
            hourlyRX = hourlyrxKB;
            byteTypeRXh = "KB";
        } else if(hourlyrxKB > kToM && hourlyrxKB < kToG) {
            hourlyRX = hourlyrxMB;
            byteTypeRXh = "MB";
        } else if(hourlyrxKB > kToG && hourlyrxKB < kToT) {
            hourlyRX = hourlyrxGB;
            byteTypeRXh = "GB";
        } else if(hourlyrxKB > kToT) {
            hourlyRX = hourlyrxTB;
            byteTypeRXh = "TB";
        }

        if(hourlytxKB < kToM) {
            hourlyTX = hourlytxKB;
            byteTypeTXh = "KB";
        } else if(hourlytxKB > kToM && hourlytxKB < kToG) {
            hourlyTX = hourlytxMB;
            byteTypeTXh = "MB";
        } else if(hourlytxKB > kToG && hourlytxKB < kToT) {
            hourlyTX = hourlytxGB;
            byteTypeTXh = "GB";
        } else if(hourlytxKB > kToT) {
            hourlyTX = hourlytxTB;
            byteTypeTXh = "TB";
        }

        if(hourlyTotalKB < kToM) {
            hourlyTotal = hourlyTotalKB;
            byteTypeTh = "KB";
        } else if(hourlyTotalKB > kToM && hourlyTotalKB < kToG) {
            hourlyTotal = hourlyTotalMB;
            byteTypeTh = "MB";
        } else if(hourlyTotalKB > kToG && hourlyTotalKB < kToT) {
            hourlyTotal = hourlyTotalGB;
            byteTypeTh = "GB";
        } else if(hourlyTotalKB > kToT) {
            hourlyTotal = hourlyTotalTB;
            byteTypeTh = "TB";
        }

        /****Daily****/
        if(dailyrxKB < kToM) {
            dailyRX = dailyrxKB;
            byteTypeRXd = "KB";
        } else if(dailyrxKB > kToM && dailyrxKB < kToG) {
            dailyRX = dailyrxMB;
            byteTypeRXd = "MB";
        } else if(dailyrxKB > kToG && dailyrxKB < kToT) {
            dailyRX = dailyrxGB;
            byteTypeRXd = "GB";
        } else if(dailyrxKB > kToT) {
            dailyRX = dailyrxTB;
            byteTypeRXd = "TB";
        }

        if(dailytxKB < kToM) {
            dailyTX = dailytxKB;
            byteTypeTXd = "KB";
        } else if(dailytxKB > kToM && dailytxKB < kToG) {
            dailyTX = dailytxMB;
            byteTypeTXd = "MB";
        } else if(dailytxKB > kToG && dailytxKB < kToT) {
            dailyTX = dailytxGB;
            byteTypeTXd = "GB";
        } else if(dailytxKB > kToT) {
            dailyTX = dailytxTB;
            byteTypeTXd = "TB";
        }

        if(dailyTotalKB < kToM) {
            dailyTotal = dailyTotalKB;
            byteTypeTd = "KB";
        } else if(dailyTotalKB > kToM && dailyTotalKB < kToG) {
            dailyTotal = dailyTotalMB;
            byteTypeTd = "MB";
        } else if(dailyTotalKB > kToG && dailyTotalKB < kToT) {
            dailyTotal = dailyTotalGB;
            byteTypeTd = "GB";
        } else if(dailyTotalKB > kToT) {
            dailyTotal = dailyTotalTB;
            byteTypeTd = "TB";
        }

        /****Monthly****/
        if(monthlyrxKB < kToM) {
            monthlyRX = monthlyrxKB;
            byteTypeRXm = "KB";
        } else if(monthlyrxKB > kToM && monthlyrxKB < kToG) {
            monthlyRX = monthlyrxMB;
            byteTypeRXm = "MB";
        } else if(monthlyrxKB > kToG && monthlyrxKB < kToT) {
            monthlyRX = monthlyrxGB;
            byteTypeRXm = "GB";
        } else if(monthlyrxKB > kToT) {
            monthlyRX = monthlyrxTB;
            byteTypeRXm = "TB";
        }

        if(monthlytxKB < kToM) {
            monthlyTX = monthlytxKB;
            byteTypeTXm = "KB";
        } else if(monthlytxKB > kToM && monthlytxKB < kToG) {
            monthlyTX = monthlytxMB;
            byteTypeTXm = "MB";
        } else if(monthlytxKB > kToG && monthlytxKB < kToT) {
            monthlyTX = monthlytxGB;
            byteTypeTXm = "GB";
        } else if(monthlytxKB > kToT) {
            monthlyTX = monthlytxTB;
            byteTypeTXm = "TB";
        }

        if(monthlyTotalKB < kToM) {
            monthlyTotal = monthlyTotalKB;
            byteTypeTm = "KB";
        } else if(monthlyTotalKB > kToM && monthlyTotalKB < kToG) {
            monthlyTotal = monthlyTotalMB;
            byteTypeTm = "MB";
        } else if(monthlyTotalKB > kToG && monthlyTotalKB < kToT) {
            monthlyTotal = monthlyTotalGB;
            byteTypeTm = "GB";
        } else if(monthlyTotalKB > kToT) {
            monthlyTotal = monthlyTotalTB;
            byteTypeTm = "TB";
        }

        totalRX = Math.round(totalRX * 100);
        totalRX = totalRX / 100;
        totalTX = Math.round(totalTX * 100);
        totalTX = totalTX / 100;
        total = Math.round(total * 100);
        total = total / 100;

        hourlyRX = Math.round(hourlyRX * 100);
        hourlyRX = hourlyRX / 100;
        hourlyTX = Math.round(hourlyTX * 100);
        hourlyTX = hourlyTX / 100;
        hourlyTotal = Math.round(hourlyTotal * 100);
        hourlyTotal = hourlyTotal / 100;

        dailyRX = Math.round(dailyRX * 100);
        dailyRX = dailyRX / 100;
        dailyTX = Math.round(dailyTX * 100);
        dailyTX = dailyTX / 100;
        dailyTotal = Math.round(dailyTotal * 100);
        dailyTotal = dailyTotal / 100;

        monthlyRX = Math.round(monthlyRX * 100);
        monthlyRX = monthlyRX / 100;
        monthlyTX = Math.round(monthlyTX * 100);
        monthlyTX = monthlyTX / 100;
        monthlyTotal = Math.round(monthlyTotal * 100);
        monthlyTotal = monthlyTotal / 100;

        overallIn = totalRX + " " + byteTypeRX;
        overallOut = totalTX + " " + byteTypeTX;
        overall = total + " " + byteType;

        hourlyIn = hourlyRX + " " + byteTypeRXh;
        hourlyOut = hourlyTX + " " + byteTypeTXh;
        hourlyOverall = hourlyTotal + " " + byteTypeTh;

        dailyIn = dailyRX + " " + byteTypeRXd;
        dailyOut = dailyTX + " " + byteTypeTXd;
        dailyOverall = dailyTotal + " " + byteTypeTd;

        monthlyIn = monthlyRX + " " + byteTypeRXm;
        monthlyOut = monthlyTX + " " + byteTypeTXm;
        monthlyOverall = monthlyTotal + " " + byteTypeTm;

        mDbHelper.close();
    }

    private void BuildPieChart(String dataType, String ifce, String host){

        DataBaseAdapter mDbHelper = new DataBaseAdapter(v.getContext());
        mDbHelper.createDatabase();
        mDbHelper.open();

        Cursor getData = mDbHelper.getPieData(dataType, ifce, host);
        String rx = getData.getString(getData.getColumnIndexOrThrow("rx"));
        String tx = getData.getString(getData.getColumnIndexOrThrow("tx"));
        float bytesIn = Float.parseFloat(rx);
        float bytesOut = Float.parseFloat(tx);

        ArrayList<Entry> entries = new ArrayList<>();
        entries.add(new Entry(bytesIn, 0));
        entries.add(new Entry(bytesOut, 1));

        pieDataSet = new PieDataSet(entries, "");

        ArrayList<String> labels = new ArrayList<>();
        labels.add("In");
        labels.add("Out");

        pieData = new PieData(labels, pieDataSet);
        pieChart.setData(pieData);


        int[] colorArray = {Color.GREEN,Color.MAGENTA};
        String[] labelArray = {"In","Out"};
        pieDataSet.setColors(colorArray);
        pieDataSet.setValueFormatter(new MyValueFormatter(dataType));
        pieDataSet.setValueTextSize(20);

        pieChart.setDescription("");
        pieChart.getLegend().setCustom(colorArray,labelArray);
        switch (dataType) {
            case "s":
                centerText = "All Time";
                break;
            case "m":
                centerText = "This Month";
                break;
            case "d":
                centerText = "This Day";
                break;
            case "h":
                centerText = "This Hour";
                break;
        }

        mDbHelper.close();

    }

    private void ReBuildPieChart(String dataType, String ifce, String host){

        DataBaseAdapter mDbHelper = new DataBaseAdapter(v.getContext());
        mDbHelper.createDatabase();
        mDbHelper.open();

        Cursor getData = mDbHelper.getPieData(dataType, ifce, host);
        String rx = getData.getString(getData.getColumnIndexOrThrow("rx"));
        String tx = getData.getString(getData.getColumnIndexOrThrow("tx"));
        float bytesIn = Float.parseFloat(rx);
        float bytesOut = Float.parseFloat(tx);

        ArrayList<Entry> entries = new ArrayList<>();
        entries.add(new Entry(bytesIn, 0));
        entries.add(new Entry(bytesOut, 1));

        pieDataSet = new PieDataSet(entries, "");

        ArrayList<String> labels = new ArrayList<>();
        labels.add("In");
        labels.add("Out");

        pieData = new PieData(labels, pieDataSet);
        pieChart.setData(pieData);


        int[] colorArray = {Color.GREEN,Color.MAGENTA};
        String[] labelArray = {"In","Out"};
        pieDataSet.setColors(colorArray);
        pieDataSet.setValueFormatter(new MyValueFormatter(dataType));
        pieDataSet.setValueTextSize(20);

        pieChart.setDescription("");
        pieChart.getLegend().setCustom(colorArray,labelArray);
        String newCenterText = "";
        switch (dataType) {
            case "s":
                newCenterText = "All Time";
                break;
            case "m":
                newCenterText = "This Month";
                break;
            case "d":
                newCenterText = "This Day";
                break;
            case "h":
                newCenterText = "This Hour";
                break;
        }

        pieChart.setCenterText(newCenterText);
        pieChart.setDrawCenterText(true);
        pieChart.animateY(0);

        mDbHelper.close();

    }

    private void GetTableData(String ifce, String host){

        DataBaseAdapter mDbHelper = new DataBaseAdapter(v.getContext());
        mDbHelper.createDatabase();
        mDbHelper.open();

        tableData = mDbHelper.getTableData("t", ifce, host);


        mDbHelper.close();
    }

    public void PopulateTable(){
        int rows = tableData.getCount();
        int cols = tableData.getColumnCount();

        tableData.moveToFirst();

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
                    tv.setGravity(Gravity.LEFT);
                } else {
                    tv.setGravity(Gravity.CENTER);
                }
                //tv.setTextSize(18);
                tv.setPadding(0, 5, 0, 5);

                tv.setText(tableData.getString(j));

                row.addView(tv);

            }

            tableData.moveToNext();

            tableLayout.addView(row);
        }
    }

    public class MyValueFormatter implements ValueFormatter {

        private DecimalFormat mFormat;
        private String s;

        public MyValueFormatter(String dataType) {
            mFormat = new DecimalFormat("###,###,##0.00"); // use one decimal
            s = dataType;
        }

        @Override
        public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
            float kToM = 1024;
            float kToG = 1048576;
            float kToT = 1073741824;

            float bToK = 1024;
            float bToM = 1048576;
            float bToG = 1073741824;
            float bToT = 1099511627776L;

            String byteTypeF = "";
            float data = 0;

            if(s.equals("h")){
                if(value < bToK) {
                    data = value;
                    byteTypeF = "B";
                } else if(value < bToM) {
                    data = value/bToK;
                    byteTypeF = "KB";
                } else if(value > bToM && value < bToG) {
                    data = value/bToM;
                    byteTypeF = "MB";
                } else if(value > bToG && value < bToT) {
                    data = value/bToG;
                    byteTypeF = "GB";
                } else if(data > bToT) {
                    data = value/bToK;
                    byteTypeF = "TB";
                }
            }else{
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
            }

            return mFormat.format(data) + " " + byteTypeF; // e.g. append a dollar-sign
        }
    }

    private class processData extends AsyncTask<String, Void, Void> {

        //ProgressDialog loadingDialog;

        @Override
        protected void onPreExecute (){
            progressBar.setVisibility(View.VISIBLE);
            /*
            loadingDialog =  new ProgressDialog(v.getContext());
            loadingDialog.setTitle("Loading");
            loadingDialog.setMessage("Please Wait");
            loadingDialog.show();
            */
        }

        @Override
        protected void onPostExecute(Void result){

            txtInOverall.setText(overallIn);
            txtOutOverall.setText(overallOut);
            txtTotalOverall.setText(overall);

            txtInHour.setText(hourlyIn);
            txtOutHour.setText(hourlyOut);
            txtTotalHour.setText(hourlyOverall);

            txtInDay.setText(dailyIn);
            txtOutDay.setText(dailyOut);
            txtTotalDay.setText(dailyOverall);

            txtInMonth.setText(monthlyIn);
            txtOutMonth.setText(monthlyOut);
            txtTotalMonth.setText(monthlyOverall);

            pieChart.setCenterText(centerText);
            pieChart.setDrawCenterText(true);
            pieChart.animateY(0);

            PopulateTable();

            progressBar.setVisibility(View.GONE);
            linearLayout.setVisibility(View.VISIBLE);


        }

        @Override
        protected Void doInBackground(String... params) {
            BuildPieChart("s", params[0],params[1]);
            refreshData(params[0],params[1]);
            GetTableData(params[0],params[1]);
            return null;
        }
    }


}
