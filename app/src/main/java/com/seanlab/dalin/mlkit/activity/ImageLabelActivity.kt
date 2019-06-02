package com.seanlab.dalin.mlkit.activity

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
//import android.support.design.widget.BottomSheetBehavior
//import android.support.v7.widget.LinearLayoutManager
//sean

import com.google.android.material.bottomsheet.BottomSheetBehavior
import 	androidx.recyclerview.widget.LinearLayoutManager


import android.view.View
import android.widget.Toast
import androidx.annotation.MainThread
import com.google.android.gms.tasks.Task
import com.google.common.collect.ImmutableList
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.label.FirebaseVisionCloudImageLabelerOptions
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabel
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabeler
//sean
import com.google.firebase.ml.vision.objects.FirebaseVisionObject
import com.google.firebase.ml.vision.objects.FirebaseVisionObjectDetector
import com.google.firebase.ml.vision.objects.FirebaseVisionObjectDetectorOptions
import com.seanlab.dalin.mlkit.R
import com.seanlab.dalin.mlkit.adapter.ImageLabelAdapter
import com.seanlab.dalin.mlkit.md.kotlin.StaticObjectDetectionActivity
import com.seanlab.dalin.mlkit.md.kotlin.objectdetection.DetectedObject
import com.seanlab.dalin.mlkit.md.kotlin.productsearch.SearchEngine
import com.seanlab.dalin.mlkit.md.kotlin.productsearch.SearchedObject
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.layout_image_label.*
import java.util.*
import kotlin.collections.ArrayList



class ImageLabelActivity : BaseCameraActivity() {

    private var itemsList: ArrayList<Any> = ArrayList()
    private lateinit var itemAdapter: ImageLabelAdapter
    //sean new



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupBottomSheet(R.layout.layout_image_label)

        rvLabel.layoutManager = LinearLayoutManager(this)
        //sean




    }

    private fun getLabelsFromDevice(bitmap: Bitmap) {
        val image = FirebaseVisionImage.fromBitmap(bitmap)
        //val detector = FirebaseVision.getInstance().onDeviceImageLabeler
        val detector: FirebaseVisionImageLabeler by lazy {
            FirebaseVisionCloudImageLabelerOptions.Builder()
                    .build().let { options ->
                        FirebaseVision.getInstance().getCloudImageLabeler(options)
                    }
        }
        itemsList.clear()

        detector.processImage(image)
                .addOnSuccessListener {
                    // Task completed successfully
                    fabProgressCircle.hide()
                    itemsList.addAll(it)
                    itemAdapter = ImageLabelAdapter(itemsList, false)
                    Log.e(ImageLabelActivity.TAG, "itemList: "+itemsList.size)
                    rvLabel.adapter = itemAdapter
                    sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED)
                }
                .addOnFailureListener {
                    // Task failed with an exception
                    fabProgressCircle.hide()
                    Toast.makeText(baseContext,"Sorry, something went wrong!",Toast.LENGTH_SHORT).show()
                }

    }

    private fun getLabelsFromClod(bitmap: Bitmap) {
        val image = FirebaseVisionImage.fromBitmap(bitmap)
        //val detector = FirebaseVision.getInstance().getOnDeviceObjectDetector()
        val detector: FirebaseVisionImageLabeler by lazy {
            FirebaseVisionCloudImageLabelerOptions.Builder()
                    .build().let { options ->
                        FirebaseVision.getInstance().getCloudImageLabeler(options)
                    }
        }
        itemsList.clear()
        detector.processImage(image)
                .addOnSuccessListener {
                    // Task completed successfully
                    fabProgressCircle.hide()
                    itemsList.addAll(it)
                    itemAdapter = ImageLabelAdapter(itemsList, true)
                    Log.e(ImageLabelActivity.TAG, "itemList: "+itemsList.size)
                    rvLabel.adapter = itemAdapter
                    sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED)
                }
                .addOnFailureListener {
                    // Task failed with an exception
                    fabProgressCircle.hide()
                    Toast.makeText(baseContext,"Sorry, something went wrong!",Toast.LENGTH_SHORT).show()
                }
    }



    override fun onClick(v: View?) {
        fabProgressCircle.show()
        cameraView.captureImage { cameraKitImage ->
            // Get the Bitmap from the captured shot
            getLabelsFromClod(cameraKitImage.bitmap)
            runOnUiThread {
                showPreview()
                imagePreview.setImageBitmap(cameraKitImage.bitmap)
            }
        }
    }
    companion object {
        private const val TAG = "ImageLabelActivity"
        private const val MAX_IMAGE_DIMENSION = 1024
    }
}
