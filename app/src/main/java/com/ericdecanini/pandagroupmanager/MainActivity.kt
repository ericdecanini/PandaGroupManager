package com.ericdecanini.pandagroupmanager

import android.content.DialogInterface
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.ContextCompat.startActivity
import android.support.v7.app.AlertDialog
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.EditText
import com.ericdecanini.pandagroupmanager.R.id.groups
import com.ericdecanini.pandagroupmanager.R.id.lv_groups
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity() {

    val RC_SIGN_IN = 0

    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()
    var providers = Arrays.asList(AuthUI.IdpConfig.EmailBuilder().build())

    val groups = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Launch authentication if not logged in
        if (auth.currentUser == null) {
            launchAuthentication()
            return
        }

        // FAB Listener to create a new group
        fab.setOnClickListener { launchAddGroupDialog() }

        // Get the List of Groups belonging to the user
        getGroupsList()
    }

    private fun launchAuthentication() {
        startActivityForResult(AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .build(), RC_SIGN_IN)
    }

    private fun launchAddGroupDialog() {
        val items = arrayOf("Create New Group", "Join Group")
        AlertDialog.Builder(this)
                .setItems(items) { dialogInterface, i ->
                    when(i) {
                        0 -> { createNewGroup() }
                        1 -> { joinGroup() }
                    }
                }
                .show()
    }

    private fun createNewGroup() {
        val intent = Intent(this, GroupActivity::class.java)
        intent.putExtra("INTENT_NEW_GROUP", true)
        startActivity(intent)
    }

    private fun joinGroup() {
        val editText = EditText(this)
        editText.tag = "groupid"
        AlertDialog.Builder(this)
                .setTitle("Enter Group ID")
                .setView(editText)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Join") { dialogInterface, i ->
                    val groupID = editText.text.toString()
                    val intent = Intent(this, GroupActivity::class.java)
                    intent.putExtra("INTENT_ID", groupID)
                    intent.putExtra("INTENT_JOINING_GROUP", true)
                    startActivity(intent)
                }
                .show()
    }

    private fun getGroupsList() {
        firestore.collection("users").document(auth.uid!!)
                .collection("groups").addSnapshotListener { groupSnapshots, firebaseFirestoreException ->
                    for (group in groupSnapshots!!) { groups.add(group.id) }

                    val adapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, groups)
                    lv_groups.adapter = adapter
                    lv_groups.setOnItemClickListener { adapterView, view, i, l ->
                        val intent = Intent(this, GroupActivity::class.java)
                        intent.putExtra("INTENT_ID", groups[i])
                        startActivity(intent)
                    }
                }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        // Sign Out and Restart Activity
        if (item?.itemId == R.id.ic_sign_out) {
            val intent = Intent(this, MainActivity::class.java)
            finish()
            startActivity(intent)
        }

        return true
    }
}
