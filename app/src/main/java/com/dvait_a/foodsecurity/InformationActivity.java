package com.dvait_a.foodsecurity;


import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import java.util.HashMap;
import java.util.Map;

public class InformationActivity extends AppCompatActivity {

    private EditText userName,userAddress,userChooseField;
    private CheckBox checkBox;
    private String uID;
    private DatabaseReference databaseReference;
    private FirebaseFirestore userCategory;
    private String typeOfUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_information);
        adjustFontScale(getResources().getConfiguration());


        userName=findViewById(R.id.editName);
        userAddress=findViewById(R.id.editAdd);
        userChooseField=findViewById(R.id.editIncome);
        TextView phoneNo = findViewById(R.id.textPhone);
        TextView eId = findViewById(R.id.textId);
        checkBox=findViewById(R.id.chk1);
        Button submit = findViewById(R.id.submit);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser mUser = mAuth.getCurrentUser();

        assert mUser != null;
        uID= mUser.getUid();
        String phoneNoUser = mUser.getPhoneNumber();

        FirebaseDatabase userDetails = FirebaseDatabase.getInstance();
        databaseReference= userDetails.getReference("Users");
        userCategory=FirebaseFirestore.getInstance();

        String textOp1="VERIFIED PHONE NUMBER :" + phoneNoUser;
        phoneNo.setText(textOp1);
        String textOp2="Generated e-ID :" +uID;
        eId.setText(textOp2);
        typeOfUser=getIntent().getStringExtra("type");


        assert typeOfUser != null;
        if(typeOfUser.equals("customer")){
            userChooseField.setHint("ENTER THE USER INCOME");
        }
        else if(typeOfUser.equals("vendor")){
            userChooseField.setHint("ENTER THE NGO UNIQUE ID NUMBER");
        }


        submit.setOnClickListener(v -> {
            if(checkBox.isChecked()){
                String userNameL=userName.getText().toString();
                String userAddressL=userAddress.getText().toString();
                String userChooseFieldL=userChooseField.getText().toString();
                String userTypeL=typeOfUser;
                if(userNameL.isEmpty() || userAddressL.isEmpty()|| userChooseFieldL.isEmpty()){
                    if(userNameL.isEmpty()){
                        userName.setError("Please Enter The Name ");
                    }
                    else if(userAddressL.isEmpty()){
                        userAddress.setError("Please Enter The Address ");
                    }
                    else {
                        userChooseField.setError("Please Enter The Field ");
                    }
                }
                else {
                    UserHelperClass userHelperClass= new UserHelperClass(userNameL,userAddressL,userChooseFieldL,userTypeL);
                    databaseReference.child(uID).setValue(userHelperClass);

                    if(typeOfUser.equals("customer")) {

                        int inc = Integer.parseInt(userChooseFieldL);
                        if (inc > 0 && inc < 25000) {
                            Record("25000");
                        } else if (inc > 25000 && inc < 50000) {
                            Record("50000");
                        } else if (inc > 50000 && inc < 90000) {
                            Record("90000");
                        } else {
                            Toast.makeText(InformationActivity.this, "You are not eligible for this Facility \nClosing the portal!", Toast.LENGTH_LONG).show();
                            finish();
                        }
                    }
                    else {
                        RecordVendor();
                    }
                }
            }
            else {
                Toast.makeText(InformationActivity.this,"PLEASE ACCEPT THE TERMS GIVEN ABOVE",Toast.LENGTH_LONG).show();
            }
        });
    }
    public  void Record(String range){
        Map<String, String> userMap = new HashMap<>();
        userMap.put("Wheat","0" );
        userMap.put("Rice","0" );
        userMap.put("Oil","0" );
        userMap.put("Date_time","00/00/0000 00:00 0M" );
        userMap.put("Vendor","0" );

        try {
            userCategory.collection(range).document(uID).set(userMap).addOnSuccessListener(avoid -> {
                Toast.makeText(InformationActivity.this, "THANKS FOR USING OUR FACILITY", Toast.LENGTH_SHORT).show();
                Intent intent =new Intent(InformationActivity.this,MainActivity.class);
                startActivity(intent);
            }).addOnFailureListener(e -> {
                String error = e.getMessage();
                Toast.makeText(InformationActivity.this, "Error :" + error, Toast.LENGTH_SHORT).show();
            });
        } catch (NullPointerException n) {
            Toast.makeText(InformationActivity.this, "Error Occurred", Toast.LENGTH_SHORT).show();
        }
    }

    public  void RecordVendor(){
        Map<String, String> userMap = new HashMap<>();
        userMap.put("Wheat","0" );
        userMap.put("Rice","0" );
        userMap.put("Oil","0" );

        try {
            userCategory.collection("Vendors").document(uID).set(userMap).addOnSuccessListener(avoid -> {
                Toast.makeText( InformationActivity.this, "THANKS FOR USING OUR FACILITY", Toast.LENGTH_SHORT).show();
                Intent intent =new Intent(InformationActivity.this,MainActivity.class);
                startActivity(intent);
            }).addOnFailureListener(e -> {
                String error = e.getMessage();
                Toast.makeText(InformationActivity.this, "Error :" + error, Toast.LENGTH_SHORT).show();
            });
        } catch (NullPointerException n) {
            Toast.makeText(InformationActivity.this, "Error Occurred", Toast.LENGTH_SHORT).show();
        }
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
}