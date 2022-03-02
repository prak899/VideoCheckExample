package api.adhyay.app.videocall.model;

 public class CreateRoom {

     private final String corpId;
     private final String service_key;
     private final String userId;
     private final String userKey;
     private final String roomType;

     public CreateRoom(String corpId, String service_key, String userId, String userKey, String roomType) {
         this.corpId = corpId;
         this.service_key = service_key;
         this.userId = userId;
         this.userKey = userKey;
         this.roomType = roomType;
     }

     public String getCorpId() {
         return corpId;
     }

     public String getService_key() {
         return service_key;
     }

     public String getUserId() {
         return userId;
     }

     public String getUserKey() {
         return userKey;
     }

     public String getRoomType() {
         return roomType;
     }
 }
