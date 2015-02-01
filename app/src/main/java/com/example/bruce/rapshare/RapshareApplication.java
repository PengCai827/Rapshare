package com.example.bruce.rapshare;

import android.app.Application;

import com.parse.Parse;

/**
 * Created by Bruce on 1/31/15.
 */
public class RapshareApplication extends Application{
    @Override
    public void  onCreate(){
        super.onCreate();
        Parse.enableLocalDatastore(this);

        Parse.initialize(this, "sNvBfpCRnpWrwZ1Oh7KOYNsn9RJTWnJveYet8xvp", "0cRogQaRzvV34qM33M6Dc4VM9S9q90IKOOUCfqNq");

    }


}
