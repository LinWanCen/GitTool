package linWanCheng.gitTool;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 林万程
 */
public class FileIO {
    public static final String PROP_FILE = "save.txt";
    public static final String GIT_PATH = "\tgitPath";
    public static final String CONFIG = "\tconfig";
    public static final String SEPARATOR = "\tseparator";
    public static final String FILE_PATH = "\tfilePath";

    public static LinkedHashMap<String, ArrayList<String>> read() {
        LinkedHashMap<String, ArrayList<String>> map = new LinkedHashMap<>();
        map.put(GIT_PATH, new ArrayList<>());
        map.put(CONFIG, new ArrayList<>());
        map.put(SEPARATOR, new ArrayList<>());
        map.put(FILE_PATH, new ArrayList<>());
        String configName = "";
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(PROP_FILE))) {
            String s;
            while ((s = bufferedReader.readLine()) != null) {
                if (map.containsKey(s)) {
                    configName = s;
                } else {
                    map.get(configName).add(s);
                }
            }
        } catch (Exception ignored) {
        }
        return map;
    }

    public static void save(LinkedHashMap<String, ArrayList<String>> map) {
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(PROP_FILE))) {
            for (String s : map.keySet()) {
                bufferedWriter.write(s);
                bufferedWriter.newLine();
                for (String line : map.get(s)) {
                    bufferedWriter.write(line);
                    bufferedWriter.newLine();
                }
            }
        } catch (Exception ignored) {
        }
    }
}
