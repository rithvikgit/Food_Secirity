package com.dvait_a.foodsecurity;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import android.widget.Button;

public class UserSelectActivity extends AppCompatActivity {

    Button user_mode,vendor_mode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_select);

        adjustFontScale(getResources().getConfiguration());

        user_mode=findViewById(R.id.user_mode);
        vendor_mode=findViewById(R.id.vendor_mode);

        user_mode.setOnClickListener(v -> {
            Intent intent=new Intent(UserSelectActivity.this,LoginActivity.class);
            intent.putExtra("type","customer");
            startActivity(intent);

        });

        vendor_mode.setOnClickListener(v -> {
            Intent intent=new Intent(UserSelectActivity.this,LoginActivity.class);
            intent.putExtra("type","vendor");
            startActivity(intent);
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