package com.sneaker.shoeapp.Adapter;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.transition.Hold;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.sneaker.shoeapp.Interface.ClickItemCart;
import com.sneaker.shoeapp.MyCartActivity;
import com.sneaker.shoeapp.ProductDetailsActivity;
import com.sneaker.shoeapp.ProfileActivity;
import com.sneaker.shoeapp.R;
import com.sneaker.shoeapp.model.Cart;
import com.sneaker.shoeapp.model.Product;

import java.util.ArrayList;
import java.util.logging.SocketHandler;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder> {


    ArrayList<Cart> productArrayList;
    ClickItemCart clickItemCart;
    Activity context;

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser user = mAuth.getCurrentUser();

    public CartAdapter(Activity context, ArrayList<Cart> productArrayList, ClickItemCart clickItemCart) {
        this.context = context;
        this.productArrayList = productArrayList;
        this.clickItemCart = clickItemCart;
    }

    //    public CartAdapter(@NonNull Activity context, int resource) {
//        super(context, resource);
//        this.context = context;
//        this.resource = resource;
//    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View viewProduct = layoutInflater.inflate(R.layout.item_cart, parent, false);
        return new ViewHolder(viewProduct);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Cart cart = productArrayList.get(position);

        Glide.with(context).load(cart.getImage()).into(holder.proImg_cart);
        holder.proName_cart.setText(cart.getProName());
        int total_cart = (int) ((int) cart.getTotal_cart() * cart.getQuantity());
        holder.proPrice_cart.setText(cart.getTotal_cart() + "");
        Integer quantity = (int) cart.getQuantity();
        holder.viewQuantity.setText(quantity + " ");
        holder.discount.setText("-10%");
        holder.removePro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               clickItemCart.removePro(position,cart);

            }
        });
        holder.increasePro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickItemCart.increasePro(position,cart);
            }
        });
        holder.decreasePro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickItemCart.decreasePro(position,cart);
            }
        });
        GradientDrawable gradientDrawable = new GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                new int[]{ Color.parseColor("#" + cart.getColor()),Color.WHITE});
        holder.item_cart.setBackground(gradientDrawable);
        holder.item_cart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Product product = new Product(cart.getProName(), cart.getPrice(), cart.getCategory(), cart.getImage(), cart.getColor(), 0, cart.getId());
                Bundle bundle = new Bundle();
                bundle.putSerializable("obj_product",product);
                Intent intent = new Intent(context, ProductDetailsActivity.class);
                intent.putExtras(bundle);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return productArrayList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView proName_cart, proPrice_cart, viewQuantity,discount;
        ImageView proImg_cart;
        ImageButton decreasePro, increasePro,removePro;
        LinearLayout item_cart;
        Product product;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            removePro = itemView.findViewById(R.id.removePro);
            proName_cart = itemView.findViewById(R.id.proName_cart);
            proPrice_cart = itemView.findViewById(R.id.proPrice_cart);
            viewQuantity = itemView.findViewById(R.id.viewQuantity);
            proImg_cart = itemView.findViewById(R.id.proImg_cart);
            decreasePro = itemView.findViewById(R.id.decreasePro);
            increasePro = itemView.findViewById(R.id.increasePro);
            item_cart = itemView.findViewById(R.id.item_cart);
            discount = itemView.findViewById(R.id.discount);
        }
    }

    public void setColorBg(GradientDrawable gradientDrawable, Product pro, FrameLayout layout) {
        int colorInt = Color.parseColor("#" + pro.getColor());
        gradientDrawable.setColors(new int[]{0xFFFFFFFF, colorInt});
        layout.setBackground(gradientDrawable);
    }
    public void removeAt(int position) {
        productArrayList.remove(position);
        notifyItemRemoved(position);

    }


//    @NonNull
//    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
//        LayoutInflater layoutInflater = this.context.getLayoutInflater();
//        View viewCustom = layoutInflater.inflate(this.resource, null);
//
//        proName_cart = viewCustom.findViewById(R.id.proName_cart);
//        proPrice_cart = viewCustom.findViewById(R.id.proPrice_cart);
//        proImg_cart = viewCustom.findViewById(R.id.proImg_cart);
//        FrameLayout bg_item_card_custom;
//        increasePro = viewCustom.findViewById(R.id.increasePro);
//        decreasePro = viewCustom.findViewById(R.id.decreasePro);
//        viewQuantity = viewCustom.findViewById(R.id.viewQuantity);
//        Product pro = getItem(position);
//        if (pro == null) {
//            return layoutInflater.inflate(R.layout.layout_cart_null, null);
//        }
//        proName_cart.setText(pro.getProName());
//        proPrice_cart.setText(pro.getPrice() + "");
//        proImg_cart.setImageResource(pro.getImage());

//        setColorBg((GradientDrawable) context.getResources().getDrawable(R.drawable.bg_item_card_custom), product, bg_item_card_custom);
//        increasePro.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                int qty = Integer.parseInt(viewQuantity.getText().toString());
//                qty++;
//                viewQuantity.setText(qty + "");
//            }
//        });
//        decreasePro.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                int qty = Integer.parseInt(viewQuantity.getText().toString());
//                if (qty > 1) {
//                    qty--;
//                    viewQuantity.setText(qty + "");
//                } else {
////                    Toast.makeText(getContext(), "Remove", Toast.LENGTH_SHORT).show();
//                }
//            }
//        });
//        return viewCustom;
//    }


}
