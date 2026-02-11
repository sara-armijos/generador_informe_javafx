package org.example;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class PdfReportService {

    public void exportar(File destino, List<Cliente> clientesFiltrados, Image chartFxImage) throws IOException {
        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);

            float margin = 50;
            float y = page.getMediaBox().getHeight() - margin;
            float x = margin;

            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {

                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA_BOLD, 18);
                cs.newLineAtOffset(x, y);
                cs.showText("Informe de Clientes");
                cs.endText();

                y -= 25;
                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA, 11);
                cs.newLineAtOffset(x, y);
                cs.showText("Este documento presenta un resumen de los clientes filtrados en la aplicación.");
                cs.endText();

                y -= 30;
                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA_BOLD, 12);
                cs.newLineAtOffset(x, y);
                cs.showText("Listado de clientes:");
                cs.endText();

                y -= 18;
                cs.setFont(PDType1Font.HELVETICA, 11);

                float yLista = y;
                for (Cliente c : clientesFiltrados) {
                    if (yLista < 120) break; // no seguimos si nos comemos el pie
                    cs.beginText();
                    cs.newLineAtOffset(x, yLista);
                    cs.showText("- " + c.getNombre() + " (" + c.getCiudad() + ")");
                    cs.endText();
                    yLista -= 14;
                }

                float yTotal = yLista - 10;
                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA_BOLD, 12);
                cs.newLineAtOffset(x, yTotal);
                cs.showText("Total de clientes: " + clientesFiltrados.size());
                cs.endText();

                if (chartFxImage != null) {
                    BufferedImage awt = SwingFXUtils.fromFXImage(chartFxImage, null);
                    PDImageXObject pdImg = LosslessFactory.createFromImage(doc, awt);

                    float chartW = 260;
                    float chartH = 180;
                    float chartX = page.getMediaBox().getWidth() - margin - chartW;
                    float chartY = page.getMediaBox().getHeight() - margin - 90 - chartH;

                    cs.drawImage(pdImg, chartX, chartY, chartW, chartH);
                }

                float yPie = 60;
                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA_OBLIQUE, 9);
                cs.newLineAtOffset(x, yPie);
                cs.showText("© 2026 Informe generado por la aplicación DAM - Todos los derechos reservados.");
                cs.endText();

                cs.moveTo(x, yPie - 10);
                cs.lineTo(page.getMediaBox().getWidth() - margin, yPie - 10);
                cs.stroke();
            }

            doc.save(destino);
        }
    }
}
