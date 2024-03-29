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
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.sneaker.shoeapp.Adapter.CartAdapter;
import com.sneaker.shoeapp.Interface.ClickItemCart;
import com.sneaker.shoeapp.model.Cart;
import com.sneaker.shoeapp.model.ListProduct;
import com.sneaker.shoeapp.model.Product;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyCartActivity extends AppCompatActivity {
    RecyclerView recyclerMyCart;
    CartAdapter cartAdapter;
    ArrayList<Cart> productArrayList;
    ImageButton decreasePro, increasePro;
    ListProduct productList;
    ImageButton btnBack;
    FirebaseFirestore db;
    FirebaseAuth mAuth;
    FirebaseUser user;
    TextView total_cart,quantity_cart;
    Button btnCheckout;
    int quantity_val= 0;
    int total_cart_val = 0;

    //    ListView listItem_cart;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_cart);
        Toolbar main_header = findViewById(R.id.menu_header_back);
        setSupportActionBar(main_header);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        if (user == null){
            finish();
            Intent intent = new Intent(MyCartActivity.this, LoginActivity.class);
            startActivity(intent);
        }
        addControls();
        addEvents();
        reloadCart();
       // loadData();
        Total_Cart();
    }


    private void Total_Cart(){
        Query query = db.collection("User").document(user.getUid()).collection("AddToCart").document().getParent();
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                for(QueryDocumentSnapshot dc: task.getResult()){
                    quantity_val += dc.getDouble("quantity") ;
                    total_cart_val += dc.getDouble("total_price");
                }
                total_cart.setText("$"+total_cart_val+"");
                quantity_cart.setText(quantity_val+"");
            }
        });
    }

    public void Order(){
        Product product = new Product();
        Map<String,Object> infoOrder = new HashMap<>();
        Map<String,Object> infoProduct = new HashMap<>();
        db.collection("User").document(user.getUid())
                .collection("Order").add(infoOrder).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        db.collection("User").document(user.getUid())
                                .collection("Order").document(documentReference.getId())
                                .collection("listPro").document(product.getId()).set(infoProduct);
                    }
                });
    }

    private void addEvents() {
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        btnCheckout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MyCartActivity.this, CheckoutActivity.class);
                Bundle bundle = new Bundle();
                intent.putExtra("pro",(Serializable) productArrayList);
                bundle.putInt("quantity",quantity_val);
                bundle.putDouble("total",(double) total_cart_val);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
    }

    private void addControls() {
        btnCheckout = findViewById(R.id.btnCheckout);
        total_cart = findViewById(R.id.total_cart);
        quantity_cart = findViewById(R.id.quantity_cart);
        recyclerMyCart = findViewById(R.id.recyclerMyCart);
        productArrayList = new ArrayList<>();
        cartAdapter = new CartAdapter(this, productArrayList, new ClickItemCart() {
            @Override
            public void increasePro(int position_item, Cart cart) {
                DocumentReference documentReference =  db.collection("User")
                        .document(user.getUid())
                        .collection("AddToCart")
                        .document(cart.getId_cart());

                documentReference.update("quantity",cart.getQuantity() + 1)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            cart.setTotal_cart((cart.getQuantity()+1) * cart.getPrice());
                            cart.setQuantity(cart.getQuantity() + 1);
                            cartAdapter.notifyItemChanged(position_item);
                            documentReference.update("total_price",(cart.getQuantity()) * cart.getPrice());
                            total_cart_val += cart.getPrice();
                            total_cart.setText("$"+total_cart_val);
                            quantity_val += 1;
                            quantity_cart.setText(quantity_val+"");
                        }
                    });
              /*  documentReference.update("total_price",cart.getTotal_cart() * cart.getQuantity())
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                cart.setTotal_cart(cart.getTotal_cart() * cart.getQuantity());
                                cartAdapter.notifyItemChanged(position_item);
                            }
                        });*/
            }
            @Override
            public void decreasePro(int position_item, Cart cart) {
                if(cart.getQuantity() > 1){
                    DocumentReference documentReference =
                    db.collection("User")
                    .document(user.getUid())
                    .collection("AddToCart")
                    .document(cart.getId_cart());
                    documentReference.update("quantity",cart.getQuantity() - 1)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            cart.setQuantity(cart.getQuantity() - 1);
                            cart.setTotal_cart(cart.getQuantity() * cart.getPrice());
                            cartAdapter.notifyItemChanged(position_item);
                            documentReference.update("total_price",(cart.getQuantity()) * cart.getPrice());
                            total_cart_val -= cart.getPrice();
                            total_cart.setText("$"+total_cart_val);
                            quantity_val -= 1;
                            quantity_cart.setText(quantity_val+"");
                        }
                    });
                }
                else{
                    db.collection("User")
                            .document(user.getUid())
                            .collection("AddToCart")
                            .document(cart.getId_cart())
                            .delete();

                    productArrayList.remove(position_item);

                    cartAdapter.notifyItemRemoved(position_item);
                }
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
                //remove
                total_cart_val -= cart.getTotal_cart();
                total_cart.setText("$"+total_cart_val);
                quantity_val -= cart.getQuantity();
                quantity_cart.setText(quantity_val+"");
            }
        });

        recyclerMyCart.setAdapter(cartAdapter);
        recyclerMyCart.setLayoutManager(new LinearLayoutManager(this));
        btnBack = findViewById(R.id.btnBack);


    }

    private void reloadCart() {
        if(user != null){
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
                                String namePro = task.getResult().getString("proName");
                                String imgPro = task.getResult().getString("image");
                                String color = task.getResult().getString("color");
                                Double price = Double.valueOf(task.getResult().getString("price")) ;

                                productArrayList.add(new Cart(namePro,price , dc.getString("category"), imgPro, color, 2, proID, quantity, total_price, dc.getId()));
                                cartAdapter.notifyDataSetChanged();
                            }
                        });
                    }
                }
            });
        }
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