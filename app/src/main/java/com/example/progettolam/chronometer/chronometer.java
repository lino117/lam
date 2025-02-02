package com.example.progettolam.chronometer;

import android.content.Context;
import android.os.SystemClock;
import android.widget.Chronometer;

public class chronometer extends Chronometer {
    public chronometer(Context context) {
        super(context);
    }
    public void start() {
        super.setBase(SystemClock.elapsedRealtime());
        super.start();
    }

    @Override
    public boolean isActivated() {
        return super.isActivated();
    }
}
