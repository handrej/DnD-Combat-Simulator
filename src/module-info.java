module ode.prj.b4 {
    requires javafx.fxml;
    requires javafx.controls;
    requires java.rmi;
    requires java.logging;
    requires java.desktop;
    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    exports fhtw.API;
    exports fhtw;

    opens fhtw.Controller;
}