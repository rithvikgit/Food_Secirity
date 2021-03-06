package com.dvait_a.foodsecurity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.google.zxing.qrcode.QRCodeWriter;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private TextView userName,userAddress,userIncome,userPhoneNo,userEid;
    private ImageView qrCode;
    private Button scanAcc,addUid;
    private String userEidSystem;
    private String sharedIncome;
    private String typeOfUser,typeOfUserSys;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        adjustFontScale(getResources().getConfiguration());
        showLoad();
        mAuth=FirebaseAuth.getInstance();
        mUser=mAuth.getCurrentUser();

        userName=findViewById(R.id.textName);
        userAddress=findViewById(R.id.textAdd);
        userIncome=findViewById(R.id.textIncome);
        userPhoneNo=findViewById(R.id.textPhone);
        userEid=findViewById(R.id.textId);
        qrCode =findViewById(R.id.qrCode);
        Button recordAcc = findViewById(R.id.record);
        Button logoutAcc = findViewById(R.id.logout);
        scanAcc=findViewById(R.id.scan);
        addUid=findViewById(R.id.addManually);

        typeOfUserSys=getIntent().getStringExtra("type");

        recordAcc.setOnClickListener(v -> {
            Intent intent=new Intent(MainActivity.this,RecordActivity.class);
            intent.putExtra("income",sharedIncome);
            intent.putExtra("type",typeOfUser);
            startActivity(intent);
        });

        logoutAcc.setOnClickListener(v -> {
            mAuth.signOut();
            mUser=null;
            Intent i=new Intent(MainActivity.this,SplashScreenActivity.class);
            startActivity(i);
            finish();
        });

        scanAcc.setOnClickListener(v -> {
            IntentIntegrator intentIntegrator=new IntentIntegrator(MainActivity.this);
            intentIntegrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
            intentIntegrator.setCameraId(0);
            intentIntegrator.setPrompt("Scanning");
            intentIntegrator.initiateScan();
        });

        addUid.setOnClickListener(v -> addRecord());
    }
    public void getUserData(final String userEidSystem){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(userEidSystem);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    String name = dataSnapshot.child("userName").getValue().toString();
                    String address = dataSnapshot.child("userAddress").getValue().toString();
                    String field = dataSnapshot.child("userField").getValue().toString();
                    typeOfUser = dataSnapshot.child("userType").getValue().toString();
                    sharedIncome=field;
                    setUserData(name,address,field,typeOfUser);
                    setLayout(typeOfUser);
                }catch (NullPointerException n){
                    Toast.makeText(MainActivity.this,"Record Not Found",Toast.LENGTH_LONG).show();
                    Intent intent=new Intent(MainActivity.this,InformationActivity.class);
                    intent.putExtra("type",typeOfUserSys);
                    startActivity(intent);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                FirebaseAuth.getInstance().signOut();
                Intent i=new Intent(MainActivity.this,LoginActivity.class);
                startActivity(i);

            }
        });
    }

    private void setLayout(String typeOfUser) {
        if(typeOfUser.equals("customer")){
            scanAcc.setVisibility(View.GONE);
            addUid.setVisibility(View.GONE);
        }
        else {
            scanAcc.setVisibility(View.VISIBLE);
            addUid.setVisibility(View.VISIBLE);
        }
    }

    private void setUserData(String name, String address, String income,String type) {

        String userNameTextView= "Name : "+name;
        String userAddrTextView="Address : "+address;
        String userIncomeTextView;
        if(type.equals("customer")) {
            userIncomeTextView = "Income : Rs. " + income;
        }
        else{
            userIncomeTextView="FSSAI No :"+income;
        }

        userName.setText(userNameTextView);
        userAddress.setText(userAddrTextView);
        userIncome.setText(userIncomeTextView);
    }
    public void genrateQr(String no){
        if(no!=null){
            try {
                QRCodeWriter qrCodeWriter=new QRCodeWriter();
                BitMatrix bitMatrix=qrCodeWriter.encode(no, BarcodeFormat.QR_CODE,250,250);
                Bitmap bitmap=Bitmap.createBitmap(250,250,Bitmap.Config.RGB_565) ;
                for(int x=0;x<250;x++){
                    for(int y=0;y<250;y++){
                        bitmap.setPixel(x,y,bitMatrix.get(x,y)? Color.BLACK : Color.WHITE);
                    }
                }
                qrCode.setImageBitmap(bitmap);
            }
            catch (WriterException e){
                e.printStackTrace();
            }
        }
        else {
            Toast.makeText(MainActivity.this,"Error in QR",Toast.LENGTH_LONG).show();
        }

    }

    @Override
    protected void onStart() {
        super.onStart();

        if(mUser==null){
            Intent intent=new Intent(MainActivity.this,UserSelectActivity.class);
            startActivity(intent);
        }
        else {
            String phoneNo = mUser.getPhoneNumber();
            userEidSystem=mUser.getUid();
            String userPhoneNoTextView="Registered Phone No : "+ phoneNo;
            userPhoneNo.setText(userPhoneNoTextView);
            String userUidTextView="U-ID : "+userEidSystem;
            userEid.setText(userUidTextView);
            getUserData(userEidSystem);
            Handler handler = new Handler();
            handler.postDelayed(() -> genrateQr(userEidSystem), 3000);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        final IntentResult intentResult=IntentIntegrator.parseActivityResult(requestCode,resultCode,data);
        if(intentResult !=null && intentResult.getContents()!=null){
            Handler handler=new Handler();
            handler.postDelayed(() -> {
                Intent i=new Intent(MainActivity.this,ScanActivity.class);
                i.putExtra("scannedId",intentResult.getContents());
                i.putExtra("uid",userEidSystem);
                startActivity(i);
            },6000);

        }
        else {
            Toast.makeText(MainActivity.this,"Invalid code",Toast.LENGTH_LONG).show();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    public void addRecord() {
        ViewGroup viewGroup = findViewById(android.R.id.content);
        final View dialogView = LayoutInflater.from(MainActivity.this).inflate(R.layout.add_manual_request, viewGroup, false);
        final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setView(dialogView);
        final AlertDialog alertDialog = builder.create();
        Objects.requireNonNull(alertDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialog.show();

        EditText editUid;
        Button submit;

        editUid=dialogView.findViewById(R.id.itemUid);
        submit=dialogView.findViewById(R.id.submitRecord);

        submit.setOnClickListener(v -> {
            String uid=editUid.getText().toString();

            if(uid.isEmpty()){
                editUid.setError("Empty UID");
            }else{
                Intent i=new Intent(MainActivity.this,ScanActivity.class);
                i.putExtra("scannedId",uid);
                i.putExtra("uid",userEidSystem);
                startActivity(i);
            }
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
        handler.postDelayed(alertDialog::dismiss, 0);
    }
}