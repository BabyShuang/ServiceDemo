package com.example.administrator.servicesdemo;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class MyService extends Service {
    private static final String TAG = "MyService";
    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate: ");
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind: ");
        //返回给客户端，调用
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    private void start(String msg){
        Log.d(TAG, "start: onReceive="+msg);
        //模拟耗时操作
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //调用接口，通知其他应用
        Log.d(TAG, "start: updateData");
        updateData();

    }
    //用于保存所有的回调接口
    private final List<Callback> mCallbacks = new ArrayList<Callback>();
    //当远程对象死掉的时候，会回调binderDied放，即可以进行相应的处理
    private final class Callback implements IBinder.DeathRecipient{
        final ICallback mCallback;
        Callback(ICallback callback){
            mCallback=callback;
        }


        @Override
        public void binderDied() {
            //同步的原因是这里是一个多线程调用
            synchronized (mCallbacks){
                mCallbacks.remove(this);
            }
            if(mCallback!=null){
                //解除死亡代理
                mCallback.asBinder().unlinkToDeath(this,0);
            }

        }
    }

    //注册
    private void registerCallback(ICallback callback) {
        synchronized (mCallbacks) {
            IBinder binder = callback.asBinder();
            int size = mCallbacks.size();
            for (int i = 0; i < size; i++) {
                Callback temp = mCallbacks.get(i);
                if (binder.equals(temp.mCallback.asBinder())) {
                    // listener already added
                    return ;
                }
            }

            try {
                Callback cb = new Callback(callback);
                binder.linkToDeath(cb, 0);
                mCallbacks.add(cb);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    //解除注册
    private void unregisterCallback(ICallback callback) {
        synchronized (mCallbacks) {
            IBinder binder = callback.asBinder();
            Callback cb = null;
            int size = mCallbacks.size();
            for (int i = 0; i < size && cb == null; i++) {
                Callback temp = mCallbacks.get(i);
                if (binder.equals(temp.mCallback.asBinder())) {
                    cb = temp;
                }
            }

            if (cb != null) {
                mCallbacks.remove(cb);
                binder.unlinkToDeath(cb, 0);
            }
        }
    }

    //调用接口notifyUpdate
    private void updateData(){
        synchronized (mCallbacks){
            int size=mCallbacks.size();
            for (int i = 0; i < size; i++) {
                Callback cb = mCallbacks.get(i);
                try {
                    cb.mCallback.notifyUpdate();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private final IBinder mBinder=new ServiceImpl(this);

    //内部静态类对外部类没有引用，所以需要获取一个外部类，才能调用外部类中的方法，对外部类进行弱引用封装，是为在内存不足时释放。
    private static class ServiceImpl extends INotifyService.Stub{

        WeakReference<MyService> mService;

        ServiceImpl(MyService service){
            mService=new WeakReference<MyService>(service);
        }

        @Override
        public void registerCallback(ICallback cb) throws RemoteException {
            mService.get().registerCallback(cb);

        }

        @Override
        public void unregisterCallback(ICallback cb) throws RemoteException {
            mService.get().unregisterCallback(cb);

        }

        @Override
        public void sendMsg(String aString) throws RemoteException {
            //该接口提供给其他应该调用，其他应用给本应用发送消息，消息处理完之后，接口通知
            mService.get().start(aString);
        }

    }

}
