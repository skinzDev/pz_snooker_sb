module com.example.pz {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires mysql.connector.j;


    opens com.example.pz to javafx.fxml;
    exports com.example.pz;
}