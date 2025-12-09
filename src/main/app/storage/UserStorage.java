package app.storage;


import app.models.User;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class UserStorage {
    private static final String PATH = "users.json";

    public static List<User> loadUsers(){
        try (FileReader reader = new FileReader(PATH)){
            Type type = new TypeToken<List<User>>(){}.getType();
            return new Gson().fromJson(reader, type);
        } catch(Exception e){
            return new ArrayList<>();
    
        }
    }

    public static void saveUsers(List<User> users) {
        try (FileWriter writer = new FileWriter(PATH)){
            new Gson(). toJson(users, writer);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
