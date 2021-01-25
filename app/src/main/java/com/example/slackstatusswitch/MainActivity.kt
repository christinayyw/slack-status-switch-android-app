package com.example.slackstatusswitch

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Switch
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import org.json.JSONObject


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var offlineSwitch: Switch = findViewById(R.id.offlineSwitch)
        offlineSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            offline(isChecked)
        }
    }

    private fun offline(enable: Boolean) {
        var endpoint ="/api/users.setPresence"
        var presence = if (enable) "away" else "auto"
        val json_body = JSONObject()
        json_body.put("presence", presence)

        postSlackRequest(endpoint, json_body)
    }

    private fun postSlackRequest(endpoint: String, json_body:JSONObject) {
        var slackToken = Cred.slackToken
        var host = "https://slack.com"
        var url = "$host/$endpoint"

        val jsonObjectRequest = object: JsonObjectRequest(Request.Method.POST, url, json_body,
                Response.Listener { response ->
                    Toast.makeText(this, "You Clicked: $response", Toast.LENGTH_SHORT).show()

                },
                Response.ErrorListener { error ->
                    Toast.makeText(this, "Error $error .message", Toast.LENGTH_SHORT).show()
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

}