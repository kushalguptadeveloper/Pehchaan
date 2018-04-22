package com.example.kushalgupta.ultrahack;

import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;

import java.io.File;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

public class DetailActivity extends AppCompatActivity {
    Button capture, upload;
    final int RC_PHOTO_PICKER = 29;
    Uri selectedImageUri;
    AmazonS3 s3;
    TransferUtility transferUtility;
    File ff;
   Util util;
    Uri photoURI;
    EditText roll;
    String no;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        capture = findViewById(R.id.capture);
        upload=findViewById(R.id.upload);
        roll=findViewById(R.id.et_roll);
        AWSMobileClient.getInstance().initialize(this).execute();
//
////sample
       util = new Util();
        transferUtility = util.getTransferUtility(this);
        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadFunction();
            }
        });
        capture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                captureFunction();
            }





        });
    }

    public void uploadFunction(){
        Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
        getIntent.setType("image/*");

        Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickIntent.setType("image/*");

        Intent chooserIntent = Intent.createChooser(getIntent, "Select Image");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{pickIntent});

        startActivityForResult(chooserIntent, RC_PHOTO_PICKER);
    }


    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    private String getPath(Uri uri) throws URISyntaxException {
        final boolean needToCheckUri = Build.VERSION.SDK_INT >= 19;
        String selection = null;
        String[] selectionArgs = null;
        // Uri is different in versions after KITKAT (Android 4.4), we need to
        // deal with different Uris.
        if (needToCheckUri && DocumentsContract.isDocumentUri(getApplicationContext(), uri)) {
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                return Environment.getExternalStorageDirectory() + "/" + split[1];
            } else if (isDownloadsDocument(uri)) {
                final String id = DocumentsContract.getDocumentId(uri);
                uri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
            } else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                if ("image".equals(type)) {
                    uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }
                selection = "_id=?";
                selectionArgs = new String[]{
                        split[1]
                };
            }
        }
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            String[] projection = {
                    MediaStore.Images.Media.DATA
            };
            Cursor cursor = null;
            try {
                cursor = getContentResolver()
                        .query(uri, projection, selection, selectionArgs, null);
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                if (cursor.moveToFirst()) {
                    return cursor.getString(column_index);
                }
            } catch (Exception e) {
            }
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_PHOTO_PICKER) {
            if (resultCode == RESULT_OK) {
                selectedImageUri = data.getData();
                String path = null;
                try {
                    path = getPath(selectedImageUri);
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
                //  Log.d("tag", "onActivityResult: "+selectedImageUri);
                Log.d("tag", "onActivityResult: " + path);
                ff = new File(path);

                uploadWithTransferUtility();


            }
        }
        if(requestCode == 2){
            Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
            getIntent.setType("image/*");

            Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            pickIntent.setType("image/*");

            Intent chooserIntent = Intent.createChooser(getIntent, "Select Image");
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{pickIntent});

            startActivityForResult(chooserIntent, RC_PHOTO_PICKER);
        }
    }

    public String getRealPathFromURI(Uri contentUri) {

        // can post image
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = managedQuery(contentUri,
                proj, // Which columns to return
                null,       // WHERE clause; which rows to return (all rows)
                null,       // WHERE clause selection arguments (none)
                null); // Order-by clause (ascending by name)
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();

        return cursor.getString(column_index);
    }

    private void uploadWithTransferUtility() {

//        AWSMobileClient.getInstance().initialize(this, new AWSStartupHandler() {
//            @Override
//            public void onComplete(AWSStartupResult awsStartupResult) {
//                transferUtility = TransferUtility.builder()
//                        .context(getApplicationContext())
//                        .awsConfiguration(AWSMobileClient.getInstance().getConfiguration())
//                        .s3Client(new AmazonS3Client(AWSMobileClient.getInstance().getCredentialsProvider()))
//                        .build();
//            }
//        }).execute();

//        TransferUtility transferUtility =
//                TransferUtility.builder()
//                        .context(getApplicationContext())
//                        .awsConfiguration(AWSMobileClient.getInstance().getConfiguration())
//                        .s3Client(new AmazonS3Client(AWSMobileClient.getInstance().getCredentialsProvider()))
//                        .build();


//
//        TransferUtility transferUtility =
//                TransferUtility.builder()
//                        .context(getApplicationContext())
//                        .awsConfiguration(AWSMobileClient.getInstance().getConfiguration())
//                        .s3Client(new AmazonS3Client(AWSMobileClient.getInstance().getCredentialsProvider()))
//                        .build();

//        TransferObserver uploadObserver =
//                transferUtility.upload(
//                        "Internal storage/WhatsApp/Media/WhatsApp Documents/Cognito_aws.txt",
//                        new File("/SD card/DCIM/Camera/S3.jpg"));
//        TransferObserver uploadObserver =
//                transferUtility.upload(
//                        "mausamt",
//                        ff);


        ObjectMetadata objectMetadata = new ObjectMetadata();
//objectMetadata.addUserMetadata("roll no","029");
        Map<String, String> userData = objectMetadata.getUserMetadata();
        no=roll.getText().toString();

        userData.put("roll", no);
        objectMetadata.setUserMetadata(userData);


        TransferObserver uploadObserver = transferUtility.upload(Constants.BUCKET_NAME, "chirag.jpg",
                ff, objectMetadata);
        Log.d("fileName", "uploadWithTransferUtility: " + ff.getName());
        // Attach a listener to the observer to get state update and progress notifications
        uploadObserver.setTransferListener(new TransferListener() {

            @Override
            public void onStateChanged(int id, TransferState state) {
                if (TransferState.COMPLETED == state) {
                    // Handle a completed upload.
                }
            }

            @Override
            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                float percentDonef = ((float) bytesCurrent / (float) bytesTotal) * 100;
                int percentDone = (int) percentDonef;

                Log.d("YourActivity", "ID:" + id + " bytesCurrent: " + bytesCurrent
                        + " bytesTotal: " + bytesTotal + " " + percentDone + "%");
            }

            @Override
            public void onError(int id, Exception ex) {
                // Handle errors
            }

        });

        // If you prefer to poll for the data, instead of attaching a
        // listener, check for the state and progress in the observer.
        if (TransferState.COMPLETED == uploadObserver.getState()) {
            // Handle a completed upload.
        }

        Log.d("YourActivity", "Bytes Transferrred: " + uploadObserver.getBytesTransferred());
        Log.d("YourActivity", "Bytes Total: " + uploadObserver.getBytesTotal());
    }

    public void captureFunction() {

        ActivityCompat.requestPermissions(DetailActivity.this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1) {
            if (grantResults[0] == PermissionChecker.PERMISSION_GRANTED) {
                // Toast.makeText(this, "permission done", Toast.LENGTH_SHORT).show();
                ActivityCompat.requestPermissions(DetailActivity.this, new String[]{android.Manifest.permission.CAMERA}, 3);


            } else {
                Toast.makeText(DetailActivity.this, "Permission Denied. Could not proceed further", Toast.LENGTH_SHORT).show();
            }
        }
        if(requestCode == 2){
            imageCaptureSuccess();
        }
        if (requestCode == 3) {
            if (grantResults[0] == PermissionChecker.PERMISSION_GRANTED) {


//                Intent imageIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
//                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
//
////folder stuff
//                File imagesFolder = new File(Environment.getExternalStorageDirectory(), "PehchaanCamera");
//                imagesFolder.mkdirs();
//
//                File image = new File(imagesFolder, "camera" + timeStamp + ".png");
//                Uri uriSavedImage = Uri.fromFile(image);
//                Log.d("camera", "onRequestPermissionsResult: "+uriSavedImage);
//                imageIntent.putExtra(MediaStore.EXTRA_OUTPUT, uriSavedImage);
//                startActivityForResult(imageIntent, 1);


                String path = Environment.getExternalStorageDirectory().toString() + "/Pehchaan";

                File dir = new File(path);
                if (!dir.exists()) {
                    dir.mkdir();
                }

                String filename = "Camera" + new SimpleDateFormat("yyyyMMdd_hhmmss").format(new Date()) + ".jpg";

                File image = new File(dir, filename);

                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                photoURI = FileProvider.getUriForFile(DetailActivity.this,
                        //  BuildConfig.APPLICATION_ID+ ".provider",
                        "com.example.kushalgupta.ultrahack.fileprovider",
                        image);

                //  Uri photoURI = Uri.fromFile(image);

                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                intent.putExtra("return-data", true);

                // startActivityForResult(intent, 1);
                Log.d("camera", "onRequestPermissionsResult: " + photoURI);
                //PATH = image.getAbsolutePath();

                startActivityForResult(intent, 2);


            } else {
                Toast.makeText(DetailActivity.this, "Please allow all permissions in settings", Toast.LENGTH_SHORT).show();
            }
        }

    }

    public void imageCaptureSuccess() {

        String path = null;
        try {
            path = getPath(photoURI);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        //  Log.d("tag", "onActivityResult: "+selectedImageUri);
        Log.d("tag", "onActivityResult: " + path);
        ff = new File(path);

        uploadWithTransferUtility();

        //   Toast.makeText(this, "pic click success", Toast.LENGTH_SHORT).show();

    }
}
