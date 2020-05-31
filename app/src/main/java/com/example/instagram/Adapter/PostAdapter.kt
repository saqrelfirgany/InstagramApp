package com.example.instagram.Adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat.startActivity
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.instagram.CommentsActivity
import com.example.instagram.Fragments.PostDetailsFragment
import com.example.instagram.Fragments.ProfileFragment
import com.example.instagram.MainActivity
import com.example.instagram.Model.Post
import com.example.instagram.Model.User
import com.example.instagram.R
import com.example.instagram.ShowUsersActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.activity_account_settings.*
import kotlinx.android.synthetic.main.activity_comments.*
import kotlinx.android.synthetic.main.post_item_layout.view.*
import javax.crypto.Cipher

class PostAdapter(
    private var mContext: Context,
    private var mPost: List<Post>
) : RecyclerView.Adapter<PostAdapter.ViewHolder>() {


    private var firebaseUser: FirebaseUser? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val view = LayoutInflater.from(mContext).inflate(R.layout.post_item_layout, parent, false)

        return ViewHolder(view)
    }

    override fun getItemCount(): Int = mPost.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        firebaseUser = FirebaseAuth.getInstance().currentUser
        val post = mPost[position]
        Picasso.get().load(post.postimage).into(holder.postImage)
        publisherInfo(holder.profileImage, holder.userName, holder.publisher, post.publisher)
        if (post.description == "") {
            holder.description.visibility = View.GONE
        } else {
            holder.description.visibility = View.VISIBLE
            holder.description.text = "description :${post.description}"
        }

        isLikes(post.postid, holder.likeButton)
        numberOfLikes(post.postid, holder.likes)
        numberOfComments(post.postid, holder.comments)
        checkSaveStatus(post.postid, holder.saveButton)

        holder.postImage.setOnClickListener {
            transaction(post.postid)
        }
        holder.publisher.setOnClickListener {
            transaction(post.publisher)
        }
        holder.profileImage.setOnClickListener {
            transaction(post.publisher)
        }
        holder.postImage.setOnClickListener {
            transaction(post.postid)
        }

        holder.likeButton.setOnClickListener {
            if (holder.likeButton.tag == "Like") {
                FirebaseDatabase.getInstance().reference.child("Likes").child(post.postid)
                    .child(firebaseUser!!.uid).setValue(true)
                addNotification(post.publisher, post.postid)

            } else {
                FirebaseDatabase.getInstance().reference.child("Likes").child(post.postid)
                    .child(firebaseUser!!.uid).removeValue()

                mContext.startActivity(Intent(mContext, MainActivity::class.java))
            }
        }
        holder.commentButton.setOnClickListener {
            val intent = Intent(mContext, CommentsActivity::class.java)
            intent.putExtra("postId", post.postid)
            intent.putExtra("publisherId", post.publisher)
            mContext.startActivity(intent)
        }
        holder.comments.setOnClickListener {
            val intent = Intent(mContext, CommentsActivity::class.java)
            intent.putExtra("postId", post.postid)
            intent.putExtra("publisherId", post.publisher)
            mContext.startActivity(intent)
        }
        holder.saveButton.setOnClickListener {
            if (holder.saveButton.tag == "Save") {
                FirebaseDatabase.getInstance().reference.child("Saves").child(firebaseUser!!.uid)
                    .child(post.postid).setValue(true)
            } else {
                FirebaseDatabase.getInstance().reference.child("Saves").child(firebaseUser!!.uid)
                    .child(post.postid).removeValue()
            }
        }
        holder.likes.setOnClickListener {
            val intent = Intent(mContext, ShowUsersActivity::class.java)
            intent.putExtra("id", post.postid)
            intent.putExtra("title", "likes")
            mContext.startActivity(intent)
        }
    }

    private fun transaction(postId: String) {
        val editor = mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit()
        editor.putString("postId", postId)
        editor.apply()
        (mContext as FragmentActivity).supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, PostDetailsFragment()).commit()
    }

    private fun checkSaveStatus(postId: String, imageView: ImageView) {
        val savedPostRef =
            FirebaseDatabase.getInstance().reference.child("Saves").child(firebaseUser!!.uid)

        savedPostRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {}
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.child(postId).exists()) {
                    imageView.setImageResource(R.drawable.save_large_icon)
                    imageView.tag = "Saved"
                } else {
                    imageView.setImageResource(R.drawable.save_unfilled_large_icon)
                    imageView.tag = "Save"
                }
            }
        })
    }

    private fun numberOfComments(postId: String, comments: TextView) {
        val commentsRef = FirebaseDatabase.getInstance().reference.child("Comments").child(postId)
        commentsRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {}
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    comments.text = "view all " + p0.childrenCount.toString() + " comments"
                }
            }
        })
    }

    private fun numberOfLikes(postId: String, likes: TextView) {
        val likesRef = FirebaseDatabase.getInstance().reference.child("Likes").child(postId)
        likesRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {}
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    likes.text = p0.childrenCount.toString() + " Likes"
                }
            }
        })
    }

    private fun isLikes(postId: String, likeButton: ImageView) {
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        val likesRef = FirebaseDatabase.getInstance().reference.child("Likes").child(postId)
        likesRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {}
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.child(firebaseUser!!.uid).exists()) {
                    likeButton.setImageResource(R.drawable.heart_clicked)
                    likeButton.tag = "Liked"
                } else {
                    likeButton.setImageResource(R.drawable.heart_not_clicked)
                    likeButton.tag = "Like"
                }
            }
        })
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var profileImage: CircleImageView = itemView.user_profile_image_post
        var postImage: ImageView = itemView.post_image_home
        var likeButton: ImageView = itemView.post_image_like_btn
        var commentButton: ImageView = itemView.post_image_comment_btn
        var saveButton: ImageView = itemView.post_save_comment_btn
        var userName: TextView = itemView.user_name_post
        var likes: TextView = itemView.likes
        var publisher: TextView = itemView.publisher
        var description: TextView = itemView.description
        var comments: TextView = itemView.comments

    }

    private fun publisherInfo(
        profileImage: CircleImageView,
        userName: TextView,
        publisher: TextView,
        publisherId: String
    ) {
        val usersRef = FirebaseDatabase.getInstance().reference.child("Users").child(publisherId)

        usersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    val user = p0.getValue(User::class.java)

                    Picasso.get().load(user!!.getImage()).placeholder(R.drawable.profile)
                        .into(profileImage)
                    userName.text = user.getUserName()
                    publisher.text = "publisher :${user.getFullName()}"
                }
            }

            override fun onCancelled(p0: DatabaseError) {}
        })
    }

    private fun addNotification(userId: String, postId: String) {

        val notificationRef = FirebaseDatabase.getInstance().reference.child("Notifications")
            .child(userId)
        val notificationMap = HashMap<String, Any>()
        notificationMap["userId"] = firebaseUser!!.uid
        notificationMap["text"] = "liked your post"
        notificationMap["postId"] = postId
        notificationMap["ispost"] = true
        notificationRef.push().setValue(notificationMap)


    }
}