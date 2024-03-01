package com.sneaker.shoeapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.sneaker.shoeapp.Adapter.CartAdapter;
import com.sneaker.shoeapp.Interface.ClickItemCart;
import com.sneaker.shoeapp.model.Cart;
import com.sneaker.shoeapp.model.ListProduct;

import java.util.ArrayList;
import java.util.List;

public class MyCartActivity extends AppCompatActivity {
    RecyclerView recyclerMyCart;
    CartAdapter cartAdapter;
    ArrayList<Cart> productArrayList;
    ImageButton decreasePro, increasePro;
    ListProduct productList;
    ImageButton btnBack;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser user = mAuth.getCurrentUser();

    //    ListView listItem_cart;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_cart);
        Toolbar main_header = findViewById(R.id.menu_header_back);
        setSupportActionBar(main_header);
        if (user == null){
            Intent intent = new Intent(MyCartActivity.this, LoginActivity.class);
            startActivity(intent);
        }
        addControls();
        addEvents();
        reloadCart();
       // loadData();
    }




    private void addEvents() {
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    private void addControls() {
        recyclerMyCart = findViewById(R.id.recyclerMyCart);
        productArrayList = new ArrayList<>();
        cartAdapter = new CartAdapter(this, productArrayList, new ClickItemCart() {
            @Override
            public void increasePro(int position_item, Cart cart) {
                db.collection("User")
                    .document(user.getUid())
                    .collection("AddToCart")
                    .document(cart.getId_cart())
                    .update("quantity",cart.getQuantity() + 1)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            cart.setQuantity(cart.getQuantity() + 1);
                            cartAdapter.notifyItemChanged(position_item);
                        }
                    });
            }
            @Override
            public void decreasePro(int position_item, Cart cart) {

            }

            @Override
            public void removePro(int position_item, Cart cart) {
                db.collection("User")
                        .document(user.getUid())
                        .collection("AddToCart")
                        .document(cart.getId_cart())
                        .delete();

                productArrayList.remove(position_item);

                cartAdapter.notifyItemRemoved(position_item);

            }
        });

        recyclerMyCart.setAdapter(cartAdapter);
        recyclerMyCart.setLayoutManager(new LinearLayoutManager(this));
        btnBack = findViewById(R.id.btnBack);


    }

    private void reloadCart() {
        db.collection("User").document(user.getUid()).collection("AddToCart").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                for (QueryDocumentSnapshot dc : task.getResult()) {
                    Double quantity = dc.getDouble("quantity");
                    Double total_price = dc.getDouble("total_price");
                    String proID = dc.getString("proID");

                    db.collection("Product").document(proID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            String namePro = task.getResult().getString("name");
                            String imgPro = task.getResult().getString("image");
                            String color = task.getResult().getString("color");
                            productArrayList.add(new Cart(namePro, 1.1, "FT", imgPro, color, 2, "2", quantity, total_price, dc.getId()));
                            cartAdapter.notifyDataSetChanged();
                        }
                    });
                }
            }
        });

    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.header_menu, menu);
        return true;
    }

   @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.bag_header) {
            Intent intent = new Intent(MyCartActivity.this, MyCartActivity.class);
            startActivity(intent);
        }

        if (item.getItemId() == R.id.account_header) {
            Intent intent = new Intent(MyCartActivity.this, MyOrderActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
   }

}