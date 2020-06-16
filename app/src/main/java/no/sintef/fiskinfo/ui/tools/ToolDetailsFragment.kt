package no.sintef.fiskinfo.ui.tools

import android.content.Context
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
import no.sintef.fiskinfo.databinding.ToolDetailsFragmentBinding
import no.sintef.fiskinfo.model.fishingfacility.ToolTypeCode
import java.util.*


class ToolDetailsFragment : Fragment() {
    // PRI: Get basic working editor
    // MAYBE: use material design to improve l&f

    companion object {
        fun newInstance() = ToolDetailsFragment()
    }

    private lateinit var mViewModel: ToolsViewModel
    private var _mBinding : ToolDetailsFragmentBinding? = null;
    private lateinit var mBinding: ToolDetailsFragmentBinding //? = null

    private lateinit var mToolCodeAdapter : ToolTypeCodeArrayAdapter
    private lateinit var mEditTextFilledExposedDropdown: AutoCompleteTextView
    //private lateinit val mBinding: ToolDetailsFragmentBinding = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.tool_details_fragment, container, false)

        mToolCodeAdapter = ToolTypeCodeArrayAdapter(context, R.layout.exposed_dropdown_menu_item, ToolTypeCode.values())
        mEditTextFilledExposedDropdown = mBinding.toolDetailsTypeField

        mEditTextFilledExposedDropdown.setOnItemClickListener { parent, view, position, id -> mViewModel.setSelectedToolCode(parent.getItemAtPosition(position) as ToolTypeCode) }
        mEditTextFilledExposedDropdown.setAdapter(mToolCodeAdapter)

        mBinding.toolDetailsDateLayout.setStartIconOnClickListener  {
            val builder : MaterialDatePicker.Builder<*> = MaterialDatePicker.Builder.datePicker()
            //val currentTimeInMillis = Calendar.getInstance().timeInMillis
            //builder.setSelection(currentTimeInMillis)
            val picker : MaterialDatePicker<*> = builder.build()
            picker.addOnPositiveButtonClickListener { mViewModel.setSelectedToolDate(it as Date) }
            picker.show(fragmentManager!!, picker.toString())
        }

        return mBinding!!.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mViewModel = ViewModelProviders.of(activity!!).get(ToolsViewModel::class.java)

        mViewModel.getSelectedTool().observe(this, Observer { tool ->
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
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _mBinding = null
    }

}
