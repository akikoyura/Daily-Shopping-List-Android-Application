package com.example.dailyshoppinglist;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.dailyshoppinglist.Model.Data;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.util.Date;
import java.util.zip.Inflater;

public class HomeActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private FloatingActionButton fab_btn;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private RecyclerView recyclerView;
    private TextView total_amount;

    //global variables
    private String type;
    private int amount;
    private String note;

    private String post_key;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        toolbar = (Toolbar) findViewById(R.id.home_toolbar);
        total_amount = findViewById(R.id.total_amount);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Daily Shopping List");

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser mUser = mAuth.getCurrentUser();
        String uId = mUser.getUid();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Shopping List").child(uId);

        recyclerView = findViewById(R.id.recycler_home);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int total = 0;
                for(DataSnapshot snap: dataSnapshot.getChildren()) {
                    Data data = snap.getValue(Data.class);
                    total += data.getAmount();
                }
                total_amount.setText(String.valueOf(total));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        mDatabase.keepSynced(true);

        fab_btn = findViewById(R.id.fab);
        fab_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                customDialog();
            }
        });
    }
    private  void customDialog()
    {
        AlertDialog.Builder mydialog = new AlertDialog.Builder(HomeActivity.this);
        LayoutInflater inflater = LayoutInflater.from(HomeActivity.this);
        View myview = inflater.inflate(R.layout.input_data,null);

        final AlertDialog dialog  = mydialog.create();
        dialog.setView(myview);

        final EditText type  = myview.findViewById(R.id.edt_type);
        final EditText amount = myview.findViewById(R.id.edt_amount);
        final EditText note = myview.findViewById(R.id.edt_note);
        Button btnSave = myview.findViewById(R.id.btn_save);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String mType  = type.getText().toString().trim();
                String mAmount = amount.getText().toString().trim();
                String mNote = note.getText().toString().trim();

                int mAmountInt = Integer.parseInt(mAmount);
                if(TextUtils.isEmpty(mType))
                {
                    type.setError("Required Field...");
                    return ;
                }
                if(TextUtils.isEmpty(mAmount))
                {
                    amount.setError("Required field...");
                    return ;
                }
                if(TextUtils.isEmpty(mNote))
                {
                    note.setError("Required field...");
                    return;
                }

                String id = mDatabase.push().getKey();
                String mDate = DateFormat.getDateInstance().format(new Date());

                Data data = new Data(mType,mAmountInt,mNote,mDate,id);
                mDatabase.child(id).setValue(data);
                Toast.makeText(getApplicationContext(),"Data Add",Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });
        dialog.show();

    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseRecyclerAdapter<Data,MyViewHolder> adapter  = new FirebaseRecyclerAdapter<Data, MyViewHolder>(Data.class,R.layout.item_data,MyViewHolder.class,mDatabase) {
            @Override
            protected void populateViewHolder(MyViewHolder viewHolder, final Data model, final int position) {
                viewHolder.setDate(model.getDate());
                viewHolder.setAmount(model.getAmount());
                viewHolder.setNote(model.getNote());
                viewHolder.SetType(model.getType());

                viewHolder.myview.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        post_key = getRef(position).getKey();
                        type = model.getType();
                        note  = model.getNote();
                        amount = model.getAmount();
                        UpdateData();
                    }
                });
            }
        };
        recyclerView.setAdapter(adapter);
    }
    public static class MyViewHolder extends  RecyclerView.ViewHolder
    {
        View myview;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            myview = itemView;
        }

        public void SetType(String Type)
        {
            TextView mType = myview.findViewById(R.id.type);
            mType.setText(Type);
        }
        public void setNote(String note)
        {
            TextView mNote = myview.findViewById(R.id.note);
            mNote.setText(note);
        }
        public void setDate (String date)
        {
            TextView mDate = myview.findViewById(R.id.date);
            mDate.setText(date);
        }
        public void setAmount (int amount)
        {
            TextView mAmount = myview.findViewById(R.id.amount);
            mAmount.setText(String.valueOf(amount));
        }
    }

    public void UpdateData()
    {
        AlertDialog.Builder mydialog = new AlertDialog.Builder(HomeActivity.this);
        LayoutInflater inflater = LayoutInflater.from(HomeActivity.this);
        View myview = inflater.inflate(R.layout.update_input,null);
        final AlertDialog dialog = mydialog.create();
        dialog.setView(myview);

       final EditText edit_type = myview.findViewById(R.id.edt_type_update);
       final EditText edit_amount = myview.findViewById(R.id.edt_amount_update);
       final EditText edit_note = myview.findViewById(R.id.edt_note_update);

        edit_type.setText(type);
        edit_type.setSelection(type.length());
        edit_amount.setText(String.valueOf(amount));
        edit_amount.setSelection(String.valueOf(amount).length());
        edit_note.setText(note);
        edit_note.setSelection(note.length());

        Button btn_update = myview.findViewById(R.id.btn_update);
        Button btn_delete = myview.findViewById(R.id.btn_delete_upd);


        //Update Record on FireBase
        btn_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                type = edit_type.getText().toString().trim();
                amount = Integer.parseInt(edit_amount.getText().toString().trim());
                note = edit_note.getText().toString().trim();
                String date = DateFormat.getDateInstance().format(new Date());
                Data data = new Data(type,amount,note,date,post_key );
                mDatabase.child(post_key).setValue(data);
                dialog.dismiss();
            }
        });

        //Delete Record on FireBase
        btn_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDatabase.child(post_key).removeValue();
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.logout:
                mAuth.signOut();
                startActivity(new Intent(getApplicationContext(),MainActivity.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
