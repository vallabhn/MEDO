package com.valak.medo;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.Source;
import com.razorpay.Checkout;
import com.razorpay.PaymentResultListener;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


public class ItemDescription extends AppCompatActivity implements PaymentResultListener {

    private static final String TAG = "Firestore";
    DatabaseReference mUserDatabase;
    FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    DocumentReference userProfileDB;
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    CollectionReference Ofirestoredb;
    String userEmail, receivedItemID, itemName, itemPacking, itemImage, itemDescription, smsTxt, smsNo;
    ImageView imageView;
    long itemPrice;
    TextView item_name, item_price, item_packing, item_description;
    ProgressDialog progressDialog;
    ConnectivityManager connectivityManager;
    NetworkInfo networkInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_description);

        Checkout.preload(getApplicationContext());

        receivedItemID = getIntent().getExtras().getString("itemID");
        mUserDatabase = FirebaseDatabase.getInstance().getReference("Items").child(receivedItemID);

        if (user != null) {
            userEmail = user.getEmail();
        }

        userProfileDB = firestore.collection("Users").document(userEmail);

        Source source = Source.DEFAULT;

        userProfileDB.get(source).addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                DocumentSnapshot document = task.getResult();
                assert document != null;
                smsNo = document.get("phone").toString();
            }
        });

        final DocumentReference firestoredb = firestore.collection("Users").document(userEmail).collection("cart").document("cartItems");
        Ofirestoredb = firestore.collection("Users").document(userEmail).collection("orders");
        findViewById(R.id.item_description_back_arrow).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        connectivityManager =(ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        networkInfo = connectivityManager.getActiveNetworkInfo();
        if(networkInfo!=null && networkInfo.isConnected())
        {

        }
        else
        {
            AlertDialog alertDialog = new AlertDialog.Builder(this).create();
            alertDialog.setTitle("No Internet");
            alertDialog.setMessage("Please connect to internet and try again!");
            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Try Again", new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int id) {
                    recreate();
                } });
            alertDialog.show();
        }

        findViewById(R.id.item_description_cart_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ItemDescription.this, Cart.class));
            }
        });


        imageView = findViewById(R.id.item_image);
        item_name = findViewById(R.id.item_name);
        item_price = findViewById(R.id.item_price);
        item_packing = findViewById(R.id.item_packing);
        item_description = findViewById(R.id.item_description);

        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                itemImage = dataSnapshot.child("image").getValue().toString();
                itemPrice = (long) dataSnapshot.child("price").getValue();
                Glide.with(getApplicationContext())
                        .load(itemImage)
                        .placeholder(R.drawable.prescription_upload)
                        .into(imageView);
                itemName = dataSnapshot.child("name").getValue().toString();
                itemPacking = dataSnapshot.child("packing").getValue().toString();
                itemDescription = dataSnapshot.child("description").getValue().toString();
                item_name.setText(itemName);
                item_price.setText(String.format("₹ %s", dataSnapshot.child("price").getValue().toString()));
                item_packing.setText(String.format("for %s", itemPacking));
                item_description.setText(itemDescription);
                smsTxt = "Placed: Order for " + itemName +" ("+ itemPacking  + ") is placed and will be delivered to you soon. Thank you for using Medo.";
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(ItemDescription.this,"Error: "+ databaseError, Toast.LENGTH_SHORT).show();
            }
        });


        findViewById(R.id.add_to_cart).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                networkInfo = connectivityManager.getActiveNetworkInfo();
                if(networkInfo!=null && networkInfo.isConnected())
                {
                    progressDialog = new ProgressDialog(ItemDescription.this);
                    progressDialog.setCancelable(false);
                    progressDialog.setMessage("Adding item to cart...");
                    progressDialog.show();

                    // Create a new user with a first, middle, and last name
                    Map<String, Object> item = new HashMap<>();
                    item.put(receivedItemID, FieldValue.increment(1));

                    // Add a new document with a generated ID
                        firestoredb.set(item, SetOptions.merge())
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(ItemDescription.this,"Successfully added to cart.", Toast.LENGTH_SHORT).show();
                                        progressDialog.dismiss();

                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(ItemDescription.this,"Error :"+e, Toast.LENGTH_SHORT).show();
                                        progressDialog.dismiss();
                                    }
                                });
                }
                else
                {
                    AlertDialog alertDialog = new AlertDialog.Builder(ItemDescription.this).create();
                    alertDialog.setTitle("No Internet");
                    alertDialog.setMessage("Please connect to internet and try again!");
                    alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Try Again", new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int id) {

                            recreate();

                        } });

                    alertDialog.show();

                }
            }
        });


        findViewById(R.id.paybtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                networkInfo = connectivityManager.getActiveNetworkInfo();
                if(networkInfo!=null && networkInfo.isConnected())
                {
                    startPayment(itemPrice);
                }
                else
                {
                    AlertDialog alertDialog = new AlertDialog.Builder(ItemDescription.this).create();
                    alertDialog.setTitle("No Internet");
                    alertDialog.setMessage("Please connect to internet and try again!");
                    alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Try Again", new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int id) {

                            recreate();

                        } });

                    alertDialog.show();

                }
            }
        });

    }

    public void startPayment(double price) {
        /*
          You need to pass current activity in order to let Razorpay create CheckoutActivity
         */

        //int amount = Integer.parseInt(price);
        double final_amount = price*100;
        String description = itemName + itemPacking;

        final Activity activity = this;

        final Checkout co = new Checkout();

        co.setImage(R.drawable.medo_logo);

        try {
            JSONObject options = new JSONObject();
            options.put("name", "Medo Pvt. Ltd.");
            options.put("description", description);
            //You can omit the image option to fetch the image from dashboard
            options.put("image", "https://s3.amazonaws.com/rzp-mobile/images/rzp.png");
            options.put("currency", "INR");
            options.put("amount", final_amount);

            /*JSONObject preFill = new JSONObject();
            preFill.put("email", "test@razorpay.com");
            preFill.put("contact", "9876543210");

            options.put("prefill", preFill);*/

            co.open(activity, options);
        } catch (Exception e) {
            Toast.makeText(activity, "Error in payment: " + e.getMessage(), Toast.LENGTH_SHORT)
                    .show();
            e.printStackTrace();
        }
    }

    @Override
    public void onPaymentSuccess(String razorpayPaymentID) {

        try {
            Toast.makeText(this, "Payment Successful: " + razorpayPaymentID, Toast.LENGTH_SHORT).show();

            DocumentReference OrderRef = Ofirestoredb.document(razorpayPaymentID);
            Map<String, String> item = new HashMap<>();
            item.put("order_id",razorpayPaymentID);
            item.put("item",itemName );
            item.put("packing", itemPacking);
            item.put("quantity", "1");
            item.put("amount", String.valueOf(itemPrice));
            item.put("image", itemImage);
            item.put("status", "Order Placed");


            // Add a new document with a generated ID
            OrderRef.set(item)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(ItemDescription.this,"Payment Successful.", Toast.LENGTH_SHORT).show();
                            new SMSTask().execute();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(ItemDescription.this,"Error :"+e, Toast.LENGTH_SHORT).show();
                        }
                    });


                    AlertDialog alertDialog = new AlertDialog.Builder(ItemDescription.this).create();
                    alertDialog.setTitle("Payment Successful !");
                    alertDialog.setMessage("Your order has been placed successfully. Grab more items with just few clicks.");
                    alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Shop More", new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int id) {

                            startActivity(new Intent(ItemDescription.this, HomeActivity.class));

                        } });

                    alertDialog.show();


        } catch (Exception e) {
            Log.e(TAG, "Exception in onPaymentSuccess", e);
        }

    }

    @Override
    public void onPaymentError(int i, String response) {
        try {
            Toast.makeText(this, "Payment failed: " + i + " " + response, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, "Exception in onPaymentError", e);
        }
    }

    private class SMSTask extends AsyncTask<Void, Void, Void> {
        String result;
        @Override
        protected Void doInBackground(Void... voids) {
            try {
                OkHttpClient client = new OkHttpClient();

                MediaType mediaType = MediaType.parse("application/json");
                RequestBody body = RequestBody.create(mediaType, "{\r\"content\": \""+smsTxt+"\",\r\"from\": \"D7-Rapid\", \r \"to\": "+"91"+smsNo+"\r }");

                Request request = new Request.Builder()
                        .url("https://d7sms.p.rapidapi.com/secure/send")
                        .post(body)
                        .addHeader("content-type", "application/json")
                        .addHeader("authorization", "Basic eXh5Yjk3ODQ6Qmp3YUNUalo=")
                        .addHeader("x-rapidapi-key", "c6f851075bmshca2f2a62275eca6p1eabd8jsn7bedd08b8236")
                        .addHeader("x-rapidapi-host", "d7sms.p.rapidapi.com")
                        .build();

                Response response = client.newCall(request).execute();
                System.out.println(response.networkResponse().toString());

            } catch (IOException e){
                e.printStackTrace();
                result = e.toString();
                System.out.println(result);
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void aVoid) {
            Toast.makeText(ItemDescription.this, "Order Placed.", Toast.LENGTH_SHORT).show();
        }
    }
}