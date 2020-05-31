package com.example.instagram.Fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.instagram.Adapter.PostAdapter
import com.example.instagram.Adapter.StoryAdapter
import com.example.instagram.Model.Post
import com.example.instagram.Model.Story
import com.example.instagram.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.fragment_home.view.*

class HomeFragment : Fragment() {

    private var postAdapter: PostAdapter? = null
    private var postList: MutableList<Post>? = null
    private var followinglist: MutableList<String>? = null
    private lateinit var storyAdapter: StoryAdapter
    private lateinit var storyList: MutableList<Story>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        val recyclerView = view.recycler_view_home
        val linearLayoutManager = LinearLayoutManager(context)
        linearLayoutManager.reverseLayout = true
        linearLayoutManager.stackFromEnd = true
        recyclerView.layoutManager = linearLayoutManager

        postList = ArrayList()
        postAdapter = context?.let { PostAdapter(it, postList as ArrayList<Post>) }
        recyclerView.adapter = postAdapter

        val recyclerViewStory = view.recycler_view_story
        recyclerViewStory.setHasFixedSize(true)
        val linearLayoutManager2 =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        recyclerViewStory.layoutManager = linearLayoutManager2

        storyList = ArrayList()
        storyAdapter = StoryAdapter(context!!, storyList as ArrayList<Story>)
        recyclerViewStory.adapter = storyAdapter

        checkFollowings()
        return view
    }

    private fun checkFollowings() {

        followinglist = ArrayList()

        val followingRef = FirebaseDatabase.getInstance().reference
            .child("Follow").child(FirebaseAuth.getInstance().currentUser!!.uid)
            .child("Following")

        followingRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {}

            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    (followinglist as ArrayList<String>).clear()
                    for (snapshot in p0.children) {
                        snapshot.key?.let {
                            (followinglist as ArrayList<String>).add(it)
                        }
                        retrievePosts()
                        retrieveStories()

                    }
                }
            }

        })

    }

    private fun retrieveStories() {
        val storyRef = FirebaseDatabase.getInstance().reference.child("Story")

        storyRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                val timeCurrent = System.currentTimeMillis()
                (storyList as ArrayList<Story>).clear()
                (storyList as ArrayList<Story>).add(Story(userid = FirebaseAuth.getInstance().currentUser!!.uid))
                for (id in followinglist!!) {
                    var countStory = 0
                    var story: Story? = null
                    for (snapshot in p0.child(id.toString()).children) {
                        story = snapshot.getValue(Story::class.java)
                        if (timeCurrent > story!!.timestart && timeCurrent < story.timeend) {
                            countStory++
                        }
                    }
                    if (countStory > 0) {
                        (storyList as ArrayList<Story>).add(story!!)
                    }
                }
                storyAdapter.notifyDataSetChanged()

            }

            override fun onCancelled(p0: DatabaseError) {}
        })
    }

    private fun retrievePosts() {

        val postsRef = FirebaseDatabase.getInstance().reference.child("Posts")
        postsRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {}

            override fun onDataChange(p0: DataSnapshot) {
                postList?.clear()
                for (snapshot in p0.children) {
                    val post = snapshot.getValue(Post::class.java)
                    for (id in (followinglist as ArrayList<*>)) {
                        if (post!!.publisher == id) {
                            postList!!.add(post)
                        }
                        postAdapter!!.notifyDataSetChanged()
                    }
                }
            }
        })
    }
}
