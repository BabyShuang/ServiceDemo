// ICallback.aidl
package com.example.administrator.servicesdemo;

// Declare any non-default types here with import statements


oneway interface ICallback {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
     void notifyUpdate();
}