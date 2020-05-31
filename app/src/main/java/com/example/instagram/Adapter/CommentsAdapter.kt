package com.example.instagram.Adapter

import android.content.Context
import android.util.Log
import android.util.Log.d
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.instagram.Model.Comment
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
import kotlinx.android.synthetic.main.comment_row.view.*

class CommentsAdapter(private val mContext: Context, private val mComment: MutableList<Comment>) :
    RecyclerView.Adapter<CommentsAdapter.ViewHolder>() {

    private lateinit var firebaseUser: FirebaseUser

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.comment_row, parent, false)
        Log.d("CommentsAdapter", "view$view")
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = mComment.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        val comment = mComment[position]
        Log.d("CommentsAdapter", "comment onBindViewHolder$comment")
        holder.commentTV.text = comment.comment
        getUserInfo(holder.imageView, holder.userNameTV, comment.publisher)
    }

    private fun getUserInfo(imageView: CircleImageView, userNameTV: TextView, publisher: String) {
        val userRef = FirebaseDatabase.getInstance().reference.child("Users").child(publisher)
        d("CommentsAdapter", "publisher$publisher")
        userRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {}
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    d("CommentsAdapter", "p0.exists()")
                    val user = p0.getValue(User::class.java)
                    Picasso.get().load(user!!.getImage()).placeholder(R.drawable.profile)
                        .into(imageView)
                    userNameTV.text = user.getUserName()
                }
            }
        })
    }


    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var imageView: CircleImageView = itemView.user_profile_image_comment
        var userNameTV: TextView = itemView.user_name_comment
        var commentTV: TextView = itemView.comment_comment

    }

}