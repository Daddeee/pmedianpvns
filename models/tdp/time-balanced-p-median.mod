### Params and sets ###

# number of customers
param n;
# number of periods
param t;
# number of medians
param p;

# set of customers
set V := 0..n-1;
# set of periods
set T := 0..t-1;

# distance between each pair of customers
param c {V,V} >= 0;

# release date of each customer
param r {V} >= 0, <= t - 1;
# due date of each customer
param d {V} >= 0, <= t - 1;

# average number of customers for each territory per period
param x_avg := n / (p*t);

param alpha := 0.2 * (1/(n*n)) * (sum{i in V, j in V} c[i,j]); # avg dist

### Variables ###

var x {V,V,T} binary;

var y {V,T} >= 0;

### Objective ###

minimize distance_and_displacement:
    sum {i in V, j in V, k in T} c[i,j] * x[i,j,k]
    + sum {i in V, k in T} alpha*y[i,k];

### Constraints ###

subject to exclusive_assignment {j in V}:
    sum {i in V, k in r[j] .. d[j]} x[i,j,k] = 1;

subject to number_of_medians {k in T}:
    sum {i in V} x[i,i,k] = p;

subject to assign_to_medians_only {i in V, j in V, k in T}:
    x[i,j,k] <= x[i,i,k];

subject to abs_1 {i in V, k in T}:
    sum {j in V} x[i,j,k] - x_avg*x[i,i,k] <= y[i,k];

subject to abs_2 {i in V, k in T}:
    x_avg*x[i,i,k] - sum {j in V} x[i,j,k] <= y[i,k];