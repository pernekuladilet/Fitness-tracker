package app.controller;

import app.model.TypeStat;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;

import java.util.List;

public class StatsController {

    @FXML private PieChart pieChart;

    public void setData(List<TypeStat> stats) {
        ObservableList<PieChart.Data> data = FXCollections.observableArrayList();
        for (TypeStat s : stats) {
            data.add(new PieChart.Data(s.getType(), s.getCount()));
        }
        pieChart.setData(data);
    }
}
