package com.example.kushalgupta.ultrahack;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import android.widget.Toast;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.AWSStartupHandler;
import com.amazonaws.mobile.client.AWSStartupResult;
import com.amazonaws.mobile.config.AWSConfiguration;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.s3.transferutility.*;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3DataSource;

import java.io.File;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.daimajia.slider.library.Animations.DescriptionAnimation;
import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.TextSliderView;


import static android.view.View.GONE;

public class MainActivity extends AppCompatActivity {
    final int RC_PHOTO_PICKER = 29;
    Uri selectedImageUri;
    File dcim = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
    File sourceFile = new File(new File(dcim, "Camera"), "S3.jpg");

    File fileToUpload = new File("/storage/emulated/0/DCIM/Camera/S3.jpg");
    File fileToDownload = new File("/SD card/DCIM/Camera/S3.jpg");
    AmazonS3 s3;
    TransferUtility transferUtility;
    Button uploadbt, loadDB;
    File ff;
    private Util util;
    DynamoDBMapper dynamoDBMapper;
    SliderLayout mSliderLayout;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSliderLayout = (SliderLayout) findViewById(R.id.home_fragment_slider);
        HashMap< String, Integer > file_maps = new HashMap<String, Integer>();
        file_maps.put("Pehchaan", R.drawable.pehchaan);
        file_maps.put("face detection", R.drawable.face_reco2);
        file_maps.put("Jaigarh", R.drawable.f3);
      //  file_maps.put("Jaipur Image", R.drawable.jaipur_image);

        for (String name : file_maps.keySet()) {
            TextSliderView textSliderView = new TextSliderView(getApplicationContext());
//            initialize a SliderLayout
            textSliderView
                    .description(name)
                    .image(file_maps.get(name))
                    .setScaleType(BaseSliderView.ScaleType.Fit);
//
            //add your extra information
            textSliderView.bundle(new Bundle());
            textSliderView.getBundle()
                    .putString("extra", name);
//
            mSliderLayout.addSlider(textSliderView);
        }
        mSliderLayout.setPresetTransformer(SliderLayout.Transformer.Accordion);
        mSliderLayout.setPresetIndicator(SliderLayout.PresetIndicators.Center_Bottom);
        mSliderLayout.setCustomAnimation(new DescriptionAnimation());
        mSliderLayout.setDuration(4000);


        Log.d("Tagger", sourceFile.getAbsolutePath());
        Log.d("Tagger", sourceFile.getTotalSpace() + "");
        uploadbt = findViewById(R.id.upload);
        loadDB = findViewById(R.id.load);
        AWSMobileClient.getInstance().initialize(this).execute();

//sample
        util = new Util();
        transferUtility = util.getTransferUtility(this);


        //end sample
//        AWSMobileClient.getInstance().initialize(this, new AWSStartupHandler() {
//
//            @Override
//            public void onComplete(AWSStartupResult awsStartupResult) {
//
//                // Add code to instantiate a AmazonDynamoDBClient
//                AmazonDynamoDBClient dynamoDBClient = new AmazonDynamoDBClient(AWSMobileClient.getInstance().getCredentialsProvider());
//                dynamoDBMapper = DynamoDBMapper.builder()
//                        .dynamoDBClient(dynamoDBClient)
//                        .awsConfiguration(
//                                AWSMobileClient.getInstance().getConfiguration())
//                        .build();
//
//            }
//        }).execute();


//        loadDB.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//
//                        com.example.kushalgupta.ultrahack.nosqlModel booksItem = dynamoDBMapper.load(
//                                com.example.kushalgupta.ultrahack.nosqlModel.class,
//                                "roll_number");    // Sort key (range key)
//
//                        // Item read
//                        Log.d("Books Item:", booksItem.toString());
//                    }
//                }).start();
//            }
//        });


        loadDB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, DownloadActivity.class);
                startActivity(intent);
            }
        });


    }

    public void setFileToUpload(View view) {

        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1) {
            if (grantResults[0] == PermissionChecker.PERMISSION_GRANTED) {
                Toast.makeText(this, "permission done", Toast.LENGTH_SHORT).show();
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CAMERA}, 3);


            } else {
                Toast.makeText(this, "Permission Denied. Could not proceed further", Toast.LENGTH_SHORT).show();
            }
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
                Uri photoURI = FileProvider.getUriForFile(MainActivity.this,
                      //  BuildConfig.APPLICATION_ID+ ".provider",
                        "com.example.kushalgupta.ultrahack.fileprovider",
                        image);

                //  Uri photoURI = Uri.fromFile(image);

                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                intent.putExtra("return-data", true);

                // startActivityForResult(intent, 1);
                Log.d("camera", "onRequestPermissionsResult: " + photoURI);
                //PATH = image.getAbsolutePath();

                startActivityForResult(intent,1);

                imageCaptureSuccess();
            } else {
                Toast.makeText(this, "Please allow all permissions in settings", Toast.LENGTH_SHORT).show();
            }
        }

    }

    public void imageCaptureSuccess() {

        Toast.makeText(this, "pic click success", Toast.LENGTH_SHORT).show();

    }

    public void setFileToDownload(View view) {

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
        userData.put("roll", "029");
        objectMetadata.setUserMetadata(userData);


        TransferObserver uploadObserver = transferUtility.upload(Constants.BUCKET_NAME, "meta2.jpg",
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


}


//todo
//        //  callback method to call credentialsProvider method.
//        credentialsProvider();
//
//        // callback method to call the setTransferUtility method
//        setTransferUtility();
//    }
//
//    public void credentialsProvider() {
//
//        // Initialize the Amazon Cognito credentials provider
//        CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
//                getApplicationContext(),
//                "us-east-1:f0e03c2f-43e3-4558-9ea5-6c083c43826d", // Identity Pool ID
//                Regions.US_EAST_1 // Region
//        );
//
//        setAmazonS3Client(credentialsProvider);
//    }
//
//    /**
//     * Create a AmazonS3Client constructor and pass the credentialsProvider.
//     *
//     * @param credentialsProvider
//     */
//    public void setAmazonS3Client(CognitoCachingCredentialsProvider credentialsProvider) {
//
//        // Create an S3 client
//        s3 = new AmazonS3Client(credentialsProvider);
//
//        // Set the region of your S3 bucket
//        s3.setRegion(Region.getRegion(Regions.US_EAST_1));
//
//    }
//
//    public void setTransferUtility() {
//
//        transferUtility = new TransferUtility(s3, getApplicationContext());
//    }
//
//    /**
//     * This method is used to upload the file to S3 by using TransferUtility class
//     *
//     * @param view
//     */
//    public void setFileToUpload(View view) {
//
//        Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
//        getIntent.setType("image/*");
//
//        Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//        pickIntent.setType("image/*");
//
//        Intent chooserIntent = Intent.createChooser(getIntent, "Select Image");
//        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{pickIntent});
//
//        startActivityForResult(chooserIntent, RC_PHOTO_PICKER);
//
////        TransferObserver transferObserver = transferUtility.upload(
////                "mausamrest",     /* The bucket to upload to */
////                "mm",    /* The key for the uploaded object */
////                sourceFile       /* The file where the data to upload exists */
////        );
////
////        transferObserverListener(transferObserver);
//    }
//
////    public String getRealPathFromURI(Context context, Uri contentUri) {
////        Cursor cursor = null;
////        try {
////            String[] proj = { MediaStore.Images.Media.DATA };
////            cursor = context.getContentResolver().query(contentUri,  proj, null, null, null);
////            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
////            cursor.moveToFirst();
////            return cursor.getString(column_index);
////        } finally {
////            if (cursor != null) {
////                cursor.close();
////            }
////        }
////    }
//
//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//
//        if (requestCode == RC_PHOTO_PICKER) {
//            if (resultCode == RESULT_OK) {
//                selectedImageUri = data.getData();
//                super.onActivityResult(requestCode, resultCode, data);
//
//File ff = new File(selectedImageUri.getPath());
//                TransferObserver transferObserver = transferUtility.upload(
//                        "mausamrest",     /* The bucket to upload to */
//                        "mm",    /* The key for the uploaded object */
//                        ff       /* The file where the data to upload exists */
//                );
//
//                transferObserverListener(transferObserver);
//
//            }
//        }
//    }
//
//    /**
//     * This method is used to Download the file to S3 by using transferUtility class
//     *
//     * @param view
//     **/
//    public void setFileToDownload(View view) {
//
//
//
//
//        TransferObserver transferObserver = transferUtility.download(
//                "mausamrest",     /* The bucket to download from */
//                "S3.jpg",    /* The key for the object to download */
//                sourceFile        /* The file to download the object to */
//        );
//
//        transferObserverListener(transferObserver);
//
//    }
//
//
//
//
//
//
//
//
//    /**
//     * This is listener method of the TransferObserver
//     * Within this listener method, we get status of uploading and downloading file,
//     * to display percentage of the part of file to be uploaded or downloaded to S3
//     * It displays an error, when there is a problem in  uploading or downloading file to or from S3.
//     *
//     * @param transferObserver
//     */
//
//    public void transferObserverListener(TransferObserver transferObserver) {
//
//        transferObserver.setTransferListener(new TransferListener() {
//
//            @Override
//            public void onStateChanged(int id, TransferState state) {
//                Log.e("statechange", state + " ");
//            }
//
//            @Override
//            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
//                int percentage = (int) (bytesCurrent / bytesTotal * 100);
//                Log.e("percentage", percentage + " ");
//            }
//
//            @Override
//            public void onError(int id, Exception ex) {
//                Log.e("error", "error");
//            }
//
//        });
//    }
//}