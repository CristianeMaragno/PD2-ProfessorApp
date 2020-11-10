package com.cristianerm.pd2professor;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.Date;

public class DiarioActivity extends AppCompatActivity {

    Toolbar toolbar_diario;
    EditText mensagemDiario;
    Button escolher_imagem;
    ImageView imagem;
    Button enviar;
    TextView mensagemError;
    ProgressBar mProgressBar;

    private static final int PICK_IMAGE_REQUEST = 1;

    private static final String TAG = "DiarioActivity";

    private FirebaseDatabase mFirebaseDatase;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference myRef;
    private StorageReference mStorageRef;

    private Uri mImageUri;
    private StorageTask mUploadTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diario);

        toolbar_diario = (Toolbar) findViewById(R.id.tool_bar_diario);
        setSupportActionBar(toolbar_diario);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar_diario.setTitle("");
        toolbar_diario.setSubtitle("");

        mensagemDiario = (EditText) findViewById(R.id.editTextDiario);
        enviar = (Button) findViewById(R.id.buttonDiario);
        escolher_imagem = (Button) findViewById(R.id.button_choose_image);
        imagem = (ImageView) findViewById(R.id.image_view_diario);
        mensagemError = (TextView) findViewById(R.id.textErrorDiario);
        mProgressBar = findViewById(R.id.progressBarDiario);
        mProgressBar.setVisibility(View.GONE);

        mStorageRef = FirebaseStorage.getInstance().getReference("uploads_diario");
        mFirebaseDatase = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();

        toolbar_diario.setNavigationOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent i;
                i = new Intent(DiarioActivity.this, MenuActivity.class);
                startActivity(i);
            }
        });

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

        escolher_imagem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();

            }
        });

        enviar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mensagem = mensagemDiario.getText().toString();

                if(!mensagem.equals("")) {
                    mensagemError.setText("");
                    mProgressBar.setVisibility(View.VISIBLE);
                    uploadFile();
                }else{
                    mensagemError.setText("O campo de texto está vazio");
                }

            }
        });


    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            mImageUri = data.getData();
            Picasso.get().load(mImageUri).into(imagem);
        }
    }

    private String getFileExtension(Uri uri) {
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    private void uploadFile(){
        if (mImageUri != null) {
            final StorageReference fileReference = mStorageRef.child(System.currentTimeMillis()
                    + "." + getFileExtension(mImageUri));

            fileReference.putFile(mImageUri).continueWithTask(
                    new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                        @Override
                        public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                            if (!task.isSuccessful()) {
                                throw task.getException(); }
                            return fileReference.getDownloadUrl();
                        } })
                    .addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if (task.isSuccessful()) { Uri downloadUri = task.getResult();

                                Date currentTime = Calendar.getInstance().getTime();
                                String data_atual = currentTime.toString();
                                String hora_e_data = "Hora: "+data_atual.substring(11,19) + " Data: " + data_atual.substring(4,10);

                                String mensagem = mensagemDiario.getText().toString().trim();
                                String url_image = downloadUri.toString();

                                Upload_diario upload = new Upload_diario(hora_e_data, mensagem, url_image);

                                String uploadId = myRef.push().getKey();
                                myRef.child(uploadId).setValue(upload);

                                mensagemDiario.getText().clear();
                                mProgressBar.setVisibility(View.GONE);
                                Toast.makeText(DiarioActivity.this, "Upload successful", Toast.LENGTH_LONG).show();
                            }
                            else { Toast.makeText(DiarioActivity.this, "upload failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(DiarioActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });

        } else {
            Toast.makeText(DiarioActivity.this, "Este post não terá imagem", Toast.LENGTH_SHORT).show();

            Date currentTime = Calendar.getInstance().getTime();
            String data_atual = currentTime.toString();
            String hora_e_data = "Hora: "+data_atual.substring(11,19) + " Data: " + data_atual.substring(4,10);

            String mensagem = mensagemDiario.getText().toString().trim();
            String url_image = "No image";

            Upload_diario upload = new Upload_diario(hora_e_data, mensagem, url_image);

            String uploadId = myRef.push().getKey();
            myRef.child(uploadId).setValue(upload);

            mensagemDiario.getText().clear();
            mProgressBar.setVisibility(View.GONE);
        }
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
