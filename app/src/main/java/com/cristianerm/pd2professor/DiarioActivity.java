package com.cristianerm.pd2professor;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.Date;

public class DiarioActivity extends AppCompatActivity {

    ImageButton voltar;
    EditText mensagemDiario;
    Button enviar;
    TextView mensagemError;

    private static final String TAG = "DiarioActivity";

    private FirebaseDatabase mFirebaseDatase;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference myRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diario);

        voltar = (ImageButton) findViewById(R.id.buttonVoltarDiario);
        mensagemDiario = (EditText) findViewById(R.id.editTextDiario);
        enviar = (Button) findViewById(R.id.buttonDiario);
        mensagemError = (TextView) findViewById(R.id.textErrorDiario);

        mFirebaseDatase = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();

        Bundle bundle = getIntent().getExtras();
        String nome_turma = bundle.getString("nome_turma");

        myRef = mFirebaseDatase.getReference().child("diario_professor").child(nome_turma);

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = mAuth.getCurrentUser();
                if (user != null) {
                    Toast.makeText(DiarioActivity.this, "User sighed in", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(DiarioActivity.this, "User not sighed in", Toast.LENGTH_SHORT).show();
                }
            }
        };

        voltar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i;
                i = new Intent(DiarioActivity.this, MenuActivity.class);
                startActivity(i);
            }
        });

        enviar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mensagem = mensagemDiario.getText().toString();

                if(!mensagem.equals("")){
                    mensagemError.setText("");

                    Date currentTime = Calendar.getInstance().getTime();
                    String data_atual = currentTime.toString();
                    String hora_e_data = "Hora: "+data_atual.substring(11,19) + " Data: " + data_atual.substring(4,10);

                    Toast.makeText(DiarioActivity.this, hora_e_data + " " + mensagem, Toast.LENGTH_LONG).show();

                    String key = myRef.push().getKey();
                    myRef.child(key).child("data").setValue(hora_e_data);
                    myRef.child(key).child("mensagem").setValue(mensagem);
                }else{
                    mensagemError.setText("O campo de texto está vazia");
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
