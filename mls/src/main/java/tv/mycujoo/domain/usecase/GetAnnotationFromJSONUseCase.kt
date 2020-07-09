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
           "annotations":[
              {
                 "id":"ann_1",
                 "offset":1699000,
                 "timeline_id":"tml_1",
                 "actions":[
                    {
                       "id":"f4354364q6afd",
                       "type":"show_timeline_marker",
                       "data":{
                          "color":"#ffffff",
                          "label":"Kickoff"
                       }
                    },
                    {
                       "id":"54afag35yag",
                       "type":"show_overlay",
                       "data":{
                          "custom_id":"scoreboard1",
                          "svg_url":"https://storage.googleapis.com/mycujoo-player-app.appspot.com/scoreboard_and_timer.svg",
                          "position":{
                             "top":5.0,
                             "trailing":5.0
                          },
                          "size":{
                             "width":25.0
                          },
                          "animatein_type":"fade_in",
                          "animatein_duration":30000,
                          "variable_positions":{
                             "###_HOMESCORE_###":"homeScore",
                             "###_AWAYSCORE_###":"awayScore",
                             "###_TIMER_###":"timer1"
                          }
                       }
                    }
                 ]
              },
              {
                 "id":"ann_2",
                 "offset":5891000,
                 "timeline_id":"tml_1",
                 "actions":[
                    {
                       "id":"fda43t943f9a",
                       "type":"show_timeline_marker",
                       "data":{
                          "color":"#ffff01",
                          "label":"Goal"
                       }
                    },
                    {
                       "id":"gagj9j9agj9a",
                       "type":"show_overlay",
                       "data":{
                          "svg_url":"https://storage.googleapis.com/mycujoo-player-app.appspot.com/announcement_overlay.svg",
                          "position":{
                             "bottom":10.0,
                             "leading":5.0
                          },
                          "size":{
                             "width":30.0
                          },
                          "animatein_type":"slide_from_leading",
                          "animateout_type":"slide_to_leading",
                          "animatein_duration":5000,
                          "animateout_duration":5000,
                          "duration":15000
                       }
                    }
                 ]
              },
              {
                 "id":"ann_3",
                 "offset":6600000,
                 "timeline_id":"tml_1",
                 "actions":[
                    {
                       "id":"bmb9t49bm34t",
                       "type":"show_timeline_marker",
                       "data":{
                          "color":"#de4f1f",
                          "label":"Fulltime"
                       }
                    },
                    {
                       "id":"f43f9ajf9dfjSX",
                       "type":"hide_overlay",
                       "data":{
                          "custom_id":"scoreboard1",
                          "animateout_type":"slide_to_trailing",
                          "animateout_duration":30000
                       }
                    }
                 ]
              }
           ]
        }
    """.trimIndent()
    }
}