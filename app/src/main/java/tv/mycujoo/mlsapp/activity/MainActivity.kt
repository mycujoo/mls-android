package tv.mycujoo.mlsapp.activity

import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import tv.mycujoo.mls.api.MyCujooLiveService
import tv.mycujoo.mlsapp.R

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val myCujooLiveStream = MyCujooLiveService.init(MyCujooLiveService.PUBLIC_KEY, this)
        myCujooLiveStream.getPlayer()?.let {
            playerWidget.setPlayer(it)

        }

        myCujooLiveStream.playView(Uri.parse("https://playlists.mycujoo.football/eu/ck8u05tfu1u090hew2kgobnud/master.m3u8"))

    }
}
