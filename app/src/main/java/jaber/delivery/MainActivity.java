package jaber.delivery;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import jaber.delivery.LocationUtil.LocationHelper;
import jaber.delivery.receiver.ConnectivityReceiver;
import jaber.delivery.utils.AppController;
import jaber.delivery.utils.Constants;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,ActivityCompat.OnRequestPermissionsResultCallback,ConnectivityReceiver.ConnectivityReceiverListener {

    private TextView title;
    private TextView signup;
    private TextView forgotPassword;
    private ImageView logo;
    private EditText phone;
    private EditText password;
    private TextInputLayout emailWrapper;
    private TextInputLayout passwordWrapper;
    private Button submit,btSignUp;
    private String phoneNumber,stPhone,stPassword;
    private ImageButton google;
    private ImageButton facebook;
    private boolean showLogin;
    Boolean isConnected;
    private ProgressDialog progressDialog;


    public Location mLastLocation;

    Double longitude,latitude;
    LocationHelper locationHelper;
    private String getPhoneNumber,getPassword;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Initialize views
        MobileAds.initialize(this, "ca-app-pub-9378948893318143~5844707837");

        locationHelper=new LocationHelper(this);
        locationHelper.checkpermission();
        if (locationHelper.checkPlayServices()) {

            // Building the GoogleApi client
            locationHelper.buildGoogleApiClient();
        }


        title = (TextView) findViewById(R.id.title);
        signup = (TextView) findViewById(R.id.signup);
        forgotPassword = (TextView) findViewById(R.id.forgotPassword);
        logo = (ImageView) findViewById(R.id.logo);
        phone = (EditText) findViewById(R.id.email);
        password = (EditText) findViewById(R.id.password);
        emailWrapper = (TextInputLayout) findViewById(R.id.wrapper_email);
        passwordWrapper = (TextInputLayout) findViewById(R.id.wrapper_password);
        submit = (Button) findViewById(R.id.submit);
        btSignUp = (Button) findViewById(R.id.signInPhone);
        google = (ImageButton) findViewById(R.id.google_login);
        facebook = (ImageButton) findViewById(R.id.facebook_login);

        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(getApplicationContext().getString(R.string.PREF_JABER), MODE_PRIVATE);
        getPhoneNumber = sharedPreferences.getString("phonenumber", "");
        getPassword = sharedPreferences.getString("password", "");
        if(!getPhoneNumber.equals("") && !getPassword.equals(""))
        {
            phone.setText(getPhoneNumber);
            password.setText(getPassword);
        }

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    stPhone = phone.getText().toString().trim();
                    stPassword = password.getText().toString().trim();
                    if(!stPhone.equals("") && !stPassword.equals("")) {

                        if(checkConnectivity()) {
                            progressDialog = new ProgressDialog(MainActivity.this);
                            progressDialog.setMessage("الرجاء الإنتظار");
                            progressDialog.show();
                            validateUser();
                        }
                        else
                        {
                            showSnack();
                        }
                    }
                    else {
                        Toast.makeText(MainActivity.this, "الرجاء التأكد من المعلومات", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });
        btSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                Intent i = new Intent(MainActivity.this,MainPhoneActivity.class);
                startActivity(i);


            }
        });


    }

    public void validateUser() throws Exception{
        String TAG = "VALIDATE";
        String url = Constants.VALIDATE_URL;
        StringRequest jsonObjectRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                progressDialog.dismiss();
                Log.i("response", response);
                if(!response.equals("notok"))

                {
                    mLastLocation = locationHelper.getLocation();

                    if (mLastLocation != null) {
                        latitude = mLastLocation.getLatitude();
                        longitude = mLastLocation.getLongitude();

                        SharedPreferences sharedPreferences2 = getApplicationContext().getSharedPreferences(getApplicationContext().getString(R.string.PREF_JABER), MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences2.edit();
                        editor.putString("latitude", String.valueOf(latitude));
                        editor.putString("longitude", String.valueOf(longitude));
                        editor.putString("id_user", response);
                        editor.putString("phonenumber", stPhone);
                        editor.putString("password", stPassword);

                        editor.apply();
                       // getAddress();
                        Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                        startActivity(intent);

                    } else {


                        showToast("لا يمكن الحصول على الموقع. تأكد من تمكين الموقع على الجهاز وفتح التطبيق من جديد");
                    }
                  /*  if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                        Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                        startActivity(intent);
                    } */
                }
                else
                {
                    showToast("الرجاء التأكد من المعلومات");

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
                params.put("phonenumber",stPhone);
                params.put("password",stPassword);


                return params;
            }
        };



        AppController.getInstance().addToRequestQueue(jsonObjectRequest, TAG);

    }
    public void showSnack() {


            Snackbar.make(findViewById(R.id.relmain), getString(R.string.NO_INTERNET), Snackbar.LENGTH_LONG)
                    .show();
    }

    @Override
    public void onNetworkChange(boolean inConnected) {
        this.isConnected = inConnected;

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        locationHelper.onActivityResult(requestCode,resultCode,data);
    }
    private boolean checkConnectivity()
    {
        return ConnectivityReceiver.isConnected();
    }


    @Override
    protected void onResume() {
        super.onResume();
        AppController.getInstance().setConnectivityReceiver(this);
        locationHelper.checkPlayServices();
    }

    /**
     * Google api callback methods
     */
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.i("Connection failed:", " ConnectionResult.getErrorCode() = "
                + result.getErrorCode());
    }

    @Override
    public void onConnected(Bundle arg0) {

        // Once connected with google api, get the location
        mLastLocation=locationHelper.getLocation();
    }

    @Override
    public void onConnectionSuspended(int arg0) {
        locationHelper.connectApiClient();
    }


    // Permission check functions
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        // redirects to utils
        locationHelper.onRequestPermissionsResult(requestCode,permissions,grantResults);

    }

    public void showToast(String message)
    {
        Toast.makeText(this,message,Toast.LENGTH_SHORT).show();
    }


}
