package com.cristianerm.pd2professor;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class AgendaActivity extends AppCompatActivity {

    Toolbar toolbar_agenda;
    Spinner spinner_aluno;
    Spinner spinner_alimento;
    Spinner spinner_sono;
    Button button_agenda;

    private static final String TAG = "Agenda Activity";

    private FirebaseDatabase mFirebaseDatase;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference myRefGetAlunos;
    private DatabaseReference myRef2;
    private DatabaseReference myRef3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agenda);

        toolbar_agenda = (Toolbar) findViewById(R.id.tool_bar_agenda);
        setSupportActionBar(toolbar_agenda);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar_agenda.setTitle("");
        toolbar_agenda.setSubtitle("");

        spinner_aluno = (Spinner) findViewById(R.id.spinner_aluno_agenda);
        spinner_alimento = (Spinner) findViewById(R.id.spinner_alimento_agenda);
        spinner_sono = (Spinner) findViewById(R.id.spinner_sono_agenda);
        button_agenda = (Button) findViewById(R.id.button_agenda);

        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatase = FirebaseDatabase.getInstance();

        Bundle bundle = getIntent().getExtras();
        String nome_turma = bundle.getString("nome_turma");
        myRefGetAlunos = mFirebaseDatase.getReference().child("turmas").child(nome_turma);

        toolbar_agenda.setNavigationOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent i;
                i = new Intent(AgendaActivity.this, MenuActivity.class);
                startActivity(i);
            }
        });

        final ArrayList<String> alunos_turma = new ArrayList<String>();

        myRefGetAlunos.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                alunos_turma.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    AlunoInformation tInfo = new AlunoInformation();
                    tInfo.setNome(ds.getValue(AlunoInformation.class).getNome());

                    alunos_turma.add(tInfo.getNome());
                }

                ///// Spinner_aluno
                ArrayAdapter<String> adapter_aluno = new ArrayAdapter<String>(AgendaActivity.this,  android.R.layout.simple_spinner_dropdown_item, alunos_turma);
                adapter_aluno.setDropDownViewResource( android.R.layout.simple_spinner_dropdown_item);

                spinner_aluno.setAdapter(adapter_aluno);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = mAuth.getCurrentUser();
                if (user != null) {
                    Toast.makeText(AgendaActivity.this, "User sighed in", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(AgendaActivity.this, "User not sighed in", Toast.LENGTH_SHORT).show();
                }
            }
        };

        ///// Spinner_alimento
        ArrayAdapter<CharSequence> adapter_alimento = ArrayAdapter.createFromResource(this,
                R.array.alimentacao, android.R.layout.simple_spinner_item);
        adapter_alimento.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner_alimento.setAdapter(adapter_alimento);

        ///// Spinner_sono
        ArrayAdapter<CharSequence> adapter_sono = ArrayAdapter.createFromResource(this,
                R.array.sono, android.R.layout.simple_spinner_item);
        adapter_sono.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner_sono.setAdapter(adapter_sono);

        //// Button
        button_agenda.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Date currentTime = Calendar.getInstance().getTime();
                String data_atual = currentTime.toString();
                final String hora_e_data = "Hora: "+data_atual.substring(11,19) + " Data: " + data_atual.substring(4,10);

                final String selected_aluno = spinner_aluno.getItemAtPosition(spinner_aluno.getSelectedItemPosition()).toString();

                final String selected_alimento = "Alimentação: " + spinner_alimento.getItemAtPosition(spinner_alimento.getSelectedItemPosition()).toString();

                final String selected_sono = "Sono: " + spinner_sono.getItemAtPosition(spinner_sono.getSelectedItemPosition()).toString();

                Toast.makeText(AgendaActivity.this, selected_aluno+" "+selected_alimento+" "+selected_sono, Toast.LENGTH_LONG).show();

                myRef2 = mFirebaseDatase.getReference().child("alunos").child(selected_aluno);

                myRef2.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                            AlunoInformation tInfo = new AlunoInformation();
                            tInfo.setUser_id(ds.getValue(AlunoInformation.class).getUser_id());

                            Toast.makeText(AgendaActivity.this, tInfo.getUser_id(), Toast.LENGTH_LONG).show();
                            String user_id = tInfo.getUser_id();
                            myRef3 = mFirebaseDatase.getReference().child(user_id).child("agenda_pessoal");
                        }
                        String key = myRef3.push().getKey();
                        myRef3.child(key).child("data").setValue(hora_e_data);
                        myRef3.child(key).child("alimento").setValue(selected_alimento);
                        myRef3.child(key).child("sono").setValue(selected_sono);
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
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
