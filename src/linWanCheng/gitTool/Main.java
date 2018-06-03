package linWanCheng.gitTool;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.stage.Stage;

import java.util.List;

public class Main extends Application {

    private Controller controller;

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("gitTool.fxml"));
        Parent root = loader.load();
        controller = loader.getController();
        controller.scene = new Scene(root, 600, 400);
        controller.primaryStage = primaryStage;
        primaryStage.setScene(controller.scene);
        primaryStage.setTitle("请填写 .git 路径");

        controller.map = FileIO.read();
        initComboBox(controller.gitPath, FileIO.GIT_PATH, new String[]{});
        initGitPath();
        initComboBox(controller.config, FileIO.CONFIG, new String[]{
                "lastName, lastTime, blameName, blameTime, 最后修改",
                "moreLineName, nameLineCount, 行最多",
                "moreCommitName, nameCommitCount, 提交最多"
        });
        initComboBox(controller.separator, FileIO.SEPARATOR, new String[]{"\\t", ","});
        List<String> list = controller.map.get(FileIO.FILE_PATH);
        StringBuilder stringBuilder = new StringBuilder();
        for (String s : list) {
            stringBuilder.append(s).append('\n');
        }
        controller.filePath.setText(stringBuilder.toString());

        primaryStage.show();
    }

    private void initComboBox(ComboBox comboBox, String configName, String[] def) {
        List<String> list = controller.map.get(configName);
        if (list.size() == 0) {
            for (String s : def) {
                list.add(s);
            }
        }
        comboBox.setItems(FXCollections.observableArrayList(list));
        if (list.size() > 0) {
            comboBox.setValue(list.get(0));
        }
    }

    private void initGitPath() {
        Object value = controller.gitPath.getValue();
        if (value != null) {
            flushPath(value.toString().trim());
        }
        controller.gitPath.valueProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                if (newValue != null && !newValue.equals(observable)) {
                    String gitPath = newValue.toString().trim();
                    flushPath(gitPath);
                    controller.map.get(FileIO.GIT_PATH).remove(gitPath);
                    controller.map.get(FileIO.GIT_PATH).add(0, gitPath);
                    FileIO.save(controller.map);
                }
            }
        });
    }

    private void flushPath(String gitPath) {
        boolean b = controller.gitTool.init(gitPath);
        if (b) {
            controller.primaryStage.setTitle(".git 路径更新完成");
        } else {
            controller.primaryStage.setTitle(".git 路径更新失败");
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
