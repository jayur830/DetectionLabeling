package DetectionLabeling;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.control.cell.*;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.converter.IntegerStringConverter;
import jnr.ffi.annotations.In;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.UnaryOperator;

class BoundingBoxLabelData {
    private final double SIZE = 420.0, GRID_SIZE = 7.0;
    private StringProperty _gridX, _gridY, _x, _y, _width, _height, _confidence, _class;
    private double gridXSrc, gridYSrc, xSrc, ySrc, widthSrc, heightSrc, confidenceSrc, classSrc;

    public BoundingBoxLabelData(double gridX, double gridY, double x, double y,
                                double width, double height, double confidence, double _class) {
        this.gridXSrc = gridX;
        this.gridYSrc = gridY;
        this.xSrc = x;
        this.ySrc = y;
        this.widthSrc = width;
        this.heightSrc = height;
        this.confidenceSrc = confidence;
        this.classSrc = _class;
        this._gridX = new SimpleStringProperty(Double.toString(gridX));
        this._gridY = new SimpleStringProperty(Double.toString(gridY));
        this._x = new SimpleStringProperty(Double.toString((x / (this.SIZE / this.GRID_SIZE)) - gridX));
        this._y = new SimpleStringProperty(Double.toString((y / (this.SIZE / this.GRID_SIZE)) - gridY));
        this._width = new SimpleStringProperty(Double.toString(width / this.SIZE));
        this._height = new SimpleStringProperty(Double.toString(height / this.SIZE));
        this._confidence = new SimpleStringProperty(Double.toString(confidence));
        this._class = new SimpleStringProperty(Double.toString(_class));
    }

    public BoundingBoxLabelData(StringProperty gridX, StringProperty gridY, StringProperty x, StringProperty y,
                                StringProperty width, StringProperty height, StringProperty confidence, StringProperty classNum) {
        this._gridX = gridX;
        this._gridY = gridY;
        this._x = x;
        this._y = y;
        this._width = width;
        this._height = height;
        this._confidence = confidence;
        this._class = classNum;
        this.gridXSrc = Double.parseDouble(this._gridX.get());
        this.gridYSrc = Double.parseDouble(this._gridY.get());
        this.xSrc = ((this.gridXSrc + Double.parseDouble(this._x.get())) / this.GRID_SIZE) * this.SIZE;
        this.ySrc = ((this.gridYSrc + Double.parseDouble(this._y.get())) / this.GRID_SIZE) * this.SIZE;
        this.widthSrc = Double.parseDouble(this._width.get()) * this.SIZE;
        this.heightSrc = Double.parseDouble(this._height.get()) * this.SIZE;
        this.confidenceSrc = Double.parseDouble(this._confidence.get());
        this.classSrc = Double.parseDouble(this._class.get());
    }

    public StringProperty getGridX() {
        return this._gridX;
    }

    public void setGridX(double gridX) {
        this._gridX = new SimpleStringProperty(Double.toString(gridX));
        this.gridXSrc = gridX;
    }

    public StringProperty getGridY() {
        return this._gridY;
    }

    public void setGridY(double gridY) {
        this._gridY = new SimpleStringProperty(Double.toString(gridY));
        this.gridYSrc = gridY;
    }

    public StringProperty getX() {
        return this._x;
    }

    public void setX(double x) {
        this._x = new SimpleStringProperty(Double.toString((x / (this.SIZE / this.GRID_SIZE)) - this.gridXSrc));
        this.xSrc = x;
    }

    public StringProperty getY() {
        return this._y;
    }

    public void setY(double y) {
        this._y = new SimpleStringProperty(Double.toString((y / (this.SIZE / this.GRID_SIZE)) - this.gridYSrc));
        this.ySrc = y;
    }

    public StringProperty getWidth() {
        return this._width;
    }

    public void setWidth(double width) {
        this._width = new SimpleStringProperty(Double.toString(width / this.SIZE));
        this.widthSrc = width;
    }

    public StringProperty getHeight() {
        return this._height;
    }

    public void setHeight(double height) {
        this._height = new SimpleStringProperty(Double.toString(height / this.SIZE));
        this.heightSrc = height;
    }

    public StringProperty getConfidence() {
        return this._confidence;
    }

    public void setConfidence(double confidence) {
        this._confidence = new SimpleStringProperty(Double.toString(confidence));
        this.confidenceSrc = confidence;
    }

    public StringProperty getClassName() {
        return this._class;
    }

    public void setClassName(double className) {
        this._class = new SimpleStringProperty(Double.toString(className));
        this.classSrc = className;
    }

    public double getGridXSrc() {
        return this.gridXSrc;
    }

    public double getGridYSrc() {
        return this.gridYSrc;
    }

    public double getXSrc() {
        return this.xSrc;
    }

    public double getYSrc() {
        return this.ySrc;
    }

    public double getWidthSrc() {
        return this.widthSrc;
    }

    public double getHeightSrc() {
        return this.heightSrc;
    }

    public double getConfidenceSrc() {
        return this.confidenceSrc;
    }

    public double getClassNameSrc() {
        return this.classSrc;
    }
}

class BoundingBox {
    public Vertex[] vertices;
    public Line[] lines;

    public BoundingBox(double x1, double y1, double x2, double y2, double padding) {
        if (x1 > x2) {
            double[] loc = swap(new double[]{x1, x2}, 0, 1);
            x1 = loc[0];
            x2 = loc[1];
        }
        if (y1 > y2) {
            double[] loc = swap(new double[]{y1, y2}, 0, 1);
            y1 = loc[0];
            y2 = loc[1];
        }
        this.vertices = new Vertex[4];
        this.lines = new Line[4];
        this.vertices[0] = new Vertex(x1, y1, padding);
        this.vertices[1] = new Vertex(x2, y1, padding);
        this.vertices[2] = new Vertex(x1, y2, padding);
        this.vertices[3] = new Vertex(x2, y2, padding);
        this.lines[0] = new Line(x1, y1, x2, y1, padding);
        this.lines[1] = new Line(x1, y1, x1, y2, padding);
        this.lines[2] = new Line(x2, y1, x2, y2, padding);
        this.lines[3] = new Line(x1, y2, x2, y2, padding);
    }

    private double[] swap(double[] arr, int i, int j) {
        double temp = arr[i];
        arr[i] = arr[j];
        arr[j] = temp;
        return arr;
    }

    public class Vertex {
        public double x1, y1, x2, y2;

        public Vertex(double x, double y, double padding) {
            this.x1 = x - padding;
            this.y1 = y - padding;
            this.x2 = x + padding;
            this.y2 = y + padding;
        }
    }

    public class Line {
        public double x1, y1, x2, y2;

        public Line(double x1, double y1, double x2, double y2, double padding) {
            if (x1 >= x2) {
                double[] loc = swap(new double[]{x1, x2}, 0, 1);
                x1 = loc[0];
                x2 = loc[1];
            }
            if (y1 >= y2) {
                double[] loc = swap(new double[]{y1, y2}, 0, 1);
                y1 = loc[0];
                y2 = loc[1];
            }
            this.x1 = x1 - padding;
            this.y1 = y1 - padding;
            this.x2 = x2 + padding;
            this.y2 = y2 + padding;
        }
    }
}

public class LocalizationLabeling extends Application implements Initializable {
    private Stage stage;
    @FXML
    private AnchorPane ui;
    @FXML
    private Canvas image, drawArea;
    @FXML
    private Button selectButton, drawButton, addButton, deleteButton, clearButton, leftButton, rightButton;
    @FXML
    private Label grid, x, y, normX, normY, widthView, heightView, normWidthView, normHeightView;
    @FXML
    private TableView<BoundingBoxLabelData> boundingBoxConfigList;
    @FXML
    private TableColumn<BoundingBoxLabelData, String>
            gridXColumn, gridYColumn, xColumn, yColumn, widthColumn, heightColumn, confidenceColumn, classColumn;
    @FXML
    private MenuItem newProjectMenu, loadProjectMenu, loadImageMenu, loadFolderMenu, saveProjectMenu, saveAsMenu, exportMenu, exitMenu;
    private List<File> imgFile = new ArrayList<>();
    private List<Integer> selectedIndex = new ArrayList<>();
    private List<ObservableList<BoundingBoxLabelData>> list = new ArrayList<>();
    private List<List<BoundingBox>> boxList = new ArrayList<>();
    private BoundingBoxLabelData selectedData;
    private int imgIndex = -1, resizeIndex = -1;
    private boolean isDrawing = false;
    private static boolean isChanged = false;

    private final double SIZE = 420.0, GRID_SIZE = 7.0;
    private int gridXMove, gridYMove;
    private double xMove, yMove, widthOfMove, heightOfMove, confMove, classMove, xClick, yClick, resizeX = -1, resizeY = -1, fixedX = -1, fixedY = -1;
    private KeyCode pressedSubKey;

    private GraphicsContext drawContext;
    private double x1, y1, x2, y2;
    private String projectName = null;
    private FXMLLoader dialogLoader;
    private Dialog msgBoxDialog;
    private Stage msgBox;

    public static void main(String[] args) {
        launch(args);
    }

    private void setDisable(boolean disable) {
        this.selectButton.setDisable(disable);
        this.drawButton.setDisable(disable);
        this.addButton.setDisable(disable);
        this.deleteButton.setDisable(disable);
        this.clearButton.setDisable(disable);
        this.boundingBoxConfigList.setDisable(disable);
    }

    private void imageInit(String fileName) {
        System.out.println(fileName);
        GraphicsContext imageContext = this.image.getGraphicsContext2D();
        if (fileName != null) {
            imageContext.drawImage(new Image(fileName, this.SIZE, this.SIZE, false, false), 0, 0);
            for (int i = 0; i < this.SIZE; i += (int) (this.SIZE / GRID_SIZE)) {
                imageContext.strokeLine(0, i, this.SIZE, i);
                imageContext.strokeLine(i, 0, i, this.SIZE);
            }
            setDisable(false);
        } else setDisable(true);
    }

    private void setLocationLabel(double x, double y) {
        if (x >= 0 && x <= this.SIZE && y >= 0 && y <= this.SIZE) {
            this.grid.setText("grid: (" + ((int) x / (int) (this.SIZE / this.GRID_SIZE)) + ", " + ((int) y / (int) (this.SIZE / this.GRID_SIZE)) + ")");
            this.x.setText("x: " + x);
            this.y.setText("y: " + y);
            this.normX.setText("norm_x: " + ((x % (this.SIZE / this.GRID_SIZE)) / (this.SIZE / this.GRID_SIZE)));
            this.normY.setText("norm_y: " + ((y % (this.SIZE / this.GRID_SIZE)) / (this.SIZE / this.GRID_SIZE)));
        } else {
            this.grid.setText("grid: (nan, nan)");
            this.x.setText("x: nan");
            this.y.setText("y: nan");
            this.normX.setText("norm_x: nan");
            this.normY.setText("norm_y: nan");
        }
    }

    private void setBoxSizeLabel(double x1, double y1, double x2, double y2) {
        this.widthView.setText("width: " + Math.abs(x2 - x1));
        this.heightView.setText("height: " + Math.abs(y2 - y1));
        this.normWidthView.setText("norm_width: " + (Math.abs(x2 - x1) / SIZE));
        this.normHeightView.setText("norm_height: " + (Math.abs(y2 - y1) / SIZE));
    }

    private void setDefaultBoxSizeLabel() {
        this.widthView.setText("width: 0");
        this.heightView.setText("height: 0");
        this.normWidthView.setText("norm_width: 0");
        this.normHeightView.setText("norm_height: 0");
    }

    private void drawBox(Color color, double x1, double y1, double x2, double y2) {
        this.drawContext.setStroke(color);
        if (x1 <= x2 && y1 <= y2) this.drawContext.strokeRect(x1, y1, x2 - x1, y2 - y1);
        else if (x1 <= x2 && y1 > y2) this.drawContext.strokeRect(x1, y2, x2 - x1, y1 - y2);
        else if (x1 > x2 && y1 <= y2) this.drawContext.strokeRect(x2, y1, x1 - x2, y2 - y1);
        else this.drawContext.strokeRect(x2, y2, x1 - x2, y1 - y2);
    }

    private void drawBox(Color color, BoundingBoxLabelData data) {
        this.drawContext.setStroke(color);
        System.out.println(data.getXSrc() + ", " + data.getYSrc());
        this.drawContext.strokeRect(data.getXSrc() - (data.getWidthSrc() * 0.5),
                data.getYSrc() - (data.getHeightSrc() * 0.5), data.getWidthSrc(), data.getHeightSrc());
    }

    private void drawBoxList(Color color) {
        for (BoundingBoxLabelData data : this.list.get(this.imgIndex))
            if (data != selectedData) drawBox(color, data);
    }

    private void removeBox(double x1, double y1, double x2, double y2) {
        if (x1 <= x2 && y1 <= y2) this.drawContext.clearRect(x1 - 1, y1 - 1, x2 - x1 + 3, y2 - y1 + 3);
        else if (x1 <= x2 && y1 > y2) this.drawContext.clearRect(x1 - 1, y2 - 1, x2 - x1 + 3, y1 - y2 + 3);
        else if (x1 > x2 && y1 <= y2) this.drawContext.clearRect(x2 - 1, y1 - 1, x1 - x2 + 3, y2 - y1 + 3);
        else this.drawContext.clearRect(x2 - 1, y2 - 1, x1 - x2 + 3, y1 - y2 + 3);
    }

    private void removeBox(BoundingBoxLabelData data) {
        double width = data.getWidthSrc(), height = data.getHeightSrc();
        this.drawContext.clearRect(data.getXSrc() - (width * 0.5) - 1, data.getYSrc() - (height * 0.5) - 1, width + 3, height + 3);
    }

    private void removeSelectedBox() {
        if (this.selectedIndex.get(this.imgIndex) != -1) {
            BoundingBoxLabelData data = this.boundingBoxConfigList.getSelectionModel().getSelectedItem();
            removeBox(data);
            this.list.get(this.imgIndex).remove(this.selectedIndex.get(this.imgIndex), this.selectedIndex.get(this.imgIndex) + 1);
            this.boxList.get(this.imgIndex).remove((int) this.selectedIndex.get(this.imgIndex));
            drawBoxList(Color.RED);
            isChanged = true;
        }
    }

    private void save(File file, String s) {
        try {
            FileWriter writer = new FileWriter(file);
            writer.write(s);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void clear(int index) {
        this.selectedIndex.set(index, -1);
        this.list.get(index).clear();
        this.boxList.get(index).clear();
        isChanged = true;
    }

    private void clear() {
        this.boundingBoxConfigList.setItems(null);
        this.imgFile.clear();
        this.selectedIndex.clear();
        this.list.clear();
        this.boxList.clear();
        this.imgIndex = -1;
        this.isDrawing = false;
        isChanged = false;
        this.image.getGraphicsContext2D().clearRect(0, 0, SIZE, SIZE);
    }

    private void intentPage(boolean isLeft) {
        if (isLeft) {
            for (BoundingBoxLabelData data : this.list.get(this.imgIndex))
                removeBox(data);
            if (this.imgIndex - 1 >= 0) --this.imgIndex;
        } else {
            for (BoundingBoxLabelData data : this.list.get(this.imgIndex))
                removeBox(data);
            if (this.imgIndex + 1 <= this.imgFile.size() - 1) ++this.imgIndex;
            else this.rightButton.setDisable(true);
        }
        System.out.println(this.imgIndex + ", " + this.imgFile.get(this.imgIndex).toString());
        imageInit(this.imgFile.get(this.imgIndex).toString());
        this.boundingBoxConfigList.setItems(this.list.get(this.imgIndex));
        drawBoxList(Color.RED);
        if (this.imgIndex == 0) setPageButtonDisable(true, false);
        else if (this.imgIndex == this.imgFile.size() - 1) setPageButtonDisable(false, true);
        else setPageButtonDisable(false, false);
    }

    private void setPageButtonDisable(boolean left, boolean right) {
        this.leftButton.setDisable(left);
        this.rightButton.setDisable(right);
    }

    private void loadImage() {
        FileChooser dialog = new FileChooser();
        dialog.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("PNG (*.png)", "*.png"),
                new FileChooser.ExtensionFilter("JPG (*.jpg;*.jpeg)", "*.jpg", ".jpeg"),
                new FileChooser.ExtensionFilter("GIF (*.gif)", "*.gif"),
                new FileChooser.ExtensionFilter("TIFF (*.tiff)", "*.tiff"),
                new FileChooser.ExtensionFilter("Bitmap (*.bmp)", "*.bmp"),
                new FileChooser.ExtensionFilter("All file (*.*)", "*.*"));
        File loadImg = dialog.showOpenDialog(this.stage);
        System.out.println(loadImg);
        if (loadImg != null) {
            this.imgFile.add(new File("file:\\" + loadImg.getPath()));
            this.list.add(FXCollections.observableArrayList());
            this.selectedIndex.add(-1);
            this.boxList.add(new ArrayList<>());
            if (this.imgFile.size() == 1) {
                imageInit("file:\\" + loadImg.getPath());
                this.imgIndex = 0;
                this.boundingBoxConfigList.setItems(this.list.get(0));
                setPageButtonDisable(true, true);
            } else if (this.imgIndex > 0 && this.imgIndex < this.imgFile.size() - 1) setPageButtonDisable(false, false);
            else if (this.imgIndex == 0) setPageButtonDisable(true, false);
            else if (this.imgIndex == this.imgFile.size() - 1) setPageButtonDisable(false, true);
            this.isDrawing = true;
            this.drawArea.setCursor(Cursor.CROSSHAIR);
        }
    }

    private void loadProject(File file) {
        if (file != null) {
            StringBuilder content = new StringBuilder();
            try {
                this.projectName = file.toString().contains("file:\\") ? file.toString().substring(6) : file.toString();
                FileReader reader = new FileReader(new File(this.projectName));
                int read;
                while ((read = reader.read()) != -1)
                    content.append((char) read);
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            String[] images = content.toString().split("@");
            if (images.length == 0) newProject();
            else {
                for (int i = 0; i < this.list.size(); ++i)
                    for (BoundingBoxLabelData data : this.list.get(i)) removeBox(data);
                clear();
                for (int count = 0; count < images.length; ++count) {
                    String[] s = images[count].split(";");
                    this.list.add(FXCollections.observableArrayList());
                    this.boxList.add(new ArrayList<>());
                    this.imgFile.add(new File(s[0]));
                    this.selectedIndex.add(-1);
                    for (int i = 1; i < s.length; ++i) {
                        String[] config = s[i].split(",");
                        System.out.println(Arrays.toString(config));
                        BoundingBoxLabelData data;
                        this.list.get(count).add(data = new BoundingBoxLabelData(
                                new SimpleStringProperty(config[0]), new SimpleStringProperty(config[1]),
                                new SimpleStringProperty(config[2]), new SimpleStringProperty(config[3]),
                                new SimpleStringProperty(config[4]), new SimpleStringProperty(config[5]),
                                new SimpleStringProperty(config[6]), new SimpleStringProperty(config[7])));
                        double x = data.getXSrc(), y = data.getYSrc(), width = data.getWidthSrc(), height = data.getHeightSrc();
                        this.boxList.get(count).add(new BoundingBox(x - (width * 0.5), y - (height * 0.5),
                                x + (width * 0.5), y + (height * 0.5), 1.5));
                    }
                    System.out.println(s[0]);
                }
                this.imgIndex = 0;
                drawBoxList(Color.RED);
                imageInit(images[0].split(";")[0]);
                isChanged = false;
                this.isDrawing = true;
                this.drawArea.setCursor(Cursor.CROSSHAIR);
                System.out.println(this.imgFile.size());
                if (this.imgFile.size() == 1) setPageButtonDisable(true, true);
                else setPageButtonDisable(true, false);
                this.boundingBoxConfigList.setItems(this.list.get(0));
            }
        }
    }

    private void dragImage(File... fileList) {
        for (File file : fileList) {
            String fileName = "file:\\" + file.toString();
            StringBuilder extension = new StringBuilder();
            for (int i = fileName.length() - 1; i >= 0 && fileName.charAt(i) != '.'; --i)
                extension.append(fileName.charAt(i));
            extension.reverse();
            if (extension.toString().equals("png") || extension.toString().equals("jpg") || extension.toString().equals("jpeg") ||
                    extension.toString().equals("gif") || extension.toString().equals("tiff") || extension.toString().equals("bmp")) {
                this.imgFile.add(new File(fileName));
                this.list.add(FXCollections.observableArrayList());
                this.selectedIndex.add(-1);
                this.boxList.add(new ArrayList<>());
                if (this.imgFile.size() == 1) {
                    imageInit(fileName);
                    this.imgIndex = 0;
                    this.boundingBoxConfigList.setItems(this.list.get(0));
                    setPageButtonDisable(true, true);
                } else if (this.imgIndex > 0 && this.imgIndex < this.imgFile.size() - 1)
                    setPageButtonDisable(false, false);
                else if (this.imgIndex == 0) setPageButtonDisable(true, false);
                else if (this.imgIndex == this.imgFile.size() - 1) setPageButtonDisable(false, true);
                this.isDrawing = true;
                this.drawArea.setCursor(Cursor.CROSSHAIR);
            } else if (extension.toString().equals("goo")) {
                loadProject(new File(fileName));
                break;
            }
        }
    }

    private void newProject() {
        if (this.imgIndex != -1) for (BoundingBoxLabelData data : this.list.get(imgIndex)) removeBox(data);
        clear();
        imageInit(null);
        this.drawArea.setCursor(Cursor.DEFAULT);
        setPageButtonDisable(true, true);
        this.isDrawing = false;
        isChanged = false;
    }

    private boolean saveProject(boolean isSaveAs) {
        File saveFile = null;
        if (isSaveAs) {
            FileChooser dialog = new FileChooser();
            dialog.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("GOO file(*.goo)", "*.goo"));
            dialog.setInitialFileName("project.goo");
            saveFile = dialog.showSaveDialog(this.stage);
        } else {
            if (this.projectName == null) {
                FileChooser dialog = new FileChooser();
                dialog.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("GOO file(*.goo)", "*.goo"));
                dialog.setInitialFileName("project.goo");
                saveFile = dialog.showSaveDialog(this.stage);
            } else saveFile = new File(this.projectName);
        }
        if (saveFile != null) {
            StringBuilder content = new StringBuilder();
            if (this.imgIndex != -1) {
                for (int i = 0; i < this.imgFile.size(); ++i) {
                    content.append(this.imgFile.get(i).getAbsolutePath().substring(this.imgFile.get(i).getAbsolutePath().indexOf("file:\\"))).append(";");
                    for (BoundingBoxLabelData data : this.list.get(i))
                        content.append(data.getGridX().get()).append(",").append(data.getGridY().get()).append(",")
                                .append(data.getX().get()).append(",").append(data.getY().get()).append(",")
                                .append(data.getWidth().get()).append(",").append(data.getHeight().get()).append(",")
                                .append(data.getConfidence().get()).append(",").append(data.getClassName().get()).append(";");
                    content.append("@");
                }
                content.delete(content.length() - 1, content.length());
            }
            save(saveFile, content.toString());
            isChanged = false;
            return true;
        } else return false;
    }

    private void dialog(String title) throws Exception {
        this.msgBox = new Stage(StageStyle.UTILITY);
        this.dialogLoader = new FXMLLoader(Class.forName("DetectionLabeling.LocalizationLabeling").getResource("dialog.fxml"));
        this.msgBoxDialog = this.dialogLoader.getController();
        this.msgBox.setScene(new Scene(this.dialogLoader.load()));
        this.msgBox.setTitle(title);
        this.msgBox.setResizable(false);
        this.msgBox.show();
    }

    public void xChange(TableColumn.CellEditEvent<BoundingBoxLabelData, String> edit) {
        BoundingBoxLabelData data = this.boundingBoxConfigList.getSelectionModel().getSelectedItem();
        removeBox(data);
        double value = Double.parseDouble(edit.getNewValue());
        if (value > 1.0) data.setX(value);
        else data.setX(((value + data.getGridXSrc()) / this.GRID_SIZE) * this.SIZE);
        drawBoxList(Color.RED);
        drawBox(Color.rgb(147, 255, 0, 1.0), data);
    }

    public void yChange(TableColumn.CellEditEvent<BoundingBoxLabelData, String> edit) {
        BoundingBoxLabelData data = this.boundingBoxConfigList.getSelectionModel().getSelectedItem();
        removeBox(data);
        double value = Double.parseDouble(edit.getNewValue());
        if (value > 1.0) data.setY(value);
        else data.setY(((value + data.getGridYSrc()) / this.GRID_SIZE) * this.SIZE);
        drawBoxList(Color.RED);
        drawBox(Color.rgb(147, 255, 0, 1.0), data);
    }

    public void widthChange(TableColumn.CellEditEvent<BoundingBoxLabelData, String> edit) {
        BoundingBoxLabelData data = this.boundingBoxConfigList.getSelectionModel().getSelectedItem();
        removeBox(data);
        double value = Double.parseDouble(edit.getNewValue());
        if (value > 1.0) data.setWidth(value);
        else data.setWidth(value * this.SIZE);
        drawBoxList(Color.RED);
        drawBox(Color.rgb(147, 255, 0, 1.0), data);
    }

    public void heightChange(TableColumn.CellEditEvent<BoundingBoxLabelData, String> edit) {
        BoundingBoxLabelData data = this.boundingBoxConfigList.getSelectionModel().getSelectedItem();
        removeBox(data);
        double value = Double.parseDouble(edit.getNewValue());
        if (value > 1.0) data.setHeight(value);
        else data.setHeight(value * this.SIZE);
        drawBoxList(Color.RED);
        drawBox(Color.rgb(147, 255, 0, 1.0), data);
    }

    public void confidenceChange(TableColumn.CellEditEvent<BoundingBoxLabelData, String> edit) {
        BoundingBoxLabelData data = this.boundingBoxConfigList.getSelectionModel().getSelectedItem();
        double value = Double.parseDouble(edit.getNewValue());
        data.setConfidence(value > 1.0 ? 1.0 : value);
    }

    public void classChange(TableColumn.CellEditEvent<BoundingBoxLabelData, String> edit) {
        BoundingBoxLabelData data = this.boundingBoxConfigList.getSelectionModel().getSelectedItem();
        data.setClassName(Double.parseDouble(edit.getNewValue().substring(edit.getNewValue().length() - 1)));
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        imageInit(null);

        this.ui.setOnKeyPressed(event -> {
            pressedSubKey = event.getCode();
            if (pressedSubKey == KeyCode.DELETE || pressedSubKey == KeyCode.BACK_SPACE) removeSelectedBox();
        });
        this.ui.setOnKeyReleased(event -> pressedSubKey = null);

        this.gridXColumn.setCellValueFactory(cell -> cell.getValue().getGridX());
        this.gridYColumn.setCellValueFactory(cell -> cell.getValue().getGridY());
        this.xColumn.setCellValueFactory(cell -> cell.getValue().getX());
        this.yColumn.setCellValueFactory(cell -> cell.getValue().getY());
        this.widthColumn.setCellValueFactory(cell -> cell.getValue().getWidth());
        this.heightColumn.setCellValueFactory(cell -> cell.getValue().getHeight());
        this.confidenceColumn.setCellValueFactory(cell -> cell.getValue().getConfidence());
        this.classColumn.setCellValueFactory(cell -> cell.getValue().getClassName());

        this.xColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        this.yColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        this.widthColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        this.heightColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        this.confidenceColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        ObservableList<String> classList = FXCollections.observableArrayList();
        classList.add("Body - 0"); classList.add("Face - 1");
        this.classColumn.setCellFactory(ComboBoxTableCell.forTableColumn(classList));

        this.boundingBoxConfigList.setEditable(true);

        this.drawContext = this.drawArea.getGraphicsContext2D();
        this.drawArea.setCursor(Cursor.DEFAULT);
        this.drawContext.setStroke(Color.RED);
        this.drawContext.setLineWidth(3);
        setPageButtonDisable(true, true);

        this.boundingBoxConfigList.setOnMouseClicked(event -> {
            selectedIndex.set(imgIndex, boundingBoxConfigList.getSelectionModel().getSelectedIndex());
            if (selectedIndex.get(imgIndex) != -1) {
                drawBoxList(Color.RED);
                drawBox(Color.rgb(147, 255, 0, 1.0), boundingBoxConfigList.getSelectionModel().getSelectedItem());
            } else drawBoxList(Color.RED);
        });
        this.boundingBoxConfigList.setOnKeyPressed(event -> {
            selectedIndex.set(imgIndex, boundingBoxConfigList.getSelectionModel().getSelectedIndex());
            if (selectedIndex.get(imgIndex) != -1) {
                if (event.getCode() == KeyCode.UP || event.getCode() == KeyCode.DOWN) {
                    switch (event.getCode()) {
                        case UP:
                            selectedIndex.set(imgIndex, selectedIndex.get(imgIndex) - 1);
                            break;
                        case DOWN:
                            selectedIndex.set(imgIndex, selectedIndex.get(imgIndex) + 1);
                            break;
                    }
                    if (selectedIndex.get(imgIndex) == -1) selectedIndex.set(imgIndex, 0);
                    else if (selectedIndex.get(imgIndex) == list.get(imgIndex).size())
                        selectedIndex.set(imgIndex, list.get(imgIndex).size() - 1);
                    drawBoxList(Color.RED);
                    drawBox(Color.rgb(147, 255, 0, 1.0), list.get(imgIndex).get(selectedIndex.get(imgIndex)));
                }
            } else drawBoxList(Color.RED);
        });
        this.boundingBoxConfigList.setOnKeyReleased(event -> {
            selectedIndex.set(imgIndex, boundingBoxConfigList.getSelectionModel().getSelectedIndex());
            if (selectedIndex.get(imgIndex) != -1) {
                drawBoxList(Color.RED);
                drawBox(Color.rgb(147, 255, 0, 1.0), boundingBoxConfigList.getSelectionModel().getSelectedItem());
            } else drawBoxList(Color.RED);
        });

        this.selectButton.setOnAction(event -> {
            drawArea.setCursor(Cursor.DEFAULT);
            isDrawing = false;
        });
        this.selectButton.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                drawArea.setCursor(Cursor.DEFAULT);
                isDrawing = false;
            }
        });
        this.drawButton.setOnAction(event -> {
            drawArea.setCursor(Cursor.CROSSHAIR);
            isDrawing = true;
        });
        this.drawButton.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                drawArea.setCursor(Cursor.CROSSHAIR);
                isDrawing = true;
            }
        });
        this.addButton.setOnAction(event -> {
            isChanged = true;
            //drawBoxList(Color.RED);
            boxList.get(imgIndex).add(new BoundingBox(210.0 - (SIZE / GRID_SIZE * 0.5), 210.0 - (SIZE / GRID_SIZE * 0.5),
                    210.0 + (SIZE / GRID_SIZE * 0.5), 210.0 + (SIZE / GRID_SIZE * 0.5), 3.0));
            list.get(imgIndex).add(new BoundingBoxLabelData(3.0, 3.0, 210.0, 210.0,
                    SIZE / GRID_SIZE, SIZE / GRID_SIZE, 1.0, 0));
            drawBox(Color.RED, list.get(imgIndex).get(list.get(imgIndex).size() - 1));
            selectedIndex.set(imgIndex, list.get(imgIndex).size() - 1);
        });
        this.addButton.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                isChanged = true;
                //drawBoxList(Color.RED);
                boxList.get(imgIndex).add(new BoundingBox(210.0 - (SIZE / GRID_SIZE * 0.5), 210.0 - (SIZE / GRID_SIZE * 0.5),
                        210.0 + (SIZE / GRID_SIZE * 0.5), 210.0 + (SIZE / GRID_SIZE * 0.5), 3.0));
                list.get(imgIndex).add(new BoundingBoxLabelData(3.0, 3.0, 210.0, 210.0,
                        SIZE / GRID_SIZE, SIZE / GRID_SIZE, 1.0, 0));
                drawBox(Color.RED, list.get(imgIndex).get(list.get(imgIndex).size() - 1));
                selectedIndex.set(imgIndex, list.get(imgIndex).size() - 1);
            }
        });
        this.deleteButton.setOnAction(event -> removeSelectedBox());
        this.deleteButton.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) removeSelectedBox();
        });
        this.clearButton.setOnAction(event -> {
            isChanged = true;
            for (BoundingBoxLabelData data : list.get(imgIndex)) removeBox(data);
            clear(imgIndex);
        });
        this.clearButton.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                isChanged = true;
                for (BoundingBoxLabelData data : list.get(imgIndex)) removeBox(data);
                clear(imgIndex);
            }
        });
        this.leftButton.setOnAction(event -> intentPage(true));
        this.leftButton.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) intentPage(true);
        });
        this.rightButton.setOnAction(event -> intentPage(false));
        this.rightButton.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) intentPage(false);
        });

        //this.drawArea.setOnMouseClicked(this::boxClick);

        this.drawArea.setOnDragOver(event -> {
            if (event.getDragboard().hasFiles())
                event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
        });
        this.drawArea.setOnDragDropped(event -> {
            List<File> fileList = event.getDragboard().getFiles();
            String path = fileList.get(0).toString();
            if (fileList.size() == 1) {
                if (!path.contains(".")) dragImage(new File(path).listFiles());
                else dragImage(fileList.get(0));
            } else dragImage(fileList.toArray(new File[fileList.size()]));
        });

        this.drawArea.setOnMouseMoved(event -> {
            double locX = event.getX(), locY = event.getY();
            setLocationLabel(locX, locY);
            resizeIndex = 0;
            if (imgIndex != -1 && !isDrawing) {
                for (BoundingBox box : boxList.get(imgIndex)) {
                    if (locX >= box.vertices[0].x1 && locX <= box.vertices[0].x2 &&
                            locY >= box.vertices[0].y1 && locY <= box.vertices[0].y2) {
                        drawArea.setCursor(Cursor.NW_RESIZE);
                        resizeX = (box.vertices[0].x1 + box.vertices[0].x2) * 0.5;
                        resizeY = (box.vertices[0].y1 + box.vertices[0].y2) * 0.5;
                        fixedX = (box.vertices[3].x1 + box.vertices[3].x2) * 0.5;
                        fixedY = (box.vertices[3].y1 + box.vertices[3].y2) * 0.5;
                        System.out.printf("NW: { x1: %.4f, y1: %.4f, x2: %.4f, y2: %.4f }\n", resizeX, resizeY, fixedX, fixedY);
                        return;
                    } else if (locX >= box.vertices[1].x1 && locX <= box.vertices[1].x2 &&
                            locY >= box.vertices[1].y1 && locY <= box.vertices[1].y2) {
                        drawArea.setCursor(Cursor.NE_RESIZE);
                        resizeX = (box.vertices[1].x1 + box.vertices[1].x2) * 0.5;
                        resizeY = (box.vertices[1].y1 + box.vertices[1].y2) * 0.5;
                        fixedX = (box.vertices[2].x1 + box.vertices[2].x2) * 0.5;
                        fixedY = (box.vertices[2].y1 + box.vertices[2].y2) * 0.5;
                        System.out.printf("NE: { x1: %.4f, y1: %.4f, x2: %.4f, y2: %.4f }\n", fixedX, resizeY, resizeX, fixedY);
                        return;
                    } else if (locX >= box.vertices[2].x1 && locX <= box.vertices[2].x2 &&
                            locY >= box.vertices[2].y1 && locY <= box.vertices[2].y2) {
                        drawArea.setCursor(Cursor.SW_RESIZE);
                        resizeX = (box.vertices[2].x1 + box.vertices[2].x2) * 0.5;
                        resizeY = (box.vertices[2].y1 + box.vertices[2].y2) * 0.5;
                        fixedX = (box.vertices[1].x1 + box.vertices[1].x2) * 0.5;
                        fixedY = (box.vertices[1].y1 + box.vertices[1].y2) * 0.5;
                        System.out.printf("SW: { x1: %.4f, y1: %.4f, x2: %.4f, y2: %.4f }\n", resizeX, fixedY, fixedX, resizeY);
                        return;
                    } else if (locX >= box.vertices[3].x1 && locX <= box.vertices[3].x2 &&
                            locY >= box.vertices[3].y1 && locY <= box.vertices[3].y2) {
                        drawArea.setCursor(Cursor.SE_RESIZE);
                        resizeX = (box.vertices[3].x1 + box.vertices[3].x2) * 0.5;
                        resizeY = (box.vertices[3].y1 + box.vertices[3].y2) * 0.5;
                        fixedX = (box.vertices[0].x1 + box.vertices[0].x2) * 0.5;
                        fixedY = (box.vertices[0].y1 + box.vertices[0].y2) * 0.5;
                        System.out.printf("SE: { x1: %.4f, y1: %.4f, x2: %.4f, y2: %.4f }\n", fixedX, fixedY, resizeX, resizeY);
                        return;
                    } else if (locX >= box.lines[0].x1 && locX <= box.lines[0].x2 &&
                            locY >= box.lines[0].y1 && locY <= box.lines[0].y2) {
                        drawArea.setCursor(Cursor.N_RESIZE);
                        resizeY = (box.lines[0].y1 + box.lines[0].y2) * 0.5;
                        fixedY = (box.lines[3].y1 + box.lines[3].y2) * 0.5;
                        return;
                    } else if (locX >= box.lines[1].x1 && locX <= box.lines[1].x2 &&
                            locY >= box.lines[1].y1 && locY <= box.lines[1].y2) {
                        drawArea.setCursor(Cursor.W_RESIZE);
                        resizeX = (box.lines[1].x1 + box.lines[1].x2) * 0.5;
                        fixedX = (box.lines[2].x1 + box.lines[2].x2) * 0.5;
                        return;
                    } else if (locX >= box.lines[2].x1 && locX <= box.lines[2].x2 &&
                            locY >= box.lines[2].y1 && locY <= box.lines[2].y2) {
                        drawArea.setCursor(Cursor.E_RESIZE);
                        resizeX = (box.lines[2].x1 + box.lines[2].x2) * 0.5;
                        fixedX = (box.lines[1].x1 + box.lines[1].x2) * 0.5;
                        return;
                    } else if (locX >= box.lines[3].x1 && locX <= box.lines[3].x2 &&
                            locY >= box.lines[3].y1 && locY <= box.lines[3].y2) {
                        drawArea.setCursor(Cursor.S_RESIZE);
                        resizeY = (box.lines[3].y1 + box.lines[3].y2) * 0.5;
                        fixedY = (box.lines[0].y1 + box.lines[0].y2) * 0.5;
                        return;
                    }
                    ++resizeIndex;
                }
                drawArea.setCursor(Cursor.DEFAULT);
                resizeX = -1;
                resizeY = -1;
                fixedX = -1;
                fixedY = -1;
                resizeIndex = -1;
            }
        });
        this.drawArea.setOnMouseExited(event -> setLocationLabel(event.getX(), event.getY()));

        this.drawArea.setOnMousePressed(event -> {
            System.out.println("start");
            if (isDrawing) {
                isChanged = true;
                drawBox(Color.RED, x1 = event.getX(), y1 = event.getY(), x2 = x1, y2 = y1);
                setBoxSizeLabel(fixedX, fixedY, resizeX, resizeY);
            } else if (imgIndex != -1) {
                //boxClick(event);

                double locX = event.getX(), locY = event.getY();
                int index = 0;
                if (resizeIndex == -1) {
                    System.out.println("a");
                    selectedIndex.set(imgIndex, -1);
                    for (BoundingBoxLabelData data : list.get(imgIndex)) {
                        double x = data.getXSrc(), y = data.getYSrc(), width = data.getWidthSrc(), height = data.getHeightSrc();
                        if (locX >= x - (width * 0.5) + 3 && locX <= x + (width * 0.5) - 3 &&
                                locY >= y - (height * 0.5) + 3 && locY <= y + (height * 0.5) - 3) {
                            if (event.getButton() == MouseButton.PRIMARY) {
                                selectedIndex.set(imgIndex, index);
                                drawBoxList(Color.RED);
                                drawBox(Color.rgb(147, 255, 0, 1.0), data);
                                break;
                            } else if (event.getButton() == MouseButton.SECONDARY) {
                                selectedIndex.set(imgIndex, -1);
                                removeBox(data);
                                drawBoxList(Color.RED);
                                break;
                            }
                        }
                        ++index;
                    }
                } else selectedIndex.set(imgIndex, resizeIndex);

//                System.out.println("[Mouse pressed] selectedIndex.get(imgIndex) : " + selectedIndex.get(imgIndex));
//                System.out.println("list.get(imgIndex).size() : " + list.get(imgIndex).size());
                if (selectedIndex.get(imgIndex) != -1 && event.getButton() != MouseButton.SECONDARY) {
                    isChanged = true;
                    boundingBoxConfigList.getSelectionModel().select(selectedIndex.get(imgIndex));
                    // Move
                    if (resizeX == -1 && resizeY == -1 && fixedX == -1 && fixedY == -1) {
                        BoundingBoxLabelData data = list.get(imgIndex).get(selectedIndex.get(imgIndex));
                        gridXMove = (int) data.getGridXSrc();
                        gridYMove = (int) data.getGridYSrc();
                        xMove = data.getXSrc();
                        yMove = data.getYSrc();
                        widthOfMove = data.getWidthSrc();
                        heightOfMove = data.getHeightSrc();
                        confMove = data.getConfidenceSrc();
                        classMove = data.getClassNameSrc();
                        xClick = event.getX();
                        yClick = event.getY();
//                        System.out.println("clicked x: " + xClick + ", clicked y: " + yClick);
//                        System.out.println("[Mouse pressed] gridX: " + gridXMove + ", gridY: " + gridYMove + ", x: " + xMove + ", y: " + yMove + ", width: " + widthOfMove + ", height: " + heightOfMove);
                        if (pressedSubKey != KeyCode.CONTROL) {
                            //list.get(imgIndex).remove((int) selectedIndex.get(imgIndex));
                            selectedData = list.get(imgIndex).get(selectedIndex.get(imgIndex));
                        }
                    } else if (resizeX != -1 && resizeY != -1 && fixedX != -1 && fixedY != -1) {
                        System.out.println("selectedIndex.get(imgIndex) : " + selectedIndex.get(imgIndex));
                        System.out.printf("{ fixedX: %.4f, fixedY: %.4f, resizeX: %.4f, resizeY: %.4f }\n", fixedX, fixedY, resizeX, resizeY);
                        removeBox(fixedX, fixedY, resizeX, resizeY);
                        selectedData = list.get(imgIndex).get(selectedIndex.get(imgIndex));
                        drawBoxList(Color.RED);
                        drawBox(Color.rgb(147, 255, 0, 1.0), fixedX, fixedY, resizeX, resizeY);
                        setBoxSizeLabel(fixedX, fixedY, resizeX, resizeY);
                    } else if (resizeX != -1 && resizeY == -1 && fixedX != -1 && fixedY == -1) {
                        resizeX = event.getX();
                        BoundingBoxLabelData data = list.get(imgIndex).get(resizeIndex);
                        double x = data.getXSrc(), y = data.getYSrc(), width = data.getWidthSrc(), height = data.getHeightSrc();
                        System.out.printf("{ resizeX: %.2f, resizeY: %.2f, fixedX: %.2f, fixedY: %.2f }\n", resizeX, y + (height * 0.5), fixedX, y - (height * 0.5));


                        removeBox(x - (width * 0.5), y - (height * 0.5), x + (width * 0.5), y + (height * 0.5));
                        selectedData = list.get(imgIndex).get(selectedIndex.get(imgIndex));
                        drawBoxList(Color.RED);
                        drawBox(Color.rgb(147, 255, 0, 1.0), fixedX, y - (height * 0.5), resizeX, y + (height * 0.5));
                        setBoxSizeLabel(fixedX, y - (height * 0.5), resizeX, y + (height * 0.5));
                    } else if (resizeX == -1 && resizeY != -1 && fixedX == -1 && fixedY != -1) {
                        resizeY = event.getY();
                        BoundingBoxLabelData data = list.get(imgIndex).get(resizeIndex);
                        double x = data.getXSrc(), y = data.getYSrc(), width = data.getWidthSrc(), height = data.getHeightSrc();
                        System.out.printf("{ resizeX: %.2f, resizeY: %.2f, fixedX: %.2f, fixedY: %.2f }\n", x + (width * 0.5), resizeY, x - (width * 0.5), fixedY);


                        removeBox(x - (width * 0.5), y - (height * 0.5), x + (width * 0.5), y + (height * 0.5));
                        selectedData = list.get(imgIndex).get(selectedIndex.get(imgIndex));
                        drawBoxList(Color.RED);
                        drawBox(Color.rgb(147, 255, 0, 1.0), x - (width * 0.5), fixedY, x + (width * 0.5), resizeY);
                        setBoxSizeLabel(x - (width * 0.5), fixedY, x + (width * 0.5), resizeY);
                    }
                } else {
                    drawBoxList(Color.RED);
                    boundingBoxConfigList.getSelectionModel().select(null);
                    selectedData = null;
                    selectedIndex.set(imgIndex, -1);
                }
            }
        });
        this.drawArea.setOnMouseDragged(event -> {
//            System.out.println("fixedX : " + fixedX + ", fixedY : " + fixedY + ", resizeX : " + resizeX + ", resizeY : " + resizeY);
            if (isDrawing) {
                isChanged = true;
                removeBox(x1, y1, x2, y2);
                drawBoxList(Color.RED);
                setLocationLabel(x2 = event.getX(), y2 = event.getY());
                setBoxSizeLabel(x1, y1, x2, y2);
                drawBox(Color.RED, x1, y1, x2, y2);
            } else if (imgIndex != -1 && selectedIndex.get(imgIndex) != -1 &&
                    event.getButton() != MouseButton.SECONDARY) {
                if (resizeX == -1 && resizeY == -1 && fixedX == -1 && fixedY == -1) {
                    isChanged = true;
//                    System.out.println(resizeX + ", " + resizeY + ", " + fixedX + ", " + fixedY);
                    removeBox(xMove - (widthOfMove * 0.5), yMove - (heightOfMove * 0.5),
                            xMove + (widthOfMove * 0.5), yMove + (heightOfMove * 0.5));
                    double distX = event.getX() - xClick, distY = event.getY() - yClick;
                    setLocationLabel(xClick = event.getX(), yClick = event.getY());
                    xMove += distX;
                    yMove += distY;
                    gridXMove = (int) (xMove / (SIZE / GRID_SIZE));
                    gridYMove = (int) (yMove / (SIZE / GRID_SIZE));
                    drawBoxList(Color.RED);
                    drawBox(Color.rgb(147, 255, 0, 1.0),
                            xMove - (widthOfMove * 0.5), yMove - (heightOfMove * 0.5),
                            xMove + (widthOfMove * 0.5), yMove + (heightOfMove * 0.5));
                } else if (resizeX != -1 && resizeY != -1 && fixedX != -1 && fixedY != -1) {
                    removeBox(fixedX, fixedY, resizeX, resizeY);
                    drawBoxList(Color.RED);
                    setLocationLabel(resizeX = event.getX(), resizeY = event.getY());
                    setBoxSizeLabel(fixedX, fixedY, resizeX, resizeY);
                    drawBox(Color.rgb(147, 255, 0, 1.0), fixedX, fixedY, resizeX, resizeY);
                } else if (resizeX != -1 && resizeY == -1 && fixedX != -1 && fixedY == -1) {
                    BoundingBoxLabelData data = list.get(imgIndex).get(resizeIndex);
                    double y = data.getYSrc(), height = data.getHeightSrc();

                    removeBox(fixedX, y - (height * 0.5), resizeX, y + (height * 0.5));
                    drawBoxList(Color.RED);

                    resizeX = event.getX();
                    System.out.printf("{ resizeX: %.2f, resizeY: %.2f, fixedX: %.2f, fixedY: %.2f }\n", resizeX, y + (height * 0.5), fixedX, y - (height * 0.5));

                    setLocationLabel(event.getX(), event.getY());
                    setBoxSizeLabel(fixedX, y - (height * 0.5), resizeX, y + (height * 0.5));
                    drawBox(Color.rgb(147, 255, 0, 1.0), fixedX, y - (height * 0.5), resizeX, y + (height * 0.5));
                } else if (resizeX == -1 && resizeY != -1 && fixedX == -1 && fixedY != -1) {
                    BoundingBoxLabelData data = list.get(imgIndex).get(resizeIndex);
                    double x = data.getXSrc(), width = data.getWidthSrc();

                    removeBox(x - (width * 0.5), fixedY, x + (width * 0.5), resizeY);
                    drawBoxList(Color.RED);

                    resizeY = event.getY();
                    System.out.printf("{ resizeX: %.2f, resizeY: %.2f, fixedX: %.2f, fixedY: %.2f }\n", x + (width * 0.5), resizeY, x - (width * 0.5), fixedY);

                    setLocationLabel(event.getX(), event.getY());
                    setBoxSizeLabel(x - (width * 0.5), fixedY, x + (width * 0.5), resizeY);
                    drawBox(Color.rgb(147, 255, 0, 1.0), x - (width * 0.5), fixedY, x + (width * 0.5), resizeY);
                }
            } else {
//                System.out.println("imgIndex : " + imgIndex);
//                System.out.println("selectedIndex.get(imgIndex) : " + selectedIndex.get(imgIndex));
//                System.out.println("event.getButton() : " + event.getButton());
            }
        });
        this.drawArea.setOnMouseReleased(event -> {
            if (isDrawing) {
                isChanged = true;
                removeBox(x1, y1, x2, y2);
                drawBoxList(Color.RED);

                setDefaultBoxSizeLabel();
                drawBox(Color.RED, x1, y1, x2 = event.getX(), y2 = event.getY());
                boxList.get(imgIndex).add(new BoundingBox(x1, y1, x2, y2, 3.0));

                double loc_x = (x1 + x2) * 0.5, loc_y = (y1 + y2) * 0.5, width = x1 <= x2 ? x2 - x1 : x1 - x2, height = y1 <= y2 ? y2 - y1 : y1 - y2, confidence = 1.0;
                int grid_x = (int) ((x1 + x2) * 0.5) / (int) (SIZE / GRID_SIZE), grid_y = (int) ((y1 + y2) * 0.5) / (int) (SIZE / GRID_SIZE);

                BoundingBoxLabelData data;
                list.get(imgIndex).add(data = new BoundingBoxLabelData(grid_x, grid_y, loc_x, loc_y, width, height, confidence, 0));
                selectedIndex.set(imgIndex, list.get(imgIndex).size() - 1);
//                System.out.println("{ grid_x: " + data.getGridX() + ", grid_y: " + data.getGridY() + ", x: " + data.getX() + ", y: " + data.getY() + ", width: " + data.getWidth() + ", height: " + data.getHeight() + ", confidence: " + data.getConfidence() + " }");
            } else if (imgIndex != -1 && selectedIndex.get(imgIndex) != -1 &&
                    event.getButton() != MouseButton.SECONDARY) {
                if (resizeX == -1 && resizeY == -1 && fixedX == -1 && fixedY == -1) {
                    isChanged = true;
//                    System.out.println("[Mouse released] selectedIndex.get(imgIndex) : " + selectedIndex.get(imgIndex));
                    removeBox(xMove - (widthOfMove * 0.5), yMove - (heightOfMove * 0.5),
                            xMove + (widthOfMove * 0.5), yMove + (heightOfMove * 0.5));
                    double distX = event.getX() - xClick, distY = event.getY() - yClick;
                    setLocationLabel(xClick = event.getX(), yClick = event.getY());
                    xMove += distX;
                    yMove += distY;
                    gridXMove = (int) (xMove / (SIZE / GRID_SIZE));
                    gridYMove = (int) (yMove / (SIZE / GRID_SIZE));
                    drawBoxList(Color.RED);
                    drawBox(Color.rgb(147, 255, 0, 1.0), xMove - (widthOfMove * 0.5), yMove - (heightOfMove * 0.5),
                            xMove + (widthOfMove * 0.5), yMove + (heightOfMove * 0.5));

//                    System.out.println("[Mouse released] gridX: " + gridXMove + ", gridY: " + gridYMove + ", x: " + xMove + ", y: " + yMove + ", width: " + widthOfMove + ", height: " + heightOfMove);

                    BoundingBoxLabelData data;
                    if (pressedSubKey == KeyCode.CONTROL) {
                        selectedIndex.set(imgIndex, selectedIndex.get(imgIndex) + 1);
                        list.get(imgIndex).add(selectedIndex.get(imgIndex), data = new BoundingBoxLabelData(gridXMove, gridYMove, xMove, yMove, widthOfMove, heightOfMove, confMove, classMove));
                        boxList.get(imgIndex).add(selectedIndex.get(imgIndex), new BoundingBox(
                                xMove - (widthOfMove * 0.5), yMove - (heightOfMove * 0.5),
                                xMove + (widthOfMove * 0.5), yMove + (heightOfMove * 0.5), 3.0));
                    } else {
                        list.get(imgIndex).set(selectedIndex.get(imgIndex), data = new BoundingBoxLabelData(gridXMove, gridYMove, xMove, yMove, widthOfMove, heightOfMove, confMove, classMove));
                        boxList.get(imgIndex).set(selectedIndex.get(imgIndex), new BoundingBox(
                                xMove - (widthOfMove * 0.5), yMove - (heightOfMove * 0.5),
                                xMove + (widthOfMove * 0.5), yMove + (heightOfMove * 0.5), 3.0));
                    }
//                    System.out.println("{ grid_x: " + data.getGridX() + ", grid_y: " + data.getGridY() + ", x: " + data.getX() + ", y: " + data.getY() + ", width: " + data.getWidth() + ", height: " + data.getHeight() + ", confidence: " + data.getConfidence() + " }");
//
//                    System.out.println("boxList.get(imgIndex).size() : " + boxList.get(imgIndex).size());
                    boundingBoxConfigList.getSelectionModel().select(selectedIndex.get(imgIndex));
                    System.out.println(selectedIndex.get(imgIndex));
                    System.out.println("boxList.get(imgIndex).size() : " + boxList.get(imgIndex).size());
                } else if (resizeX != -1 && resizeY != -1 && fixedX != -1 && fixedY != -1) {
                    removeBox(fixedX, fixedY, resizeX, resizeY);
                    drawBoxList(Color.RED);

                    setDefaultBoxSizeLabel();
                    drawBox(Color.rgb(147, 255, 0, 1.0),
                            fixedX, fixedY, resizeX = event.getX(), resizeY = event.getY());
                    boxList.get(imgIndex).set(selectedIndex.get(imgIndex),
                            new BoundingBox(fixedX, fixedY, resizeX, resizeY, 3.0));

                    BoundingBoxLabelData data = list.get(imgIndex).get(selectedIndex.get(imgIndex));
                    double loc_x = (fixedX + resizeX) * 0.5, loc_y = (fixedY + resizeY) * 0.5, width = Math.abs(fixedX - resizeX), height = Math.abs(fixedY - resizeY), confidence = data.getConfidenceSrc(), classNum = data.getClassNameSrc();
                    int grid_x = (int) ((fixedX + resizeX) * 0.5) / (int) (SIZE / GRID_SIZE), grid_y = (int) ((fixedY + resizeY) * 0.5) / (int) (SIZE / GRID_SIZE);

                    list.get(imgIndex).set(selectedIndex.get(imgIndex), data = new BoundingBoxLabelData(grid_x, grid_y, loc_x, loc_y, width, height, confidence, classNum));
                    System.out.println("{ grid_x: " + data.getGridX() + ", grid_y: " + data.getGridY() + ", x: " + data.getX() + ", y: " + data.getY() + ", width: " + data.getWidth() + ", height: " + data.getHeight() + ", confidence: " + data.getConfidence() + " }");
                    boundingBoxConfigList.getSelectionModel().select(selectedIndex.get(imgIndex));
                } else if (resizeX != -1 && resizeY == -1 && fixedX != -1 && fixedY == -1) {
                    BoundingBoxLabelData data = list.get(imgIndex).get(resizeIndex);
                    double y = data.getYSrc(), height = data.getHeightSrc();

                    removeBox(fixedX, y - (height * 0.5), resizeX, y + (height * 0.5));
                    drawBoxList(Color.RED);

                    resizeX = event.getX();
                    System.out.printf("{ resizeX: %.2f, resizeY: %.2f, fixedX: %.2f, fixedY: %.2f }\n", resizeX, y + (height * 0.5), fixedX, y - (height * 0.5));
                    setDefaultBoxSizeLabel();

                    drawBox(Color.rgb(147, 255, 0, 1.0),
                            fixedX, y - (height * 0.5), resizeX, y + (height * 0.5));
                    boxList.get(imgIndex).set(selectedIndex.get(imgIndex),
                            new BoundingBox(fixedX, y - (height * 0.5), resizeX, y + (height * 0.5), 3.0));

                    data = list.get(imgIndex).get(selectedIndex.get(imgIndex));
                    double loc_x = (fixedX + resizeX) * 0.5, loc_y = y, width = Math.abs(fixedX - resizeX), confidence = data.getConfidenceSrc(), classNum = data.getClassNameSrc();
                    int grid_x = (int) ((fixedX + resizeX) * 0.5) / (int) (SIZE / GRID_SIZE), grid_y = (int) y / (int) (SIZE / GRID_SIZE);

                    list.get(imgIndex).set(selectedIndex.get(imgIndex), data = new BoundingBoxLabelData(grid_x, grid_y, loc_x, loc_y, width, height, confidence, classNum));
                    System.out.println("{ grid_x: " + data.getGridX() + ", grid_y: " + data.getGridY() + ", x: " + data.getX() + ", y: " + data.getY() + ", width: " + data.getWidth() + ", height: " + data.getHeight() + ", confidence: " + data.getConfidence() + " }");
                    boundingBoxConfigList.getSelectionModel().select(selectedIndex.get(imgIndex));

                } else if (resizeX == -1 && resizeY != -1 && fixedX == -1 && fixedY != -1) {
                    BoundingBoxLabelData data = list.get(imgIndex).get(resizeIndex);
                    double x = data.getXSrc(), width = data.getWidthSrc();

                    removeBox(x - (width * 0.5), fixedY, x + (width * 0.5), resizeY);
                    drawBoxList(Color.RED);

                    resizeY = event.getY();
                    System.out.printf("{ resizeX: %.2f, resizeY: %.2f, fixedX: %.2f, fixedY: %.2f }\n", x + (width * 0.5), resizeY, x - (width * 0.5), fixedY);
                    setDefaultBoxSizeLabel();

                    drawBox(Color.rgb(147, 255, 0, 1.0),
                            x - (width * 0.5), fixedY, x + (width * 0.5), resizeY);
                    boxList.get(imgIndex).set(selectedIndex.get(imgIndex),
                            new BoundingBox(x - (width * 0.5), fixedY, x + (width * 0.5), resizeY, 3.0));

                    data = list.get(imgIndex).get(selectedIndex.get(imgIndex));
                    double loc_x = x, loc_y = (fixedY + resizeY) * 0.5, height = Math.abs(fixedY - resizeY), confidence = data.getConfidenceSrc(), classNum = data.getClassNameSrc();
                    int grid_x = (int) x / (int) (SIZE / GRID_SIZE), grid_y = (int) ((fixedY + resizeY) * 0.5) / (int) (SIZE / GRID_SIZE);

                    list.get(imgIndex).set(selectedIndex.get(imgIndex), data = new BoundingBoxLabelData(grid_x, grid_y, loc_x, loc_y, width, height, confidence, classNum));
                    System.out.println("{ grid_x: " + data.getGridX() + ", grid_y: " + data.getGridY() + ", x: " + data.getX() + ", y: " + data.getY() + ", width: " + data.getWidth() + ", height: " + data.getHeight() + ", confidence: " + data.getConfidence() + " }");
                    boundingBoxConfigList.getSelectionModel().select(selectedIndex.get(imgIndex));

                }
            }
            System.out.println("end");
        });

        this.newProjectMenu.setOnAction(event -> {
            if (isChanged) {
                try {
                    dialog("New project");
                    msgBoxDialog = dialogLoader.getController();
                    msgBoxDialog.start(msgBox);
                    msgBoxDialog.dialogYesButton.setOnAction(ev -> {
                        msgBoxDialog.stage.close();
                        if (saveProject(false)) newProject();
                    });
                    this.msgBoxDialog.dialogNoButton.setOnAction(ev -> {
                        newProject();
                        msgBoxDialog.stage.close();
                    });
                    System.out.println(msgBoxDialog);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else newProject();
        });
        this.loadProjectMenu.setOnAction(event -> {
            FileChooser dialog = new FileChooser();
            dialog.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("GOO file(*.goo)", "*.goo"));
            loadProject(dialog.showOpenDialog(stage));
        });
        this.loadImageMenu.setOnAction(event -> loadImage());
        this.saveProjectMenu.setOnAction(event -> saveProject(false));
        this.saveAsMenu.setOnAction(event -> saveProject(true));
        this.exportMenu.setOnAction(event -> {
            FileChooser dialog = new FileChooser();
            dialog.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("CSV file(*.csv)", "*.csv"),
                    new FileChooser.ExtensionFilter("Text file(*.txt)", "*.txt"),
                    new FileChooser.ExtensionFilter("All file (*.*)", "*.*"));
            dialog.setInitialFileName(imgFile.get(imgIndex).getName().substring(0, imgFile.get(imgIndex).getName().indexOf('.')));
            File saveFile = dialog.showSaveDialog(stage);
            if (saveFile != null) {
                StringBuilder content = new StringBuilder();
                for (BoundingBoxLabelData data : list.get(imgIndex))
                    content.append(data.getGridX().get()).append(" ")
                            .append(data.getGridY().get()).append(" ")
                            .append(data.getX().get()).append(" ")
                            .append(data.getY().get()).append(" ")
                            .append(data.getWidth().get()).append(" ")
                            .append(data.getHeight().get()).append(" ")
                            .append(data.getConfidence().get()).append(" ")
                            .append(data.getClassName().get()).append("\n");
                save(saveFile, content.toString());
            }
        });
        this.exitMenu.setOnAction(event -> {
            if (isChanged) {
                try {
                    dialog("Program exit");
                    this.msgBoxDialog = this.dialogLoader.getController();
                    this.msgBoxDialog.start(this.msgBox);
                    this.msgBoxDialog.dialogYesButton.setOnAction(ev -> {
                        this.msgBoxDialog.stage.close();
                        if (saveProject(false)) Platform.exit();
                    });
                    this.msgBoxDialog.dialogNoButton.setOnAction(ev -> {
                        this.msgBoxDialog.stage.close();
                        Platform.exit();
                    });
                    System.out.println(this.msgBoxDialog);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else Platform.exit();
        });
    }

    @Override
    public void start(Stage _stage) throws Exception {
        stage = _stage;
        stage.setOnCloseRequest(event -> {
            System.out.println(isChanged);
            if (isChanged) {
                event.consume();
                try {
                    dialog("Program exit");
                    this.msgBoxDialog = this.dialogLoader.getController();
                    this.msgBoxDialog.start(this.msgBox);
                    this.msgBoxDialog.dialogYesButton.setOnAction(ev -> {
                        this.msgBoxDialog.stage.close();
                        if (saveProject(false)) Platform.exit();
                    });
                    this.msgBoxDialog.dialogNoButton.setOnAction(ev -> {
                        this.msgBoxDialog.stage.close();
                        Platform.exit();
                    });
                    System.out.println(this.msgBoxDialog);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        stage.setTitle("Detection Labeling");
        stage.setResizable(false);
        stage.setScene(new Scene(FXMLLoader.load(Class.forName("DetectionLabeling.LocalizationLabeling").getResource("ui.fxml"))));
        stage.show();
    }
}