package com.example.instagram.Adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.instagram.Fragments.ProfileFragment
import com.example.instagram.MainActivity
import com.example.instagram.Model.User
import com.example.instagram.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.user_item_layout.view.*

class UserAdapter(
    private var mContext: Context,
    private var mUser: List<User>,
    private var isFragment: Boolean = false
) : RecyclerView.Adapter<UserAdapter.ViewHolder>() {

    private var firebaseUser: FirebaseUser? = FirebaseAuth.getInstance().currentUser

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.user_item_layout, parent, false)

        return ViewHolder(view)
    }

    override fun getItemCount() = mUser.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = mUser[position]
        holder.userName.text = user.getUserName()
        holder.fullName.text = user.getFullName()
        Picasso.get().load(user.getImage()).placeholder(R.drawable.profile).into(holder.userImage)

        checkFollowingStatus(user.getUID(), holder.followButton)

        holder.itemView.setOnClickListener(View.OnClickListener {

            if (isFragment) {
                val pref = mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit()
                pref.putString("profileId", user.getUID())
                pref.apply()

                (mContext as FragmentActivity).supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, ProfileFragment()).commit()
            } else {
                val intent = Intent(mContext, MainActivity::class.java)
                intent.putExtra("publisherId", user.getUID())
                mContext.startActivity(intent)
            }

        })

        holder.followButton.setOnClickListener {
            if (holder.followButton.text.toString() == "Follow") {
                firebaseUser?.uid.let { it1 ->
                    FirebaseDatabase.getInstance().reference.child("Follow")
                        .child(it1.toString())
                        .child("Following").child(user.getUID())
                        .setValue(true).addOnCompleteListener { it2 ->
                            if (it2.isSuccessful) {

                                firebaseUser?.uid.let { it1 ->
                                    FirebaseDatabase.getInstance().reference.child("Follow")
                                        .child(user.getUID())
                                        .child("Followers").child(it1.toString())
                                        .setValue(true).addOnCompleteListener { it2 ->
                                            if (it2.isSuccessful) {

                                            }
                                        }
                                }
                            }
                        }
                }
                addNotification(user.getUID())
            } else {
                firebaseUser?.uid.let { it1 ->
                    FirebaseDatabase.getInstance().reference.child("Follow").child(it1.toString())
                        .child("Following").child(user.getUID())
                        .removeValue().addOnCompleteListener { it1 ->
                            if (it1.isSuccessful) {

                                firebaseUser?.uid.let { it1 ->
                                    FirebaseDatabase.getInstance().reference.child("Follow")
                                        .child(user.getUID())
                                        .child("Followers").child(it1.toString())
                                        .removeValue().addOnCompleteListener { it2 ->
                                            if (it2.isSuccessful) {

                                            }
                                        }
                                }
                            }
                        }
                }
            }
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {


        var userName: TextView = itemView.userName_search
        var fullName: TextView = itemView.fullName_search
        var userImage: CircleImageView = itemView.userProfile_image_search_frag
        var followButton: Button = itemView.follow_btn_search

    }

    private fun checkFollowingStatus(uid: String, followButton: Button) {


        val followingRef = firebaseUser?.uid.let { it1 ->
            FirebaseDatabase.getInstance().reference
                .child("Follow").child(it1.toString())
                .child("Following")
        }
        followingRef.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(dataSnapshot: DataSnapshot) {

                if (dataSnapshot.child(uid).exists()) {
                    followButton.text = "Following"
                } else {
                    followButton.text = "Follow"
                }


            }

            override fun onCancelled(p0: DatabaseError) {

            }

        })
    }

    private fun addNotification(userId: String) {

        val notificationRef = FirebaseDatabase.getInstance().reference.child("Notifications")
            .child(userId)
        val notificationMap = HashMap<String, Any>()
        notificationMap["userId"] = firebaseUser!!.uid
        notificationMap["text"] = "started following you"
        notificationMap["postId"] = ""
        notificationMap["ispost"] = false
        notificationRef.push().setValue(notificationMap)


    }


}