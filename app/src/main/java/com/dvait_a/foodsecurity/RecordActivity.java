package com.dvait_a.foodsecurity;


import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.anychart.APIlib;
import com.anychart.AnyChart;
import com.anychart.AnyChartView;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.chart.common.dataentry.ValueDataEntry;
import com.anychart.charts.Pie;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class RecordActivity extends AppCompatActivity {
    private final List<DataEntry> wheatSummary=new ArrayList<>();
    private final List<DataEntry> riceSummary=new ArrayList<>();
    private final List<DataEntry> oilSummary=new ArrayList<>();
    private TextView wheatUsed;
    private TextView riceUsed;
    private TextView oilUsed;
    private TextView date_time;
    private TextView vendor;
    private int wheatUsedI;
    private int riceUsedI;
    private int oilUsedI;
    private int wheatTtlInt;
    private int riceTtlInt;
    private int oilTtlInt;
    private final String[] Summary = new String[] { "USED","REMAINING"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);

        adjustFontScale(getResources().getConfiguration());
        //Contexts
        wheatUsed=findViewById(R.id.wheatUsed);
        riceUsed=findViewById(R.id.riceUsed);
        oilUsed=findViewById(R.id.oilUsed);
        date_time=findViewById(R.id.date_time);
        vendor=findViewById(R.id.vendor);
        TextView wheatT = findViewById(R.id.wheatTotal);
        TextView riceT = findViewById(R.id.riceTotal);
        TextView oilT = findViewById(R.id.oilTotal);
        RelativeLayout l1 = findViewById(R.id.r3);
        FirebaseFirestore records = FirebaseFirestore.getInstance();

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser mUser = mAuth.getCurrentUser();
        String userEidSystem = mUser.getUid();
        showLoad();

        String typeOfUser = getIntent().getStringExtra("type");
        String oilTtlString;
        String riceTtlString;
        String wheatTtlString;
        if(typeOfUser.equals("customer")) {
            String userIncome = getIntent().getStringExtra("income");
            String incS = "";
            int inc = Integer.parseInt(userIncome);
            if (inc > 0 && inc < 25000) {
                incS = "25000";
            } else if (inc > 25000 && inc < 50000) {
                incS = "50000";
            } else if (inc > 50000 && inc < 90000) {
                incS = "90000";
            }

            DocumentReference documentReference = records.collection(incS).document(userEidSystem);//Getting Dashboard

            documentReference.get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            wheatUsed.setText(documentSnapshot.getString("Wheat"));
                            riceUsed.setText(documentSnapshot.getString("Rice"));
                            oilUsed.setText(documentSnapshot.getString("Oil"));
                            date_time.setText(documentSnapshot.getString("Date_time"));
                            vendor.setText(documentSnapshot.getString("Vendor"));
                        }
                    }).addOnFailureListener(e -> {
                        Toast.makeText(RecordActivity.this, "Error In Loading Record", Toast.LENGTH_LONG).show();
                        Log.d("FireStore", e.toString());
                    });
            wheatTtlInt =10;
            riceTtlInt =10;
            oilTtlInt =10;
            wheatTtlString =String.valueOf(wheatTtlInt);
            riceTtlString =String.valueOf(riceTtlInt);
            oilTtlString =String.valueOf(oilTtlInt);
            wheatT.setText(wheatTtlString);
            riceT.setText(riceTtlString);
            oilT.setText(oilTtlString);
        }
        else{

            FirebaseFirestore records1 = FirebaseFirestore.getInstance();

            DocumentReference documentReference1 = records1.collection("Vendors").document(userEidSystem);//Getting Dashboard

            documentReference1.get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            wheatUsed.setText(documentSnapshot.getString("Wheat"));
                            riceUsed.setText(documentSnapshot.getString("Rice"));
                            oilUsed.setText(documentSnapshot.getString("Oil"));
                        }
                    }).addOnFailureListener(e -> {
                        Toast.makeText(RecordActivity.this, "Error In Loading Record", Toast.LENGTH_LONG).show();
                        Log.d("FireStore", e.toString());
                    });
            wheatTtlInt =100;
            riceTtlInt =100;
            oilTtlInt =50;
            wheatTtlString =String.valueOf(wheatTtlInt);
            riceTtlString =String.valueOf(riceTtlInt);
            oilTtlString =String.valueOf(oilTtlInt);
            wheatT.setText(wheatTtlString);
            riceT.setText(riceTtlString);
            oilT.setText(oilTtlString);

            l1.setVisibility(View.GONE);

        }

        Handler handler=new Handler();
        handler.postDelayed(() -> {
            try {
                wheatUsedI = Integer.parseInt(wheatUsed.getText().toString());
                riceUsedI = Integer.parseInt(riceUsed.getText().toString());
                oilUsedI = Integer.parseInt(oilUsed.getText().toString());
            }
            catch (NumberFormatException e){
                oilUsedI =0;
            }
            setGraph();
        },4000);

    }

    private void setGraph(){
        int wheatRemain = wheatTtlInt - Integer.parseInt(wheatUsed.getText().toString());
        int riceRemain = riceTtlInt - Integer.parseInt(riceUsed.getText().toString());
        int oilRemain = oilTtlInt - Integer.parseInt(oilUsed.getText().toString());

        wheatSummary.add(new ValueDataEntry(Summary[0], wheatUsedI));
        wheatSummary.add(new ValueDataEntry(Summary[1], wheatRemain));

        AnyChartView anyChartView=findViewById(R.id.wheatChart);                                  //Set First Chart
        APIlib.getInstance().setActiveAnyChartView(anyChartView);
        Pie pie= AnyChart.pie();
        pie.data(wheatSummary);
        anyChartView.setChart(pie);

        riceSummary.add(new ValueDataEntry(Summary[0], riceUsedI));
        riceSummary.add(new ValueDataEntry(Summary[1], riceRemain));
        AnyChartView anyChartView1=findViewById(R.id.riceChart);                                  //Set First Chart
        APIlib.getInstance().setActiveAnyChartView(anyChartView1);
        Pie pie1= AnyChart.pie();
        pie1.data(riceSummary);
        anyChartView1.setChart(pie1);

        oilSummary.add(new ValueDataEntry(Summary[0], oilUsedI));
        oilSummary.add(new ValueDataEntry(Summary[1], oilRemain));
        AnyChartView anyChartView2=findViewById(R.id.oilChart);                                  //Set First Chart
        APIlib.getInstance().setActiveAnyChartView(anyChartView2);
        Pie pie2= AnyChart.pie();
        pie2.data(oilSummary);
        anyChartView2.setChart(pie2);

    }
    public void adjustFontScale(Configuration configuration)
    {
        configuration.fontScale = (float) 0.8;
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(metrics);
        metrics.scaledDensity = configuration.fontScale * metrics.density;
        getBaseContext().getResources().updateConfiguration(configuration, metrics);
    }
    public void showLoad(){
        ViewGroup viewGroup = findViewById(android.R.id.content);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.loading_dialogue, viewGroup, false);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        final AlertDialog alertDialog = builder.create();
        Objects.requireNonNull(alertDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialog.show();
        ProgressBar p1= dialogView.findViewById(R.id.p1);
        p1.setVisibility(View.VISIBLE);

        Handler handler =new Handler();
        handler.postDelayed(alertDialog::dismiss,4000);
    }

}