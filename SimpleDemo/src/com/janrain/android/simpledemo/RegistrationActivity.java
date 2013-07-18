/*
 *  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *  Copyright (c) 2013, Janrain, Inc.
 *
 *  All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without modification,
 *  are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation and/or
 *    other materials provided with the distribution.
 *  * Neither the name of the Janrain, Inc. nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 *  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
 *  ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 *  ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 */

package com.janrain.android.simpledemo;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import com.janrain.android.Jump;
import com.janrain.android.capture.CaptureApiError;
import com.janrain.android.capture.CaptureRecord;
import org.json.JSONObject;

import java.util.Map;

import static com.janrain.android.simpledemo.R.id.trad_reg_display_name;
import static com.janrain.android.simpledemo.R.id.trad_reg_email;
import static com.janrain.android.simpledemo.R.id.trad_reg_first_name;
import static com.janrain.android.simpledemo.R.id.trad_reg_last_name;
import static com.janrain.android.simpledemo.R.id.trad_reg_password;

public class RegistrationActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registration_activity);
    }

    public void register(View view) {
        String email, displayName, firstName, lastName, password;
        email = getEditTextString(trad_reg_email);
        displayName = getEditTextString(trad_reg_display_name);
        firstName = getEditTextString(trad_reg_first_name);
        lastName = getEditTextString(trad_reg_last_name);
        password = getEditTextString(trad_reg_password);
        JSONObject newUser = new JSONObject();

        Jump.registerNewUser(newUser, null, new Jump.RegistrationResultHandler() {
            public void onSuccess() {
                Toast.makeText(RegistrationActivity.this, "Registration Complete", Toast.LENGTH_LONG).show();
                finish();
            }

            public void onFailure(RegistrationError error) {
                AlertDialog.Builder adb = new AlertDialog.Builder(RegistrationActivity.this);
                if (error.captureApiError.code == CaptureApiError.FORM_VALIDATION_ERROR) {
                    Map<String, String[]> messages =
                            error.captureApiError.getLocalizedValidationErrorMessages();
                    adb.setMessage(messages.toString());
                }
            }
        });
    }

    private String getEditTextString(int layoutId) {
        return ((EditText) findViewById(layoutId)).getText().toString();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Don't need to call Jump.saveToDisk here, there's no state since the user isn't signed in until
        // after they are registered.
    }
}
