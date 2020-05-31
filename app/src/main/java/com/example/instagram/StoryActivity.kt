package com.example.instagram

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import com.example.instagram.Model.Story
import com.example.instagram.Model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import jp.shts.android.storiesprogressview.StoriesProgressView
import kotlinx.android.synthetic.main.activity_story.*

class StoryActivity : AppCompatActivity(), StoriesProgressView.StoriesListener {

    private lateinit var currentUserId: String
    private lateinit var userId: String
    private var counter = 0
    private var pressTime = 0L
    private var limit = 500L
    private lateinit var imagesList: List<String>
    private lateinit var storyIdList: List<String>
    private var storiesProgressView: StoriesProgressView? = null
    private val onTouchListener = View.OnTouchListener { v, event ->
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                pressTime = System.currentTimeMillis()
                storiesProgressView?.pause()
                return@OnTouchListener false
            }
            MotionEvent.ACTION_UP -> {
                val now = System.currentTimeMillis()
                storiesProgressView?.resume()
                return@OnTouchListener limit < now - pressTime
            }
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_story)
        currentUserId = FirebaseAuth.getInstance().currentUser!!.uid
        userId = intent.getStringExtra("userId")!!
        //counter = intent.getStringExtra("userId")!!
        storiesProgressView = stories_progress
        layout_seen.visibility = View.GONE
        story_delete.visibility = View.GONE
        if (userId == currentUserId) {
            layout_seen.visibility = View.VISIBLE
            story_delete.visibility = View.VISIBLE
        }
        getStories(userId)
        userInfo(userId)
        val reverse = reverse
        reverse.setOnClickListener { storiesProgressView?.reverse() }
        reverse.setOnTouchListener(onTouchListener)

        val skip = skip
        skip.setOnClickListener { storiesProgressView?.skip() }
        skip.setOnTouchListener(onTouchListener)

        seen_number.setOnClickListener {
            val intent = Intent(this, ShowUsersActivity::class.java)
            intent.putExtra("id", userId)
            intent.putExtra("storyId", storyIdList[counter])
            intent.putExtra("title", "views")
            startActivity(intent)
        }
        story_delete.setOnClickListener {
            val ref = FirebaseDatabase.getInstance().reference.child("Story").child(userId)
                .child(storyIdList[counter])
            ref.removeValue().addOnCompleteListener {
                if (it.isSuccessful) {
                    Toast.makeText(this, "Deleted Successfully", Toast.LENGTH_LONG).show()
                }
            }
        }

    }

    private fun getStories(userId: String) {
        imagesList = ArrayList()
        storyIdList = ArrayList()
        val ref = FirebaseDatabase.getInstance().reference.child("Story").child(userId)

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {}
            override fun onDataChange(p0: DataSnapshot) {
                (imagesList as ArrayList<String>).clear()
                (storyIdList as ArrayList<String>).clear()
                for (snapshot in p0.children) {
                    val story = snapshot.getValue(Story::class.java)
                    val timeCurrent = System.currentTimeMillis()
                    if (timeCurrent > story!!.timestart && timeCurrent < story.timeend) {
                        (imagesList as ArrayList<String>).add(story.imageurl)
                        (storyIdList as ArrayList<String>).add(story.storyid)
                    }
                }
                storiesProgressView?.setStoriesCount((imagesList as ArrayList<String>).size)
                storiesProgressView?.setStoryDuration(10000L)
                storiesProgressView?.setStoriesListener(this@StoryActivity)
                storiesProgressView?.startStories(counter)
                Picasso.get().load(imagesList[counter]).placeholder(R.drawable.profile)
                    .into(image_story)
                addViewToStory(storyIdList[counter])
                seenNumber(storyIdList[counter])
            }
        })
    }

    private fun userInfo(userId: String) {

        val userRef = FirebaseDatabase.getInstance().reference.child("Users").child(userId)
        userRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {}
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    val user = p0.getValue(User::class.java)
                    Picasso.get().load(user!!.getImage()).placeholder(R.drawable.profile)
                        .into(story_profileImage)
                    story_username.text = user.getUserName()
                }
            }
        })
    }

    private fun addViewToStory(storyId: String) {
        FirebaseDatabase.getInstance().reference.child("Story").child(userId).child(storyId)
            .child("views").child(currentUserId).setValue(true)

    }

    private fun seenNumber(storyId: String) {
        val ref =
            FirebaseDatabase.getInstance().reference.child("Story").child(userId).child(storyId)
                .child("views")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {}
            override fun onDataChange(p0: DataSnapshot) {
                seen_number.text = p0.childrenCount.toString()
            }
        })

    }

    override fun onComplete() {
        finish()
    }

    override fun onPrev() {
        Picasso.get().load(imagesList[--counter]).placeholder(R.drawable.profile)
            .into(image_story)

        seenNumber(storyIdList[counter])
    }

    override fun onNext() {
        Picasso.get().load(imagesList[++counter]).placeholder(R.drawable.profile)
            .into(image_story)
        addViewToStory(storyIdList[counter])
        seenNumber(storyIdList[counter])
    }

    override fun onDestroy() {
        super.onDestroy()
        storiesProgressView?.destroy()
    }

    override fun onResume() {
        super.onResume()
        storiesProgressView?.resume()
    }

    override fun onPause() {
        super.onPause()
        storiesProgressView?.pause()
    }
}
