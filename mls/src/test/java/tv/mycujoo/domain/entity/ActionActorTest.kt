package tv.mycujoo.domain.entity

import org.junit.Test
import tv.mycujoo.mls.TestData.Companion.getSampleHideOverlayAction
import tv.mycujoo.mls.TestData.Companion.getSampleShowOverlayActionN

class ActionActorTest {
    @Test
    fun `given ShowAction, without any HideAction, should return ADD`() {
        val showOverlayAction = getSampleShowOverlayActionN(1000L, "cid_00")


        val act =
            ActionActor().act(1L, mutableMapOf(Pair("cid_00", showOverlayAction)), mutableMapOf())
        assert(act.size == 1)
        assert(act[0].first == ActionActor.ActionAct.INTRO)
        assert(act[0].second == showOverlayAction)
    }

    @Test
    fun `given ShowAction, with unrelated HideAction, should return both`() {
        val showOverlayAction = getSampleShowOverlayActionN(1000L, "cid_00")
        val hideOverlayAction = getSampleHideOverlayAction(1000L, "cid_01") // cid is different!

        val act = ActionActor().act(
            1L,
            mutableMapOf(Pair("cid_00", showOverlayAction)),
            mutableMapOf(Pair("cid_01", hideOverlayAction))
        )
        assert(act.size == 2)
        assert(act.any { it.first == ActionActor.ActionAct.INTRO && it.second == showOverlayAction })
        assert(act.any { it.first == ActionActor.ActionAct.REMOVE && it.second == hideOverlayAction })
    }

    @Test
    fun `given ShowAction, then HideAction, should return REMOVE`() {
        val showOverlayAction = getSampleShowOverlayActionN(1000L, "cid_00")
        val hideOverlayAction = getSampleHideOverlayAction(1000L, "cid_00")


        val act = ActionActor().act(
            1L,
            mutableMapOf(Pair("cid_00", showOverlayAction)),
            mutableMapOf(Pair("cid_00", hideOverlayAction))
        )
        assert(act.size == 1)
        assert(act.any { it.first == ActionActor.ActionAct.REMOVE && it.second == hideOverlayAction })
        assert(act.none { it.first == ActionActor.ActionAct.INTRO })

    }

    @Test
    fun `given HideAction, then ShowAction, should return ADD`() {
        val hideOverlayAction = getSampleHideOverlayAction(1000L, "cid_00")
        val showOverlayAction = getSampleShowOverlayActionN(3000L, "cid_00")


        val act = ActionActor().act(
            2001L,
            mutableMapOf(Pair("cid_00", showOverlayAction)),
            mutableMapOf(Pair("cid_00", hideOverlayAction))
        )
        assert(act.size == 1)
        assert(act.any { it.first == ActionActor.ActionAct.INTRO && it.second == showOverlayAction })
        assert(act.none { it.first == ActionActor.ActionAct.REMOVE })

    }


    @Test
    fun `given multiple ShowAction with same cid, and multiple HideAction with same cid in-between, should return ADD with latest ShowAction`() {
        val showOverlayActionFirst = getSampleShowOverlayActionN(1000L, "cid_00")
        val hideOverlayActionFirst = getSampleHideOverlayAction(3000L, "cid_00")
        val showOverlayActionSecond = getSampleShowOverlayActionN(6000L, "cid_00")
        val hideOverlayActionSecond = getSampleHideOverlayAction(9000L, "cid_00")
        val showOverlayActionThird = getSampleShowOverlayActionN(12000L, "cid_00")


        val act = ActionActor().act(
            11001L,
            mutableMapOf(
                Pair("cid_00", showOverlayActionFirst),
                Pair("cid_00", showOverlayActionSecond),
                Pair("cid_00", showOverlayActionThird)
            ),
            mutableMapOf(
                Pair("cid_00", hideOverlayActionFirst),
                Pair("cid_00", hideOverlayActionSecond)
            )
        )
        assert(act.size == 1)
        assert(act.any { it.first == ActionActor.ActionAct.INTRO && it.second == showOverlayActionThird })
        assert(act.none { it.first == ActionActor.ActionAct.REMOVE })
    }


    @Test
    fun `given multiple ShowAction with same cid, and multiple HideAction with same cid in-between, should return REMOVE with latest HideAction`() {
        val showOverlayActionFirst = getSampleShowOverlayActionN(1000L, "cid_00")
        val hideOverlayActionFirst = getSampleHideOverlayAction(3000L, "cid_00")
        val showOverlayActionSecond = getSampleShowOverlayActionN(6000L, "cid_00")
        val hideOverlayActionSecond = getSampleHideOverlayAction(9000L, "cid_00")


        val act = ActionActor().act(
            8001L,
            mutableMapOf(
                Pair("cid_00", showOverlayActionFirst),
                Pair("cid_00", showOverlayActionSecond)
            ),
            mutableMapOf(
                Pair("cid_00", hideOverlayActionFirst),
                Pair("cid_00", hideOverlayActionSecond)
            )
        )
        assert(act.size == 1)
        assert(act.any { it.first == ActionActor.ActionAct.REMOVE && it.second == hideOverlayActionSecond })
        assert(act.none { it.first == ActionActor.ActionAct.INTRO })
    }

    @Test
    fun `given HideAction, without ShowAction, should return REMOVE`() {
        val hideOverlayAction = getSampleHideOverlayAction(1000L, "cid_00")

        val act = ActionActor().act(
            1L,
            mutableMapOf(),
            mutableMapOf(Pair("cid_00", hideOverlayAction))
        )
        assert(act.size == 1)
        assert(act.any { it.first == ActionActor.ActionAct.REMOVE && it.second == hideOverlayAction })
    }

    @Test
    fun `given multiple ShowAction with same cid, without any HideAction, should return ADD with most recent one`() {
        val showOverlayActionFirst = getSampleShowOverlayActionN(1000L, "cid_00")
        val showOverlayActionSecond = getSampleShowOverlayActionN(3000L, "cid_00")
        val showOverlayActionThird = getSampleShowOverlayActionN(6000L, "cid_00")


        val act = ActionActor().act(
            5001L,
            mutableMapOf(
                Pair("cid_00", showOverlayActionFirst),
                Pair("cid_00", showOverlayActionSecond),
                Pair("cid_00", showOverlayActionThird)
            ),
            mutableMapOf()
        )
        assert(act.size == 1)
        assert(act.any { it.first == ActionActor.ActionAct.INTRO && it.second == showOverlayActionThird })
    }


    @Test
    fun `given multiple HideAction with same cid, without any ShowAction, should return REMOVE with most recent one`() {
        val hideOverlayActionFirst = getSampleHideOverlayAction(1000L, "cid_00")
        val hideOverlayActionSecond = getSampleHideOverlayAction(3000L, "cid_00")
        val hideOverlayActionThird = getSampleHideOverlayAction(6000L, "cid_00")


        val act = ActionActor().act(
            5001L,
            mutableMapOf(),
            mutableMapOf(
                Pair("cid_00", hideOverlayActionFirst),
                Pair("cid_00", hideOverlayActionSecond),
                Pair("cid_00", hideOverlayActionThird)
            )
        )
        assert(act.size == 1)
        assert(act.any { it.first == ActionActor.ActionAct.REMOVE && it.second == hideOverlayActionThird })
    }
}