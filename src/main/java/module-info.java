module net.viktors.gameoflifefx {
    requires javafx.controls;
    requires javafx.fxml;
    requires kotlin.stdlib;

    requires com.almasb.fxgl.all;

    opens net.viktors.gameoflifefx to javafx.fxml;
    exports net.viktors.gameoflifefx;
}