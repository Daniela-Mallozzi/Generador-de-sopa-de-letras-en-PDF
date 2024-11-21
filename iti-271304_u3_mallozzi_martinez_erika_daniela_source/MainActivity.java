package com.z_iti_271304_u3_mallozzi_martinez_erika_daniela;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.Toast;
import android.widget.Button;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.AreaBreak;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.properties.TextAlignment;
import android.content.Intent;
import android.net.Uri;
import java.io.File;

public class MainActivity extends AppCompatActivity {

    private GridLayout gridLayout;
    private EditText etWords, etPages;
    private Button btnGenerate, btnOpenPdf;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requestPermissions();

        etWords = findViewById(R.id.etWords);
        etPages = findViewById(R.id.etPages);
        btnGenerate = findViewById(R.id.btnGenerate);
        gridLayout = findViewById(R.id.gridLayout);
        btnOpenPdf = findViewById(R.id.btnOpenPdf);


        btnGenerate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    String wordsInput = etWords.getText().toString();
                    String[] words = wordsInput.split(",");

                    for (int i = 0; i < words.length; i++) {
                        words[i] = words[i].replaceAll("[0-9]", "");
                    }

                    if (words.length == 0 || words[0].isEmpty()) {
                        Toast.makeText(MainActivity.this, "Ingresa al menos una palabra válida", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    int pages = Integer.parseInt(etPages.getText().toString());

                    generateSopaDeLetras(words);
                    generatePDF(String.join(",", words), pages);
                } catch (NumberFormatException e) {
                    Toast.makeText(MainActivity.this, "Ingresa un número válido de páginas", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });



        btnOpenPdf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Ruta al archivo PDF en el directorio de descargas
                String pdfPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/SopaDeLetras.pdf";
                File file = new File(pdfPath);

                if (file.exists()) {
                    // Usa FileProvider para obtener el URI del archivo
                    Uri pdfUri = FileProvider.getUriForFile(MainActivity.this,
                            "com.z_iti_271304_u3_mallozzi_martinez_erika_daniela.fileprovider", file);

                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(pdfUri, "application/pdf");
                    intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); // Permite que otras apps lean el archivo
                    startActivity(intent);
                } else {
                    Toast.makeText(MainActivity.this, "Genera un PDF con tu sopa de letras primero. El PDF no se ha encontrado", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }


    public void generatePDF(String inputWords, int pages) {
        try {
            String[] words = inputWords.split(",");

            for (int i = 0; i < words.length; i++) {
                words[i] = words[i].toUpperCase().replace(" ", "");
            }

            String pdfPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/SopaDeLetras.pdf";
            PdfWriter writer = new PdfWriter(pdfPath);
            PdfDocument pdfDocument = new PdfDocument(writer);
            Document document = new Document(pdfDocument);

            document.setFontSize(14);

            for (int page = 0; page < pages; page++) {
                String title = "Sopa de Letras - Página #" + (page + 1);
                document.add(new Paragraph(title)
                        .setBold()
                        .setFontSize(18)
                        .setTextAlignment(TextAlignment.CENTER));

                document.add(new Paragraph("\n"));

                StringBuilder line = new StringBuilder();

                int count = 0;
                for (int index = 0; index < words.length; index++) {
                    line.append("\u2022 ").append(words[index]).append("   ");

                    count++;

                    if (count % 8 == 0) {
                        document.add(new Paragraph(line.toString()).setFontSize(12));
                        line.setLength(0);
                    }
                }

                if (line.length() > 0) {
                    document.add(new Paragraph(line.toString()).setFontSize(12));
                }

                document.add(new Paragraph("\n"));

                char[][] sopa = generateSopaDeLetrasArray(words);
                Table table = createSopaTable(sopa);
                document.add(table);

                if (page < pages - 1) {
                    document.add(new AreaBreak());
                }
            }

            document.close();
            Toast.makeText(this, "PDF generado en: " + pdfPath, Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al generar el PDF", Toast.LENGTH_LONG).show();
        }
    }

    private char[][] generateSopaDeLetrasArray(String[] words) {
        int numRows = 15;
        int numColumns = 19;

        char[][] grid = new char[numRows][numColumns];

        for (int i = 0; i < numRows; i++) {
            for (int j = 0; j < numColumns; j++) {
                grid[i][j] = ' ';
            }
        }

        for (String word : words) {
            boolean placed = false;
            while (!placed) {
                boolean horizontal = Math.random() < 0.5;

                int row = (int) (Math.random() * numRows);
                int col = (int) (Math.random() * numColumns);

                boolean canPlace = true;
                if (horizontal) {
                    if (col + word.length() > numColumns) {
                        canPlace = false;
                    } else {
                        for (int i = 0; i < word.length(); i++) {
                            if (grid[row][col + i] != ' ' && grid[row][col + i] != word.charAt(i)) {
                                canPlace = false;
                                break;
                            }
                        }
                    }
                } else {
                    if (row + word.length() > numRows) {
                        canPlace = false;
                    } else {
                        for (int i = 0; i < word.length(); i++) {
                            if (grid[row + i][col] != ' ' && grid[row + i][col] != word.charAt(i)) {
                                canPlace = false;
                                break;
                            }
                        }
                    }
                }

                if (canPlace) {
                    for (int i = 0; i < word.length(); i++) {
                        if (horizontal) {
                            grid[row][col + i] = word.charAt(i);
                        } else {
                            grid[row + i][col] = word.charAt(i);
                        }
                    }
                    placed = true;
                }
            }
        }

        for (int i = 0; i < numRows; i++) {
            for (int j = 0; j < numColumns; j++) {
                if (grid[i][j] == ' ') {
                    grid[i][j] = (char) ('A' + (Math.random() * 26));
                }
            }
        }

        return grid;
    }


    private void generateSopaDeLetras(String[] words) {
        gridLayout.removeAllViews();

        char[][] sopa = generateSopaDeLetrasArray(words);

        gridLayout.setRowCount(sopa.length);
        gridLayout.setColumnCount(sopa[0].length);

        for (int row = 0; row < sopa.length; row++) {
            for (int col = 0; col < sopa[row].length; col++) {
                TextView letterView = new TextView(this);
                letterView.setText(String.valueOf(sopa[row][col]));
                letterView.setTextSize(20);
                letterView.setPadding(16, 16, 16, 16);
                letterView.setTextColor(getResources().getColor(android.R.color.black));

                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.rowSpec = GridLayout.spec(row);
                params.columnSpec = GridLayout.spec(col);
                letterView.setLayoutParams(params);

                // Agregar cada letra al GridLayout
                gridLayout.addView(letterView);
            }
        }
    }



    private Table createSopaTable(char[][] grid) {
        int numRows = grid.length;
        int numColumns = grid[0].length;

        float[] columnWidths = new float[numColumns];
        for (int i = 0; i < numColumns; i++) {
            columnWidths[i] = 1f;
        }

        Table table = new Table(columnWidths);

        // Crear celdas de la tabla para cada letra en la sopa de letras
        for (int row = 0; row < numRows; row++) {
            for (int col = 0; col < numColumns; col++) {
                Cell cell = new Cell()
                        .add(new Paragraph(String.valueOf(grid[row][col])))
                        .setPadding(8)  // Aumentar el padding para celdas más grandes
                        .setFontSize(12);  // Aumentar el tamaño de la fuente en las celdas

                // Agregar la celda a la tabla
                table.addCell(cell);
            }
        }

        return table;
    }




    private void requestPermissions() {
        // Verificar si el permiso ya ha sido otorgado
        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // Solicitar el permiso en tiempo de ejecución
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permiso otorgado, puedes realizar operaciones de escritura en el almacenamiento
                Toast.makeText(this, "Permiso otorgado", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permiso denegado", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
