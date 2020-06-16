package no.sintef.fiskinfo.ui.tools

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.datepicker.MaterialDatePicker
import no.sintef.fiskinfo.R
import no.sintef.fiskinfo.databinding.ToolDeploymentEditorFragmentBinding
import no.sintef.fiskinfo.model.fishingfacility.ToolTypeCode
import java.time.Instant
import java.util.*

class DeploymentEditorFragment: Fragment() {
    companion object {
        fun newInstance() = DeploymentEditorFragment()
    }

    private lateinit var mViewModel: DeploymentViewModel
    private var _mBinding: ToolDeploymentEditorFragmentBinding? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val mBinding get() = _mBinding!!

    private lateinit var mToolCodeAdapter : ToolTypeCodeArrayAdapter
    private lateinit var mEditTextFilledExposedDropdown: AutoCompleteTextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _mBinding = DataBindingUtil.inflate(inflater, R.layout.tool_deployment_editor_fragment, container, false)

        mToolCodeAdapter = ToolTypeCodeArrayAdapter(context, R.layout.exposed_dropdown_menu_item, ToolTypeCode.values())
        mEditTextFilledExposedDropdown = mBinding.toolDetailsTypeField

        mEditTextFilledExposedDropdown.setOnItemClickListener { parent, view, position, id -> mViewModel.toolTypeCode.value = parent.getItemAtPosition(position) as ToolTypeCode }
        mEditTextFilledExposedDropdown.setAdapter(mToolCodeAdapter)

        mBinding.toolDetailsDateLayout.setStartIconOnClickListener  {
            val builder : MaterialDatePicker.Builder<*> = MaterialDatePicker.Builder.datePicker()
            //val currentTimeInMillis = Calendar.getInstance().timeInMillis
            //builder.setSelection(currentTimeInMillis)
            val picker : MaterialDatePicker<*> = builder.build()
            picker.addOnPositiveButtonClickListener {
                var cal = Calendar.getInstance()
                cal.timeInMillis = it as Long
                mViewModel.setSetupDate( cal.time )
            }
            picker.show(fragmentManager!!, picker.toString())
        }

        return mBinding!!.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mViewModel = ViewModelProviders.of(activity!!).get(DeploymentViewModel::class.java)
        mViewModel.clearInfo()

        mBinding.deploymentviewmodel = mViewModel
/*
        mViewModel.observe(this, Observer { tool ->
            if (tool != null) {
                mBinding.tool = tool
                mBinding.toolviewmodel = mViewModel
            }
        })

        mViewModel.selectedToolCodeName.observe(this, Observer { toolCodeName ->
            if (toolCodeName != null) {
                mBinding.toolcodename = toolCodeName
            }
        })

 */
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _mBinding = null
    }

}