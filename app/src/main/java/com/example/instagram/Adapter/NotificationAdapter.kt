package com.example.instagram.Adapter

import android.content.Context
import android.opengl.Visibility
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.instagram.Fragments.PostDetailsFragment
import com.example.instagram.Fragments.ProfileFragment
import com.example.instagram.Model.Notification
import com.example.instagram.Model.Post
import com.example.instagram.Model.User
import com.example.instagram.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.activity_comments.*
import kotlinx.android.synthetic.main.fragment_profile.view.*
import kotlinx.android.synthetic.main.notifications_item_layout.view.*

class NotificationAdapter(
    private val mContext: Context,
    private val mNotification: List<Notification>
) : RecyclerView.Adapter<NotificationAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(mContext).inflate(R.layout.notifications_item_layout, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = mNotification.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val notification = mNotification[position]

        when {
            notification.text.equals("started following you") -> {
                holder.text.text = "started following you"

            }
            notification.text.equals("liked your post") -> {

                holder.text.text = "liked your post"

            }
            notification.text.contains("commented :") -> {

                holder.text.text = notification.text.replace("commented :", "commented: ")
            }
            else -> {
                holder.text.text = notification.text
            }
        }

        userInfo(holder.profileImage, holder.userName, notification.userId)

        if (notification.ispost) {
            holder.postImage.visibility = View.VISIBLE
            getPostImage(holder.postImage, notification.postId)
        } else {
            holder.postImage.visibility = View.GONE
        }

        holder.itemView.setOnClickListener {
            if (notification.ispost) {
                val editor = mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit()
                editor.putString("postId", notification.postId)
                editor.apply()
                (mContext as FragmentActivity).supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, PostDetailsFragment()).commit()
            } else {
                val editor = mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit()
                editor.putString("profileId", notification.userId)
                editor.apply()
                (mContext as FragmentActivity).supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, ProfileFragment()).commit()
            }
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var postImage: ImageView = itemView.notification_post_image
        var profileImage: CircleImageView = itemView.notification_profileImage
        var userName: TextView = itemView.username_notification
        var text: TextView = itemView.comment_notification

    }

    private fun userInfo(imageView: ImageView, userName: TextView, publisherId: String) {

        val userRef =
            FirebaseDatabase.getInstance().getReference().child("Users").child(publisherId)
        userRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {}
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    val user = p0.getValue<User>(User::class.java)
                    Picasso.get().load(user!!.getImage()).placeholder(R.drawable.profile)
                        .into(imageView)

                    userName.text = user.getUserName()
                }
            }
        })
    }

    private fun getPostImage(imageView: ImageView, postID: String) {
        val postRef =
            FirebaseDatabase.getInstance().reference.child("Posts").child(postID)

        postRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {}
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {

                    val post = p0.getValue(Post::class.java)

                    Picasso.get().load(post!!.postimage).placeholder(R.drawable.profile)
                        .into(imageView)

                }
            }
        })
    }

}