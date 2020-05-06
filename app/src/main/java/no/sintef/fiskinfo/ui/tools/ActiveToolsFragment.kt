/**
 * Copyright (C) 2019 SINTEF
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package no.sintef.fiskinfo.ui.tools

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton

import no.sintef.fiskinfo.R
import no.sintef.fiskinfo.model.SnapMessage
import no.sintef.fiskinfo.model.fishingfacility.FishingFacility
import no.sintef.fiskinfo.model.fishingfacility.FiskInfoProfileDTO
import no.sintef.fiskinfo.ui.snap.SnapRecyclerViewAdapter

/**
 * A fragment for showing the active tools.
 */
class ActiveToolsFragment : Fragment(), ToolsRecyclerViewAdapter.OnToolInteractionListener {  //, SnapRecyclerViewAdapter.OnSnapInteractionListener {

    private val mViewModel: ToolsViewModel by activityViewModels()

    private var mAdapter: ToolsRecyclerViewAdapter? = null
    private var mSwipeLayout: SwipeRefreshLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val profile = mViewModel.getProfile()
        profile?.observe(this,

            Observer<FiskInfoProfileDTO> { pro  ->
                val test = pro.fiskinfoProfile
                test.toString()
            }

        )

        val tools = mViewModel!!.getConfirmedTools()

        tools?.observe(this,
            Observer<List<FishingFacility>> { confirmed ->
                mAdapter!!.setTools(confirmed)
                if (mSwipeLayout != null)
                    mSwipeLayout!!.isRefreshing = false
            })





        //val box = if (mIsInbox) mViewModel!!.getConfirmedTools() else mViewModel!!.getUnconfirmedTools()

/*        box?.observe(this,
            Observer { snaps ->
                mAdapter!!.setSnaps(snaps)
                if (mSwipeLayout != null)
                    mSwipeLayout!!.isRefreshing = false
            })
*/
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.tool_list_fragment, container, false)

        val listView = view.findViewById<RecyclerView>(R.id.tool_list)
        val fab = view.findViewById<FloatingActionButton>(R.id.fab)
        fab.visibility = View.GONE // No fab for confirmed tools

        val context = view.context
        listView.layoutManager = LinearLayoutManager(context)
        mAdapter = ToolsRecyclerViewAdapter(this, true)
        listView.adapter = mAdapter

        mSwipeLayout = view.findViewById(R.id.toollistswipelayout) as SwipeRefreshLayout
        //swipeLayout.setProgressBackgroundColorSchemeResource(R.color.colorBrn);

//        if (mIsInbox) // Refresh only supported on inbox as outbox is currently only local on phone
//            mSwipeLayout!!.setOnRefreshListener { mViewModel!!.refreshInboxContent() }

        return view
    }
/*
    override fun onViewSnapClicked(v: View, snap: SnapMessage?) {
        mViewModel!!.selectSnap(snap, mIsInbox)
        Navigation.findNavController(v).navigate(R.id.action_fragment_snap_to_snapDetailFragment)
    }

    override fun onViewSnapInMapClicked(v: View, snap: SnapMessage?) {
        val toast = Toast.makeText(this.context, "Not yet implemented!", Toast.LENGTH_SHORT)
        toast.show()
    }

*/
    companion object {
        fun newInstance() = ActiveToolsFragment()
    }

    override fun onViewToolClicked(v: View, tool: FishingFacility?) {
        TODO("Not yet implemented")
    }
}
