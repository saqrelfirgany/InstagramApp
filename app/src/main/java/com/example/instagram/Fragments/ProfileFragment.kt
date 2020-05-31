package com.example.instagram.Fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.instagram.AccountSettingsActivity
import com.example.instagram.Adapter.MyImagesAdapter
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
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.android.synthetic.main.fragment_profile.view.*
import java.util.*
import kotlin.collections.ArrayList

class ProfileFragment : Fragment() {

    private lateinit var profileId: String
    private lateinit var firebaseUser: FirebaseUser
    private lateinit var postList: List<Post>
    private lateinit var myImagesAdapter: MyImagesAdapter

    private lateinit var mySavedImagesAdapter: MyImagesAdapter
    private lateinit var postListSaved: List<Post>
    private lateinit var mySavedImg: List<String>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_profile, container, false)


        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        val pref = context?.getSharedPreferences("PREFS", Context.MODE_PRIVATE)
        if (pref != null) {
            this.profileId = pref.getString("profileId", "none")!!
        }

        if (profileId == firebaseUser.uid) {
            view.edit_account_settings_btn.text = "Edit Profile"
        } else if (profileId != firebaseUser.uid) {

            checkFollowAndFollowingButtonStatus()
        }

        val recyclerViewUploadedImages = view.recycler_View_upload_pic
        recyclerViewUploadedImages.setHasFixedSize(true)
        recyclerViewUploadedImages.layoutManager = GridLayoutManager(context, 3)
        postList = ArrayList()
        myImagesAdapter = MyImagesAdapter(context!!, postList as ArrayList<Post>)
        recyclerViewUploadedImages.adapter = myImagesAdapter


        val recyclerViewSavedImages = view.recycler_View_saved_pic
        recyclerViewSavedImages.setHasFixedSize(true)
        recyclerViewSavedImages.layoutManager = GridLayoutManager(context, 3)
        postListSaved = ArrayList()
        mySavedImagesAdapter = MyImagesAdapter(context!!, postListSaved as ArrayList<Post>)
        recyclerViewSavedImages.adapter = mySavedImagesAdapter

        recyclerViewSavedImages.visibility = View.GONE
        recyclerViewUploadedImages.visibility = View.VISIBLE

        view.images_grid_view_btn.setOnClickListener {
            recyclerViewSavedImages.visibility = View.GONE
            recyclerViewUploadedImages.visibility = View.VISIBLE
        }
        view.images_save_btn.setOnClickListener {
            recyclerViewUploadedImages.visibility = View.GONE
            recyclerViewSavedImages.visibility = View.VISIBLE
        }

        view.total_Followers_linear.setOnClickListener {
            val intent = Intent(context, ShowUsersActivity::class.java)
            intent.putExtra("id", profileId)
            intent.putExtra("title", "followers")
            startActivity(intent)
        }

        view.total_Following_linear.setOnClickListener {
            val intent = Intent(context, ShowUsersActivity::class.java)
            intent.putExtra("id", profileId)
            intent.putExtra("title", "following")
            startActivity(intent)
        }

        view.edit_account_settings_btn.setOnClickListener {

            when (view.edit_account_settings_btn.text.toString()) {
                "Edit Profile" -> startActivity(
                    Intent(context, AccountSettingsActivity::class.java)
                )
                "Follow" -> {
                    firebaseUser.uid.let {
                        FirebaseDatabase.getInstance().reference.child("Follow")
                            .child(it.toString()).child("Following").child(profileId).setValue(true)
                    }
                    firebaseUser.uid.let {
                        FirebaseDatabase.getInstance().reference.child("Follow")
                            .child(profileId).child("Followers").child(it.toString()).setValue(true)
                    }
                    addNotification()
                }

                "Following" -> {
                    firebaseUser.uid.let {
                        FirebaseDatabase.getInstance().reference.child("Follow")
                            .child(it.toString()).child("Following").child(profileId).removeValue()
                    }
                    firebaseUser.uid.let {
                        FirebaseDatabase.getInstance().reference.child("Follow")
                            .child(profileId).child("Followers").child(it.toString()).removeValue()
                    }
                }
            }
        }

        getFollower()
        getFollowings()
        userInfo()
        myPhotos()
        getTotalNumberOfPost()
        mySaves()

        return view
    }

    private fun mySaves() {
        mySavedImg = ArrayList()

        val savesRef =
            FirebaseDatabase.getInstance().reference.child("Saves").child(firebaseUser.uid)

        savesRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {}
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    for (snapshot in p0.children) {
                        (mySavedImg as ArrayList<String>).add(snapshot.key!!)
                    }
                    readSavedImagesData()
                }
            }
        })
    }

    private fun readSavedImagesData() {
        val postRef = FirebaseDatabase.getInstance().reference.child("Posts")

        postRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {}
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    (postListSaved as ArrayList<Post>).clear()
                    for (snapshot in p0.children) {
                        val post = snapshot.getValue(Post::class.java)
                        for (id in mySavedImg) {
                            if (id == post!!.postid) {
                                (postListSaved as ArrayList<Post>).add(post)
                            }
                        }
                    }
                    mySavedImagesAdapter.notifyDataSetChanged()
                }
            }
        })
    }

    private fun checkFollowAndFollowingButtonStatus() {

        val followingRef = firebaseUser.uid.let {
            FirebaseDatabase.getInstance().reference
                .child("Follow").child(it)
                .child("Following")
        }
        followingRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {}
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.child(profileId).exists()) {
                    view?.edit_account_settings_btn?.text = "Following"
                } else {
                    view?.edit_account_settings_btn?.text = "Follow"
                }
            }
        })
    }

    private fun getFollower() {

        val followersRef = FirebaseDatabase.getInstance().reference
            .child("Follow").child(profileId)
            .child("Followers")

        followersRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {}

            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    view?.total_Followers?.text = p0.childrenCount.toString()
                }
            }
        })
    }

    private fun getFollowings() {

        val followersRef = FirebaseDatabase.getInstance().reference
            .child("Follow").child(profileId)
            .child("Following")

        followersRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {}

            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    view?.total_Following?.text = p0.childrenCount.toString()
                }
            }
        })
    }

    private fun userInfo() {

        val userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(profileId)
        userRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {}
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    val user = p0.getValue<User>(User::class.java)
                    Picasso.get().load(user!!.getImage()).placeholder(R.drawable.profile)
                        .into(view?.profile_fragment_profileImage)

                    view?.profile_fragment_username?.text = user.getUserName()
                    view?.full_name_profile_frag?.text = user.getFullName()
                    view?.bio_profile_frag?.text = user.getBio()
                }
            }
        })
    }

    private fun myPhotos() {

        val postRef = FirebaseDatabase.getInstance().reference.child("Posts")

        postRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {}
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    (postList as ArrayList<Post>).clear()
                    for (snapshot in p0.children) {
                        val post = snapshot.getValue(Post::class.java)
                        if (post!!.publisher.equals(profileId)) {
                            (postList as ArrayList<Post>).add(post)
                        }
                        Collections.reverse(postList)
                        myImagesAdapter.notifyDataSetChanged()
                    }
                }
            }
        })
    }

    override fun onStop() {
        super.onStop()
        val pref = context?.getSharedPreferences("PREFS", Context.MODE_PRIVATE)?.edit()
        pref?.putString("profileId", firebaseUser.uid)
        pref?.apply()
    }

    override fun onPause() {
        super.onPause()
        val pref = context?.getSharedPreferences("PREFS", Context.MODE_PRIVATE)?.edit()
        pref?.putString("profileId", firebaseUser.uid)
        pref?.apply()
    }

    override fun onDestroy() {
        super.onDestroy()
        val pref = context?.getSharedPreferences("PREFS", Context.MODE_PRIVATE)?.edit()
        pref?.putString("profileId", firebaseUser.uid)
        pref?.apply()
    }

    private fun getTotalNumberOfPost() {
        val postRef = FirebaseDatabase.getInstance().reference.child("Posts")

        postRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {}
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    var postCounter = 0
                    for (snapshot in p0.children) {
                        val post = snapshot.getValue(Post::class.java)!!
                        if (post.publisher == profileId) {
                            postCounter++
                        }
                    }
                    total_posts.text = postCounter.toString()
                }
            }
        })

    }

    private fun addNotification() {

        val notificationRef = FirebaseDatabase.getInstance().reference.child("Notifications")
            .child(profileId)
        val notificationMap = HashMap<String, Any>()
        notificationMap["userId"] = firebaseUser!!.uid
        notificationMap["text"] = "started following you"
        notificationMap["postId"] = ""
        notificationMap["ispost"] = false
        notificationRef.push().setValue(notificationMap)


    }
}
