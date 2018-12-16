package com.ericdecanini.pandagroupmanager

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.PopupMenu
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.list_item_member.view.*

class UsersAdapter(context: Context, val resource: Int, val users: ArrayList<User>): ArrayAdapter<User>(context, resource, users) {

    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val user = users[position]
        var view = convertView

        if (view == null) {
            view = LayoutInflater.from(context).inflate(resource, parent, false)
        }

        // Prepare the text and the popup menu
        view!!.tv_name.text = user.id
        view.iv_menu.setOnClickListener {
            val popup = PopupMenu(context, it)
            popup.menuInflater.inflate(R.menu.menu_user, popup.menu)
            popup.setOnMenuItemClickListener { item ->
                when(item.itemId) {
                    R.id.ic_delete_user -> { deleteUser(users[position].id) }
                }
                true
            }
            popup.show()
        }

        return view
    }

    private fun deleteUser(uid: String) {
        val role = (context as GroupActivity).role
        val groupID = (context as GroupActivity).groupID

        if (role != 0) {
            Toast.makeText(context, "You do not have permission to do this", Toast.LENGTH_SHORT).show()
            return
        }

        // Delete group from users ref
        firestore.collection("users").document(uid)
                .collection("groups").document(groupID).delete()

        // Delete user from group ref
        firestore.collection("groups").document(groupID)
                .collection("users").document(uid).delete()
    }

}