package tv.mycujoo.domain.usecase

import com.google.gson.Gson
import tv.mycujoo.domain.entity.AnnotationsSourceData
import tv.mycujoo.domain.entity.NEWAnnotationEntity
import tv.mycujoo.domain.mapper.AnnotationMapper

class GetAnnotationFromJSONUseCase {

    // todo : use real use-case abstract class instead of mocking data


    companion object {

        fun result(): AnnotationsSourceData {

            val annotationSourceData = Gson().fromJson<AnnotationsSourceData>(
                sourceRawResponse,
                AnnotationsSourceData::class.java
            )

            return annotationSourceData
        }

        fun mappedResult(): List<NEWAnnotationEntity> {
            return AnnotationMapper.mapToAnnotationEntityList(result())
        }


        val sourceRawResponse = """
       {
    "annotations": [
        {
            "actions": [
                {
                    "data": {
                        "color": "#ffffff", 
                        "label": "Kickoff"
                    }, 
                    "id": "f4354364q6afd", 
                    "type": "show_timeline_marker"
                }, 
                {
                    "data": {
                        "animatein_duration": 3000, 
                        "animatein_type": "fade_in",
                        "custom_id": "scoreboard1",
                        "position": {
                            "leading": 5.0, 
                            "top": 5.0
                        }, 
                        "size": {
                            "width": 25.0
                        }, 
                        "svg_url": "https://storage.googleapis.com/mycujoo-player-app.appspot.com/scoreboard_and_timer.svg", 
                        "variable_positions": {
                            "###_AWAYSCORE_###": "awayScore", 
                            "###_HOMESCORE_###": "homeScore", 
                            "###_TIMER_###": "timer1"
                        }
                    }, 
                    "id": "54afag35yag", 
                    "type": "show_overlay"
                },
                 {"data": {
                        "duration": 10000, 
                         "custom_id": "scoreboard2",
                         "position": {
                             "leading": 5.0, 
                             "bottom": 5.0
                         }, 
                         "size": {
                             "width": 25.0
                         }, 
                         "svg_url": "https://storage.googleapis.com/mycujoo-player-app.appspot.com/scoreboard_and_timer.svg", 
                         "variable_positions": {
                             "###_AWAYSCORE_###": "awayScore", 
                             "###_HOMESCORE_###": "homeScore", 
                             "###_TIMER_###": "timer1"
                         }
                     }, 
                     "id": "54afag35yag2", 
                     "type": "show_overlay"
                 },
                 {
                     "data": {
                         "animatein_duration": 3000, 
                         "animatein_type": "fade_in",
                         "animateout_duration": 5000, 
                        "animateout_type": "fade_out",
                         "custom_id": "scoreboard3",
                          "duration": 10000, 
                         "position": {
                             "trailing": 5.0, 
                             "top": 5.0
                         }, 
                         "size": {
                             "width": 25.0
                         }, 
                         "svg_url": "https://storage.googleapis.com/mycujoo-player-app.appspot.com/scoreboard_and_timer.svg", 
                         "variable_positions": {
                             "###_AWAYSCORE_###": "awayScore", 
                             "###_HOMESCORE_###": "homeScore", 
                             "###_TIMER_###": "timer1"
                         }
                     }, 
                     "id": "54afag35yag3", 
                     "type": "show_overlay"
                 },
                 {
                     "data": {
                         "animatein_duration": 50000, 
                         "animatein_type": "slide_from_trailing",
                         "animateout_duration": 50000, 
                        "animateout_type": "slide_to_trailing", 
                         "custom_id": "scoreboard4",
                          "duration": 50000, 
                         "position": {
                             "trailing": 5.0, 
                             "bottom": 5.0
                         }, 
                         "size": {
                             "width": 25.0
                         }, 
                         "svg_url": "https://storage.googleapis.com/mycujoo-player-app.appspot.com/scoreboard_and_timer.svg", 
                         "variable_positions": {
                             "###_AWAYSCORE_###": "awayScore", 
                             "###_HOMESCORE_###": "homeScore", 
                             "###_TIMER_###": "timer1"
                         }
                     }, 
                     "id": "54afag35yagp4", 
                     "type": "show_overlay"
                 }
            ], 
            "id": "ann_1", 
            "offset": 5000, 
            "timeline_id": "tml_1"
        }, 
        {
            "actions": [
                {
                    "data": {
                        "color": "#ffff01", 
                        "label": "Goal"
                    }, 
                    "id": "fda43t943f9a", 
                    "type": "show_timeline_marker"
                }, 
                {
                    "data": {
                        "animatein_duration": 5000, 
                        "animatein_type": "slide_from_leading", 
                        "animateout_duration": 5000, 
                        "animateout_type": "slide_to_leading", 
                        "duration": 1000000, 
                        "position": {
                            "bottom": 10.0, 
                            "leading": 5.0
                        }, 
                        "size": {
                            "width": 30.0
                        }, 
                        "svg_url": "https://storage.googleapis.com/mycujoo-player-app.appspot.com/announcement_overlay.svg"
                    }, 
                    "id": "gagj9j9agj9a", 
                    "type": "show_overlay"
                }
            ], 
            "id": "ann_2", 
            "offset": 66000, 
            "timeline_id": "tml_1"
        }, 
        {
            "actions": [
                {
                    "data": {
                        "color": "#de4f1f", 
                        "label": "Fulltime"
                    }, 
                    "id": "bmb9t49bm34t", 
                    "type": "show_timeline_marker"
                }, 
                {
                    "data": {
                        "animateout_duration": 300, 
                        "animateout_type": "fade_out", 
                        "custom_id": "scoreboard1"
                    }, 
                    "id": "f43f9ajf9dfjSX", 
                    "type": "hide_overlay"
                }
            ], 
            "id": "ann_3", 
            "offset": 850000, 
            "timeline_id": "tml_1"
        }
    ]
}
    """.trimIndent()
    }
}