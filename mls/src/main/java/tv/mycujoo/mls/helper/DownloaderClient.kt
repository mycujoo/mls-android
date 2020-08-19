package tv.mycujoo.mls.helper

import android.util.Log
import okhttp3.*
import tv.mycujoo.domain.entity.OverlayEntity
import tv.mycujoo.domain.entity.SvgData
import java.io.IOException
import java.util.*

class DownloaderClient(val okHttpClient: OkHttpClient) : IDownloaderClient {

    override fun download(
        overlayEntity: OverlayEntity,
        callback: (OverlayEntity) -> Unit
    ) {
        overlayEntity.isDownloading = true

        val svgUrl = overlayEntity.svgData!!.svgUrl!!
        val request: Request = Request.Builder().url(svgUrl).build()
        okHttpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                overlayEntity.isDownloading = false

                Log.e("downloadSVGThenCallLis", "downloadSVGThenCallListener() - onFailure()")
            }

            override fun onResponse(call: Call, response: Response) {

                if (response.isSuccessful && response.body() != null) {

                    val stringBuilder = StringBuilder()

                    val scanner = Scanner(response.body()!!.byteStream())
                    while (scanner.hasNext()) {
                        stringBuilder.append(scanner.nextLine())
                    }
                    val svgString = stringBuilder.toString()

                    overlayEntity.isDownloading = false
                    overlayEntity.isOnScreen = true

                    callback(
                        overlayEntity.copy(
                            svgData = SvgData(svgUrl, null, svgString)
                        )
                    )
                }
            }
        })
    }


}