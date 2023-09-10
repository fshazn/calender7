package lk.nibm.calender7

import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.util.TypedValue
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONArray
import org.json.JSONObject

class Globle_Time : Fragment() {

    private lateinit var spnCountry: Spinner
    private lateinit var spnYear: Spinner
    private lateinit var spnMonth: Spinner
    private lateinit var holidayRecyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar

    var selectedCountry: String = ""
    var selectedYear: String = ""
    var selectedMonth: String = ""

    var holidayDataArray = JSONArray()
    var holidayAdapter: HolidayAdapter? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val rootView = inflater.inflate(R.layout.fragment_globle__time, container, false)

        holidayRecyclerView = rootView.findViewById(R.id.holidayRecyclerView)
        spnCountry = rootView.findViewById(R.id.spnCountry)
        spnYear = rootView.findViewById(R.id.spnYear)
        spnMonth = rootView.findViewById(R.id.spnMonth)
        progressBar = rootView.findViewById(R.id.progressBar)

        class CountryAdapter(
            context: Context,
            textViewResourceId: Int,
            private val countryList: List<String>
        ) : ArrayAdapter<String>(context, textViewResourceId, countryList) {

            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent) as TextView
                view.setTypeface(Typeface.DEFAULT_BOLD)
                view.setTextColor(ContextCompat.getColor(context, R.color.light_green))
                view.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24f)// set text size to 24sp

                // Get the current country item
                val currentItem = getItem(position)

                // Set the text to "Holidays Of + countryName"
                view.text = "Holidays Of $currentItem"
                return view
            }


            // Override the getDropDownView method to customize the appearance of the dropdown items
            override fun getDropDownView(
                position: Int,
                convertView: View?,
                parent: ViewGroup
            ): View {
                val view = super.getDropDownView(position, convertView, parent) as TextView
                view.setTextColor(ContextCompat.getColor(context, android.R.color.white))
                view.setTypeface(Typeface.DEFAULT)
                return view
            }
        }

        // Set up country spinner
        val countries = ArrayList<String>()
        val url =
            "https://calendarific.com/api/v2/countries?api_key="+resources.getString(R.string.API)
        val resultCountries = StringRequest(Request.Method.GET, url, Response.Listener { response ->
            try {
                val jsonObject = JSONObject(response)
                val jsonObjectResponse = jsonObject.getJSONObject("response")
                val jsonArrayCountries = jsonObjectResponse.getJSONArray("countries")
                for (i in 0 until jsonArrayCountries.length()) {
                    val jsonObjectCountry = jsonArrayCountries.getJSONObject(i)
                    countries.add(jsonObjectCountry.getString("country_name"))
                }
                val adapterCountry = CountryAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, countries)
                spnCountry.adapter = adapterCountry
            } catch (e: Exception) {
                Toast.makeText(requireContext(), e.message, Toast.LENGTH_SHORT).show()
            }
        }, Response.ErrorListener { error ->
            Toast.makeText(requireContext(), error.message, Toast.LENGTH_SHORT).show()
        })
        Volley.newRequestQueue(requireContext()).add(resultCountries)

        spnCountry.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long)
            {
                getCountryId(spnCountry.selectedItem.toString())

                getHolidayData(selectedCountry, selectedYear, selectedMonth)


            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                Toast.makeText(requireContext(), "Please select a country", Toast.LENGTH_SHORT).show()
            }
        }

        // Set up year spinner
        val years = (2014..2023).map { it.toString() }.reversed().toTypedArray()
        val yearAdapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, years)
        spnYear.adapter = yearAdapter

        spnYear.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                selectedYear = years[position]
                getHolidayData(selectedCountry, selectedYear, selectedMonth)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        }

        // Set up month spinner
        val months = arrayOf(
            "January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"
        )
        val monthAdapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, months)
        spnMonth.adapter = monthAdapter

        spnMonth.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                selectedMonth = (position + 1).toString()
                getHolidayData(selectedCountry, selectedYear, selectedMonth)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        }

        holidayRecyclerView.layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.VERTICAL, false
        )

        holidayRecyclerView.adapter = HolidayAdapter()
        return rootView
    }

    private fun getCountryId(name: String) {
        val url = "https://calendarific.com/api/v2/countries?api_key="+resources.getString(R.string.API)
        val resultCountries = StringRequest(Request.Method.GET, url, Response.Listener { response ->
            try {
                val jsonObject = JSONObject(response)
                val jsonObjectResponse = jsonObject.getJSONObject("response")
                val jsonArrayCountries = jsonObjectResponse.getJSONArray("countries")
                for (i in 0 until jsonArrayCountries.length()) {
                    val jsonObjectCountry = jsonArrayCountries.getJSONObject(i)
                    if (jsonObjectCountry.getString("country_name") == name) {
                        selectedCountry = jsonObjectCountry.getString("iso-3166").toString()
                        break
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), e.message, Toast.LENGTH_SHORT).show()
            }
        }, Response.ErrorListener { error ->
            Toast.makeText(requireContext(), error.message, Toast.LENGTH_SHORT).show()
        })
        Volley.newRequestQueue(requireContext()).add(resultCountries)
    }

    private fun getHolidayData(countryName: String, year: String, month: String) {

        progressBar.visibility = View.VISIBLE // show the progress bar

        val url =
            "https://calendarific.com/api/v2/holidays?&api_key="+resources.getString(R.string.API)+"&country=" + countryName + "&year=" + year + "&month=" + month + ""

        val request = StringRequest(Request.Method.GET, url,
            Response.Listener { response ->
                try {
                    val jsonObject = JSONObject(response)
                    val jsonObjectResponse = jsonObject.getJSONObject("response")
                    holidayDataArray = jsonObjectResponse.getJSONArray("holidays")
                    holidayRecyclerView.adapter?.notifyDataSetChanged()

                    progressBar.visibility = View.GONE // hide the progress bar

                } catch (e: Exception) {
                    e.printStackTrace()
                    progressBar.visibility = View.GONE // hide the progress bar

                }
            },
            Response.ErrorListener { error ->
                error.printStackTrace()
                progressBar.visibility = View.GONE // hide the progress bar

            })

        Volley.newRequestQueue(requireContext()).add(request)
    }

    inner class HolidayAdapter : RecyclerView.Adapter<HolidayViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolidayViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.holiday_row, parent, false)

            return HolidayViewHolder(view)
        }

        override fun onBindViewHolder(holder: HolidayViewHolder, position: Int) {
            try {
                val holiday = holidayDataArray.getJSONObject(position)

                holder.holidayTitle.text = holiday.getString("name")
                holder.holidayType.text = holiday.getString("primary_type")
                holder.holidayDate.text =
                    holiday.getJSONObject("date").getJSONObject("datetime").getInt("day").toString()

                holder.itemView.setOnClickListener {
                    val dialogBuilder = AlertDialog.Builder(holder.itemView.context)
                    dialogBuilder.setTitle(holiday.getString("name"))
                    dialogBuilder.setMessage(holiday.getString("description"))
                    dialogBuilder.setPositiveButton("OK", null)
                    dialogBuilder.create().show()
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        override fun getItemCount(): Int {
            return holidayDataArray.length()
        }
    }

    inner class HolidayViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val holidayTitle: TextView = itemView.findViewById(R.id.holidayName)
        val holidayType: TextView = itemView.findViewById(R.id.holidayType)
        val holidayDate: TextView = itemView.findViewById(R.id.holidayDate)
    }
}
