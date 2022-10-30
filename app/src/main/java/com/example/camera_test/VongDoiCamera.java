package com.example.camera_test;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LifecycleRegistry;

public class VongDoiCamera implements LifecycleOwner {
    private LifecycleRegistry lifecycleRegistry;
    public VongDoiCamera()
    {
        lifecycleRegistry = new LifecycleRegistry(this);
        lifecycleRegistry.markState(Lifecycle.State.CREATED);
    }
    public void onResum()
    {
        lifecycleRegistry.markState(Lifecycle.State.RESUMED);
    }
    public void onStop()
    {
        lifecycleRegistry.markState(Lifecycle.State.DESTROYED);

    }

    @NonNull
    @Override
    public Lifecycle getLifecycle() {
        return lifecycleRegistry;
    }
}
