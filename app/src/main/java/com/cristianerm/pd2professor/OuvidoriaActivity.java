package com.cristianerm.pd2professor;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

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

import org.w3c.dom.Text;

import java.util.Calendar;
import java.util.Date;

public class OuvidoriaActivity extends AppCompatActivity {

    Toolbar toolbar_ouvidoria;
    EditText edit_text_mensagem;
    CheckBox check_box_anonimato;
    Button button_enviar;
    TextView text_view_mensagem_error;

    private static final String TAG = "OuvidoriaActivity";

    private FirebaseDatabase mFirebaseDatase;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference myRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ouvidoria);

        toolbar_ouvidoria = (Toolbar) findViewById(R.id.tool_bar_ouvidoria);
        setSupportActionBar(toolbar_ouvidoria);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar_ouvidoria.setTitle("");
        toolbar_ouvidoria.setSubtitle("");

        edit_text_mensagem = (EditText) findViewById(R.id.edit_text_ouvidoria);
        check_box_anonimato = (CheckBox) findViewById(R.id.check_box_ouvidoria);
        button_enviar = (Button) findViewById(R.id.button_ouvidoria);
        text_view_mensagem_error = (TextView) findViewById(R.id.text_view_error_ouvidoria);

        mFirebaseDatase = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();
        myRef = mFirebaseDatase.getReference().child("ouvidoria");

        toolbar_ouvidoria.setNavigationOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent i;
                i = new Intent(OuvidoriaActivity.this, MenuActivity.class);
                startActivity(i);
            }
        });

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = mAuth.getCurrentUser();
                if (user != null) {
                    Toast.makeText(OuvidoriaActivity.this, "User sighed in", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(OuvidoriaActivity.this, "User not sighed in", Toast.LENGTH_SHORT).show();
                }
            }
        };

        button_enviar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mensagem = edit_text_mensagem.getText().toString();

                if(!mensagem.equals("")){
                    text_view_mensagem_error.setText("");

                    Date currentTime = Calendar.getInstance().getTime();
                    String data_atual = currentTime.toString();
                    String hora_e_data = "Hora: "+data_atual.substring(11,19) + " Data: " + data_atual.substring(4,10);

                    Bundle bundle = getIntent().getExtras();
                    String nome_professor = bundle.getString("nome_professor");

                    if(check_box_anonimato.isChecked()){
                        nome_professor = "Anônimo";
                    }

                    Toast.makeText(OuvidoriaActivity.this, nome_professor + " " + hora_e_data + " " + mensagem, Toast.LENGTH_LONG).show();
                    String key = myRef.push().getKey();
                    myRef.child(key).child("data").setValue(hora_e_data);
                    myRef.child(key).child("mensagem").setValue(mensagem);
                    myRef.child(key).child("professor").setValue(nome_professor);
                }else{
                    text_view_mensagem_error.setText("O campo de texto está vazia");
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
