package in.com.testbook.chatapp.Activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.media.Image;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import in.com.testbook.chatapp.R;

public class UserProfileActivity extends AppCompatActivity {

    private ImageView mProfileImage;
    private TextView mProfileName, mProfileStatus, mProfileFriendsCount;
    private Button mProfileSendReqBtn, mDeclineBtn;

    private DatabaseReference mUsersDatabase;

    private ProgressDialog mProgressDialog;
    private DatabaseReference mFriendReqDatabase;
    private DatabaseReference mFriendDatabase;
    private DatabaseReference mRootRef;

    private FirebaseUser mCurrent_user;

    private String mCurrent_state;
    Toolbar  mToolbar;

    String user_id;
    ImageView user_single_online_icon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_userprofile);

        user_id = getIntent().getStringExtra("user_id");

        mRootRef = FirebaseDatabase.getInstance().getReference();

        initControls(); // initialize controls
        initDatabase(); // initialize databse for friends and notification
        getProfileData(); // get profile and update data notify

        clickListener(); // click of controls

    }

    private void clickListener() {
        mProfileSendReqBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mProfileSendReqBtn.setEnabled(false);

                if (mCurrent_state.equals("not_friends")) { // not friend
                    updateFriend();
                }
                if (mCurrent_state.equals("req_sent")) { // if already other sent Request
                    removeFriend();
                }
                if (mCurrent_state.equals("req_received")) { // For Recieve Friend Request
                    friendRequestReceived();
                }
                if (mCurrent_state.equals("friends")) {
                    unFriend();
                }

            }
        });
    }

    private void setOnlineOffline(){
        DatabaseReference UsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        UsersDatabase.child(user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(dataSnapshot.hasChild("online")) {

                    String userOnline = dataSnapshot.child("online").getValue().toString();
                    if(userOnline.equals("true")){
                        user_single_online_icon.setVisibility(View.VISIBLE);
                    }else{
                        user_single_online_icon.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void unFriend() {
        Map unfriendMap = new HashMap();
        unfriendMap.put("Friends/" + mCurrent_user.getUid() + "/" + user_id, null);
        unfriendMap.put("Friends/" + user_id + "/" + mCurrent_user.getUid(), null);

        mRootRef.updateChildren(unfriendMap, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {


                if (databaseError == null) {

                    mCurrent_state = "not_friends";
                    mProfileSendReqBtn.setText("Send Friend Request");

                    mDeclineBtn.setVisibility(View.INVISIBLE);
                    mDeclineBtn.setEnabled(false);

                } else {

                    String error = databaseError.getMessage();

                    Toast.makeText(UserProfileActivity.this, error, Toast.LENGTH_SHORT).show();

                }

                mProfileSendReqBtn.setEnabled(true);

            }
        });
    }

    private void friendRequestReceived() {
        final String currentDate = DateFormat.getDateTimeInstance().format(new Date());

        Map friendsMap = new HashMap();
        friendsMap.put("Friends/" + mCurrent_user.getUid() + "/" + user_id + "/date", currentDate);
        friendsMap.put("Friends/" + user_id + "/" + mCurrent_user.getUid() + "/date", currentDate);

        friendsMap.put("Friend_req/" + mCurrent_user.getUid() + "/" + user_id, null);
        friendsMap.put("Friend_req/" + user_id + "/" + mCurrent_user.getUid(), null);

        mRootRef.updateChildren(friendsMap, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                if (databaseError == null) {
                    mProfileSendReqBtn.setEnabled(true);
                    mCurrent_state = "friends";
                    mProfileSendReqBtn.setVisibility(View.INVISIBLE);
                    mDeclineBtn.setVisibility(View.INVISIBLE);
                    mDeclineBtn.setEnabled(false);

                } else {

                    String error = databaseError.getMessage();

                    Toast.makeText(UserProfileActivity.this, error, Toast.LENGTH_SHORT).show();


                }

            }
        });
    }

    private void removeFriend() {
        mFriendReqDatabase.child(mCurrent_user.getUid()).child(user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

                mFriendReqDatabase.child(user_id).child(mCurrent_user.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {


                        mProfileSendReqBtn.setEnabled(true);
                        mCurrent_state = "not_friends";
                        mProfileSendReqBtn.setText("Send Friend Request");

                        mDeclineBtn.setVisibility(View.INVISIBLE);
                        mDeclineBtn.setEnabled(false);


                    }
                });

            }
        });
    }

    private void updateFriend() {
        DatabaseReference newNotificationref = mRootRef.child("notifications").child(user_id).push();
        String newNotificationId = newNotificationref.getKey();

        HashMap<String, String> notificationData = new HashMap<>();
        notificationData.put("from", mCurrent_user.getUid());
        notificationData.put("type", "request");

        Map requestMap = new HashMap();
        requestMap.put("Friend_req/" + mCurrent_user.getUid() + "/" + user_id + "/request_type", "sent");
        requestMap.put("Friend_req/" + user_id + "/" + mCurrent_user.getUid() + "/request_type", "received");
        requestMap.put("notifications/" + user_id + "/" + newNotificationId, notificationData);

        mRootRef.updateChildren(requestMap, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                if (databaseError != null) {

                    Toast.makeText(UserProfileActivity.this, "There was some error in sending request", Toast.LENGTH_SHORT).show();

                } else {

                    mCurrent_state = "req_sent";
                    mProfileSendReqBtn.setText("Cancel Friend Request");
                }

                mProfileSendReqBtn.setEnabled(true);

            }
        });
    }

    private void getProfileData() {

        setOnlineOffline();
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle("Loading User Data");
        mProgressDialog.setMessage("Please...");
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.show();


        mUsersDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {


                try{
                    String display_name = dataSnapshot.child("fname").getValue().toString() + dataSnapshot.child("lname").getValue().toString();
                    String status = dataSnapshot.child("status").getValue().toString();
                    String image = dataSnapshot.child("image").getValue().toString();

                    mProfileName.setText(display_name);
                    mProfileStatus.setText(status);

                    Picasso.with(UserProfileActivity.this).load(image).placeholder(R.drawable.default_avatar).into(mProfileImage);
                }catch (Exception e){

                }


                if (mCurrent_user.getUid().equals(user_id)) {

                    mDeclineBtn.setEnabled(false);
                    mDeclineBtn.setVisibility(View.INVISIBLE);

                    mProfileSendReqBtn.setEnabled(false);
                    mProfileSendReqBtn.setVisibility(View.INVISIBLE);
                }

                // Friends List

                mFriendReqDatabase.child(mCurrent_user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (dataSnapshot.hasChild(user_id)) {

                            String req_type = dataSnapshot.child(user_id).child("request_type").getValue().toString();

                            if (req_type.equals("received")) {
                                mCurrent_state = "req_received";
                                mProfileSendReqBtn.setText("Accept Friend Request");

                                mDeclineBtn.setVisibility(View.VISIBLE);
                                mDeclineBtn.setEnabled(true);
                            } else if (req_type.equals("sent")) {
                                mCurrent_state = "req_sent";
                                mProfileSendReqBtn.setVisibility(View.INVISIBLE);
                                mDeclineBtn.setVisibility(View.INVISIBLE);
                                mDeclineBtn.setEnabled(false);
                            }
                            mProgressDialog.dismiss();
                        } else {
                            mFriendDatabase.child(mCurrent_user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    if (dataSnapshot.hasChild(user_id)) {

                                        mCurrent_state = "friends";
                                        mProfileSendReqBtn.setVisibility(View.INVISIBLE);
                                        mDeclineBtn.setVisibility(View.INVISIBLE);
                                        mDeclineBtn.setEnabled(false);
                                    }
                                    mProgressDialog.dismiss();
                                }
                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    mProgressDialog.dismiss();

                                }
                            });

                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void initDatabase() {
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(user_id);
        mFriendReqDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_req");
        mFriendDatabase = FirebaseDatabase.getInstance().getReference().child("Friends");
        mCurrent_user = FirebaseAuth.getInstance().getCurrentUser();
        mCurrent_state = "not_friends";


    }

    private void initControls() {
        mToolbar = (Toolbar) findViewById(R.id.register_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("User Profile");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mProfileImage = (ImageView) findViewById(R.id.profile_image);
        mProfileName = (TextView) findViewById(R.id.profile_displayName);
        mProfileStatus = (TextView) findViewById(R.id.profile_status);
        mProfileFriendsCount = (TextView) findViewById(R.id.profile_totalFriends);
        mProfileSendReqBtn = (Button) findViewById(R.id.profile_send_req_btn);
        mDeclineBtn = (Button) findViewById(R.id.profile_decline_btn);
        user_single_online_icon=(ImageView)findViewById(R.id.user_single_online_icon);

        mDeclineBtn.setVisibility(View.INVISIBLE);
        mDeclineBtn.setEnabled(false);

    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
