package com.example.capstonedesign;

import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;
import com.google.android.gms.tasks.Task;

import java.util.HashMap;
import java.util.Map;

public class GPTRecommender {
    private final FirebaseFunctions functions;

    public GPTRecommender() {
        functions = FirebaseFunctions.getInstance("asia-northeast3");
    }

    public Task<String> getRecommendations(String userId) {
        Map<String, Object> data = new HashMap<>();
        data.put("userId", userId);

        return functions
                .getHttpsCallable("recommendPlacesByGPT")
                .call(data)
                .continueWith(task -> {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    HttpsCallableResult result = task.getResult();
                    return (String) result.getData();
                });
    }
}
