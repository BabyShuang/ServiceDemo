// INotifyService.aidl
package com.example.administrator.servicesdemo;

// Declare any non-default types here with import statements
import com.example.administrator.servicesdemo.ICallback;
interface INotifyService {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
      void registerCallback(ICallback cb);
      void unregisterCallback(ICallback cb);
      void sendMsg(String aString);

}