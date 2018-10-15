package in.com.testbook.chatapp.Activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.cocosw.bottomsheet.BottomSheet;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;
import in.com.testbook.chatapp.R;
import in.com.testbook.chatapp.Utils.MyApplication;

import static in.com.testbook.chatapp.Utils.MyApplication.PATH_BASE;

public class MyProfileActivity extends AppCompatActivity {

    private DatabaseReference mUserDatabase;
    private StorageReference mImageStorage;
    private FirebaseUser mCurrentUser;

    private CircleImageView profile_image;
    private TextView label_title;
    private static final int GALLERY_PICK = 1;
    private static final int CAMERA_PICK = 2;

    private TextInputLayout edtFirstName, edtLastName;
    private TextInputLayout mEmail;

    private ProgressDialog mProgressDialog;
    File dest_file = null,source_file=null;
    String filename = "";
    Button reg_create_btn;
    MyApplication myApplication;
    private Toolbar mToolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mImageStorage = FirebaseStorage.getInstance().getReference();
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        myApplication=new MyApplication();
        initControls();
        getuserData();
        clickListener();
    }


    private void getuserData() {
        String current_uid = mCurrentUser.getUid();
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(current_uid);
        mUserDatabase.keepSynced(true);

        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String image="";
                try{

                    String fname = dataSnapshot.child("fname").getValue().toString();
                    String lname = dataSnapshot.child("lname").getValue().toString();
                      image = dataSnapshot.child("image").getValue().toString();

                    edtFirstName.getEditText().setText(fname);
                    edtLastName.getEditText().setText(lname);
                    mEmail.getEditText().setText(mCurrentUser.getEmail());
                    mEmail.getEditText().setEnabled(false);

                }catch (Exception e){

                }




                if(!image.equals("default")) {

                    final String finalImage = image;
                    Picasso.with(MyProfileActivity.this).load(image).networkPolicy(NetworkPolicy.OFFLINE)
                            .placeholder(R.drawable.default_avatar).into(profile_image, new Callback() {
                        @Override
                        public void onSuccess() {
                        }
                        @Override
                        public void onError() {
                            Picasso.with(MyProfileActivity.this).load(finalImage).placeholder(R.drawable.default_avatar).into(profile_image);
                        }
                    });

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void initControls() {
        mToolbar = (Toolbar) findViewById(R.id.register_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Update Profile");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        profile_image = (CircleImageView) findViewById(R.id.profile_image);
        label_title=(TextView)findViewById(R.id.label_title);
        label_title.setVisibility(View.GONE);
        edtFirstName = (TextInputLayout) findViewById(R.id.register_first_name);
        edtLastName = (TextInputLayout) findViewById(R.id.register_last_name);
        mEmail = (TextInputLayout) findViewById(R.id.register_email);
        reg_create_btn=(Button)findViewById(R.id.reg_create_btn);

        mProgressDialog = new ProgressDialog(this);
    }

    private void clickListener() {

        reg_create_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateInputs();
            }
        });

        profile_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ShowImagePickerDilaog();
            }
        });
    }
    private void ShowImagePickerDilaog() {
        new BottomSheet.Builder(this, R.style.BottomSheet_StyleDialog).title(getResources().getString(R.string.app_name)).sheet(R.menu.bottom_sheet_menu).listener(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                filename = System.currentTimeMillis() + ".png";
                File img_url = new File(PATH_BASE
                        + filename);
                switch (which) {
                    case R.id.cancel:
                        break;
                    case R.id.from_album:

                        Intent int_album = new Intent(Intent.ACTION_PICK);
                        int_album.setType("image/*");
                        int_album.putExtra(MediaStore.EXTRA_OUTPUT, img_url);
                        startActivityForResult(int_album, GALLERY_PICK);

                        break;
                    case R.id.from_camera:
                        Intent cameraIntent = new Intent(
                                MediaStore.ACTION_IMAGE_CAPTURE);
                        Uri photoURI = null;
                        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
                            photoURI = Uri.fromFile(img_url);
                        } else {
                            photoURI = FileProvider.getUriForFile(MyProfileActivity.this, getString(R.string.file_provider_authority), img_url);
                        }

                        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                        startActivityForResult(cameraIntent, CAMERA_PICK);
                        break;
                }
            }
        }).show();
    }

    private void validateInputs() {

        if (TextUtils.isEmpty(edtFirstName.getEditText().getText().toString())) {
            ShowSnackBar(getString(R.string.validation_input_firstname));
        } else if (TextUtils.isEmpty(edtLastName.getEditText().getText().toString())) {
            ShowSnackBar(getString(R.string.validation_input_lastname));
        }  else {
            if (!myApplication.isInternetAvailable(MyProfileActivity.this)) {
                ShowSnackBar(getString(R.string.noInternet));

            } else {
                mProgressDialog.setTitle("Update User");
                mProgressDialog.setMessage("Please wait...");
                mProgressDialog.setCanceledOnTouchOutside(false);
                mProgressDialog.show();
                updateProfile();
            }

        }

    }

    private void ShowSnackBar(String msg) {
        View parentLayout = findViewById(android.R.id.content);
        Snackbar.make(parentLayout, msg, Snackbar.LENGTH_LONG)
                .setAction("OK", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                    }
                })
                .setActionTextColor(getResources().getColor(android.R.color.holo_red_light))
                .show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if(requestCode == GALLERY_PICK && resultCode == RESULT_OK){

            Uri selectedImage = data.getData();
            try {

                filename = System.currentTimeMillis() + ".png";

                new LongFileOperation(selectedImage, this, filename).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

            } catch (Exception anfe) {

            }

        }

        else if (requestCode == CAMERA_PICK && resultCode == RESULT_OK) {
            dest_file = new File(PATH_BASE
                    + filename);

            if (dest_file != null) {
                Picasso.with(this).load(dest_file).placeholder(R.drawable
                        .default_avatar).centerCrop().resize(200, 200).into(profile_image);
                UploadImage();
            }

        }




    }


    private void UploadImage() {
        String current_uid = mCurrentUser.getUid();
        Bitmap thumb_bitmap = new Compressor(this)
                .setMaxWidth(200)
                .setMaxHeight(200)
                .setQuality(75)
                .compressToBitmap(dest_file);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        thumb_bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        final byte[] thumb_byte = baos.toByteArray();


        StorageReference filepath = mImageStorage.child("profile_images").child(current_uid + ".jpg");
        final StorageReference thumb_filepath = mImageStorage.child("profile_images").child("thumbs").child(current_uid + ".jpg");
        Uri photoURI;

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
            photoURI = Uri.fromFile(dest_file);
        } else {
            photoURI = FileProvider.getUriForFile(MyProfileActivity.this, getString(R.string.file_provider_authority), dest_file);
        }

        filepath.putFile(photoURI).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                if(task.isSuccessful()){
                    final String download_url = task.getResult().getDownloadUrl().toString();

                    UploadTask uploadTask = thumb_filepath.putBytes(thumb_byte);
                    uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> thumb_task) {

                            String thumb_downloadUrl = thumb_task.getResult().getDownloadUrl().toString();

                            if(thumb_task.isSuccessful()){

                                Map update_hashMap = new HashMap();
                                update_hashMap.put("image", download_url);
                                update_hashMap.put("thumb_image", thumb_downloadUrl);

                                mUserDatabase.updateChildren(update_hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {

                                        if(task.isSuccessful()){
                                            mProgressDialog.dismiss();
                                            Toast.makeText(MyProfileActivity.this, "Update profile successfully.", Toast.LENGTH_LONG).show();

                                        }

                                    }
                                });


                            } else {

                                Toast.makeText(MyProfileActivity.this, "Error in updating profile .", Toast.LENGTH_LONG).show();
                                mProgressDialog.dismiss();

                            }


                        }
                    });


                } else {

                    Toast.makeText(MyProfileActivity.this, "Error in updating profile.", Toast.LENGTH_LONG).show();
                    mProgressDialog.dismiss();

                }

            }
        });
    }





    private class LongFileOperation extends AsyncTask<String, Void, String> {

        Uri uri;
        Context c;
        String strfileName = "";

        LongFileOperation(Uri uri, Context c, String strfileName) {
            this.strfileName = strfileName;
            this.uri = uri;
            this.c = c;
        }

        @Override
        protected String doInBackground(String... params) {

            File dir1 = new File(PATH_BASE);

            if (!dir1.exists()) {
                try {
                    if (dir1.mkdirs()) {
                        System.out.println("Directory created");
                    } else {
                        System.out.println("Directory NOT created");
                    }
                } catch (Exception e) {
                    // TODO: handle exception
                    Log.e("Exception====", e.getMessage());
                }
            }

            if (dir1.exists()) {

                dest_file = new File(PATH_BASE + strfileName);

                source_file = new File(getRealPathFromURI(uri, c));
                try {
                    copyFile(source_file, dest_file);
                } catch (Exception e) {
                    System.out.println("error in copying file");
                    // TODO Auto-generated catch block
                    //	e.printStackTrace();
                }

            }
            return "Executed";
        }

        @Override
        protected void onPostExecute(String result) {
            if (dest_file != null) {

                Picasso.with(MyProfileActivity.this).load(dest_file).placeholder(R.drawable
                        .default_avatar).centerCrop().resize(200, 200).into(profile_image);
                UploadImage();

            }
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }
    }

    private String getRealPathFromURI(Uri contentURI, Context c) {
        String result;
        Cursor cursor = c.getContentResolver().query(contentURI, null, null,
                null, null);
        if (cursor == null) {

            result = contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor
                    .getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            result = cursor.getString(idx);
            cursor.close();
        }
        return result;
    }

    // Copy file to destination folder from gallery
    private void copyFile(File sourceFile, File destFile) throws IOException {

        Bitmap b = BitmapFactory.decodeFile(sourceFile.getAbsolutePath());
        Bitmap out = Bitmap.createScaledBitmap(b, 400, 400, false);


        FileOutputStream fOut;
        try {
            fOut = new FileOutputStream(destFile);
            out.compress(Bitmap.CompressFormat.PNG, 100, fOut);
            fOut.flush();
            fOut.close();
            b.recycle();
            out.recycle();
        } catch (Exception e) {
        }

    }

    private void updateProfile(){

        String device_token = FirebaseInstanceId.getInstance().getToken();

        Map  userMap = new HashMap<>();
        userMap.put("fname", edtFirstName.getEditText().getText().toString());
        userMap.put("lname", edtLastName.getEditText().getText().toString());
        userMap.put("status", "Hi there I'm using testbook Chat App.");
        userMap.put("device_token", device_token);

        mUserDatabase.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if (task.isSuccessful()) {

                    if (dest_file == null) {
                        mProgressDialog.dismiss();
                        Toast.makeText(MyProfileActivity.this, "Update profile successfully.", Toast.LENGTH_LONG).show();

                    } else {
                        mImageStorage = FirebaseStorage.getInstance().getReference();
                        UploadImage();
                    }

                }

            }
        });

    }


}
