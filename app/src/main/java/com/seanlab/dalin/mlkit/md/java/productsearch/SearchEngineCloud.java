/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.seanlab.dalin.mlkit.md.java.productsearch;


import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.label.FirebaseVisionCloudImageLabelerOptions;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabel;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabeler;
import com.seanlab.dalin.mlkit.md.common.FrameMetadata;
import com.seanlab.dalin.mlkit.md.common.GraphicOverlay;
import com.seanlab.dalin.mlkit.md.java.VisionProcessorBase;
import com.seanlab.dalin.mlkit.md.java.cloudimagelabeling.CloudLabelGraphic;
import com.seanlab.dalin.mlkit.md.java.objectdetection.DetectedObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

//import com.google.firebase.ml.md.java.objectdetection.DetectedObject;
import com.seanlab.dalin.mlkit.md.common.FrameMetadata;
import com.seanlab.dalin.mlkit.md.common.GraphicOverlay;
import com.seanlab.dalin.mlkit.md.java.cloudimagelabeling.CloudLabelGraphic;

/** A fake search engine to help simulate the complete work flow. */
public class SearchEngineCloud  extends VisionProcessorBase<List<FirebaseVisionImageLabel>> {

  private static final String TAG = "SearchEngineCloud";

    private final FirebaseVisionImageLabeler detector;
    private List<String> cloudlabelsStr;

    private Bitmap searchbitmap;
    

    public interface SearchResultListener {
    void onSearchCompleted(DetectedObject object, List<Product> productList);
  }

  private final RequestQueue searchRequestQueue;
  private final ExecutorService requestCreationExecutor;

  public SearchEngineCloud(Context context) {
    searchRequestQueue = Volley.newRequestQueue(context);
    requestCreationExecutor = Executors.newSingleThreadExecutor();

      FirebaseVisionCloudImageLabelerOptions.Builder optionsBuilder =
              new FirebaseVisionCloudImageLabelerOptions.Builder();

      detector = FirebaseVision.getInstance().getCloudImageLabeler(optionsBuilder.build());
  }

  public void search(DetectedObject object, SearchResultListener listener) {
    // Crops the object image out of the full image is expensive, so do it off the UI thread.


    Tasks.call(requestCreationExecutor, () -> createRequest(object))
        .addOnSuccessListener(
                productRequest -> searchRequestQueue.add(productRequest.setTag(TAG))

        )
        .addOnFailureListener(
            e -> {
              Log.e(TAG, "Failed to create product search request!", e);
              // Remove the below dummy code after your own product search backed hooked up.
              List<Product> productList = new ArrayList<>();
              for (int i = 0; i < 10; i++) {
                productList.add(
                    new Product( "", "Product title " + i, "Product subtitle " + i));
              }
              listener.onSearchCompleted(object, productList);
            });

  }

  private  JsonObjectRequest createRequest(DetectedObject searchingObject) throws Exception {
    byte[] objectImageData = searchingObject.getImageData();

    if (objectImageData == null) {
      throw new Exception("Failed to get object image data!");
    }

    // Hooks up with your own product search backend here.
    Log.e(TAG, "SEAN:image lenght" +objectImageData.length);
    //inputBitmap=searchingObject.getBitmap();
      List<Product> productList = new ArrayList<>();
      for (int i = 0; i < 10; i++) {
          productList.add(
                  new Product( "", "Product title " + i, "Product subtitle " + i));
      }


    throw new Exception("Hooks up with your own product search backend.");
  }


  public void shutdown() {
    searchRequestQueue.cancelAll(TAG);
    requestCreationExecutor.shutdown();
  }

    @Override
    protected Task<List<FirebaseVisionImageLabel>> detectInImage(FirebaseVisionImage image) {
        return detector.processImage(image);
    }

    @Override
    protected void onSuccess(
            @Nullable Bitmap originalCameraImage,
            @NonNull List<FirebaseVisionImageLabel> labels,
            @NonNull FrameMetadata frameMetadata,
            @NonNull GraphicOverlay graphicOverlay) {
        graphicOverlay.clear();
        Log.d(TAG, "cloud label size: " + labels.size());
        List<String> labelsStr = new ArrayList<>();
        for (int i = 0; i < labels.size(); ++i) {
            FirebaseVisionImageLabel label = labels.get(i);
            Log.d(TAG, "cloud label: " + label);
            if (label.getText() != null) {
                labelsStr.add((label.getText()));
            }
        }
        /*
        CloudLabelGraphic cloudLabelGraphic = new CloudLabelGraphic(graphicOverlay, labelsStr);
        graphicOverlay.add(cloudLabelGraphic);
        graphicOverlay.postInvalidate();
        */
    }

    @Override
    protected void onFailure(@NonNull Exception e) {
        Log.e(TAG, "Cloud Label detection failed " + e);
    }


}
