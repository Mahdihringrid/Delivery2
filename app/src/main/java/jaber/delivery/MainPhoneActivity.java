package jaber.delivery;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.auth.FirebaseAuth;

import java.util.HashMap;
import java.util.Map;

import jaber.delivery.receiver.ConnectivityReceiver;
import jaber.delivery.utils.AppController;
import jaber.delivery.utils.Constants;

public class MainPhoneActivity extends AppCompatActivity implements ConnectivityReceiver.ConnectivityReceiverListener{

    private Spinner spinner;
    private EditText editText,etPassword;
    private ProgressDialog progressDialog;
    private String number,code,password,phoneNumber;
    Boolean isConnected;
    InterstitialAd mInterstitialAd;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_phone);
    //    setFont(Typeface.createFromAsset(getAssets(), "Montserrat-Medium.ttf"));
      //  setTheme(Theme.LIGHT);
        spinner = findViewById(R.id.spinnerCountries);
        spinner.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, CountryData.countryNames));
        editText = findViewById(R.id.editTextPhone);
        etPassword = findViewById(R.id.password);

        mInterstitialAd = new InterstitialAd(MainPhoneActivity.this);
        mInterstitialAd.setAdUnitId("ca-app-pub-9378948893318143/7817099204");
        mInterstitialAd.loadAd(new AdRequest.Builder().build());
        mInterstitialAd.setAdListener(new AdListener(){
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                if (mInterstitialAd.isLoaded()) {
                    mInterstitialAd.show();
                }
            }
        });
        findViewById(R.id.buttonContinue).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                 code = CountryData.countryAreaCodes[spinner.getSelectedItemPosition()];

                number = editText.getText().toString().trim();
                password = etPassword.getText().toString().trim();

                if (number.isEmpty() || number.length() < 8 || password.length() < 8 || password.equals("")) {
                    Toast.makeText(MainPhoneActivity.this, "الرجاء التأكد من المعلومات", Toast.LENGTH_SHORT).show();
                    //editText.requestFocus();
                    return;
                }
                try {
                    if(checkConnectivity()) {
                        progressDialog = new ProgressDialog(MainPhoneActivity.this);
                        progressDialog.setMessage("الرجاء الانتظار");
                        progressDialog.show();
                        validatePhone();
                    }
                    else
                    {
                        showSnack();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });
    }
    public void showSnack() {


        Snackbar.make(findViewById(R.id.mainphone), getString(R.string.NO_INTERNET), Snackbar.LENGTH_LONG)
                .show();
    }

    @Override
    public void onNetworkChange(boolean inConnected) {
        this.isConnected = inConnected;

    }

    private boolean checkConnectivity()
    {
        return ConnectivityReceiver.isConnected();
    }


    @Override
    protected void onResume() {
        super.onResume();
        AppController.getInstance().setConnectivityReceiver(this);
    }
    public void validatePhone() throws Exception{
        String TAG = "VALIDATE_PHONE";
        String url = Constants.VALIDATEPHONE_URL;
        StringRequest jsonObjectRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                progressDialog.dismiss();
                Log.i("response", response);
                if(response.equals("notok"))

                {
                     phoneNumber = "+" + code + number;
                    SharedPreferences sharedPreferences2 = getApplicationContext().getSharedPreferences(getApplicationContext().getString(R.string.PREF_JABER), MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences2.edit();
                    editor.putString("password",password);
                    editor.apply();
                    Intent intent = new Intent(MainPhoneActivity.this, VerifyPhoneActivity.class);
                    intent.putExtra("phonenumber", phoneNumber);
                    intent.putExtra("phonenumberreal", number);
                    intent.putExtra("password", password);

                    startActivity(intent);


                  /*  if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                        Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                        startActivity(intent);
                    } */
                }
                else
                {
                    Toast.makeText(MainPhoneActivity.this, "تم تسجيل مسبقا حساب بهذا الرقم!", Toast.LENGTH_SHORT).show();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                try {
                    progressDialog.dismiss();
                    Log.e("error", "" +error.getMessage());
                }catch (NullPointerException e)
                {
                    e.printStackTrace();
                }


            }
        })
        {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                Map<String,String> params= new HashMap<String,String>();
                params.put("phonenumber",number);


                return params;
            }
        };



        AppController.getInstance().addToRequestQueue(jsonObjectRequest, TAG);

    }
   /* @Override
    protected void onStart() {
        super.onStart();

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            Intent intent = new Intent(this, ProfileActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            startActivity(intent);
        }
    } */
    }

