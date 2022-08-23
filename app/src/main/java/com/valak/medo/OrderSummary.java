package com.valak.medo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
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
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Source;
import com.razorpay.Checkout;
import com.razorpay.PaymentResultListener;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

public class OrderSummary extends AppCompatActivity implements PaymentResultListener {

    private static final String TAG = "signout";
    FirebaseAuth.AuthStateListener mAuthStateListener;
    final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    TextView userName, userPhone, userAddress, amount;
    String userEmail,smsNo;
    ProgressDialog progressDialog;
    RecyclerView mOrderSummary;
    int final_amount;
    DocumentReference firestoredb;
    CollectionReference Ofirestoredb;
    final FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    DocumentReference userProfileDB;
    AlertDialog alertDialog;
    RadioGroup radioGroup;
    RadioButton radioButton;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_summary);

        final_amount = Cart.total_amount;
        alertDialog = new AlertDialog.Builder(OrderSummary.this).create();

        ConnectivityManager connectivityManager =(ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        progressDialog = new ProgressDialog(OrderSummary.this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Loading...");
        progressDialog.show();

        setupFirebaseListener();

        userName = findViewById(R.id.order_summary_userName);
        userPhone = findViewById(R.id.order_summary_userMobile);
        userAddress = findViewById(R.id.order_summary_userAddress);
        amount = findViewById(R.id.total_amount);
        radioGroup = findViewById(R.id.radioGroup);

        if (user != null) {
            userEmail = user.getEmail();
        }

        getUserProfile();

        firestoredb = firestore.collection("Users").document(userEmail).collection("cart").document("cartItems");
        Ofirestoredb = firestore.collection("Users").document(userEmail).collection("orders");

        mOrderSummary = (RecyclerView) findViewById(R.id.cart_recycler_view);
        mOrderSummary.setHasFixedSize(true);
        mOrderSummary.setLayoutManager(new LinearLayoutManager(OrderSummary.this));

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // Do something after 5s = 5000ms
                progressDialog.dismiss();
                mOrderSummary.setAdapter(new CartItemAdapter(getApplicationContext(),Cart.ids, Cart.qty, Cart.name, Cart.price, Cart.packing, Cart.image));
                amount.setText(String.format("₹%s", final_amount));
            }
        }, 2000);

        findViewById(R.id.order_summary_back_arrow).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        findViewById(R.id.updateInfo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(OrderSummary.this, UpdateProfile.class);
                intent.putExtra("key","os");
                startActivity(intent);
            }
        });

        findViewById(R.id.order_continue).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(networkInfo!=null && networkInfo.isConnected())
                {
                    progressDialog.show();
                    int selectedId = radioGroup.getCheckedRadioButtonId();
                    radioButton = findViewById(selectedId);
                    if (selectedId == R.id.onlinePayment){
                        startPayment(final_amount);
                    }
                    else if (selectedId == R.id.COD){
                        final AlertDialog alertDialog = new AlertDialog.Builder(OrderSummary.this).create();
                        alertDialog.setTitle("Order Confirmation");
                        alertDialog.setMessage("Are you sure want to confirm your order?\nOrder Value: ₹" + final_amount + "\nPayment Method: Cash on Delivery");
                        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "YES", new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int id) {
                                try {

                                    Random rand = new Random();
                                    int int_order_id = rand.nextInt(100000);
                                    String order_id = String.valueOf(int_order_id);

                                    DocumentReference OrderRef = Ofirestoredb.document(order_id);
                                    Map<String, String> item = new HashMap<>();
                                    item.put("order_id",order_id);
                                    item.put("item",Cart.name.toString());
                                    item.put("packing", Cart.packing.toString());
                                    item.put("quantity", Cart.qty.toString());
                                    item.put("amount", String.valueOf(final_amount));
                                    item.put("status", "Order Placed");


                                    // Add a new document with a generated ID
                                    OrderRef.set(item)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    Toast.makeText(OrderSummary.this,"Order Placed", Toast.LENGTH_SHORT).show();
                                                    new SMSTask().execute();
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Toast.makeText(OrderSummary.this,"Error :"+e, Toast.LENGTH_SHORT).show();
                                                }
                                            });

                                    firestoredb.delete()
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    Log.d(TAG, "DocumentSnapshot successfully deleted!");
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Log.w(TAG, "Error deleting document", e);
                                                }
                                            });
                                    progressDialog.dismiss();
                                    AlertDialog alertDialog = new AlertDialog.Builder(OrderSummary.this).create();
                                    alertDialog.setTitle("Order Placed! \uD83D\uDE03");
                                    alertDialog.setMessage("Your order has been placed successfully. Grab more items with just few clicks.");
                                    alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Shop More", new DialogInterface.OnClickListener() {

                                        public void onClick(DialogInterface dialog, int id) {

                                            startActivity(new Intent(OrderSummary.this, HomeActivity.class));

                                        } });

                                    alertDialog.show();



                                } catch (Exception e) {
                                    Log.e(TAG, "Exception in onPaymentSuccess", e);
                                }
                            } });
                        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "NO", new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int id) {

                                alertDialog.dismiss();
                                progressDialog.dismiss();

                            } });

                        alertDialog.show();
                    }
                    else {
                        progressDialog.dismiss();
                        final AlertDialog alertDialog = new AlertDialog.Builder(OrderSummary.this).create();
                        alertDialog.setMessage("Please select payment method");
                        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Okay", new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int id) {

                                alertDialog.dismiss();

                            } });

                        alertDialog.show();
                    }

                }
                else
                {
                    AlertDialog alertDialog = new AlertDialog.Builder(OrderSummary.this).create();
                    alertDialog.setCancelable(false);
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

    private void getUserProfile() {
        userProfileDB = firestore.collection("Users").document(userEmail);

        Source source = Source.DEFAULT;

        userProfileDB.get(source).addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                DocumentSnapshot document = task.getResult();
                assert document != null;
                if (document.get("address") == null){
                    AlertDialog alertDialog = new AlertDialog.Builder(OrderSummary.this).create();
                    alertDialog.setMessage("Please Add address to continue.");
                    alertDialog.setCancelable(false);
                    alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Add Address", new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int id) {
                            startActivity(new Intent(OrderSummary.this, UpdateProfile.class));
                        } });
                    alertDialog.show();
                }
                else {
                    smsNo = document.get("phone").toString();
                    String Address = document.get("address") + ", " + document.get("city") + ", " + document.get("state") + " - " + document.get("pincode");
                    userName.setText(Objects.requireNonNull(document.get("name")).toString());
                    userPhone.setText("+91" + Objects.requireNonNull(document.get("phone")).toString());
                    userAddress.setText(Address);
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        alertDialog.setMessage("Are you really want to go back?");
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Yes", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int id) {
                startActivity(new Intent(OrderSummary.this,Cart.class));
            } });
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "No", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int id) {
                alertDialog.dismiss();
            } });
        alertDialog.show();
    }

    public void startPayment(int price) {
        /*
          You need to pass current activity in order to let Razorpay create CheckoutActivity
         */

        //int amount = Integer.parseInt(price);
        int final_amount = price*100;
        String description = "OrderID #M101";

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
            item.put("item",Cart.name.toString());
            item.put("packing", Cart.packing.toString());
            item.put("quantity", Cart.qty.toString());
            item.put("amount", String.valueOf(final_amount));
            item.put("status", "Order Placed");


            // Add a new document with a generated ID
            OrderRef.set(item)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(OrderSummary.this,"Payment Successful.", Toast.LENGTH_SHORT).show();
                            new SMSTask().execute();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(OrderSummary.this,"Error :"+e, Toast.LENGTH_SHORT).show();
                        }
                    });

            firestoredb.delete()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, "DocumentSnapshot successfully deleted!");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w(TAG, "Error deleting document", e);
                        }
                    });

                    AlertDialog alertDialog = new AlertDialog.Builder(OrderSummary.this).create();
                    alertDialog.setTitle("Payment Successful ! \uD83D\uDE03");
                    alertDialog.setMessage("Your order has been placed successfully. Grab more items with just few clicks.");
                    alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Shop More", new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int id) {

                            startActivity(new Intent(OrderSummary.this, HomeActivity.class));

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
        String smsTxt = "Placed: Order for " + Cart.name +" is placed and will be delivered to you soon. Thank you for using Medo.";
        @Override
        protected Void doInBackground(Void... voids) {
            try {
                OkHttpClient client = new OkHttpClient();
                String content = "{\r \"content\": \""+smsTxt+"\",\r \"from\": \"Medico\",\r \"to\": 918975129325\r }";

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
            Toast.makeText(OrderSummary.this, "Order Placed.", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupFirebaseListener(){
        Log.d(TAG, "setupFirebaseListener: setting up the auth state listener.");
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if(user != null){
                    Log.d(TAG, "onAuthStateChanged: signed_in: " + user.getEmail());
                }else{
                    Log.d(TAG, "onAuthStateChanged: signed_out");
                    Toast.makeText(OrderSummary.this, "Signed out", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(OrderSummary.this, LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }
            }
        };
    }
}