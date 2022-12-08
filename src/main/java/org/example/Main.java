package org.example;

import lombok.Data;

import java.util.*;
import java.util.stream.Collectors;

public class Main {

    public static double[][] calculateH(int index, double[][] nOverX, double[][] nOverY, Point[] weights,double determinant, double conductivity){
        double[][] yRow = Matrix.getRow(index, nOverY);
        double[][] yCol = Matrix.replace2dArrayDimensions(yRow);
        double[][] xRow = Matrix.getRow(index, nOverX);
        double[][] xCol = Matrix.replace2dArrayDimensions(xRow);
        double[][] yColumnMultipliedByYRow = Matrix.multiply2dArrays(yCol, yRow);
        double[][] xColumnMultipliedByXRow = Matrix.multiply2dArrays(xCol, xRow);
        double[][] H = Matrix.add2dArrays(xColumnMultipliedByXRow, yColumnMultipliedByYRow);
        H = Matrix.multiplyNumberBy2dArray(determinant * conductivity, H);
        H = Matrix.multiplyNumberBy2dArray(weights[index].x * weights[index].y, H);
        return H;
    }

    public static void main(String[] args) {
        GlobalData globalData;
        Grid grid;
        String fileName = "test.txt";

        int numberOfIntegrationPoints = 2;//liczba punktow calkowania
        int nDSF= 4;//liczba funkcji ksztaltu(nazwa zmiennej do zmiany!!!)

        DataImporter dataImporter = new DataImporter();
        dataImporter.importData(fileName);
        globalData = dataImporter.getGlobalData();
        grid = dataImporter.getGrid();

        EquationsSystem equationsSystem = new EquationsSystem(grid.getNumberOfNodes());

        Point[] weights = MathFunctions.nodesAndCoefficientsOfGaussianLagrangeQuadrature(numberOfIntegrationPoints).getCoefficientPoints();
        Point[] nodes = MathFunctions.nodesAndCoefficientsOfGaussianLagrangeQuadrature(numberOfIntegrationPoints).getNodePoints();

        UniversalElement universalElement = new UniversalElement(nodes, nDSF);
        universalElement.setNOverKsi(Matrix.transformHorizontally2dArray(universalElement.getNOverKsi()));
        universalElement.setNOverEta(Matrix.transformVertically2dArray(universalElement.getNOverEta()));

        for(int resultElementCounter = 0; resultElementCounter < grid.getElements().length; resultElementCounter++) {
            int []nodeIds = grid.getElements()[resultElementCounter].getNodeIds();
            Node[] resultNodes = new Node[4];
            for (int i = 0; i < resultNodes.length; i++){
                final int finalI = i;
                Node n = Arrays.stream(grid.getNodes()).collect(Collectors.toList()).stream()
                        .filter(node -> node.getNodeId() == nodeIds[finalI])
                        .findFirst()
                        .get();//error for null!!!
                resultNodes[i] = n;
            }

            double [][]resultH = new double[nDSF][nDSF];

            for(int resultNodesCounter = 0; resultNodesCounter < nodes.length; resultNodesCounter++) {

                double x = 0.0;
                double y = 0.0;
                for (int i = 0; i < resultNodes.length; i++) {
                    x += resultNodes[i].getX() * universalElement.getNOverKsi()[resultNodesCounter][i];
                    y += resultNodes[i].getY() * universalElement.getNOverEta()[resultNodesCounter][i];
                }
                double[][] jacobi = {{x, 0}, {0, y}};
                double determinant = jacobi[0][0] * jacobi[1][1] - jacobi[0][1] * jacobi[1][0];
                double[][] inverseJacobi = Matrix.multiplyNumberBy2dArray(1.0 / determinant, jacobi);
                double nOverX[][] = new double[nodes.length][nDSF];
                double nOverY[][] = new double[nodes.length][nDSF];
                for (int i = 0; i < nodes.length; i++) {
                    for (int j = 0; j < nDSF; j++) {
                        nOverX[i][j] = inverseJacobi[0][1] * universalElement.getNOverKsi()[i][j] + inverseJacobi[1][1] * universalElement.getNOverKsi()[i][j];
                        nOverY[i][j] = inverseJacobi[0][0] * universalElement.getNOverEta()[i][j] + inverseJacobi[1][0] * universalElement.getNOverEta()[i][j];
                    }
                }
                double[][] H = calculateH(resultNodesCounter, nOverX, nOverY, weights, determinant, globalData.getConductivity());
                resultH = Matrix.add2dArrays(resultH, H);
            }
            grid.getElements()[resultElementCounter].setH(resultH);//zapisywanie do elementow
            equationsSystem.add(grid.getElements()[resultElementCounter]);//wczytywanie do ukladu rownan
        }
        //Matrix.print2dArray(equationsSystem.getHG());

/*        container = calculateNodesAndWeights(numberOfIntegrationPoints);
        weights = container.getWeights();
        nodes = container.getNodes();
        universalElement = new UniversalElement(nodes, nDSF);*/

        //weights = container.getWeights();
        //nodes = container.getNodes();

        //DALA JEDNEJ SCIANY KTORA JEST SCIANA BRZEGOWA
        //Point[] dummyPoints = {new Point(-1,0.5773), new Point(-1,-0.5773)};//0.7886

        int numberOfIntegrationPointsOnSide = 5;// to trzeba zmienic tak zeby ustalac z mienna i na tej podstawie ustawai sie od[pwiednia ilosc punkow calkowania w tabeli

        double [] nodes2 = MathFunctions.nodesOfGaussianLagrangeQuadrature(numberOfIntegrationPointsOnSide);


        System.out.println("lelleaesdasd");
        Point[] dummyPoints = new Point[numberOfIntegrationPointsOnSide * 4];
        int [] sidesValues = {-1, 1, 1, -1};
        //                     y  x  y   x
        int counter = 0;
        //bok dol
        for (int i = 0; i < numberOfIntegrationPointsOnSide; i++) {//lece po jednym boku
            dummyPoints[counter] = new Point(nodes2[i], -1);
            counter++;
        }
        //bok prawo
        for (int i = 0; i < numberOfIntegrationPointsOnSide; i++) {//lece po jednym boku
            dummyPoints[counter] = new Point(1, nodes2[i]);
            counter++;
        }
        //bok gora
        for (int i = 0; i < numberOfIntegrationPointsOnSide; i++) {//lece po jednym boku
            dummyPoints[counter] = new Point(nodes2[i], 1);
            counter++;
        }
        //bok lewo
        for (int i = 0; i < numberOfIntegrationPointsOnSide; i++) {//lece po jednym boku
            dummyPoints[counter] = new Point(-1, nodes2[i]);
            counter++;
        }






/*        for(Point p : dummyPoints){
            System.out.println(p.x + " " + p.y);
        }

        System.out.println("-------");*/


        double[] dummyWeights = MathFunctions.coefficientsOfGaussianLagrangeQuadrature2(numberOfIntegrationPointsOnSide);
        Point[] dummyNodes = {new Point(0,0), new Point(0.025,0), new Point(0.025,0.025), new Point(0,0.025)};
        double[][] nArray = new double[numberOfIntegrationPointsOnSide][nDSF];
        double alfa = 25.0;

        for(int x = 0; x < 4; x++) {//lece 4 razy bo dla kazdego boku
            double[][] HBC = new double[nDSF][nDSF];
            //System.out.println(x);

            double detJ;
            if(x != 3){
                detJ = MathFunctions.distance(dummyNodes[x], dummyNodes[x + 1]) / 2.0;
            }
            else {
                detJ = MathFunctions.distance(dummyNodes[3], dummyNodes[0]) / 2.0;
            }
            double[][] row = new double[0][];
            double[][] transRow = new double[0][];
            for (int i = 0; i < numberOfIntegrationPointsOnSide; i++) {// i lece po jednym boku i licze dla niego nArray
                double ksi = dummyPoints[numberOfIntegrationPointsOnSide * x + i].x;
                double eta = dummyPoints[numberOfIntegrationPointsOnSide * x + i].y;
                //System.out.println(ksi + " " + eta);
                for (int j = 0; j < nDSF; j++) {
                    if (j == 0) {
                        nArray[i][j] = 0.25 * (1 - ksi) * (1 - eta);
                    } else if (j == 1) {
                        nArray[i][j] = 0.25 * (1 + ksi) * (1 - eta);
                    } else if (j == 2) {
                        nArray[i][j] = 0.25 * (1 + ksi) * (1 + eta);
                    } else if (j == 3) {
                        nArray[i][j] = 0.25 * (1 - ksi) * (1 + eta);
                    }
                }
                //Matrix.print2dArray(nArray);
                row = Matrix.getRow(i, nArray);//nArray to macierz przmnozona przez ksi eta
                transRow = Matrix.replace2dArrayDimensions(Matrix.getRow(i, nArray));
                HBC = Matrix.add2dArrays(HBC, Matrix.multiplyNumberBy2dArray(detJ, Matrix.multiplyNumberBy2dArray(alfa * dummyWeights[i], Matrix.multiply2dArrays(transRow, row))));
            }
            //System.out.println(x);
            //Matrix.print2dArray(nArray);
            Matrix.print2dArray(HBC);//dobrze wychodzi
            //tera trzeba powtorzyc dla innych bokow
        }


        /*  BUG!!! DEL WHEN FIXED
        double [][] row = Matrix.getRow(0,nArray);
        Matrix.print2dArray(row);
        double [][] transRow = Matrix.replace2dArrayDimensions(Matrix.getRow(0,nArray));
        Matrix.print2dArray(transRow);
        Matrix.print2dArray(Matrix.replace2dArrayDimensions(transRow));
        //tu jest blaad dzial tylko z row -> col, a z col -> row to tak srednio
        //+
        //bug w funkcji do transponowania (replace2dArrayDimensions) w Matrix
        */
    }
}