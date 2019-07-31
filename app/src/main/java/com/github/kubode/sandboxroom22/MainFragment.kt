package com.github.kubode.sandboxroom22

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment

class MainFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.main_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<ViewGroup>(R.id.container).apply {
            addButton<PrePackagedDatabaseFragment>()
            addButton<SchemaDefaultValuesFragment>()
            addButton<ManyToManyRelationsFragment>()
            addButton<OneToOneRelationsFragment>()
            addButton<TargetEntityFragment>()
        }
    }

    private inline fun <reified F : Fragment> ViewGroup.addButton() {
        addView(Button(context).apply {
            text = F::class.java.simpleName
            isAllCaps = false
            setOnClickListener {
                requireFragmentManager().beginTransaction()
                    .replace(R.id.container, F::class.java.newInstance())
                    .addToBackStack(null)
                    .commit()
            }
        })
    }
}
