import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom2.Element;

import models.Competence;
import models.Student;
import utilities.CompetenceUtilities;
import xaml.XamlGenerator;
import xml.XmlGenerator;

public class CompetenceView {
    private String file;

    private Element page;
    private Element grid;
    private Element canvas;

    private List<String> students;
    private Student student;

    private List<Competence> competences;

    public CompetenceView(String file) {
        this.file = file;

        // get competences
        this.competences = CompetenceUtilities.getCompetences(this.file);

        // get students
        this.students = CompetenceUtilities.getStudents(this.file);
        String firstStudent = CompetenceUtilities.getFirstStudent(this.file);
        this.student = new Student(firstStudent);

        this.init();
    }

    private void init() {
        // page
        this.page = XamlGenerator.getNavigationPage("SilverlightCSharp.Views.CompetenceView", "CompetenceView");

        // grid
        String[] rowSizes = {"50", "1*", "40"};
        String[] colSizes = {"200", "80*", "10*"};
        this.grid = XamlGenerator.getGrid(rowSizes, colSizes, false);

        // viewbox
        Element viewbox = XamlGenerator.getViewBox();
        viewbox.setAttribute("Grid.Column", "1");
        viewbox.setAttribute("Grid.Row", "1");

        // canvas
        this.canvas = XamlGenerator.getCanvas("700", "700");

        viewbox.addContent(this.canvas);
        XamlGenerator.setXName(viewbox, "CompetenceGraph");
        this.grid.addContent(viewbox);

        this.page.addContent(this.grid);

        this.generateGraphStructure();
        this.generateStudentBlock();
        this.generateStudentScrollViewer();

        this.fillGraphCompetences();
    }

    private void generateGraphStructure() {
        double canvasHeight = Double.parseDouble(this.canvas.getAttributeValue("Height"));
        double canvasWidth = Double.parseDouble(this.canvas.getAttributeValue("Width"));

        double x1, x2;
        double y1, y2;

        List<Element> graphLines = new ArrayList<>();

        // horizontal line
        // 95% of total
        x2 = 95.0 / 100.0 * canvasWidth;
        // 95% - 90% of total
        x1 = x2 - (90.0 / 100.0 * canvasWidth);
        y1 = y2 = canvasHeight / 2.0;

        Element horizontalLine = XamlGenerator.getLine(Double.toString(x1), Double.toString(y1),
                Double.toString(x2), Double.toString(y2), "Black", "1.5");

        // horizontal line - graph lines
        double incr = (x2 - x1) / 20.0;
        double y = canvasHeight / 2.0;
        for (double i = x1; i <= x2; i += incr) {
            if (i == x1 || i == canvasWidth / 2 || i == x2) {
                y1 = y + 10.0;
                y2 = y - 10.0;
            } else {
                y1 = y + 6.0;
                y2 = y - 6.0;
            }

            Element graphLine = XamlGenerator.getLine(Double.toString(i), Double.toString(y1), Double.toString(i),
                    Double.toString(y2), "Black", "1.5");
            graphLines.add(graphLine);
        }

        // vertical line
        // 95% of total
        y2 = 95.0 / 100.0 * canvasHeight;
        // 95% - 90% of total
        y1 = y2 - (90.0 / 100.0 * canvasHeight);
        x1 = x2 = canvasWidth / 2;

        Element verticalLine = XamlGenerator.getLine(Double.toString(x1), Double.toString(y1),
                Double.toString(x2), Double.toString(y2), "Black", "1.5");

        // horizontal line - graph lines
        incr = (y2 - y1) / 20.0;
        double x = canvasWidth / 2.0;
        for (double i = y1; i <= y2; i += incr) {
            if (i == y1 || i == canvasHeight / 2 || i == y2) {
                x1 = x + 10.0;
                x2 = x - 10.0;
            } else {
                x1 = x + 6.0;
                x2 = x - 6.0;
            }

            Element graphLine = XamlGenerator.getLine(Double.toString(x1), Double.toString(i), Double.toString(x2),
                    Double.toString(i), "Black", "1.5");
            graphLines.add(graphLine);
        }

        this.canvas.addContent(horizontalLine);
        this.canvas.addContent(verticalLine);
        this.canvas.addContent(graphLines);
    }
    private void generateStudentBlock() {
        // student name
        Element textblock = XamlGenerator.getTextBlock(this.student.getName(), "32", "Normal", "SemiBold", "Center", "Top");

        textblock.setAttribute("Grid.Column", "1");
        textblock.setAttribute("Grid.Row", "0");

        XamlGenerator.setXName(textblock, "StudentNameBlock");
        this.grid.addContent(textblock);
    }
    private void generateStudentScrollViewer() {
        Element scrollViewer = XamlGenerator.getScrollViewer();

        Element stack = XamlGenerator.getStackPanel("5", "Vertical");
        Element border;
        Element textblock;

        for (String student : this.students) {
            border = XamlGenerator.getBorder("0.5", "White", "Hand");
            textblock = XamlGenerator.getTextBlock(student, "12", "Normal", "Normal", "Center", "Center");

            textblock.setAttribute("Margin", "0,1.5,0,1");
            border.addContent(textblock);

            border.setAttribute("Margin", "0,0,0,8.5");

            Element effect = XamlGenerator.getEffect("Border");
            effect.addContent(XamlGenerator.getDropShadowEffect("15", "60", "0"));
            border.addContent(effect);

            // interactivity
            XamlGenerator.setXName(border, student.replace(" ", "_") + "_Border");
            XamlGenerator.setXName(border, student.replace(" ", "_") + "_Text_Block");
            border.setAttribute("MouseLeftButtonUp", "SelectStudent");

            stack.addContent(border);
        }

        scrollViewer.addContent(stack);
        scrollViewer.setAttribute("Grid.Column", "0");
        scrollViewer.setAttribute("Grid.Row", "1");
        XamlGenerator.setXName(scrollViewer, "StudentScroller");
        this.grid.addContent(scrollViewer);
    }

    private void fillGraphCompetences() {
        double canvasWidth = Double.parseDouble(this.canvas.getAttributeValue("Width"));
        double canvasHeight = Double.parseDouble(this.canvas.getAttributeValue("Height"));

        double centerX = canvasWidth / 2.0;
        double centerY = canvasHeight / 2.0;

        this.fillGraphMeanLines(canvasWidth, canvasHeight, centerX, centerY);
        this.fillGraphCompetenceTextBlocks(canvasWidth, canvasHeight, centerX, centerY);
    }
    private void fillGraphCompetenceTextBlocks(double canvasWidth, double canvasHeight, double centerX, double centerY) {
        double x;
        double y;

        Element textblock;

        for (Competence competence : this.competences) {
            textblock = XamlGenerator.getTextBlock(
                    competence.getName().equals("AnalyseOntwerp") ? "Analyse en Ontwerp" : competence.getName(), "40");

            switch (competence.getName()) {
                case "Communiceren":
                    x = centerX - 90.0;y = -20;
                    break;
                case "Management":
                    x = canvasWidth / 12.0;y = centerY + 10.0;
                    break;
                case "Implementatie":
                    x = centerX - 90.0;y = canvasHeight - 35.0;
                    break;
                case "AnalyseOntwerp":
                    x = canvasWidth / 1.47;y = centerY + 10.0;
                    break;
                default:
                    x = 0;y=0;break;
            }
            textblock.setAttribute("Canvas.Top", Double.toString(y));
            textblock.setAttribute("Canvas.Left", Double.toString(x));

            this.canvas.addContent(textblock);
        }
    }
    private void fillGraphMeanLines(double canvasWidth, double canvasHeight, double centerX, double centerY) {
        List<Element> lines = new ArrayList<>();
        Map<String, Double> means = new HashMap<>();

        double usedCanvasWidth = 90.0 / 100.0 * canvasWidth;
        double usedCanvasHeight = 90.0 / 100.0 * canvasHeight;

        String mng = "Management";
        String impl = "Implementatie";
        String an = "AnalyseOntwerp";
        String com = "Communiceren";

        String yellow = "Yellow";
        String yelThick = "8";
        String darkgreen = "DarkGreen";
        String dgThick = "4";

        // get scaled means, according to graph sizes
        CompetenceUtilities.scaleMeans(usedCanvasWidth, usedCanvasHeight, this.competences, means);

        // competence lines
        CompetenceUtilities.fillLine(centerX, centerY, -means.get(mng), -means.get(com),
                lines, yellow, yelThick);
        CompetenceUtilities.fillLine(centerX, centerY, -means.get(mng), means.get(impl),
                lines, yellow, yelThick);
        CompetenceUtilities.fillLine(centerX, centerY, means.get(an), means.get(impl),
                lines, yellow, yelThick);
        CompetenceUtilities.fillLine(centerX, centerY, means.get(an), -means.get(com),
                lines, yellow, yelThick);

        // student competence lines
        String visibility;
        List<Competence> competences = new ArrayList<>();
        for (String student : this.students) {
            means.clear();
            means.clear();
            competences.clear();

            // interactivity
            visibility = "Collapsed";

            for (Competence competence : this.competences) {
                competences.add(new Competence(competence.getName(), student, this.file));
            }

            CompetenceUtilities.scaleMeans(usedCanvasWidth, usedCanvasHeight, competences, means);
            CompetenceUtilities.fillLine(centerX, centerY, -means.get(mng), -means.get(com),
                    lines, darkgreen, dgThick, visibility,
                    student.replace(" ", "_") + "_" + mng + "_" + com);
            CompetenceUtilities.fillLine(centerX, centerY, -means.get(mng), means.get(impl),
                    lines, darkgreen, dgThick, visibility,
                    student.replace(" ", "_") + "_" + mng + "_" + impl);
            CompetenceUtilities.fillLine(centerX, centerY, means.get(an), means.get(impl),
                    lines, darkgreen, dgThick, visibility,
                    student.replace(" ", "_") + "_" + an + "_" + impl);
            CompetenceUtilities.fillLine(centerX, centerY, means.get(an), -means.get(com),
                    lines, darkgreen, dgThick, visibility,
                    student.replace(" ", "_") + "_" + an + "_" + com);
        }

        this.canvas.addContent(lines);
    }

    public void write() {
        // write to resources
        //XmlGenerator.writeDocument(XmlGenerator.getDocument(this.page), "CompetenceView.xaml", "xaml");
        XmlGenerator.writeCompetenceViewDocument(XmlGenerator.getDocument(this.page));
    }

    public static void main(String[] args) {
        CompetenceView cv = new CompetenceView("competences.xml");
        cv.write();
    }
}
