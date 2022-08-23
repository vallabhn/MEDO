package com.valak.medo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;

import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    boolean doubleBackToExitPressedOnce = false;
    private DatabaseReference mUserDatabase;
    ImageButton imageButton1,imageButton2,imageButton3,imageButton4,imageButton5,imageButton6,imageButton7,imageButton8,imageButton9;
    ArrayList<String> itemIDs;
    ImageButton[] imageViews;
    ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        progressDialog = new ProgressDialog(HomeActivity.this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Loading...");
        progressDialog.show();

        imageButton1 = findViewById(R.id.imageButton1);
        imageButton2 = findViewById(R.id.imageButton2);
        imageButton3 = findViewById(R.id.imageButton3);
        imageButton4 = findViewById(R.id.imageButton4);
        imageButton5 = findViewById(R.id.imageButton5);
        imageButton6 = findViewById(R.id.imageButton6);
        imageButton7 = findViewById(R.id.imageButton7);
        imageButton8 = findViewById(R.id.imageButton8);
        imageButton9 = findViewById(R.id.imageButton9);

        itemIDs = new ArrayList<>();
        imageViews = new ImageButton[]{imageButton1,imageButton2,imageButton3,imageButton4,imageButton5,imageButton6,imageButton7,imageButton8,imageButton9};

        findViewById(R.id.covid_banner).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String urlString = "https://www.1mg.com/survey/SVC5YE1608371969?source=homePage";
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(urlString));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setPackage("com.android.chrome");
                try {
                    startActivity(intent);
                } catch (ActivityNotFoundException ex) {
                    // Chrome browser presumably not installed so allow user to choose instead
                    intent.setPackage(null);
                    startActivity(intent);
                }
            }
        });

        findViewById(R.id.home_cart_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(HomeActivity.this, Cart.class));
            }
        });
        findViewById(R.id.searchBar).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(HomeActivity.this,Search.class));
            }
        });

        findViewById(R.id.homebtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(HomeActivity.this,HomeActivity.class));
            }
        });


        findViewById(R.id.profilebtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(HomeActivity.this,Profile.class));
            }
        });

        findViewById(R.id.cartbtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(HomeActivity.this,Cart.class));
            }
        });

        findViewById(R.id.upload_prescription).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(HomeActivity.this,PrescriptionUpload.class));
            }
        });


        mUserDatabase = FirebaseDatabase.getInstance().getReference("HomeScreen");

        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Iterator<DataSnapshot> items = snapshot.getChildren().iterator();
                snapshot.getChildrenCount();
                while (items.hasNext())
                {
                    DataSnapshot item = items.next();
                    String itemID;
                    itemID = item.getValue().toString();
                    itemIDs.add(itemID);
                }
                System.out.println(itemIDs);
                putValue(itemIDs,imageViews);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        findViewById(R.id.imageButton1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeActivity.this,ItemDescription.class);
                intent.putExtra("itemID", itemIDs.get(0));
                startActivity(intent);
            }
        });

        findViewById(R.id.imageButton2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeActivity.this,ItemDescription.class);
                intent.putExtra("itemID", itemIDs.get(1));
                startActivity(intent);
            }
        });


        findViewById(R.id.imageButton3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeActivity.this,ItemDescription.class);
                intent.putExtra("itemID", itemIDs.get(2));
                startActivity(intent);
            }
        });

        findViewById(R.id.imageButton4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeActivity.this,ItemDescription.class);
                intent.putExtra("itemID", itemIDs.get(3));
                startActivity(intent);
            }
        });

        findViewById(R.id.imageButton5).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeActivity.this,ItemDescription.class);
                intent.putExtra("itemID", itemIDs.get(4));
                startActivity(intent);
            }
        });


        findViewById(R.id.imageButton6).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeActivity.this,ItemDescription.class);
                intent.putExtra("itemID", itemIDs.get(5));
                startActivity(intent);
            }
        });

        findViewById(R.id.imageButton7).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeActivity.this,ItemDescription.class);
                intent.putExtra("itemID", itemIDs.get(6));
                startActivity(intent);
            }
        });

        findViewById(R.id.imageButton8).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeActivity.this,ItemDescription.class);
                intent.putExtra("itemID", itemIDs.get(7));
                startActivity(intent);
            }
        });


        findViewById(R.id.imageButton9).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeActivity.this,ItemDescription.class);
                intent.putExtra("itemID", itemIDs.get(8));
                startActivity(intent);
            }
        });

    }

    private void putValue(ArrayList<String> itemIDs, final ImageButton[] textViewList) {

        int i;
        for (i=0; i<itemIDs.size(); i++)
        {
            mUserDatabase = FirebaseDatabase.getInstance().getReference("Items").child(itemIDs.get(i));
            final int finalI = i;
            mUserDatabase.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    CircularProgressDrawable circularProgressDrawable;
                    circularProgressDrawable = new CircularProgressDrawable(HomeActivity.this);
                    circularProgressDrawable.setStrokeWidth(5f);
                    circularProgressDrawable.start();
                    String itemName;
                    itemName = snapshot.child("image").getValue().toString();
                    Glide.with(HomeActivity.this).load(itemName).placeholder(circularProgressDrawable).into(imageViews[finalI]);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

        progressDialog.dismiss();
    }


    @Override
    public void onBackPressed() {

        if (doubleBackToExitPressedOnce) {
            finishAffinity();
            System.exit(0);
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;
            }
        }, 2000);

    }
}