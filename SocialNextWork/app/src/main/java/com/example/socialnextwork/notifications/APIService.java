package com.example.socialnextwork.notifications;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {

    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAAuuEgKbo:APA91bHv55wfamv2g9_p0fZx66hUWNBdhys9dQ5fJsTA0E2EQs2YGK93bVtHfakcbj-N_flytZQWGJ9Sp-0jrEocrLQDp87mL3bt-UJ7pt1TH1TsDKJf2CFSqplDz94k_EQ3x98ek6RW"

    })

    @POST("fcm/send")
    Call<Response> sendNotification(@Body Sender body);
}
