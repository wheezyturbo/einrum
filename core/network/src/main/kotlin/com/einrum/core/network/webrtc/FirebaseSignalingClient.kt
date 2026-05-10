package com.einrum.core.network.webrtc

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.webrtc.IceCandidate
import org.webrtc.SessionDescription

class FirebaseSignalingClient : SignalingClient {

    private val db = FirebaseDatabase.getInstance().reference
    private var meetingId: String? = null

    private val _signalingEvents = MutableSharedFlow<SignalingEvent>()
    override val signalingEvents: SharedFlow<SignalingEvent> = _signalingEvents.asSharedFlow()

    private val scope = CoroutineScope(Dispatchers.IO)

    private var meetingListener: ValueEventListener? = null
    private var candidatesListener: ValueEventListener? = null

    override suspend fun joinMeeting(meetingId: String) {
        this.meetingId = meetingId
        val meetingRef = db.child("meetings").child(meetingId)

        // 1. Listen for Offer/Answer (SDP)
        meetingListener = meetingRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) return

                val data = snapshot.value as? Map<String, Any> ?: return

                if (data.containsKey("offer")) {
                    val offerData = data["offer"] as Map<String, String>
                    val sdp = SessionDescription(
                        SessionDescription.Type.fromCanonicalForm(offerData["type"]),
                        offerData["sdp"]
                    )
                    scope.launch { _signalingEvents.emit(SignalingEvent.OfferReceived(sdp)) }
                }

                if (data.containsKey("answer")) {
                    val answerData = data["answer"] as Map<String, String>
                    val sdp = SessionDescription(
                        SessionDescription.Type.fromCanonicalForm(answerData["type"]),
                        answerData["sdp"]
                    )
                    scope.launch { _signalingEvents.emit(SignalingEvent.AnswerReceived(sdp)) }
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        // 2. Listen for ICE Candidates
        candidatesListener = meetingRef.child("candidates").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) return
                
                for (child in snapshot.children) {
                    val candidateData = child.value as? Map<String, Any> ?: continue
                    val candidate = IceCandidate(
                        candidateData["sdpMid"] as String,
                        (candidateData["sdpMLineIndex"] as Long).toInt(),
                        candidateData["sdpCandidate"] as String
                    )
                    scope.launch { _signalingEvents.emit(SignalingEvent.IceCandidateReceived(candidate)) }
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    override suspend fun sendOffer(offer: SessionDescription) {
        val id = meetingId ?: return
        val offerMap = hashMapOf(
            "type" to offer.type.canonicalForm(),
            "sdp" to offer.description
        )
        db.child("meetings").child(id).child("offer").setValue(offerMap).await()
    }

    override suspend fun sendAnswer(answer: SessionDescription) {
        val id = meetingId ?: return
        val answerMap = hashMapOf(
            "type" to answer.type.canonicalForm(),
            "sdp" to answer.description
        )
        db.child("meetings").child(id).child("answer").setValue(answerMap).await()
    }

    override suspend fun sendIceCandidate(candidate: IceCandidate, isLocalUser: Boolean) {
        val id = meetingId ?: return
        val candidateMap = hashMapOf(
            "sdpMid" to candidate.sdpMid,
            "sdpMLineIndex" to candidate.sdpMLineIndex,
            "sdpCandidate" to candidate.sdp
        )
        db.child("meetings").child(id).child("candidates").push().setValue(candidateMap).await()
    }

    override suspend fun leaveMeeting() {
        val id = meetingId ?: return
        val meetingRef = db.child("meetings").child(id)
        meetingListener?.let { meetingRef.removeEventListener(it) }
        candidatesListener?.let { meetingRef.child("candidates").removeEventListener(it) }
        
        meetingRef.removeValue().await()
        meetingId = null
    }
}
