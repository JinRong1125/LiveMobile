package com.tti.tlivemobile.manager;

import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.request.retrofit.RetrofitSpiceRequest;
import com.tti.tlivelibrary.tliveservice.TliveHttpService;
import com.tti.tlivemobile.application.TliveMoblie;

/**
 * Created by dylan_liang on 2017/8/7.
 */

public class SpiceServiceManager {

    private static SpiceServiceManager spiceServiceManager;
    private static SpiceManager spiceManager;

    public static SpiceServiceManager getInstance() {
        if (spiceServiceManager == null) {
            spiceServiceManager = new SpiceServiceManager();
        }
        return spiceServiceManager;
    }

    public void startSpice() {
        spiceManager = new SpiceManager(TliveHttpService.class);
        spiceManager.start(TliveMoblie.getContext());
    }

    public void cancelRequest(RetrofitSpiceRequest... requests) {
        for (int i = 0; i < requests.length; i++) {
            if (requests[i] != null)
                spiceManager.cancel(requests[i]);
        }
    }

    public SpiceManager getSpiceManager() {
        return spiceManager;
    }
}
