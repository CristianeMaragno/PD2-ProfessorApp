package com.cristianerm.pd2professor;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Date;

public class MainActivity extends AppCompatActivity {

    Button login;
    EditText aluno;
    EditText senha;
    ProgressBar progressBar;
    TextView textError;

    private static final String TAG = "Main Activity";

    private FirebaseAuth mAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private DatabaseReference myRefUserDeletado;
    private String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        login = (Button) findViewById(R.id.buttonLogin);
        aluno = (EditText) findViewById(R.id.nomeProfessor);
        senha = (EditText) findViewById(R.id.senhaProfessor);
        progressBar = (ProgressBar) findViewById(R.id.progressBarLogin);
        progressBar.setVisibility(View.GONE);
        textError = (TextView) findViewById(R.id.textErrorLogin);

        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                String email = aluno.getText().toString();
                String pass = senha.getText().toString();
                if(!email.equals("") && !pass.equals("")){
                    signIn(email, pass);
                }else{
                    progressBar.setVisibility(View.GONE);
                    textError.setText("Você não preencheu todos os campos");
                }
            }
        });
    }

    private void signIn(final String email, String password) {
        Log.d(TAG, "signIn:" + email);
        // [START sign_in_with_email]
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success
                            textError.setText("");
                            Log.d(TAG, "signInWithEmail:success");

                            checkUser(email);
                            checkStatus();
                        }
                        // [START_EXCLUDE]
                        if (!task.isSuccessful()) {
                            progressBar.setVisibility(View.GONE);
                            try {
                                throw task.getException();
                            } catch(FirebaseAuthInvalidCredentialsException e) {
                                textError.setText("Email ou senha incorreta");
                            } catch(FirebaseAuthUserCollisionException e) {
                                textError.setText("Tente novamente");
                            } catch(Exception e) {
                                Log.e(TAG, e.getMessage());
                            }
                        }
                    }
                });
        // [END sign_in_with_email]
    }

    public void checkUser(final String email){

        myRefUserDeletado = mFirebaseDatabase.getReference().child("users_deletados");

        myRefUserDeletado.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    UserInformation uInfo = new UserInformation();
                    uInfo.setEmail(ds.getValue(UserInformation.class).getEmail());

                    Log.d(TAG, "showData: Email: " + uInfo.getEmail());
                    String email_database = uInfo.getEmail();
                    progressBar.setVisibility(View.GONE);

                    if(email_database.equals(email)){
                        new AlertDialog.Builder(MainActivity.this)
                                .setTitle("Opa!")
                                .setMessage("Este usuário não tem mais acesso ao aplicativo! Entre em contato com a direção caso" +
                                        " tenha havido algum engano")
                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        // Continue with delete operation
                                    }
                                })
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .show();

                        mAuth.signOut();
                        aluno.getText().clear();
                        senha.getText().clear();
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

    }

    @Override
    public void onStart() {
        super.onStart();
    }

    public void checkStatus(){ ;
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        userID = user.getUid();
        myRef = mFirebaseDatabase.getReference().child(userID).child("info_user");

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    UserInformation uInfo = new UserInformation();
                    uInfo.setStatus(ds.getValue(UserInformation.class).getStatus());

                    Log.d(TAG, "showData: Status: " + uInfo.getStatus());
                    String status = uInfo.getStatus();
                    progressBar.setVisibility(View.GONE);

                    if(status.equals("Professor(a)")){
                        Intent i = new Intent(MainActivity.this, MenuActivity.class);
                        startActivity(i);
                    }else{
                        new AlertDialog.Builder(MainActivity.this)
                                .setTitle("Opa!")
                                .setMessage("Este aplicativo é para o uso exclusivo dos professores do PD2! Faça download na Play Store " +
                                        "do aplicativo para alunos(as) ou do aplicativo para direção!")
                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        // Continue with delete operation
                                    }
                                })
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .show();
                        aluno.getText().clear();
                        senha.getText().clear();
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

    }
}
