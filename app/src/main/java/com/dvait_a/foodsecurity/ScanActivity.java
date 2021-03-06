package com.dvait_a.foodsecurity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ScanActivity extends AppCompatActivity {

    private TextView income;
    private String scannedId, fieldIncome, uID;
    private FirebaseFirestore records,records2;
    private TextView wheatUsed, riceUsed, oilUsed,wheatVendor,riceVendor,oilVendor;;
    private int wheatRemain, riceRemain, oilRemain, wheatUsedI, riceUsedI, oilUsedI;
    private String WheatUsed, RiceUsed, OilUsed;
    private FirebaseFirestore userCategory;
    private String vendorId;
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        adjustFontScale(getResources().getConfiguration());
        setContentView(R.layout.activity_scan);
        scannedId = getIntent().getStringExtra("scannedId");
        records = FirebaseFirestore.getInstance();
        TextView uId = findViewById(R.id.textEid);
        income = findViewById(R.id.textIncome);
        wheatUsed = findViewById(R.id.wheatUsed);
        riceUsed = findViewById(R.id.riceUsed);
        oilUsed = findViewById(R.id.oilUsed);
        wheatVendor=findViewById(R.id.vendorWheat);
        riceVendor=findViewById(R.id.vendorRice);
        oilVendor=findViewById(R.id.vendorOil);


        Button issueRequirement = findViewById(R.id.issueR);
        String id = "Scanned User Id :" + scannedId;
        uId.setText(id);
        getUserData(scannedId);
        vendorId=getIntent().getStringExtra("uid");


        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser mUser = mAuth.getCurrentUser();
        uID = mUser.getUid();

        Handler handler=new Handler();
        handler.postDelayed(() -> {
            WheatUsed = wheatUsed.getText().toString();
            RiceUsed = riceUsed.getText().toString();
            OilUsed = oilUsed.getText().toString();

            try {
                wheatUsedI = Integer.parseInt(WheatUsed);
                riceUsedI = Integer.parseInt(RiceUsed);
                oilUsedI = Integer.parseInt(OilUsed);
            }
            catch (NullPointerException n ){
                Toast.makeText(ScanActivity.this,"Error Loading Values",Toast.LENGTH_LONG).show();
            }
            catch (NumberFormatException e){
                oilUsedI =0;
            }

            wheatRemain = 10 - wheatUsedI;
            riceRemain = 10 - riceUsedI;
            oilRemain = 5 - oilUsedI;
        },4000);


        issueRequirement.setOnClickListener(v -> {
            if (wheatUsedI == 10 || riceUsedI == 10 || oilUsedI == 5) {
                Toast.makeText(ScanActivity.this, "Sorry The Limit Already Exist.Please Try in Next Month", Toast.LENGTH_LONG).show();
            } else {
                showDialogue();
            }
        });
    }

    public void getUserData(final String userEidSystem) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(userEidSystem);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    fieldIncome = dataSnapshot.child("userField").getValue().toString();
                    setIncome(fieldIncome);
                } catch (NullPointerException n) {
                    Toast.makeText(ScanActivity.this, "Record Not Found 1", Toast.LENGTH_LONG).show();
                    Intent intent=new Intent(ScanActivity.this,MainActivity.class);
                    startActivity(intent);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ScanActivity.this, "Record Not Found 2", Toast.LENGTH_LONG).show();
                Intent intent=new Intent(ScanActivity.this,MainActivity.class);
                startActivity(intent);
            }
        });
    }

    public void getRecord() {
        String incS = "";
        int inc = Integer.parseInt(fieldIncome);
        if (inc > 0 && inc < 25000) {
            incS = "25000";
        } else if (inc > 25000 && inc < 50000) {
            incS = "50000";
        } else if (inc > 50000 && inc < 90000) {
            incS = "90000";
        }

        DocumentReference documentReference = records.collection(incS).document(scannedId);//Getting Dashboard

        documentReference.get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        wheatUsed.setText(documentSnapshot.getString("Wheat"));
                        riceUsed.setText(documentSnapshot.getString("Rice"));
                        oilUsed.setText(documentSnapshot.getString("Oil"));
                    }
                }).addOnFailureListener(e -> {
                    Toast.makeText(ScanActivity.this, "Error In Loading Record", Toast.LENGTH_LONG).show();
                    Log.d("FireStore", e.toString());
                });

        records2=FirebaseFirestore.getInstance();

        DocumentReference documentReference2 = records2.collection("Vendors").document(uID);//Getting Dashboard

        documentReference2.get()
                .addOnSuccessListener(documentSnapshot -> {
                    if(documentSnapshot.exists()){
                        wheatVendor.setText(documentSnapshot.getString("Wheat"));
                        riceVendor.setText(documentSnapshot.getString("Rice"));
                        oilVendor.setText(documentSnapshot.getString("Oil"));
                    }
                }).addOnFailureListener(e -> {
                    Toast.makeText(ScanActivity.this,"Error In Loading Record",Toast.LENGTH_LONG).show();
                    Log.d("FireStore",e.toString());
                });
    }

    public void setIncome(String incomeSys) {
        String text = "Income : " + incomeSys;
        income.setText(text);
        getRecord();
    }

    public boolean placeOrder(String wheatU,String riceU,String oilU) {
        String range = "";
        final boolean[] stat = new boolean[1];
        int inc = Integer.parseInt(fieldIncome);
        if (inc > 0 && inc < 25000) {
            range = "25000";
        } else if (inc > 25000 && inc < 50000) {
            range = "50000";
        } else if (inc > 50000 && inc < 90000) {
            range = "90000";
        }

        Calendar c = Calendar.getInstance();
        System.out.println("Current time => " + c.getTime());

        @SuppressLint("SimpleDateFormat") SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        String formattedDate = df.format(c.getTime());

        userCategory=FirebaseFirestore.getInstance();

        DocumentReference doc=userCategory.collection(range).document(scannedId);

        doc.delete();

        final Map<String, String> userMap = new HashMap<>();
        userMap.put("Wheat",wheatU );
        userMap.put("Rice",riceU );
        userMap.put("Oil",oilU );
        userMap.put("Date_time",formattedDate );
        userMap.put("Vendor",uID );
        Handler handler=new Handler();
        final String finalRange = range;
        handler.postDelayed(() -> {

            try {
                userCategory.collection(finalRange).document(scannedId).set(userMap).addOnSuccessListener(avoid -> {
                    Toast.makeText(ScanActivity.this, "ALLOTMENT SUCCESSFUL", Toast.LENGTH_SHORT).show();
                    stat[0]=true;

                }).addOnFailureListener(e -> {
                    String error = e.getMessage();
                    Toast.makeText(ScanActivity.this, "Error :" + error, Toast.LENGTH_SHORT).show();
                    stat[0]=false;
                });
            } catch (NullPointerException n) {
                Toast.makeText(ScanActivity.this, "Error Occurred", Toast.LENGTH_SHORT).show();
                stat[0]=false;
            }

        },3000);
        return stat[0];
    }
    public void showDialogue() {                                                                   //Show Maintainace Dialog
        Button btn;
        final EditText wheatAsked,riceAsked,oilAsked;
        TextView wheatText,riceText,oilText;

        final int wheatRemainInt,riceRemainInt,oilRemainInt,wheatUsedInt,riceUsedInt,oilUsedInt;
        String wheatRemainString,riceRemainString,oilRemainString;

        ViewGroup viewGroup = findViewById(android.R.id.content);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialogue_new_record, viewGroup, false);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        final AlertDialog alertDialog = builder.create();
        Objects.requireNonNull(alertDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialog.show();
        btn=dialogView.findViewById(R.id.submit);
        wheatAsked=dialogView.findViewById(R.id.edit_wheat);
        riceAsked=dialogView.findViewById(R.id.edit_rice);
        oilAsked=dialogView.findViewById(R.id.edit_oil);
        wheatText=dialogView.findViewById(R.id.wheatTtl);
        riceText=dialogView.findViewById(R.id.riceTtl);
        oilText=dialogView.findViewById(R.id.oilTtl);

        wheatRemainInt=wheatRemain;
        riceRemainInt=riceRemain;
        oilRemainInt=oilRemain;
        wheatRemainString=String.valueOf(wheatRemainInt);
        riceRemainString=String.valueOf(riceRemainInt);
        oilRemainString=String.valueOf(oilRemainInt);
        wheatText.setText(wheatRemainString);
        riceText.setText(riceRemainString);
        oilText.setText(oilRemainString);

        wheatUsedInt= wheatUsedI;
        riceUsedInt= riceUsedI;
        oilUsedInt= oilUsedI;


        btn.setOnClickListener(new View.OnClickListener() {
            String wheatAskedString,riceAskedString,oilAskedString;
            int wheatAskedInt,riceAskedInt,oilAskedInt;
            int wheatUpdateInt,riceUpdateInt,oilUpdateInt;
            String wheatUpdateString="",riceUpdateString="" ,oilUpdateString="0";

            @Override
            public void onClick(View v) {

                wheatAskedString = wheatAsked.getText().toString();
                riceAskedString = riceAsked.getText().toString();
                oilAskedString = oilAsked.getText().toString();

                wheatAskedInt = Integer.parseInt(wheatAskedString);
                riceAskedInt = Integer.parseInt(riceAskedString);
                oilAskedInt = Integer.parseInt(oilAskedString);

                if (wheatAskedInt > wheatRemainInt || riceAskedInt > riceRemainInt || oilAskedInt > oilRemainInt) {
                    Toast.makeText(ScanActivity.this,"Order Exceeds than limit",Toast.LENGTH_LONG).show();
                    alertDialog.dismiss();
                } else {

                    if (wheatAskedInt == 0) {
                        wheatUpdateInt = wheatUsedInt;
                        wheatUpdateString = String.valueOf(wheatUpdateInt);
                    } else if (wheatAskedInt < wheatRemainInt) {
                        wheatUpdateInt = wheatAskedInt + wheatUsedInt;
                        wheatUpdateString = String.valueOf(wheatUpdateInt);
                    }

                    if (riceAskedInt == 0) {
                        riceUpdateInt = riceUsedInt;
                        riceUpdateString = String.valueOf(riceUpdateInt);
                    } else if (riceAskedInt < riceRemainInt) {
                        riceUpdateInt = riceAskedInt + riceUsedInt;
                        riceUpdateString = String.valueOf(riceUpdateInt);
                    }


                    if (wheatAskedInt == 0) {
                        oilUpdateInt = oilUsedInt;
                        oilUpdateString = String.valueOf(oilUpdateInt);
                    } else if (oilAskedInt < oilRemainInt) {
                        oilUpdateInt = oilAskedInt + oilUsedInt;
                        oilUpdateString = String.valueOf(oilUpdateInt);
                    }
                    records2=FirebaseFirestore.getInstance();
                    DocumentReference doc=records2.collection("Vendors").document(vendorId);
                    doc.delete().addOnSuccessListener(aVoid -> Toast.makeText(ScanActivity.this,"UPLDATING THE DOCUMENT",Toast.LENGTH_LONG).show())
                            .addOnFailureListener(e -> Toast.makeText(ScanActivity.this,"Error IN TRANSACTION",Toast.LENGTH_LONG).show());

                    if (placeOrder(wheatUpdateString, riceUpdateString, oilUpdateString)) {
                        alertDialog.dismiss();
                        getRecord();
                        showInvoice(wheatUpdateString,riceUpdateString,oilUpdateString);
                        Toast.makeText(ScanActivity.this, "Order Placed Successfully", Toast.LENGTH_LONG).show();
                    } else {
                        alertDialog.dismiss();
                        getRecord();
                        // Toast.makeText(ScanActivity.this,"Order Can't be Placed ,Please Try Again",Toast.LENGTH_LONG).show();
                    }


                    /// update vendor
                    int wheatVSys,riceVSys,oilVSys,oilInt,wheatInt,riceInt;
                    String wheatVUpdate,riceVUpdate,oilVUpdate,wheatV,riceV,oilV;



                    wheatV=wheatVendor.getText().toString();
                    riceV=riceVendor.getText().toString();
                    oilV=oilVendor.getText().toString();

                    wheatVSys=Integer.parseInt(wheatV);
                    riceVSys=Integer.parseInt(riceV);
                    oilVSys=Integer.parseInt(oilV);

                    wheatInt=wheatVSys+wheatAskedInt;
                    riceInt=riceVSys+riceAskedInt;
                    oilInt=oilVSys+oilAskedInt;

                    wheatVUpdate=String.valueOf(wheatInt);
                    riceVUpdate=String.valueOf(riceInt);
                    oilVUpdate=String.valueOf(oilInt);


                    final boolean[] stat = new boolean[1];

                    final Map<String, String> userMap = new HashMap<>();
                    userMap.put("Wheat",wheatVUpdate );
                    userMap.put("Rice",riceVUpdate);
                    userMap.put("Oil",oilVUpdate );
                    Handler handler=new Handler();
                    handler.postDelayed(() -> {
                        try {
                            userCategory.collection("Vendors").document(uID).set(userMap).addOnSuccessListener(avoid -> {
                                Toast.makeText(ScanActivity.this, "ALLOTMENT SUCCESSFUL", Toast.LENGTH_SHORT).show();
                                stat[0]=true;

                            }).addOnFailureListener(e -> {
                                String error = e.getMessage();
                                Toast.makeText(ScanActivity.this, "Error :" + error, Toast.LENGTH_SHORT).show();
                                stat[0]=false;
                            });
                        } catch (NullPointerException n) {
                            Toast.makeText(ScanActivity.this, "Error Occurred", Toast.LENGTH_SHORT).show();
                            stat[0]=false;
                        }
                    },4000);

                    Handler handler2=new Handler();
                    handler2.postDelayed(() -> showInvoice(wheatAskedString,riceAskedString,oilAskedString),6000);
                }
            }
        });
    }

    public void showInvoice(String wU,String rU,String oU) {
        ViewGroup viewGroup = findViewById(android.R.id.content);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.genrated_invoice, viewGroup, false);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        final AlertDialog alertDialog = builder.create();
        Objects.requireNonNull(alertDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialog.show();

        TextView wheatttl,ricettl,oilttl,wheatrs,ricers,oilrs,sumttl,pricettl,scanId,vendorId,time;
        int wheatttlInt,ricettlInt,oilttlInt,wheatrsInt,ricersInt,oilrsInt,kgTotal,priceTotal,income;
        String wheatUpdate,riceUpate,oilUpdate,incomeS,kgTotalString,priceStringString,oilRs,wheatRs,riceRs;
        Button print;

        int wheatRate = 0,riceRate = 0,oilRate = 0;

        wheatUpdate=wU;
        riceUpate=rU;
        oilUpdate=oU;

        wheatttl=dialogView.findViewById(R.id.wheatKg);
        ricettl=dialogView.findViewById(R.id.riceKg);
        oilttl=dialogView.findViewById(R.id.oilLitre);
        wheatrs=dialogView.findViewById(R.id.wheatRs);
        ricers=dialogView.findViewById(R.id.ricers);
        oilrs=dialogView.findViewById(R.id.oilRs);
        sumttl=dialogView.findViewById(R.id.totalKg);
        pricettl=dialogView.findViewById(R.id.totalRs);
        print=dialogView.findViewById(R.id.issueR);
        scanId=dialogView.findViewById(R.id.textName);
        vendorId=dialogView.findViewById(R.id.textVendor);
        time=dialogView.findViewById(R.id.textTime);

        Calendar c = Calendar.getInstance();
        System.out.println("Current time => " + c.getTime());

        @SuppressLint("SimpleDateFormat") SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        String formattedDate = df.format(c.getTime());

        String date="Date & Time : "+formattedDate;

        String uidd="Vendor Id : "+uID;

        String user="Customer : "+scannedId;


        String wheatS=wheatUpdate+" Kg.";
        String riceS=wheatUpdate+" Kg.";
        String oilS=wheatUpdate+" Ltr.";
        wheatttl.setText(wheatS);
        ricettl.setText(riceS);
        oilttl.setText(oilS);
        scanId.setText(user);
        vendorId.setText(uidd);
        time.setText(date);

        wheatttlInt=Integer.parseInt(wheatUpdate);
        ricettlInt=Integer.parseInt(riceUpate);
        oilttlInt=Integer.parseInt(oilUpdate);

        incomeS=fieldIncome;
        income=Integer.parseInt(incomeS);

        if (income>0 && income<=25000){
            wheatRate=0;
            riceRate=0;
            oilRate=0;
        }
        else if(income >25000 && income <50000){
            wheatRate=15;
            riceRate=20;
            oilRate=25;
        }
        else if(income>50000){
            wheatRate=25;
            riceRate=30;
            oilRate=35;
        }
        wheatrsInt=wheatttlInt*wheatRate;
        ricersInt=ricettlInt*riceRate;
        oilrsInt=oilttlInt*oilRate;


        wheatRs=String.valueOf(wheatrsInt);
        riceRs=String.valueOf(ricersInt);
        oilRs=String.valueOf(oilrsInt);

        kgTotal=wheatttlInt+ricettlInt+oilttlInt;
        priceTotal=wheatrsInt+ricersInt+oilrsInt;

        String ktS= kgTotal + " KgLtr";
        String ptS=" Rs."+ priceTotal;

        kgTotalString=ktS;
        priceStringString=ptS;

        String wrS=wheatRs+" Rs.";
        String rrS=riceRs+" Rs.";
        String orS=oilRs+" Rs.";
        wheatrs.setText(wrS);
        ricers.setText(rrS);
        oilrs.setText(orS);
        sumttl.setText(kgTotalString);
        pricettl.setText(priceStringString);
        imageView=dialogView.findViewById(R.id.imageView);

        print.setOnClickListener(v -> {

            Bitmap b = Screenshot.takeScreenshotOfRootView(imageView);
            imageView.setImageBitmap(b);
            Handler handler=new Handler();
            handler.postDelayed(alertDialog::dismiss,4000);
            Handler handler1=new Handler();
            handler1.postDelayed(() -> {
                Intent intent=new Intent(ScanActivity.this,MainActivity.class);
                startActivity(intent);
            },4000);

        });
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

    @Override
    protected void onStart() {
        super.onStart();
    }

}