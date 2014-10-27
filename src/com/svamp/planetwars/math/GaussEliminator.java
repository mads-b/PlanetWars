package com.svamp.planetwars.math;

/**
 * Simple class implementing the Gauss elimination algorithm for linear systems.
 * NOTE: The A matrix used within is defined like the mathematical definition of matrices (A[rows][columns])
 * NOTE2: The matrix provided as A MUST be quadratic (n equations, n unknowns)!
 * Anything else will throw an IllegalArgumentException. This exception will also be thrown if the matrix is singular.
 */
public class GaussEliminator {
    /**
     * Solves the given linear problem. A must be nonsingular and quadratic (nxn), and b must be of n length.
     * The problem is defined as the problem Ax=b.
     * @param A nxn Problem matrix
     * @param b nx1 Column vector
     * @return Solution vector x
     * @throws IllegalArgumentException If matrix is singular, nonquadratic, or if b is not the correct size compared to argument A.
     */
    public static double[] solve(double[][] A,double[] b) throws IllegalArgumentException {
        if(A.length != A[0].length) throw new IllegalArgumentException("Matrix A is not quadratic!");
        if(A.length != b.length) throw new IllegalArgumentException("Solution vector b is not the correct size!");
        GaussEliminator.gaussElimination(A,b);
        GaussEliminator.backSubstitution(A,b);
        return b;
    }


    /**
     * Takes an arbitrary quadratic matrix A, and a solution vector b, and in-place reduces A to row-echelon form.
     * @param A nxn Problem matrix
     * @param b nx1 Column vector
     * @throws IllegalArgumentException When matrix is singular.
     */
    private static void gaussElimination(double[][] A,double[] b) throws IllegalArgumentException {
        //For every column..
        for(int k=0;k<A.length;k++) {
            //Find pivot for this row..
            int biggestRow = argMax(A,k);
            //Is this a singular matrix?
            if(A[k][biggestRow]==0) throw new IllegalArgumentException("Tried to solve a singular matrix!");
            //Put the pivot row in the top of the matrix remaining to compute
            swapRows(A,b,k,biggestRow);
            //For all rows below pivot:
            for(int i=k+1;i<A.length;i++) {
                //For all elements in the current row (excluding zeroes)
                for(int j=k+1;j<A.length;j++) {
                    A[i][j] = A[i][j] - A[k][j] * (A[i][k]/A[k][k]);
                }
                //Update b as well!
                b[i] = b[i] - b[k] * (A[i][k]/A[k][k]);
                //Fill lower triangle with nulls
                A[i][k] = 0;
            }
        }
        //Complete! A is now on row-echelon form.
    }

    /**
     * Reduces a matrix in row-echelon form to an identity matrix multiplied with the solution vector b.
     * @param A Matrix to reduce to I
     * @param b Solution vector.
     */
    private static void backSubstitution(double[][] A,double[] b) {
        //Iterate from the bottom row and up
        for(int i=A.length-1;i>=0;i--) {
            //Iterate over all columns, excluding diagonal.
            for(int j=i+1;j<A.length;j++) {
                //Subtract the following from b, current row: current column element, multiplied with its value.
                b[i] = b[i] - A[i][j] * b[j];
                //Set this matrix element to 0.
                A[i][j]=0;
            }
            //Normalize b (divide by diagonal):
            b[i] = b[i]/A[i][i];
            A[i][i] = 1;
        }
    }

    /**
     * Find the largest element in the specified column. Does not search in rows with index < colNum
     * @param A Matrix to search in
     * @param colNum Column in A to search in
     * @return Row index containing the largest elements in the column specified by colNum
     */
    private static int argMax(double[][] A,int colNum) {
        double maxVal=A[colNum][colNum];
        int maxIndex = colNum;

        for(int i=colNum;i<A.length;i++) {
            double val = Math.abs(A[i][colNum]);
            if(val>maxVal) {
                maxVal= val;
                maxIndex=i;
            }
        }
        return maxIndex;
    }

    /**
     * Swap two rows in the provided array, in-place.
     * @param A Array to swap rows in.
     * @param row1 index of first row to swap
     * @param row2 index of second row to swap
     */
    private static void swapRows(double [][] A,double[] b,int row1,int row2) {
        double[] tmp = A[row1];
        A[row1] = A[row2];
        A[row2] = tmp;

        //Swap b as well
        double tmp2 = b[row1];
        b[row1] = b[row2];
        b[row2] = tmp2;
    }
}
