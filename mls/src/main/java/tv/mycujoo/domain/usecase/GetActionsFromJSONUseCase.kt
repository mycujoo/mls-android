package tv.mycujoo.domain.usecase

import com.google.gson.Gson
import tv.mycujoo.data.entity.ActionCollections
import tv.mycujoo.data.entity.ActionResponse
import tv.mycujoo.domain.mapper.ActionMapper.Companion.mapToActionCollections

class GetActionsFromJSONUseCase {

    // todo : use real use-case abstract class instead of mocking data


    companion object {

        fun result(): ActionResponse {
            return Gson().fromJson(
                sourceRawResponse,
                ActionResponse::class.java
            )
        }

        fun mappedActionCollections(): ActionCollections {
            return mapToActionCollections(result())
        }

        private val sourceRawResponse2 = """
            {
            "actions": [
                        {
                         "data": {
                             "animatein_duration": 3000, 
                             "animatein_type": "slide_from_right",
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
                             "svg_url": "https://storage.googleapis.com/mycujoo-player-app.appspot.com/scoreboard_and_timer.svg"
                         }, 
                         "id": "54afag35yag3", 
                         "type": "show_overlay",
                          "offset": 5000, 
                          "timeline_id": "tml_1"
                            },
                        ]
            }
            """

        private val sourceRawResponse = """
       {
            "actions": [
                {
                    "offset": 2000, 
					"id": "43faf4j59595959",
					"type": "set_variable",
					"data": {
						"name": "${"$"}homeScore",
						"value": 0,
						"type": "long", // enum: double, long, string
						"double_precision": 2
					}
				},
                {
                    "offset": 2000, 
					"id": "43faf4j59595960",
					"type": "set_variable",
					"data": {
						"name": "${"$"}awayScore",
						"value": 0,
						"type": "long", // enum: double, long, string
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
                    "offset": 120000, 
					"id": "43faf4j59595961",
					"type": "increment_variable",
					"data": {
						"name": "${"$"}awayScore",
						"amount": 1
					}
				},                
                    {
                    "offset": 180000, 
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
                        "animatein_duration": 1000, 
                        "animatein_type": "slide_from_top",
                        "custom_id": "scoreboard1",
                        "position": {
                            "left": 5.0, 
                            "top": 5.0
                        }, 
                        "size": {
                            "width": 25.0
                        }, 
                        "svg_url": "https://storage.googleapis.com/mycujoo-player-app.appspot.com/scoreboard_and_timer.svg",
                         "variable_positions": ["${"$"}awayScore", "${"$"}homeScore", "${"$"}scoreboardTimer"]
                    }, 
                    "id": "54afag35yag", 
                    "type": "show_overlay",
                     "offset": 3000, 
                    "timeline_id": "tml_1"
                },
                 {"data": {
                        "duration": 990000,
                         "custom_id": "scoreboard2",
                         "position": {
                             "left": 5.0, 
                             "bottom": 5.0
                         }, 
                         "size": {
                             "width": 50.0
                         }, 
                         "svg_url": "https://storage.googleapis.com/mycujoo-player-app.appspot.com/scoreboard_and_timer.svg",
                         "variable_positions": ["${"$"}awayScore", "${"$"}homeScore", "${"$"}scoreboardTimer"]
                     }, 
                     "id": "54afag35yag2", 
                     "type": "show_overlay",
                      "offset": 5000, 
                       "timeline_id": "tml_1"
                 },
                 {
                     "data": {
                         "animatein_duration": 3000, 
                         "animatein_type": "slide_from_right",
                         "animateout_duration": 5000, 
                        "animateout_type": "slide_to_top",
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
                         "variable_positions": ["${"$"}awayScore","${"$"}homeScore", "${"$"}scoreboardTimer"]
                     }, 
                     "id": "54afag35yag3", 
                     "type": "show_overlay",
                      "offset": 5000, 
                      "timeline_id": "tml_1"
                 },
                 {
                     "data": {
                         "animatein_duration": 5000, 
                         "animatein_type": "slide_from_bottom",
                         "animateout_duration": 5000, 
                        "animateout_type": "slide_to_top", 
                         "custom_id": "scoreboard4",
                          "duration": 12000, 
                         "position": {
                             "right": 5.0,
                             "bottom": 5.0
                         }, 
                         "size": {
                             "width": 25.0
                         }, 
                         "svg_url": "https://storage.googleapis.com/mycujoo-player-app.appspot.com/scoreboard_and_timer.svg", 
                         "variable_positions": ["${"$"}awayScore", "${"$"}homeScore", "${"$"}scoreboardTimer"]
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
                    "offset": 19600,
                    "id": "fda43t943f9a", 
                    "type": "show_timeline_marker"
                }
                ,{
                    "data": {
                        "color": "#ffff01", 
                        "label": "Goal 2"
                    }, 
                    "offset": 19600,
                    "id": "fda43t943f9a", 
                    "type": "show_timeline_marker"
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
                        "animateout_duration": 3000, 
                        "animateout_type": "fade_out",
                        "custom_id": "scoreboard1"
                    }, 
                    "id": "f43f9ajf9dfjSX", 
                    "type": "hide_overlay",
                     "offset": 16000, 
                     "timeline_id": "tml_1"
                },
                {
                	"id": "bbaaaa4444sssstg",
                    "offset": 0,
                	"type": "create_timer",
                	"data": {
                		// A custom name that is defined by the customer, simply to label their timer for later referencing.
                		"name": "${"$"}scoreboardTimer",
                		// The format indicates how the ms value is textually represented. Options:
                		// - ms: Minutes (if > 0) and seconds, each separated by a colon (e.g. 40:01)
                		// - s:  Seconds
                		"format": "ms",
                		// Indicates whether the clock counts "up" or "down"
                		"direction": "up",
                		// 45 minutes, represented in milliseconds
                		"start_value": 0,
                		// Step size
                		"step": 1000,
                		// capValue is a value at which the timer should continue counting, 
                		// but the visual representation is stuck at this value.
                		// Useful for ensuring a visual representation doesn't go negative, or useful for capping e.g. at 45 minutes to prevent overtime from showing.
                		// The capValue is dependent on the direction (up/down)
                		"cap_value": -1
                	}
                },
                {
					"id": "4fdaf5tygfhfhffha",
                    "offset": 0,
					"type": "start_timer",
					"data": {
						"name": "${"$"}scoreboardTimer"
					}
				}

            ]
        }
    """.trimIndent()
    }
}