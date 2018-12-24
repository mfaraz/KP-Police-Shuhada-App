package com.example.kt.shudaapp;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.kt.shudaapp.InterfaceClasses.ComplaintiApi;
import com.example.kt.shudaapp.InterfaceClasses.RegisterApi;
import com.example.kt.shudaapp.ModelClasses.ComplaintRegisterModel;
import com.example.kt.shudaapp.ModelClasses.RegisterModel;
import com.example.kt.shudaapp.Utils.Config;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import im.delight.android.location.SimpleLocation;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ComplaintActivity extends AppCompatActivity {
    private SimpleLocation location;
    private EditText subject_et, details_et;
    private RelativeLayout submit_btn;
    String subject;
    String details;
    String date;
    String time;
    HashMap<String, RequestBody> mMap = new HashMap<>();
    ProgressDialog progressDialog;
    ConstraintLayout complaint_layout;
    SharedPreferences mSharedPreferences;
    String s_id, mobile_no, member_id;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complaint);

        mSharedPreferences = getApplicationContext().getSharedPreferences("pref", Context.MODE_PRIVATE);
        s_id = mSharedPreferences.getString("s_id", "No Data");
        mobile_no = mSharedPreferences.getString("mobile_no", "No Data");
        member_id = mSharedPreferences.getString("member_id", "No Data");

        location = new SimpleLocation(this);
        if (!location.hasLocationEnabled()){
            SimpleLocation.openSettings(this);
        }
        complaint_layout = findViewById(R.id.complaint_layout);
        subject_et = findViewById(R.id.subject);
        details_et = findViewById(R.id.details);
        submit_btn = findViewById(R.id.submit_btn);
        progressDialog = new ProgressDialog(this);
        submit_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                subject = subject_et.getText().toString();
                details = details_et.getText().toString();
                 if(TextUtils.isEmpty(details)){
                     details_et.setError("Enter Details");
                     details_et.requestFocus();
                }else {
                     if (isNetworkAvailable()){
                         runOnUiThread(new Runnable() {
                             @Override
                             public void run() {
                                 progressDialog.setMessage("Please wait...");
                                 progressDialog.setCanceledOnTouchOutside(false);
                                 progressDialog.show();
                                 DateFormat df = new SimpleDateFormat("h:mm a");
                                 DateFormat df2 = new SimpleDateFormat("yyyy-MM-dd");
                                 final String time = df.format(Calendar.getInstance().getTime());
                                 final String date = df2.format(Calendar.getInstance().getTime());
                                 final Double lat = location.getLatitude();
                                 final Double lng = location.getLongitude();
                                 new AlertDialog.Builder(ComplaintActivity.this)
                                         .setMessage("Are you sure!")
                                         .setPositiveButton("OK", new DialogInterface.OnClickListener()
                                         {
                                             @Override
                                             public void onClick(DialogInterface dialog, int which) {
                                                 postData(mobile_no, subject, details, date, time, s_id, lat, lng, member_id);

                                             }

                                         })
                                         .setCancelable(false)
                                         .setNegativeButton("Cancel", null)
                                         .show();

                             }
                         });

                     }else {
                         runOnUiThread(new Runnable() {
                             @Override
                             public void run() {
                                 progressDialog.dismiss();
                                 Snackbar snackbar = Snackbar.make(complaint_layout, "No internet Connection", Snackbar.LENGTH_SHORT);
                                 View snackBarView = snackbar.getView();
                                 snackBarView.setBackgroundColor(getResources().getColor(R.color.text_color_red));
                                 TextView textView =  snackBarView.findViewById(android.support.design.R.id.snackbar_text);
                                 textView.setTextColor(getResources().getColor(android.R.color.white));
                                 snackbar.show();
                             }
                         });
                     }

                }
            }
        });

        }

    private void postData(String mobile_no, String subject, String details, String date, String time, String s_id, Double lat, Double lng, String member_id) {
        mMap.put("mobile_no", createPartFromString(mobile_no));
        mMap.put("subject", createPartFromString(subject));
        mMap.put("message_detail", createPartFromString(details));
        mMap.put("recived_date", createPartFromString(date));
        mMap.put("time",  createPartFromString(time));
        mMap.put("s_id",  createPartFromString(s_id));
        mMap.put("lat",  createPartFromString(String.valueOf(lat)));
        mMap.put("lng",  createPartFromString(String.valueOf(lng)));
        mMap.put("member_id",  createPartFromString(member_id));
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Config.BaseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        ComplaintiApi complaintiApi =  retrofit.create(ComplaintiApi.class);
        Call<ComplaintRegisterModel> call = complaintiApi.post_complaint(mMap);
        call.enqueue(new Callback<ComplaintRegisterModel>() {
            @Override
            public void onResponse(Call<ComplaintRegisterModel> call, Response<ComplaintRegisterModel> response) {
                if (response.isSuccessful()){
                    if (response.body().getSuccess() == 1){
                        Log.e("onResponse: ", response.toString());
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progressDialog.dismiss();
                                new AlertDialog.Builder(ComplaintActivity.this)
                                        .setTitle("Note!")
                                        .setMessage("Your problem has been registered")
                                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                finish();
                                            }
                                        })
                                        .show();
                            }
                        });

                    }else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progressDialog.dismiss();
                                Snackbar snackbar = Snackbar
                                        .make(complaint_layout, "Some thing went wrong", Snackbar.LENGTH_SHORT);
                                View snackBarView = snackbar.getView();
                                snackBarView.setBackgroundColor(getResources().getColor(R.color.text_color_red));
                                TextView textView =  snackBarView.findViewById(android.support.design.R.id.snackbar_text);
                                textView.setTextColor(getResources().getColor(android.R.color.white));
                                snackbar.show();

                            }
                        });
                    }
                }
            }

            @Override
            public void onFailure(Call<ComplaintRegisterModel> call, Throwable t) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.dismiss();
                        Snackbar snackbar = Snackbar
                                .make(complaint_layout, "Some thing went wrong dsdadsa", Snackbar.LENGTH_SHORT);
                        View snackBarView = snackbar.getView();
                        snackBarView.setBackgroundColor(getResources().getColor(R.color.text_color_red));
                        TextView textView =  snackBarView.findViewById(android.support.design.R.id.snackbar_text);
                        textView.setTextColor(getResources().getColor(android.R.color.white));
                        snackbar.show();

                    }
                });
            }
        });
    }

    private boolean isNetworkAvailable() {

        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();

        boolean isAvailable = false;
        if (networkInfo != null && networkInfo.isConnected())
        {
            isAvailable = true;

        }
        return isAvailable;
    }
    @NonNull
    private RequestBody createPartFromString(String val) {
        return RequestBody.create(okhttp3.MultipartBody.FORM,  val);
    }


    @Override
    protected void onResume() {
        super.onResume();

        // make the device update its location
        location.beginUpdates();

        // ...
    }

    @Override
    protected void onPause() {
        // stop location updates (saves battery)
        location.endUpdates();

        // ...

        super.onPause();
    }
}
