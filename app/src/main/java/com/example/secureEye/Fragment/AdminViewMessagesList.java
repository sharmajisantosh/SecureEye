package com.example.secureEye.Fragment;


import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.SpannableString;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;

import com.example.secureEye.Activity.AdminViewMessage;
import com.example.secureEye.Adapter.UsersMessageAdapter;
import com.example.secureEye.Interface.RecyclerItemClickListner;
import com.example.secureEye.Model.UserMessage;
import com.example.secureEye.R;
import com.example.secureEye.Utils.Constant_URLS;
import com.example.secureEye.Utils.TypefaceSpan;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class AdminViewMessagesList extends Fragment {

    private static final String TAG = "AdminViewMessagesList";
    private FirebaseAuth mAuth;
    private RecyclerView userMessageRecycler;
    private List<UserMessage> messageList;
    private CollectionReference adminProfileRef;
    private UsersMessageAdapter messageAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_admin_view_message_list, container, false);
        mAuth=FirebaseAuth.getInstance();

        SpannableString str = new SpannableString("Users Message List");
        str.setSpan(new TypefaceSpan(getActivity(), TypefaceSpan.fontName), 0, str.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        getActivity().setTitle(str);

        userMessageRecycler=view.findViewById(R.id.recyclerAllMessagesList);
        userMessageRecycler.setHasFixedSize(false);

        messageList=new ArrayList<>();

        adminProfileRef= Constant_URLS.ADMIN_PROFILE_REF;

        setUpMessageList();

        return view;
    }

    private void setUpMessageList() {

        Query query=adminProfileRef.document(mAuth.getUid()).collection("usersMessage")
                .orderBy("timeStamp", Query.Direction.DESCENDING);
        FirestoreRecyclerOptions<UserMessage> options=new FirestoreRecyclerOptions.Builder<UserMessage>()
                .setQuery(query,UserMessage.class)
                .build();
        messageAdapter=new UsersMessageAdapter(options, new RecyclerItemClickListner() {
            @Override
            public void onItemClickListner(int position) {
                UserMessage userMessage=messageAdapter.getItem(position);

                Intent intent=new Intent(getActivity(), AdminViewMessage.class);
                intent.putExtra("userMessage", userMessage);
                startActivity(intent);

                if (!userMessage.isRead()) {
                    DocumentSnapshot snapshot = options.getSnapshots().getSnapshot(position);
                    adminProfileRef.document(mAuth.getUid()).collection("usersMessage").document(snapshot.getId())
                            .update("read", true).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                        }
                    });
                }
            }
        });
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        userMessageRecycler.setLayoutManager(mLayoutManager);
        userMessageRecycler.setItemAnimator(new DefaultItemAnimator());
        userMessageRecycler.addItemDecoration(new DividerItemDecoration(getActivity(), 0));
        final LayoutAnimationController controller = AnimationUtils.loadLayoutAnimation(getActivity(), R.anim.layout_animation_right_to_left);
        userMessageRecycler.setLayoutAnimation(controller);
        userMessageRecycler.setAdapter(messageAdapter);



    }

    @Override
    public void onStart() {
        super.onStart();
        messageAdapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        messageAdapter.stopListening();
    }
}
