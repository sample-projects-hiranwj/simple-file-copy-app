package controller;

import com.jfoenix.controls.JFXButton;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.Rectangle;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.*;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.Optional;

public class MainFormController {
    public AnchorPane pneContainer;
    public JFXButton btnCopy;
    public Label lblPath;
    public Label lblSize;
    public Rectangle pgbLoad;
    public Rectangle pgbContainer;
    public JFXButton btnSelectFile;
    public Label lblNameSize;
    public JFXButton btnSelectDir;
    public Label lblProgress;
    private File srcFile;
    private File destDir;

    private static final DecimalFormat df = new DecimalFormat("0.00");

    public void initialize(){
        btnCopy.setDisable(true);
        double input = 3.14159265359;

        System.out.println("double : " + input);
        System.out.println("double : " + df.format(input));
    }

    public void btnSelectFileOnAction(ActionEvent actionEvent) {

        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File(System.getProperty("user.dir")));
        fileChooser.setTitle("Select a file to copy");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("All Files (*.*)", "*.*"));

        srcFile = fileChooser.showOpenDialog(lblPath.getScene().getWindow());

        if (srcFile != null) {
            lblNameSize.setText("File name: " + srcFile.getName() + "     File size: " + df.format(srcFile.length() / 1024.0) + "Kb");
        } else {
            lblNameSize.setText("No file selected");
        }

        btnCopy.setDisable(srcFile == null || destDir == null);
    }


    public void btnSelectDirOnAction(ActionEvent actionEvent) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        directoryChooser.setTitle("Select a destination folder");

        destDir = directoryChooser.showDialog(lblPath.getScene().getWindow());

        if (destDir != null) {
            lblPath.setText("Path: " + destDir.getAbsolutePath());
        } else {
            lblPath.setText("No folder selected");
        }

        btnCopy.setDisable(srcFile == null || destDir == null);
    }


    public void btnCopyOnAction(ActionEvent actionEvent) throws IOException {
        File destFile = new File(destDir, srcFile.getName());
        if (!destFile.exists()) {
            destFile.createNewFile();
        } else {
            Optional<ButtonType> result = new Alert(Alert.AlertType.INFORMATION,
                    "File already exists. Do you want to overwrite?",
                    ButtonType.YES, ButtonType.NO).showAndWait();
            if (result.get() == ButtonType.NO) {
                return;
            }
        }

        /* Why do we need a new thread here? Do you have an answer for that? */
        new Thread(()->{
            try {
                FileInputStream fis = new FileInputStream(srcFile);
                FileOutputStream fos = new FileOutputStream(destFile);

                long fileSize = srcFile.length();
                for (int i = 0; i < fileSize; i++) {
                    int readByte = fis.read();
                    fos.write(readByte);
                    int k = i;  // <- Don't think about this line yet, we will cover it very soon

                    /*
                     * What is the deal with -? Why do we need it?
                     * What happens if we remove it? Does the code still work?
                     * Remove (Unwrap) the Platform.runLater() code block, Does it work?
                     */
                    Platform.runLater(()->{
                        pgbLoad.setWidth(pgbContainer.getWidth() / fileSize * k);
                        lblProgress.setText("Progress: " + df.format(k * 1.0 / fileSize * 100) + "%");
                        lblSize.setText(df.format(k / 1024.0) + " / " + df.format(fileSize / 1024.0) + " Kb");
                    });
                }

                fos.close();
                fis.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            /* Here again, why do we need this Platform.runLater() shit? */
            Platform.runLater(()->{
                pgbLoad.setWidth(pgbContainer.getWidth());
                new Alert(Alert.AlertType.INFORMATION, "File has been copied successfully").showAndWait();
                lblSize.setText("0 / 0 Kb");
                lblProgress.setText("Progress: 0%");
                lblNameSize.setText("No folder selected");
                lblPath.setText("No file selected");
                btnCopy.setDisable(true);
                srcFile = null;
                destDir = null;
            });

        }).start();
    }




}
