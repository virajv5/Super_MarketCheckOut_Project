package com.ddit.project.supermarketcheckouter;

import static com.ddit.project.supermarketcheckouter.Constant.SHARED_USER_ID;
import static com.ddit.project.supermarketcheckouter.Constant.SHARED_USER_email;
import static com.ddit.project.supermarketcheckouter.Constant.SHARED_USER_name;
import static com.ddit.project.supermarketcheckouter.Constant.update_reward;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ddit.project.supermarketcheckouter.Models.CartItem_GetSet;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class User_PaymentActivity extends AppCompatActivity {


    CardView loading_cardview;
    LinearLayout no_data_ll;
    TextView bill_title;
    final int UPI_PAYMENT = 0;

    String status_glob, traid;
    LottieAnimationView animview;
    TextView finaldata, datetxt, infotxt, tranidtxt, traemail;
    Button okbtn;
    PrefStorageManager pref;
    CardView payment_done_cv;
    CartDbHandler db_cart;
    ArrayList<CartItem_GetSet> order_item_list = new ArrayList<>();
    String json_product = "";

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_process_payment);
        pref = new PrefStorageManager(User_PaymentActivity.this);
        db_cart = new CartDbHandler(this);

        order_item_list = new ArrayList<>();
        order_item_list.addAll(db_cart.getAllCartLists());

        GsonBuilder gsonBuilder = new GsonBuilder();
        Gson gson = gsonBuilder.create();
        json_product = gson.toJson(order_item_list);

        payment_done_cv = findViewById(R.id.payment_done_cv);
        payment_done_cv.setVisibility(View.GONE);

        findViewById(R.id.okbtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(User_PaymentActivity.this, HomeActivity.class));
                finish();
            }
        });

        this.loading_cardview = findViewById(R.id.loading_cardview);
        this.no_data_ll = findViewById(R.id.no_data_ll);
        this.bill_title = findViewById(R.id.bill_title);
        no_data_ll.setVisibility(View.GONE);
        loading_cardview.setVisibility(View.VISIBLE);

        bill_title.setText(Constant.pass_payment_value + "");

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Uri uri = Uri.parse("upi://pay").buildUpon()
                        .appendQueryParameter("pa", Constant.getupi_id)
                        .appendQueryParameter("pn", "BharatPeMarchant")
                        //.appendQueryParameter("mc", "Payee merchant code")
                        .appendQueryParameter("tr", "" + System.currentTimeMillis())
                        .appendQueryParameter("tn", "Pay for Supermarket Checkout")
                        .appendQueryParameter("am", Constant.pass_payment_value + "")
                        .appendQueryParameter("cu", "INR").build();

                Intent upiPayIntent = new Intent(Intent.ACTION_VIEW);
                upiPayIntent.setData(uri);
                Intent chooser = Intent.createChooser(upiPayIntent, "Pay with");

                if (null != chooser.resolveActivity(getPackageManager())) {
                    startActivityForResult(chooser, UPI_PAYMENT);
                } else {
                    Toast.makeText(User_PaymentActivity.this, "No UPI app found, please install one to continue", Toast.LENGTH_SHORT).show();
                    onBackPressed();
                }
            }
        }, 1000);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case UPI_PAYMENT:
                if ((RESULT_OK == resultCode) || (resultCode == 11)) {
                    if (data != null) {
                        String trxt = data.getStringExtra("response");
                        ArrayList<String> dataList = new ArrayList<>();
                        dataList.add(trxt);
                        upiPaymentDataOperation(dataList);
                    } else {
                        ArrayList<String> dataList = new ArrayList<>();
                        dataList.add("nothing");
                        upiPaymentDataOperation(dataList);
                    }
                } else {
                    ArrayList<String> dataList = new ArrayList<>();
                    dataList.add("nothing");
                    upiPaymentDataOperation(dataList);
                }
                break;
        }


// Add the rest of your code

    }

    @SuppressLint("MissingPermission")
    public static boolean isConnectionAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();
            if (netInfo != null && netInfo.isConnected()
                    && netInfo.isConnectedOrConnecting()
                    && netInfo.isAvailable()) {
                return true;
            }
        }
        return false;
    }


    private void upiPaymentDataOperation(ArrayList<String> data) {
        if (isConnectionAvailable(User_PaymentActivity.this)) {

            String str = data.get(0);
            String paymentCancel = "Payment cancelled by user.";
            if (str == null) {
                str = "discard";
            }
            String status = "";
            traid = "";
            String response[] = str.split("&");
            for (int i = 0; i < response.length; i++) {
                String equalStr[] = response[i].split("=");
                if (equalStr.length >= 2) {
                    if (equalStr[0].toLowerCase().equals("Status".toLowerCase())) {
                        status = equalStr[1].toLowerCase();
                    } else if (equalStr[0].toLowerCase().equals("ApprovalRefNo".toLowerCase()) || equalStr[0].toLowerCase().equals("txnRef".toLowerCase())) {
                        traid = equalStr[1];
                    }
                } else {
                    status = "Payment cancelled by user.";
                }
            }

            if (status.equals("success")) {
                status_glob = "success";
            } else if (status.equals(paymentCancel)) {
                status_glob = "cancel";
            } else {
                status_glob = "fail";
            }
            showpyamentdone();
        } else {
            loading_cardview.setVisibility(View.GONE);
            Toast.makeText(User_PaymentActivity.this, "Internet connection is not available. Please check and try again", Toast.LENGTH_SHORT).show();
        }
    }

    public void showpyamentdone() {
        Query lastQuery = FirebaseDatabase.getInstance().getReference().child("order").orderByKey().limitToLast(1);
        lastQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (traid.equals("")) {
                    traid = "TRX" + System.currentTimeMillis();
                }
                SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy HH:mm:ss aa");
                String currentDateTimeString = sdf.format(new Date()).replace("AM", "am").replace("PM", "pm");

                String lastorder_id = "";
                if (dataSnapshot.exists()) {
                    for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                        if (childSnapshot.exists()) {
                            lastorder_id = childSnapshot.child("order_id").getValue().toString();
                        }
                    }
                }

                int neworder_id = 1;
                if (!lastorder_id.equals("")) {
                    neworder_id = (Integer.parseInt(lastorder_id) + 1);
                }

                Map<String, Object> valuesorder = new HashMap<>();
                valuesorder.put("order_id", String.valueOf(neworder_id));
                valuesorder.put("payment_refid", traid);
                valuesorder.put("user_id", pref.getStringvalue(SHARED_USER_ID));
                valuesorder.put("user_name", pref.getStringvalue(SHARED_USER_name));
                valuesorder.put("productlist", json_product + "");
                valuesorder.put("total_amount", Constant.pass_payment_value + "");
                valuesorder.put("ondate", currentDateTimeString);
                valuesorder.put("payment_status", status_glob);
                valuesorder.put("admin_approve", "0");

                FirebaseDatabase.getInstance().getReference("order").push().setValue(valuesorder).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (status_glob == null) {
                            status_glob = "fail";
                        }

                        finaldata = findViewById(R.id.finaldata);
                        datetxt = findViewById(R.id.date);
                        infotxt = findViewById(R.id.textinfo);
                        tranidtxt = findViewById(R.id.tranID);
                        traemail = findViewById(R.id.tranemail);
                        okbtn = findViewById(R.id.okbtn);
                        animview = findViewById(R.id.animationView);
                        datetxt.setText(currentDateTimeString);

                        tranidtxt.setText(traid);
                        traemail.setText(pref.getStringvaluedef(SHARED_USER_email, ""));

                        if (status_glob.equals("cancel")) {
                            finaldata.setText("Payment Cancelled!");
                            finaldata.setTextColor(Color.RED);
                            animview.setAnimation(R.raw.fail);
                            infotxt.setText("Oops! Something went wrong\nPlease try again!");
                            okbtn.setBackgroundColor(Color.RED);
                        } else if (status_glob.equals("success")) {
                            finaldata.setText("Payment Success!");
                            finaldata.setTextColor(Color.GREEN);
                            animview.setAnimation(R.raw.sucesso);
                            infotxt.setText("Great! Now you can Earn More\n Good Luck!");
                            okbtn.setBackgroundColor(Color.GREEN);
                        } else if (status_glob.equals("fail")) {
                            finaldata.setText("Fail");
                            finaldata.setTextColor(Color.RED);
                            animview.setAnimation(R.raw.fail);
                            infotxt.setText("Oops! Something went wrong\nPlease try again!");
                            okbtn.setBackgroundColor(Color.RED);

                        }
                        if (status_glob.equals("success")) {
                            db_cart.removeAll();
                            if (update_reward) {
                                float addreward = (Constant.pass_payment_value * Float.parseFloat("0.1"));
                                Map<String, Object> valuesreward = new HashMap<>();
                                valuesreward.put("reward", String.valueOf(addreward));
                                FirebaseDatabase.getInstance().getReference().child("user").child(pref.getStringvalue(SHARED_USER_ID)).updateChildren(valuesreward);
                            } else {
                                FirebaseDatabase.getInstance().getReference().child("user").child(pref.getStringvalue(SHARED_USER_ID)).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if (snapshot.exists()) {
                                            float addreward = (Constant.pass_payment_value * Float.parseFloat("0.1"));
                                            float getrew = Float.parseFloat(snapshot.child("reward").getValue().toString());
                                            if (getrew > 0) {
                                                addreward = ((Constant.pass_payment_value * Float.parseFloat("0.1")) + getrew);
                                            }
                                            Map<String, Object> valuesreward = new HashMap<>();
                                            valuesreward.put("reward", String.valueOf(addreward));
                                            FirebaseDatabase.getInstance().getReference().child("user").child(pref.getStringvalue(SHARED_USER_ID)).updateChildren(valuesreward);
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                            }
                        }
                        payment_done_cv.setVisibility(View.VISIBLE);
                        loading_cardview.setVisibility(View.GONE);
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                loading_cardview.setVisibility(View.GONE);
            }
        });
    }

}
