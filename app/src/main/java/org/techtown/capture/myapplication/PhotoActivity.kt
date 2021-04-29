package org.techtown.capture.myapplication

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import android.Manifest
import android.content.pm.PackageManager
import android.util.Log

class PhotoActivity : AppCompatActivity() {
    var imageView: ImageView? = null
    val REQUEST_TAKE_PHOTO = 101

    //경로 변수 생성
    var mCurrentPhotoPath: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo2)
        imageView = findViewById(R.id.imageView)
        val button = findViewById<Button>(R.id.button)
        button.setOnClickListener { takePicture() }
        //Companion.loadAllPermissions(this, 101)
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED
                    && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
            } else {
                ActivityCompat.requestPermissions(this,
                        arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
            }
        }
    }

    fun takePicture() {
        //카메라 인텐트 실행하는 합수
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        //MediaStore.ACTION_IMAGE_CAPTURE = 카메라 앱 띄우는 액션 정보
        if (intent.resolveActivity(packageManager) != null) {
            var photoFile: File? = null
            try {
                photoFile = createFile()
            } catch (e: IOException) {
                //e.printStackTrace();
            }
            if (photoFile != null) {
                val uri = FileProvider.getUriForFile(this, "org.techtown.capture.myapplication.fileprovider", photoFile)
                intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
                startActivityForResult(intent, 101)
            }
        }
    }

    // 카메라로 촬영한 사진의 썸네일을 가져와 이미지뷰에 띄워줌
    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        if (requestCode == 101 && resultCode == Activity.RESULT_OK) {
            val file = File(mCurrentPhotoPath)
            val bitmap: Bitmap?
            if (Build.VERSION.SDK_INT >= 29) {
                val source = ImageDecoder.createSource(contentResolver, Uri.fromFile(file))
                try {
                    bitmap = ImageDecoder.decodeBitmap(source)
                    if (bitmap != null) {
                        imageView!!.setImageBitmap(bitmap)
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            } else {
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(contentResolver, Uri.fromFile(file))
                    if (bitmap != null) {
                        imageView!!.setImageBitmap(bitmap)
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun createFile(): File {
        //촬영한 사진 이미지 파일로 저장하는 함수
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        //파일 이름
        val filename = "JPEG_" + timeStamp + "_"
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val image = File.createTempFile(filename, ".jpg", storageDir)
        mCurrentPhotoPath = image.absolutePath
        return image
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(grantResults[0] == PackageManager.PERMISSION_GRANTED
                && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Permisson: " + permissions[0] + " was " + grantResults[0])
        }
    }

    companion object {
        private const val TAG = "CHECK"
    }
}