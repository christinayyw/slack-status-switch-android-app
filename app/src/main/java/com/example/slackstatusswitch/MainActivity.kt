package com.example.slackstatusswitch

//import android.R
import android.os.Bundle
import android.view.View
import android.widget.RadioButton
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import org.json.JSONObject


class MainActivity : AppCompatActivity() {
    private var recyclerView: RecyclerView? = null
    private var mAdapter: RecyclerView.Adapter<*>? = null
    private var layoutManager: RecyclerView.LayoutManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var offlineSwitch: Switch = findViewById(R.id.offlineSwitch)
        offlineSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            offline(isChecked)
        }
        getUserStatus()


    }

    private fun setUserStatus(response: JSONObject) {
        var recyclerView = findViewById<View>(R.id.recyclerView) as RecyclerView
        recyclerView.setHasFixedSize(true)
        layoutManager = LinearLayoutManager(this)
        recyclerView.setLayoutManager(layoutManager)

        var profile = response.getJSONObject("user").getJSONObject("profile")
        var status_text = profile.getString("status_text")
        val user_status: MutableList<String> = ArrayList()
        print(status_text)
        user_status.add("Current Status: $status_text")

        mAdapter = MyAdapter(user_status)
        recyclerView.setAdapter(mAdapter)

    }

    private fun offline(enable: Boolean) {
        var endpoint ="/api/users.setPresence"
        var presence = if (enable) "away" else "auto"
        val json_body = JSONObject()
        json_body.put("presence", presence)

        postSlackRequest(endpoint, json_body)
    }

    fun onRadioButtonClicked(view: View) {
        if (view is RadioButton) {
            // Is the button now checked?
            if (view.isChecked) {
                // Check which radio button was clicked
                var status_text = ""
                var status_emoji = ""

                when (view.getId()) {
                    R.id.radio_empty -> {
                        status_text = "";
                        status_emoji = ""
                    }
                    R.id.radio_lunch -> {
                        status_text = "Lunch break";
                        status_emoji = ":rice:"
                    }
                    R.id.radio_driving -> {
                        status_text = "Driving";
                        status_emoji = ":car:"
                    }
                    R.id.radio_away -> {
                        status_text = "Away from desk"
                        status_emoji = ":coffee:"
                    }
                }

                //Send slack request
                var profile = JSONObject()
                profile.put("status_text", status_text)
                profile.put("status_emoji", status_emoji)
                profile.put("status_expiration", 0)
                val json_body = JSONObject()
                json_body.put("profile", profile)
                var endpoint = "/api/users.profile.set"
                postSlackRequest(endpoint, json_body)
                Toast.makeText(this, "Status switched to $status_text", Toast.LENGTH_SHORT).show()

                Thread.sleep(1000)
                getUserStatus()


            }
        }
    }


    private fun postSlackRequest(endpoint: String, json_body:JSONObject) {
        var slackToken = Cred.slackToken
        var host = "https://slack.com"
        var url = "$host/$endpoint"

        val jsonObjectRequest = object: JsonObjectRequest(Request.Method.POST, url, json_body,
                Response.Listener { response ->
//                    Toast.makeText(this, "Response to $endpoint: $response", Toast.LENGTH_SHORT).show()

                },
                Response.ErrorListener { error ->
                    Toast.makeText(this, "Error to $endpoint: $error.message", Toast.LENGTH_SHORT).show()
                }
        ){
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Authorization"] = "Bearer $slackToken"
                return headers
            }
        }

        VolleySingleton.getInstance(this).addToRequestQueue(jsonObjectRequest)
    }

    private fun getUserStatus() {
        var slackToken = Cred.slackToken
        var host = "https://slack.com"
        var endpoint = "api/users.info"
        var url = "$host/$endpoint"

        val jsonObjectRequest = object: StringRequest(Request.Method.POST, url,
                Response.Listener { response ->
//                    Toast.makeText(this, "Response to $endpoint: $response", Toast.LENGTH_SHORT).show()
                    setUserStatus(JSONObject(response))
                },
                Response.ErrorListener { error ->
                    Toast.makeText(this, "Error to $endpoint: $error.message", Toast.LENGTH_SHORT).show()
                }
        ){
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["user"] = "U5JTK5E8G"
                return params
            }
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Authorization"] = "Bearer $slackToken"
                return headers
            }
        }

        VolleySingleton.getInstance(this).addToRequestQueue(jsonObjectRequest)
    }

}
