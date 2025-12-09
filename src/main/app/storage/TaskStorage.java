package app.storage;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

import app.models.Task;

public class TaskStorage {

    private static String currentUser = null;

    public static void setCurrentUser(String usuario){
        currentUser = usuario;
    }

    private static String getUserTaskFile() {
        return "tasks_" + currentUser + ".json";
    }

    private static final Gson gson = new Gson();

    public static void saveTasks(List<Task> tasks) {
        try (FileWriter writer = new FileWriter(getUserTaskFile())) {
            gson.toJson(tasks, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<Task> loadTasks() {
        try (FileReader reader = new FileReader(getUserTaskFile())) {
            Type type = new TypeToken<List<Task>>() {}.getType();
            return gson.fromJson(reader, type);
        } catch (Exception e) {
            return null; // arquivo ainda não existe
        }
    }
}
