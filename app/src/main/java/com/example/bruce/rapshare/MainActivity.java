package com.example.bruce.rapshare;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseAnalytics;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.SimpleTimeZone;


public class MainActivity extends ActionBarActivity implements ActionBar.TabListener {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;
    public static  final int TAKE_PHOTO_REQUEST = 0;
    public static  final int TAKE_VIDEO_REQUEST = 1;
    public static  final int PICK_PHOTO_REQUEST = 2;
    public static  final int PICK_VIDEO_REQUEST = 3;

    public static  final int MEDIA_TYPE_IMAGE = 4;
    public static  final int MEDIA_TYPE_VIDEO = 5;
    public static  final int FILE_SIZE_LIMIT = 1024*1024*10;//10MB
     protected Uri mMediaUri;
    public  static  final String TAG = MainActivity.class.getSimpleName();

    protected DialogInterface.OnClickListener mDialogListener = new DialogInterface.OnClickListener(){

            @Override
            public void onClick(DialogInterface dialog, int which ){
                switch (which){
                    case 0: //take pic

                        Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        mMediaUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);
                            if(mMediaUri==null){
                                //display an errors
                                Toast.makeText(MainActivity.this,R.string.error_external_storage,Toast.LENGTH_LONG).show();
                            }else{
                                takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, mMediaUri);
                                startActivityForResult(takePhotoIntent, TAKE_PHOTO_REQUEST);
                            }
                        break;
                    case 1:

                        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                        mMediaUri =getOutputMediaFileUri(MEDIA_TYPE_VIDEO);
                        startActivityForResult(takeVideoIntent,TAKE_VIDEO_REQUEST);
                        if(mMediaUri==null){
                            //display an errors
                            Toast.makeText(MainActivity.this,R.string.error_external_storage,Toast.LENGTH_LONG).show();
                        }else{
                            takeVideoIntent.putExtra(MediaStore.EXTRA_OUTPUT, mMediaUri);
                            takeVideoIntent.putExtra(MediaStore.EXTRA_DURATION_LIMIT,10 );
                            takeVideoIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY,0 );//0 for worst quality
                            startActivityForResult(takeVideoIntent,TAKE_VIDEO_REQUEST);
                        }
                        break;
                    case 2://choose  a pic
                        Intent choosePhotoIntent = new Intent(Intent.ACTION_GET_CONTENT);
                        choosePhotoIntent.setType("image/*");
                        Toast.makeText(MainActivity.this,"The selected video must be less than 10MB",Toast.LENGTH_LONG).show();
                        startActivityForResult(choosePhotoIntent, PICK_PHOTO_REQUEST);

                        break;
                    case 3:

                        Intent chooseVideoIntent = new Intent(Intent.ACTION_GET_CONTENT);
                        chooseVideoIntent.setType("video/*");
                        startActivityForResult(chooseVideoIntent,PICK_VIDEO_REQUEST);
                        break;
                }


            }

    };

    private Uri getOutputMediaFileUri(int mediaType) {
        if (isExternalStorageAvailable()) {
            // get the URI

            // 1. Get the external storage directory
            String appName = MainActivity.this.getString(R.string.app_name);
            File mediaStorageDir = new File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                    appName);

            // 2. Create our subdirectory
            if (! mediaStorageDir.exists()) {
                if (! mediaStorageDir.mkdirs()) {
                    Log.e(TAG, "Failed to create directory.");
                    return null;
                }
            }

            // 3. Create a file name
            // 4. Create the file
            File mediaFile;
            Date now = new Date();
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(now);

            String path = mediaStorageDir.getPath() + File.separator;
            if (mediaType == MEDIA_TYPE_IMAGE) {
                mediaFile = new File(path + "IMG_" + timestamp + ".jpg");
            }
            else if (mediaType == MEDIA_TYPE_VIDEO) {
                mediaFile = new File(path + "VID_" + timestamp + ".mp4");
            }
            else {
                return null;
            }

            Log.d("BRUCE", "File: " + Uri.fromFile(mediaFile));

            // 5. Return the file's URI
            return Uri.fromFile(mediaFile);
        }
        else {
            return null;
        }


    }
    private boolean isExternalStorageAvailable(){
        String state = Environment.getExternalStorageState();
        if(state.equals(Environment.MEDIA_MOUNTED)){
            return true;
        }else{
            return false;
        }
    }

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        ParseAnalytics.trackAppOpenedInBackground(getIntent());
        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser == null) {


            navigateToLogin();
    }else{
            Log.i(TAG, currentUser.getUsername());
        }
        // Set up the action bar.
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager()); // Error- SectionsPageAdapter() in  SectionsPageAdapter cannot be applied to:


        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // When swiping between different sections, select the corresponding
        // tab. We can also use ActionBar.Tab#select() to do this if we have
        // a reference to the Tab.
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by
            // the adapter. Also specify this Activity object, which implements
            // the TabListener interface, as the callback (listener) for when
            // this tab is selected.
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(mSectionsPagerAdapter.getPageTitle(i))
                            .setTabListener(this));
        }
    }


    @Override


    protected  void onActivityResult(int requestCode, int resultCode,Intent data){
            super.onActivityResult(requestCode,resultCode,data);
        //case 2,case3 选择的pic和video返回到intent data里了。
        if(resultCode == RESULT_OK) {
            //success
            if (requestCode == PICK_PHOTO_REQUEST || requestCode == PICK_VIDEO_REQUEST) {
                if (data == null) {
                    Toast.makeText(this, R.string.general_error, Toast.LENGTH_LONG).show();
                } else {
                    mMediaUri = data.getData();
                }
                if(requestCode==PICK_VIDEO_REQUEST){
                    //make file is less than 10MB
                    int fileSize=0;
                    InputStream inputStream = null;
                    try {
                        inputStream = getContentResolver().openInputStream(mMediaUri);
                        fileSize=inputStream.available();

                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "There was a problem with the selected file", Toast.LENGTH_LONG).show();
                        return;
                    }catch(IOException e){
                        Toast.makeText(this, "There was a problem with the selected file", Toast.LENGTH_LONG).show();
                        return;
                    }
                    //finally get called no matter what
                    finally{
                        try {
                            inputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if(fileSize>=FILE_SIZE_LIMIT){
                        Toast.makeText(this, "The selected file was too large, select a new file", Toast.LENGTH_LONG).show();
                        return;
                    }


                }
            } else {


            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            mediaScanIntent.setData(mMediaUri);
            sendBroadcast(mediaScanIntent);
        }

            Intent recipientsIntent =new Intent(this,RecipientsActivity.class);
            recipientsIntent.setData(mMediaUri);
            String fileType;
            if (requestCode == PICK_PHOTO_REQUEST || requestCode == TAKE_PHOTO_REQUEST) {
                fileType = ParseConstants.TYPE_IMAGE;
            }
            else {
                fileType = ParseConstants.TYPE_VIDEO;
            }

            recipientsIntent.putExtra(ParseConstants.KEY_FILE_TYPE, fileType);
            startActivity(recipientsIntent);
        }else if(resultCode != RESULT_CANCELED){
            Toast.makeText(this, R.string.general_error,Toast.LENGTH_LONG).show();
        }
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id){
            case  R.id.action_logout:
                ParseUser.logOut();
                navigateToLogin();
                break;
            case  R.id.action_edit_friends:
                Intent intent = new Intent(this, EditFriendsActivity.class);
                startActivity(intent);
                break;
            case R.id.action_camera:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setItems(R.array.camera_choices, mDialogListener);
                AlertDialog dialog = builder.create();
                dialog.show();
                break;

        }
        //noinspection SimplifiableIfStatement


        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }



    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_inbox, container, false);


            return rootView;
        }
    }

}




