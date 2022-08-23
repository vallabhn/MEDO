package com.valak.medo;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class CartItemAdapter extends RecyclerView.Adapter<CartItemAdapter.CartItemViewHolder> {

    private List Id,Qty,Name,Price,Packing,Image;
    private Context ctx;

    public CartItemAdapter(Context ctx, List Id, List Qty, List Name, List Price, List Packing, List Image){
        this.ctx = ctx;
        this.Id = Id;
        this.Qty = Qty;
        this.Name = Name;
        this.Price = Price;
        this.Packing = Packing;
        this.Image = Image;
    }

    @NonNull
    @Override
    public CartItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view =inflater.inflate(R.layout.cart_items,parent,false);

        return new CartItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartItemViewHolder holder, int position) {
        final String SId = (String) Id.get(position);
        String SQty = (String) Qty.get(position);
        String SName = (String) Name.get(position);
        String SPrice = (String) Price.get(position);
        String SPacking = (String) Packing.get(position);
        String SImage = (String) Image.get(position);
        Glide.with(ctx)
                .load(SImage)
                .placeholder(R.drawable.prescription_upload)
                .into(holder.imageView);
        holder.Quantity.setText(String.format("Quantity: %s", SQty));
        holder.Name.setText(SName);
        holder.Price.setText(String.format("₹%s", SPrice));
        holder.Packing.setText(String.format("for %s", SPacking));

        holder.relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String itemID = SId;
                Intent profileIntent = new Intent(ctx,ItemDescription.class);
                profileIntent.putExtra("itemID",SId);
                profileIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                ctx.startActivity(profileIntent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return Id.size();
    }

    public class CartItemViewHolder extends RecyclerView.ViewHolder{

        TextView Name,Price,Packing,Quantity;
        ImageView imageView;
        RelativeLayout relativeLayout;

        public CartItemViewHolder(@NonNull View itemView) {
            super(itemView);
            Name = itemView.findViewById(R.id.cart_item_text);
            Price = itemView.findViewById(R.id.cart_item_price);
            Packing = itemView.findViewById(R.id.cart_item_packing);
            imageView = itemView.findViewById(R.id.cart_item_image);
            Quantity = itemView.findViewById(R.id.cart_item_quantity);

            relativeLayout = (RelativeLayout) itemView.findViewById(R.id.cart_items);

        }
    }
}
