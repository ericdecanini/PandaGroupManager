package com.ericdecanini.pandagroupmanager

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_group.*

class GroupActivity : AppCompatActivity() {

    val firestore = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    var role = -1
    var groupID = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group)

        val groupID = intent.getStringExtra("INTENT_ID")
        if (intent.hasExtra("INTENT_NEW_GROUP")) { createNewGroup() }
        else {
            this.groupID = groupID
            if (intent.hasExtra("INTENT_JOINING_GROUP")) { joinGroup(groupID) }
            loadGroup(groupID)
        }
    }

    private fun loadGroup(groupID: String) {
        firestore.collection("groups").document(groupID).collection("users")
                .addSnapshotListener { userSnapshot, firebaseFirestoreException ->
                    val users = ArrayList<User>()

                    if (userSnapshot == null || userSnapshot.isEmpty) {
                        Toast.makeText(this, "Group does not exist", Toast.LENGTH_SHORT).show()
                        finish()
                        return@addSnapshotListener
                    }

                    for (user in userSnapshot) {
                        users.add(User(user.id, (user.get("role") as Long).toInt()))
                        if (user.id == auth.uid!!) { role = (user.get("role") as Long).toInt() }
                    }
                    val adapter = UsersAdapter(this, R.layout.list_item_member, users)
                    lv_users.adapter = adapter
                }
    }

    private fun joinGroup(groupID: String) {
        // Add the user with role as user (int 1)
        val usersMap = HashMap<String, Any>()
        usersMap["id"] = auth.uid!!
        usersMap["role"] = 1
        firestore.collection("groups").document(groupID)
                .collection("users").document(auth.uid!!).set(usersMap)

        // Add the group to the user's group subcollection
        val groupMap = HashMap<String, Any>()
        firestore.collection("users").document(auth.uid!!).collection("groups")
                .document(groupID).set(groupMap)
    }

    private fun createNewGroup() {
        // Prepare an empty map for a new group document
        val groupMap = HashMap<String, Any>()

        // Prepare a new map for the current user as an admin (int 0)
        val userMap = HashMap<String, Any>()
        userMap["id"] = auth.uid!!
        userMap["role"] = 0

        // Add the group map and then the user map
        firestore.collection("groups").add(groupMap)
                .addOnSuccessListener {
                    // Init the users subcollection
                    it.collection("users").document(auth.uid!!).set(userMap)

                    // Add the group to the user's group subcollection
                    val groupMap = HashMap<String, Any>()
                    groupMap["id"] = it.id
                    groupMap["name"] = "New Group"
                    firestore.collection("users").document(auth.uid!!)
                            .collection("groups").document(it.id).set(groupMap)
                }
    }
}
