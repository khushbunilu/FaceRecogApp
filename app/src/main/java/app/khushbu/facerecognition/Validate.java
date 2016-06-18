package app.khushbu.facerecognition;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.media.FaceDetector;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import cat.lafosca.facecropper.FaceCropper;
import cz.msebera.android.httpclient.Header;

class Server_call extends AsyncTask<String, Integer, Long> {


    ProgressDialog dialog;
    String encodedImage;
    Validate v;

    Server_call(Validate v, String encodedImage){
        dialog = new ProgressDialog(v);
        // dialog.setTitle("Processing...");
        dialog.setMessage(Html.fromHtml("Loading. Please wait..."));
        dialog.setCancelable(false);
        dialog.setIndeterminate(true);
        this.encodedImage=encodedImage;
        this.v=v;

    }

    @Override
    protected Long doInBackground(String... params) {
        try{
            v.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    SendImageToServer();
                }
            });

        }
        catch(Exception e){
            v.runOnUiThread(new Runnable() {
                @Override
                public void run() {

                }
            });
        }
        return null;
    }

    protected void onPreExecute() {
        dialog.show();
    }


    protected void onPostExecute(Long result) {

    }

    void SendImageToServer(){


        RequestParams rp = new RequestParams();
        rp.add("Image", encodedImage);


        HttpUtils.post("imageclass/RecogniseImage", rp, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                System.out.println("successfull");
                TextView tv = (TextView) v.findViewById(R.id.textView2);
                tv.setVisibility(View.VISIBLE);
                try {
                    tv.setText("Image is of " + String.valueOf(response.get("name")));
                    dialog.dismiss();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode,
                                  cz.msebera.android.httpclient.Header[] headers,
                                  java.lang.Throwable throwable,
                                  org.json.JSONObject errorResponse) {
                System.out.println("failed " + errorResponse);
                dialog.dismiss();
            }


        });
    }





}



public class Validate extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_validate);

        final Drawable upArrow = ContextCompat.getDrawable(getApplicationContext(), R.drawable.abc_ic_ab_back_mtrl_am_alpha);
        upArrow.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.primarytext_dark), PorterDuff.Mode.SRC_ATOP);
        getSupportActionBar().setHomeAsUpIndicator(upArrow);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Button bt=(Button) findViewById(R.id.button);
        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
                break;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        finish();
    }


    static final int REQUEST_IMAGE_CAPTURE = 1;

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }




    String encodedImage;

    Bitmap imageBitmap;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            imageBitmap = (Bitmap) extras.get("data");

            ImageView mImageView=(ImageView) findViewById(R.id.imageView);

            Bitmap newBmp = Bitmap.createScaledBitmap(imageBitmap, 92 , 112, false);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            newBmp.compress(Bitmap.CompressFormat.JPEG, 100, baos);


            FaceCropper mFaceCropper = new FaceCropper();
            mFaceCropper.setMaxFaces(1);
            mFaceCropper.setFaceMarginPx(1);
            imageBitmap=mFaceCropper.getCroppedImage(imageBitmap);
            //imageBitmap=mFaceCropper.getFullDebugImage(imageBitmap);
            mImageView.setImageBitmap(imageBitmap);






            //this will convert image to byte[]
            byte[] byteArrayImage = baos.toByteArray();
            // this will convert byte[] to string
            encodedImage = Base64.encodeToString(byteArrayImage, Base64.DEFAULT);


            Button bt=(Button) findViewById(R.id.button);
            bt.setText("Validate");

            bt.setOnClickListener(null);
            bt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AsyncTask task = new Server_call(Validate.this,encodedImage ).execute();
                }
            });


        }
    }




}
