package com.example.instagram.Fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.instagram.Adapter.PostAdapter
import com.example.instagram.Model.Post

import com.example.instagram.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.fragment_post_details.view.*

/**
 * A simple [Fragment] subclass.
 */
class PostDetailsFragment : Fragment() {

    private lateinit var postAdapter: PostAdapter
    private lateinit var postList: MutableList<Post>
    private lateinit var postId: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_post_details, container, false)


        val preferences = context?.getSharedPreferences("PREFS", Context.MODE_PRIVATE)
        if (preferences != null) {
            postId = preferences.getString("postId","none").toString()
        }
        val recyclerView = view.recycler_view_post_details
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(context)
        postList = ArrayList()
        postAdapter = context?.let { PostAdapter(it, postList as ArrayList<Post>) }!!
        recyclerView.adapter = postAdapter

        retrievePosts()
        return view
    }

    private fun retrievePosts() {

        val postsRef = FirebaseDatabase.getInstance().reference.child("Posts").child(postId)
        postsRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {}

            override fun onDataChange(p0: DataSnapshot) {
                postList.clear()
                val post = p0.getValue(Post::class.java)
                postList.add(post!!)
                postAdapter.notifyDataSetChanged()
            }
        })
    }
}
