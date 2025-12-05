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

    private static final String FILE_PATH = "tasks.json";
    private static final Gson gson = new Gson();

    public static void saveTasks(List<Task> tasks) {
        try (FileWriter writer = new FileWriter(FILE_PATH)) {
            gson.toJson(tasks, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<Task> loadTasks() {
        try (FileReader reader = new FileReader(FILE_PATH)) {
            Type type = new TypeToken<List<Task>>() {}.getType();
            return gson.fromJson(reader, type);
        } catch (Exception e) {
            return null; // arquivo ainda não existe
        }
    }
}
