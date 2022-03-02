package api.adhyay.app.videocall.Storage;

import android.content.Context;
import android.content.SharedPreferences;

public class PrefManager {

   private static Context context;
   private static PrefManager prefManager;

    public PrefManager(Context context) {
        PrefManager.context = context;
    }

    public static synchronized PrefManager getInstance(Context context){
        if(prefManager==null)
          prefManager = new PrefManager(context);

        return prefManager;
    }

    public void saveDetails(String name, String id) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("LoginDetails", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("Name", name);
        editor.putString("Id", id);
        editor.apply();
    }

    public static String getName() {
        SharedPreferences sharedPreferences = context.getSharedPreferences("LoginDetails", Context.MODE_PRIVATE);
        return sharedPreferences.getString("Name", "");
    }

    public static String getId() {
        SharedPreferences sharedPreferences = context.getSharedPreferences("LoginDetails", Context.MODE_PRIVATE);
        return sharedPreferences.getString("Id", "");
    }

}
