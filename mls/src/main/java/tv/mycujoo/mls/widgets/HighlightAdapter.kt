package tv.mycujoo.mls.widgets

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_highlight.view.*
import tv.mycujoo.mls.R
import tv.mycujoo.mls.entity.HighlightEntity

class HighlightAdapter(private val highlightList: List<HighlightEntity>) :
    RecyclerView.Adapter<HighlightAdapter.ViewHolder>() {

    private var clickListener: ListClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_highlight, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {

        return highlightList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.updateView(highlightList[position])
        holder.setOnClickListener(clickListener, position)
    }

    fun setOnClickListener(clickListener: ListClickListener) {
        this.clickListener = clickListener
    }


    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val titleTextView: TextView = view.highlight_item_title
        private val timeLabelTextView: TextView = view.highlightItem_timeLabelTextView

        fun updateView(highlightEntity: HighlightEntity) {
            titleTextView.text = highlightEntity.title
            timeLabelTextView.text = highlightEntity.timeLabel
        }

        fun setOnClickListener(
            clickListener: ListClickListener?,
            position: Int
        ) {
            titleTextView.setOnClickListener { clickListener?.onClick(position) }
            timeLabelTextView.setOnClickListener { clickListener?.onClick(position) }
        }

    }


}