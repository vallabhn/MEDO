package com.valak.medo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.IOException;

public class Search extends AppCompatActivity {

    private EditText mSearchField;
    private ImageButton mSearchBtn;
    private RecyclerView mResultList;
    private DatabaseReference mUserDatabase;
    InputMethodManager imm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);


        findViewById(R.id.upload_prescription).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Search.this, PrescriptionUpload.class));
            }
        });

        mSearchField = (EditText) findViewById(R.id.search_field);
        mSearchField.requestFocus();

        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(mSearchField, InputMethodManager.SHOW_IMPLICIT);

        mUserDatabase = FirebaseDatabase.getInstance().getReference("Items");


        mSearchField = (EditText) findViewById(R.id.search_field);
        mSearchBtn = (ImageButton) findViewById(R.id.search_btn);

        mResultList = (RecyclerView) findViewById(R.id.result_list);
        mResultList.setHasFixedSize(true);
        mResultList.setLayoutManager(new LinearLayoutManager(this));

        mSearchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String searchText = mSearchField.getText().toString();
                firebaseUserSearch(searchText);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);


            }
        });

    }

    private void firebaseUserSearch(String searchText) {



        Query firebaseSearchQuery = mUserDatabase.orderByChild("search").startAt(searchText.toLowerCase()).endAt(searchText.toLowerCase() + "uf8ff");

        FirebaseRecyclerAdapter<Items, UsersViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Items, UsersViewHolder>(

                Items.class,
                R.layout.list_layout,
                UsersViewHolder.class,
                firebaseSearchQuery

        ) {
            @Override
            protected void populateViewHolder(UsersViewHolder viewHolder, final Items model, final int position) {

                viewHolder.setDetails(getApplicationContext(), model.getName(), model.getPrice(), model.getImage(), model.getPacking());

                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String itemID = getRef(position).getKey();
                        Intent profileIntent = new Intent(Search.this,ItemDescription.class);
                        profileIntent.putExtra("itemID",itemID);
                        startActivity(profileIntent);
                    }
                });

            }
        };

        mResultList.setAdapter(firebaseRecyclerAdapter);

    }


    // View Holder Class
    public static class UsersViewHolder extends RecyclerView.ViewHolder {

        View mView;


        public UsersViewHolder(View itemView) {
            super(itemView);

            mView = itemView;

        }

        public void setDetails(Context ctx, String itemName, long itemPrice, String itemImage, String itemPacking){

            CircularProgressDrawable circularProgressDrawable;
            circularProgressDrawable = new CircularProgressDrawable(ctx);
            circularProgressDrawable.setStrokeWidth(5f);
            circularProgressDrawable.start();

            TextView item_name = (TextView) mView.findViewById(R.id.name_text);
            TextView item_price = (TextView) mView.findViewById(R.id.price_text);
            TextView item_packing = (TextView) mView.findViewById(R.id.packing_text);
            ImageView item_image = (ImageView) mView.findViewById(R.id.profile_image);


            item_name.setText(itemName);
            item_packing.setText(String.format("for %s", itemPacking));
            item_price.setText(String.format("₹%s", itemPrice));
            Glide.with(ctx).load(itemImage).placeholder(circularProgressDrawable).into(item_image);

        }
    }
}
