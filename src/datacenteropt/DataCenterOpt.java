/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package datacenteropt;

import ilog.concert.IloException;
import ilog.concert.IloIntVar;
import ilog.concert.IloLinearIntExpr;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

/**
 *
 * @author vilela
 */
public class DataCenterOpt {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws FileNotFoundException, IOException, IloException {
        String filePath = "dc.in";
        File file = new File(filePath);

        BufferedReader br = new BufferedReader(new FileReader(file));

        String st = br.readLine();
        Integer R = Integer.parseInt(st.split(" ")[0]); //rows
        Integer S = Integer.parseInt(st.split(" ")[1]); //slots
        Integer U = Integer.parseInt(st.split(" ")[2]); //un slots
        Integer P = Integer.parseInt(st.split(" ")[3]); // pools
        Integer M = Integer.parseInt(st.split(" ")[4]); //servers
        Double BM = Double.MAX_VALUE;
        IloCplex model = new IloCplex();

        IloIntVar x[][][] = new IloIntVar[P][R][M];
        for (int i = 0; i < P; i++) {
            for (int j = 0; j < R; j++) {
                for (int k = 0; k < M; k++) {
                    x[i][j][k] = model.intVar(0, 1);
                }
            }
        }
        IloIntVar y[][][] = new IloIntVar[S][R][M];
        for (int i = 0; i < S; i++) {
            for (int j = 0; j < R; j++) {
                for (int k = 0; k < M; k++) {
                    //System.out.println(k);
                    y[i][j][k] = model.intVar(0, 1);
                }
            }
        }
        //Rest 3
        for (int i = 0; i < U; i++) {
            st = br.readLine();
            Integer ua = Integer.parseInt(st.split(" ")[0]);
            Integer ub = Integer.parseInt(st.split(" ")[1]);
            IloLinearNumExpr exp = model.linearNumExpr();
            for (int k = 0; k < M; k++) {
                exp.addTerm(1, y[ub][ua][k]);
            }
            model.addEq(exp, 0.0);
        }
        Integer[] z = new Integer[M];
        Integer[] c = new Integer[M];
        for (int i = 0; i < M; i++) {
            st = br.readLine();

            z[i] = Integer.parseInt(st.split(" ")[0]);
            c[i] = Integer.parseInt(st.split(" ")[1]);
            System.out.println(c[i]);
        }

        IloLinearNumExpr score = model.linearNumExpr();

        //rest 6
        for (int k = 0; k < M; k++) {
            IloLinearNumExpr exp = model.linearNumExpr();
            for (int i = 0; i < P; i++) {
                for (int j = 0; j < R; j++) {
                    exp.addTerm(x[i][j][k], 1);
                }
            }
            model.addEq(exp, 1);
        }

        //rest 4
        for (int j = 0; j < R; j++) {
            for (int k = 0; k < M; k++) {
                IloLinearNumExpr exp1 = model.linearNumExpr();
                IloLinearNumExpr exp1BM = model.linearNumExpr();
                IloLinearNumExpr exp2 = model.linearNumExpr();
                IloLinearNumExpr exp2BM = model.linearNumExpr();
                for (int i = 0; i < P; i++) {
                    exp1.addTerm(x[i][j][k], 1);
                    exp1BM.addTerm(x[i][j][k], BM);
                }
                for (int i = 0; i < S; i++) {
                    exp2.addTerm(y[i][j][k], 1);
                    exp2BM.addTerm(y[i][j][k], BM);
                }
                //4.1
                model.addLe(exp1, exp2BM);
                //4.2
                model.addLe(exp2, exp1BM);

            }
        }

        //rest 7
        for (int i = 0; i < P; i++) {
            for (int j = 0; j < R; j++) {
                IloLinearNumExpr exp = model.linearNumExpr();
                for (int k = 0; k < M; k++){
                    exp.addTerm(x[i][j][k], 1);
                }
                model.addLe(exp, 1);
            }
        }
        //rest 8
        for (int i = 0; i < P; i++) {
            IloLinearNumExpr exp = model.linearNumExpr();
                
            for (int j = 0; j < R; j++) {
                for (int k = 0; k < M; k++){
                    exp.addTerm(x[i][j][k], c[k]);
                }
            }
            model.addLe(exp, 535);
        }
        //rest 5
        
        for (int j = 0; j < R; j++) {
            for (int k = 0; k < M; k++) {
                IloLinearNumExpr exp1 = model.linearNumExpr();
                IloLinearNumExpr exp2 = model.linearNumExpr();
                for (int i = 0; i < S; i++) {
                    exp1.addTerm(y[i][j][k], 1);
                }
                for (int i = 0; i < P; i++) {
                    exp2.addTerm(x[i][j][k], z[k]);
                }
                //4.1
                model.addEq(exp1, exp2);

            }
        }
        
        
        
        IloLinearNumExpr gci = model.linearNumExpr();
        for (int i = 0; i < P; i++) {
            IloLinearNumExpr sum = model.linearNumExpr();

            for (int r = 0; r < R; r++) {
                IloLinearNumExpr sum1 = model.linearNumExpr();
                for (int j = 0; j < R; j++) {
                    for (int k = 0; k < M; k++) {
                        sum1.addTerm(x[i][j][k], c[k]);
                    }
                }

                IloLinearNumExpr sum2 = model.linearNumExpr();
                for (int k = 0; k < M; k++) {
                    sum1.addTerm(x[i][r][k], c[k] * -1);
                }
                sum.add(sum1);
                sum.add(sum2);
            }
            gci.add(sum);
            //Rest 2
            //model.addLe(gci, sum);
            //Rest 1
            //model.addLe(score, gci);
        }
        model.addMaximize(gci);

        if (model.solve()) {
//            ArrayList<Integer> gArr = new ArrayList<>();
//            for (int i = 0; i < P; i++) {
//                int min = 99999999;
//                ArrayList<Integer> rArr = new ArrayList<>();    
//                for (int j = 0; j < R; j++) {
//                    int sum = 0;
//                    for (int k = 0; k < M; k++) {
//                        if (model.getValue(x[i][j][k]) == 1.0) {
//                            
//                        }
//                        // = model.intVar(0, 1); 
//                    }
//                    rArr.add(sum);
//                }
//                gArr.add(rArr.get(rArr.indexOf(Collections.min(rArr))));
//            }
            ArrayList<ArrayList<Integer>> pArr = new ArrayList<>();
            ArrayList<ArrayList<Integer>> rArr = new ArrayList<>();

            for (int i = 0; i < P; i++) {
                ArrayList<Integer> pool = new ArrayList<>();

                for (int j = 0; j < R; j++) {
                    for (int k = 0; k < M; k++) {
                        if (model.getValue(x[i][j][k]) == 1.0) {
                            pool.add(k);
                        }
                        // = model.intVar(0, 1); 
                    }
                }
                pArr.add(pool);
            }

            for (int j = 0; j < R; j++) {
                ArrayList<Integer> row = new ArrayList<>();

                for (int i = 0; i < P; i++) {
                    for (int k = 0; k < M; k++) {
                        if (model.getValue(x[i][j][k]) == 1.0) {
                            row.add(k);
                        }
                        // = model.intVar(0, 1); 
                    }
                }
                rArr.add(row);
            }
            Integer min = 9999999;
            for (ArrayList<Integer> pool : pArr) {
                int sum = 0;

                int sum2 = 0;
                int minR = 99999999;

                for (int server : pool) {
                    sum += c[server];
                }
                //sum += c[server];
                for (ArrayList<Integer> row : rArr) {
                    int acVal = sum;
                    
                    for (int server : pool) {
                        if (row.contains(server)) {
                            acVal -= (c[server]);
                        }
                    }
                    if (acVal < minR) minR = acVal;

                }
                
                if (minR < min) min = minR;
            }
            System.out.println(min);
            System.out.println(model.getObjValue());
        }
        // TODO code application logic here
    }

}
