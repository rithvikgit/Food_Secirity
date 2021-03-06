package com.dvait_a.foodsecurity;


import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

public class VerifyActivity extends AppCompatActivity {
    private TextView otpUser;
    private FirebaseAuth mAuth;
    private String mAuthCredentials;
    private String typeOfUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify);
        adjustFontScale(getResources().getConfiguration());

        otpUser=findViewById(R.id.otp_sent);
        Button verify = findViewById(R.id.verify);

        mAuth=FirebaseAuth.getInstance();

        mAuthCredentials=getIntent().getStringExtra("otpSystem");
        typeOfUser=getIntent().getStringExtra("type");

        verify.setOnClickListener(v -> {
            String otpEntered=otpUser.getText().toString();
            if(otpEntered.isEmpty()){
                Toast.makeText(VerifyActivity.this,"PLEASE ENTERED THE OTP SENT ON \n",Toast.LENGTH_LONG).show();
            }
            else {
                PhoneAuthCredential phoneAuthCredential= PhoneAuthProvider.getCredential(mAuthCredentials,otpEntered);
                signInWithPhoneAuthCredential(phoneAuthCredential);
            }
        });
    }
    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(VerifyActivity.this,"Successful",Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(VerifyActivity.this, MainActivity.class);
                        intent.putExtra("type",typeOfUser);
                        startActivity(intent);
                    } else {
                        Toast.makeText(VerifyActivity.this,"Failed",Toast.LENGTH_LONG).show();
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