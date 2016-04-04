package com.knewto.milknote;

import android.net.Uri;

import com.nuance.speechkit.PcmFormat;

/**
 * All Nuance Developers configuration parameters can be set here.
 *
 * Copyright (c) 2015 Nuance Communications. All rights reserved.
 */
public class Configuration {

    //All fields are required.
    //Your credentials can be found in your Nuance Developers portal, under "Manage My Apps".
//    public static final String APP_KEY = "7a213a719b3e60405fe22072d225895b09883f16e0c54bd9166fe6fb0aa76a84c9a4f664e256a44239c4b40be93d67f2c5a608a56b547e81c3657ccd0ce752d5";
    public static final String APP_KEY = BuildConfig.NUANCE_APP_KEY;
    public static final String APP_ID = BuildConfig.NUANCE_APP_ID;
    public static final String SERVER_HOST = BuildConfig.NUANCE_SERVER_HOST;
    public static final String SERVER_PORT = BuildConfig.NUANCE_SERVER_PORT;

    public static final Uri SERVER_URI = Uri.parse("nmsps://" + APP_ID + "@" + SERVER_HOST + ":" + SERVER_PORT);

    //Only needed if using NLU
    public static final String CONTEXT_TAG = "!NLU_CONTEXT_TAG!";

    public static final PcmFormat PCM_FORMAT = new PcmFormat(PcmFormat.SampleFormat.SignedLinear16, 16000, 1);
}


