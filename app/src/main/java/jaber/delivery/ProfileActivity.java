package jaber.delivery;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.Toast;
import com.google.android.gms.ads.*;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import javax.net.ssl.HttpsURLConnection;
import jaber.delivery.receiver.ConnectivityReceiver;
import jaber.delivery.utils.AppController;
import jaber.delivery.utils.Constants;

public class ProfileActivity extends AppCompatActivity implements ConnectivityReceiver.ConnectivityReceiverListener{

    EditText etMessage;
    public Bitmap FixBitmap;
    String ImageTag = "image_tag" ;
    String password;
    String ImageName = "image_data" ;
    String latitude,longitude,idUser;
    String message,phoneNumber,Password;
    ByteArrayOutputStream byteArrayOutputStream ;
    String ConvertImage,stMessage;
    Boolean isConnected;
    ImageView showImage;
    Button btSelectImage,btUploadImage;
    public String image;
    private ProgressDialog progressDialog;
    boolean check = true;
    private int GALLERY = 1, CAMERA = 2;
    public int lengthbmp;
    private Handler mHandler;       // Handler to display the ad on the UI thread
    private Runnable displayAd;     // Code to execute to perform this operation
    private InterstitialAd mInterstitialAd;

    ScrollView scrollView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        scrollView = (ScrollView)findViewById(R.id.relprofile);
        btSelectImage = (Button)findViewById(R.id.selectimage);
        btUploadImage = (Button)findViewById(R.id.uploadimage);
        showImage = (ImageView)findViewById(R.id.show_image);
        etMessage = (EditText)findViewById(R.id.etMessage);
        mInterstitialAd = new InterstitialAd(ProfileActivity.this);
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

        byteArrayOutputStream = new ByteArrayOutputStream();
        btSelectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPictureDialog();


            }
        });


        btUploadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this);
                builder.setTitle("تأكيد الطلب ؟");
                builder.setPositiveButton("نعم", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        //   GetImageNameFromEditText = imageName.getText().toString().trim();
                        stMessage = etMessage.getText().toString().trim();
                        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(getApplicationContext().getString(R.string.PREF_JABER), MODE_PRIVATE);
                        phoneNumber = sharedPreferences.getString("phonenumber", "");
                        latitude = sharedPreferences.getString("latitude", "");
                        longitude = sharedPreferences.getString("longitude", "");
                        password = sharedPreferences.getString("password","");
                        idUser = sharedPreferences.getString("id_user","");

                        Log.i("MESSAGE",stMessage);
                        Log.i("PHONE",phoneNumber);
                        Log.i("LATITUDE",String.valueOf(latitude));
                        Log.i("LONGITUDE",String.valueOf(longitude));
                        Log.i("PASSWORD",password);

                        if(!stMessage.equals(""))
                        {
                            if(FixBitmap != null)
                            {
                                if(imgSize(FixBitmap) < 10000000 ) {
                                    if (checkConnectivity()) {
                                        try {
                                            progressDialog = new ProgressDialog(ProfileActivity.this);
                                            progressDialog.setMessage("الرجاء الانتظار");
                                            progressDialog.show();
                                            sendData(FixBitmap);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    } else {
                                        showSnack();

                                    }
                                }
                                else{
                                    Toast.makeText(ProfileActivity.this,"حجم الصورة يفوق 10MB!",Toast.LENGTH_LONG).show();
                                }

                            }
                            else
                            {
                                if (checkConnectivity()) {
                                    try {
                                        progressDialog = new ProgressDialog(ProfileActivity.this);
                                        progressDialog.setMessage("الرجاء الانتظار");
                                        progressDialog.show();
                                        sendData(FixBitmap);
                                    } catch (Exception e) {
                                        if(progressDialog != null)
                                            progressDialog.dismiss();
                                        e.printStackTrace();
                                    }
                                } else {
                                    showSnack();

                                }
                            }
                        }
                        else
                        {
                            Toast.makeText(ProfileActivity.this,"الرجاء التأكد من المعلومات",Toast.LENGTH_LONG).show();
                        }

                    }

                });
                builder.setNegativeButton("كلا", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                        dialog.dismiss();
                    }
                });
                AlertDialog alt = builder.create();
                alt.show();

            }
        });

        if (ContextCompat.checkSelfPermission(ProfileActivity.this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{android.Manifest.permission.CAMERA},
                        5);
            }
        }
    }

    private void showPictureDialog(){
        AlertDialog.Builder pictureDialog = new AlertDialog.Builder(this);
        pictureDialog.setTitle("إختيار");
        String[] pictureDialogItems = {
                "معرض الصور",
                "الكاميرا" };
        pictureDialog.setItems(pictureDialogItems,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                choosePhotoFromGallary();
                                break;
                            case 1:
                                takePhotoFromCamera();
                                break;
                        }
                    }
                });
        pictureDialog.show();
    }
    public void choosePhotoFromGallary() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        startActivityForResult(galleryIntent, GALLERY);
    }

    private void takePhotoFromCamera() {
        Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, CAMERA);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == this.RESULT_CANCELED) {
            return;
        }
        if (requestCode == GALLERY) {
            if (data != null) {
                Uri contentURI = data.getData();
                try {
                    FixBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), contentURI);
                    long testing = imgSize(FixBitmap);
                    Log.i("SIZEIMAGE", String.valueOf(testing));
                    //showImage.setImageBitmap(FixBitmap);
                    Glide.with(this).load(FixBitmap).into(showImage);
                    showImage.setVisibility(View.VISIBLE);
                    scrollView.fullScroll(View.FOCUS_DOWN);
                    // String path = saveImage(bitmap);
                    //Toast.makeText(MainActivity.this, "Image Saved!", Toast.LENGTH_SHORT).show();
                 //   ShowSelectedImage.setImageBitmap(FixBitmap);

                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(ProfileActivity.this, "خطئ!", Toast.LENGTH_SHORT).show();
                }
            }

        } else if (requestCode == CAMERA) {
            FixBitmap = (Bitmap) data.getExtras().get("data");
          //  ShowSelectedImage.setImageBitmap(FixBitmap);
            long testing = imgSize(FixBitmap);
            Log.i("SIZEIMAGE", String.valueOf(testing));
            Glide.with(this).load(FixBitmap).into(showImage);
            showImage.setVisibility(View.VISIBLE);
            scrollView.fullScroll(View.FOCUS_DOWN);

            //  saveImage(thumbnail);
            //Toast.makeText(ShadiRegistrationPart5.this, "Image Saved!", Toast.LENGTH_SHORT).show();
        }
    }



    public void showSnack() {


        Snackbar.make(findViewById(R.id.relprofile), getString(R.string.NO_INTERNET), Snackbar.LENGTH_LONG)
                .show();
    }

    public void sendData(final Bitmap bitmap) throws Exception{

        String TAG = "POSTS";
        String url = Constants.POSTS_URL;
        StringRequest jsonObjectRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if(progressDialog != null)
                    progressDialog.dismiss();
                if(response.equals("success"))
                {
                    Toast.makeText(getApplicationContext(),"تم إرسال الطلب بنجاح",Toast.LENGTH_LONG).show();


                }
                else
                {
                    Toast.makeText(getApplicationContext(),"لا يمكن إرسال طلبك قبل مرور ساعة على الطلب السابق",Toast.LENGTH_LONG).show();


                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                try {
                    if(progressDialog != null) {
                        progressDialog.dismiss();
                        Log.e("error", "" + error.getMessage());
                    }
                    Toast.makeText(getApplicationContext(),"فشل في إرسال الصورة الرجاء المحاولة لاحقا",Toast.LENGTH_LONG).show();
                    AdRequest adRequest = new AdRequest.Builder().build();

                    // Prepare the Interstitial Ad


                }catch (NullPointerException e)
                {
                    e.printStackTrace();
                }


            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                Map<String, String> params = new HashMap<String, String>();
               // params.put("image_tag", GetImageNameFromEditText);
                if(bitmap != null)
                params.put("image_data", imgtoString(FixBitmap));
                else
                    params.put("image_data", "NO_IMAGE");
                params.put("phonenumber", phoneNumber);
                params.put("password",password);
                params.put("latitude", latitude);
                params.put("longitude", longitude);
                params.put("message", stMessage);
                params.put("id_user", idUser);


                return params;
            }
        };


        AppController.getInstance().addToRequestQueue(jsonObjectRequest, TAG);
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 5) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Now user should be able to use camera

            }
            else {

                Toast.makeText(ProfileActivity.this, "يتعذر استخدام الكاميرا ... الرجاء السماح لنا باستخدام الكاميرا", Toast.LENGTH_LONG).show();

            }
        }
    }

    private String imgtoString(Bitmap bitmap)
    {
        try {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG,100,byteArrayOutputStream);
        byte[] imgbytes = byteArrayOutputStream.toByteArray();
         image = Base64.encodeToString(imgbytes,Base64.DEFAULT);
        }catch (Exception e)
        {
            e.getMessage();
        }
        return image;

    }
    private int imgSize(Bitmap bitmap)
    {
        try {


        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,100,byteArrayOutputStream);
        byte[] imgbytes = byteArrayOutputStream.toByteArray();
        lengthbmp = imgbytes.length;
        }catch (Exception e)
        {
            e.getMessage();
        }
        return lengthbmp;
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menuitem, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

         if (item.getItemId() == R.id.action_settings)
        {
            FirebaseAuth.getInstance().signOut();

            Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
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

    @Override
    public void onNetworkChange(boolean inConnected) {
        this.isConnected = inConnected;


    }
}
