package com.valak.medo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.Objects;

public class Orders extends AppCompatActivity {

    private FirestoreRecyclerAdapter<OrdersModel, ProductViewHolder> adapter;
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    String userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders);

        findViewById(R.id.orders_cart_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Orders.this, Cart.class));
            }
        });

        findViewById(R.id.orders_back_arrow).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        if (user != null) {
            userEmail = user.getEmail();
        }

        RecyclerView recyclerView = findViewById(R.id.order_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        FirebaseFirestore rootRef = FirebaseFirestore.getInstance();
        Query query = rootRef.collection("Users").document(userEmail).collection("orders");

        FirestoreRecyclerOptions<OrdersModel> options = new FirestoreRecyclerOptions.Builder<OrdersModel>()
                .setQuery(query, OrdersModel.class)
                .build();

        adapter = new FirestoreRecyclerAdapter<OrdersModel, ProductViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull ProductViewHolder holder, int position, @NonNull OrdersModel model) {
                holder.order_amount.setText(String.format("Amount: ₹%s", model.getAmount()));
                holder.order_item.setText(String.format("Product: %s", model.getItem()));
                holder.order_packing.setText(String.format("Packing: %s", model.getPacking()));
                holder.order_quantity.setText(String.format("Quantity: %s", model.getQuantity()));
                holder.order_status.setText(model.getStatus());
                holder.order_id.setText(String.format("Order ID: %s", model.getOrder_id()));
                Glide.with(getApplicationContext())
                        .load(model.getImage())
                        .placeholder(R.drawable.prescription_upload)
                        .into(holder.order_image);
            }
            @NonNull
            @Override
            public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.order_items, parent, false);
                return new ProductViewHolder(view);
            }
        };
        recyclerView.setAdapter(adapter);

    }

    private class ProductViewHolder extends RecyclerView.ViewHolder {
        private View view;
        TextView order_id,order_amount,order_item,order_packing,order_quantity,order_status;
        ImageView order_image;

        ProductViewHolder(View itemView) {
            super(itemView);
            view = itemView;

            order_amount = view.findViewById(R.id.order_item_amount);
            order_item = view.findViewById(R.id.order_item_name);
            order_image = view.findViewById(R.id.order_item_image);
            order_packing = view.findViewById(R.id.order_item_packing);
            order_quantity = view.findViewById(R.id.order_item_quantity);
            order_status = view.findViewById(R.id.order_status);
            order_id = view.findViewById(R.id.order_id);
        }
    }
    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (adapter != null) {
            adapter.stopListening();
        }
    }

}