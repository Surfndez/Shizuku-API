package moe.shizuku.api;

import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;

import moe.shizuku.server.IShizukuService;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP_PREFIX;

public class ShizukuService {

    private static IShizukuService sService;

    public static void setBinder(IBinder binder) {
        sService = IShizukuService.Stub.asInterface(binder);
    }

    private static IShizukuService requireService() {
        if (getService() == null) {
            throw new IllegalStateException("Binder haven't received, check Shizuku and your code.");
        }
        return getService();
    }

    private static IShizukuService getService() {
        return sService;
    }

    public static IBinder getBinder() {
        IShizukuService service = getService();
        return service != null ? service.asBinder() : null;
    }

    public static boolean pingBinder() {
        if (getBinder() == null)
            return false;

        IBinder binder = getBinder();
        return binder != null && binder.pingBinder();
    }

    /**
     * Call {@link IBinder#transact(int, Parcel, Parcel, int)} at remote service.
     *
     * <p>How to construct the data parcel:
     * <code><br>data.writeInterfaceToken(ShizukuApiConstants.BINDER_DESCRIPTOR);
     * <br>data.writeStrongBinder(\/* binder you want to use at remote *\/);
     * <br>data.writeInt(\/* transact code you want to use *\/);
     * <br>data.writeInterfaceToken(\/* interface name of that binder *\/);
     * <br>\/* write data of the binder call you want*\/</code>
     *
     * @see SystemServiceHelper#obtainParcel(String, String, String)
     * @see SystemServiceHelper#obtainParcel(String, String, String, String)
     */
    public static void transactRemote(@NonNull Parcel data, @Nullable Parcel reply, int flags) throws RemoteException {
        requireService().asBinder().transact(ShizukuApiConstants.BINDER_TRANSACTION_transact, data, reply, flags);
    }

    /**
     * Start a new process at remote service, parameters are passed to {@link java.lang.Runtime#exec(String, String[], java.io.File)}.
     *
     * @return RemoteProcess holds the binder of remote process
     */
    public static RemoteProcess newProcess(@NonNull String[] cmd, @Nullable String[] env, @Nullable String dir) throws RemoteException {
        return new RemoteProcess(requireService().newProcess(cmd, env, dir));
    }

    /**
     * Returns uid of remote service.
     *
     * @return uid
     */
    public static int getUid() throws RemoteException {
        return requireService().getUid();
    }

    /**
     * Returns remote service version.
     *
     * @return server version
     */
    public static int getVersion() throws RemoteException {
        return requireService().getVersion();
    }

    /**
     * Check permission at remote service.
     *
     * @param permission permission name
     * @return PackageManager.PERMISSION_DENIED or PackageManager.PERMISSION_GRANTED
     */
    public static int checkPermission(String permission) throws RemoteException {
        return requireService().checkPermission(permission);
    }

    /**
     * Set token of current process. Do not call this on API 23+.
     *
     * @param token token
     * @return is token correct
     * @throws IllegalStateException call on API 23+
     */
    public static boolean setTokenPre23(String token) throws RemoteException {
        return requireService().setUidToken(token);
    }

    /**
     * Returns SELinux context of Shizuku server process.
     *
     * <p>This API is only meaningful for root app using {@link ShizukuService#newProcess(String[], String[], String)}.</p>
     *
     * <p>For adb, context should always be <code>u:r:shell:s0</code>.
     * <br>For root, context depends on su the user uses. E.g., context of Magisk is <code>u:r:magisk:s0</code>.
     * If the user's su does not allow binder calls between su and app, Shizuku will switch to context <code>u:r:shell:s0</code>.
     * </p>
     *
     * @return SELinux context
     * @since added from version 6
     */
    public static String getSELinuxContext() throws RemoteException {
        return requireService().getSELinuxContext();
    }

    /**
     * Used by manager only.
     */
    @RestrictTo(LIBRARY_GROUP_PREFIX)
    public static void setUidPermissionPre23(int uid, boolean granted) throws RemoteException {
        requireService().setUidPermissionPre23(uid, granted);
    }
}
