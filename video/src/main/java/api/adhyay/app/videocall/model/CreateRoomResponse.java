package api.adhyay.app.videocall.model;

 public class CreateRoomResponse {

    private String status;
    private String id;
    private String password;
    private String type;

     public CreateRoomResponse(String status, String id, String password, String type) {
         this.status = status;
         this.id = id;
         this.password = password;
         this.type = type;
     }

     public String getStatus() {
         return status;
     }

     public String getId() {
         return id;
     }

     public String getPassword() {
         return password;
     }

     public String getType() {
         return type;
     }
 }

