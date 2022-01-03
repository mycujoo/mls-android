package tv.mycujoo.mcls.helper

import okhttp3.*
import timber.log.Timber
import tv.mycujoo.domain.entity.Action
import tv.mycujoo.domain.entity.SvgData
import java.io.IOException
import java.util.*
import javax.inject.Inject

/**
 * Implementation of IDownloaderClient which downloads & returns SVG Data
 * @constructor takes okHttpClient which will have a Cache layer to skip downloading repeated SVGs
 * @see IDownloaderClient
 * @see SvgData
 */
class DownloaderClient @Inject constructor(
    val okHttpClient: OkHttpClient
) : IDownloaderClient {

    /**
     * download SVG & call with ready-to-parse SVGData
     * @param showOverlayAction the action which SVG url will be downloaded from
     * @param callback higher order function callback to call after download of SVG
     */
    override fun download(
        showOverlayAction: Action.ShowOverlayAction,
        callback: (Action.ShowOverlayAction) -> Unit
    ) {
        if (showOverlayAction.svgData?.svgUrl == null) {
            return
        }

        val svgUrl = showOverlayAction.svgData.svgUrl
        val request: Request = Request.Builder().url(svgUrl).build()
        okHttpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Timber.e("downloadSVGThenCallListener() - onFailure()")
            }

            override fun onResponse(call: Call, response: Response) {

                if (response.isSuccessful && response.body != null) {

                    val stringBuilder = StringBuilder()

                    val scanner = Scanner(response.body!!.byteStream())
                    while (scanner.hasNext()) {
                        stringBuilder.append(scanner.nextLine())
                    }
                    val svgString = stringBuilder.toString()

                    callback(
                        showOverlayAction.copy(
                            svgData = SvgData(svgUrl, svgString)
                        )
                    )
                }
            }
        })
    }


}