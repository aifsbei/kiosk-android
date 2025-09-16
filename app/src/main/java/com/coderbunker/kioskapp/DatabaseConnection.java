package com.coderbunker.kioskapp;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.util.Log;

import com.coderbunker.kioskapp.config.ConfigEncrypter;
import com.coderbunker.kioskapp.config.Configuration;
import com.coderbunker.kioskapp.config.encryption.EncryptionException;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class DatabaseConnection {

    public static final int FIREBASE_CONFIG_FETCH_TIMEOUT_MS = 30_000;

    private FirebaseDatabase database;

    private Handler timeoutHandler = new Handler(Looper.getMainLooper());


    public DatabaseConnection() {
        database = FirebaseDatabase.getInstance();
    }

    public void addViewer(Viewer viewer) {
        DatabaseReference reference = getFaceDetectionReference();
        String id = reference.push().getKey();
        reference.child(id).setValue(viewer);
    }

    @NonNull
    private DatabaseReference getFaceDetectionReference() {
        return database.getReference("faceDetections");
    }

    public void readViewer(final OnViewersReceived onViewersReceived) {
        DatabaseReference reference = getFaceDetectionReference();
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final List<Viewer> viewers = new ArrayList<>();
                for (DataSnapshot dsp : dataSnapshot.getChildren()) {
                    Viewer viewer = dsp.getValue(Viewer.class);
                    viewers.add((Viewer) viewer); //add result into array list
                }
                onViewersReceived.onViewersReceived(viewers);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }

        });
    }

    public void saveConfiguration(Configuration configuration) throws EncryptionException {
        DatabaseReference configurationReferences = getConfigurationReferences();
        DatabaseReference device = configurationReferences.child(configuration.getUuid());
        DatabaseReference configChild = device.child("configuration");
        String encryptedConfiguration = encryptConfiguration(configuration);
        configChild.setValue(encryptedConfiguration);

        device.child("groupLabel").setValue(configuration.getGroupLabel());
        device.child("deviceLabel").setValue(configuration.getDeviceLabel());
    }

    private String encryptConfiguration(Configuration configuration) throws EncryptionException {
        return new ConfigEncrypter().encrypt(configuration.getPassphrase(), configuration);
    }

    public interface OnViewersReceived {
        void onViewersReceived(List<Viewer> viewers);
    }

    public void getConfiguration(final String passphrase, String uuid, final Context context,
                                 final OnConfigChanged onConfigChanged, boolean callOnce) {
        if (!isFirebaseInitialized()) {
            onConfigChanged.OnConfigChanged(replaceConfigWithLocal(context));
            return;
        }

        DatabaseReference device = getConfigurationReferences().child(uuid);

        final ConfigValueEventListener listener = new ConfigValueEventListener(passphrase, context, onConfigChanged);
        if (callOnce) {
            device.addListenerForSingleValueEvent(listener);
        } else {
            device.addValueEventListener(listener);
        }

        timeoutHandler.postDelayed(
                new Runnable() {
                    @Override
                    public void run() {
                        onConfigChanged.OnConfigChanged(replaceConfigWithLocal(context));
                    }
                },
                FIREBASE_CONFIG_FETCH_TIMEOUT_MS
        );
    }

    private boolean isFirebaseInitialized() {
        try {
            FirebaseApp.getInstance();
            return true;
        } catch (IllegalStateException e) {
            return false;
        }
    }

    @NonNull
    private Configuration replaceConfigWithLocal(Context context) {
        Configuration configuration = Configuration.loadFromPreferences(context);
        try {
            saveConfiguration(configuration);
        } catch (EncryptionException e1) {
            e1.printStackTrace();
            //do nothing
        }
        return configuration;
    }

    public interface OnConfigChanged {
        void OnConfigChanged(Configuration configuration);

    }

    private Configuration decryptConfiguration(String passphrase, String value, Context context) throws EncryptionException {
        return new ConfigEncrypter().decrypt(passphrase, value, context);
    }

    private DatabaseReference getConfigurationReferences() {
        return database.getReference("configurations");
    }

    private class ConfigValueEventListener implements ValueEventListener {
        private final String passphrase;
        private final Context context;
        private final OnConfigChanged onConfigChanged;

        public ConfigValueEventListener(String passphrase, Context context, OnConfigChanged onConfigChanged) {
            this.passphrase = passphrase;
            this.context = context;
            this.onConfigChanged = onConfigChanged;
        }

        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            timeoutHandler.removeCallbacksAndMessages(null);
            String value = dataSnapshot.child("configuration").getValue(String.class);
            Configuration configuration = null;
            try {
                configuration = decryptConfiguration(passphrase, value, context);
                if (configuration == null) {
                    configuration = replaceConfigWithLocal(context);
                }
            } catch (Exception e) {
                e.printStackTrace();
                configuration = replaceConfigWithLocal(context);
            } finally {
                onConfigChanged.OnConfigChanged(configuration);
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
            timeoutHandler.removeCallbacksAndMessages(null);
            Log.e("DatabaseConnection", "database error");
        }
    }
}