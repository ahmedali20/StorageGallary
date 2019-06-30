package com.example.storagegallary;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    private static final int SPAN_COUNT = 2;
    private static final int SPACING = 3;

    private static final int PICK_IMG_REQUEST = 1;
    FloatingActionButton fab;
    FirebaseDatabase database;
    FirebaseStorage storage;
    DatabaseReference databaseReference;
    StorageReference storageReference,userImageRef;
    ImageAdapter adapter;
    List<Image> images = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        userImageRef = storageReference.child("userUid").child("images");
        databaseReference = database.getReference().child("userUid").child("images");

        fab = findViewById(R.id.fab);
        recyclerView = findViewById(R.id.recyclerview);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickImage();
            }
        });

        adapter = new ImageAdapter(images, this, new ImageAdapter.OnClickListener() {
            @Override
            public void onClick(int index) {
                Image image = images.get(index);
                download(image.getImagePath());
            }
        });
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, SPAN_COUNT);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.addItemDecoration(new GridItemDecoration(SPAN_COUNT, SPACING, true));
        recyclerView.setAdapter(adapter);

        databaseReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Image image = dataSnapshot.getValue(Image.class);
                images.add(image);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void pickImage() {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, PICK_IMG_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            final Uri imageUri = data.getData();
            upload(imageUri);
        } else {
            Toast.makeText(this, "no image selected :/", Toast.LENGTH_SHORT).show();
        }
    }

    private void upload(Uri uri) {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.show();
        progressDialog.setCancelable(false);
        final String imageName = UUID.randomUUID().toString() + ".jpg";

        userImageRef.child(imageName).putFile(uri)
                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        int progress = (int) ((100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount());
                        progressDialog.setMessage(progress + "%");
                    }
                }).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull final Task<UploadTask.TaskSnapshot> task) {
                progressDialog.dismiss();
                if (task.isSuccessful()) {
                    task.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            String link = taskSnapshot.getMetadata().getReference().getDownloadUrl().toString();
                            String path = taskSnapshot.getStorage().getPath();
                            saveImagePathToDatabase(link, path);
                            Toast.makeText(MainActivity.this, "Uplaod Succeed", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Log.e("Ahmed", "upload Failed" + task.getException().getLocalizedMessage());
                    Toast.makeText(MainActivity.this, "Uplaod Failed :( " + task.getException().getLocalizedMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void saveImagePathToDatabase(String link, String path) {
        Image image = new Image(link, path);
        databaseReference.push().setValue(image);
    }

    private void download(String imagePath) {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Downloading...");
        progressDialog.show();
        progressDialog.setCancelable(false);
        String localFileName = UUID.randomUUID().toString() + ".jpg";
        final File file = new File(getFilesDir(), localFileName);
        storageReference.child(imagePath).getFile(file)
                .addOnCompleteListener(new OnCompleteListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<FileDownloadTask.TaskSnapshot> task) {
                        progressDialog.dismiss();
                        if (task.isSuccessful()) {
                            Toast.makeText(MainActivity.this, "file Downloaded to " + file.getPath(), Toast.LENGTH_SHORT).show();
                            Log.e("Ahmed", "File Downloaded to " + file.getPath());
                        } else {
                            Toast.makeText(MainActivity.this, "download Failed " + task.getException().getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                            Log.e("Ahmed", "Download Failed " + task.getException().getLocalizedMessage());
                        }
                    }
                }).addOnProgressListener(new OnProgressListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onProgress(FileDownloadTask.TaskSnapshot taskSnapshot) {
                int progress = (int) ((100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount());
                progressDialog.setMessage(progress + "%");
            }
        });
    }
}
