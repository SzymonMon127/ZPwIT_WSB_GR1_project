package com.zpwit_wsb_gr1_project.fragments;

import android.animation.TimeAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.zpwit_wsb_gr1_project.R;
import com.zpwit_wsb_gr1_project.adapter.HomeAdapter;
import com.zpwit_wsb_gr1_project.adapter.NotificationAdapter;
import com.zpwit_wsb_gr1_project.adapter.StoriesAdapter;
import com.zpwit_wsb_gr1_project.chat.ChatUsersActivity;
import com.zpwit_wsb_gr1_project.model.HomeModel;
import com.zpwit_wsb_gr1_project.model.NotificationModel;
import com.zpwit_wsb_gr1_project.model.StoriesModel;
import com.zpwit_wsb_gr1_project.model.Users;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Home extends Fragment {


    private RecyclerView recyclerView;
    HomeAdapter adapter;
    private List<HomeModel> list;
    Activity activity;
    private FirebaseUser user;
    Context context1;
    RecyclerView storiesRecyclerView;
    List<StoriesModel> storiesModelList;
    StoriesAdapter storiesAdapter;
    OnDataPass2 onDataPass2;

    public Home() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        context1 = context;
        onDataPass2 = (OnDataPass2) context;

    }

    public interface OnDataPass2 {
        void onChange2(String uid);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        activity = getActivity();
        init(view);


        list = new ArrayList<>();
        adapter = new HomeAdapter(list, getActivity());
        recyclerView.setAdapter(adapter);
        loadDataFromFirestore();

        SwipeRefreshLayout swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadDataFromFirestore();

                adapter.notifyDataSetChanged(); // Odświeżenie adaptera
                swipeRefreshLayout.setRefreshing(false); // Zakończenie animacji odświeżania
            }
        });

        adapter.OnPressed(new HomeAdapter.OnPressed() {
            @Override
            public void onLiked(int position, String id, String uid, List<String> likeList, boolean isChecked) {
                DocumentReference reference = FirebaseFirestore.getInstance().collection("Users")
                        .document(uid)
                        .collection("Post Images")
                        .document(id);

                if (likeList.contains(user.getUid()) ) {
                    likeList.remove(user.getUid()); // unlike
                } else {
                    likeList.add(user.getUid()); // like
                }

                Map<String, Object> map = new HashMap<>();
                map.put("likes", likeList);

                reference.update(map);
                try {
                    adapter.notifyDataSetChanged();
                }
                catch (Exception e)
                {

                }

            }

        });

        view.findViewById(R.id.sendBtn).setOnClickListener(v -> {

            Intent intent = new Intent(getActivity(), ChatUsersActivity.class);
            startActivity(intent);

        });

        clickListener();
    }


    private void loadDataFromFirestore() {




        final DocumentReference reference = FirebaseFirestore.getInstance().collection("Users")
                .document(user.getUid());

        reference.addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.d("Error: ", error.getMessage());
                return;
            }

            if (value == null)
            {
                return;
            }

            List<String> uidList = (List<String>) value.get("following");

            if (uidList == null || uidList.isEmpty())
            {
                return;
            }
            loadtable(uidList);
            loadStories(uidList);
        });
    }

    private void loadtable(List<String> followingList) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        list.clear(); // Wyczyść listę przed załadowaniem nowych danych

        // Utwórz zapytanie "whereIn" na podstawie listy "followingList"
        Query query = db.collection("Users")
                .whereIn("uid", followingList);

        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot snapshot : task.getResult()) {
                        String uid = snapshot.getId();
                        CollectionReference postImagesCollectionReference = snapshot.getReference().collection("Post Images");

                        postImagesCollectionReference.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    for (QueryDocumentSnapshot postSnapshot : task.getResult()) {
                                        if (!postSnapshot.exists()) {
                                            continue;
                                        }

                                        HomeModel model = postSnapshot.toObject(HomeModel.class);

                                        // Sprawdź, czy dany element już istnieje na liście "list", aby uniknąć duplikatów
                                        if (!list.contains(model)) {
                                            list.add(new HomeModel(
                                                    model.getName(),
                                                    model.getProfileImage(),
                                                    model.getImageUrl(),
                                                    model.getUid(),
                                                    model.getDescription(),
                                                    model.getId(),
                                                    model.getTimestamp(),
                                                    model.getLikes()));
                                        }
                                    }

                                    // Aktualizuj listę "list" i notyfikuj adapter po zakończeniu pętli dla danego użytkownika
                                    Collections.sort(list, (o1, o2) -> o2.getTimestamp().compareTo(o1.getTimestamp()));
                                    adapter.notifyDataSetChanged();
                                } else {
                                    Log.d("Error: ", task.getException().getMessage());
                                }
                            }
                        });
                    }
                } else {
                    Log.d("Error: ", task.getException().getMessage());
                }
            }
        });
    }


    void loadStories(List<String> followingList) {
        FirebaseFirestore.getInstance().collection("Stories")
                .whereIn("uid", followingList)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.d("Error: ", error.getMessage());
                        return;
                    }

                    storiesModelList.clear(); // clear the list before adding new data

                    for (QueryDocumentSnapshot snapshot : value) {
                        StoriesModel model = snapshot.toObject(StoriesModel.class);
                        storiesModelList.add(model);
                    }

                    storiesAdapter.notifyDataSetChanged();
                });
    }

    private void init(View view) {
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(context1));

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        if (getActivity() != null)
        {
            ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);

        }

        storiesRecyclerView = view.findViewById(R.id.storiesRecyclerView);
        storiesRecyclerView.setHasFixedSize(true);
        storiesRecyclerView
                .setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        storiesModelList = new ArrayList<>();
        storiesModelList.add(new StoriesModel("", "", "", "", ""));
        storiesAdapter = new StoriesAdapter(storiesModelList, getActivity());
        storiesRecyclerView.setAdapter(storiesAdapter);


        FirebaseAuth auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
    }

    private void clickListener() {
        adapter.OnNotClicked2(new HomeAdapter.OnNotClicked2() {
            @Override
            public void onClicked(String uid) {
                onDataPass2.onChange2(uid);
            }
        });
    }


    @Override
    public void onPause() {
        super.onPause();
        onDestroyView();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}