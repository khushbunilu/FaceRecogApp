package app.khushbu.facerecognition;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;

import cz.msebera.android.httpclient.Header;

public class Register extends AppCompatActivity {

    int i;
    String encodedImage;

    String ImgName1;

    static final int REQUEST_IMAGE_CAPTURE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        TextView tv=(TextView)findViewById(R.id.imagetextView);
        i=1;
        tv.setText("Image: 1");

        final Drawable upArrow = ContextCompat.getDrawable(getApplicationContext(), R.drawable.abc_ic_ab_back_mtrl_am_alpha);
        upArrow.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.primarytext_dark), PorterDuff.Mode.SRC_ATOP);
        getSupportActionBar().setHomeAsUpIndicator(upArrow);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent i=getIntent();

        ImgName1=i.getStringExtra("name");

        Button bt=(Button) findViewById(R.id.button5);
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


    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }






    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");

            ImageView mImageView=(ImageView) findViewById(R.id.imageView1);

            System.out.println(imageBitmap.getHeight());
            System.out.println(imageBitmap.getWidth());

            Bitmap newBmp = Bitmap.createScaledBitmap(imageBitmap, 92, 112, false);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            newBmp.compress(Bitmap.CompressFormat.JPEG, 100, baos);


            /*FaceCropper mFaceCropper = new FaceCropper();
            mFaceCropper.setMaxFaces(1);
            mFaceCropper.setFaceMarginPx(5);
            mFaceCropper.setDebug(true);
            imageBitmap=mFaceCropper.getFullDebugImage(imageBitmap);*/
            mImageView.setImageBitmap(imageBitmap);




            //this will convert image to byte[]
            byte[] byteArrayImage = baos.toByteArray();
            // this will convert byte[] to string
            encodedImage = Base64.encodeToString(byteArrayImage, Base64.DEFAULT);

            Button bt1=(Button) findViewById(R.id.button4);
            bt1.setVisibility(View.VISIBLE);

            bt1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dispatchTakePictureIntent();
                }
            });
            Button bt = (Button) findViewById(R.id.button5);
            if(i!=5) {

                bt.setText("Next");
            }
            else
            {
                bt.setText("Done");
            }
            bt.setOnClickListener(null);
            bt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SendImageToServer();
                }
            });


        }
    }

    void SuccessToastMessage(){
        Toast.makeText(this, "Registration Successfull", Toast.LENGTH_LONG).show();
        Intent i=new Intent(getApplicationContext(),MainActivity.class);
        startActivity(i);
        finish();
    }

    void FailureToastMessage(){
        Toast.makeText(this, "Some error occurred...Please try again", Toast.LENGTH_LONG).show();
    }

    void resetButtons(){
        ImageView mImageView=(ImageView) findViewById(R.id.imageView1);
        mImageView.setImageDrawable(null);

        Button bt1=(Button) findViewById(R.id.button4);
        bt1.setVisibility(View.INVISIBLE);

        Button bt=(Button) findViewById(R.id.button5);
        bt.setText("Take \n Image");

        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });


    }


    void SendImageToServer(){


        RequestParams rp = new RequestParams();
        rp.add("Image", encodedImage);
        rp.add("name", ImgName1);


        HttpUtils.post("imageclass/AddToTrainingSet", rp, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

                try {
                    System.out.println("Status is +"+response.get("status"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if(i==5)
                {
                    SuccessToastMessage();
                }




                TextView tv=(TextView)findViewById(R.id.imagetextView);
                i++;
                tv.setText("Image: "+i);
                resetButtons();

            }

            @Override
            public void onFailure(int statusCode,
                                  cz.msebera.android.httpclient.Header[] headers,
                                  java.lang.Throwable throwable,
                                  org.json.JSONObject errorResponse) {
                System.out.println("failed " + errorResponse);
                FailureToastMessage();
            }


        });
    }






}
