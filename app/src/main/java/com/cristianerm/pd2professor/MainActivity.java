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

    EditText edit_text_professor;
    EditText edit_text_senha;
    Button button_login;
    ProgressBar progress_bar;
    TextView text_view_error;

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

        edit_text_professor = (EditText) findViewById(R.id.email_professor_login);
        edit_text_senha = (EditText) findViewById(R.id.senha_professor_login);
        button_login = (Button) findViewById(R.id.button_login);
        progress_bar = (ProgressBar) findViewById(R.id.progress_bar_login);
        text_view_error = (TextView) findViewById(R.id.text_view_error_login);

        progress_bar.setVisibility(View.GONE);

        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();

        button_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progress_bar.setVisibility(View.VISIBLE);
                String email = edit_text_professor.getText().toString();
                String pass = edit_text_senha.getText().toString();
                if(!email.equals("") && !pass.equals("")){
                    SignIn(email, pass);
                }else{
                    progress_bar.setVisibility(View.GONE);
                    text_view_error.setText("Você não preencheu todos os campos");
                }
            }
        });
    }

    private void SignIn(final String email, String password) {
        Log.d(TAG, "signIn:" + email);
        // [START sign_in_with_email]
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success
                            text_view_error.setText("");
                            Log.d(TAG, "signInWithEmail:success");

                            CheckUser(email);
                            CheckStatus();
                        }
                        // [START_EXCLUDE]
                        if (!task.isSuccessful()) {
                            progress_bar.setVisibility(View.GONE);
                            try {
                                throw task.getException();
                            } catch(FirebaseAuthInvalidCredentialsException e) {
                                text_view_error.setText("Email ou senha incorreta");
                            } catch(FirebaseAuthUserCollisionException e) {
                                text_view_error.setText("Tente novamente");
                            } catch(Exception e) {
                                Log.e(TAG, e.getMessage());
                            }
                        }
                    }
                });
        // [END sign_in_with_email]
    }

    public void CheckUser(final String email){

        myRefUserDeletado = mFirebaseDatabase.getReference().child("users_deletados");

        myRefUserDeletado.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    UserInformation uInfo = new UserInformation();
                    uInfo.setEmail(ds.getValue(UserInformation.class).getEmail());

                    Log.d(TAG, "showData: Email: " + uInfo.getEmail());
                    String email_database = uInfo.getEmail();
                    progress_bar.setVisibility(View.GONE);

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
                        edit_text_professor.getText().clear();
                        edit_text_senha.getText().clear();
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

    public void CheckStatus(){ ;
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
                    progress_bar.setVisibility(View.GONE);

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
                        edit_text_professor.getText().clear();
                        edit_text_senha.getText().clear();
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

    }
}
