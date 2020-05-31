package com.example.instagram

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.HorizontalScrollView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.instagram.Adapter.CommentsAdapter
import com.example.instagram.Model.Comment
import com.example.instagram.Model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_account_settings.*
import kotlinx.android.synthetic.main.activity_comments.*

class CommentsActivity : AppCompatActivity() {

    private lateinit var postId: String
    private lateinit var publisherId: String
    private lateinit var firebaseUser: FirebaseUser
    private lateinit var commentAdapter: CommentsAdapter
    private lateinit var commentList: MutableList<Comment>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comments)

        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        postId = intent.getStringExtra("postId")!!
        publisherId = intent.getStringExtra("publisherId")!!
        userInfo()


        val recyclerView = recycler_view_comments
        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.reverseLayout = true
        recyclerView.layoutManager = linearLayoutManager
        commentList = ArrayList()
        commentAdapter = CommentsAdapter(this, commentList)
        recyclerView.adapter = commentAdapter

        readComments()
        getPostImage()
        post_comment.setOnClickListener {
            if (add_comment.text.toString() == "") {
                Toast.makeText(this@CommentsActivity, "Enter your Comment first", Toast.LENGTH_LONG)
                    .show()
            } else {
                addComment()
            }
        }
    }

    private fun addComment() {
        val commentsRef =
            FirebaseDatabase.getInstance().reference.child("Comments").child(postId)
        val commentsMap = HashMap<String, Any>()
        commentsMap["comment"] = add_comment!!.text.toString()
        commentsMap["publisher"] = firebaseUser.uid
        commentsRef.push().setValue(commentsMap)
        addNotification()
        add_comment.text.clear()
    }

    private fun userInfo() {
        val userRef =
            FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUser.uid)
        userRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {}
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    val user = p0.getValue(User::class.java)
                    Picasso.get().load(user!!.getImage()).placeholder(R.drawable.profile)
                        .into(profile_image_comment)
                }
            }
        })
    }

    private fun getPostImage() {
        val postRef =
            FirebaseDatabase.getInstance().reference.child("Posts").child(postId).child("postimage")
        postRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {}
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    val image = p0.value.toString()
                    Picasso.get().load(image).placeholder(R.drawable.profile)
                        .into(post_image_comment)
                }
            }
        })
    }

    private fun readComments() {
        val commentsRef = FirebaseDatabase.getInstance().reference.child("Comments").child(postId)
        Log.d("CommentsAdapter", "commentsRef$commentsRef")

        commentsRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {}
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    commentList.clear()
                    Log.d("CommentsAdapter", "commentsRef p0.exists()")
                    for (snapshot in p0.children) {
                        val comment = snapshot.getValue(Comment::class.java)
                        Log.d("CommentsAdapter", "comment$comment")
                        commentList.add(comment!!)
                        Log.d("CommentsAdapter", "commentList.add(comment!!)")
                    }
                    Log.d("CommentsAdapter", "commentAdapter$commentAdapter")
                    commentAdapter.notifyDataSetChanged()
                }
            }
        })
    }

    private fun addNotification() {
        val notificationRef = FirebaseDatabase.getInstance().reference.child("Notifications").child(publisherId)
        val notificationMap = HashMap<String, Any>()
        notificationMap["userId"] = firebaseUser.uid
        notificationMap["text"] = "commented :" + add_comment!!.text.toString()
        notificationMap["postId"] = postId
        notificationMap["ispost"] = true
        notificationRef.push().setValue(notificationMap)


    }
}
