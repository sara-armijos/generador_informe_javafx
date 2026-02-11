package org.example;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.data.general.DefaultPieDataset;

import java.awt.image.BufferedImage;
import java.util.Map;

public class ChartService {

    public Image crearGraficoTartaComoImagen(Map<String, Integer> conteoPorCiudad, int width, int height) {
        DefaultPieDataset<String> dataset = new DefaultPieDataset<>();
        for (var e : conteoPorCiudad.entrySet()) {
            dataset.setValue(e.getKey(), e.getValue());
        }

        JFreeChart chart = ChartFactory.createPieChart(
                "Clientes por ciudad",
                dataset,
                true,
                true,
                false
        );

        BufferedImage buffered = chart.createBufferedImage(width, height);
        return SwingFXUtils.toFXImage(buffered, null);
    }
}