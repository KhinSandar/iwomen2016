package org.undp_iwomen.iwomen.downloadclient;

import android.app.Application;
import android.util.Log;

import com.path.android.jobqueue.JobManager;
import com.path.android.jobqueue.config.Configuration;
import com.path.android.jobqueue.log.CustomLogger;

public class MyApplication extends Application {
	private static MyApplication instance;
    private JobManager jobManager;

    public MyApplication() {
        instance = this;
    }

    public void onCreate() {
        super.onCreate();
        configureJobManager();
    }

    private void configureJobManager() {
        Configuration configuration = new Configuration.Builder(this)
        .customLogger(new CustomLogger() {
            private static final String TAG = "JOBS";
            
            public boolean isDebugEnabled() {
                return true;
            }

            public void d(String text, Object... args) {
                Log.d(TAG, String.format(text, args));
            }

            public void e(Throwable t, String text, Object... args) {
                Log.e(TAG, String.format(text, args), t);
            }

            public void e(String text, Object... args) {
                Log.e(TAG, String.format(text, args));
            }
        })
        .minConsumerCount(1)//always keep at least one consumer alive
        .maxConsumerCount(1)//up to 3 consumers at a time
        .loadFactor(1)//3 jobs per consumer
        .consumerKeepAlive(120)//wait 2 minute
        .build();
        jobManager = new JobManager(this, configuration);
    }

    public JobManager getJobManager() {
        return jobManager;
    }

    public static MyApplication getInstance() {
    	if(instance == null){
    		instance = new MyApplication();
		}
		return instance;
    }

}
