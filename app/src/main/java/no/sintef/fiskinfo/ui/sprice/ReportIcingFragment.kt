package no.sintef.fiskinfo.ui.sprice

import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentResultListener
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.logEvent
import no.sintef.fiskinfo.R
import no.sintef.fiskinfo.databinding.SpriceReportIcingFragmentBinding
import no.sintef.fiskinfo.model.sprice.IcingReportHourEnum
import no.sintef.fiskinfo.model.sprice.MaxMiddleWindTimeEnum
import no.sintef.fiskinfo.repository.OrapRepository
import no.sintef.fiskinfo.ui.tools.*
import no.sintef.fiskinfo.ui.tools.LocationDmsDialogFragment.LocationDmsDialogListener
import no.sintef.fiskinfo.util.DMSLocation
import java.time.Instant
import java.util.*

class ReportIcingFragment : LocationRecyclerViewAdapter.OnLocationInteractionListener, FragmentResultListener,
    Fragment(),
    LocationDmsDialogListener {

    companion object {
        fun newInstance() = ReportIcingFragment()
    }

    private lateinit var mViewModel: ReportIcingViewModel
    private lateinit var mLocationViewModel: LocationViewModel
    private lateinit var locAdapter: LocationRecyclerViewAdapter

    private lateinit var mMaxMiddleWindAdapter: maxMiddleWindTimeArrayAdapter
    private lateinit var mReportingHourAdapter: DropDownMenuArrayAdapter<IcingReportHourEnum>
    private lateinit var icingDetailsDropdown: AutoCompleteTextView
    private lateinit var mSynopHourDropdown: AutoCompleteTextView
    private var _mBinding: SpriceReportIcingFragmentBinding? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val mBinding get() = _mBinding!!

    private lateinit var mFirebaseAnalytics: FirebaseAnalytics

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(requireContext())
        _mBinding = SpriceReportIcingFragmentBinding.inflate(inflater, container, false)

        initMaxMiddleWindDropDown()
        initReportingHourDropDown()
//        setHasOptionsMenu(true)

        mBinding.icingReportDateField.setOnClickListener {
            val builder: MaterialDatePicker.Builder<Long> = MaterialDatePicker.Builder.datePicker()
            builder.setSelection(mViewModel.synopTime.value!!.time)
            Log.e("setSelection", "Reporting time: ${mViewModel.synopTime.value!!.time}")

            val picker: MaterialDatePicker<*> = builder.build()
            picker.addOnPositiveButtonClickListener {
                val cal = Calendar.getInstance()
                cal.timeInMillis = it as Long
                mViewModel.setObservationDate(cal.time)
            }
            picker.show(parentFragmentManager, picker.toString())
        }

        mBinding.toolPositionRecyclerView.layoutManager = LinearLayoutManager(context)
        locAdapter = LocationRecyclerViewAdapter(this)
        mBinding.toolPositionRecyclerView.adapter = locAdapter

        return mBinding.root
    }

//    @Deprecated("Deprecated in Java")
//    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
//        inflater.inflate(R.menu.sprice_report_icing_menu, menu)
//
//        super.onCreateOptionsMenu(menu, inflater)
//    }

//    @Deprecated("Deprecated in Java")
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        mViewModel.reportingTime.value = Date.from(Instant.now())
//        val calendar = Calendar.getInstance()
//        calendar.time = mViewModel.synopTime.value!!
//        calendar.set(Calendar.HOUR_OF_DAY, mViewModel.getSynopHourAsInt())
//        calendar.set(Calendar.MINUTE, 0)
//        calendar.set(Calendar.SECOND, 0)
//        calendar.set(Calendar.MILLISECOND, 0)
//        mViewModel.synopTime.value = calendar.time
//        Log.w("Request", mViewModel.getIcingReportBody().getRequestBodyForReportSubmissionAsString())
//        Log.e(
//            "onOptionsItemSelected", "\nSynop date: ${mViewModel.synopTime.value}, synop time: ${mViewModel.synopHourSelect.value}, reporting time: ${mViewModel.reportingTime.value}, synop unix: ${calendar.time.time}\n" +
//                    "air temp: ${mViewModel.airTemperature.value}, sea temp: ${mViewModel.seaTemperature.value}, icing thickness: ${mViewModel.vesselIcingThickness.value},\n" +
//                    "${mViewModel.maxMiddleWindTime.value?.getFormValue()},\n" +
//                    "location: (lat: ${mViewModel.location.value?.latitude}, lon: ${mViewModel.location.value?.longitude})"
//        )
//        return true
//
//        if (item.itemId == R.id.send_icing_report_action) {
//            val result = OrapRepository.getInstance(requireContext()).SendIcingReport(mViewModel.getIcingReportBody())
//
//            result.observe(this) {
//                if (it.success) {
//                    mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT) {
//                        param(FirebaseAnalytics.Param.CONTENT_TYPE, "Send icing report, success")
//                        param(FirebaseAnalytics.Param.SCREEN_NAME, "Icing report")
//                        param(FirebaseAnalytics.Param.SCREEN_CLASS, "ReportIcingFragment")
//                    }
//
//                    val text = getString(R.string.icing_report_sent)
//                    val toast = Toast.makeText(this.requireActivity(), text, Toast.LENGTH_SHORT)
//                    toast.show()
////                    mViewModel.clear()
//                    Navigation.findNavController(this.requireView()).navigateUp()
//                } else {
//                    mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT) {
//                        param(FirebaseAnalytics.Param.CONTENT_TYPE, "Send icing report, error")
//                        param(FirebaseAnalytics.Param.SCREEN_NAME, "Icing report")
//                        param(FirebaseAnalytics.Param.SCREEN_CLASS, "ReportIcingFragment")
//                    }
//
//                    Snackbar.make(
//                        requireView(),
//                        getString(R.string.icing_report_submit_error) + it.errorMsg,
//                        Snackbar.LENGTH_LONG
//                    )
//                        .show()
//                }
//            }
//
//            return true
//        } else if (item.itemId == R.id.check_icing_report_action) {
//            checkReportedValues()
//
//            val report = mViewModel.getIcingReportBody()
//
//            Log.i("TAG", report.getRequestBodyForReportSubmissionAsString())
//
//            val result = OrapRepository.getInstance(requireContext()).checkIcingReportValues(report)
//
//            result.observe(this) {
//                if (it.success) {
//                    mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT) {
//                        param(FirebaseAnalytics.Param.CONTENT_TYPE, "Check icing report, success")
//                        param(FirebaseAnalytics.Param.SCREEN_NAME, "Icing report")
//                        param(FirebaseAnalytics.Param.SCREEN_CLASS, "ReportIcingFragment")
//                    }
//
//                    val text = getString(R.string.icing_report_sent)
//                    val toast = Toast.makeText(this.requireActivity(), text, Toast.LENGTH_SHORT)
//                    toast.show()
//
//                    mViewModel.reportChecked.value = true
////                    mViewModel.clear()
//
//                    // TODO: Switch menu from check to send
//                } else {
//                    mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT) {
//                        param(FirebaseAnalytics.Param.CONTENT_TYPE, "Check icing report, error")
//                        param(FirebaseAnalytics.Param.SCREEN_NAME, "Report icing")
//                        param(FirebaseAnalytics.Param.SCREEN_CLASS, "ReportIcingFragment")
//                    }
//
//                    Snackbar.make(
//                        requireView(),
//                        getString(R.string.icing_report_check_error) + it.errorMsg,
//                        Snackbar.LENGTH_LONG
//                    )
//                        .show()
//                }
//            }
//            return true
//        }
//        return false
//    }

    private fun initMaxMiddleWindDropDown() {
        mMaxMiddleWindAdapter = maxMiddleWindTimeArrayAdapter(
            requireContext(),
            R.layout.exposed_dropdown_menu_item,
            MaxMiddleWindTimeEnum.values()
        )
        icingDetailsDropdown = mBinding.icingDetailsTypeField

        icingDetailsDropdown.setOnItemClickListener { parent, _, position, _ ->
            mViewModel.maxMiddleWindTime.value = parent.getItemAtPosition(position) as MaxMiddleWindTimeEnum
        }
        icingDetailsDropdown.setAdapter(mMaxMiddleWindAdapter)
    }

    private fun initReportingHourDropDown() {
        mReportingHourAdapter = DropDownMenuArrayAdapter(
            requireContext(),
            R.layout.exposed_dropdown_menu_item,
            IcingReportHourEnum.values()
        )
        mSynopHourDropdown = mBinding.icingReportTimeField

        mSynopHourDropdown.setOnItemClickListener { parent, _, time, _ ->
            Log.e("setReportingTime", "Synop time updated, old: ${mViewModel.synopHourSelect.value}, new:${time}")
            mViewModel.synopHourSelect.value = (parent.getItemAtPosition(time) as IcingReportHourEnum).code
        }

        mSynopHourDropdown.setAdapter(mReportingHourAdapter)
    }

    override fun onDmsEditConfirmed() {
        val location = mLocationViewModel.getLocation()
        if (location != null) {
            mViewModel.location.value = location
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initMenu() // TODO: Change setHasOptionsMenu to this

        mViewModel = ViewModelProvider(requireActivity())[ReportIcingViewModel::class.java]
        mLocationViewModel = ViewModelProvider(requireActivity())[LocationDmsViewModel::class.java]
        mViewModel.init()

        mViewModel.synopTime.observe(viewLifecycleOwner) {
            mBinding.viewmodel = mViewModel
        }

        mViewModel.location.observe(viewLifecycleOwner) {
            locAdapter.locations = listOf<Location>(it)
        }
    }

    //    As this provides a consistent, optionally Lifecycle -aware, and modular way to handle
    //    menu creation and item selection, replace usages of this method with one or more calls
    //    to addMenuProvider(MenuProvider) in your Activity's onCreate(Bundle) method, having
    //    each provider override MenuProvider.onCreateMenu(Menu, MenuInflater) to create their menu items.
    private fun initMenu() {
        // The usage of an interface lets you inject your own implementation
        val menuHost: MenuHost = requireActivity()

        // Add menu items without using the Fragment Menu APIs
        // Note how we can tie the MenuProvider to the viewLifecycleOwner
        // and an optional Lifecycle.State (here, RESUMED) to indicate when
        // the menu should be visible
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                // Add menu items here
                menuInflater.inflate(R.menu.sprice_report_icing_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                // Handle the menu selection
                Log.i("Menu Sel", "Selected menu option:")

                mViewModel.reportingTime.value = Date.from(Instant.now())
                val calendar = Calendar.getInstance()
                calendar.time = mViewModel.synopTime.value!!
                calendar.set(Calendar.HOUR_OF_DAY, mViewModel.getSynopHourAsInt())
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                mViewModel.synopTime.value = calendar.time
                Log.w("Request", mViewModel.getIcingReportBody().getRequestBodyForReportSubmissionAsString())
                Log.e(
                    "onOptionsItemSelected", "\nSynop date: ${mViewModel.synopTime.value}, synop time: ${mViewModel.synopHourSelect.value}, reporting time: ${mViewModel.reportingTime.value}, synop unix: ${calendar.time.time}\n" +
                            "air temp: ${mViewModel.airTemperature.value}, sea temp: ${mViewModel.seaTemperature.value}, icing thickness: ${mViewModel.vesselIcingThickness.value},\n" +
                            "${mViewModel.maxMiddleWindTime.value?.getFormValue()},\n" +
                            "location: (lat: ${mViewModel.location.value?.latitude}, lon: ${mViewModel.location.value?.longitude})"
                )
                return true

                if (menuItem.itemId == R.id.send_icing_report_action) {
                    val result = OrapRepository.getInstance(requireContext()).SendIcingReport(mViewModel.getIcingReportBody())

                    result.observe(viewLifecycleOwner) {
                        if (it.success) {
                            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT) {
                                param(FirebaseAnalytics.Param.CONTENT_TYPE, "Send icing report, success")
                                param(FirebaseAnalytics.Param.SCREEN_NAME, "Icing report")
                                param(FirebaseAnalytics.Param.SCREEN_CLASS, "ReportIcingFragment")
                            }

                            val text = getString(R.string.icing_report_sent)
                            val toast = Toast.makeText(requireContext(), text, Toast.LENGTH_SHORT)
                            toast.show()
//                    mViewModel.clear()
                            Navigation.findNavController(requireView()).navigateUp()
                        } else {
                            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT) {
                                param(FirebaseAnalytics.Param.CONTENT_TYPE, "Send icing report, error")
                                param(FirebaseAnalytics.Param.SCREEN_NAME, "Icing report")
                                param(FirebaseAnalytics.Param.SCREEN_CLASS, "ReportIcingFragment")
                            }

                            Snackbar.make(
                                requireView(),
                                getString(R.string.icing_report_submit_error) + it.errorMsg,
                                Snackbar.LENGTH_LONG
                            )
                                .show()
                        }
                    }

                    return true
                } else if (menuItem.itemId == R.id.check_icing_report_action) {
                    checkReportedValues()

                    val report = mViewModel.getIcingReportBody()

                    Log.i("TAG", report.getRequestBodyForReportSubmissionAsString())

                    val result = OrapRepository.getInstance(requireContext()).checkIcingReportValues(report)

                    result.observe(viewLifecycleOwner) {
                        if (it.success) {
                            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT) {
                                param(FirebaseAnalytics.Param.CONTENT_TYPE, "Check icing report, success")
                                param(FirebaseAnalytics.Param.SCREEN_NAME, "Icing report")
                                param(FirebaseAnalytics.Param.SCREEN_CLASS, "ReportIcingFragment")
                            }

                            val text = getString(R.string.icing_report_sent)
                            val toast = Toast.makeText(requireActivity(), text, Toast.LENGTH_SHORT)
                            toast.show()

                            mViewModel.reportChecked.value = true
//                    mViewModel.clear()

                            // TODO: Switch menu from check to send
                        } else {
                            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT) {
                                param(FirebaseAnalytics.Param.CONTENT_TYPE, "Check icing report, error")
                                param(FirebaseAnalytics.Param.SCREEN_NAME, "Report icing")
                                param(FirebaseAnalytics.Param.SCREEN_CLASS, "ReportIcingFragment")
                            }

                            Snackbar.make(
                                requireView(),
                                getString(R.string.icing_report_check_error) + it.errorMsg,
                                Snackbar.LENGTH_LONG
                            )
                                .show()
                        }
                    }
                    return true
                }
                return false
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun checkReportedValues(): Boolean {
        var invalid = false
        if (mViewModel.maxMiddleWindTime.value != null) {
            mBinding.icingDetailsTypeField.error = getString(R.string.icing_missing_value)
            invalid = true
        }

        // TODO: Check other values

        return invalid
    }

    override fun onEditLocationClicked(v: View, itemClicked: Int) {
        mLocationViewModel.initWithLocation(mViewModel.location.value!!, itemClicked)
        val fm: FragmentManager = parentFragmentManager
        val locDialogFragment: LocationDmsDialogFragment =
            LocationDmsDialogFragment.newInstance(getString(R.string.tool_edit_location))

        fm.setFragmentResultListener(DMSLocation.EDIT_DMS_POSITION_FRAGMENT_RESULT_REQUEST_KEY, viewLifecycleOwner, this)
        locDialogFragment.show(fm, "fragment_edit_location")
    }

    override fun onFragmentResult(requestKey: String, result: Bundle) {
        val location = mLocationViewModel.getLocation()
        if (location != null) {
            mViewModel.location.value = location
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _mBinding = null
    }
}