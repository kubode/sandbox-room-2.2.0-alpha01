package com.github.kubode.sandboxroom22

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import com.airbnb.epoxy.TextProp

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class SimpleTextItemView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    init {
        View.inflate(context, R.layout.simple_text_item_view, this)
    }

    private val idTextView: TextView = findViewById(R.id.id)
    private val textTextView: TextView = findViewById(R.id.text)

    var number: Long = 0 // cannot use name "id"
        @ModelProp set(value) {
            field = value
            idTextView.text = "$value"
        }
    var text: CharSequence? = null
        @TextProp set(value) {
            field = value
            textTextView.text = value
        }
}
