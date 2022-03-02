package api.adhyay.app.videocall.api;


import api.adhyay.app.videocall.model.CreateRoomResponse;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface Api {

  @FormUrlEncoded
  @POST("createRoom.php")
  Call<CreateRoomResponse> getCreateRoomResponse(
          @Field("corp_id") String corpId,
          @Field("service_key") String serviceKey,
          @Field("user_id") String userId,
          @Field("user_key") String userKey,
          @Field("room_type") String roomType);

}
