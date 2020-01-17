package com.androiddevs.locationtrackerjava;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import java.util.concurrent.ExecutionException;

import static android.content.ContentValues.TAG;

public class LocationWorker extends Worker {

    public LocationWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    private void updateLocationOnFirestore(String uid, GeoPoint geoPoint) {
        CollectionReference userCollection = FirebaseFirestore.getInstance()
                .collection("users");
        userCollection.document(uid).update("location", geoPoint)
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: An exception occured: " + e.toString());
                    }
                });
    }

    @NonNull
    @Override
    public Result doWork() {
        final String uid = getInputData().getString("uid");

        FusedLocationProviderClient client = LocationServices.getFusedLocationProviderClient(getApplicationContext());
        try {
            Tasks.await(client.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    GeoPoint geoPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
                    updateLocationOnFirestore(uid, geoPoint);
                }
            }));
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return Result.success();
    }
}
