package compsci290.edu.duke.coconutproject.activities;

import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import compsci290.edu.duke.coconutproject.utils.DatabaseManager;
import compsci290.edu.duke.coconutproject.models.DukeLocation;
import compsci290.edu.duke.coconutproject.R;
import compsci290.edu.duke.coconutproject.interfaces.MeetingImageUploadListener;
import compsci290.edu.duke.coconutproject.models.CurrentUser;
import compsci290.edu.duke.coconutproject.models.Meeting;
import compsci290.edu.duke.coconutproject.models.User;


public class CreateEventActivity extends AppCompatActivity {
    private int PICK_IMAGE_REQUEST = 1;
    private EditText editText;
    private Button upload;
    private Button takePhoto;
    private Spinner locationSpinner;
    private Spinner timeSpinner;
    static final int REQUEST_IMAGE_CAPTURE = 2;
    private Uri mImageUri;
    private String mCurrentPhotoPath;
    private Bitmap currentImage;

//    private File createTemporaryFile(String part, String ext) throws Exception
//    {
//        File tempDir= Environment.getExternalStorageDirectory();
//        tempDir=new File(tempDir.getAbsolutePath()+"/.temp/");
//        if(!tempDir.exists())
//        {
//            tempDir.mkdirs();
//        }
//        return File.createTempFile(part, ext, tempDir);
//    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    public void grabImage(ImageView imageView)
    {
        this.getContentResolver().notifyChange(mImageUri, null);
        ContentResolver cr = this.getContentResolver();
        Bitmap bitmap;
        try
        {
            bitmap = android.provider.MediaStore.Images.Media.getBitmap(cr, mImageUri);
            currentImage = bitmap;
            Bitmap finalBitmap;
            if (currentImage.getWidth() >= currentImage.getHeight()){

                finalBitmap = Bitmap.createBitmap(
                        currentImage,
                        currentImage.getWidth()/2 - currentImage.getHeight()/2,
                        0,
                        currentImage.getHeight(),
                        currentImage.getHeight()
                );

            }else{

                finalBitmap = Bitmap.createBitmap(
                        currentImage,
                        0,
                        currentImage.getHeight()/2 - currentImage.getWidth()/2,
                        currentImage.getWidth(),
                        currentImage.getWidth()
                );
            }
            imageView.setImageBitmap(finalBitmap);
            currentImage = finalBitmap;
        }
        catch (Exception e)
        {
            Toast.makeText(this, "Failed to load", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_event_activity);

        TextView textView = (TextView) findViewById(R.id.create_new_event_text);
        textView.setTypeface(MainActivity.font);


        upload = (Button) findViewById(R.id.upload_a_photo);
        takePhoto = (Button) findViewById(R.id.camera_photo_upload);

        editText = (EditText) findViewById(R.id.editText);


        locationSpinner = (Spinner) findViewById(R.id.location_spinner);
        timeSpinner = (Spinner) findViewById(R.id.location_spinner_time);
        // Create an ArrayAdapter using the string array and a default locationSpinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.LocationsToChoose, android.R.layout.simple_spinner_item);
        ArrayAdapter<CharSequence> timeAdapter = ArrayAdapter.createFromResource(this, R.array.TimesToChoose, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the locationSpinner
        locationSpinner.setAdapter(adapter);
        timeSpinner.setAdapter(timeAdapter);


        takePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                    if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
//                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, exampleUri);
//                        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);

                            // place where to store camera taken picture
                            File myPhoto = null;
                        try {
                            myPhoto = createImageFile();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        if(myPhoto != null) {
                            mImageUri = FileProvider.getUriForFile(getBaseContext(),
                                    "com.compsci290.edu.duke.coconutproject.fileprovider",
                                    myPhoto);
                        }
                            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);
                            //start camera intent
                            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                           // myPhoto.delete();

                    }
            }
        });


        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                // Show only images, no videos or anything else
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                // Always show the chooser (if there are multiple options available)
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);

            }
        });



        final ImageView deleteImageButton =(ImageView) findViewById(R.id.delete_image_button);
        deleteImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ImageView imageView = (ImageView) findViewById(R.id.user_image_upload_preview);
                imageView.setVisibility(view.GONE);
                deleteImageButton.setVisibility(View.GONE);
                upload.setVisibility(view.VISIBLE);
                takePhoto.setVisibility(view.VISIBLE);
            }
        });

        ImageView goBack = (ImageView) findViewById(R.id.create_event_back_button);
        goBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Intent backToFeed = new Intent(getBaseContext(), MainActivity.class);
              //  startActivity(backToFeed);
                finish();
            }
        });

        TextView postMyEvent = (TextView) findViewById(R.id.post_my_event);
        postMyEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                postMeeting();
                finish();
            }
        });



    }

    private void postMeeting() {
        String locationName = locationSpinner.getSelectedItem().toString();
        String time = timeSpinner.getSelectedItem().toString();
        final User currentUser = CurrentUser.currentUser;
        double randomFileNameTag = Math.random() * 100;
        String fileName = currentUser.getmUser_id() + "/" + locationName + randomFileNameTag;
        final Meeting meetingWithoutImage = new Meeting(currentUser, editText.getText().toString(), null, time, new DukeLocation(locationName), editText.getText().toString(), currentUser.getProfilePicUrl());

        // First upload photo, then get image URL to put into meeting object
        MeetingImageUploadListener uploadListener = new MeetingImageUploadListener() {
            @Override
            public void onHandleMeetingImageUpload(String imageUploadURL) {

                Meeting meetingToPost = new Meeting(currentUser, meetingWithoutImage.getTitle(), meetingWithoutImage.getAttendees(), meetingWithoutImage.getTime(), meetingWithoutImage.getLocation(), meetingWithoutImage.getImage(), meetingWithoutImage.getmBitmap(), meetingWithoutImage.getmDescription(), currentUser.getProfilePicUrl(), imageUploadURL);
                DatabaseManager.writeEventToDatabase(currentUser, meetingToPost.getMeetingInfoMap());
            }
        };

        DatabaseManager.uploadPhoto(currentImage, fileName, uploadListener);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {

            Uri uri = data.getData();

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                // Log.d(TAG, String.valueOf(bitmap));
                currentImage = bitmap;
                ImageView imageView = (ImageView) findViewById(R.id.user_image_upload_preview);
                imageView.setImageBitmap(bitmap);
                imageView.setVisibility(View.VISIBLE);
                upload.setVisibility(View.GONE);
                takePhoto.setVisibility(View.GONE);

                ImageView delteImageButton =(ImageView) findViewById(R.id.delete_image_button);
                delteImageButton.setVisibility(View.VISIBLE);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
//            Bundle extras = data.getExtras();
//
           ImageView imageView = (ImageView) findViewById(R.id.user_image_upload_preview);
      //      imageView.setImageBitmap(imageBitmap);

            this.grabImage(imageView);

            imageView.setVisibility(View.VISIBLE);

            upload.setVisibility(View.GONE);
            takePhoto.setVisibility(View.GONE);

            ImageView delteImageButton =(ImageView) findViewById(R.id.delete_image_button);
            delteImageButton.setVisibility(View.VISIBLE);


        }

    }

}
