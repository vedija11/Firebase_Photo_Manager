package com.example.inclass11_group22;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    static final int REQUEST_IMAGE_CAPTURE = 1;
    String TAG = "demo";

    RecyclerView recyclerView;
    Button button_upload;
    ProgressBar progressBar;
    Bitmap bitmapUpload = null;
    ArrayList<String> ImageURLsList=new ArrayList<>();
    static ImageAdapter imageAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerView);
        button_upload = findViewById(R.id.button_upload);
        progressBar = findViewById(R.id.progressBar);

        FirebaseStorage storage = FirebaseStorage.getInstance();
        final StorageReference listRef = storage.getReference().child("images/");

        listRef.listAll().addOnSuccessListener(new OnSuccessListener<ListResult>() {
            @Override
            public void onSuccess(ListResult listResult) {
                for (StorageReference storeRef: listResult.getItems()) {
                    storeRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Log.d(TAG, "onSuccess: "+uri.toString());
                            ImageURLsList.add(uri.toString());
                            Log.d(TAG, "URL: "+ImageURLsList);

                            recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                            imageAdapter = new ImageAdapter(ImageURLsList, new ImageAdapter.OnItemLongClickListener() {
                                @Override
                                public void onItemLongClick(String item) {
                                    Log.d(TAG, "Deleting: "+item);
                                    ImageURLsList.remove(ImageURLsList.indexOf(item));
                                    imageAdapter.notifyDataSetChanged();
                                }
                            });

                            recyclerView.setAdapter(imageAdapter);

                        }
                    });
                }

            }
        });

        button_upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dispatchTakePictureIntent();
            }
        });
    }

    //    Upload Camera Photo to Cloud Storage....
    private void uploadImage(Bitmap photoBitmap){
        FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
        StorageReference storageReference = firebaseStorage.getReference();



        final StorageReference imageRepo = storageReference.child("images/"+ UUID.randomUUID().toString() +".jpeg");

//        Converting the Bitmap into a bytearrayOutputstream....
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        photoBitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
        byte[] data = baos.toByteArray();

        UploadTask uploadTask = imageRepo.putBytes(data);
//        uploadTask.addOnFailureListener(new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception e) {
//                Log.e(TAG, "onFailure: "+e.getMessage());
//            }
//        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
//            @Override
//            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                Log.d(TAG, "onSuccess: "+"Image Uploaded!!!");
//            }
//        });

        Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
//                return null;
                if (!task.isSuccessful()){
                    throw task.getException();
                }

                return imageRepo.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()){
                    Log.d(TAG, "Image Download URL"+ task.getResult());
                    String imageURL = task.getResult().toString();
                    Log.d(TAG, "onSuccess: "+task.getResult());
                    ImageURLsList.add(imageURL);
                    imageAdapter.notifyDataSetChanged();
                    //Picasso.get().load(imageURL).into(iv_uploadedPhoto);
                }
            }
        });

//        ProgressBar......

        uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                progressBar.setProgress((int) progress);
                System.out.println("Upload is " + progress + "% done");
            }
        });

    }

    //    TAKE PHOTO USING CAMERA...
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK){
            Bundle extras = data.getExtras();
            Bitmap bitmap = (Bitmap) extras.get("data");

            bitmapUpload = bitmap;
            uploadImage(bitmapUpload);

        }
    }
}
