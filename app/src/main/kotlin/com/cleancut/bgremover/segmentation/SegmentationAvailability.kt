package com.cleancut.bgremover.segmentation

import android.content.Context
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.OptionalModuleApi
import com.google.android.gms.common.moduleinstall.ModuleInstall
import com.google.android.gms.common.moduleinstall.ModuleInstallRequest
import com.google.android.gms.common.moduleinstall.ModuleInstallStatusUpdate
import kotlinx.coroutines.tasks.await

/**
 * Gates ML Kit segmentation behind an availability/download check so a 48MP decode
 * never starts on a device that can't actually run the model - no Play Services,
 * or the on-demand module hasn't been downloaded and there's no network yet.
 */
class SegmentationAvailability(private val context: Context) {

    fun isPlayServicesAvailable(): Boolean {
        val result = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context)
        return result == ConnectionResult.SUCCESS
    }

    suspend fun ensureModuleAvailable(
        client: OptionalModuleApi,
        onDownloadProgress: (bytesDownloaded: Long, totalBytesToDownload: Long) -> Unit,
    ): Result<Unit> {
        if (!isPlayServicesAvailable()) {
            return Result.failure(BackgroundRemovalError.PlayServicesUnavailable)
        }

        return try {
            val moduleInstallClient = ModuleInstall.getClient(context)
            val availability = moduleInstallClient.areModulesAvailable(client).await()

            if (availability.areModulesAvailable()) {
                return Result.success(Unit)
            }

            val request = ModuleInstallRequest.newBuilder()
                .addApi(client)
                .setListener { update: ModuleInstallStatusUpdate ->
                    val progress = update.progressInfo
                    if (progress != null) {
                        onDownloadProgress(progress.bytesDownloaded, progress.totalBytesToDownload)
                    }
                }
                .build()

            moduleInstallClient.installModules(request).await()
            Result.success(Unit)
        } catch (t: Throwable) {
            Result.failure(BackgroundRemovalError.ModuleDownloadFailed)
        }
    }
}
