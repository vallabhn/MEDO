package com.valak.medo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Source;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class Cart extends AppCompatActivity {

    FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    DatabaseReference mUserDatabase;
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    String userEmail, a;
    public static String imageurl, itemName, itemPacking, itemQuantity;
    public long itemPrice;
    Map<String, String> newMap = new HashMap<String, String>();
    ProgressDialog progressDialog;
    DocumentReference firestoredb;

    public static List<String> ids, qty, name, price, packing, image;
    public static int total_amount;
    TextView amount;
    RecyclerView mCartList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        total_amount = 0;

        ids = new ArrayList<>();
        qty = new ArrayList<>();
        name = new ArrayList<>();
        price = new ArrayList<>();
        packing = new ArrayList<>();
        image = new ArrayList<>();
        amount = findViewById(R.id.total_amount);

        progressDialog = new ProgressDialog(Cart.this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Loading Cart items...");
        progressDialog.show();

        mCartList = (RecyclerView) findViewById(R.id.cart_recycler_view);
        mCartList.setHasFixedSize(true);
        mCartList.setLayoutManager(new LinearLayoutManager(Cart.this));

        if (user != null) {
            userEmail = user.getEmail();
            boolean emailVerified = user.isEmailVerified();
            String uid = user.getUid();
        }

        findViewById(R.id.cart_back_arrow).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Cart.this, HomeActivity.class));
            }
        });

        findViewById(R.id.place_order).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent order_summary = new Intent(Cart.this, OrderSummary.class);
                startActivity(order_summary);
            }
        });

        firestoredb = firestore.collection("Users").document(userEmail).collection("cart").document("cartItems");
        mUserDatabase = FirebaseDatabase.getInstance().getReference("Items");

        // Source can be CACHE, SERVER, or DEFAULT.
        Source source = Source.DEFAULT;

        // Get the document, forcing the SDK to use the offline cache
        firestoredb.get(source).addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            private static final String TAG = "VUK";

            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    // Document found in the offline cache
                    DocumentSnapshot document = task.getResult();
                    Set<String> key;
                    key = null;
                    if (document.getData()!= null)
                    {key = document.getData().keySet();
                    // Creating an iterator
                    assert key != null;
                    // Displaying the values after iterating through the iterator
                    for (String s : key) {
                        a = s;
                        itemQuantity = Objects.requireNonNull(document.get(a)).toString();
                        qty.add(itemQuantity);
                        ids.add(a);
                    }
                    }
                } else {
                    Log.d(TAG, "Cached get failed: ", task.getException());
                }

            }

        }).addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (!(ids.isEmpty())){
                    setValues();
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            // Do something after 5s = 5000ms
                            progressDialog.dismiss();
                            if (!(ids.isEmpty() && qty.isEmpty() && name.isEmpty() && price.isEmpty() && packing.isEmpty() && image.isEmpty()))
                            {mCartList.setAdapter(new CartItemAdapter(getApplicationContext(),ids, qty, name, price, packing, image));
                                amount.setText(String.format("₹%s", total_amount));}
                            else {
                                AlertDialog alertDialog = new AlertDialog.Builder(Cart.this).create();
                                alertDialog.setMessage("Failed to load Cart.");
                                alertDialog.setCancelable(false);
                                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Try Again", new DialogInterface.OnClickListener() {

                                    public void onClick(DialogInterface dialog, int id) {
                                        recreate();
                                    } });
                                alertDialog.show();
                            }
                        }
                    }, 5000);}
                else{
                    AlertDialog alertDialog = new AlertDialog.Builder(Cart.this).create();
                    alertDialog.setMessage("Cart is Empty");
                    alertDialog.setCancelable(false);
                    alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Add items to cart", new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int id) {
                            startActivity(new Intent(Cart.this, HomeActivity.class));
                        } });
                    alertDialog.show();
                }
            }
        });


    findViewById(R.id.clear_cart).setOnClickListener(new View.OnClickListener() {

        @Override
        public void onClick(View view) {

            final AlertDialog alertDialog = new AlertDialog.Builder(Cart.this).create();
            alertDialog.setCancelable(false);
            alertDialog.setMessage("Are you sure want to clear cart? \uD83D\uDE14");
            alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Yes", new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int id) {

                    progressDialog.setCancelable(false);
                    progressDialog.setMessage("Clearing Cart items...");
                    progressDialog.show();

                    firestoredb.delete()
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    progressDialog.dismiss();
                                    AlertDialog alertDialog = new AlertDialog.Builder(Cart.this).create();
                                    alertDialog.setCancelable(false);
                                    alertDialog.setTitle("Cart Cleared \uD83D\uDE14 ");
                                    alertDialog.setMessage("Grab more items with just few clicks.");
                                    alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Shop More", new DialogInterface.OnClickListener() {

                                        public void onClick(DialogInterface dialog, int id) {

                                            startActivity(new Intent(Cart.this, HomeActivity.class));

                                        } });

                                    alertDialog.show();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    progressDialog.dismiss();
                                    Toast.makeText(Cart.this, e.toString(), Toast.LENGTH_SHORT).show();
                                }
                            });

                } });
            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    alertDialog.dismiss();
                }
            });
            alertDialog.show();

        }
    });}

    private void setValues() {
        int i;
        for (i = 0; i < ids.size(); i++) {

            final int finalI = i;
            mUserDatabase.child(ids.get(i)).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String imageurl = dataSnapshot.child("image").getValue().toString();
                    itemPrice = (long) dataSnapshot.child("price").getValue();
                    itemName = dataSnapshot.child("name").getValue().toString();
                    itemPacking = dataSnapshot.child("packing").getValue().toString();
                    name.add(itemName);
                    packing.add(itemPacking);
                    price.add(String.valueOf(itemPrice));
                    image.add(imageurl);
                    total_amount = (int) (total_amount + (itemPrice*Integer.parseInt(qty.get(finalI))));
                    System.out.println("The iterator values are: " + ids + qty + name + price + packing + image);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(Cart.this,"Error: "+ databaseError, Toast.LENGTH_SHORT).show();
                }
            });

        }

    }

    @Override
    public void onBackPressed() {
        ids = new ArrayList<>();
        qty = new ArrayList<>();
        name = new ArrayList<>();
        price = new ArrayList<>();
        packing = new ArrayList<>();
        image = new ArrayList<>();
        total_amount = 0;
        startActivity(new Intent(Cart.this, HomeActivity.class));
    }
}



