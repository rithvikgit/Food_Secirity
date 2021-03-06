package com.dvait_a.foodsecurity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import java.util.concurrent.TimeUnit;

public class LoginActivity extends AppCompatActivity {
    private TextView phone_no;
    private String completePhoneNo;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private FirebaseAuth mAuth;
    private String typeOfUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        adjustFontScale(getResources().getConfiguration());

        Button proceed = findViewById(R.id.login);
        phone_no=findViewById(R.id.phone_no);

        mAuth=FirebaseAuth.getInstance();

        typeOfUser=getIntent().getStringExtra("type");

        proceed.setOnClickListener(v -> {
            String phoneNo;
            phoneNo=phone_no.getText().toString();
            if(phoneNo!=null && phoneNo.length()==10){
                completePhoneNo="+91" + phoneNo;

                if(completePhoneNo.length()<13)
                    Toast.makeText(LoginActivity.this,"Enter the Valid Phone Number",Toast.LENGTH_LONG).show();
                else {
                    PhoneAuthProvider.getInstance().verifyPhoneNumber(
                            completePhoneNo,
                            60,
                            TimeUnit.SECONDS,
                            LoginActivity.this,
                            mCallbacks
                    );
                }
            }
            else {
                Toast.makeText(LoginActivity.this,"Enter the Valid Phone Number",Toast.LENGTH_LONG).show();
            }
        });
        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                Toast.makeText(LoginActivity.this,""+e.getMessage(),Toast.LENGTH_LONG).show();

            }

            @Override
            public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                //super.onCodeSent(s, forceResendingToken);
                Intent intent = new Intent(LoginActivity.this, VerifyActivity.class);
                intent.putExtra("otpSystem", s);
                intent.putExtra("PhoneNo",completePhoneNo);
                intent.putExtra("type",typeOfUser);
                startActivity(intent);
            }
        };
    }
    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(LoginActivity.this, "LOADING PLEASE WAIT", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        intent.putExtra("type",typeOfUser);
                        startActivity(intent);

                    } else {
                        Toast.makeText(LoginActivity.this, "FAILED TO LOG YOU IN", Toast.LENGTH_LONG).show();
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
}