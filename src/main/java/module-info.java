module org.example.projekt_hormonika {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.desktop;

    opens org.example.projekt_hormonika to javafx.fxml;
    exports org.example.projekt_hormonika;

}