package com.cristianerm.pd2professor;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class MenuActivity extends AppCompatActivity {

    ImageView calendario, agenda, diario, ouvidoria;

    Button button_logout;
    TextView text_view_identificador;

    private static final String TAG = "MenuActivity";

    private FirebaseDatabase mFirebaseDatase;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference myRef;
    private String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        agenda = (ImageView) findViewById(R.id.image_view_agenda);
        calendario = (ImageView) findViewById(R.id.image_view_calendario);
        ouvidoria = (ImageView) findViewById(R.id.image_view_ouvidoria);
        diario = (ImageView) findViewById(R.id.image_view_diario);
        button_logout = (Button) findViewById(R.id.button_logout);
        text_view_identificador = (TextView) findViewById(R.id.text_view_identificador_professor);

        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatase = FirebaseDatabase.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        userID = user.getUid();
        myRef = mFirebaseDatase.getReference().child(userID).child("info_user");

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = mAuth.getCurrentUser();
                if (user != null) {
                    //Toast.makeText(MenuActivity.this, "User sighed in", Toast.LENGTH_SHORT).show();
                } else {
                    //Toast.makeText(MenuActivity.this, "User not sighed in", Toast.LENGTH_SHORT).show();
                }
            }
        };

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    UserInformation uInfo = new UserInformation();
                    uInfo.setNome(ds.getValue(UserInformation.class).getNome());
                    uInfo.setTurma(ds.getValue(UserInformation.class).getTurma());

                    String turma = uInfo.getTurma();

                    Log.d(TAG, "showData: Nome: " + uInfo.getNome());

                    text_view_identificador.setText(uInfo.getNome());
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        button_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                Intent i = new Intent(MenuActivity.this, MainActivity.class);
                startActivity(i);
            }
        });

        agenda.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                            UserInformation uInfo = new UserInformation();
                            uInfo.setTurma(ds.getValue(UserInformation.class).getTurma());

                            String turma = uInfo.getTurma();
                            Intent i;
                            i = new Intent(MenuActivity.this, AgendaActivity.class);
                            i.putExtra("nome_turma", turma);
                            startActivity(i);
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
            }
        });

        calendario.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i;
                i = new Intent(MenuActivity.this, CalendarioActivity.class);
                startActivity(i);
            }
        });

        diario.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                            UserInformation uInfo = new UserInformation();
                            uInfo.setTurma(ds.getValue(UserInformation.class).getTurma());

                            String turma = uInfo.getTurma();
                            Intent i;
                            i = new Intent(MenuActivity.this, DiarioActivity.class);
                            i.putExtra("nome_turma", turma);
                            startActivity(i);
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
            }
        });

        ouvidoria.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i;
                i = new Intent(MenuActivity.this, OuvidoriaActivity.class);
                String nome_professor = text_view_identificador.getText().toString();
                if(nome_professor == "Professor(a)"){
                    Toast.makeText(MenuActivity.this, "Espere um momento, aplicação carregando...", Toast.LENGTH_LONG).show();
                }else{
                    i.putExtra("nome_professor", nome_professor);
                    startActivity(i);
                }
            }
        });

    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

}
