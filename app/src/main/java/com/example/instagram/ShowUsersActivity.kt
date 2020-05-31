package com.example.instagram

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.instagram.Adapter.UserAdapter
import com.example.instagram.Model.User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_show_users.*
import kotlinx.android.synthetic.main.fragment_profile.view.*
import kotlinx.android.synthetic.main.fragment_search.view.*

class ShowUsersActivity : AppCompatActivity() {

    lateinit var id: String
    lateinit var storyId: String
    lateinit var title: String
    lateinit var userAdapter: UserAdapter
    lateinit var userList: List<User>
    lateinit var idList: List<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_users)

        id = intent.getStringExtra("id")!!
        storyId = intent.getStringExtra("storyId")!!
        title = intent.getStringExtra("title")!!
        toolbar.title = title.capitalize()
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            finish()
        }
        val recyclerView = recycler_view
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(this)
        userList = ArrayList()
        userAdapter = UserAdapter(this, userList as ArrayList<User>)
        recyclerView.adapter = userAdapter
        idList = ArrayList()

        when (title) {
            "likes" -> getLikes()
            "following" -> getFollowing()
            "followers" -> getFollowers()
            "views" -> getViews()
        }

    }

    private fun getViews() {
        val ref = FirebaseDatabase.getInstance().reference
            .child("Story").child(id)
            .child(storyId).child("views")

        ref.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {}

            override fun onDataChange(p0: DataSnapshot) {

                (idList as ArrayList<String>).clear()
                for (snapshot in p0.children) {
                    (idList as ArrayList<String>).add(snapshot.key.toString())
                }
                shoUsers()
            }
        })
    }


    private fun getFollowers() {
        val followersRef = FirebaseDatabase.getInstance().reference
            .child("Follow").child(id)
            .child("Followers")

        followersRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {}

            override fun onDataChange(p0: DataSnapshot) {

                (idList as ArrayList<String>).clear()
                for (snapshot in p0.children) {
                    (idList as ArrayList<String>).add(snapshot.key.toString())
                }
                shoUsers()
            }
        })
    }

    private fun getFollowing() {
        val followersRef = FirebaseDatabase.getInstance().reference
            .child("Follow").child(id)
            .child("Following")

        followersRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {}

            override fun onDataChange(p0: DataSnapshot) {

                (idList as ArrayList<String>).clear()
                for (snapshot in p0.children) {
                    (idList as ArrayList<String>).add(snapshot.key.toString())
                }
                shoUsers()

            }
        })
    }

    private fun getLikes() {

        val likesRef = FirebaseDatabase.getInstance().reference.child("Likes").child(id)
        likesRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {}
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    (idList as ArrayList<String>).clear()
                    for (snapshot in p0.children) {
                        (idList as ArrayList<String>).add(snapshot.key.toString())
                    }
                    shoUsers()
                }
            }
        })
    }

    private fun shoUsers() {

        val usersRef = FirebaseDatabase.getInstance().reference.child("Users")
        usersRef.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                (userList as ArrayList<User>).clear()
                for (snapshot in dataSnapshot.children) {
                    val user = snapshot.getValue(User::class.java)
                    for (id in idList) {
                        if (user!!.getUID() == id) {
                            (userList as ArrayList<User>).add(user)
                        }
                    }
                }
                userAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(p0: DatabaseError) {}
        })
    }
}
