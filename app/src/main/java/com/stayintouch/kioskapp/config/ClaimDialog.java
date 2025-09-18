package com.stayintouch.kioskapp.config;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.stayintouch.kioskapp.R;

public class ClaimDialog extends Dialog {
    public ClaimDialog(@NonNull Context context) {
        super(context, false, null);
        setContentView(R.layout.claim_dialog);

        final EditText passphraseView = findViewById(R.id.passphrase);

        Button claimBtn = findViewById(R.id.claim);
        Button cancelBtn = findViewById(R.id.cancel);

        claimBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String passphrase = passphraseView.getText().toString();
                Configuration configuration = Configuration.loadFromPreferences(getContext());
                configuration.setPassphrase(new ConfigEncrypter().hashPassphrase(passphrase));
                dismiss();
            }
        });

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }
}
