package tv.mycujoo.domain.entity

import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldContainNone
import org.amshove.kluent.shouldNotContain
import org.junit.Test
import tv.mycujoo.mcls.TestData.Companion.getSampleHideOverlayAction
import tv.mycujoo.mcls.TestData.Companion.getSampleShowOverlayActionN

class ActionActorTest {
    @Test
    fun `given ShowAction, without any HideAction, should return ADD`() {
        val showOverlayAction = getSampleShowOverlayActionN(1000L, "cid_00")


        val act =
            ActionActor().act(
                1L,
                arrayListOf(showOverlayAction),
                arrayListOf()
            )


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
            arrayListOf(showOverlayAction),
            arrayListOf(hideOverlayAction)
        )

        act.forEach {
            print("${it.first}, ")
        }


        assert(act.size == 2)
        assert(act.any { it.first == ActionActor.ActionAct.INTRO && it.second == showOverlayAction })
        assert(act.any { it.first == ActionActor.ActionAct.OUTRO && it.second == hideOverlayAction })
    }

    @Test
    fun `given ShowAction, then HideAction, should remove the ShowAction`() {
        val showOverlayAction = getSampleShowOverlayActionN(1000L, "cid_00")
        val hideOverlayAction = getSampleHideOverlayAction(1000L, "cid_00")


        val act = ActionActor().act(
            1L,
            arrayListOf(showOverlayAction),
            arrayListOf(hideOverlayAction)
        )

        act shouldNotContain showOverlayAction
    }

    @Test
    fun `given HideAction, then ShowAction, should return ADD`() {
        val hideOverlayAction = getSampleHideOverlayAction(1000L, "cid_00")
        val showOverlayAction = getSampleShowOverlayActionN(3000L, "cid_00")


        val act = ActionActor().act(
            2001L,
            arrayListOf(showOverlayAction),
            arrayListOf(hideOverlayAction)
        )


        assert(act.size == 1)
        assert(act.any { it.first == ActionActor.ActionAct.INTRO && it.second == showOverlayAction })
        assert(act.none { it.first == ActionActor.ActionAct.REMOVE })

    }

    @Test
    fun `given aforetime HideAction, should return REMOVE`() {
        val hideOverlayAction = getSampleHideOverlayAction(1000L, "cid_00")


        val act = ActionActor().act(
            2001L,
            arrayListOf(),
            arrayListOf(hideOverlayAction)
        )
        assert(act.size == 1)
        assert(act.any { it.first == ActionActor.ActionAct.REMOVE && it.second == hideOverlayAction })
    }

    @Test
    fun `given outro HideAction, should return OUTRO`() {
        val hideOverlayAction = getSampleHideOverlayAction(1000L, "cid_00")


        val act = ActionActor().act(
            1L,
            arrayListOf(),
            arrayListOf(hideOverlayAction)
        )

        assert(act.size == 1)
        assert(act.any { it.first == ActionActor.ActionAct.OUTRO && it.second == hideOverlayAction })
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
            arrayListOf(
                showOverlayActionFirst,
                showOverlayActionSecond,
                showOverlayActionThird
            ),
            arrayListOf(
                hideOverlayActionFirst,
                hideOverlayActionSecond
            )
        )
        assert(act.size == 1)
        assert(act.any { it.first == ActionActor.ActionAct.INTRO && it.second == showOverlayActionThird })
        assert(act.none { it.first == ActionActor.ActionAct.REMOVE })
    }


    @Test
    fun `given multiple ShowAction with same cid, and multiple HideAction with same cid in-between, should return list of one element`() {
        val showOverlayActionFirst = getSampleShowOverlayActionN(1000L, "cid_00")
        val hideOverlayActionFirst = getSampleHideOverlayAction(3000L, "cid_00")
        val showOverlayActionSecond = getSampleShowOverlayActionN(6000L, "cid_00")
        val hideOverlayActionSecond = getSampleHideOverlayAction(9000L, "cid_00")


        val act = ActionActor().act(
            8001L,
            arrayListOf(
                showOverlayActionFirst,
                showOverlayActionSecond
            ),
            arrayListOf(
                hideOverlayActionFirst,
                hideOverlayActionSecond
            )
        )


        act.size shouldBeEqualTo 1
//        assert(act.any { it.first == ActionActor.ActionAct.OUTRO && it.second == hideOverlayActionSecond })
//        assert(act.none { it.first == ActionActor.ActionAct.INTRO })
    }

    @Test
    fun `given HideAction, without ShowAction, should return OUTRO`() {
        val hideOverlayAction = getSampleHideOverlayAction(1000L, "cid_00")

        val act = ActionActor().act(
            1L,
            arrayListOf(),
            arrayListOf(hideOverlayAction)
        )

        println("${act.size}")
        act.forEach {
            println("${it.first}")
        }

        assert(act.size == 1)
        assert(act.any { it.first == ActionActor.ActionAct.OUTRO && it.second == hideOverlayAction })
    }

    @Test
    fun `given multiple ShowAction with same cid, without any HideAction, should return ADD with most recent one`() {
        val showOverlayActionFirst = getSampleShowOverlayActionN(1000L, "cid_00")
        val showOverlayActionSecond = getSampleShowOverlayActionN(3000L, "cid_00")
        val showOverlayActionThird = getSampleShowOverlayActionN(6000L, "cid_00")


        val act = ActionActor().act(
            5001L,
            arrayListOf(
                showOverlayActionFirst,
                showOverlayActionSecond,
                showOverlayActionThird
            ),
            arrayListOf()
        )


        assert(act.size == 1)
        assert(act.any { it.first == ActionActor.ActionAct.INTRO && it.second == showOverlayActionThird })
    }


    @Test
    fun `given multiple HideAction with same cid, without any ShowAction, should return OUTRO with most recent one`() {
        val hideOverlayActionFirst = getSampleHideOverlayAction(1000L, "cid_00")
        val hideOverlayActionSecond = getSampleHideOverlayAction(3000L, "cid_00")
        val hideOverlayActionThird = getSampleHideOverlayAction(6000L, "cid_00")


        val act = ActionActor().act(
            5001L,
            arrayListOf(),
            arrayListOf(
                hideOverlayActionFirst,
                hideOverlayActionSecond,
                hideOverlayActionThird
            )
        )


        assert(act.size == 1)
        assert(act.any { it.first == ActionActor.ActionAct.OUTRO && it.second == hideOverlayActionThird })
    }
}