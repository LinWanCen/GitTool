package linWanCheng.gitTool;

import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

import java.util.*;

/**
 * JavaFX Controller
 */
public class Controller {

    public GitTool gitTool = new GitTool();
    public Stage primaryStage;
    public Scene scene;
    public LinkedHashMap<String, ArrayList<String>> map;
    public ComboBox gitPath;
    public ComboBox config;
    public ComboBox separator;
    public TextArea filePath;
    public TextArea logInfo;

    public void getLogInfo(ActionEvent actionEvent) {
        Object spt = separator.getValue();
        String sep = (spt == null || "\\t".equals(spt)) ? "\t" : spt.toString();

        Set<String> param = null;
        Object cfg = config.getValue();
        if (cfg != null) {
            String[] configs = {cfg.toString().trim()};
            if (!"".equals(configs[0])) {
                // save
                List<String> list = map.get(FileIO.CONFIG);
                list.remove(configs[0]);
                list.add(0, configs[0]);

                if (configs[0].contains(",")) {
                    configs = configs[0].split(",");
                }
                param = new LinkedHashSet<>();
                for (String s : configs) {
                    param.add(s.trim());
                }
            }
        }

        List<Map<String, String>> list = gitTool.logInfoMultiLine(filePath.getText(), param);
        StringBuilder logInfoBuilder = new StringBuilder();
        boolean isConfigWrite = false;
        for (Map<String, String> map : list) {
            if (param == null) {
                // show all
                for (String s : map.values()) {
                    logInfoBuilder.append(s).append(sep);
                }
                //
                if (!isConfigWrite) {
                    StringBuilder configBuilder = new StringBuilder();
                    for (String s : map.keySet()) {
                        configBuilder.append(s).append(", ");
                    }
                    config.setValue(configBuilder.toString());
                    isConfigWrite = true;
                }
            } else {
                for (String s : param) {
                    if (map.containsKey(s)) {
                        logInfoBuilder.append(map.get(s));
                    }
                    logInfoBuilder.append(sep);
                }
            }
            logInfoBuilder.append('\n');
        }
        logInfo.setText(logInfoBuilder.toString());
        // save
        ArrayList<String> sparatorList = map.get(FileIO.SEPARATOR);
        if ("\t".equals(sep)) {
            sep = "\\t";
        }
        sparatorList.remove(sep);
        sparatorList.add(0, sep);
        List<String> filePathList = map.get(FileIO.FILE_PATH);
        filePathList.clear();
        filePathList.add(0, filePath.getText());
        FileIO.save(map);
    }
}
