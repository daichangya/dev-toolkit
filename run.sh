#!/bin/bash
JAVA_FX_PATH="/path/to/javafx-sdk/lib"
java --module-path $JAVA_FX_PATH --add-modules javafx.controls,javafx.fxml,javafx.web \
     -jar dev-toolkit-app/target/dev-toolkit-app-1.0-SNAPSHOT.jar 