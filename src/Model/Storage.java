package Model;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Storage {
    private ObjectMapper objectMapper;
    private String FILENAME = "data.json";
    private File file;

    public Storage() {
        this.objectMapper = new ObjectMapper();
        File file = new File(FILENAME);
        try {
            if (!file.exists()) {
                System.out.println("Created new file");
                file.createNewFile();
            }
            this.file = file;
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public void store(List<Server> servers) {
        try {
            objectMapper.writeValue(file, servers);
        } catch(Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public List<Server> retrieve() {
        List<Server> servers = new ArrayList<>();
        try {
            Scanner reader = new Scanner(file);
            String jsonArray = reader.nextLine();
            System.out.println(jsonArray);
            servers = objectMapper.readValue(jsonArray, new TypeReference<List<Server>>() {});
        } catch(Exception e) {
            System.out.println(e.getMessage());
        }
        return servers;
    }
}
