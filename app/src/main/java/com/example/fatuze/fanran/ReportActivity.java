package com.example.fatuze.fanran;

import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Environment;
import java.io.File;
import android.content.Intent;
import android.content.Context;
import android.util.Log;
import android.net.Uri;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Button;
import android.view.View;
import android.view.View.OnClickListener;
import android.provider.MediaStore;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;
import android.media.ThumbnailUtils;
import android.widget.TextView;
import android.widget.Toast;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.app.Activity;

public class ReportActivity extends AppCompatActivity {

    private static final String LOG_TAG = "ReportActivity";
    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
    private ImageButton mDeleteButton = null;
    private ImageView imageThumbView = null;
    private CheckBox mCheckBox1 = null;
    private CheckBox mCheckBox2 = null;
    private Button mButtonReport = null;
    private Button mButtonSave = null;
    private TextView mSiteName = null;
    private EditText mRemarkText = null;

    private Uri mPicFileUri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String siteName = null;
        setContentView(R.layout.activity_report);
        Bundle bundle = this.getIntent().getExtras();
        if(bundle != null) {
            siteName = bundle.getString("SiteName");
            Log.d(LOG_TAG, "onCreate: siteName = " + siteName);
        } else {
            Log.d(LOG_TAG, "onCreate: No siteName!");
        }
        mSiteName = (TextView) findViewById(R.id.report_site_name);
        if(mSiteName == null) {
            Log.w(LOG_TAG, "onCreate: mSiteName == null!");
        }
        else if(siteName != null){
            mSiteName.setText(siteName);
        }
        mCheckBox1 = (CheckBox) findViewById(R.id.checkBox1);  // Normal condition
        mCheckBox2 = (CheckBox) findViewById(R.id.checkBox2);  // Abnormal condition
        mCheckBox1.setOnClickListener(checkBoxClickListener);
        mCheckBox2.setOnClickListener(checkBoxClickListener);
        mCheckBox1.setOnCheckedChangeListener(checkBoxCheckedListener);
        mCheckBox2.setOnCheckedChangeListener(checkBoxCheckedListener);
        mRemarkText = (EditText) findViewById(R.id.remarkText);
        imageThumbView = (ImageView) findViewById(R.id.imageView);
        imageThumbView.setOnClickListener(takePicClickListener);
        mDeleteButton = (ImageButton) findViewById(R.id.DeleteButton);
        mDeleteButton.setOnClickListener(clearPicClickListener);
        mButtonSave = (Button) findViewById(R.id.button_save);
        mButtonSave.setOnClickListener(saveButtonClickListener);
    }


    private final OnClickListener takePicClickListener = new View.OnClickListener()
    {

        @Override
        public void onClick(View v)
        {
            Log.d(LOG_TAG, "Take Picture Button Click");
            // 利用系统自带的相机应用:拍照
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

            // create a file to save the image
            File picFile = getOutputFile(1);

            if(picFile == null) {
                Log.w(LOG_TAG, "getOutputFile return null!");
            } else {
                mPicFileUri = Uri.fromFile(picFile);
                // 此处这句intent的值设置关系到后面的onActivityResult中会进入那个分支，即关系到data是否为null，如果此处指定，则后来的data为null
                // set the image file name
                intent.putExtra(MediaStore.EXTRA_OUTPUT, mPicFileUri);
                startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
            }
        }

    };

    private final OnClickListener clearPicClickListener = new View.OnClickListener()
    {

        @Override
        public void onClick(View v)
        {
            Log.d(LOG_TAG, "Clear Picture Button Click");
            recycleBitmapOfImageView(imageThumbView);
            imageThumbView.setImageResource(android.R.color.black);
            mPicFileUri = null;
        }

    };

    private final OnClickListener checkBoxClickListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View v) {
            if(v instanceof CheckBox)
            {
                CheckBox checkbox = (CheckBox)v;
                if(checkbox.isChecked() == false){
                    checkbox.setChecked(true);
                }
            }
        }
    };

    private final OnClickListener saveButtonClickListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View v) {
            Log.d(LOG_TAG, "SaveButton clicked!");
            if(v instanceof Button)
            {
                SharedPreferences reportPreferences = getSharedPreferences("report_status", Activity.MODE_PRIVATE);
                int save_count = reportPreferences.getInt("saved_count", 0);
                Log.d(LOG_TAG, "Save_count = " + save_count);
                save_count++;
                SharedPreferences reportSave = getSharedPreferences("save_" + save_count, Activity.MODE_PRIVATE);
                SharedPreferences.Editor editor = reportSave.edit();
                editor.putString("name", mSiteName.getText().toString());
                int status = mCheckBox1.isChecked() ? 0 : 1; //0 - Normal, 1 - Abnormal
                editor.putInt("status", status);
                editor.putString("description", mRemarkText.getText().toString());
                if(null != mPicFileUri) {
                    Log.w(LOG_TAG, "mPicFileUri -- " + mPicFileUri.getPath());
                    editor.putString("pic_path_1", mPicFileUri.getPath());
                }
                editor.commit();
                SharedPreferences.Editor editor2 = reportPreferences.edit();
                editor2.putInt("saved_count", save_count);
                editor2.commit();
            }
        }
    };

    private final CompoundButton.OnCheckedChangeListener checkBoxCheckedListener = new CompoundButton.OnCheckedChangeListener()
    {
        @Override
        public void onCheckedChanged(CompoundButton button, boolean checked){
            if(checked){
                if(button == mCheckBox1) {
                    mCheckBox2.setChecked(false);
                } else if (button == mCheckBox2) {
                    mCheckBox1.setChecked(false);
                }
            }
        }
    };


    private void recycleBitmapOfImageView(ImageView view)
    {
        if(view == null)
            return;
        Drawable drawable = view.getDrawable();
        if(drawable instanceof BitmapDrawable){
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            Bitmap bitmap = bitmapDrawable.getBitmap();
            if (null != bitmap && !bitmap.isRecycled()){
                bitmap.recycle();
                bitmap = null;
            } else {
                Log.d(LOG_TAG, "recycleBitmapOfImageView: bitmap is null or already recycled!");
            }
        } else {
            Log.d(LOG_TAG, "recycleBitmapOfImageView: drawable is NOT BitmapDrawable!");
        }
    }

    //返回true 表示可用,false表示不可用
    private static boolean isSDCardExist() {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     *
     * @param path 文件夹路径
     */
    public static boolean isFileOrDirExist(String path) {
        File file = new File(path);
        //判断文件夹是否存在,如果不存在则创建文件夹
        if (!file.exists()) {
            return false;
        }
        return true;
    }

    /** Create a file Uri for saving an image or video */
    private Uri getOutputFileUri(int type)
    {
        return Uri.fromFile(getOutputFile(type));
    }

    /** Create a File for saving an image or video */
    private File getOutputFile(int type)
    {
        if(false == isSDCardExist()){
            Log.w(LOG_TAG, "No SD card founded!");
            return null;
        }

        File path = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File file = new File(path, "IMG_" + type + ".jpg");
        return file;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(LOG_TAG, "onActivityResult: requestCode: " + requestCode
                + ", resultCode: " + requestCode + ", data: " + data);
        // 如果是拍照
        if (CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE == requestCode)
        {
            Log.d(LOG_TAG, "CAPTURE_IMAGE");

            if (RESULT_OK == resultCode)
            {
                Log.d(LOG_TAG, "RESULT_OK");

                // Check if the result includes a thumbnail Bitmap
                if (data != null)
                {
                    // 没有指定特定存储路径的时候
                    Log.d(LOG_TAG, "data is NOT null, file on default position.");

                    // 指定了存储路径的时候（intent.putExtra(MediaStore.EXTRA_OUTPUT,fileUri);）
                    // Image captured and saved to fileUri specified in the
                    // Intent
                    Toast.makeText(this, "Image saved to:\n" + data.getData(),
                            Toast.LENGTH_LONG).show();

                    if (data.hasExtra("data"))
                    {
                        Bitmap thumbnail = data.getParcelableExtra("data");
                        imageThumbView.setImageBitmap(thumbnail);
                    }
                }
                else
                {
                    Log.d(LOG_TAG, "data is null, file saved on target position.");
                    // If there is no thumbnail image data, the image
                    // will have been stored in the target output URI.
                    if(null == mPicFileUri) {
                        Log.w(LOG_TAG, "mPicFileUri is null!");
                        return;
                    }
                    File imageFile = new File(mPicFileUri.getPath());
                    if(!imageFile.exists()){
                        Log.w(LOG_TAG, "Image file not exist!");
                        return;
                    }

                    // Resize the full image to fit in out image view.
                    int width = imageThumbView.getWidth();
                    int height = imageThumbView.getHeight();

                    BitmapFactory.Options factoryOptions = new BitmapFactory.Options();

                    factoryOptions.inJustDecodeBounds = true;
                    BitmapFactory.decodeFile(mPicFileUri.getPath(), factoryOptions);

                    int imageWidth = factoryOptions.outWidth;
                    int imageHeight = factoryOptions.outHeight;

                    // Determine how much to scale down the image
                    int scaleFactor = Math.min(imageWidth / width, imageHeight / height);

                    Log.d(LOG_TAG, "GetBitmap from FILE: " + mPicFileUri + " | Size " + imageWidth + "x" + imageHeight
                        + " --> " + width + "x" + height + ". Scale = " + scaleFactor);

                    // Decode the image file into a Bitmap sized to fill the
                    // View
                    factoryOptions.inJustDecodeBounds = false;
                    factoryOptions.inSampleSize = scaleFactor;

                    Bitmap bitmap = BitmapFactory.decodeFile(mPicFileUri.getPath(),
                            factoryOptions);

                    imageThumbView.setImageBitmap(bitmap);
                }
            }
            else if (resultCode == RESULT_CANCELED)
            {
                Log.d(LOG_TAG, "RESULT_CANCELED");
                // User cancelled the image capture
            }
            else
            {
                Log.w(LOG_TAG, "RESULT_FAILED! resultCode = " + resultCode);
                // Image capture failed, advise user
            }
        }
    }

    /**
     * 根据指定的图像路径和大小来获取缩略图
     * 此方法有两点好处：
     *     1. 使用较小的内存空间，第一次获取的bitmap实际上为null，只是为了读取宽度和高度，
     *        第二次读取的bitmap是根据比例压缩过的图像，第三次读取的bitmap是所要的缩略图。
     *     2. 缩略图对于原图像来讲没有拉伸，这里使用了2.2版本的新工具ThumbnailUtils，使
     *        用这个工具生成的图像不会被拉伸。
     * @param imagePath 图像的路径
     * @param width 指定输出图像的宽度
     * @param height 指定输出图像的高度
     * @return 生成的缩略图
     */
    private Bitmap getImageThumbnail(String imagePath, int width, int height) {
        Bitmap bitmap = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        // 获取这个图片的宽和高，注意此处的bitmap为null
        bitmap = BitmapFactory.decodeFile(imagePath, options);
        options.inJustDecodeBounds = false; // 设为 false
        // 计算缩放比
        int h = options.outHeight;
        int w = options.outWidth;
        int beWidth = w / width;
        int beHeight = h / height;
        int be = 1;
        if (beWidth < beHeight) {
            be = beWidth;
        } else {
            be = beHeight;
        }
        if (be <= 0) {
            be = 1;
        }
        options.inSampleSize = be;
        // 重新读入图片，读取缩放后的bitmap，注意这次要把options.inJustDecodeBounds 设为 false
        bitmap = BitmapFactory.decodeFile(imagePath, options);
        // 利用ThumbnailUtils来创建缩略图，这里要指定要缩放哪个Bitmap对象
        bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height,
                ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
        return bitmap;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        recycleBitmapOfImageView(imageThumbView);
    }

}
