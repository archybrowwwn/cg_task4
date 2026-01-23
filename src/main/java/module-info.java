module com.cgvsu {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires transitive javafx.graphics;


    opens com.cgvsu to javafx.fxml;
    exports com.cgvsu;
}