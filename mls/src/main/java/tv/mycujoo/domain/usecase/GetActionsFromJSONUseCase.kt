package tv.mycujoo.domain.usecase

import com.google.gson.Gson
import tv.mycujoo.data.entity.ActionResponse
import tv.mycujoo.domain.entity.IncrementVariableEntity
import tv.mycujoo.domain.entity.NEWActionEntity
import tv.mycujoo.domain.entity.SetVariableEntity
import tv.mycujoo.domain.mapper.ActionMapper.Companion.mapToIncrementVariableEntityList
import tv.mycujoo.domain.mapper.ActionMapper.Companion.mapToNEWActionEntityList
import tv.mycujoo.domain.mapper.ActionMapper.Companion.mapToSetVariableEntityList

class GetActionsFromJSONUseCase {

    // todo : use real use-case abstract class instead of mocking data


    companion object {

        fun result(): ActionResponse {
            return Gson().fromJson(
                sourceRawResponse,
                ActionResponse::class.java
            )
        }

        fun mappedResult(): List<NEWActionEntity> {
            return mapToNEWActionEntityList(result())
        }

        fun mappedSetVariables(): List<SetVariableEntity> {
            return mapToSetVariableEntityList(result())
        }

        fun mappedIncrementVariables(): List<IncrementVariableEntity> {
            return mapToIncrementVariableEntityList(result())
        }


        val sourceRawResponse = """
       {
            "actions": [
                {
                    "offset": 4000, 
					"id": "43faf4j59595959",
					"type": "set_variable",
					"data": {
						"name": "${"$"}homeScore",
						"value": 0,
						"type": "double", // enum: double, long, string
						"double_precision": 2
					}
				},
                {
                    "offset": 4000, 
					"id": "43faf4j59595960",
					"type": "set_variable",
					"data": {
						"name": "${"$"}awayScore",
						"value": 0,
						"type": "double", // enum: double, long, string
						"double_precision": 2
					}
				},
                {
                    "offset": 60000, 
					"id": "43faf4j59595961",
					"type": "increment_variable",
					"data": {
						"name": "${"$"}awayScore",
						"amount": 1
					}
				},
                {
                    "data": {
                        "color": "#ffffff", 
                        "label": "Kickoff"
                    }, 
                    "id": "f4354364q6afd",
                    "offset": 1000, 
                    "type": "show_timeline_marker"
                }, 
                {
                    "data": {
                        "animatein_duration": 3000, 
                        "animatein_type": "fade_in",
                        "custom_id": "scoreboard1",
                        "position": {
                            "left": 5.0, 
                            "top": 5.0
                        }, 
                        "size": {
                            "width": 25.0
                        }, 
                        "svg_url": "https://storage.googleapis.com/mycujoo-player-app.appspot.com/scoreboard_and_timer.svg", 
                        "variable_positions": {
                            "###_AWAYSCORE_###": "awayScore", 
                            "###_HOMESCORE_###": "${"$"}homeScore",
                            "###_TIMER_###": "timer1"
                        }
                    }, 
                    "id": "54afag35yag", 
                    "type": "show_overlay",
                     "offset": 3000, 
                    "timeline_id": "tml_1"
                },
                 {"data": {
                        "duration": 150000,
                         "custom_id": "scoreboard2",
                         "position": {
                             "left": 5.0, 
                             "bottom": 5.0
                         }, 
                         "size": {
                             "width": 25.0
                         }, 
                         "svg_url": "https://storage.googleapis.com/mycujoo-player-app.appspot.com/scoreboard_and_timer.svg", 
                         "variable_positions": {
                             "###_AWAYSCORE_###": "${"$"}awayScore",
                             "###_HOMESCORE_###": "${"$"}homeScore",
                             "###_TIMER_###": "timer1"
                         }
                     }, 
                     "id": "54afag35yag2", 
                     "type": "show_overlay",
                      "offset": 2000, 
                       "timeline_id": "tml_1"
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
                             "right": 5.0, 
                             "top": 5.0
                         }, 
                         "size": {
                             "width": 25.0
                         }, 
                         "svg_url": "https://storage.googleapis.com/mycujoo-player-app.appspot.com/scoreboard_and_timer.svg", 
                         "variable_positions": {
                             "###_AWAYSCORE_###": "awayScore", 
                             "###_HOMESCORE_###": "${"$"}homeScore",
                             "###_TIMER_###": "timer1"
                         }
                     }, 
                     "id": "54afag35yag3", 
                     "type": "show_overlay",
                      "offset": 5000, 
                      "timeline_id": "tml_1"
                 },
                 {
                     "data": {
                         "animatein_duration": 50000, 
                         "animatein_type": "slide_from_right",
                         "animateout_duration": 50000, 
                        "animateout_type": "slide_to_right", 
                         "custom_id": "scoreboard4",
                          "duration": 70000, 
                         "position": {
                             "right": 5.0,
                             "bottom": 5.0
                         }, 
                         "size": {
                             "width": 25.0
                         }, 
                         "svg_url": "https://storage.googleapis.com/mycujoo-player-app.appspot.com/scoreboard_and_timer.svg", 
                         "variable_positions": {
                             "###_AWAYSCORE_###": "awayScore", 
                             "###_HOMESCORE_###": "${"$"}homeScore",
                             "###_TIMER_###": "timer1"
                         }
                     }, 
                     "id": "54afag35yagp4", 
                     "type": "show_overlay",
                     "offset": 5000, 
                     "timeline_id": "tml_1"
                 }
                    ,{
                    "data": {
                        "color": "#ffff01", 
                        "label": "Goal"
                    }, 
                    "offset": 166000,
                    "id": "fda43t943f9a", 
                    "type": "show_timeline_marker"
                }, 
                {
                    "data": {
                        "animatein_duration": 5000, 
                        "animatein_type": "slide_from_left", 
                        "animateout_duration": 5000, 
                        "animateout_type": "slide_to_left", 
                        "duration": 1000000,
                        "position": {
                            "bottom": 10.0, 
                            "left": 5.0
                        }, 
                        "size": {
                            "width": 30.0
                        }, 
                        "svg_url": "https://storage.googleapis.com/mycujoo-player-app.appspot.com/announcement_overlay.svg"
                    }, 
                    "id": "gagj9j9agj9a", 
                    "type": "show_overlay",
                     "offset": 166000, 
                     "timeline_id": "tml_1"
            }
            ,{
                    "data": {
                        "color": "#de4f1f", 
                        "label": "Fulltime"
                    }, 
                    "id": "bmb9t49bm34t",
                    "offset": 566000, 
                    "type": "show_timeline_marker"
                }, 
                {
                    "data": {
                        "animateout_duration": 300, 
                        "animateout_type": "fade_out",
                        "custom_id": "scoreboard1"
                    }, 
                    "id": "f43f9ajf9dfjSX", 
                    "type": "hide_overlay",
                     "offset": 566000, 
                     "timeline_id": "tml_1"
                }
            ]
        }
    """.trimIndent()
    }
}