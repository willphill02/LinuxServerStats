package com.marnistek.serverstats.Activites;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.marnistek.serverstats.R;

public class LicenceInfo extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lincence_info);

        FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "11");
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "licence_detail_opened");
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "licence_detail_opened");
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        }
        TextView licenceText = (TextView) findViewById(R.id.licence_info);
        String licenceName = getIntent().getStringExtra("nkey");
        String licence = getIntent().getStringExtra("lkey");
        getSupportActionBar().setTitle(licenceName);
        if (licenceText != null) {
            licenceText.setText(licence);
        }
    }

}
